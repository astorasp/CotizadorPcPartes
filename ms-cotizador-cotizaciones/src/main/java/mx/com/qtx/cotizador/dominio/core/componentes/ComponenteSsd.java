package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Componente específico para discos sólidos (SSD).
 */
public class ComponenteSsd extends Componente {
    
    private Integer capacidad;
    private String interfaz;
    private Integer velocidadLectura;
    
    /**
     * Constructor completo con especificaciones técnicas
     */
    public ComponenteSsd(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, Integer capacidad, 
                        String interfaz, Integer velocidadLectura) {
        super(id, descripcion, precioBase, marca, modelo);
        this.capacidad = capacidad;
        this.interfaz = interfaz;
        this.velocidadLectura = velocidadLectura;
    }
    
    /**
     * Constructor simplificado con promoción (para converter)
     */
    public ComponenteSsd(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, IPromocion promocion) {
        super(id, descripcion, precioBase, marca, modelo, promocion);
        this.capacidad = 0;
        this.interfaz = "No especificada";
        this.velocidadLectura = 0;
    }
    
    /**
     * Constructor simplificado sin promoción
     */
    public ComponenteSsd(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo) {
        this(id, descripcion, precioBase, marca, modelo, null);
    }
    
    @Override
    public String getCategoria() {
        return "SSD";
    }
    
    // Getters y setters específicos
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    
    public String getInterfaz() { return interfaz; }
    public void setInterfaz(String interfaz) { this.interfaz = interfaz; }
    
    public Integer getVelocidadLectura() { return velocidadLectura; }
    public void setVelocidadLectura(Integer velocidadLectura) { this.velocidadLectura = velocidadLectura; }
}