package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.PromocionChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.servicio.promocion.PromocionServicio;
import mx.com.qtx.cotizador.servicio.cache.PromocionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de cambio de promociones desde ms-cotizador-componentes.
 * 
 * Procesa eventos de creación, actualización y eliminación de promociones
 * para mantener sincronizada la información local necesaria para cotizaciones.
 * 
 * @author Subagente3E - [2025-01-17 18:55:00 MST] - Listener de eventos de promociones
 */
@Component
public class PromocionChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(PromocionChangeListener.class);
    
    @Autowired
    private PromocionServicio promocionServicio;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Autowired
    private PromocionCacheService promocionCacheService;
    
    @Value("${kafka.topics.promociones-changes}")
    private String promocionesTopic;

    /**
     * Procesa eventos de cambio de promociones.
     * 
     * @param event Evento de cambio de promoción
     * @param partition Partición del mensaje
     * @param offset Offset del mensaje
     * @param key Clave del mensaje
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
     */
    private void handlePromocionCreated(PromocionChangeEvent event) {
        logger.info("Procesando creación de promoción: id={}, nombre={}, tipo={}, activa={}", 
                   event.getEntityId(), event.getNombre(), event.getTipoPromocion(), event.getActiva());
        
        // Invalidar caché de promociones para incluir la nueva promoción
        promocionCacheService.invalidarPromocion(event.getEntityId().intValue());
        promocionCacheService.invalidarPromocionesActivas();
        
        // Pre-cargar promociones activas si la nueva promoción está activa
        if (Boolean.TRUE.equals(event.getActiva())) {
            promocionCacheService.precargarPromocionesActivas();
        }
        
        // TODO: Evaluar si puede aplicarse a cotizaciones existentes
        
        logger.debug("Promoción creada registrada: id={}, fechaInicio={}, fechaFin={}, valorDescuento={}", 
                    event.getEntityId(), event.getFechaInicio(), event.getFechaFin(), event.getValorDescuento());
    }
    
    /**
     * Procesa la actualización de una promoción existente.
     */
    private void handlePromocionUpdated(PromocionChangeEvent event) {
        logger.info("Procesando actualización de promoción: id={}, nombre={}, tipo={}, activa={}", 
                   event.getEntityId(), event.getNombre(), event.getTipoPromocion(), event.getActiva());
        
        // Invalidar caché de promociones para forzar recarga con datos actualizados
        promocionCacheService.invalidarPromocion(event.getEntityId().intValue());
        promocionCacheService.invalidarPromocionesActivas();
        
        // Pre-cargar promociones activas si la promoción está activa
        if (Boolean.TRUE.equals(event.getActiva())) {
            promocionCacheService.precargarPromocionesActivas();
        }
        
        // TODO: Recalcular cotizaciones que usen esta promoción
        // TODO: Actualizar descuentos en cotizaciones activas si es necesario
        
        logger.debug("Promoción actualizada registrada: id={}, fechaInicio={}, fechaFin={}, valorDescuento={}", 
                    event.getEntityId(), event.getFechaInicio(), event.getFechaFin(), event.getValorDescuento());
    }
    
    /**
     * Procesa la eliminación de una promoción.
     */
    private void handlePromocionDeleted(PromocionChangeEvent event) {
        logger.info("Procesando eliminación de promoción: id={}", event.getEntityId());
        
        // Invalidar caché de promociones para remover la promoción eliminada
        promocionCacheService.invalidarPromocion(event.getEntityId().intValue());
        promocionCacheService.invalidarPromocionesActivas();
        
        // TODO: Remover promoción de cotizaciones activas
        // TODO: Recalcular totales de cotizaciones afectadas
        
        logger.debug("Promoción eliminada registrada: id={}", event.getEntityId());
    }
}