package mx.com.qtx.cotizador.controlador;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para monitoreo del consumidor Kafka.
 * 
 * Proporciona endpoints para consultar métricas y estado de salud
 * del consumidor Kafka de eventos de componentes y promociones.
 * 
 * @author Subagente3E - [2025-01-17 19:10:00 MST] - Controlador de monitoreo Kafka
 */
@RestController
@RequestMapping("/kafka/monitor")
public class KafkaMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitorController.class);
    
    @Autowired
    private KafkaMonitorService monitorService;

    /**
     * Obtiene las métricas actuales del consumidor Kafka.
     * Disponible solo para usuarios con rol ADMIN o GERENTE.
     * 
     * @return ResponseEntity con métricas del consumidor
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponse<KafkaMonitorService.KafkaConsumerMetrics>> getKafkaMetrics() {
        try {
            logger.debug("Consultando métricas del consumidor Kafka");
            
            KafkaMonitorService.KafkaConsumerMetrics metrics = monitorService.getMetrics();
            
            logger.debug("Métricas obtenidas: total={}, componentes={}, promociones={}, errores={}", 
                        metrics.getTotalMessagesProcessed(), 
                        metrics.getComponenteMessagesProcessed(),
                        metrics.getPromocionMessagesProcessed(),
                        metrics.getTotalErrors());
            
            return ResponseEntity.ok(new ApiResponse<>("200", "Métricas del consumidor Kafka obtenidas exitosamente", metrics));
            
        } catch (Exception e) {
            logger.error("Error obteniendo métricas del consumidor Kafka", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>("500", "Error interno obteniendo métricas del consumidor Kafka"));
        }
    }

    /**
     * Verifica el estado de salud del consumidor Kafka.
     * Disponible para todos los usuarios autenticados.
     * 
     * @return ResponseEntity con estado de salud
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO', 'VENDEDOR', 'CONSULTOR')")
    public ResponseEntity<ApiResponse<HealthStatus>> getKafkaHealth() {
        try {
            logger.debug("Verificando estado de salud del consumidor Kafka");
            
            boolean healthy = monitorService.isHealthy();
            HealthStatus status = new HealthStatus(healthy, healthy ? "Consumidor funcionando correctamente" : "Consumidor con problemas");
            
            logger.debug("Estado de salud del consumidor: {}", healthy ? "HEALTHY" : "UNHEALTHY");
            
            return ResponseEntity.ok(new ApiResponse<>("200", "Estado de salud del consumidor verificado", status));
            
        } catch (Exception e) {
            logger.error("Error verificando estado de salud del consumidor Kafka", e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>("500", "Error interno verificando estado de salud del consumidor"));
        }
    }
    
    /**
     * Clase para representar el estado de salud del consumidor.
     */
    public static class HealthStatus {
        private final boolean healthy;
        private final String message;
        
        public HealthStatus(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }
        
        public boolean isHealthy() {
            return healthy;
        }
        
        public String getMessage() {
            return message;
        }
    }
}