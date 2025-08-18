package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Componente específico para discos duros (HDD).
 */
public class ComponenteHdd extends Componente {
    
    private Integer capacidad;
    private Integer rpm;
    private String interfaz;
    
    /**
     * Constructor completo con especificaciones técnicas
     */
    public ComponenteHdd(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, Integer capacidad, 
                        Integer rpm, String interfaz) {
        super(id, descripcion, precioBase, marca, modelo);
        this.capacidad = capacidad;
        this.rpm = rpm;
        this.interfaz = interfaz;
    }
    
    /**
     * Constructor simplificado con promoción (para converter)
     */
    public ComponenteHdd(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, IPromocion promocion) {
        super(id, descripcion, precioBase, marca, modelo, promocion);
        this.capacidad = 0;
        this.rpm = 0;
        this.interfaz = "No especificada";
    }
    
    /**
     * Constructor simplificado sin promoción
     */
    public ComponenteHdd(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo) {
        this(id, descripcion, precioBase, marca, modelo, null);
    }
    
    @Override
    public String getCategoria() {
        return "HDD";
    }
    
    // Getters y setters específicos
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    
    public Integer getRpm() { return rpm; }
    public void setRpm(Integer rpm) { this.rpm = rpm; }
    
    public String getInterfaz() { return interfaz; }
    public void setInterfaz(String interfaz) { this.interfaz = interfaz; }
}