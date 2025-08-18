package mx.com.qtx.cotizador.dto.promocion.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para parámetros de promociones tipo "Compra N, Paga M"
 * 
 * Ejemplo: Compra 3, Paga 2 (3x2)
 * - llevent = 3 (unidades que debe llevar el cliente)
 * - paguen = 2 (unidades que debe pagar el cliente)
 */
public class ParametrosNxMRequest {
    
    @NotNull(message = "El valor 'llevent' es requerido")
    @Min(value = 2, message = "El cliente debe llevar mínimo 2 unidades")
    private Integer llevent;
    
    @NotNull(message = "El valor 'paguen' es requerido")
    @Min(value = 1, message = "El cliente debe pagar mínimo 1 unidad")
    private Integer paguen;
    
    // Constructores
    public ParametrosNxMRequest() {
        // Constructor vacío para Jackson
    }
    
    public ParametrosNxMRequest(Integer llevent, Integer paguen) {
        this.llevent = llevent;
        this.paguen = paguen;
    }
    
    // Getters y setters
    public Integer getLlevent() {
        return llevent;
    }
    
    public void setLlevent(Integer llevent) {
        this.llevent = llevent;
    }
    
    public Integer getPaguen() {
        return paguen;
    }
    
    public void setPaguen(Integer paguen) {
        this.paguen = paguen;
    }
    
    /**
     * Valida que la promoción sea lógicamente correcta
     */
    public boolean esValida() {
        return llevent != null && paguen != null && llevent > paguen && paguen > 0;
    }
    
    /**
     * Calcula el porcentaje de descuento equivalente
     */
    public double calcularPorcentajeDescuento() {
        if (!esValida()) return 0.0;
        return ((double)(llevent - paguen) / llevent) * 100;
    }
    
    @Override
    public String toString() {
        return "ParametrosNxMRequest{" +
                "llevent=" + llevent +
                ", paguen=" + paguen +
                ", descripcion='Compra " + llevent + ", Paga " + paguen + "'" +
                '}';
    }
} 