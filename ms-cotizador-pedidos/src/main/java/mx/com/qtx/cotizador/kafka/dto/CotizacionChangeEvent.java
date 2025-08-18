package mx.com.qtx.cotizador.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de cambio para entidades de Cotización.
 * 
 * Se envía cuando se crea, actualiza o elimina una cotización en el sistema.
 * Contiene información de la cotización afectada para sincronización con el microservicio de pedidos.
 * 
 * @author Subagente4E - [2025-08-17 10:45:00 MST] - Evento de cambio para cotizaciones en ms-cotizador-pedidos
 */
public class CotizacionChangeEvent extends BaseChangeEvent {
    
    private String cliente;
    private String descripcion;
    private String estado;
    private BigDecimal montoTotal;
    private BigDecimal montoDscto;
    private BigDecimal montoImpuestos;
    private String pais;
    private LocalDateTime fechaCotizacion;
    private String algoritmo;
    
    /**
     * Constructor por defecto
     */
    public CotizacionChangeEvent() {
        super();
        setEventType(EventType.COTIZACION_CHANGE);
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public CotizacionChangeEvent(OperationType operationType, Long entityId) {
        super(EventType.COTIZACION_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public CotizacionChangeEvent(OperationType operationType, Long entityId, String cliente, 
                                String descripcion, String estado, BigDecimal montoTotal, 
                                BigDecimal montoDscto, BigDecimal montoImpuestos, String pais, 
                                LocalDateTime fechaCotizacion, String algoritmo) {
        this(operationType, entityId);
        this.cliente = cliente;
        this.descripcion = descripcion;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.montoDscto = montoDscto;
        this.montoImpuestos = montoImpuestos;
        this.pais = pais;
        this.fechaCotizacion = fechaCotizacion;
        this.algoritmo = algoritmo;
    }
    
    // Getters y Setters
    public String getCliente() {
        return cliente;
    }
    
    public void setCliente(String cliente) {
        this.cliente = cliente;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
    
    public BigDecimal getMontoDscto() {
        return montoDscto;
    }
    
    public void setMontoDscto(BigDecimal montoDscto) {
        this.montoDscto = montoDscto;
    }
    
    public BigDecimal getMontoImpuestos() {
        return montoImpuestos;
    }
    
    public void setMontoImpuestos(BigDecimal montoImpuestos) {
        this.montoImpuestos = montoImpuestos;
    }
    
    public String getPais() {
        return pais;
    }
    
    public void setPais(String pais) {
        this.pais = pais;
    }
    
    public LocalDateTime getFechaCotizacion() {
        return fechaCotizacion;
    }
    
    public void setFechaCotizacion(LocalDateTime fechaCotizacion) {
        this.fechaCotizacion = fechaCotizacion;
    }
    
    public String getAlgoritmo() {
        return algoritmo;
    }
    
    public void setAlgoritmo(String algoritmo) {
        this.algoritmo = algoritmo;
    }
}