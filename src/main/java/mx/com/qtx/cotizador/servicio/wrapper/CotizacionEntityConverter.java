package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.DetalleCotizacion;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;

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
    public static Cotizacion convertToEntity(
            mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionCore,
            ComponenteRepositorio componenteRepo) {
        
        if (cotizacionCore == null) {
            return null;
        }
        
        // Crear y configurar la entidad
        Cotizacion cotizacionEntity = new Cotizacion();
        
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
            mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionCore,
            Cotizacion cotizacionEntity,
            ComponenteRepositorio componenteRepo) {
        
        if (cotizacionCore == null || cotizacionEntity == null) {
            return;
        }
        
        // Convertir y agregar los detalles
        for (mx.com.qtx.cotizador.dominio.core.DetalleCotizacion detalleCore : cotizacionCore.getDetalles()) {
            // Crear nueva entidad DetalleCotizacion
            DetalleCotizacion detalleEntity =
                new DetalleCotizacion();
            
            // Configurar ID compuesto
            DetalleCotizacion.DetalleCotizacionId id =
                new DetalleCotizacion.DetalleCotizacionId();
            id.setFolio(cotizacionEntity.getFolio());
            id.setNumDetalle(detalleCore.getNumDetalle());
            detalleEntity.setId(id);
            
            // Establecer propiedades
            detalleEntity.setCantidad(detalleCore.getCantidad());
            detalleEntity.setDescripcion(detalleCore.getDescripcion());
            detalleEntity.setPrecioBase(detalleCore.getPrecioBase());
            
            // Buscar y establecer la referencia al componente
            if (detalleCore.getIdComponente() != null) {
                Componente componente =
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
    public static Cotizacion convertToNewEntity(mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionCore) {
        if (cotizacionCore == null) {
            return null;
        }
        
        // Crear y configurar la entidad
        Cotizacion cotizacionEntity = new Cotizacion();
        
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