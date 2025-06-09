package mx.com.qtx.cotizador.dto.pedido.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de request para generar pedido desde cotización (Caso 5.2: Generar pedido)
 * 
 * Este DTO integra con ManejadorCreacionPedidos para convertir
 * una cotización existente en un pedido formal
 * 
 * Validaciones:
 * - ID cotización requerido
 * - Proveedor requerido (clave válida)
 * - Fecha de entrega futura requerida
 * - Nivel de surtido no negativo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPedidoRequest {
    
    @NotBlank(message = "El ID de la cotización es requerido")
    @Size(max = 50, message = "El ID de la cotización no puede exceder 50 caracteres")
    private String cotizacionId;
    
    @NotBlank(message = "La clave del proveedor es requerida")
    @Size(max = 10, message = "La clave del proveedor no puede exceder 10 caracteres")
    private String cveProveedor;
    
    @NotNull(message = "La fecha de entrega es requerida")
    @Future(message = "La fecha de entrega debe ser futura")
    private LocalDate fechaEntrega;
    
    @PositiveOrZero(message = "El nivel de surtido debe ser positivo o cero")
    @Builder.Default
    private Integer nivelSurtido = 0;
    
    // Campo opcional - si no se proporciona, se usa la fecha actual
    @Builder.Default
    private LocalDate fechaEmision = LocalDate.now();
} 