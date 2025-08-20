package mx.com.qtx.cotizador.kafka.service;

import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.PedidoChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.ProveedorChangeEvent;
import mx.com.qtx.cotizador.entidad.Pedido;
import mx.com.qtx.cotizador.entidad.Proveedor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Servicio para envío de eventos Kafka desde ms-cotizador-pedidos.
 * 
 * Maneja el envío de eventos de cambio para pedidos y proveedores
 * hacia otros microservicios del sistema, implementando:
 * - Envío asíncrono con callbacks
 * - Manejo de errores y reintentos
 * - Logging estructurado de eventos
 * - Sincronización bidireccional
 * 
 * @author Subagente4E - [2025-08-17 11:40:00 MST] - Servicio productor de eventos Kafka para ms-cotizador-pedidos
 */
@Service
@Profile("!test")
public class EventProducerService {

    private static final Logger logger = LoggerFactory.getLogger(EventProducerService.class);
    
    @Autowired
    private KafkaTemplate<String, BaseChangeEvent> kafkaTemplate;
    
    @Autowired
    private KafkaMonitorService monitorService;
    
    @Value("${kafka.topics.pedidos-changes}")
    private String pedidosTopic;
    
    @Value("${kafka.topics.proveedores-changes}")
    private String proveedoresTopic;
    
    // === EVENTOS DE PEDIDOS ===
    
