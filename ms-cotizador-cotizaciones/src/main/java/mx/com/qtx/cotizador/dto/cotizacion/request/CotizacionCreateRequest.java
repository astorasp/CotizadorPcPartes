package mx.com.qtx.cotizador.dto.cotizacion.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import java.util.List;

/**
 * DTO de request para crear cotizaciones.
 * Incluye el tipo de cotizador, impuestos y detalles de componentes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionCreateRequest {
    
    /**
     * Tipo de cotizador a utilizar (A, B, etc.)
     */
    @NotBlank(message = "El tipo de cotizador es requerido")
    private String tipoCotizador;
    
    /**
     * Lista de tipos de impuestos a aplicar (IVA, LOCAL, FEDERAL, etc.)
     */
    private List<String> impuestos;
    
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
    
    /**
     * Fecha de la cotización (formato yyyy-MM-dd)
     * Si no se proporciona, se usará la fecha actual
     */
    private String fecha;
} 