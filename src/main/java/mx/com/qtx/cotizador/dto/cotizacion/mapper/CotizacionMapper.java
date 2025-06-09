package mx.com.qtx.cotizador.dto.cotizacion.mapper;

import mx.com.qtx.cotizador.dto.cotizacion.request.CotizacionCreateRequest;
import mx.com.qtx.cotizador.dto.cotizacion.request.DetalleCotizacionRequest;
import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.dto.cotizacion.response.DetalleCotizacionResponse;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.DetalleCotizacion;
import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;

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
     * Convierte un CotizacionCreateRequest a Cotizacion del dominio
     */
    public static mx.com.qtx.cotizador.dominio.core.Cotizacion toDomain(CotizacionCreateRequest request, CotizacionServicio cotizacionServicio) {
        if (request == null) {
            return null;
        }
        
        var cotizacion = new mx.com.qtx.cotizador.dominio.core.Cotizacion();
        
        // Agregar detalles
        if (request.getDetalles() != null) {
            for (int i = 0; i < request.getDetalles().size(); i++) {
                DetalleCotizacionRequest detalleRequest = request.getDetalles().get(i);
                mx.com.qtx.cotizador.dominio.core.DetalleCotizacion detalle = toDetalleDomain(detalleRequest, i + 1);
                cotizacion.agregarDetalle(detalle);
            }
        }
        
        return cotizacion;
    }
    
    /**
     * Convierte un DetalleCotizacionRequest a DetalleCotizacion del dominio
     */
    public static mx.com.qtx.cotizador.dominio.core.DetalleCotizacion toDetalleDomain(DetalleCotizacionRequest request, int numDetalle) {
        if (request == null) {
            return null;
        }
        
        // Usar valores por defecto para campos requeridos que se calcularán después
        String descripcion = request.getDescripcion() != null ? request.getDescripcion() : "";
        java.math.BigDecimal precioBase = request.getPrecioBase() != null ? request.getPrecioBase() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal importeCotizado = precioBase.multiply(java.math.BigDecimal.valueOf(request.getCantidad()));
        String categoria = ""; // Se establecerá después al obtener el componente
        
        return new mx.com.qtx.cotizador.dominio.core.DetalleCotizacion(
            numDetalle,
            request.getIdComponente(),
            descripcion,
            request.getCantidad(),
            precioBase,
            importeCotizado,
            categoria
        );
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