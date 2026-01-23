package mx.com.qtx.cotizador.dto.componente.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la actualización de componentes
 * No incluye el ID ya que se envía como parámetro de la URL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteUpdateRequest {
    
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
    
    @NotBlank(message = "La marca es requerida")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;
    
    @NotBlank(message = "El modelo es requerido")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;
    
    @NotNull(message = "El costo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
    private BigDecimal costo;
    
    @NotNull(message = "El precio base es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;
    
    @NotBlank(message = "El tipo de componente es requerido")
    private String tipoComponente; // MONITOR, DISCO_DURO, TARJETA_VIDEO, PC
    
    // Campos específicos para disco duro
    @Size(max = 20, message = "La capacidad de almacenamiento no puede exceder 20 caracteres")
    private String capacidadAlm;
    
    // Campos específicos para tarjeta de video
    @Size(max = 20, message = "La memoria no puede exceder 20 caracteres")
    private String memoria;
} 