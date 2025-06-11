package mx.com.qtx.cotizador.dto.cotizacion.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO de request para detalles de cotización.
 * Representa un componente individual dentro de una solicitud de cotización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCotizacionRequest {
    
    /**
     * Identificador del componente
     */
    @NotBlank(message = "El ID del componente es requerido")
    @Size(max = 50, message = "El ID del componente no puede exceder 50 caracteres")
    private String idComponente;
    
    /**
     * Cantidad del componente
     */
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer cantidad;
    
    /**
     * Descripción del componente (opcional, se puede obtener del repositorio)
     */
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
    
    /**
     * Precio base del componente (opcional, se puede obtener del repositorio)
     */
    @Positive(message = "El precio base debe ser positivo")
    private BigDecimal precioBase;
} 