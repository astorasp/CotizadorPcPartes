package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import java.math.BigDecimal;

/**
 * Convertidor para transformar eventos Kafka de cotizaciones a entidades JPA.
 * Facilita la sincronización de datos desde ms-cotizador-cotizaciones.
 */
public class CotizacionEventConverter {
    
    /**
     * Convierte un CotizacionChangeEvent de Kafka a una entidad Cotizacion para persistir localmente.
     * 
     * @param event Evento de cambio de cotización desde Kafka
     * @return Entidad Cotizacion lista para persistir
     */
    public static Cotizacion toEntity(CotizacionChangeEvent event) {
        if (event == null) {
            return null;
        }
        
        Cotizacion cotizacion = new Cotizacion();
        
        // ID del evento -> folio (primary key de la entidad)
        if (event.getEntityId() != null) {
            cotizacion.setFolio(Integer.parseInt(event.getEntityId()));
        }
        
        // Fecha de cotización -> fecha (String en la entidad)
        if (event.getFechaCotizacion() != null) {
            cotizacion.setFecha(event.getFechaCotizacion().toString());
        }
        
        // País no está disponible en la entidad simplificada
        
        // Montos: mapear desde evento
        if (event.getMontoTotal() != null) {
            cotizacion.setTotal(event.getMontoTotal());
        }
        
        if (event.getMontoImpuestos() != null) {
            cotizacion.setImpuestos(event.getMontoImpuestos());
        }
        
        // Calcular subtotal: total - impuestos (si ambos están disponibles)
        if (event.getMontoTotal() != null && event.getMontoImpuestos() != null) {
            BigDecimal subtotal = event.getMontoTotal().subtract(event.getMontoImpuestos());
            cotizacion.setSubtotal(subtotal);
        } else if (event.getMontoTotal() != null) {
            // Si no hay impuestos especificados, asumir que el total es el subtotal
            cotizacion.setSubtotal(event.getMontoTotal());
        }
        
        return cotizacion;
    }
    
    /**
     * Convierte campos adicionales del evento que no están directamente en la entidad básica.
     * Útil para logging y validaciones.
     * 
     * @param event Evento de cambio de cotización
     * @return String con información adicional para logs
     */
    public static String extractAdditionalInfo(CotizacionChangeEvent event) {
        if (event == null) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        
        if (event.getCliente() != null) {
            info.append("Cliente: ").append(event.getCliente()).append(", ");
        }
        
        if (event.getEstado() != null) {
            info.append("Estado: ").append(event.getEstado()).append(", ");
        }
        
        if (event.getAlgoritmo() != null) {
            info.append("Algoritmo: ").append(event.getAlgoritmo()).append(", ");
        }
        
        if (event.getMontoDscto() != null) {
            info.append("Descuento: ").append(event.getMontoDscto()).append(", ");
        }
        
        if (event.getDescripcion() != null) {
            info.append("Desc: ").append(event.getDescripcion());
        }
        
        return info.toString();
    }
    
    /**
     * Valida si el evento contiene los datos mínimos necesarios para crear una cotización.
     * 
     * @param event Evento a validar
     * @return true si el evento es válido, false otherwise
     */
    public static boolean isValidEvent(CotizacionChangeEvent event) {
        if (event == null) {
            return false;
        }
        
        // Validaciones básicas: debe tener ID y al menos un monto
        return event.getEntityId() != null && 
               (event.getMontoTotal() != null || event.getMontoImpuestos() != null);
    }
    
    /**
     * Determina si la cotización está lista para generar un pedido basándose en el estado.
     * 
     * @param event Evento de cotización
     * @return true si la cotización está aprobada, false otherwise
     */
    public static boolean isReadyForOrder(CotizacionChangeEvent event) {
        if (event == null) {
            return false;
        }
        
        return "APROBADA".equalsIgnoreCase(event.getEstado());
    }
}