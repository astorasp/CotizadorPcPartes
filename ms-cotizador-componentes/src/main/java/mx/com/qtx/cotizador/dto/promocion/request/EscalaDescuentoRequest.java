package mx.com.qtx.cotizador.dto.promocion.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTO para configurar escalas de descuento por cantidad.
 * 
 * Soporta tanto cantidad fija como rangos:
 * - Cantidad fija: cantidad = 5 (exactamente 5 unidades)
 * - Rango: cantidadMinima = 5, cantidadMaxima = 10 (entre 5 y 10 unidades)
 * - Rango abierto: cantidadMinima = 5, cantidadMaxima = null (5 o más unidades)
 */
@ValidarCantidad
public class EscalaDescuentoRequest {
    
    // Campo legacy para compatibilidad - se mapea a cantidadMinima si no se especifica
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    // Campos nuevos para soporte de rangos
    @Min(value = 1, message = "La cantidad mínima debe ser mayor a 0")
    private Integer cantidadMinima;
    
    @Min(value = 1, message = "La cantidad máxima debe ser mayor a 0")
    private Integer cantidadMaxima;
    
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
    
    public EscalaDescuentoRequest(Integer cantidadMinima, Integer cantidadMaxima, Double descuento) {
        this.cantidadMinima = cantidadMinima;
        this.cantidadMaxima = cantidadMaxima;
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
    
    public Integer getCantidadMinima() {
        return cantidadMinima;
    }
    
    public void setCantidadMinima(Integer cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }
    
    public Integer getCantidadMaxima() {
        return cantidadMaxima;
    }
    
    public void setCantidadMaxima(Integer cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }
    
    /**
     * Valida que los parámetros sean correctos.
     * Soporta validación tanto para cantidad fija como para rangos.
     */
    public boolean esValida() {
        // Validar descuento
        if (descuento == null || descuento < 0.0 || descuento > 100.0) {
            return false;
        }
        
        // Caso 1: Cantidad fija (campo legacy)
        if (cantidad != null) {
            return cantidad > 0;
        }
        
        // Caso 2: Rango con cantidadMinima (nuevo comportamiento)
        if (cantidadMinima != null) {
            // cantidadMinima debe ser válida
            if (cantidadMinima <= 0) {
                return false;
            }
            
            // Si hay cantidadMaxima, debe ser mayor que cantidadMinima
            if (cantidadMaxima != null && cantidadMaxima <= cantidadMinima) {
                return false;
            }
            
            return true;
        }
        
        // No se especificó ninguna cantidad válida
        return false;
    }
    
    /**
     * Obtiene la cantidad mínima efectiva.
     * Si se usa campo legacy 'cantidad', lo retorna.
     * Si se usa 'cantidadMinima', lo retorna.
     */
    public Integer getCantidadMinimaEfectiva() {
        return cantidad != null ? cantidad : cantidadMinima;
    }
    
    /**
     * Obtiene la cantidad máxima efectiva.
     * Si se usa campo legacy 'cantidad', retorna el mismo valor (rango de 1).
     * Si se usa 'cantidadMaxima', lo retorna (puede ser null para rango abierto).
     */
    public Integer getCantidadMaximaEfectiva() {
        return cantidad != null ? cantidad : cantidadMaxima;
    }
    
    @Override
    public String toString() {
        if (cantidad != null) {
            return "EscalaDescuentoRequest{cantidad=" + cantidad + ", descuento=" + descuento + "%}";
        } else {
            String rangoStr = cantidadMinima + (cantidadMaxima != null ? "-" + cantidadMaxima : "+");
            return "EscalaDescuentoRequest{rango=" + rangoStr + ", descuento=" + descuento + "%}";
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EscalaDescuentoRequest that = (EscalaDescuentoRequest) obj;
        
        // Comparar por cantidad mínima efectiva
        Integer estaCantidadMin = this.getCantidadMinimaEfectiva();
        Integer otraCantidadMin = that.getCantidadMinimaEfectiva();
        
        return estaCantidadMin != null && estaCantidadMin.equals(otraCantidadMin);
    }
    
    @Override
    public int hashCode() {
        Integer cantidadMin = getCantidadMinimaEfectiva();
        return cantidadMin != null ? cantidadMin.hashCode() : 0;
    }
}

/**
 * Validación personalizada para asegurar que se especifique al menos una cantidad válida.
 */
@Documented
@Constraint(validatedBy = ValidarCantidadValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@interface ValidarCantidad {
    String message() default "Debe especificar 'cantidad' o 'cantidadMinima'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Validador para la anotación @ValidarCantidad.
 */
class ValidarCantidadValidator implements ConstraintValidator<ValidarCantidad, EscalaDescuentoRequest> {
    
    @Override
    public void initialize(ValidarCantidad constraintAnnotation) {
        // No hay inicialización necesaria
    }
    
    @Override
    public boolean isValid(EscalaDescuentoRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Dejar que @NotNull maneje esto
        }
        
        // Debe tener cantidad O cantidadMinima (pero no ambos)
        boolean tieneCantidad = value.getCantidad() != null;
        boolean tieneCantidadMinima = value.getCantidadMinima() != null;
        
        if (!tieneCantidad && !tieneCantidadMinima) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("La cantidad es requerida")
                   .addPropertyNode("cantidad")
                   .addConstraintViolation();
            return false;
        }
        
        // Si tiene cantidadMaxima, debe tener cantidadMinima también
        if (value.getCantidadMaxima() != null && !tieneCantidadMinima) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Si especifica cantidadMaxima, debe especificar cantidadMinima")
                   .addPropertyNode("cantidadMinima")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}