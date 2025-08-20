package mx.com.qtx.cotizador.kafka.dto;

/**
 * Evento de cambio para entidades de Proveedor.
 * 
 * Se envía cuando se crea, actualiza o elimina un proveedor en el sistema.
 * Contiene información del proveedor afectado para sincronización entre microservicios.
 * 
 * @author Subagente4E - [2025-08-17 10:50:00 MST] - Evento de cambio para proveedores en ms-cotizador-pedidos
 */
public class ProveedorChangeEvent extends BaseChangeEvent {
    
    private String nombre;
    private String telefono;
    private String email;
    private String contacto;
    private String direccion;
    private String ciudad;
    private String estado;
    private String pais;
    private String codigoPostal;
    private Boolean activo;
    
    /**
     * Constructor por defecto
     */
    public ProveedorChangeEvent() {
        super();
        setEventType(EventType.PROVEEDOR_CHANGE);
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public ProveedorChangeEvent(OperationType operationType, String entityId) {
        super(EventType.PROVEEDOR_CHANGE, operationType, entityId);
    }
    
    /**
     * Constructor completo para creación/actualización
     */
    public ProveedorChangeEvent(OperationType operationType, String entityId, String nombre, 
                               String telefono, String email, String contacto, String direccion, 
                               String ciudad, String estado, String pais, String codigoPostal, Boolean activo) {
        this(operationType, entityId);
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.contacto = contacto;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = estado;
        this.pais = pais;
        this.codigoPostal = codigoPostal;
        this.activo = activo;
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getContacto() {
        return contacto;
    }
    
    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getPais() {
        return pais;
    }
    
    public void setPais(String pais) {
        this.pais = pais;
    }
    
    public String getCodigoPostal() {
        return codigoPostal;
    }
    
    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}