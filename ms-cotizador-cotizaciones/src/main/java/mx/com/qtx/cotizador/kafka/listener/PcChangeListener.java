package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.PcChangeEvent;
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
 * Listener para eventos de cambio de PCs desde ms-cotizador-componentes.
 * 
 * Procesa eventos de creación, actualización y eliminación de PCs
 * para mantener sincronizada la información local necesaria para cotizaciones.
 * 
 * @author Claude - [2025-08-20 13:00:00 MST] - Listener de eventos de PC para ms-cotizador-cotizaciones
 */
@Component
@Profile("!test")
public class PcChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(PcChangeListener.class);
    
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.pcs-changes}")
    private String pcsChangesTopic;

    /**
     * Procesa eventos de cambio de PCs con Dead Letter Topic.
     * 
     * @param event Evento de cambio de PC
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
        topics = "${kafka.topics.pcs-changes}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePcChangeEvent(@Payload PcChangeEvent event) {
        
        try {
            logger.info("Procesando evento de PC: eventId={}, operationType={}, entityId={}", 
                       event.getEventId(), event.getOperationType(), event.getEntityId());
            
            // TODO: Implementar verificación de eventos duplicados
            
            // Procesar según el tipo de operación
            switch (event.getOperationType()) {
                case CREATE:
                    handlePcCreated(event);
                    break;
                case UPDATE:
                    handlePcUpdated(event);
                    break;
                case DELETE:
                    handlePcDeleted(event);
                    break;
                default:
                    logger.warn("Tipo de operación no reconocido: {}", event.getOperationType());
            }
            
            // TODO: Marcar evento como procesado
            
            // Registrar métricas de procesamiento exitoso
            monitorService.recordMessageProcessed(pcsChangesTopic);
            
            logger.info("Evento de PC procesado exitosamente: eventId={}, entityId={}", 
                       event.getEventId(), event.getEntityId());
            
        } catch (Exception e) {
            logger.error("Error procesando evento de PC: eventId={}, entityId={}, error={}", 
                        event.getEventId(), event.getEntityId(), e.getMessage(), e);
            
            // Registrar error en métricas
            monitorService.recordError(pcsChangesTopic, e);
            
            // Re-lanzar excepción para activar RetryableTopic
            throw e;
        }
    }
    
    /**
     * Procesa la creación de una nueva PC.
     */
    private void handlePcCreated(PcChangeEvent event) {
        logger.info("Procesando creación de PC: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            // TODO: Sincronizar PC localmente para cotizaciones
            logger.info("PC creada recibida: {}", event.getEntityId());
            
            logger.debug("PC creada registrada: id={}, activa={}", 
                        event.getEntityId(), event.getActiva());
        } catch (Exception e) {
            logger.error("Error procesando creación de PC: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la actualización de una PC existente.
     */
    private void handlePcUpdated(PcChangeEvent event) {
        logger.info("Procesando actualización de PC: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            // TODO: Sincronizar cambios de la PC
            logger.info("PC actualizada recibida: {}", event.getEntityId());
            
            // TODO: Verificar impacto en cotizaciones existentes
            if (event.getPrecio() != null) {
                logger.info("Cambio de precio en PC: {} -> {}", event.getEntityId(), event.getPrecio());
            }
            
            logger.debug("PC actualizada registrada: id={}, activa={}", 
                        event.getEntityId(), event.getActiva());
        } catch (Exception e) {
            logger.error("Error procesando actualización de PC: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la eliminación de una PC.
     */
    private void handlePcDeleted(PcChangeEvent event) {
        logger.info("Procesando eliminación de PC: id={}", event.getEntityId());
        
        try {
            // TODO: Marcar PC como inactiva localmente
            logger.info("PC eliminada recibida: {}", event.getEntityId());
            
            // TODO: Verificar cotizaciones afectadas por la eliminación
            
            logger.debug("PC eliminada registrada: id={}", event.getEntityId());
        } catch (Exception e) {
            logger.error("Error procesando eliminación de PC: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Maneja mensajes que fallaron después de todos los reintentos.
     */
    @DltHandler
    public void handleDltMessage(
            @Payload PcChangeEvent event,
            Exception exception,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        logger.error("MENSAJE ENVIADO A DEAD LETTER TOPIC después de 3 fallos: " +
                    "topic={}, partition={}, offset={}, eventId={}, entityId={}, error={}", 
                    topic, partition, offset, event.getEventId(), event.getEntityId(), 
                    exception.getMessage(), exception);
        
        // Registrar métricas de mensaje enviado a DLT
        monitorService.recordError(pcsChangesTopic + "-dlt", exception);
        
        logger.warn("Mensaje registrado en DLT. Requiere investigación manual: eventId={}, entityId={}", 
                   event.getEventId(), event.getEntityId());
    }
}