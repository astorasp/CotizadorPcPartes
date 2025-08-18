package mx.com.qtx.cotizador.kafka.dto;

import java.time.LocalDateTime;

/**
 * Evento de cambio para entidades de Promocion.
 * 
 * Se envía cuando se crea, actualiza o elimina una promoción en el sistema.
 * Contiene información de la promoción afectada para sincronización con otros microservicios.
 * 
 * @author Subagente3E - [2025-01-17 18:40:00 MST] - Evento de cambio para promociones en consumidor
 */
public class PromocionChangeEvent extends BaseChangeEvent {
    
    private String nombre;
    private String descripcion;
    private String tipoPromocion;
    private String tipoPromocionAcumulable;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activa;
    private Double valorDescuento;
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    
    /**
     * Constructor por defecto
     */
    public PromocionChangeEvent() {
        super();
        setEventType(EventType.PROMOCION_CHANGE);
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public PromocionChangeEvent(OperationType operationType, Long entityId) {
        super(EventType.PROMOCION_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public PromocionChangeEvent(OperationType operationType, Long entityId, String nombre, 
                               String descripcion, String tipoPromocion, String tipoPromocionAcumulable,
                               LocalDateTime fechaInicio, LocalDateTime fechaFin, Boolean activa,
                               Double valorDescuento, Integer cantidadMinima, Integer cantidadMaxima) {
        this(operationType, entityId);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoPromocion = tipoPromocion;
        this.tipoPromocionAcumulable = tipoPromocionAcumulable;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activa = activa;
        this.valorDescuento = valorDescuento;
        this.cantidadMinima = cantidadMinima;
        this.cantidadMaxima = cantidadMaxima;
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getTipoPromocion() {
        return tipoPromocion;
    }
    
    public void setTipoPromocion(String tipoPromocion) {
        this.tipoPromocion = tipoPromocion;
    }
    
    public String getTipoPromocionAcumulable() {
        return tipoPromocionAcumulable;
    }
    
    public void setTipoPromocionAcumulable(String tipoPromocionAcumulable) {
        this.tipoPromocionAcumulable = tipoPromocionAcumulable;
    }
    
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public Boolean getActiva() {
        return activa;
    }
    
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
    
    public Double getValorDescuento() {
        return valorDescuento;
    }
    
    public void setValorDescuento(Double valorDescuento) {
        this.valorDescuento = valorDescuento;
    }
    
    public Integer getCantidadMinima() {
        return cantidadMinima;
    }
    
    public void setCantidadMinima(Integer cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }
    
    public Integer getCantidadMaxima() {
        return cantidadMaxima;
    }
    
    public void setCantidadMaxima(Integer cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }
}