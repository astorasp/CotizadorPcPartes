package mx.com.qtx.cotizador.dto.promocion.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para configurar escalas de descuento por cantidad.
 * 
 * Ejemplo: "Si compra 5 unidades, obtiene 10% de descuento"
 * - cantidad = 5
 * - descuento = 10.0 (porcentaje)
 */
public class EscalaDescuentoRequest {
    
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    @NotNull(message = "El porcentaje de descuento es requerido")
    @DecimalMin(value = "0.0", message = "El descuento debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor al 100%")
    private Double descuento;
    
    // Constructores
    public EscalaDescuentoRequest() {
        // Constructor vacío para Jackson
    }
    
    public EscalaDescuentoRequest(Integer cantidad, Double descuento) {
        this.cantidad = cantidad;
        this.descuento = descuento;
    }
    
    // Getters y setters
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Double getDescuento() {
        return descuento;
    }
    
    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }
    
    /**
     * Valida que los parámetros sean correctos
     */
    public boolean esValida() {
        return cantidad != null && cantidad > 0 && 
               descuento != null && descuento >= 0.0 && descuento <= 100.0;
    }
    
    @Override
    public String toString() {
        return "EscalaDescuentoRequest{" +
                "cantidad=" + cantidad +
                ", descuento=" + descuento + "%" +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EscalaDescuentoRequest that = (EscalaDescuentoRequest) obj;
        return cantidad.equals(that.cantidad);
    }
    
    @Override
    public int hashCode() {
        return cantidad.hashCode();
    }
} 