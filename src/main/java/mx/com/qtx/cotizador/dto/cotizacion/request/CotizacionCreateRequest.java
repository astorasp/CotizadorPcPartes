package mx.com.qtx.cotizador.dto.cotizacion.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

/**
 * DTO para solicitudes de creación de cotizaciones.
 * Incluye validaciones para asegurar que los datos requeridos estén presentes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionCreateRequest {
    
    /**
     * Lista de detalles de la cotización.
     * Debe contener al menos un detalle.
     */
    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La cotización debe tener al menos un detalle")
    @Valid
    private List<DetalleCotizacionRequest> detalles;
    
    /**
     * Observaciones adicionales sobre la cotización (opcional)
     */
    private String observaciones;
} 