package mx.com.qtx.cotizador.dto.pedido.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de request para generar un pedido desde una cotización existente
 * 
 * Validaciones:
 * - ID de cotización requerido
 * - Clave de proveedor requerida, máximo 10 caracteres
 * - Fechas requeridas
 * - Nivel de surtido entre 0 y 100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPedidoRequest {
    
    @NotNull(message = "El ID de la cotización es requerido")
    private Integer cotizacionId;
    
    @NotBlank(message = "La clave del proveedor es requerida")
    @Size(max = 10, message = "La clave del proveedor no puede exceder 10 caracteres")
    private String cveProveedor;
    
    @NotNull(message = "La fecha de emisión es requerida")
    private LocalDate fechaEmision;
    
    @NotNull(message = "La fecha de entrega es requerida")
    private LocalDate fechaEntrega;
    
    @NotNull(message = "El nivel de surtido es requerido")
    @Min(value = 0, message = "El nivel de surtido no puede ser menor a 0")
    @Max(value = 100, message = "El nivel de surtido no puede ser mayor a 100")
    private Integer nivelSurtido;
} 