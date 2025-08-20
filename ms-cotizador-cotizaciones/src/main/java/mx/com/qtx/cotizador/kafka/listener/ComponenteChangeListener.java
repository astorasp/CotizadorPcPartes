package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.servicio.wrapper.ComponenteEventConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de cambio de componentes desde ms-cotizador-componentes.
 * 
 * Procesa eventos de creación, actualización y eliminación de componentes
 * para mantener sincronizada la información local en la base de datos del microservicio.
 * Reemplaza la arquitectura de cache por persistencia directa en BD local.
 * 
 * Se desactiva automáticamente en tests cuando kafka.consumer.enabled=false.
 */
@Component
@ConditionalOnProperty(value = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
@Profile("!test")
public class ComponenteChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteChangeListener.class);
    
    @Autowired
    private ComponenteRepositorio componenteRepositorio;
    
    @Autowired
    private TipoComponenteRepositorio tipoComponenteRepositorio;
    
    @Autowired
    private PromocionRepositorio promocionRepositorio;
    
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
    
    /**
     * Procesa la creación de un nuevo componente.
     * Persistir el nuevo componente en la base de datos local.
     */
    private void handleComponenteCreated(ComponenteChangeEvent event) {
        logger.info("Procesando creación de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecioBase());
        
        try {
            // Buscar el tipo de componente correspondiente
            String tipoNombre = ComponenteEventConverter.mapearTipoComponente(event.getTipoComponente());
            TipoComponente tipoComponente = tipoComponenteRepositorio.findByNombre(tipoNombre);
            
            if (tipoComponente == null) {
                logger.warn("Tipo de componente no encontrado: {}. Creando componente sin tipo.", tipoNombre);
            }
            
            // Convertir evento a entidad y persistir
            Componente componente = ComponenteEventConverter.toEntity(event, tipoComponente, promocionRepositorio);
            componenteRepositorio.save(componente);
            
            logger.info("Componente creado y persistido localmente: id={}, tipo={}", 
                       event.getEntityId(), tipoNombre);
                       
        } catch (Exception e) {
            logger.error("Error persistiendo componente creado: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
    
    /**
     * Procesa la actualización de un componente existente.
     * Actualiza el componente en la base de datos local.
     */
    private void handleComponenteUpdated(ComponenteChangeEvent event) {
        logger.info("Procesando actualización de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecioBase());
        
        try {
            // Buscar el tipo de componente correspondiente
            String tipoNombre = ComponenteEventConverter.mapearTipoComponente(event.getTipoComponente());
            TipoComponente tipoComponente = tipoComponenteRepositorio.findByNombre(tipoNombre);
            
            if (tipoComponente == null) {
                logger.warn("Tipo de componente no encontrado: {}. Actualizando componente sin tipo.", tipoNombre);
            }
            
            // Convertir evento a entidad y persistir (save hace upsert por ID)
            Componente componente = ComponenteEventConverter.toEntity(event, tipoComponente, promocionRepositorio);
            componenteRepositorio.save(componente);
            
            logger.info("Componente actualizado y persistido localmente: id={}, tipo={}", 
                       event.getEntityId(), tipoNombre);
                       
            // TODO: Verificar si hay cotizaciones activas que usen este componente
            // TODO: Notificar cambios de precio para recálculo de cotizaciones si es necesario
                       
        } catch (Exception e) {
            logger.error("Error persistiendo componente actualizado: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
    
    /**
     * Procesa la eliminación de un componente.
     * Elimina el componente de la base de datos local.
     */
    private void handleComponenteDeleted(ComponenteChangeEvent event) {
        logger.info("Procesando eliminación de componente: id={}", event.getEntityId());
        
        try {
            String componenteId = event.getEntityId().toString();
            
            // Verificar si el componente existe antes de eliminarlo
            if (componenteRepositorio.existsById(componenteId)) {
                componenteRepositorio.deleteById(componenteId);
                logger.info("Componente eliminado de la base de datos local: id={}", componenteId);
            } else {
                logger.warn("Componente a eliminar no encontrado en BD local: id={}", componenteId);
            }
            
            // TODO: Marcar como inactivo en cotizaciones existentes
            // TODO: Notificar si hay cotizaciones afectadas que requieran recálculo
            
        } catch (Exception e) {
            logger.error("Error eliminando componente: id={}, error={}", 
                        event.getEntityId(), e.getMessage(), e);
            throw e; // Re-lanzar para trigger retry
        }
    }
}