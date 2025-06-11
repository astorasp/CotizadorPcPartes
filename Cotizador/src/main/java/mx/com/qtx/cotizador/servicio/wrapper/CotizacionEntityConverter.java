package mx.com.qtx.cotizador.servicio.wrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    public static mx.com.qtx.cotizador.entidad.Cotizacion convertToEntity(
            mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionCore, 
            mx.com.qtx.cotizador.repositorio.ComponenteRepositorio componenteRepo) {
        
        if (cotizacionCore == null) {
            return null;
        }
        
        // Crear y configurar la entidad
        mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntity = new mx.com.qtx.cotizador.entidad.Cotizacion();
        
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
            mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntity,
            mx.com.qtx.cotizador.repositorio.ComponenteRepositorio componenteRepo) {
        
        if (cotizacionCore == null || cotizacionEntity == null) {
            return;
        }
        
        // Convertir y agregar los detalles
        for (mx.com.qtx.cotizador.dominio.core.DetalleCotizacion detalleCore : cotizacionCore.getDetalles()) {
            // Crear nueva entidad DetalleCotizacion
            mx.com.qtx.cotizador.entidad.DetalleCotizacion detalleEntity = 
                new mx.com.qtx.cotizador.entidad.DetalleCotizacion();
            
            // Configurar ID compuesto
            mx.com.qtx.cotizador.entidad.DetalleCotizacion.DetalleCotizacionId id = 
                new mx.com.qtx.cotizador.entidad.DetalleCotizacion.DetalleCotizacionId();
            id.setFolio(cotizacionEntity.getFolio());
            id.setNumDetalle(detalleCore.getNumDetalle());
            detalleEntity.setId(id);
            
            // Establecer propiedades
            detalleEntity.setCantidad(detalleCore.getCantidad());
            detalleEntity.setDescripcion(detalleCore.getDescripcion());
            detalleEntity.setPrecioBase(detalleCore.getPrecioBase());
            
            // Buscar y establecer la referencia al componente
            if (detalleCore.getIdComponente() != null) {
                mx.com.qtx.cotizador.entidad.Componente componente = 
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
    public static mx.com.qtx.cotizador.entidad.Cotizacion convertToNewEntity(mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionCore) {
        if (cotizacionCore == null) {
            return null;
        }
        
        // Crear y configurar la entidad
        mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntity = new mx.com.qtx.cotizador.entidad.Cotizacion();
        
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
    
    /**
     * Convierte una entidad Cotización de persistencia a un objeto de dominio
     * 
     * @param cotizacionEntity Entidad Cotización de persistencia
     * @return Cotización del dominio
     */
    public static mx.com.qtx.cotizador.dominio.core.Cotizacion convertToDomain(
            mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntity) {
        
        if (cotizacionEntity == null) {
            return null;
        }
        
        // Convertir fecha de String a LocalDate
        LocalDate fecha = null;
        if (cotizacionEntity.getFecha() != null && !cotizacionEntity.getFecha().isEmpty()) {
            try {
                fecha = LocalDate.parse(cotizacionEntity.getFecha(), DATE_FORMATTER);
            } catch (Exception e) {
                // Si la fecha no puede parsearse, usamos fecha actual
                fecha = LocalDate.now();
            }
        }
        
        // Crear objeto de dominio Cotizacion
        mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionDominio = 
            new mx.com.qtx.cotizador.dominio.core.Cotizacion();
        
        // Establecer la fecha si es diferente de la actual
        if (fecha != null) {
            cotizacionDominio.setFecha(fecha);
        }
        
        // Convertir los detalles
        if (cotizacionEntity.getDetalles() != null && !cotizacionEntity.getDetalles().isEmpty()) {
            List<mx.com.qtx.cotizador.dominio.core.DetalleCotizacion> detallesDominio = 
                cotizacionEntity.getDetalles().stream()
                    .map(CotizacionEntityConverter::convertDetalleToDomain)
                    .collect(Collectors.toList());
            
            // Agregar detalles a la cotización de dominio
            for (mx.com.qtx.cotizador.dominio.core.DetalleCotizacion detalle : detallesDominio) {
                cotizacionDominio.agregarDetalle(detalle);
            }
        }
        
        return cotizacionDominio;
    }
    
    /**
     * Convierte un detalle de cotización de entidad a dominio
     * 
     * @param detalleEntity Entidad DetalleCotizacion
     * @return DetalleCotizacion del dominio
     */
    private static mx.com.qtx.cotizador.dominio.core.DetalleCotizacion convertDetalleToDomain(
            mx.com.qtx.cotizador.entidad.DetalleCotizacion detalleEntity) {
        
        if (detalleEntity == null) {
            return null;
        }
        
        // Obtener ID del componente
        String idComponente = null;
        if (detalleEntity.getComponente() != null) {
            idComponente = detalleEntity.getComponente().getId();
        }
        
        // Calcular importe cotizado como precioBase * cantidad
        java.math.BigDecimal precioBase = detalleEntity.getPrecioBase() != null ? 
            detalleEntity.getPrecioBase() : java.math.BigDecimal.ZERO;
        int cantidad = detalleEntity.getCantidad() != null ? detalleEntity.getCantidad() : 0;
        java.math.BigDecimal importeCotizado = precioBase.multiply(java.math.BigDecimal.valueOf(cantidad));
        
        // Obtener categoría del componente (a través de TipoComponente)
        String categoria = "COMPONENTE";
        if (detalleEntity.getComponente() != null && 
            detalleEntity.getComponente().getTipoComponente() != null &&
            detalleEntity.getComponente().getTipoComponente().getNombre() != null) {
            categoria = detalleEntity.getComponente().getTipoComponente().getNombre();
        }
        
        // Crear detalle de dominio
        return new mx.com.qtx.cotizador.dominio.core.DetalleCotizacion(
            detalleEntity.getId() != null ? detalleEntity.getId().getNumDetalle() : 0,
            idComponente,
            detalleEntity.getDescripcion() != null ? detalleEntity.getDescripcion() : "",
            cantidad,
            precioBase,
            importeCotizado,
            categoria
        );
    }
}