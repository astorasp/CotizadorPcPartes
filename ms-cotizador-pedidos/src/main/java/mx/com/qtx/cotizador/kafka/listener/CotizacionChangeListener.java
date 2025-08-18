package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.servicio.wrapper.CotizacionEventConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de cambio de cotizaciones desde ms-cotizador-cotizaciones.
 * 
 * Procesa eventos de creación, actualización y eliminación de cotizaciones
 * para mantener sincronizada la información local en la base de datos del microservicio.
 * Siguiendo el mismo patrón que ComponenteChangeListener con persistencia directa en BD.
 * 
 * Se desactiva automáticamente en tests cuando kafka.consumer.enabled=false.
 */
@Component
@ConditionalOnProperty(value = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class CotizacionChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(CotizacionChangeListener.class);
    
    @Autowired
    private CotizacionRepositorio cotizacionRepositorio;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.cotizaciones-changes}")
    private String cotizacionesTopic;

    /**
     * Procesa eventos de cambio de cotizaciones.
     * 
     * @param event Evento de cambio de cotización
     * @param acknowledgment Acknowledgment para confirmar procesamiento
     */
    @KafkaListener(
        topics = "${kafka.topics.cotizaciones-changes}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void handleCotizacionChangeEvent(
            @Payload CotizacionChangeEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Procesando evento de cotización: eventId={}, operationType={}, entityId={}", 
                       event.getEventId(), event.getOperationType(), event.getEntityId());
            
            // Validar evento antes de procesarlo
            if (!CotizacionEventConverter.isValidEvent(event)) {
                logger.warn("Evento de cotización inválido recibido: eventId={}, entityId={}", 
                           event.getEventId(), event.getEntityId());
                acknowledgment.acknowledge();
                return;
            }
            
            // Procesar según el tipo de operación
            switch (event.getOperationType()) {
                case CREATE:
                    handleCotizacionCreated(event);
                    break;
                case UPDATE:
                    handleCotizacionUpdated(event);
                    break;
                case DELETE:
                    handleCotizacionDeleted(event);
                    break;
                default:
                    logger.warn("Tipo de operación no reconocido: {}", event.getOperationType());
            }
            
            // Confirmar procesamiento exitoso
            acknowledgment.acknowledge();
            
            // Registrar métricas de procesamiento exitoso
            monitorService.recordMessageProcessed(cotizacionesTopic);
            
            logger.info("Evento de cotización procesado exitosamente: eventId={}, entityId={}", 
                       event.getEventId(), event.getEntityId());
            
        } catch (Exception e) {
            logger.error("Error procesando evento de cotización: eventId={}, entityId={}, error={}", 
                        event.getEventId(), event.getEntityId(), e.getMessage(), e);
            
            // Registrar error en métricas
            monitorService.recordError(cotizacionesTopic, e);
            
            // No hacer acknowledge para que el mensaje sea reprocessado
            throw e;
        }
    }
    
    /**
     * Procesa la creación de una nueva cotización.
     * Persistir la nueva cotización en la base de datos local.
     */
    private void handleCotizacionCreated(CotizacionChangeEvent event) {
        logger.info("Procesando creación de cotización: id={}, cliente={}, montoTotal={}", 
                   event.getEntityId(), event.getCliente(), event.getMontoTotal());
        
        try {
            // Convertir evento a entidad y persistir
            Cotizacion cotizacion = CotizacionEventConverter.toEntity(event);
            cotizacionRepositorio.save(cotizacion);
            
            String additionalInfo = CotizacionEventConverter.extractAdditionalInfo(event);
            logger.info("Cotización creada y persistida localmente: id={}, info=[{}]", 
                       event.getEntityId(), additionalInfo);
                       
        } catch (Exception e) {
            logger.error("Error persistiendo cotización creada: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
    
    /**
     * Procesa la actualización de una cotización existente.
     * Actualiza la cotización en la base de datos local.
     */
    private void handleCotizacionUpdated(CotizacionChangeEvent event) {
        logger.info("Procesando actualización de cotización: id={}, cliente={}, estado={}, montoTotal={}", 
                   event.getEntityId(), event.getCliente(), event.getEstado(), event.getMontoTotal());
        
        try {
            // Convertir evento a entidad y persistir (save hace upsert por ID)  
            Cotizacion cotizacion = CotizacionEventConverter.toEntity(event);
            cotizacionRepositorio.save(cotizacion);
            
            String additionalInfo = CotizacionEventConverter.extractAdditionalInfo(event);
            logger.info("Cotización actualizada y persistida localmente: id={}, info=[{}]", 
                       event.getEntityId(), additionalInfo);
                       
        } catch (Exception e) {
            logger.error("Error persistiendo cotización actualizada: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
    
    /**
     * Procesa la eliminación de una cotización.
     * Elimina la cotización de la base de datos local.
     */
    private void handleCotizacionDeleted(CotizacionChangeEvent event) {
        logger.info("Procesando eliminación de cotización: id={}", event.getEntityId());
        
        try {
            Integer cotizacionId = Integer.parseInt(event.getEntityId());
            
            // Verificar si la cotización existe antes de eliminarla
            if (cotizacionRepositorio.existsById(cotizacionId)) {
                cotizacionRepositorio.deleteById(cotizacionId);
                logger.info("Cotización eliminada de la base de datos local: id={}", cotizacionId);
            } else {
                logger.warn("Cotización a eliminar no encontrada en BD local: id={}", cotizacionId);
            }
            
        } catch (Exception e) {
            logger.error("Error eliminando cotización: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
}