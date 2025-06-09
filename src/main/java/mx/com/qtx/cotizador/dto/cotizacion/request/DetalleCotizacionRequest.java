package mx.com.qtx.cotizador.dto.cotizacion.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * DTO para solicitudes de detalle de cotización.
 * Representa un componente individual dentro de una cotización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCotizacionRequest {
    
    /**
     * Identificador del componente a cotizar
     */
    @NotBlank(message = "El ID del componente es requerido")
    private String idComponente;
    
    /**
     * Cantidad del componente solicitada
     */
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
    
    /**
     * Descripción personalizada del componente (opcional)
     */
    private String descripcion;
    
    /**
     * Precio base personalizado (opcional - si no se proporciona, se usa el del componente)
     */
    @PositiveOrZero(message = "El precio base debe ser mayor o igual a cero")
    private BigDecimal precioBase;
} 