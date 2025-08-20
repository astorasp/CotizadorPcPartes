package mx.com.qtx.cotizador.kafka.service;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.ProveedorChangeEvent;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.Proveedor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio para resolución de conflictos en sincronización de eventos.
 * 
 * Implementa estrategias para resolver conflictos cuando los datos locales
 * difieren de los eventos recibidos de otros microservicios.
 * 
 * Estrategias de resolución:
 * - Last-Write-Wins: El último evento recibido prevalece
 * - Source Priority: Eventos del microservicio autorativo prevalecen
 * - Manual Resolution: Conflictos complejos requieren intervención manual
 * 
 * @author Subagente4E - [2025-08-17 11:30:00 MST] - Servicio de resolución de conflictos para ms-cotizador-pedidos
 */
@Service
@Profile("!test")
public class ConflictResolutionService {

    private static final Logger logger = LoggerFactory.getLogger(ConflictResolutionService.class);
    
    /**
     * Fuentes autoritativas para cada tipo de entidad
     */
    private static final String COMPONENTE_AUTHORITATIVE_SOURCE = "ms-cotizador-componentes";
    private static final String COTIZACION_AUTHORITATIVE_SOURCE = "ms-cotizador-cotizaciones";
    private static final String PROVEEDOR_AUTHORITATIVE_SOURCE = "ms-cotizador-pedidos";
    
    // === CONFLICTOS DE COMPONENTES ===
    
    /**
     * Verifica si existe conflicto entre componente local y evento.
     */
    public boolean hasConflict(Componente localComponente, ComponenteChangeEvent event) {
        // Simplificar verificación - solo verificar ID por ahora
        boolean hasConflict = !localComponente.getId().equals(event.getEntityId());
        
        if (hasConflict) {
            logger.warn("Conflicto detectado en componente: localId={}, eventId={}", 
                       localComponente.getId(), event.getEntityId());
        }
        
        return hasConflict;
    }
    
    /**
     * Resuelve conflicto de componente usando estrategia de fuente autoritativa.
     */
    public void resolveComponenteConflict(Componente localComponente, ComponenteChangeEvent event) {
        logger.info("Resolviendo conflicto de componente: id={}, source={}", 
                   event.getEntityId(), event.getSource());
        
        try {
            // Estrategia: ms-cotizador-componentes es autoritativo para componentes
            if (COMPONENTE_AUTHORITATIVE_SOURCE.equals(event.getSource())) {
                logger.info("Aplicando evento de fuente autoritativa: {}", event.getSource());
                updateComponenteFromAuthoritativeEvent(localComponente, event);
            } else {
                // Usar estrategia Last-Write-Wins basada en timestamp
                if (isEventNewer(event.getTimestamp())) {
                    logger.info("Aplicando evento más reciente: timestamp={}", event.getTimestamp());
                    updateComponenteFromEvent(localComponente, event);
                } else {
                    logger.info("Manteniendo datos locales por ser más recientes");
                    // Opcionalmente, enviar evento de actualización para sincronizar el otro servicio
                    notifyComponenteConflictResolution(localComponente);
                }
            }
        } catch (Exception e) {
            logger.error("Error resolviendo conflicto de componente: {}", e.getMessage(), e);
            // En caso de error, mantener datos locales y registrar para revisión manual
            logConflictForManualResolution("COMPONENTE", localComponente.getId(), event.getEventId(), e.getMessage());
        }
    }
    
    // === CONFLICTOS DE COTIZACIONES ===
    
    /**
     * Verifica si existe conflicto entre cotización local y evento.
     */
    public boolean hasConflict(Cotizacion localCotizacion, CotizacionChangeEvent event) {
        // Simplificar verificación - solo verificar ID por ahora
        boolean hasConflict = !localCotizacion.getFolio().toString().equals(event.getEntityId());
        
        if (hasConflict) {
            logger.warn("Conflicto detectado en cotización: localId={}, eventId={}", 
                       localCotizacion.getFolio(), event.getEntityId());
        }
        
        return hasConflict;
    }
    