    /**
     * Envía evento de pedido creado
     * @param pedidoId ID del pedido creado
     * @param proveedorCve Clave del proveedor
     */
    public void sendPedidoCreated(Integer pedidoId, String proveedorCve) {
        logger.info("Enviando evento de pedido creado: pedidoId={}, proveedor={}", 
                   pedidoId, proveedorCve);
        
        try {
            PedidoChangeEvent event = new PedidoChangeEvent(BaseChangeEvent.OperationType.CREATE, 
                                                          Long.valueOf(pedidoId));
            event.setProveedorCve(proveedorCve);
            sendEventAsync(pedidosTopic, pedidoId.toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de pedido creado: {}", e.getMessage(), e);
            monitorService.recordError(pedidosTopic, e);
        }
    }
    
    /**
     * Envía evento de pedido cancelado
     * @param pedidoId ID del pedido cancelado
     * @param proveedorCve Clave del proveedor
     */
    public void sendPedidoCancelled(Integer pedidoId, String proveedorCve) {
        logger.info("Enviando evento de pedido cancelado: pedidoId={}, proveedor={}", 
                   pedidoId, proveedorCve);
        
        try {
            PedidoChangeEvent event = new PedidoChangeEvent(BaseChangeEvent.OperationType.DELETE, 
                                                          Long.valueOf(pedidoId));
            event.setProveedorCve(proveedorCve);
            sendEventAsync(pedidosTopic, pedidoId.toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de pedido cancelado: {}", e.getMessage(), e);
            monitorService.recordError(pedidosTopic, e);
        }
    }
    
    /**
     * Envía evento de creación de pedido.
     * 
     * @param pedido Pedido creado
     */
    public void sendPedidoCreatedEvent(Pedido pedido) {
        logger.info("Enviando evento de creación de pedido: id={}", 
                   pedido.getNumPedido());
        
        try {
            PedidoChangeEvent event = createPedidoChangeEvent(BaseChangeEvent.OperationType.CREATE, pedido);
            sendEventAsync(pedidosTopic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de creación de pedido: {}", e.getMessage(), e);
            monitorService.recordError(pedidosTopic, e);
        }
    }
    
    /**
     * Envía evento de actualización de pedido.
     * 
     * @param pedido Pedido actualizado
     */
    public void sendPedidoUpdatedEvent(Pedido pedido) {
        logger.info("Enviando evento de actualización de pedido: id={}", 
                   pedido.getNumPedido());
        
        try {
            PedidoChangeEvent event = createPedidoChangeEvent(BaseChangeEvent.OperationType.UPDATE, pedido);
            sendEventAsync(pedidosTopic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de actualización de pedido: {}", e.getMessage(), e);
            monitorService.recordError(pedidosTopic, e);
        }
    }
    
    /**
     * Envía evento de eliminación de pedido.
     * 
     * @param pedidoId ID del pedido eliminado
     */
    public void sendPedidoDeletedEvent(Long pedidoId) {
        logger.info("Enviando evento de eliminación de pedido: id={}", pedidoId);
        
        try {
            PedidoChangeEvent event = new PedidoChangeEvent(BaseChangeEvent.OperationType.DELETE, pedidoId);
            sendEventAsync(pedidosTopic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de eliminación de pedido: {}", e.getMessage(), e);
            monitorService.recordError(pedidosTopic, e);
        }
    }
    
    // === EVENTOS DE PROVEEDORES ===
    
    /**
     * Envía evento de creación de proveedor.
     * 
     * @param proveedor Proveedor creado
     */
    public void sendProveedorCreatedEvent(Proveedor proveedor) {
        logger.info("Enviando evento de creación de proveedor: cve={}, nombre={}", 
                   proveedor.getCve(), proveedor.getNombre());
        
        try {
            ProveedorChangeEvent event = createProveedorChangeEvent(BaseChangeEvent.OperationType.CREATE, proveedor);
            sendEventAsync(proveedoresTopic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de creación de proveedor: {}", e.getMessage(), e);
            monitorService.recordError(proveedoresTopic, e);
        }
    }
    
    /**
     * Envía evento de actualización de proveedor.
     * 
     * @param proveedor Proveedor actualizado
     */
    public void sendProveedorUpdatedEvent(Proveedor proveedor) {
        logger.info("Enviando evento de actualización de proveedor: cve={}, nombre={}", 
                   proveedor.getCve(), proveedor.getNombre());
        
        try {
            ProveedorChangeEvent event = createProveedorChangeEvent(BaseChangeEvent.OperationType.UPDATE, proveedor);
            sendEventAsync(proveedoresTopic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de actualización de proveedor: {}", e.getMessage(), e);
            monitorService.recordError(proveedoresTopic, e);
        }
    }
    
    /**
     * Envía evento de eliminación de proveedor.
     * 
     * @param proveedorId ID del proveedor eliminado
     */
    public void sendProveedorDeletedEvent(Long proveedorId) {
        logger.info("Enviando evento de eliminación de proveedor: id={}", proveedorId);
        
        try {
            ProveedorChangeEvent event = new ProveedorChangeEvent(BaseChangeEvent.OperationType.DELETE, String.valueOf(proveedorId));
            sendEventAsync(proveedoresTopic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de eliminación de proveedor: {}", e.getMessage(), e);
            monitorService.recordError(proveedoresTopic, e);
        }
    }
    
    // === MÉTODOS DE APOYO ===
    
    /**
     * Envía evento de forma asíncrona con manejo de callbacks.
     * 
     * @param topic Topic de destino
     * @param key Clave del mensaje
     * @param event Evento a enviar
     */
    private void sendEventAsync(String topic, String key, BaseChangeEvent event) {
        logger.debug("Enviando evento: topic={}, key={}, eventId={}, type={}", 
                    topic, key, event.getEventId(), event.getEventType());
        
        CompletableFuture<SendResult<String, BaseChangeEvent>> future = 
            kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("Error enviando evento: topic={}, eventId={}, error={}", 
                           topic, event.getEventId(), throwable.getMessage(), throwable);
                monitorService.recordError(topic, new RuntimeException(throwable));
            } else {
                logger.debug("Evento enviado exitosamente: topic={}, eventId={}, partition={}, offset={}", 
                           topic, event.getEventId(), 
                           result.getRecordMetadata().partition(), 
                           result.getRecordMetadata().offset());
                monitorService.recordMessageProcessed(topic + ".sent");
            }
        });
    }
    
    /**
     * Crea evento de cambio para pedido.
     * 
     * @param operationType Tipo de operación
     * @param pedido Pedido
     * @return Evento de cambio de pedido
     */
    private PedidoChangeEvent createPedidoChangeEvent(BaseChangeEvent.OperationType operationType, Pedido pedido) {
        return new PedidoChangeEvent(
            operationType,
            pedido.getNumPedido().longValue(),
            null, // No hay cotizacion en esta entidad
            null, // No hay cliente en esta entidad
            null, // No hay estado en esta entidad
            pedido.getTotal(),
            pedido.getFechaEmision().atStartOfDay(),
            pedido.getFechaEntrega().atStartOfDay(),
            null  // No hay observaciones en esta entidad
        );
    }
    
    /**
     * Crea evento de cambio para proveedor.
     * 
     * @param operationType Tipo de operación
     * @param proveedor Proveedor
     * @return Evento de cambio de proveedor
     */
    private ProveedorChangeEvent createProveedorChangeEvent(BaseChangeEvent.OperationType operationType, Proveedor proveedor) {
        return new ProveedorChangeEvent(
            operationType,
            proveedor.getCve(),
            proveedor.getNombre(),
            null, // No hay telefono en la entidad
            null, // No hay email en la entidad
            null, // No hay contacto en la entidad
            null, // No hay direccion en la entidad
            null, // No hay ciudad en la entidad
            null, // No hay estado en la entidad
            null, // No hay pais en la entidad
            null, // No hay codigoPostal en la entidad
            true  // Asumir activo por defecto
        );
    }
    
    /**
     * Envía evento de sincronización para resolver conflictos.
     * 
     * @param topic Topic de destino
     * @param event Evento de sincronización
     */
    public void sendSyncEvent(String topic, BaseChangeEvent event) {
        logger.info("Enviando evento de sincronización: topic={}, eventId={}, type={}", 
                   topic, event.getEventId(), event.getEventType());
        
        try {
            sendEventAsync(topic, event.getEntityId().toString(), event);
        } catch (Exception e) {
            logger.error("Error enviando evento de sincronización: {}", e.getMessage(), e);
            monitorService.recordError(topic, e);
        }
    }
}