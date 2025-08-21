package mx.com.qtx.cotizador.kafka.dto;

import java.util.List;

/**
 * Evento de cambio para entidades de PC.
 * 
 * Se envía cuando se crea, actualiza o elimina una PC en el sistema.
 * Contiene información de la PC afectada y sus componentes para sincronización con otros microservicios.
 * 
 * @author Subagente3E - [2025-08-20 11:35:00 MST] - Evento de cambio para PCs en ms-cotizador-cotizaciones
 */
public class PcChangeEvent extends BaseChangeEvent {
    
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activa;
    private List<String> componenteIds;
    private Integer cantidadComponentes;
    private Integer promocionId;
    
    /**
     * Constructor por defecto
     */
    public PcChangeEvent() {
        super();
    }
    
    /**
     * Constructor para crear evento de PC
     */
    public PcChangeEvent(OperationType operationType, String entityId, String nombre, 
                        String descripcion, Double precio, Boolean activa) {
        super(EventType.PC_CHANGE, operationType, entityId);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.activa = activa;
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
    
    public Double getPrecio() {
        return precio;
    }
    
    public void setPrecio(Double precio) {
        this.precio = precio;
    }
    
    public Boolean getActiva() {
        return activa;
    }
    
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
    
    public List<String> getComponenteIds() {
        return componenteIds;
    }
    
    public void setComponenteIds(List<String> componenteIds) {
        this.componenteIds = componenteIds;
    }
    
    public Integer getCantidadComponentes() {
        return cantidadComponentes;
    }
    
    public void setCantidadComponentes(Integer cantidadComponentes) {
        this.cantidadComponentes = cantidadComponentes;
    }
    
    public Integer getPromocionId() {
        return promocionId;
    }
    
    public void setPromocionId(Integer promocionId) {
        this.promocionId = promocionId;
    }
    
    @Override
    public String toString() {
        return "PcChangeEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", operationType=" + getOperationType() +
                ", entityId='" + getEntityId() + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", activa=" + activa +
                ", componenteIds=" + componenteIds +
                ", cantidadComponentes=" + cantidadComponentes +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}