package mx.com.qtx.cotizador.dto.pedido.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de request para detalles de pedido
 * 
 * Validaciones:
 * - ID artículo requerido, máximo 50 caracteres
 * - Descripción requerida, máximo 200 caracteres
 * - Cantidad positiva requerida
 * - Precio unitario positivo requerido
 * - Total cotizado positivo requerido
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoRequest {
    
    @NotBlank(message = "El ID del artículo es requerido")
    @Size(max = 50, message = "El ID del artículo no puede exceder 50 caracteres")
    private String idArticulo;
    
    @NotBlank(message = "La descripción del artículo es requerida")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
    
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer cantidad;
    
    @NotNull(message = "El precio unitario es requerido")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal precioUnitario;
    
    @NotNull(message = "El total cotizado es requerido")
    @Positive(message = "El total cotizado debe ser positivo")
    private BigDecimal totalCotizado;
} 