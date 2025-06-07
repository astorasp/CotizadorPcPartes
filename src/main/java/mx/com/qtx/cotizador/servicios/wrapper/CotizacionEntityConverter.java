package mx.com.qtx.cotizadorv1ds.servicios.wrapper;

import java.time.format.DateTimeFormatter;

/**
 * Clase utilitaria para convertir objetos de Cotización entre el dominio y la persistencia
 */
public class CotizacionEntityConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Convierte una Cotización del dominio a una entidad de persistencia
     * 
     * @param cotizacionCore Cotización del dominio
     * @param componenteRepo Repositorio de componentes para obtener referencias
     * @return Entidad Cotización para persistencia
     */
    public static mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion convertToEntity(
            mx.com.qtx.cotizadorv1ds.core.Cotizacion cotizacionCore, 
            mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ComponenteRepositorio componenteRepo) {
        
        if (cotizacionCore == null) {
            return null;
        }
        
        // Crear y configurar la entidad
        mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion cotizacionEntity = new mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion();
        
        // La clave primaria (folio) la generará automáticamente
        
        // Convertir fecha de LocalDate a String
        cotizacionEntity.setFecha(
            cotizacionCore.getFecha() != null 
                ? cotizacionCore.getFecha().format(DATE_FORMATTER) 
                : null
        );
        
        // Establecer montos
        cotizacionEntity.setSubtotal(
            cotizacionCore.getTotal().subtract(cotizacionCore.getTotalImpuestos())
        );
        cotizacionEntity.setImpuestos(cotizacionCore.getTotalImpuestos());
        cotizacionEntity.setTotal(cotizacionCore.getTotal());
        
        // Procesar los detalles (se debe hacer después de guardar la cotización)
        
        return cotizacionEntity;
    }
    
    /**
     * Agrega los detalles de la cotización a la entidad ya persistida
     * 
     * @param cotizacionCore Cotización del dominio (fuente de los detalles)
     * @param cotizacionEntity Entidad Cotización ya persistida (con ID generado)
     * @param componenteRepo Repositorio de componentes para obtener referencias
     */
    public static void addDetallesTo(
            mx.com.qtx.cotizadorv1ds.core.Cotizacion cotizacionCore, 
            mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion cotizacionEntity,
            mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ComponenteRepositorio componenteRepo) {
        
        if (cotizacionCore == null || cotizacionEntity == null) {
            return;
        }
        
        // Convertir y agregar los detalles
        for (mx.com.qtx.cotizadorv1ds.core.DetalleCotizacion detalleCore : cotizacionCore.getDetalles()) {
            // Crear nueva entidad DetalleCotizacion
            mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetalleCotizacion detalleEntity = 
                new mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetalleCotizacion();
            
            // Configurar ID compuesto
            mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetalleCotizacion.DetalleCotizacionId id = 
                new mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetalleCotizacion.DetalleCotizacionId();
            id.setFolio(cotizacionEntity.getFolio());
            id.setNumDetalle(detalleCore.getNumDetalle());
            detalleEntity.setId(id);
            
            // Establecer propiedades
            detalleEntity.setCantidad(detalleCore.getCantidad());
            detalleEntity.setDescripcion(detalleCore.getDescripcion());
            detalleEntity.setPrecioBase(detalleCore.getPrecioBase());
            
            // Buscar y establecer la referencia al componente
            if (detalleCore.getIdComponente() != null) {
                mx.com.qtx.cotizadorv1ds.persistencia.entidades.Componente componente = 
                    componenteRepo.findById(detalleCore.getIdComponente()).orElse(null);
                detalleEntity.setComponente(componente);
            }
            
            // Establecer la relación con la cotización
            cotizacionEntity.addDetalle(detalleEntity);
        }
    }
    
    /**
     * Convierte una Cotización completa del dominio a una entidad para persistencia,
     * incluyendo sus detalles. Este método no persiste la entidad ni consulta la base de datos.
     * 
     * @param cotizacionCore Cotización del dominio
     * @return Entidad Cotización para persistencia con sus detalles
     */
    public static mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion convertToNewEntity(mx.com.qtx.cotizadorv1ds.core.Cotizacion cotizacionCore) {
        if (cotizacionCore == null) {
            return null;
        }
        
        // Crear y configurar la entidad
        mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion cotizacionEntity = new mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion();
        
        // Convertir fecha de LocalDate a String
        cotizacionEntity.setFecha(
            cotizacionCore.getFecha() != null 
                ? cotizacionCore.getFecha().format(DATE_FORMATTER) 
                : null
        );
        
        // Establecer montos
        cotizacionEntity.setSubtotal(
            cotizacionCore.getTotal().subtract(cotizacionCore.getTotalImpuestos())
        );
        cotizacionEntity.setImpuestos(cotizacionCore.getTotalImpuestos());
        cotizacionEntity.setTotal(cotizacionCore.getTotal());
        
        return cotizacionEntity;
    }
}