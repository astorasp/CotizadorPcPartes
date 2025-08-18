package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import mx.com.qtx.cotizador.kafka.service.EventSyncService;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de cambio de cotizaciones desde ms-cotizador-cotizaciones.
 * 
 * Procesa eventos de creación, actualización y eliminación de cotizaciones
 * para mantener sincronizada la información necesaria para gestión de pedidos.
 * 
 * @author Subagente4E - [2025-08-17 11:15:00 MST] - Listener de eventos de cotizaciones para ms-cotizador-pedidos
 */
@Component
@Profile("!test")
public class CotizacionChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(CotizacionChangeListener.class);
    
    @Autowired
    private EventSyncService eventSyncService;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.cotizaciones-changes}")
    private String cotizacionesTopic;

    /**
     * Procesa eventos de cambio de cotizaciones.
     * 
     * @param event Evento de cambio de cotización
     * @param partition Partición del mensaje
     * @param offset Offset del mensaje
     * @param key Clave del mensaje
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
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Procesando evento de cotización: eventId={}, operationType={}, entityId={}, partition={}, offset={}", 
                       event.getEventId(), event.getOperationType(), event.getEntityId(), partition, offset);
            
            // Verificar si el evento ya fue procesado (manejo de duplicados)
            if (eventSyncService.isEventProcessed(event.getEventId())) {
                logger.info("Evento ya procesado anteriormente: eventId={}, saltando procesamiento", event.getEventId());
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
            
            // Marcar evento como procesado
            eventSyncService.markEventAsProcessed(event.getEventId(), event.getSource());
            
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
     */
    private void handleCotizacionCreated(CotizacionChangeEvent event) {
        logger.info("Procesando creación de cotización: id={}, cliente={}, montoTotal={}", 
                   event.getEntityId(), event.getCliente(), event.getMontoTotal());
        
        try {
            // Sincronizar cotización localmente
            eventSyncService.syncCotizacionCreated(event);
            
            // Verificar si la cotización está lista para convertir a pedido
            if ("APROBADA".equals(event.getEstado())) {
                eventSyncService.evaluateQuotationForOrder(event.getEntityId());
            }
            
            logger.debug("Cotización creada registrada: id={}, estado={}, algoritmo={}", 
                        event.getEntityId(), event.getEstado(), event.getAlgoritmo());
        } catch (Exception e) {
            logger.error("Error procesando creación de cotización: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la actualización de una cotización existente.
     */
    private void handleCotizacionUpdated(CotizacionChangeEvent event) {
        logger.info("Procesando actualización de cotización: id={}, cliente={}, estado={}, montoTotal={}", 
                   event.getEntityId(), event.getCliente(), event.getEstado(), event.getMontoTotal());
        
        try {
            // Sincronizar cambios de la cotización
            eventSyncService.syncCotizacionUpdated(event);
            
            // Verificar cambios de estado que afecten pedidos
            if ("APROBADA".equals(event.getEstado())) {
                eventSyncService.handleQuotationApproved(event.getEntityId());
            } else if ("RECHAZADA".equals(event.getEstado()) || "CANCELADA".equals(event.getEstado())) {
                eventSyncService.handleQuotationCancelled(event.getEntityId());
            }
            
            // Verificar cambios en montos que afecten pedidos existentes
            if (event.getMontoTotal() != null) {
                eventSyncService.validateOrderAmountChanges(event.getEntityId(), event.getMontoTotal());
            }
            
            logger.debug("Cotización actualizada registrada: id={}, estado={}, montoTotal={}", 
                        event.getEntityId(), event.getEstado(), event.getMontoTotal());
        } catch (Exception e) {
            logger.error("Error procesando actualización de cotización: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la eliminación de una cotización.
     */
    private void handleCotizacionDeleted(CotizacionChangeEvent event) {
        logger.info("Procesando eliminación de cotización: id={}", event.getEntityId());
        
        try {
            // Marcar cotización como eliminada localmente
            eventSyncService.syncCotizacionDeleted(event);
            
            // Verificar pedidos relacionados con esta cotización
            eventSyncService.handleOrdersWithDeletedQuotation(event.getEntityId());
            
            logger.debug("Cotización eliminada registrada: id={}", event.getEntityId());
        } catch (Exception e) {
            logger.error("Error procesando eliminación de cotización: {}", e.getMessage(), e);
            throw e;
        }
    }
}