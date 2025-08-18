package mx.com.qtx.cotizador.kafka.service;

import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.entidad.DetallePromocion;
import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.PcChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.PromocionChangeEvent;
import mx.com.qtx.cotizador.kafka.producer.ComponenteEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio simplificado para publicar eventos de cambio en Kafka.
 * 
 * Se integra con los servicios existentes para publicar automáticamente
 * eventos cuando ocurren cambios en componentes, promociones y PCs.
 * 
 * @author Subagente2E - [2025-01-17 19:30:00 MST] - Servicio simplificado de publicación de eventos
 */
@Service
public class EventPublishingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);

    @Autowired
    private ComponenteEventProducer eventProducer;

    /**
     * Publica evento de creación de componente.
     * 
     * @param componente Componente creado
     */
    @Async
    public void publishComponenteCreated(Componente componente) {
        try {
            if (!eventProducer.isKafkaEnabled()) {
                logger.debug("Kafka desactivado - Evento de componente no enviado: ID={}", componente.getId());
                return;
            }
            
            ComponenteChangeEvent event = new ComponenteChangeEvent(
                BaseChangeEvent.OperationType.CREATE,
                componente.getId(),
                componente.getDescripcion(),
                componente.getPrecioBase() != null ? componente.getPrecioBase().doubleValue() : null,
                componente.getMarca(),
                componente.getModelo(),
                componente.getTipoComponente() != null ? componente.getTipoComponente().getNombre() : null,
                componente.getPromocion() != null ? componente.getPromocion().getIdPromocion().longValue() : null,
                componente.getCapacidadAlm(),
                componente.getMemoria(),
                true // activo - asumir true para componentes creados
            );

            eventProducer.sendComponenteChangeEvent(event);
            logger.info("Evento de creación de componente enviado: ID={}", componente.getId());

        } catch (Exception e) {
            logger.error("Error publicando evento de creación de componente {}: {}", componente.getId(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de actualización de componente.
     * 
     * @param componente Componente actualizado
     */
    @Async
    public void publishComponenteUpdated(Componente componente) {
        try {
            if (!eventProducer.isKafkaEnabled()) {
                logger.debug("Kafka desactivado - Evento de componente no enviado: ID={}", componente.getId());
                return;
            }
            
            ComponenteChangeEvent event = new ComponenteChangeEvent(
                BaseChangeEvent.OperationType.UPDATE,
                componente.getId(),
                componente.getDescripcion(),
                componente.getPrecioBase() != null ? componente.getPrecioBase().doubleValue() : null,
                componente.getMarca(),
                componente.getModelo(),
                componente.getTipoComponente() != null ? componente.getTipoComponente().getNombre() : null,
                componente.getPromocion() != null ? componente.getPromocion().getIdPromocion().longValue() : null,
                componente.getCapacidadAlm(),
                componente.getMemoria(),
                true // activo - asumir true para componentes actualizados
            );

            eventProducer.sendComponenteChangeEvent(event);
            logger.info("Evento de actualización de componente enviado: ID={}", componente.getId());

        } catch (Exception e) {
            logger.error("Error publicando evento de actualización de componente {}: {}", componente.getId(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de eliminación de componente.
     * 
     * @param componenteId ID del componente eliminado
     */
    @Async
    public void publishComponenteDeleted(String componenteId) {
        try {
            ComponenteChangeEvent event = new ComponenteChangeEvent(
                BaseChangeEvent.OperationType.DELETE,
                componenteId
            );

            eventProducer.sendComponenteChangeEvent(event);
            logger.info("Evento de eliminación de componente enviado: ID={}", componenteId);

        } catch (Exception e) {
            logger.error("Error publicando evento de eliminación de componente {}: {}", componenteId, e.getMessage(), e);
        }
    }

    /**
     * Publica evento de creación de promoción.
     * 
     * @param promocion Promoción creada
     */
    @Async
    public void publishPromocionCreated(Promocion promocion) {
        try {
            if (!eventProducer.isKafkaEnabled()) {
                logger.debug("Kafka desactivado - Evento de promoción no enviado: ID={}", promocion.getIdPromocion());
                return;
            }
            
            // Obtener información de los detalles de promoción si existen
            String tipoPromBase = null;
            String tipoPromAcumulable = null;
            Double valorDescuento = null;
            Integer cantidadMinima = null;
            Integer cantidadMaxima = null;
            
            if (promocion.getDetalles() != null && !promocion.getDetalles().isEmpty()) {
                DetallePromocion detalle = promocion.getDetalles().get(0); // Tomar el primer detalle
                tipoPromBase = detalle.getTipoPromBase();
                tipoPromAcumulable = detalle.getTipoPromAcumulable();
                valorDescuento = detalle.getPorcDctoPlano();
                cantidadMinima = detalle.getPaguen();
                cantidadMaxima = detalle.getLlevent();
            }
            
            PromocionChangeEvent event = new PromocionChangeEvent(
                BaseChangeEvent.OperationType.CREATE,
                String.valueOf(promocion.getIdPromocion()),
                promocion.getNombre(),
                promocion.getDescripcion(),
                tipoPromBase,
                tipoPromAcumulable,
                promocion.getVigenciaDesde() != null ? promocion.getVigenciaDesde().atStartOfDay() : null,
                promocion.getVigenciaHasta() != null ? promocion.getVigenciaHasta().atTime(23, 59, 59) : null,
                true, // activa - asumir true para nuevas promociones
                valorDescuento,
                cantidadMinima,
                cantidadMaxima
            );

            eventProducer.sendPromocionChangeEvent(event);
            logger.info("Evento de creación de promoción enviado: ID={}, Nombre={}", promocion.getIdPromocion(), promocion.getNombre());

        } catch (Exception e) {
            logger.error("Error publicando evento de creación de promoción {}: {}", promocion.getIdPromocion(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de actualización de promoción.
     * 
     * @param promocion Promoción actualizada
     */
    @Async
    public void publishPromocionUpdated(Promocion promocion) {
        try {
            if (!eventProducer.isKafkaEnabled()) {
                logger.debug("Kafka desactivado - Evento de promoción no enviado: ID={}", promocion.getIdPromocion());
                return;
            }
            
            // Obtener información de los detalles de promoción si existen
            String tipoPromBase = null;
            String tipoPromAcumulable = null;
            Double valorDescuento = null;
            Integer cantidadMinima = null;
            Integer cantidadMaxima = null;
            
            if (promocion.getDetalles() != null && !promocion.getDetalles().isEmpty()) {
                DetallePromocion detalle = promocion.getDetalles().get(0); // Tomar el primer detalle
                tipoPromBase = detalle.getTipoPromBase();
                tipoPromAcumulable = detalle.getTipoPromAcumulable();
                valorDescuento = detalle.getPorcDctoPlano();
                cantidadMinima = detalle.getPaguen();
                cantidadMaxima = detalle.getLlevent();
            }
            
            PromocionChangeEvent event = new PromocionChangeEvent(
                BaseChangeEvent.OperationType.UPDATE,
                String.valueOf(promocion.getIdPromocion()),
                promocion.getNombre(),
                promocion.getDescripcion(),
                tipoPromBase,
                tipoPromAcumulable,
                promocion.getVigenciaDesde() != null ? promocion.getVigenciaDesde().atStartOfDay() : null,
                promocion.getVigenciaHasta() != null ? promocion.getVigenciaHasta().atTime(23, 59, 59) : null,
                true, // activa - determinar según lógica de negocio
                valorDescuento,
                cantidadMinima,
                cantidadMaxima
            );

            eventProducer.sendPromocionChangeEvent(event);
            logger.info("Evento de actualización de promoción enviado: ID={}, Nombre={}", promocion.getIdPromocion(), promocion.getNombre());

        } catch (Exception e) {
            logger.error("Error publicando evento de actualización de promoción {}: {}", promocion.getIdPromocion(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de eliminación de promoción.
     * 
     * @param promocionId ID de la promoción eliminada
     */
    @Async
    public void publishPromocionDeleted(Long promocionId) {
        try {
            PromocionChangeEvent event = new PromocionChangeEvent(
                BaseChangeEvent.OperationType.DELETE,
                String.valueOf(promocionId)
            );

            eventProducer.sendPromocionChangeEvent(event);
            logger.info("Evento de eliminación de promoción enviado: ID={}", promocionId);

        } catch (Exception e) {
            logger.error("Error publicando evento de eliminación de promoción {}: {}", promocionId, e.getMessage(), e);
        }
    }

    /**
     * Publica evento de creación de PC.
     * 
     * @param pcId ID de la PC creada
     * @param nombre Nombre de la PC
     * @param descripcion Descripción de la PC
     * @param precio Precio de la PC
     * @param activa Estado activo de la PC
     */
    @Async
    public void publishPcCreated(String pcId, String nombre, String descripcion, Double precio, Boolean activa) {
        try {
            PcChangeEvent event = new PcChangeEvent(
                BaseChangeEvent.OperationType.CREATE,
                pcId
            );

            eventProducer.sendPcChangeEvent(event);
            logger.info("Evento de creación de PC enviado: ID={}", pcId);

        } catch (Exception e) {
            logger.error("Error publicando evento de creación de PC {}: {}", pcId, e.getMessage(), e);
        }
    }

    /**
     * Publica evento de actualización de PC.
     * 
     * @param pcId ID de la PC actualizada
     * @param nombre Nombre de la PC
     * @param descripcion Descripción de la PC
     * @param precio Precio de la PC
     * @param activa Estado activo de la PC
     */
    @Async
    public void publishPcUpdated(String pcId, String nombre, String descripcion, Double precio, Boolean activa) {
        try {
            PcChangeEvent event = new PcChangeEvent(
                BaseChangeEvent.OperationType.UPDATE,
                pcId
            );

            eventProducer.sendPcChangeEvent(event);
            logger.info("Evento de actualización de PC enviado: ID={}", pcId);

        } catch (Exception e) {
            logger.error("Error publicando evento de actualización de PC {}: {}", pcId, e.getMessage(), e);
        }
    }

    /**
     * Publica evento de eliminación de PC.
     * 
     * @param pcId ID de la PC eliminada
     */
    @Async
    public void publishPcDeleted(String pcId) {
        try {
            PcChangeEvent event = new PcChangeEvent(
                BaseChangeEvent.OperationType.DELETE,
                pcId
            );

            eventProducer.sendPcChangeEvent(event);
            logger.info("Evento de eliminación de PC enviado: ID={}", pcId);

        } catch (Exception e) {
            logger.error("Error publicando evento de eliminación de PC {}: {}", pcId, e.getMessage(), e);
        }
    }
}