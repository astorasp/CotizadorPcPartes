package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.PromocionChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.servicio.wrapper.PromocionEventConverter;
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
 * Listener para eventos de cambio de promociones desde ms-cotizador-componentes.
 * 
 * Procesa eventos de creación, actualización y eliminación de promociones
 * para mantener sincronizada la información local en la base de datos del microservicio.
 * Siguiendo el mismo patrón que ComponenteChangeListener con persistencia directa en BD.
 * 
 * Se desactiva automáticamente en tests cuando kafka.consumer.enabled=false.
 */
@Component
@ConditionalOnProperty(value = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class PromocionChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(PromocionChangeListener.class);
    
    @Autowired
    private PromocionRepositorio promocionRepositorio;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.promociones-changes}")
    private String promocionesTopic;

    /**
     * Procesa eventos de cambio de promociones.
     * 
     * @param event Evento de cambio de promoción
     * @param acknowledgment Acknowledgment para confirmar procesamiento
     */
    @KafkaListener(
        topics = "${kafka.topics.promociones-changes}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void handlePromocionChangeEvent(
            @Payload PromocionChangeEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Procesando evento de promoción: eventId={}, operationType={}, entityId={}", 
                       event.getEventId(), event.getOperationType(), event.getEntityId());
            
            // Validar evento antes de procesarlo
            if (!PromocionEventConverter.isValidEvent(event)) {
                logger.warn("Evento de promoción inválido recibido: eventId={}, entityId={}", 
                           event.getEventId(), event.getEntityId());
                acknowledgment.acknowledge();
                return;
            }
            
            // Procesar según el tipo de operación
            switch (event.getOperationType()) {
                case CREATE:
                    handlePromocionCreated(event);
                    break;
                case UPDATE:
                    handlePromocionUpdated(event);
                    break;
                case DELETE:
                    handlePromocionDeleted(event);
                    break;
                default:
                    logger.warn("Tipo de operación no reconocido: {}", event.getOperationType());
            }
            
            // Confirmar procesamiento exitoso
            acknowledgment.acknowledge();
            
            // Registrar métricas de procesamiento exitoso
            monitorService.recordMessageProcessed(promocionesTopic);
            
            logger.info("Evento de promoción procesado exitosamente: eventId={}, entityId={}", 
                       event.getEventId(), event.getEntityId());
            
        } catch (Exception e) {
            logger.error("Error procesando evento de promoción: eventId={}, entityId={}, error={}", 
                        event.getEventId(), event.getEntityId(), e.getMessage(), e);
            
            // Registrar error en métricas
            monitorService.recordError(promocionesTopic, e);
            
            // No hacer acknowledge para que el mensaje sea reprocessado
            throw e;
        }
    }
    
    /**
     * Procesa la creación de una nueva promoción.
     * Persistir la nueva promoción en la base de datos local.
     */
    private void handlePromocionCreated(PromocionChangeEvent event) {
        logger.info("Procesando creación de promoción: id={}, nombre={}", 
                   event.getEntityId(), event.getNombre());
        
        try {
            // Convertir evento a entidad y persistir
            Promocion promocion = PromocionEventConverter.toEntity(event);
            promocionRepositorio.save(promocion);
            
            String additionalInfo = PromocionEventConverter.extractAdditionalInfo(event);
            logger.info("Promoción creada y persistida localmente: id={}, nombre={}, info=[{}]", 
                       event.getEntityId(), event.getNombre(), additionalInfo);
                       
        } catch (Exception e) {
            logger.error("Error persistiendo promoción creada: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
    
    /**
     * Procesa la actualización de una promoción existente.
     * Actualiza la promoción en la base de datos local.
     */
    private void handlePromocionUpdated(PromocionChangeEvent event) {
        logger.info("Procesando actualización de promoción: id={}, nombre={}", 
                   event.getEntityId(), event.getNombre());
        
        try {
            // Convertir evento a entidad y persistir (save hace upsert por ID)
            Promocion promocion = PromocionEventConverter.toEntity(event);
            promocionRepositorio.save(promocion);
            
            String additionalInfo = PromocionEventConverter.extractAdditionalInfo(event);
            logger.info("Promoción actualizada y persistida localmente: id={}, nombre={}, info=[{}]", 
                       event.getEntityId(), event.getNombre(), additionalInfo);
                       
        } catch (Exception e) {
            logger.error("Error persistiendo promoción actualizada: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
    
    /**
     * Procesa la eliminación de una promoción.
     * Elimina la promoción de la base de datos local.
     */
    private void handlePromocionDeleted(PromocionChangeEvent event) {
        logger.info("Procesando eliminación de promoción: id={}", event.getEntityId());
        
        try {
            Integer promocionId = Integer.valueOf(event.getEntityId());
            
            // Verificar si la promoción existe antes de eliminarla
            if (promocionRepositorio.existsById(promocionId)) {
                promocionRepositorio.deleteById(promocionId);
                logger.info("Promoción eliminada de la base de datos local: id={}", promocionId);
            } else {
                logger.warn("Promoción a eliminar no encontrada en BD local: id={}", promocionId);
            }
            
        } catch (NumberFormatException e) {
            logger.error("ID de promoción inválido para eliminación: id={}", event.getEntityId());
            throw e;
        } catch (Exception e) {
            logger.error("Error eliminando promoción: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
}