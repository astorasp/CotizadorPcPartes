package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Componente específico para monitores.
 */
public class ComponenteMonitor extends Componente {
    
    private Integer tamaño;
    private String resolucion;
    private String tecnologia;
    
    /**
     * Constructor completo con especificaciones técnicas
     */
    public ComponenteMonitor(String id, String descripcion, BigDecimal precioBase, 
                           String marca, String modelo, Integer tamaño, 
                           String resolucion, String tecnologia) {
        super(id, descripcion, precioBase, marca, modelo);
        this.tamaño = tamaño;
        this.resolucion = resolucion;
        this.tecnologia = tecnologia;
    }
    
    /**
     * Constructor simplificado con promoción (para converter)
     */
    public ComponenteMonitor(String id, String descripcion, BigDecimal precioBase, 
                           String marca, String modelo, IPromocion promocion) {
        super(id, descripcion, precioBase, marca, modelo, promocion);
        this.tamaño = 0;
        this.resolucion = "No especificada";
        this.tecnologia = "No especificada";
    }
    
    /**
     * Constructor simplificado sin promoción
     */
    public ComponenteMonitor(String id, String descripcion, BigDecimal precioBase, 
                           String marca, String modelo) {
        this(id, descripcion, precioBase, marca, modelo, null);
    }
    
    @Override
    public String getCategoria() {
        return "MONITOR";
    }
    
    // Getters y setters específicos
    public Integer getTamaño() { return tamaño; }
    public void setTamaño(Integer tamaño) { this.tamaño = tamaño; }
    
    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }
    
    public String getTecnologia() { return tecnologia; }
    public void setTecnologia(String tecnologia) { this.tecnologia = tecnologia; }
}