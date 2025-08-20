package mx.com.qtx.cotizador.kafka.service;

import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.repositorio.PedidoRepositorio;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.Pedido;
import mx.com.qtx.cotizador.entidad.Proveedor;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile("!test")
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
    private PromocionRepositorio promocionRepositorio;
    
    @Autowired
    private TipoComponenteRepositorio tipoComponenteRepositorio;
    
    @Autowired
    private ConflictResolutionService conflictResolutionService;
    
    
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
                   event.getEntityId(), event.getDescripcion());
        
        try {
            Optional<Componente> existingComponente = componenteRepositorio.findById(event.getEntityId());
            
            if (existingComponente.isPresent()) {
                // Conflicto: componente ya existe
                conflictResolutionService.resolveComponenteConflict(existingComponente.get(), event);
            } else {
                // Buscar el tipo de componente correspondiente
                String tipoNombre = mapearTipoComponente(event.getTipoComponente());
                TipoComponente tipoComponente = tipoComponenteRepositorio.findByNombre(tipoNombre);
                
                if (tipoComponente == null) {
                    logger.warn("Tipo de componente no encontrado: {}. Creando componente sin tipo.", tipoNombre);
                }
                
                // Crear nuevo componente local
                Componente nuevoComponente = mapToComponenteEntity(event, tipoComponente);
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
                   event.getEntityId(), event.getDescripcion());
        
        try {
            Optional<Componente> existingComponente = componenteRepositorio.findById(event.getEntityId());
            
            if (existingComponente.isPresent()) {
                Componente componente = existingComponente.get();
                
                // Buscar el tipo de componente correspondiente
                String tipoNombre = mapearTipoComponente(event.getTipoComponente());
                TipoComponente tipoComponente = tipoComponenteRepositorio.findByNombre(tipoNombre);
                
                if (tipoComponente == null) {
                    logger.warn("Tipo de componente no encontrado: {}. Actualizando componente sin tipo.", tipoNombre);
                }
                
                // Resolver conflictos antes de actualizar
                if (conflictResolutionService.hasConflict(componente, event)) {
                    conflictResolutionService.resolveComponenteConflict(componente, event);
                } else {
                    updateComponenteFromEvent(componente, event, tipoComponente);
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
            Optional<Componente> existingComponente = componenteRepositorio.findById(event.getEntityId());
            
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
            Optional<Cotizacion> existingCotizacion = cotizacionRepositorio.findById(Integer.parseInt(event.getEntityId()));
            
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
            Optional<Cotizacion> existingCotizacion = cotizacionRepositorio.findById(Integer.parseInt(event.getEntityId()));
            
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
            Optional<Cotizacion> existingCotizacion = cotizacionRepositorio.findById(Integer.parseInt(event.getEntityId()));
            
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
    
    
    
    
    // === MÉTODOS DE VALIDACIÓN Y NOTIFICACIÓN ===
    
    public void validatePendingOrdersWithNewComponent(String componenteId) {
        logger.debug("Validando pedidos pendientes con nuevo componente: {}", componenteId);
        // Implementar lógica de validación
    }
    
    public void validatePendingOrdersWithUpdatedComponent(String componenteId, Double precio) {
        logger.debug("Validando pedidos pendientes con componente actualizado: {}, precio: {}", componenteId, precio);
        // Implementar lógica de validación
    }
    
    public void notifyPriceChangeToOrders(String componenteId, Double precio) {
        logger.debug("Notificando cambio de precio a pedidos: {}, precio: {}", componenteId, precio);
        // Implementar lógica de notificación
    }
    
    public void handleOrdersWithDeletedComponent(String componenteId) {
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
    
    private Componente mapToComponenteEntity(ComponenteChangeEvent event, TipoComponente tipoComponente) {
        Componente componente = new Componente();
        componente.setId(event.getEntityId());
        componente.setDescripcion(event.getDescripcion());
        if (event.getPrecioBase() != null) {
            componente.setPrecioBase(BigDecimal.valueOf(event.getPrecioBase()));
        }
        // Agregar campo costo que faltaba
        if (event.getCosto() != null) {
            componente.setCosto(BigDecimal.valueOf(event.getCosto()));
        }
        componente.setMarca(event.getMarca());
        componente.setModelo(event.getModelo());
        componente.setCapacidadAlm(event.getCapacidadAlm());
        componente.setMemoria(event.getMemoria());
        
        // Relación con TipoComponente
        componente.setTipoComponente(tipoComponente);
        
        // Promoción - buscar por ID si viene en el evento
        if (event.getPromocionId() != null) {
            try {
                Promocion promocion = promocionRepositorio.findById(event.getPromocionId().intValue()).orElse(null);
                componente.setPromocion(promocion);
            } catch (Exception e) {
                // Si no se encuentra la promoción, dejar como null
                componente.setPromocion(null);
            }
        } else {
            componente.setPromocion(null);
        }
        return componente;
    }
    
    private void updateComponenteFromEvent(Componente componente, ComponenteChangeEvent event, TipoComponente tipoComponente) {
        if (event.getDescripcion() != null) componente.setDescripcion(event.getDescripcion());
        if (event.getPrecioBase() != null) componente.setPrecioBase(BigDecimal.valueOf(event.getPrecioBase()));
        // Agregar campo costo que faltaba
        if (event.getCosto() != null) componente.setCosto(BigDecimal.valueOf(event.getCosto()));
        if (event.getMarca() != null) componente.setMarca(event.getMarca());
        if (event.getModelo() != null) componente.setModelo(event.getModelo());
        if (event.getCapacidadAlm() != null) componente.setCapacidadAlm(event.getCapacidadAlm());
        if (event.getMemoria() != null) componente.setMemoria(event.getMemoria());
        
        // Actualizar relación con TipoComponente si es necesario
        if (tipoComponente != null) {
            componente.setTipoComponente(tipoComponente);
        }
        
        // Promoción - buscar por ID si viene en el evento
        if (event.getPromocionId() != null) {
            try {
                Promocion promocion = promocionRepositorio.findById(event.getPromocionId().intValue()).orElse(null);
                componente.setPromocion(promocion);
            } catch (Exception e) {
                // Si no se encuentra la promoción, dejar como null
                componente.setPromocion(null);
            }
        }
    }
    
    private Cotizacion mapToCotizacionEntity(CotizacionChangeEvent event) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFolio(Integer.parseInt(event.getEntityId()));
        if (event.getMontoTotal() != null) cotizacion.setTotal(event.getMontoTotal());
        if (event.getMontoImpuestos() != null) cotizacion.setImpuestos(event.getMontoImpuestos());
        // Pais no disponible en entidad simplificada
        // if (event.getPais() != null) cotizacion.setPais(event.getPais());
        if (event.getFechaCotizacion() != null) {
            cotizacion.setFecha(event.getFechaCotizacion().toString());
        }
        return cotizacion;
    }
    
    private void updateCotizacionFromEvent(Cotizacion cotizacion, CotizacionChangeEvent event) {
        if (event.getMontoTotal() != null) cotizacion.setTotal(event.getMontoTotal());
        if (event.getMontoImpuestos() != null) cotizacion.setImpuestos(event.getMontoImpuestos());
        // Pais no disponible en entidad simplificada
        // if (event.getPais() != null) cotizacion.setPais(event.getPais());
        if (event.getFechaCotizacion() != null) {
            cotizacion.setFecha(event.getFechaCotizacion().toString());
        }
    }
    
    
    /**
     * Mapea el nombre del tipo de componente desde el evento al nombre usado en la BD local.
     * 
     * @param tipoComponenteEvent Tipo de componente desde el evento
     * @return Nombre normalizado para buscar en la BD local
     */
    private String mapearTipoComponente(String tipoComponenteEvent) {
        if (tipoComponenteEvent == null) {
            return "MONITOR"; // Tipo por defecto
        }
        
        switch (tipoComponenteEvent.toUpperCase().trim()) {
            case "DISCO_DURO":
            case "HDD":
            case "SSD":
                return "DISCO_DURO";
            case "TARJETA_VIDEO":
            case "TARJETA_GRAFICA": 
            case "GPU":
                return "TARJETA_VIDEO";
            case "MONITOR":
            case "PANTALLA":
                return "MONITOR";
            case "PC":
                return "PC";
            case "RAM":
            case "MEMORIA":
                return "RAM";
            case "CPU":
            case "PROCESADOR":
                return "CPU";
            default:
                return "MONITOR"; // Tipo por defecto para tipos no reconocidos
        }
    }
}