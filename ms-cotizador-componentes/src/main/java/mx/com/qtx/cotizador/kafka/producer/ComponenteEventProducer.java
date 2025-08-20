package mx.com.qtx.cotizador.kafka.producer;

import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Productor de eventos Kafka para el microservicio de componentes.
 * 
 * Maneja el envío de eventos de cambio para componentes, promociones y PCs
 * con retry logic, logging detallado y manejo de errores.
 * 
 * @author Subagente2E - [2025-01-17 16:55:00 MST] - Productor de eventos Kafka
 */
@Component
public class ComponenteEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.producer.enabled:true}")
    private boolean kafkaEnabled;

    @Value("${kafka.monitoring.log-events:true}")
    private boolean logEvents;

    @Value("${kafka.topics.componentes-changes}")
    private String componentesChangesTopic;

    @Value("${kafka.topics.promociones-changes}")
    private String promocionesChangesTopic;
    
    @Value("${kafka.topics.pcs-changes}")
    private String pcsChangesTopic;

    /**
     * Envía un evento de cambio de componente al topic correspondiente.
     * 
     * @param event Evento de cambio a enviar
     * @return CompletableFuture con el resultado del envío
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<SendResult<String, Object>> sendComponenteChangeEvent(BaseChangeEvent event) {
        if (!kafkaEnabled) {
            logger.debug("Kafka está deshabilitado. Evento no enviado: {}", event.getEventId());
            return CompletableFuture.completedFuture(null);
        }

        String topicName = determineTopicName(event);
        String eventKey = generateEventKey(event);

        if (logEvents) {
            logger.info("Enviando evento Kafka - Topic: {}, Key: {}, EventId: {}, Type: {}, Operation: {}, EntityId: {}",
                    topicName, eventKey, event.getEventId(), event.getEventType(), 
                    event.getOperationType(), event.getEntityId());
        }

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, eventKey, event);

        // Configurar callbacks para success/failure
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                if (logEvents) {
                    logger.info("Evento Kafka enviado exitosamente - EventId: {}, Partition: {}, Offset: {}",
                            event.getEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            } else {
                logger.error("Error enviando evento Kafka - EventId: {}, Topic: {}, Error: {}",
                        event.getEventId(), topicName, exception.getMessage(), exception);
            }
        });

        return future;
    }

    /**
     * Envía un evento de cambio de promoción al topic correspondiente.
     * 
     * @param event Evento de cambio a enviar
     * @return CompletableFuture con el resultado del envío
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<SendResult<String, Object>> sendPromocionChangeEvent(BaseChangeEvent event) {
        return sendComponenteChangeEvent(event); // Usa la misma lógica
    }

    /**
     * Envía un evento de cambio de PC al topic de componentes (ya que los PCs están compuestos por componentes).
     * 
     * @param event Evento de cambio a enviar
     * @return CompletableFuture con el resultado del envío
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<SendResult<String, Object>> sendPcChangeEvent(BaseChangeEvent event) {
        return sendComponenteChangeEvent(event); // Usa la misma lógica
    }

    /**
     * Determina el topic apropiado basado en el tipo de evento.
     * 
     * @param event Evento a procesar
     * @return Nombre del topic
     */
    private String determineTopicName(BaseChangeEvent event) {
        return switch (event.getEventType()) {
            case COMPONENTE_CHANGE -> componentesChangesTopic;
            case PC_CHANGE -> pcsChangesTopic;
            case PROMOCION_CHANGE -> promocionesChangesTopic;
            default -> componentesChangesTopic;
        };
    }

    /**
     * Genera una clave única para el evento basada en el tipo y ID de entidad.
     * 
     * @param event Evento a procesar
     * @return Clave del evento
     */
    private String generateEventKey(BaseChangeEvent event) {
        return String.format("%s_%s_%s",
                event.getEventType().name(),
                event.getOperationType().name(),
                event.getEntityId());
    }

    /**
     * Método de respaldo cuando fallan todos los reintentos.
     * 
     * @param event Evento que falló
     * @param ex Excepción que causó el fallo
     */
    public void handleFailedEvent(BaseChangeEvent event, Exception ex) {
        logger.error("FALLO CRÍTICO: No se pudo enviar evento después de todos los reintentos - " +
                "EventId: {}, Type: {}, Operation: {}, EntityId: {}, Error: {}",
                event.getEventId(), event.getEventType(), event.getOperationType(),
                event.getEntityId(), ex.getMessage(), ex);

        // Aquí podrías implementar una cola de eventos fallidos o alertas
        // Por ejemplo, guardar en base de datos para reintento posterior
    }

    /**
     * Verifica si Kafka está habilitado.
     * 
     * @return true si Kafka está habilitado
     */
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }
}