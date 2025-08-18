package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
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
 * Listener para eventos de cambio de componentes desde ms-cotizador-componentes.
 * 
 * Procesa eventos de creación, actualización y eliminación de componentes
 * para mantener sincronizada la información local necesaria para gestión de pedidos.
 * 
 * @author Subagente4E - [2025-08-17 11:10:00 MST] - Listener de eventos de componentes para ms-cotizador-pedidos
 */
@Component
public class ComponenteChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteChangeListener.class);
    
    @Autowired
    private EventSyncService eventSyncService;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.componentes-changes}")
    private String componentesTopic;

    /**
     * Procesa eventos de cambio de componentes.
     * 
     * @param event Evento de cambio de componente
     * @param partition Partición del mensaje
     * @param offset Offset del mensaje
     * @param key Clave del mensaje
     * @param acknowledgment Acknowledgment para confirmar procesamiento
     */
    @KafkaListener(
        topics = "${kafka.topics.componentes-changes}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void handleComponenteChangeEvent(
            @Payload ComponenteChangeEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Procesando evento de componente: eventId={}, operationType={}, entityId={}, partition={}, offset={}", 
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
                    handleComponenteCreated(event);
                    break;
                case UPDATE:
                    handleComponenteUpdated(event);
                    break;
                case DELETE:
                    handleComponenteDeleted(event);
                    break;
                default:
                    logger.warn("Tipo de operación no reconocido: {}", event.getOperationType());
            }
            
            // Marcar evento como procesado
            eventSyncService.markEventAsProcessed(event.getEventId(), event.getSource());
            
            // Confirmar procesamiento exitoso
            acknowledgment.acknowledge();
            
            // Registrar métricas de procesamiento exitoso
            monitorService.recordMessageProcessed(componentesTopic);
            
            logger.info("Evento de componente procesado exitosamente: eventId={}, entityId={}", 
                       event.getEventId(), event.getEntityId());
            
        } catch (Exception e) {
            logger.error("Error procesando evento de componente: eventId={}, entityId={}, error={}", 
                        event.getEventId(), event.getEntityId(), e.getMessage(), e);
            
            // Registrar error en métricas
            monitorService.recordError(componentesTopic, e);
            
            // No hacer acknowledge para que el mensaje sea reprocessado
            throw e;
        }
    }
    
    /**
     * Procesa la creación de un nuevo componente.
     */
    private void handleComponenteCreated(ComponenteChangeEvent event) {
        logger.info("Procesando creación de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            // Sincronizar componente localmente si es necesario
            eventSyncService.syncComponenteCreated(event);
            
            // Verificar impacto en pedidos existentes
            eventSyncService.validatePendingOrdersWithNewComponent(event.getEntityId());
            
            logger.debug("Componente creado registrado: id={}, tipo={}, activo={}", 
                        event.getEntityId(), event.getTipoComponente(), event.getActivo());
        } catch (Exception e) {
            logger.error("Error procesando creación de componente: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la actualización de un componente existente.
     */
    private void handleComponenteUpdated(ComponenteChangeEvent event) {
        logger.info("Procesando actualización de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            // Sincronizar cambios del componente
            eventSyncService.syncComponenteUpdated(event);
            
            // Verificar impacto en pedidos pendientes
            eventSyncService.validatePendingOrdersWithUpdatedComponent(event.getEntityId(), event.getPrecio());
            
            // Notificar cambios de precio si es relevante
            if (event.getPrecio() != null) {
                eventSyncService.notifyPriceChangeToOrders(event.getEntityId(), event.getPrecio());
            }
            
            logger.debug("Componente actualizado registrado: id={}, tipo={}, activo={}", 
                        event.getEntityId(), event.getTipoComponente(), event.getActivo());
        } catch (Exception e) {
            logger.error("Error procesando actualización de componente: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la eliminación de un componente.
     */
    private void handleComponenteDeleted(ComponenteChangeEvent event) {
        logger.info("Procesando eliminación de componente: id={}", event.getEntityId());
        
        try {
            // Marcar componente como inactivo localmente
            eventSyncService.syncComponenteDeleted(event);
            
            // Verificar pedidos afectados por la eliminación
            eventSyncService.handleOrdersWithDeletedComponent(event.getEntityId());
            
            logger.debug("Componente eliminado registrado: id={}", event.getEntityId());
        } catch (Exception e) {
            logger.error("Error procesando eliminación de componente: {}", e.getMessage(), e);
            throw e;
        }
    }
}