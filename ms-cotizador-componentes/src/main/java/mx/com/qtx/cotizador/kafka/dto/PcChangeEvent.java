package mx.com.qtx.cotizador.kafka.dto;

import java.util.List;

/**
 * Evento de cambio para entidades de PC.
 * 
 * Se envía cuando se crea, actualiza o elimina una PC en el sistema.
 * Contiene información de la PC afectada y sus componentes para sincronización con otros microservicios.
 * 
 * @author Subagente2E - [2025-01-17 16:50:00 MST] - Evento de cambio para PCs
 */
public class PcChangeEvent extends BaseChangeEvent {
    
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activa;
    private List<Long> componenteIds;
    private Integer cantidadComponentes;
    
    /**
     * Constructor por defecto
     */
    public PcChangeEvent() {
        super();
        setEventType(EventType.PC_CHANGE);
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public PcChangeEvent(OperationType operationType, String entityId) {
        super(EventType.PC_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public PcChangeEvent(OperationType operationType, String entityId, String nombre, 
                        String descripcion, Double precio, Boolean activa,
                        List<Long> componenteIds, Integer cantidadComponentes) {
        this(operationType, entityId);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.activa = activa;
        this.componenteIds = componenteIds;
        this.cantidadComponentes = cantidadComponentes;
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
    
    public List<Long> getComponenteIds() {
        return componenteIds;
    }
    
    public void setComponenteIds(List<Long> componenteIds) {
        this.componenteIds = componenteIds;
    }
    
    public Integer getCantidadComponentes() {
        return cantidadComponentes;
    }
    
    public void setCantidadComponentes(Integer cantidadComponentes) {
        this.cantidadComponentes = cantidadComponentes;
    }
}