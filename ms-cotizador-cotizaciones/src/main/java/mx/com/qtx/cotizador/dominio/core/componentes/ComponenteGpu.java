package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Componente específico para tarjetas gráficas (GPU).
 */
public class ComponenteGpu extends Componente {
    
    private Integer memoria;
    private String tipoMemoria;
    
    /**
     * Constructor completo con especificaciones técnicas
     */
    public ComponenteGpu(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, Integer memoria, String tipoMemoria) {
        super(id, descripcion, precioBase, marca, modelo);
        this.memoria = memoria;
        this.tipoMemoria = tipoMemoria;
    }
    
    /**
     * Constructor simplificado con promoción (para converter)
     */
    public ComponenteGpu(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, IPromocion promocion) {
        super(id, descripcion, precioBase, marca, modelo, promocion);
        this.memoria = 0;
        this.tipoMemoria = "No especificada";
    }
    
    /**
     * Constructor simplificado sin promoción
     */
    public ComponenteGpu(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo) {
        this(id, descripcion, precioBase, marca, modelo, null);
    }
    
    @Override
    public String getCategoria() {
        return "GPU";
    }
    
    // Getters y setters específicos
    public Integer getMemoria() { return memoria; }
    public void setMemoria(Integer memoria) { this.memoria = memoria; }
    
    public String getTipoMemoria() { return tipoMemoria; }
    public void setTipoMemoria(String tipoMemoria) { this.tipoMemoria = tipoMemoria; }
}