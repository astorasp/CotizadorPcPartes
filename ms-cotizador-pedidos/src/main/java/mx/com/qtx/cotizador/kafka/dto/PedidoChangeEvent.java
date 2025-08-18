package mx.com.qtx.cotizador.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de cambio para entidades de Pedido.
 * 
 * Se envía cuando se crea, actualiza o elimina un pedido en el sistema.
 * Contiene información del pedido afectado para sincronización con otros microservicios.
 * 
 * @author Subagente4E - [2025-08-17 10:55:00 MST] - Evento de cambio para pedidos en ms-cotizador-pedidos
 */
public class PedidoChangeEvent extends BaseChangeEvent {
    
    private Long cotizacionId;
    private String cliente;
    private String estado;
    private BigDecimal montoTotal;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntregaEstimada;
    private String observaciones;
    private String proveedorCve;
    
    /**
     * Constructor por defecto
     */
    public PedidoChangeEvent() {
        super();
        setEventType(EventType.PEDIDO_CHANGE);
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public PedidoChangeEvent(OperationType operationType, Long entityId) {
        super(EventType.PEDIDO_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public PedidoChangeEvent(OperationType operationType, Long entityId, Long cotizacionId, 
                            String cliente, String estado, BigDecimal montoTotal, 
                            LocalDateTime fechaPedido, LocalDateTime fechaEntregaEstimada, String observaciones) {
        this(operationType, entityId);
        this.cotizacionId = cotizacionId;
        this.cliente = cliente;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.fechaPedido = fechaPedido;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public Long getCotizacionId() {
        return cotizacionId;
    }
    
    public void setCotizacionId(Long cotizacionId) {
        this.cotizacionId = cotizacionId;
    }
    
    public String getCliente() {
        return cliente;
    }
    
    public void setCliente(String cliente) {
        this.cliente = cliente;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
    
    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
    
    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }
    
    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }
    
    public LocalDateTime getFechaEntregaEstimada() {
        return fechaEntregaEstimada;
    }
    
    public void setFechaEntregaEstimada(LocalDateTime fechaEntregaEstimada) {
        this.fechaEntregaEstimada = fechaEntregaEstimada;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public String getProveedorCve() {
        return proveedorCve;
    }
    
    public void setProveedorCve(String proveedorCve) {
        this.proveedorCve = proveedorCve;
    }
}