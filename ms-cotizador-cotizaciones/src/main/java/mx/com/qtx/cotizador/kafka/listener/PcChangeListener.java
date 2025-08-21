package mx.com.qtx.cotizador.kafka.listener;

import mx.com.qtx.cotizador.kafka.dto.PcChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import mx.com.qtx.cotizador.repositorio.PcRepositorio;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.entidad.Promocion;
import java.math.BigDecimal;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private PcRepositorio pcRepositorio;
    
    @Autowired
    private ComponenteRepositorio componenteRepositorio;
    
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
                case ADD_COMPONENT:
                    handlePcComponentAdded(event);
                    break;
                case REMOVE_COMPONENT:
                    handlePcComponentRemoved(event);
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
    @Transactional
    private void handlePcCreated(PcChangeEvent event) {
        logger.info("Procesando creación de PC: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            String pcId = event.getEntityId().toString();
            
            // 1. CREAR la PC como componente si no existe
            if (!componenteRepositorio.existsById(pcId)) {
                // Crear PC como componente (necesario para foreign key)
                Componente pcComponente = new Componente();
                pcComponente.setId(pcId);
                pcComponente.setDescripcion(event.getNombre() != null ? event.getNombre() : "PC Completa");
                pcComponente.setCosto(event.getPrecio() != null ? BigDecimal.valueOf(event.getPrecio()) : BigDecimal.ZERO);
                pcComponente.setPrecioBase(event.getPrecio() != null ? BigDecimal.valueOf(event.getPrecio()) : BigDecimal.ZERO);
                pcComponente.setMarca("PC Ensamblada");
                pcComponente.setModelo(pcId);
                
                // Asignar tipo de componente "PC" (id = 1)
                TipoComponente tipoPC = new TipoComponente();
                tipoPC.setId((short) 1);
                pcComponente.setTipoComponente(tipoPC);
                
                // Asignar promoción usando el promocionId del evento
                if (event.getPromocionId() != null) {
                    Promocion promocion = new Promocion();
                    promocion.setIdPromocion(event.getPromocionId());
                    pcComponente.setPromocion(promocion);
                } else {
                    // Fallback a promoción Regular (id = 1) si no se especifica
                    Promocion promocionDefault = new Promocion();
                    promocionDefault.setIdPromocion(1);
                    pcComponente.setPromocion(promocionDefault);
                }
                
                componenteRepositorio.save(pcComponente);
                logger.info("PC {} creada como componente en cocomponente", pcId);
            }
            
            // 2. Eliminar relaciones existentes por idempotencia
            pcRepositorio.deleteByIdPc(pcId);
            
            // Si el evento incluye componenteIds, crear las relaciones
            if (event.getComponenteIds() != null && !event.getComponenteIds().isEmpty()) {
                for (String componenteId : event.getComponenteIds()) {
                    
                    // Verificar que el componente existe localmente antes de crear la relación
                    if (componenteRepositorio.existsById(componenteId)) {
                        PcParte pcParte = new PcParte(pcId, componenteId);
                        pcRepositorio.save(pcParte);
                        logger.debug("Relación PC-Componente creada: PC={}, Componente={}", pcId, componenteId);
                    } else {
                        logger.warn("Componente {} no encontrado localmente para PC {}, saltando relación", componenteId, pcId);
                    }
                }
                
                logger.info("PC creada y sincronizada: id={}, relaciones creadas={}", 
                           pcId, event.getComponenteIds().size());
            } else {
                logger.info("PC registrada sin componentes en evento: id={}, nombre={}, precio={}, activa={}", 
                           pcId, event.getNombre(), event.getPrecio(), event.getActiva());
            }
            
        } catch (Exception e) {
            logger.error("Error procesando creación de PC: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la actualización de una PC existente.
     */
    @Transactional
    private void handlePcUpdated(PcChangeEvent event) {
        logger.info("Procesando actualización de PC: id={}, nombre={}, precio={}", 
                   event.getEntityId(), event.getNombre(), event.getPrecio());
        
        try {
            String pcId = event.getEntityId().toString();
            
            // Actualizar relaciones PC-Componente si se incluyen en el evento
            if (event.getComponenteIds() != null && !event.getComponenteIds().isEmpty()) {
                // Eliminar relaciones existentes
                pcRepositorio.deleteByIdPc(pcId);
                
                // Crear nuevas relaciones
                for (String componenteId : event.getComponenteIds()) {
                    
                    if (componenteRepositorio.existsById(componenteId)) {
                        PcParte pcParte = new PcParte(pcId, componenteId);
                        pcRepositorio.save(pcParte);
                        logger.debug("Relación PC-Componente actualizada: PC={}, Componente={}", pcId, componenteId);
                    } else {
                        logger.warn("Componente {} no encontrado localmente para PC actualizada {}", componenteId, pcId);
                    }
                }
                
                logger.info("PC actualizada y sincronizada: id={}, relaciones actualizadas={}", 
                           pcId, event.getComponenteIds().size());
            } else {
                logger.info("PC actualizada sin componentes en evento: id={}, nombre={}, precio={}, activa={}", 
                           pcId, event.getNombre(), event.getPrecio(), event.getActiva());
            }
            
            // TODO: Verificar impacto en cotizaciones existentes que usen esta PC
            if (event.getPrecio() != null) {
                logger.info("Cambio de precio en PC: {} -> precio={}", pcId, event.getPrecio());
            }
            
        } catch (Exception e) {
            logger.error("Error procesando actualización de PC: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la eliminación de una PC.
     */
    @Transactional
    private void handlePcDeleted(PcChangeEvent event) {
        logger.info("Procesando eliminación de PC: id={}", event.getEntityId());
        
        try {
            String pcId = event.getEntityId().toString();
            
            // Eliminar relaciones PC-Componente de esta PC
            pcRepositorio.deleteByIdPc(pcId);
            
            logger.info("PC eliminada y relaciones removidas: id={}", pcId);
            
            // TODO: Verificar cotizaciones afectadas por la eliminación
            
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
    
    /**
     * Procesa la adición de un componente a una PC existente.
     */
    @Transactional
    private void handlePcComponentAdded(PcChangeEvent event) {
        logger.info("Procesando agregado de componente a PC: pcId={}, componenteIds={}", 
                   event.getEntityId(), event.getComponenteIds());
        
        try {
            String pcId = event.getEntityId().toString();
            
            // Si el evento incluye componenteIds, crear las relaciones
            if (event.getComponenteIds() != null && !event.getComponenteIds().isEmpty()) {
                for (String componenteId : event.getComponenteIds()) {
                    
                    // Verificar que el componente existe localmente antes de crear la relación
                    if (componenteRepositorio.existsById(componenteId)) {
                        // Verificar si la relación ya existe (idempotencia)
                        if (!pcRepositorio.existsByPcId(pcId) || 
                            pcRepositorio.findByPcId(pcId).stream()
                                .noneMatch(pc -> pc.getIdComponente().equals(componenteId))) {
                            
                            PcParte pcParte = new PcParte(pcId, componenteId);
                            pcRepositorio.save(pcParte);
                            logger.info("Componente agregado a PC: PC={}, Componente={}", pcId, componenteId);
                        } else {
                            logger.debug("Componente {} ya asociado a PC {}, operación idempotente", componenteId, pcId);
                        }
                    } else {
                        logger.warn("Componente {} no encontrado localmente para agregar a PC {}", componenteId, pcId);
                    }
                }
            } else {
                logger.warn("Evento ADD_COMPONENT sin componenteIds: pcId={}", pcId);
            }
            
        } catch (Exception e) {
            logger.error("Error procesando agregado de componente a PC: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Procesa la remoción de un componente de una PC existente.
     */
    @Transactional
    private void handlePcComponentRemoved(PcChangeEvent event) {
        logger.info("Procesando remoción de componente de PC: pcId={}, componenteIds={}", 
                   event.getEntityId(), event.getComponenteIds());
        
        try {
            String pcId = event.getEntityId().toString();
            
            // Si el evento incluye componenteIds, eliminar las relaciones específicas
            if (event.getComponenteIds() != null && !event.getComponenteIds().isEmpty()) {
                for (String componenteId : event.getComponenteIds()) {
                    
                    // Buscar la relación específica y eliminarla
                    List<PcParte> relaciones = pcRepositorio.findByPcId(pcId);
                    relaciones.stream()
                        .filter(pc -> pc.getIdComponente().equals(componenteId))
                        .forEach(pcRepositorio::delete);
                        
                    logger.info("Componente removido de PC: PC={}, Componente={}", pcId, componenteId);
                }
            } else {
                logger.warn("Evento REMOVE_COMPONENT sin componenteIds: pcId={}", pcId);
            }
            
        } catch (Exception e) {
            logger.error("Error procesando remoción de componente de PC: {}", e.getMessage(), e);
            throw e;
        }
    }
}