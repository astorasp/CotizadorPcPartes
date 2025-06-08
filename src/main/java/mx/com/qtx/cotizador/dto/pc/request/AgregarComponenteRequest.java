package mx.com.qtx.cotizador.dto.pc.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para agregar un componente individual a una PC existente
 * Caso de uso: 2.2 Agregar Componentes
 */
@Data
public class AgregarComponenteRequest {
    
    @NotBlank(message = "El ID del componente es requerido")
    @Size(max = 50, message = "El ID no puede exceder 50 caracteres")
    private String id;
    
    @NotBlank(message = "El tipo de componente es requerido")
    @Pattern(regexp = "^(MONITOR|DISCO_DURO|TARJETA_VIDEO)$", 
             message = "Tipo de componente debe ser: MONITOR, DISCO_DURO, o TARJETA_VIDEO")
    private String tipoComponente;
    
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotBlank(message = "La marca es requerida")
    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    private String marca;
    
    @NotBlank(message = "El modelo es requerido")
    @Size(max = 100, message = "El modelo no puede exceder 100 caracteres")
    private String modelo;
    
    @NotNull(message = "El costo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El costo debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal costo;
    
    @NotNull(message = "El precio base es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio base debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precioBase;
    
    // Campos específicos por tipo de componente
    
    // Para DISCO_DURO
    @Size(max = 50, message = "La capacidad de almacenamiento no puede exceder 50 caracteres")
    private String capacidadAlm;
    
    // Para TARJETA_VIDEO
    @Size(max = 50, message = "La memoria no puede exceder 50 caracteres")
    private String memoria;
    
    // Los campos específicos se validan en el mapper según el tipo
} 