package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.service.EventSyncService;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
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
@Profile("!test")
public class ComponenteChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteChangeListener.class);
    
    @Autowired
    private EventSyncService eventSyncService;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.componentes-changes}")
    private String componentesTopic;

    /**
     * Procesa eventos de cambio de componentes con Dead Letter Topic.
     * 
     * Configuración de reintentos:
     * - 3 intentos máximo con backoff exponencial (1s, 2s, 4s)
     * - Después de 3 fallos → mensaje va al Dead Letter Topic
     * - Consumer continúa procesando siguientes mensajes (NO SE BLOQUEA)
     * 
     * @param event Evento de cambio de componente
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
        retryTopicSuffix = "-retry",
        dltTopicSuffix = "-dlt",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        include = {Exception.class}
    )
    @KafkaListener(
        topics = "${kafka.topics.componentes-changes}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleComponenteChangeEvent(@Payload ComponenteChangeEvent event) {
        
        try {
            logger.info("Procesando evento de componente: eventId={}, operationType={}, entityId={}", 
                       event.getEventId(), event.getOperationType(), event.getEntityId());
            
            // Verificar si el evento ya fue procesado (manejo de duplicados)
            if (eventSyncService.isEventProcessed(event.getEventId())) {
                logger.info("Evento ya procesado anteriormente: eventId={}, saltando procesamiento", event.getEventId());
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
            
            // Registrar métricas de procesamiento exitoso
            monitorService.recordMessageProcessed(componentesTopic);
            
            logger.info("Evento de componente procesado exitosamente: eventId={}, entityId={}", 
                       event.getEventId(), event.getEntityId());
            
        } catch (Exception e) {
            logger.error("Error procesando evento de componente: eventId={}, entityId={}, error={}", 
                        event.getEventId(), event.getEntityId(), e.getMessage(), e);
            
            // Registrar error en métricas
            monitorService.recordError(componentesTopic, e);
            
            // Re-lanzar excepción para activar RetryableTopic
            throw e;
        }
    }
    
    /**
     * Procesa la creación de un nuevo componente.
     */
    private void handleComponenteCreated(ComponenteChangeEvent event) {
        logger.info("Procesando creación de componente: id={}, descripcion={}, precio={}", 
                   event.getEntityId(), event.getDescripcion(), event.getPrecioBase());
        
        try {
            // Sincronizar componente localmente si es necesario
            eventSyncService.syncComponenteCreated(event);
            
            // Verificar impacto en pedidos existentes
            eventSyncService.validatePendingOrdersWithNewComponent(event.getEntityId());
            
            logger.debug("Componente creado registrado: id={}, tipo={}", 
                        event.getEntityId(), event.getTipoComponente());
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
                   event.getEntityId(), event.getDescripcion(), event.getPrecioBase());
        
        try {
            // Sincronizar cambios del componente
            eventSyncService.syncComponenteUpdated(event);
            
            // Verificar impacto en pedidos pendientes
            eventSyncService.validatePendingOrdersWithUpdatedComponent(event.getEntityId(), event.getPrecioBase());
            
            // Notificar cambios de precio si es relevante
            if (event.getPrecioBase() != null) {
                eventSyncService.notifyPriceChangeToOrders(event.getEntityId(), event.getPrecioBase());
            }
            
            logger.debug("Componente actualizado registrado: id={}, tipo={}", 
                        event.getEntityId(), event.getTipoComponente());
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
    
    /**
     * Maneja mensajes que fallaron después de todos los reintentos.
     * 
     * Estos mensajes se envían al Dead Letter Topic para investigación manual.
     * El consumer principal continúa procesando normalmente.
     * 
     * @param event Evento que falló después de 3 reintentos
     * @param exception Última excepción que causó el fallo
     * @param partition Partición del mensaje
     * @param offset Offset del mensaje
     * @param topic Topic original del mensaje
     */
    @DltHandler
    public void handleDltMessage(
            @Payload ComponenteChangeEvent event,
            Exception exception,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        logger.error("MENSAJE ENVIADO A DEAD LETTER TOPIC después de 3 fallos: " +
                    "topic={}, partition={}, offset={}, eventId={}, entityId={}, error={}", 
                    topic, partition, offset, event.getEventId(), event.getEntityId(), 
                    exception.getMessage(), exception);
        
        // TODO: Persistir en tabla de errores para investigación
        // TODO: Enviar alerta a equipo de desarrollo  
        // TODO: Considerar compensación manual si es crítico
        
        // Registrar métricas de mensaje enviado a DLT
        monitorService.recordError(componentesTopic + "-dlt", exception);
        
        logger.warn("Mensaje registrado en DLT. Requiere investigación manual: eventId={}, entityId={}", 
                   event.getEventId(), event.getEntityId());
    }
}