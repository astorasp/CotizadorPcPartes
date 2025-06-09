package mx.com.qtx.cotizador.dto.pedido.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de request para crear un nuevo pedido (Caso 5.1: Agregar pedido)
 * 
 * Validaciones:
 * - Proveedor requerido (clave válida)
 * - Fecha de entrega futura requerida
 * - Nivel de surtido no negativo
 * - Al menos un detalle de pedido
 * - Todos los detalles válidos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreateRequest {
    
    @NotBlank(message = "La clave del proveedor es requerida")
    @Size(max = 10, message = "La clave del proveedor no puede exceder 10 caracteres")
    private String cveProveedor;
    
    @NotNull(message = "La fecha de entrega es requerida")
    @Future(message = "La fecha de entrega debe ser futura")
    private LocalDate fechaEntrega;
    
    @PositiveOrZero(message = "El nivel de surtido debe ser positivo o cero")
    @Builder.Default
    private Integer nivelSurtido = 0;
    
    @NotEmpty(message = "El pedido debe tener al menos un detalle")
    @Valid
    private List<DetallePedidoRequest> detalles;
    
    // Campo opcional - si no se proporciona, se usa la fecha actual
    @Builder.Default
    private LocalDate fechaEmision = LocalDate.now();
} 