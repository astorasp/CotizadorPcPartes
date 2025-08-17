package mx.com.qtx.cotizador.dto.pc.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para actualizar una PC completa con sus sub-componentes
 * No incluye ID ya que se toma del path parameter
 */
@Data
public class PcUpdateRequest {
    
    @NotBlank(message = "El nombre de la PC es requerido")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;
    
    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precio;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "La lista de sub-componentes es requerida")
    @Size(min = 1, message = "Una PC debe tener al menos un sub-componente")
    @Size(max = 10, message = "Una PC no puede tener más de 10 sub-componentes")
    @Valid
    private List<ComponenteCreateRequest> subComponentes;
    
    // Campos específicos de PC
    @Size(max = 100, message = "El modelo no puede exceder 100 caracteres")
    private String modelo;
    
    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    private String marca;
    
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 1000, message = "La cantidad no puede exceder 1000")
    private Integer cantidad = 1;
} 