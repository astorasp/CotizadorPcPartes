package mx.com.qtx.cotizador.dto.proveedor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para actualizar un proveedor existente
 * 
 * Nota: La clave (CVE) se toma del path parameter, no del body
 * 
 * Validaciones:
 * - Nombre requerido, máximo 100 caracteres
 * - Razón social requerida, máximo 200 caracteres
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorUpdateRequest {
    
    @NotBlank(message = "El nombre del proveedor es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La razón social es requerida")
    @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
    private String razonSocial;
} 