package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.dominio.core.componentes.IPromocion;
import mx.com.qtx.cotizador.dominio.promos.PromocionBuilder;
import mx.com.qtx.cotizador.dominio.promos.PromSinDescto;
import mx.com.qtx.cotizador.entidad.Promocion;

/**
 * Converter para transformar entidades JPA de promociones a objetos de dominio.
 * Se encarga de la conversión compleja de promociones usando el PromocionBuilder.
 */
public class PromocionEntityConverter {
    
    /**
     * Convierte una entidad JPA Promocion a un objeto de dominio IPromocion.
     * Utiliza PromocionBuilder para determinar el tipo específico de promoción.
     * 
     * @param entidadPromocion Entidad JPA de promoción (puede ser null)
     * @return Objeto de dominio que implementa IPromocion
     */
    public static IPromocion convertirADominio(Promocion entidadPromocion) {
        if (entidadPromocion == null) {
            return PromSinDescto.crearPorDefecto();
        }
        
        try {
            return PromocionBuilder.construirDesdeEntidad(entidadPromocion);
        } catch (Exception e) {
            // Si hay cualquier error en la conversión, retornar promoción sin descuento
            // esto asegura que el sistema siga funcionando aunque haya datos corruptos
            return new PromSinDescto(entidadPromocion.getIdPromocion(),
                                   entidadPromocion.getNombre() != null ? entidadPromocion.getNombre() : "Promoción inválida",
                                   "Error en conversión: " + e.getMessage(),
                                   entidadPromocion.getVigenciaDesde(),
                                   entidadPromocion.getVigenciaHasta());
        }
    }
    
    /**
     * Método de conveniencia para conversión con logging de debugging.
     * Útil para desarrollo y debugging.
     * 
     * @param entidadPromocion Entidad JPA de promoción
     * @param debug Si debe imprimir información de debugging
     * @return Objeto de dominio que implementa IPromocion
     */
    public static IPromocion convertirADominioConDebug(Promocion entidadPromocion, boolean debug) {
        if (debug && entidadPromocion != null) {
            String analisis = PromocionBuilder.analizarTipoPromocion(entidadPromocion);
            System.out.println("DEBUG PromocionEntityConverter: " + analisis);
        }
        
        return convertirADominio(entidadPromocion);
    }
    
    /**
     * Verifica si una entidad de promoción puede ser convertida exitosamente.
     * Útil para validación antes de conversión.
     * 
     * @param entidadPromocion Entidad a validar
     * @return true si la conversión será exitosa, false si fallará
     */
    public static boolean puedeConvertir(Promocion entidadPromocion) {
        if (entidadPromocion == null) {
            return true; // Se puede convertir a PromSinDescto por defecto
        }
        
        try {
            PromocionBuilder.construirDesdeEntidad(entidadPromocion);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtiene información detallada sobre el tipo de promoción sin hacer la conversión completa.
     * Útil para logging y debugging.
     * 
     * @param entidadPromocion Entidad a analizar
     * @return String con información sobre el tipo de promoción
     */
    public static String obtenerInfoTipoPromocion(Promocion entidadPromocion) {
        return PromocionBuilder.analizarTipoPromocion(entidadPromocion);
    }
}