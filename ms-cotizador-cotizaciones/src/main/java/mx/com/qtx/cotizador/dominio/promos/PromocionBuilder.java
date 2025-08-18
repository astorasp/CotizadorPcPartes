package mx.com.qtx.cotizador.dominio.promos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.entidad.DetallePromocion;
import mx.com.qtx.cotizador.entidad.DetallePromDsctoXCant;

/**
 * Builder para construcción de objetos de dominio de promociones a partir de entidades JPA.
 * Implementa el patrón Builder para la conversión de entidades complejas a objetos de dominio.
 */
public class PromocionBuilder {
    
    /**
     * Construye un objeto de dominio de promoción a partir de una entidad JPA.
     * Analiza los detalles de la promoción para determinar el tipo específico a crear.
     * 
     * @param entidadPromocion Entidad JPA de promoción
     * @return Objeto de dominio de promoción del tipo específico
     */
    public static mx.com.qtx.cotizador.dominio.promos.Promocion construirDesdeEntidad(Promocion entidadPromocion) {
        if (entidadPromocion == null) {
            return PromSinDescto.crearPorDefecto();
        }
        
        if (entidadPromocion.getDetalles() == null || entidadPromocion.getDetalles().isEmpty()) {
            return new PromSinDescto(entidadPromocion.getIdPromocion(),
                                   entidadPromocion.getNombre(),
                                   entidadPromocion.getDescripcion(),
                                   entidadPromocion.getVigenciaDesde(),
                                   entidadPromocion.getVigenciaHasta());
        }
        
        // Tomar el primer detalle para determinar el tipo de promoción
        DetallePromocion primerDetalle = entidadPromocion.getDetalles().get(0);
        
        if (primerDetalle.getEsBase() != null && primerDetalle.getEsBase()) {
            return construirPromocionBase(entidadPromocion, primerDetalle);
        } else {
            return construirPromocionAcumulable(entidadPromocion, primerDetalle);
        }
    }
    
    /**
     * Construye una promoción base según el tipo especificado en el detalle.
     */
    private static mx.com.qtx.cotizador.dominio.promos.Promocion construirPromocionBase(
            Promocion entidad, DetallePromocion detalle) {
        
        String tipoBase = detalle.getTipoPromBase();
        
        if (tipoBase == null) {
            return construirPromSinDescto(entidad);
        }
        
        switch (tipoBase.toUpperCase()) {
            case "SIN_DESCUENTO":
            case "NINGUNO":
                return construirPromSinDescto(entidad);
                
            case "NXM":
            case "LLEVENT_PAGUEN":
                return construirPromNXM(entidad, detalle);
                
            default:
                // Tipo no reconocido, crear promoción sin descuento
                return construirPromSinDescto(entidad);
        }
    }
    
    /**
     * Construye una promoción acumulable según el tipo especificado en el detalle.
     */
    private static mx.com.qtx.cotizador.dominio.promos.Promocion construirPromocionAcumulable(
            Promocion entidad, DetallePromocion detalle) {
        
        String tipoAcumulable = detalle.getTipoPromAcumulable();
        
        if (tipoAcumulable == null) {
            return construirPromSinDescto(entidad);
        }
        
        switch (tipoAcumulable.toUpperCase()) {
            case "DESCUENTO_PLANO":
            case "PORCENTAJE_FIJO":
                return construirPromDsctoPlano(entidad, detalle);
                
            case "DESCUENTO_POR_CANTIDAD":
            case "ESCALAS_CANTIDAD":
                return construirPromDsctoXcantidad(entidad, detalle);
                
            default:
                // Tipo no reconocido, crear promoción sin descuento
                return construirPromSinDescto(entidad);
        }
    }
    
    /**
     * Construye PromSinDescto
     */
    private static PromSinDescto construirPromSinDescto(Promocion entidad) {
        return new PromSinDescto(entidad.getIdPromocion(),
                               entidad.getNombre(),
                               entidad.getDescripcion(),
                               entidad.getVigenciaDesde(),
                               entidad.getVigenciaHasta());
    }
    
    /**
     * Construye PromNXM a partir del detalle que contiene llevent y paguen
     */
    private static PromNXM construirPromNXM(Promocion entidad, DetallePromocion detalle) {
        int llevent = detalle.getLlevent() != null ? detalle.getLlevent() : 1;
        int paguen = detalle.getPaguen() != null ? detalle.getPaguen() : 1;
        
        // Validar que los valores sean lógicos para una promoción NxM
        if (llevent <= 0 || paguen <= 0 || paguen >= llevent) {
            // Valores inválidos, crear promoción sin descuento
            return new PromNXM(entidad.getIdPromocion(),
                              entidad.getNombre(),
                              entidad.getDescripcion(),
                              entidad.getVigenciaDesde(),
                              entidad.getVigenciaHasta(),
                              1, 1); // Valores por defecto válidos
        }
        
        return new PromNXM(entidad.getIdPromocion(),
                          entidad.getNombre(),
                          entidad.getDescripcion(),
                          entidad.getVigenciaDesde(),
                          entidad.getVigenciaHasta(),
                          llevent,
                          paguen);
    }
    
