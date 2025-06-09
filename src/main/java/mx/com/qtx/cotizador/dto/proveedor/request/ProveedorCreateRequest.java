package mx.com.qtx.cotizador.dto.proveedor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para crear un nuevo proveedor
 * 
 * Validaciones:
 * - Clave requerida, máximo 10 caracteres
 * - Nombre requerido, máximo 100 caracteres
 * - Razón social requerida, máximo 200 caracteres
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorCreateRequest {
    
    @NotBlank(message = "La clave del proveedor es requerida")
    @Size(max = 10, message = "La clave no puede exceder 10 caracteres")
    private String cve;
    
    @NotBlank(message = "El nombre del proveedor es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La razón social es requerida")
    @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
    private String razonSocial;
} 