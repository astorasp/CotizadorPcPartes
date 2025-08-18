package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Componente específico para procesadores (CPU).
 */
public class ComponenteCpu extends Componente {
    
    private String arquitectura;
    private Integer nucleos;
    private String socket;
    
    /**
     * Constructor completo con especificaciones técnicas
     */
    public ComponenteCpu(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, String arquitectura, 
                        Integer nucleos, String socket) {
        super(id, descripcion, precioBase, marca, modelo);
        this.arquitectura = arquitectura;
        this.nucleos = nucleos;
        this.socket = socket;
    }
    
    /**
     * Constructor simplificado con promoción (para converter)
     */
    public ComponenteCpu(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo, IPromocion promocion) {
        super(id, descripcion, precioBase, marca, modelo, promocion);
        this.arquitectura = "No especificada";
        this.nucleos = 0;
        this.socket = "No especificado";
    }
    
    /**
     * Constructor simplificado sin promoción
     */
    public ComponenteCpu(String id, String descripcion, BigDecimal precioBase, 
                        String marca, String modelo) {
        this(id, descripcion, precioBase, marca, modelo, null);
    }
    
    @Override
    public String getCategoria() {
        return "CPU";
    }
    
    // Getters y setters específicos
    public String getArquitectura() { return arquitectura; }
    public void setArquitectura(String arquitectura) { this.arquitectura = arquitectura; }
    
    public Integer getNucleos() { return nucleos; }
    public void setNucleos(Integer nucleos) { this.nucleos = nucleos; }
    
    public String getSocket() { return socket; }
    public void setSocket(String socket) { this.socket = socket; }
}