    /**
     * Construye PromDsctoPlano a partir del porcentaje de descuento
     */
    private static PromDsctoPlano construirPromDsctoPlano(Promocion entidad, DetallePromocion detalle) {
        double porcentaje = detalle.getPorcDctoPlano() != null ? detalle.getPorcDctoPlano() : 0.0;
        
        // Validar rango del porcentaje
        if (porcentaje < 0 || porcentaje > 100) {
            // Porcentaje inválido, usar 0%
            porcentaje = 0.0;
        }
        
        return new PromDsctoPlano(entidad.getIdPromocion(),
                                entidad.getNombre(),
                                entidad.getDescripcion(),
                                entidad.getVigenciaDesde(),
                                entidad.getVigenciaHasta(),
                                porcentaje);
    }
    
    /**
     * Construye PromDsctoXcantidad a partir de las escalas de descuento
     */
    private static PromDsctoXcantidad construirPromDsctoXcantidad(Promocion entidad, DetallePromocion detalle) {
        Map<Integer, Double> escalas = new HashMap<>();
        
        if (detalle.getDescuentosPorCantidad() != null && !detalle.getDescuentosPorCantidad().isEmpty()) {
            for (DetallePromDsctoXCant escala : detalle.getDescuentosPorCantidad()) {
                if (escala.getCantidad() != null && escala.getDscto() != null) {
                    int cantidad = escala.getCantidad();
                    double descuento = escala.getDscto();
                    
                    // Validar valores
                    if (cantidad > 0 && descuento >= 0 && descuento <= 100) {
                        escalas.put(cantidad, descuento);
                    }
                }
            }
        }
        
        // Si no hay escalas válidas, crear escalas por defecto
        if (escalas.isEmpty()) {
            escalas.put(1, 0.0); // Escala por defecto sin descuento
        }
        
        return new PromDsctoXcantidad(entidad.getIdPromocion(),
                                    entidad.getNombre(),
                                    entidad.getDescripcion(),
                                    entidad.getVigenciaDesde(),
                                    entidad.getVigenciaHasta(),
                                    escalas);
    }
    
    /**
     * Método helper para debugging - retorna información sobre el tipo de promoción detectado
     */
    public static String analizarTipoPromocion(Promocion entidadPromocion) {
        if (entidadPromocion == null) {
            return "Promoción nula -> PromSinDescto por defecto";
        }
        
        if (entidadPromocion.getDetalles() == null || entidadPromocion.getDetalles().isEmpty()) {
            return "Sin detalles -> PromSinDescto";
        }
        
        DetallePromocion primerDetalle = entidadPromocion.getDetalles().get(0);
        
        if (primerDetalle.getEsBase() != null && primerDetalle.getEsBase()) {
            String tipoBase = primerDetalle.getTipoPromBase();
            return String.format("Promoción BASE: %s -> %s", 
                                tipoBase, 
                                mapearTipoBase(tipoBase));
        } else {
            String tipoAcumulable = primerDetalle.getTipoPromAcumulable();
            return String.format("Promoción ACUMULABLE: %s -> %s", 
                                tipoAcumulable, 
                                mapearTipoAcumulable(tipoAcumulable));
        }
    }
    
    private static String mapearTipoBase(String tipo) {
        if (tipo == null) return "PromSinDescto";
        switch (tipo.toUpperCase()) {
            case "NXM":
            case "LLEVENT_PAGUEN":
                return "PromNXM";
            case "SIN_DESCUENTO":
            case "NINGUNO":
                return "PromSinDescto";
            default:
                return "PromSinDescto (tipo no reconocido)";
        }
    }
    
    private static String mapearTipoAcumulable(String tipo) {
        if (tipo == null) return "PromSinDescto";
        switch (tipo.toUpperCase()) {
            case "DESCUENTO_PLANO":
            case "PORCENTAJE_FIJO":
                return "PromDsctoPlano";
            case "DESCUENTO_POR_CANTIDAD":
            case "ESCALAS_CANTIDAD":
                return "PromDsctoXcantidad";
            default:
                return "PromSinDescto (tipo no reconocido)";
        }
    }
}