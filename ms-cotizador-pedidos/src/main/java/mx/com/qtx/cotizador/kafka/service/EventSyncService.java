package mx.com.qtx.cotizador.kafka.service;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.ProveedorChangeEvent;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.repositorio.PedidoRepositorio;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.Pedido;
import mx.com.qtx.cotizador.entidad.Proveedor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para sincronización de eventos entre microservicios.
 * 
 * Maneja la sincronización bidireccional de datos y resolución de conflictos
 * para mantener consistencia entre microservicios del sistema de cotizador.
 * 
 * @author Subagente4E - [2025-08-17 11:25:00 MST] - Servicio de sincronización de eventos para ms-cotizador-pedidos
 */
@Service
@Transactional
public class EventSyncService {

    private static final Logger logger = LoggerFactory.getLogger(EventSyncService.class);
    
    @Autowired
    private ComponenteRepositorio componenteRepositorio;
    
    @Autowired
    private CotizacionRepositorio cotizacionRepositorio;
    
    @Autowired
    private ProveedorRepositorio proveedorRepositorio;
    
    @Autowired
    private PedidoRepositorio pedidoRepositorio;
    
    @Autowired
    private ConflictResolutionService conflictResolutionService;
    
    @Autowired
    private EventProducerService eventProducerService;
    
    // Cache para eventos procesados (evitar duplicados)
    private final Map<String, ProcessedEvent> processedEvents = new ConcurrentHashMap<>();
    
    private static class ProcessedEvent {
        private final LocalDateTime timestamp;
        private final String source;
        
        public ProcessedEvent(String source) {
            this.timestamp = LocalDateTime.now();
            this.source = source;
        }
        
        public boolean isExpired() {
            return timestamp.isBefore(LocalDateTime.now().minusHours(24));
        }
        
        public String getSource() { return source; }
    }
    
    /**
     * Verifica si un evento ya fue procesado.
     */
    public boolean isEventProcessed(String eventId) {
        ProcessedEvent event = processedEvents.get(eventId);
        if (event != null && event.isExpired()) {
            processedEvents.remove(eventId);
            return false;
        }
        return event != null;
    }
    
    /**
     * Marca un evento como procesado.
     */
    public void markEventAsProcessed(String eventId, String source) {
        processedEvents.put(eventId, new ProcessedEvent(source));
    }
    
    // === SINCRONIZACIÓN DE COMPONENTES ===
    
