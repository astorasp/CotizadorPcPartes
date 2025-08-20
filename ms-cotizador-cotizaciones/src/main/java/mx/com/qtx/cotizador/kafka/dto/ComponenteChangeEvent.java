package mx.com.qtx.cotizador.kafka.dto;

/**
 * Evento de cambio para entidades de Componente.
 * 
 * Se envía cuando se crea, actualiza o elimina un componente en el sistema.
 * Contiene información del componente afectado para sincronización con otros microservicios.
 * 
 * @author Subagente3E - [2025-01-17 18:35:00 MST] - Evento de cambio para componentes en consumidor
 */
public class ComponenteChangeEvent extends BaseChangeEvent {
    
    private String nombre;
    private String descripcion;
    private Double costo;
    private Double precioBase;
    private String marca;
    private String modelo;
    private String tipoComponente;
    private Long promocionId;
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
    public ComponenteChangeEvent(OperationType operationType, String entityId) {
        super(EventType.COMPONENTE_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public ComponenteChangeEvent(OperationType operationType, String entityId, String nombre, 
                                String descripcion, Double costo, Double precioBase, String marca, String modelo, 
                                String tipoComponente, Long promocionId, Long proveedorId, String especificaciones, Boolean activo) {
        this(operationType, entityId);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.precioBase = precioBase;
        this.marca = marca;
        this.modelo = modelo;
        this.tipoComponente = tipoComponente;
        this.promocionId = promocionId;
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
    
    public Double getCosto() {
        return costo;
    }
    
    public void setCosto(Double costo) {
        this.costo = costo;
    }
    
    public Double getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(Double precioBase) {
        this.precioBase = precioBase;
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
    
    public Long getPromocionId() {
        return promocionId;
    }
    
    public void setPromocionId(Long promocionId) {
        this.promocionId = promocionId;
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