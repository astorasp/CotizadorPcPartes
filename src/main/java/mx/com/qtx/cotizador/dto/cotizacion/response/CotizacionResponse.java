package mx.com.qtx.cotizador.dto.cotizacion.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para cotizaciones.
 * Incluye toda la información de la cotización para ser enviada al cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionResponse {
    
    /**
     * Folio único de la cotización
     */
    private Integer folio;
    
    /**
     * Fecha de la cotización
     */
    private String fecha;
    
    /**
     * Subtotal de la cotización (antes de impuestos)
     */
    private BigDecimal subtotal;
    
    /**
     * Total de impuestos aplicados
     */
    private BigDecimal impuestos;
    
    /**
     * Total final de la cotización
     */
    private BigDecimal total;
    
    /**
     * Lista de detalles de la cotización
     */
    private List<DetalleCotizacionResponse> detalles;
} 