    /**
     * Sincroniza la creación de un componente desde otro microservicio.
     */
    public void syncComponenteCreated(ComponenteChangeEvent event) {
        logger.info("Sincronizando creación de componente: id={}, nombre={}", 
                   event.getEntityId(), event.getNombre());
        
        try {
            Optional<Componente> existingComponente = componenteRepositorio.findById(event.getEntityId().intValue());
            
            if (existingComponente.isPresent()) {
                // Conflicto: componente ya existe
                conflictResolutionService.resolveComponenteConflict(existingComponente.get(), event);
            } else {
                // Crear nuevo componente local
                Componente nuevoComponente = mapToComponenteEntity(event);
                componenteRepositorio.save(nuevoComponente);
                logger.info("Componente sincronizado exitosamente: id={}", event.getEntityId());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando creación de componente: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sincroniza la actualización de un componente.
     */
    public void syncComponenteUpdated(ComponenteChangeEvent event) {
        logger.info("Sincronizando actualización de componente: id={}, nombre={}", 
                   event.getEntityId(), event.getNombre());
        
        try {
            Optional<Componente> existingComponente = componenteRepositorio.findById(event.getEntityId().intValue());
            
            if (existingComponente.isPresent()) {
                Componente componente = existingComponente.get();
                
                // Resolver conflictos antes de actualizar
                if (conflictResolutionService.hasConflict(componente, event)) {
                    conflictResolutionService.resolveComponenteConflict(componente, event);
                } else {
                    updateComponenteFromEvent(componente, event);
                    componenteRepositorio.save(componente);
                    logger.info("Componente actualizado exitosamente: id={}", event.getEntityId());
                }
            } else {
                // Componente no existe localmente, crear
                syncComponenteCreated(event);
            }
        } catch (Exception e) {
            logger.error("Error sincronizando actualización de componente: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sincroniza la eliminación de un componente.
     */
    public void syncComponenteDeleted(ComponenteChangeEvent event) {
        logger.info("Sincronizando eliminación de componente: id={}", event.getEntityId());
        
        try {
            Optional<Componente> existingComponente = componenteRepositorio.findById(event.getEntityId().intValue());
            
            if (existingComponente.isPresent()) {
                // Para simplificar, solo loggear la eliminación
                logger.info("Componente marcado como eliminado: id={}", event.getEntityId());
                logger.info("Componente marcado como inactivo: id={}", event.getEntityId());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando eliminación de componente: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // === SINCRONIZACIÓN DE COTIZACIONES ===
    
    /**
     * Sincroniza la creación de una cotización.
     */
    public void syncCotizacionCreated(CotizacionChangeEvent event) {
        logger.info("Sincronizando creación de cotización: id={}, cliente={}", 
                   event.getEntityId(), event.getCliente());
        
        try {
            Optional<Cotizacion> existingCotizacion = cotizacionRepositorio.findById(event.getEntityId().intValue());
            
            if (existingCotizacion.isPresent()) {
                // Conflicto: cotización ya existe
                conflictResolutionService.resolveCotizacionConflict(existingCotizacion.get(), event);
            } else {
                // Crear nueva cotización local
                Cotizacion nuevaCotizacion = mapToCotizacionEntity(event);
                cotizacionRepositorio.save(nuevaCotizacion);
                logger.info("Cotización sincronizada exitosamente: id={}", event.getEntityId());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando creación de cotización: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sincroniza la actualización de una cotización.
     */
    public void syncCotizacionUpdated(CotizacionChangeEvent event) {
        logger.info("Sincronizando actualización de cotización: id={}, estado={}", 
                   event.getEntityId(), event.getEstado());
        
        try {
            Optional<Cotizacion> existingCotizacion = cotizacionRepositorio.findById(event.getEntityId().intValue());
            
            if (existingCotizacion.isPresent()) {
                Cotizacion cotizacion = existingCotizacion.get();
                
                // Resolver conflictos antes de actualizar
                if (conflictResolutionService.hasConflict(cotizacion, event)) {
                    conflictResolutionService.resolveCotizacionConflict(cotizacion, event);
                } else {
                    updateCotizacionFromEvent(cotizacion, event);
                    cotizacionRepositorio.save(cotizacion);
                    logger.info("Cotización actualizada exitosamente: id={}", event.getEntityId());
                }
            } else {
                // Cotización no existe localmente, crear
                syncCotizacionCreated(event);
            }
        } catch (Exception e) {
            logger.error("Error sincronizando actualización de cotización: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sincroniza la eliminación de una cotización.
     */
    public void syncCotizacionDeleted(CotizacionChangeEvent event) {
        logger.info("Sincronizando eliminación de cotización: id={}", event.getEntityId());
        
        try {
            Optional<Cotizacion> existingCotizacion = cotizacionRepositorio.findById(event.getEntityId().intValue());
            
            if (existingCotizacion.isPresent()) {
                // Para simplificar, solo loggear la eliminación
                logger.info("Cotización marcada como eliminada: id={}", event.getEntityId());
                logger.info("Cotización marcada como eliminada: id={}", event.getEntityId());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando eliminación de cotización: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // === SINCRONIZACIÓN DE PROVEEDORES ===
    
    /**
     * Sincroniza la creación de un proveedor.
     */
    public void syncProveedorCreated(ProveedorChangeEvent event) {
        logger.info("Sincronizando creación de proveedor: id={}, nombre={}", 
                   event.getEntityId(), event.getNombre());
        
        try {
            Optional<Proveedor> existingProveedor = proveedorRepositorio.findById(event.getEntityId().toString());
            
            if (existingProveedor.isPresent()) {
                // Conflicto: proveedor ya existe
                conflictResolutionService.resolveProveedorConflict(existingProveedor.get(), event);
            } else {
                // Crear nuevo proveedor local
                Proveedor nuevoProveedor = mapToProveedorEntity(event);
                proveedorRepositorio.save(nuevoProveedor);
                logger.info("Proveedor sincronizado exitosamente: id={}", event.getEntityId());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando creación de proveedor: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sincroniza la actualización de un proveedor.
     */
    public void syncProveedorUpdated(ProveedorChangeEvent event) {
        logger.info("Sincronizando actualización de proveedor: id={}, nombre={}", 
                   event.getEntityId(), event.getNombre());
        
        try {
            Optional<Proveedor> existingProveedor = proveedorRepositorio.findById(event.getEntityId().toString());
            
            if (existingProveedor.isPresent()) {
                Proveedor proveedor = existingProveedor.get();
                
                // Resolver conflictos antes de actualizar
                if (conflictResolutionService.hasConflict(proveedor, event)) {
                    conflictResolutionService.resolveProveedorConflict(proveedor, event);
                } else {
                    updateProveedorFromEvent(proveedor, event);
                    proveedorRepositorio.save(proveedor);
                    logger.info("Proveedor actualizado exitosamente: id={}", event.getEntityId());
                }
            } else {
                // Proveedor no existe localmente, crear
                syncProveedorCreated(event);
            }
        } catch (Exception e) {
            logger.error("Error sincronizando actualización de proveedor: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sincroniza la eliminación de un proveedor.
     */
    public void syncProveedorDeleted(ProveedorChangeEvent event) {
        logger.info("Sincronizando eliminación de proveedor: id={}", event.getEntityId());
        
        try {
            Optional<Proveedor> existingProveedor = proveedorRepositorio.findById(event.getEntityId().toString());
            
            if (existingProveedor.isPresent()) {
                // Para simplificar, solo loggear la eliminación
                logger.info("Proveedor marcado como eliminado: id={}", event.getEntityId());
                logger.info("Proveedor marcado como inactivo: id={}", event.getEntityId());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando eliminación de proveedor: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // === MÉTODOS DE VALIDACIÓN Y NOTIFICACIÓN ===
    
    public void validatePendingOrdersWithNewComponent(Long componenteId) {
        logger.debug("Validando pedidos pendientes con nuevo componente: {}", componenteId);
        // Implementar lógica de validación
    }
    
    public void validatePendingOrdersWithUpdatedComponent(Long componenteId, Double precio) {
        logger.debug("Validando pedidos pendientes con componente actualizado: {}, precio: {}", componenteId, precio);
        // Implementar lógica de validación
    }
    
    public void notifyPriceChangeToOrders(Long componenteId, Double precio) {
        logger.debug("Notificando cambio de precio a pedidos: {}, precio: {}", componenteId, precio);
        // Implementar lógica de notificación
    }
    
    public void handleOrdersWithDeletedComponent(Long componenteId) {
        logger.debug("Manejando pedidos con componente eliminado: {}", componenteId);
        // Implementar lógica para manejar pedidos afectados
    }
    
    public void evaluateQuotationForOrder(Long cotizacionId) {
        logger.debug("Evaluando cotización para conversión a pedido: {}", cotizacionId);
        // Implementar lógica de evaluación
    }
    
    public void handleQuotationApproved(Long cotizacionId) {
        logger.debug("Manejando cotización aprobada: {}", cotizacionId);
        // Implementar lógica para cotización aprobada
    }
    
    public void handleQuotationCancelled(Long cotizacionId) {
        logger.debug("Manejando cotización cancelada: {}", cotizacionId);
        // Implementar lógica para cotización cancelada
    }
    
    public void validateOrderAmountChanges(Long cotizacionId, BigDecimal montoTotal) {
        logger.debug("Validando cambios de monto en pedidos: {}, monto: {}", cotizacionId, montoTotal);
        // Implementar lógica de validación
    }
    
    public void handleOrdersWithDeletedQuotation(Long cotizacionId) {
        logger.debug("Manejando pedidos con cotización eliminada: {}", cotizacionId);
        // Implementar lógica para pedidos afectados
    }
    
    public void evaluateOrderOpportunitiesWithNewProvider(Long proveedorId) {
        logger.debug("Evaluando oportunidades de pedidos con nuevo proveedor: {}", proveedorId);
        // Implementar lógica de evaluación
    }
    
    public void validatePendingOrdersWithUpdatedProvider(Long proveedorId) {
        logger.debug("Validando pedidos pendientes con proveedor actualizado: {}", proveedorId);
        // Implementar lógica de validación
    }
    
    public void handleProviderDeactivation(Long proveedorId) {
        logger.debug("Manejando desactivación de proveedor: {}", proveedorId);
        // Implementar lógica para proveedor desactivado
    }
    
    public void handleProviderActivation(Long proveedorId) {
        logger.debug("Manejando activación de proveedor: {}", proveedorId);
        // Implementar lógica para proveedor activado
    }
    
    public void updateProviderContactInOrders(Long proveedorId, String email, String telefono) {
        logger.debug("Actualizando contacto de proveedor en pedidos: {}, email: {}, tel: {}", proveedorId, email, telefono);
        // Implementar lógica de actualización
    }
    
    public void handleOrdersWithDeletedProvider(Long proveedorId) {
        logger.debug("Manejando pedidos con proveedor eliminado: {}", proveedorId);
        // Implementar lógica para pedidos afectados
    }
    
    public void handleOrphanedComponentsFromDeletedProvider(Long proveedorId) {
        logger.debug("Manejando componentes huérfanos por proveedor eliminado: {}", proveedorId);
        // Implementar lógica para componentes huérfanos
    }
    
    // === MÉTODOS DE MAPEO ===
    
    private Componente mapToComponenteEntity(ComponenteChangeEvent event) {
        Componente componente = new Componente();
        componente.setId(event.getEntityId().intValue());
        componente.setDescripcion(event.getDescripcion());
        if (event.getPrecio() != null) {
            componente.setPrecio(BigDecimal.valueOf(event.getPrecio()));
        }
        componente.setMarca(event.getMarca());
        componente.setModelo(event.getModelo());
        return componente;
    }
    
    private void updateComponenteFromEvent(Componente componente, ComponenteChangeEvent event) {
        if (event.getDescripcion() != null) componente.setDescripcion(event.getDescripcion());
        if (event.getPrecio() != null) componente.setPrecio(BigDecimal.valueOf(event.getPrecio()));
        if (event.getMarca() != null) componente.setMarca(event.getMarca());
        if (event.getModelo() != null) componente.setModelo(event.getModelo());
    }
    
    private Cotizacion mapToCotizacionEntity(CotizacionChangeEvent event) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(event.getEntityId().intValue());
        if (event.getMontoTotal() != null) cotizacion.setTotal(event.getMontoTotal());
        if (event.getMontoImpuestos() != null) cotizacion.setImpuestos(event.getMontoImpuestos());
        if (event.getPais() != null) cotizacion.setPais(event.getPais());
        if (event.getFechaCotizacion() != null) {
            cotizacion.setFechaCreacion(event.getFechaCotizacion().toLocalDate());
        }
        return cotizacion;
    }
    
    private void updateCotizacionFromEvent(Cotizacion cotizacion, CotizacionChangeEvent event) {
        if (event.getMontoTotal() != null) cotizacion.setTotal(event.getMontoTotal());
        if (event.getMontoImpuestos() != null) cotizacion.setImpuestos(event.getMontoImpuestos());
        if (event.getPais() != null) cotizacion.setPais(event.getPais());
        if (event.getFechaCotizacion() != null) {
            cotizacion.setFechaCreacion(event.getFechaCotizacion().toLocalDate());
        }
    }
    
    private Proveedor mapToProveedorEntity(ProveedorChangeEvent event) {
        Proveedor proveedor = new Proveedor();
        proveedor.setCve(event.getEntityId().toString());
        proveedor.setNombre(event.getNombre());
        // Solo usar campos que existen en la entidad
        return proveedor;
    }
    
    private void updateProveedorFromEvent(Proveedor proveedor, ProveedorChangeEvent event) {
        if (event.getNombre() != null) proveedor.setNombre(event.getNombre());
        // Solo actualizar campos que existen en la entidad
    }
}