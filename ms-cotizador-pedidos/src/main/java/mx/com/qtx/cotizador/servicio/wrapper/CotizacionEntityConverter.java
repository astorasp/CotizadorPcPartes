package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Convertidor para transformar entre entidades de cotización y objetos de dominio
 * Maneja la conversión bidireccional entre la capa de persistencia y el dominio
 */
public class CotizacionEntityConverter {
    
    /**
     * Convierte una entidad Cotizacion a objeto de dominio Cotizacion
     * 
     * @param entidad La entidad de cotización de la base de datos
     * @return Objeto de dominio Cotizacion
     */
    public static Cotizacion convertToDomain(mx.com.qtx.cotizador.entidad.Cotizacion entidad) {
        if (entidad == null) {
            return null;
        }
        
        // Crear cotización de dominio
        Cotizacion cotizacionDominio = new Cotizacion();
        cotizacionDominio.setId(entidad.getId());
        cotizacionDominio.setFechaCreacion(entidad.getFechaCreacion());
        cotizacionDominio.setSubtotal(entidad.getSubtotal());
        cotizacionDominio.setImpuestos(entidad.getImpuestos());
        cotizacionDominio.setTotal(entidad.getTotal());
        cotizacionDominio.setPais(entidad.getPais());
        
        // Convertir detalles
        List<DetalleCotizacion> detallesDominio = new ArrayList<>();
        if (entidad.getDetalles() != null) {
            for (mx.com.qtx.cotizador.entidad.DetalleCotizacion detalleEntity : entidad.getDetalles()) {
                DetalleCotizacion detalleDominio = convertDetalleToDomain(detalleEntity);
                if (detalleDominio != null) {
                    detallesDominio.add(detalleDominio);
                }
            }
        }
        cotizacionDominio.setDetalles(detallesDominio);
        
        return cotizacionDominio;
    }
    
    /**
     * Convierte un detalle de cotización entidad a dominio
     */
    private static DetalleCotizacion convertDetalleToDomain(mx.com.qtx.cotizador.entidad.DetalleCotizacion entidad) {
        if (entidad == null) {
            return null;
        }
        
        DetalleCotizacion detalleDominio = new DetalleCotizacion();
        detalleDominio.setId(entidad.getId());
        detalleDominio.setCantidad(entidad.getCantidad());
        detalleDominio.setPrecioUnitario(entidad.getPrecioUnitario());
        detalleDominio.setSubtotal(entidad.getSubtotal());
        
        // Para el componente, necesitamos crear un objeto de dominio básico
        if (entidad.getComponente() != null) {
            mx.com.qtx.cotizador.dominio.core.Componente componenteDominio = 
                new mx.com.qtx.cotizador.dominio.core.Componente();
            componenteDominio.setId(entidad.getComponente().getId());
            componenteDominio.setMarca(entidad.getComponente().getMarca());
            componenteDominio.setModelo(entidad.getComponente().getModelo());
            componenteDominio.setDescripcion(entidad.getComponente().getDescripcion());
            componenteDominio.setPrecio(entidad.getComponente().getPrecio());
            
            detalleDominio.setComponente(componenteDominio);
        }
        
        return detalleDominio;
    }
    
    /**
     * Convierte un objeto de dominio Cotizacion a entidad
     * 
     * @param dominio El objeto de dominio
     * @return Entidad de cotización para persistencia
     */
    public static mx.com.qtx.cotizador.entidad.Cotizacion convertToEntity(Cotizacion dominio) {
        if (dominio == null) {
            return null;
        }
        
        mx.com.qtx.cotizador.entidad.Cotizacion entidad = new mx.com.qtx.cotizador.entidad.Cotizacion();
        entidad.setId(dominio.getId());
        entidad.setFechaCreacion(dominio.getFechaCreacion());
        entidad.setSubtotal(dominio.getSubtotal());
        entidad.setImpuestos(dominio.getImpuestos());
        entidad.setTotal(dominio.getTotal());
        entidad.setPais(dominio.getPais());
        
        // Los detalles se manejan por separado si es necesario
        
        return entidad;
    }
}