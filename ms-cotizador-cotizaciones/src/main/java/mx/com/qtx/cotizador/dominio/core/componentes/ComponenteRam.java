package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Componente específico para memoria RAM.
 */
public class ComponenteRam extends Componente {
    
    private Integer capacidad;
    private String tipo;
    private Integer frecuencia;
    
    /**
     * Constructor completo con especificaciones técnicas
     */
    public ComponenteRam(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, Integer capacidad, 
                        String tipo, Integer frecuencia) {
        super(id, descripcion, precioBase, marca, modelo);
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.frecuencia = frecuencia;
    }
    
    /**
     * Constructor simplificado con promoción (para converter)
     */
    public ComponenteRam(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, IPromocion promocion) {
        super(id, descripcion, precioBase, marca, modelo, promocion);
        this.capacidad = 0;
        this.tipo = "No especificado";
        this.frecuencia = 0;
    }
    
    /**
     * Constructor simplificado sin promoción
     */
    public ComponenteRam(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo) {
        this(id, descripcion, precioBase, marca, modelo, null);
    }
    
    @Override
    public String getCategoria() {
        return "RAM";
    }
    
    // Getters y setters específicos
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public Integer getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Integer frecuencia) { this.frecuencia = frecuencia; }
}