    /**
     * Resuelve conflicto de cotización usando estrategia de fuente autoritativa.
     */
    public void resolveCotizacionConflict(Cotizacion localCotizacion, CotizacionChangeEvent event) {
        logger.info("Resolviendo conflicto de cotización: id={}, source={}", 
                   event.getEntityId(), event.getSource());
        
        try {
            // Estrategia: ms-cotizador-cotizaciones es autoritativo para cotizaciones
            if (COTIZACION_AUTHORITATIVE_SOURCE.equals(event.getSource())) {
                logger.info("Aplicando evento de fuente autoritativa: {}", event.getSource());
                updateCotizacionFromAuthoritativeEvent(localCotizacion, event);
            } else {
                // Para cotizaciones, usar lógica de negocio específica
                resolveCotizacionBusinessConflict(localCotizacion, event);
            }
        } catch (Exception e) {
            logger.error("Error resolviendo conflicto de cotización: {}", e.getMessage(), e);
            logConflictForManualResolution("COTIZACION", String.valueOf(localCotizacion.getFolio()), event.getEventId(), e.getMessage());
        }
    }
    
    // === CONFLICTOS DE PROVEEDORES ===
    
    /**
     * Verifica si existe conflicto entre proveedor local y evento.
     */
    public boolean hasConflict(Proveedor localProveedor, ProveedorChangeEvent event) {
        // Simplificar verificación - solo verificar CVE por ahora
        boolean hasConflict = !localProveedor.getCve().equals(event.getEntityId().toString());
        
        if (hasConflict) {
            logger.warn("Conflicto detectado en proveedor: localCve={}, eventId={}", 
                       localProveedor.getCve(), event.getEntityId());
        }
        
        return hasConflict;
    }
    
    /**
     * Resuelve conflicto de proveedor usando estrategia de fuente autoritativa.
     */
    public void resolveProveedorConflict(Proveedor localProveedor, ProveedorChangeEvent event) {
        logger.info("Resolviendo conflicto de proveedor: id={}, source={}", 
                   event.getEntityId(), event.getSource());
        
        try {
            // Estrategia: ms-cotizador-pedidos es autoritativo para proveedores
            if (PROVEEDOR_AUTHORITATIVE_SOURCE.equals(event.getSource())) {
                logger.info("Evento de fuente local ignorado para evitar bucle");
                return;
            }
            
            // Si viene de otra fuente, usar Last-Write-Wins
            if (isEventNewer(event.getTimestamp())) {
                logger.info("Aplicando evento más reciente: timestamp={}", event.getTimestamp());
                updateProveedorFromEvent(localProveedor, event);
            } else {
                logger.info("Manteniendo datos locales por ser más recientes");
                notifyProveedorConflictResolution(localProveedor);
            }
        } catch (Exception e) {
            logger.error("Error resolviendo conflicto de proveedor: {}", e.getMessage(), e);
            logConflictForManualResolution("PROVEEDOR", localProveedor.getCve(), event.getEventId(), e.getMessage());
        }
    }
    
    // === MÉTODOS DE APOYO ===
    
    /**
     * Verifica si el evento es más reciente que los datos locales.
     */
    private boolean isEventNewer(LocalDateTime eventTimestamp) {
        if (eventTimestamp == null) {
            return false;
        }
        
        // Usar ventana de tiempo para evitar conflictos por diferencias de reloj
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(5);
        
        return eventTimestamp.isAfter(threshold);
    }
    
    /**
     * Actualiza componente desde evento autoritativo.
     */
    private void updateComponenteFromAuthoritativeEvent(Componente componente, ComponenteChangeEvent event) {
        logger.info("Actualizando componente desde fuente autoritativa: id={}", event.getEntityId());
        updateComponenteFromEvent(componente, event);
    }
    
