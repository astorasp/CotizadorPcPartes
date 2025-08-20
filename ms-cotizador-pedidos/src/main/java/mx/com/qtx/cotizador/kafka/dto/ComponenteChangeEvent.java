package mx.com.qtx.cotizador.kafka.dto;

/**
 * Evento de cambio para entidades de Componente.
 * 
 * Se envía cuando se crea, actualiza o elimina un componente en el sistema.
 * Contiene información del componente afectado para sincronización con el microservicio de pedidos.
 */
public class ComponenteChangeEvent extends BaseChangeEvent {
    
    private String descripcion;
    private Double costo;
    private Double precioBase;
    private String marca;
    private String modelo;
    private String tipoComponente;
    private Long promocionId;
    private String capacidadAlm;
    private String memoria;
    
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
    public ComponenteChangeEvent(OperationType operationType, String entityId, String descripcion, 
                                Double costo, Double precioBase, String marca, String modelo, 
                                String tipoComponente, Long promocionId, String capacidadAlm, 
                                String memoria) {
        this(operationType, entityId);
        this.descripcion = descripcion;
        this.costo = costo;
        this.precioBase = precioBase;
        this.marca = marca;
        this.modelo = modelo;
        this.tipoComponente = tipoComponente;
        this.promocionId = promocionId;
        this.capacidadAlm = capacidadAlm;
        this.memoria = memoria;
    }
    
    // Getters y Setters
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
    
    public String getCapacidadAlm() {
        return capacidadAlm;
    }
    
    public void setCapacidadAlm(String capacidadAlm) {
        this.capacidadAlm = capacidadAlm;
    }
    
    public String getMemoria() {
        return memoria;
    }
    
    public void setMemoria(String memoria) {
        this.memoria = memoria;
    }
}