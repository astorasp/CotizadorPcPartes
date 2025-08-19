package mx.com.qtx.cotizador.kafka.service;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.CotizacionChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

/**
 * Servicio para publicar eventos de cambio de cotizaciones en Kafka.
 * 
 * Se integra con los servicios de cotizaciones para publicar automáticamente
 * eventos cuando ocurren cambios en cotizaciones.
 */
@Service
@Profile("!test")
public class EventPublishingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.cotizaciones-changes}")
    private String cotizacionesTopic;
    
    @Value("${kafka.producer.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * Publica evento de creación de cotización.
     * 
     * @param cotizacion Cotización creada
     */
    @Async
    public void publishCotizacionCreated(Cotizacion cotizacion) {
        try {
            if (!kafkaEnabled) {
                logger.debug("Kafka desactivado - Evento de cotización no enviado: ID={}", cotizacion.getNum());
                return;
            }
            
            CotizacionChangeEvent event = new CotizacionChangeEvent(
                BaseChangeEvent.OperationType.CREATE,
                cotizacion.getNum(),
                "N/A", // cliente - no está disponible en dominio
                "Cotización generada", // descripción  
                "PENDIENTE", // estado por defecto
                cotizacion.getTotal(),
                null, // montoDscto - no disponible en dominio
                cotizacion.getTotalImpuestos(),
                "MX", // país por defecto
                cotizacion.getFecha() != null ? cotizacion.getFecha().atStartOfDay() : null,
                "A" // algoritmo por defecto
            );

            kafkaTemplate.send(cotizacionesTopic, String.valueOf(cotizacion.getNum()), event);
            logger.info("Evento de creación de cotización enviado: ID={}", 
                       cotizacion.getNum());

        } catch (Exception e) {
            logger.error("Error publicando evento de creación de cotización {}: {}", 
                        cotizacion.getNum(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de actualización de cotización.
     * 
     * @param cotizacion Cotización actualizada
     */
    @Async
    public void publishCotizacionUpdated(Cotizacion cotizacion) {
        try {
            if (!kafkaEnabled) {
                logger.debug("Kafka desactivado - Evento de cotización no enviado: ID={}", cotizacion.getNum());
                return;
            }
            
            CotizacionChangeEvent event = new CotizacionChangeEvent(
                BaseChangeEvent.OperationType.UPDATE,
                cotizacion.getNum(),
                "N/A", // cliente - no está disponible en dominio
                "Cotización actualizada", // descripción
                "PENDIENTE", // estado por defecto
                cotizacion.getTotal(),
                null, // montoDscto - no disponible en dominio
                cotizacion.getTotalImpuestos(),
                "MX", // país por defecto
                cotizacion.getFecha() != null ? cotizacion.getFecha().atStartOfDay() : null,
                "A" // algoritmo por defecto
            );

            kafkaTemplate.send(cotizacionesTopic, String.valueOf(cotizacion.getNum()), event);
            logger.info("Evento de actualización de cotización enviado: ID={}", 
                       cotizacion.getNum());

        } catch (Exception e) {
            logger.error("Error publicando evento de actualización de cotización {}: {}", 
                        cotizacion.getNum(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de eliminación de cotización.
     * 
     * @param cotizacionId ID de la cotización eliminada
     */
    @Async
    public void publishCotizacionDeleted(Integer cotizacionId) {
        try {
            if (!kafkaEnabled) {
                logger.debug("Kafka desactivado - Evento de cotización no enviado: ID={}", cotizacionId);
                return;
            }
            
            CotizacionChangeEvent event = new CotizacionChangeEvent(
                BaseChangeEvent.OperationType.DELETE,
                cotizacionId.longValue()
            );

            kafkaTemplate.send(cotizacionesTopic, String.valueOf(cotizacionId), event);
            logger.info("Evento de eliminación de cotización enviado: ID={}", cotizacionId);

        } catch (Exception e) {
            logger.error("Error publicando evento de eliminación de cotización {}: {}", 
                        cotizacionId, e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si Kafka está habilitado.
     */
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }
}