package mx.com.qtx.cotizador.dto.cotizacion.mapper;

import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.dto.cotizacion.response.DetalleCotizacionResponse;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.DetalleCotizacion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de cotización y DTOs.
 * Proporciona métodos estáticos para las conversiones necesarias en la API.
 */
public class CotizacionMapper {
    
    /**
     * Convierte una entidad Cotizacion a CotizacionResponse
     */
    public static CotizacionResponse toResponse(Cotizacion cotizacion) {
        if (cotizacion == null) {
            return null;
        }
        
        List<DetalleCotizacionResponse> detallesResponse = cotizacion.getDetalles().stream()
                .map(CotizacionMapper::toDetalleResponse)
                .collect(Collectors.toList());
        
        return CotizacionResponse.builder()
                .folio(cotizacion.getFolio())
                .fecha(cotizacion.getFecha())
                .subtotal(cotizacion.getSubtotal())
                .impuestos(cotizacion.getImpuestos())
                .total(cotizacion.getTotal())
                .detalles(detallesResponse)
                .observaciones(null) // Las observaciones no se almacenan en la entidad actual
                .build();
    }
    
    /**
     * Convierte una entidad DetalleCotizacion a DetalleCotizacionResponse
     */
    public static DetalleCotizacionResponse toDetalleResponse(DetalleCotizacion detalle) {
        if (detalle == null) {
            return null;
        }
        
        return DetalleCotizacionResponse.builder()
                .numDetalle(detalle.getId() != null ? detalle.getId().getNumDetalle() : null)
                .idComponente(detalle.getComponente() != null ? detalle.getComponente().getId() : null)
                .nombreComponente(detalle.getComponente() != null ? detalle.getComponente().getDescripcion() : null)
                .categoria(detalle.getComponente() != null && detalle.getComponente().getTipoComponente() != null ? 
                          detalle.getComponente().getTipoComponente().getNombre() : null)
                .cantidad(detalle.getCantidad())
                .descripcion(detalle.getDescripcion())
                .precioBase(detalle.getPrecioBase())
                .importeTotal(calcularImporteTotal(detalle))
                .build();
    }
    
    /**
     * Convierte una lista de entidades Cotizacion a lista de CotizacionResponse
     */
    public static List<CotizacionResponse> toResponseList(List<Cotizacion> cotizaciones) {
        if (cotizaciones == null) {
            return null;
        }
        
        return cotizaciones.stream()
                .map(CotizacionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Calcula el importe total de un detalle de cotización
     */
    private static java.math.BigDecimal calcularImporteTotal(DetalleCotizacion detalle) {
        if (detalle.getPrecioBase() == null || detalle.getCantidad() == null) {
            return java.math.BigDecimal.ZERO;
        }
        
        return detalle.getPrecioBase().multiply(java.math.BigDecimal.valueOf(detalle.getCantidad()));
    }
} 