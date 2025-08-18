package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;
import mx.com.qtx.cotizador.servicio.cache.ComponenteCacheService;
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
 * para mantener sincronizada la información local necesaria para cotizaciones.
 * 
 * @author Subagente3E - [2025-01-17 18:50:00 MST] - Listener de eventos de componentes
 */
@Component
public class ComponenteChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteChangeListener.class);
    
    @Autowired
    private CotizacionServicio cotizacionServicio;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Autowired
    private ComponenteCacheService componenteCacheService;
    
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
     */
    private void handleComponenteCreated(ComponenteChangeEvent event) {
        logger.info("Procesando creación de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        // Invalidar caché de componentes para forzar recarga del nuevo componente
        componenteCacheService.invalidarComponente(String.valueOf(event.getEntityId()));
        
        // Pre-cargar el nuevo componente en cache si está activo
        if (Boolean.TRUE.equals(event.getActivo())) {
            componenteCacheService.precargarComponente(String.valueOf(event.getEntityId()));
        }
        
        logger.debug("Componente creado registrado: id={}, tipo={}, activo={}", 
                    event.getEntityId(), event.getTipoComponente(), event.getActivo());
    }
    
    /**
     * Procesa la actualización de un componente existente.
     */
    private void handleComponenteUpdated(ComponenteChangeEvent event) {
        logger.info("Procesando actualización de componente: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        // Invalidar caché de componentes para forzar recarga con datos actualizados
        componenteCacheService.invalidarComponente(String.valueOf(event.getEntityId()));
        
        // Pre-cargar el componente actualizado en cache si está activo
        if (Boolean.TRUE.equals(event.getActivo())) {
            componenteCacheService.precargarComponente(String.valueOf(event.getEntityId()));
        }
        
        // TODO: Verificar si hay cotizaciones activas que usen este componente
        // TODO: Notificar cambios de precio para recálculo de cotizaciones si es necesario
        
        logger.debug("Componente actualizado registrado: id={}, tipo={}, activo={}", 
                    event.getEntityId(), event.getTipoComponente(), event.getActivo());
    }
    
    /**
     * Procesa la eliminación de un componente.
     */
    private void handleComponenteDeleted(ComponenteChangeEvent event) {
        logger.info("Procesando eliminación de componente: id={}", event.getEntityId());
        
        // Invalidar caché de componentes para remover el componente eliminado
        componenteCacheService.invalidarComponente(String.valueOf(event.getEntityId()));
        
        // TODO: Marcar como inactivo en cotizaciones existentes
        // TODO: Notificar si hay cotizaciones afectadas que requieran recálculo
        
        logger.debug("Componente eliminado registrado: id={}", event.getEntityId());
    }
}