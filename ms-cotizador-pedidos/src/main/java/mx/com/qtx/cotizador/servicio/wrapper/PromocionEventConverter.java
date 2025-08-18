package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.kafka.dto.PromocionChangeEvent;
import java.time.LocalDate;

/**
 * Convertidor para transformar eventos Kafka de promociones a entidades JPA.
 * Facilita la sincronización de datos desde ms-cotizador-componentes.
 */
public class PromocionEventConverter {
    
    /**
     * Convierte un PromocionChangeEvent de Kafka a una entidad Promocion para persistir localmente.
     * 
     * @param event Evento de cambio de promoción desde Kafka
     * @return Entidad Promocion lista para persistir
     */
    public static Promocion toEntity(PromocionChangeEvent event) {
        if (event == null) {
            return null;
        }
        
        Promocion promocion = new Promocion();
        
        // Campos básicos del evento
        if (event.getEntityId() != null) {
            try {
                promocion.setIdPromocion(Integer.valueOf(event.getEntityId()));
            } catch (NumberFormatException e) {
                // Si no se puede convertir, dejar null para que JPA genere el ID
                promocion.setIdPromocion(null);
            }
        }
        
        promocion.setNombre(event.getNombre());
        promocion.setDescripcion(event.getDescripcion());
        
        // Conversión de fechas: LocalDateTime del evento -> LocalDate de la entidad
        if (event.getFechaInicio() != null) {
            promocion.setVigenciaDesde(event.getFechaInicio().toLocalDate());
        }
        
        if (event.getFechaFin() != null) {
            promocion.setVigenciaHasta(event.getFechaFin().toLocalDate());
        }
        
        return promocion;
    }
    
    /**
     * Convierte campos adicionales del evento que no están directamente en la entidad básica.
     * Útil para logging y validaciones.
     * 
     * @param event Evento de cambio de promoción
     * @return String con información adicional para logs
     */
    public static String extractAdditionalInfo(PromocionChangeEvent event) {
        if (event == null) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        
        if (event.getTipoPromocion() != null) {
            info.append("Tipo: ").append(event.getTipoPromocion()).append(", ");
        }
        
        if (event.getActiva() != null) {
            info.append("Activa: ").append(event.getActiva()).append(", ");
        }
        
        if (event.getValorDescuento() != null) {
            info.append("Descuento: ").append(event.getValorDescuento()).append(", ");
        }
        
        if (event.getCantidadMinima() != null || event.getCantidadMaxima() != null) {
            info.append("Cantidad [")
                .append(event.getCantidadMinima() != null ? event.getCantidadMinima() : "∞")
                .append("-")
                .append(event.getCantidadMaxima() != null ? event.getCantidadMaxima() : "∞")
                .append("]");
        }
        
        return info.toString();
    }
    
    /**
     * Valida si el evento contiene los datos mínimos necesarios para crear una promoción.
     * 
     * @param event Evento a validar
     * @return true si el evento es válido, false otherwise
     */
    public static boolean isValidEvent(PromocionChangeEvent event) {
        if (event == null) {
            return false;
        }
        
        // Validaciones básicas
        return event.getEntityId() != null && 
               event.getNombre() != null && !event.getNombre().trim().isEmpty();
    }
}