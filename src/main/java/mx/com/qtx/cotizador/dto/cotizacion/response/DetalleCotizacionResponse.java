package mx.com.qtx.cotizador.dto.cotizacion.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO de respuesta para detalles de cotización.
 * Representa un componente individual dentro de una cotización en la respuesta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCotizacionResponse {
    
    /**
     * Número de detalle dentro de la cotización
     */
    private Integer numDetalle;
    
    /**
     * Identificador del componente
     */
    private String idComponente;
    
    /**
     * Nombre del componente
     */
    private String nombreComponente;
    
    /**
     * Categoría del componente
     */
    private String categoria;
    
    /**
     * Cantidad del componente
     */
    private Integer cantidad;
    
    /**
     * Descripción del componente
     */
    private String descripcion;
    
    /**
     * Precio base unitario
     */
    private BigDecimal precioBase;
    
    /**
     * Importe total del detalle (cantidad * precio con impuestos)
     */
    private BigDecimal importeTotal;
} 