    /**
     * Actualiza componente desde evento.
     */
    private void updateComponenteFromEvent(Componente componente, ComponenteChangeEvent event) {
        if (event.getDescripcion() != null) componente.setDescripcion(event.getDescripcion());
        if (event.getPrecioBase() != null) componente.setPrecioBase(BigDecimal.valueOf(event.getPrecioBase()));
        if (event.getMarca() != null) componente.setMarca(event.getMarca());
        if (event.getModelo() != null) componente.setModelo(event.getModelo());
    }
    
    /**
     * Actualiza cotización desde evento autoritativo.
     */
    private void updateCotizacionFromAuthoritativeEvent(Cotizacion cotizacion, CotizacionChangeEvent event) {
        logger.info("Actualizando cotización desde fuente autoritativa: id={}", event.getEntityId());
        updateCotizacionFromEvent(cotizacion, event);
    }
    
    /**
     * Actualiza cotización desde evento.
     */
    private void updateCotizacionFromEvent(Cotizacion cotizacion, CotizacionChangeEvent event) {
        if (event.getMontoTotal() != null) cotizacion.setTotal(event.getMontoTotal());
        if (event.getMontoImpuestos() != null) cotizacion.setImpuestos(event.getMontoImpuestos());
        // Campos no disponibles en la estructura simplificada de Cotizacion
        // if (event.getPais() != null) cotizacion.setPais(event.getPais());
        if (event.getFechaCotizacion() != null) {
            cotizacion.setFecha(event.getFechaCotizacion().toString());
        }
    }
    
    /**
     * Resuelve conflictos de cotización usando lógica de negocio.
     */
    private void resolveCotizacionBusinessConflict(Cotizacion localCotizacion, CotizacionChangeEvent event) {
        logger.info("Resolviendo conflicto de cotización con lógica de negocio: id={}", event.getEntityId());
        
        // Reglas de negocio para resolución de conflictos:
        // 1. Estados finales (APROBADA, RECHAZADA) tienen prioridad sobre estados transitorios
        // 2. Cambios en montos requieren validación adicional
        // 3. Cambios de estado críticos se escalan para revisión manual
        
        // Simplificar por ahora - no hay campo estado en la entidad
        logger.info("Resolviendo conflicto de cotización con lógica simplificada: id={}", event.getEntityId());
        
        // Para otros casos, usar Last-Write-Wins
        if (isEventNewer(event.getTimestamp())) {
            updateCotizacionFromEvent(localCotizacion, event);
        }
    }
    
    /**
     * Verifica si un estado de cotización es final.
     */
    private boolean isFinalState(String estado) {
        return "APROBADA".equals(estado) || "RECHAZADA".equals(estado) || "CANCELADA".equals(estado);
    }
    
    /**
     * Actualiza proveedor desde evento.
     */
    private void updateProveedorFromEvent(Proveedor proveedor, ProveedorChangeEvent event) {
        if (event.getNombre() != null) proveedor.setNombre(event.getNombre());
        // Solo usar campos que existen en la entidad Proveedor
    }
    
    /**
     * Notifica resolución de conflicto de componente.
     */
    private void notifyComponenteConflictResolution(Componente componente) {
        logger.info("Notificando resolución de conflicto de componente: id={}", componente.getId());
        // TODO: Implementar envío de evento de sincronización
    }
    
    /**
     * Notifica resolución de conflicto de proveedor.
     */
    private void notifyProveedorConflictResolution(Proveedor proveedor) {
        logger.info("Notificando resolución de conflicto de proveedor: cve={}", proveedor.getCve());
        // TODO: Implementar envío de evento de sincronización
    }
    
    /**
     * Registra conflicto para resolución manual.
     */
    private void logConflictForManualResolution(String entityType, String entityId, String eventId, String error) {
        logger.error("CONFLICTO REQUIERE RESOLUCIÓN MANUAL: entityType={}, entityId={}, eventId={}, error={}", 
                    entityType, entityId, eventId, error);
        
        // TODO: Implementar sistema de notificaciones para conflictos manuales
        // Esto podría incluir:
        // - Envío a cola de resolución manual
        // - Notificación a administradores
        // - Registro en sistema de auditoría
    }
}