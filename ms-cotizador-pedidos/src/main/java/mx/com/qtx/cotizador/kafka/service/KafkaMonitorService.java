package mx.com.qtx.cotizador.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de monitoreo para eventos Kafka.
 * 
 * Proporciona métricas y monitoreo de eventos Kafka para ms-cotizador-pedidos:
 * - Contadores de mensajes procesados por topic
 * - Registro de errores por topic
 * - Métricas de rendimiento
 * - Logs estructurados para análisis
 * 
 * @author Subagente4E - [2025-08-17 11:35:00 MST] - Servicio de monitoreo Kafka para ms-cotizador-pedidos
 */
@Service
public class KafkaMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitorService.class);
    private static final Logger metricsLogger = LoggerFactory.getLogger("kafka.metrics");
    
    // Contadores de mensajes procesados por topic
    private final ConcurrentHashMap<String, AtomicLong> processedMessages = new ConcurrentHashMap<>();
    
    // Contadores de errores por topic
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    
    // Últimas métricas por topic
    private final ConcurrentHashMap<String, TopicMetrics> topicMetrics = new ConcurrentHashMap<>();
    
    /**
     * Clase para almacenar métricas por topic
     */
    private static class TopicMetrics {
        private final AtomicLong processedCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private volatile LocalDateTime lastProcessed;
        private volatile LocalDateTime lastError;
        private volatile String lastErrorMessage;
        
        public void updateProcessed() {
            processedCount.incrementAndGet();
            lastProcessed = LocalDateTime.now();
        }
        
        public void updateError(String errorMessage) {
            errorCount.incrementAndGet();
            lastError = LocalDateTime.now();
            lastErrorMessage = errorMessage;
        }
        
        public long getProcessedCount() { return processedCount.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public LocalDateTime getLastProcessed() { return lastProcessed; }
        public LocalDateTime getLastError() { return lastError; }
        public String getLastErrorMessage() { return lastErrorMessage; }
    }
    
    /**
     * Registra un mensaje procesado exitosamente.
     * 
     * @param topic Topic del mensaje procesado
     */
    public void recordMessageProcessed(String topic) {
        processedMessages.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
        topicMetrics.computeIfAbsent(topic, k -> new TopicMetrics()).updateProcessed();
        
        // Log estructurado para métricas
        metricsLogger.info("message_processed,topic={},timestamp={},service=ms-cotizador-pedidos", 
                          topic, LocalDateTime.now());
        
        logger.debug("Mensaje procesado exitosamente: topic={}, total={}", 
                    topic, getProcessedMessageCount(topic));
    }
    
    /**
     * Registra un error en el procesamiento de mensajes.
     * 
     * @param topic Topic donde ocurrió el error
     * @param error Excepción que causó el error
     */
    public void recordError(String topic, Exception error) {
        errorCounts.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
        topicMetrics.computeIfAbsent(topic, k -> new TopicMetrics()).updateError(error.getMessage());
        
        // Log estructurado para métricas de errores
        metricsLogger.error("message_error,topic={},error_type={},error_message={},timestamp={},service=ms-cotizador-pedidos", 
                           topic, error.getClass().getSimpleName(), error.getMessage(), LocalDateTime.now());
        
        logger.error("Error procesando mensaje: topic={}, total_errores={}, error={}", 
                    topic, getErrorCount(topic), error.getMessage());
    }
    
    /**
     * Obtiene el número total de mensajes procesados para un topic.
     * 
     * @param topic Topic a consultar
     * @return Número de mensajes procesados
     */
    public long getProcessedMessageCount(String topic) {
        AtomicLong counter = processedMessages.get(topic);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Obtiene el número total de errores para un topic.
     * 
     * @param topic Topic a consultar
     * @return Número de errores
     */
    public long getErrorCount(String topic) {
        AtomicLong counter = errorCounts.get(topic);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Obtiene la tasa de error para un topic.
     * 
     * @param topic Topic a consultar
     * @return Tasa de error (0.0 a 1.0)
     */
    public double getErrorRate(String topic) {
        long processed = getProcessedMessageCount(topic);
        long errors = getErrorCount(topic);
        long total = processed + errors;
        
        return total > 0 ? (double) errors / total : 0.0;
    }
    
    /**
     * Obtiene métricas detalladas para un topic.
     * 
     * @param topic Topic a consultar
     * @return Métricas del topic
     */
    public TopicHealth getTopicHealth(String topic) {
        TopicMetrics metrics = topicMetrics.get(topic);
        
        if (metrics == null) {
            return new TopicHealth(topic, 0, 0, 0.0, null, null, null, "NO_DATA");
        }
        
        long processed = metrics.getProcessedCount();
        long errors = metrics.getErrorCount();
        double errorRate = processed + errors > 0 ? (double) errors / (processed + errors) : 0.0;
        
        String healthStatus = determineHealthStatus(errorRate, metrics.getLastProcessed());
        
        return new TopicHealth(topic, processed, errors, errorRate, 
                              metrics.getLastProcessed(), metrics.getLastError(), 
                              metrics.getLastErrorMessage(), healthStatus);
    }
    
    /**
     * Determina el estado de salud de un topic basado en métricas.
     */
    private String determineHealthStatus(double errorRate, LocalDateTime lastProcessed) {
        // Si no hay actividad reciente (más de 30 minutos), estado INACTIVE
        if (lastProcessed == null || lastProcessed.isBefore(LocalDateTime.now().minusMinutes(30))) {
            return "INACTIVE";
        }
        
        // Si la tasa de error es alta (>10%), estado UNHEALTHY
        if (errorRate > 0.10) {
            return "UNHEALTHY";
        }
        
        // Si hay algunos errores pero no muchos (<5%), estado WARNING
        if (errorRate > 0.05) {
            return "WARNING";
        }
        
        // Todo normal
        return "HEALTHY";
    }
    
    /**
     * Genera reporte de métricas para todos los topics.
     */
    public void logMetricsReport() {
        logger.info("=== REPORTE DE MÉTRICAS KAFKA ===");
        
        for (String topic : topicMetrics.keySet()) {
            TopicHealth health = getTopicHealth(topic);
            
            logger.info("Topic: {} | Procesados: {} | Errores: {} | Tasa Error: {:.2f}% | Estado: {}", 
                       health.getTopic(), health.getProcessedCount(), health.getErrorCount(), 
                       health.getErrorRate() * 100, health.getHealthStatus());
            
            if (health.getLastError() != null) {
                logger.info("  Último error: {} - {}", health.getLastError(), health.getLastErrorMessage());
            }
            
            if (health.getLastProcessed() != null) {
                logger.info("  Última actividad: {}", health.getLastProcessed());
            }
        }
        
        logger.info("=== FIN REPORTE MÉTRICAS ===");
    }
    
    /**
     * Reinicia las métricas de un topic específico.
     * 
     * @param topic Topic a reiniciar
     */
    public void resetTopicMetrics(String topic) {
        processedMessages.remove(topic);
        errorCounts.remove(topic);
        topicMetrics.remove(topic);
        
        logger.info("Métricas reiniciadas para topic: {}", topic);
    }
    
    /**
     * Reinicia todas las métricas.
     */
    public void resetAllMetrics() {
        processedMessages.clear();
        errorCounts.clear();
        topicMetrics.clear();
        
        logger.info("Todas las métricas han sido reiniciadas");
    }
    
    /**
     * Clase para encapsular información de salud de un topic.
     */
    public static class TopicHealth {
        private final String topic;
        private final long processedCount;
        private final long errorCount;
        private final double errorRate;
        private final LocalDateTime lastProcessed;
        private final LocalDateTime lastError;
        private final String lastErrorMessage;
        private final String healthStatus;
        
        public TopicHealth(String topic, long processedCount, long errorCount, double errorRate,
                          LocalDateTime lastProcessed, LocalDateTime lastError, 
                          String lastErrorMessage, String healthStatus) {
            this.topic = topic;
            this.processedCount = processedCount;
            this.errorCount = errorCount;
            this.errorRate = errorRate;
            this.lastProcessed = lastProcessed;
            this.lastError = lastError;
            this.lastErrorMessage = lastErrorMessage;
            this.healthStatus = healthStatus;
        }
        
        // Getters
        public String getTopic() { return topic; }
        public long getProcessedCount() { return processedCount; }
        public long getErrorCount() { return errorCount; }
        public double getErrorRate() { return errorRate; }
        public LocalDateTime getLastProcessed() { return lastProcessed; }
        public LocalDateTime getLastError() { return lastError; }
        public String getLastErrorMessage() { return lastErrorMessage; }
        public String getHealthStatus() { return healthStatus; }
    }
}