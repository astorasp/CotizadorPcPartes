package mx.com.qtx.cotizador.kafka.dto;

/**
 * Evento de cambio para entidades de Componente.
 * 
 * Se envía cuando se crea, actualiza o elimina un componente en el sistema.
 * Contiene información del componente afectado para sincronización con otros microservicios.
 * 
 * @author Subagente2E - [2025-01-17 16:40:00 MST] - Evento de cambio para componentes
 */
public class ComponenteChangeEvent extends BaseChangeEvent {
    
    private String nombre;
    private String descripcion;
    private Double precio;
    private String marca;
    private String modelo;
    private String tipoComponente;
    private Long proveedorId;
    private String especificaciones;
    private Boolean activo;
    
    /**
     * Constructor por defecto
     */
    public ComponenteChangeEvent() {
        super();
        setEventType(EventType.COMPONENTE_CHANGE);
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public ComponenteChangeEvent(OperationType operationType, Long entityId) {
        super(EventType.COMPONENTE_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public ComponenteChangeEvent(OperationType operationType, Long entityId, String nombre, 
                                String descripcion, Double precio, String marca, String modelo, 
                                String tipoComponente, Long proveedorId, String especificaciones, Boolean activo) {
        this(operationType, entityId);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.marca = marca;
        this.modelo = modelo;
        this.tipoComponente = tipoComponente;
        this.proveedorId = proveedorId;
        this.especificaciones = especificaciones;
        this.activo = activo;
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
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getTipoComponente() {
        return tipoComponente;
    }
    
    public void setTipoComponente(String tipoComponente) {
        this.tipoComponente = tipoComponente;
    }
    
    public Long getProveedorId() {
        return proveedorId;
    }
    
    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }
    
    public String getEspecificaciones() {
        return especificaciones;
    }
    
    public void setEspecificaciones(String especificaciones) {
        this.especificaciones = especificaciones;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}