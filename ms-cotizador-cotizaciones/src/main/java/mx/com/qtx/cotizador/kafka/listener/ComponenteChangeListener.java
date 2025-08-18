package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.servicio.wrapper.ComponenteEventConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de cambio de componentes desde ms-cotizador-componentes.
 * 
 * Procesa eventos de creación, actualización y eliminación de componentes
 * para mantener sincronizada la información local en la base de datos del microservicio.
 * Reemplaza la arquitectura de cache por persistencia directa en BD local.
 */
@Component
public class ComponenteChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteChangeListener.class);
    
    @Autowired
    private ComponenteRepositorio componenteRepositorio;
    
    @Autowired
    private TipoComponenteRepositorio tipoComponenteRepositorio;
    
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
            Acknowledgment acknowledgment) {
        
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
     * Persistir el nuevo componente en la base de datos local.
     */
    private void handleComponenteCreated(ComponenteChangeEvent event) {
        logger.info("Procesando creación de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            // Buscar el tipo de componente correspondiente
            String tipoNombre = ComponenteEventConverter.mapearTipoComponente(event.getTipoComponente());
            TipoComponente tipoComponente = tipoComponenteRepositorio.findByNombre(tipoNombre);
            
            if (tipoComponente == null) {
                logger.warn("Tipo de componente no encontrado: {}. Creando componente sin tipo.", tipoNombre);
            }
            
            // Convertir evento a entidad y persistir
            Componente componente = ComponenteEventConverter.toEntity(event, tipoComponente);
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
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            // Buscar el tipo de componente correspondiente
            String tipoNombre = ComponenteEventConverter.mapearTipoComponente(event.getTipoComponente());
            TipoComponente tipoComponente = tipoComponenteRepositorio.findByNombre(tipoNombre);
            
            if (tipoComponente == null) {
                logger.warn("Tipo de componente no encontrado: {}. Actualizando componente sin tipo.", tipoNombre);
            }
            
            // Convertir evento a entidad y persistir (save hace upsert por ID)
            Componente componente = ComponenteEventConverter.toEntity(event, tipoComponente);
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