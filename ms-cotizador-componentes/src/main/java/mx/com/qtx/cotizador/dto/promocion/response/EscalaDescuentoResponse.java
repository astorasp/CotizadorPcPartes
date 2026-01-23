package mx.com.qtx.cotizador.dto.promocion.response;

/**
 * DTO de respuesta para escalas de descuento por cantidad.
 * 
 * Representa un nivel específico de descuento basado en cantidad.
 */
public class EscalaDescuentoResponse {
    
    private Integer cantidad;
    private Double descuento;
    private String descripcion; // Descripción calculada del descuento
    
    // Constructores
    public EscalaDescuentoResponse() {
        // Constructor vacío para Jackson
    }
    
    public EscalaDescuentoResponse(Integer cantidad, Double descuento) {
        this.cantidad = cantidad;
        this.descuento = descuento;
        this.descripcion = generarDescripcion();
    }
    
    // Getters y setters
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        this.descripcion = generarDescripcion();
    }
    
    public Double getDescuento() {
        return descuento;
    }
    
    public void setDescuento(Double descuento) {
        this.descuento = descuento;
        this.descripcion = generarDescripcion();
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    /**
     * Genera una descripción legible del descuento
     */
    private String generarDescripcion() {
        if (cantidad == null || descuento == null) {
            return "";
        }
        return String.format("Compre %d+ unidades y obtenga %.1f%% de descuento", 
                           cantidad, descuento);
    }
    
    @Override
    public String toString() {
        return "EscalaDescuentoResponse{" +
                "cantidad=" + cantidad +
                ", descuento=" + descuento + "%" +
                '}';
    }
} 