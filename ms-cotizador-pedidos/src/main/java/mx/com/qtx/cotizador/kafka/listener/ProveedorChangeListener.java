package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.ProveedorChangeEvent;
import mx.com.qtx.cotizador.kafka.service.EventSyncService;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
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
 * Listener para eventos de cambio de proveedores desde otros microservicios.
 * 
 * Procesa eventos de creación, actualización y eliminación de proveedores
 * para mantener sincronizada la información necesaria para gestión de pedidos.
 * 
 * @author Subagente4E - [2025-08-17 11:20:00 MST] - Listener de eventos de proveedores para ms-cotizador-pedidos
 */
@Component
public class ProveedorChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorChangeListener.class);
    
    @Autowired
    private EventSyncService eventSyncService;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.proveedores-changes}")
    private String proveedoresTopic;

    /**
     * Procesa eventos de cambio de proveedores.
     * 
     * @param event Evento de cambio de proveedor
     * @param partition Partición del mensaje
     * @param offset Offset del mensaje
     * @param key Clave del mensaje
     * @param acknowledgment Acknowledgment para confirmar procesamiento
     */
    @KafkaListener(
        topics = "${kafka.topics.proveedores-changes}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void handleProveedorChangeEvent(
            @Payload ProveedorChangeEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Procesando evento de proveedor: eventId={}, operationType={}, entityId={}, partition={}, offset={}", 
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
                    handleProveedorCreated(event);
                    break;
                case UPDATE:
                    handleProveedorUpdated(event);
                    break;
                case DELETE:
                    handleProveedorDeleted(event);
                    break;
                default:
                    logger.warn("Tipo de operación no reconocido: {}", event.getOperationType());
            }
            
            // Marcar evento como procesado
            eventSyncService.markEventAsProcessed(event.getEventId(), event.getSource());
            
            // Confirmar procesamiento exitoso
            acknowledgment.acknowledge();
            
            // Registrar métricas de procesamiento exitoso
            monitorService.recordMessageProcessed(proveedoresTopic);
            
            logger.info("Evento de proveedor procesado exitosamente: eventId={}, entityId={}", 
                       event.getEventId(), event.getEntityId());
            
        } catch (Exception e) {
            logger.error("Error procesando evento de proveedor: eventId={}, entityId={}, error={}", 
                        event.getEventId(), event.getEntityId(), e.getMessage(), e);
            
            // Registrar error en métricas
            monitorService.recordError(proveedoresTopic, e);
            
            // No hacer acknowledge para que el mensaje sea reprocessado
            throw e;
        }
    }
    
    /**
     * Procesa la creación de un nuevo proveedor.
     */
    private void handleProveedorCreated(ProveedorChangeEvent event) {
        logger.info("Procesando creación de proveedor: id={}, nombre={}, email={}", 
                   event.getEntityId(), event.getNombre(), event.getEmail());
        
        try {
            // Sincronizar proveedor localmente
            eventSyncService.syncProveedorCreated(event);
            
            // Evaluar oportunidades de pedidos con el nuevo proveedor
            eventSyncService.evaluateOrderOpportunitiesWithNewProvider(event.getEntityId());
            
            logger.debug("Proveedor creado registrado: id={}, activo={}, pais={}", 
                        event.getEntityId(), event.getActivo(), event.getPais());
        } catch (Exception e) {
            logger.error("Error procesando creación de proveedor: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la actualización de un proveedor existente.
     */
    private void handleProveedorUpdated(ProveedorChangeEvent event) {
        logger.info("Procesando actualización de proveedor: id={}, nombre={}, email={}", 
                   event.getEntityId(), event.getNombre(), event.getEmail());
        
        try {
            // Sincronizar cambios del proveedor
            eventSyncService.syncProveedorUpdated(event);
            
            // Verificar impacto en pedidos existentes
            eventSyncService.validatePendingOrdersWithUpdatedProvider(event.getEntityId());
            
            // Verificar cambios en estado activo
            if (Boolean.FALSE.equals(event.getActivo())) {
                eventSyncService.handleProviderDeactivation(event.getEntityId());
            } else if (Boolean.TRUE.equals(event.getActivo())) {
                eventSyncService.handleProviderActivation(event.getEntityId());
            }
            
            // Verificar cambios en información de contacto que afecten pedidos
            if (event.getEmail() != null || event.getTelefono() != null) {
                eventSyncService.updateProviderContactInOrders(event.getEntityId(), event.getEmail(), event.getTelefono());
            }
            
            logger.debug("Proveedor actualizado registrado: id={}, activo={}, pais={}", 
                        event.getEntityId(), event.getActivo(), event.getPais());
        } catch (Exception e) {
            logger.error("Error procesando actualización de proveedor: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la eliminación de un proveedor.
     */
    private void handleProveedorDeleted(ProveedorChangeEvent event) {
        logger.info("Procesando eliminación de proveedor: id={}", event.getEntityId());
        
        try {
            // Marcar proveedor como eliminado localmente
            eventSyncService.syncProveedorDeleted(event);
            
            // Verificar pedidos afectados por la eliminación del proveedor
            eventSyncService.handleOrdersWithDeletedProvider(event.getEntityId());
            
            // Notificar sobre componentes huérfanos (sin proveedor)
            eventSyncService.handleOrphanedComponentsFromDeletedProvider(event.getEntityId());
            
            logger.debug("Proveedor eliminado registrado: id={}", event.getEntityId());
        } catch (Exception e) {
            logger.error("Error procesando eliminación de proveedor: {}", e.getMessage(), e);
            throw e;
        }
    }
}