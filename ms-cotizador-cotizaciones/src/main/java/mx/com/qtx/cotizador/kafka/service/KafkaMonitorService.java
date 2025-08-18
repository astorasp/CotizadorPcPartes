package mx.com.qtx.cotizador.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de monitoreo para el consumidor Kafka.
 * 
 * Proporciona métricas y monitoring del estado del consumidor,
 * incluyendo contadores de mensajes procesados, errores y latencia.
 * 
 * @author Subagente3E - [2025-01-17 19:05:00 MST] - Monitoreo de consumidor Kafka
 */
@Service
public class KafkaMonitorService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitorService.class);
    
    // Contadores para métricas
    private final AtomicLong totalMessagesProcessed = new AtomicLong(0);
    private final AtomicLong componenteMessagesProcessed = new AtomicLong(0);
    private final AtomicLong promocionMessagesProcessed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    private volatile LocalDateTime lastMessageProcessed = LocalDateTime.now();
    
    @Value("${kafka.topics.componentes-changes}")
    private String componentesTopic;
    
    @Value("${kafka.topics.promociones-changes}")
    private String promocionesTopic;
    
    /**
     * Registra el procesamiento exitoso de un mensaje.
     */
    public void recordMessageProcessed(String topic) {
        totalMessagesProcessed.incrementAndGet();
        lastMessageProcessed = LocalDateTime.now();
        
        if (componentesTopic.equals(topic)) {
            componenteMessagesProcessed.incrementAndGet();
        } else if (promocionesTopic.equals(topic)) {
            promocionMessagesProcessed.incrementAndGet();
        }
        
        // Log cada 10 mensajes procesados
        long total = totalMessagesProcessed.get();
        if (total % 10 == 0) {
            logger.info("Kafka Consumer Stats - Total: {}, Componentes: {}, Promociones: {}, Errores: {}", 
                       total, componenteMessagesProcessed.get(), promocionMessagesProcessed.get(), totalErrors.get());
        }
    }
    
    /**
     * Registra un error durante el procesamiento.
     */
    public void recordError(String topic, Exception error) {
        totalErrors.incrementAndGet();
        logger.error("Error registrado en topic {}: {}", topic, error.getMessage());
    }
    
    /**
     * Obtiene las métricas actuales del consumidor.
     */
    public KafkaConsumerMetrics getMetrics() {
        return new KafkaConsumerMetrics(
            totalMessagesProcessed.get(),
            componenteMessagesProcessed.get(),
            promocionMessagesProcessed.get(),
            totalErrors.get(),
            lastMessageProcessed
        );
    }
    
    /**
     * Verifica el estado de salud del consumidor.
     */
    public boolean isHealthy() {
        // Considerar unhealthy si no se han procesado mensajes en los últimos 5 minutos
        // y si la tasa de errores es mayor al 10%
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        boolean recentActivity = lastMessageProcessed.isAfter(fiveMinutesAgo);
        
        long total = totalMessagesProcessed.get();
        double errorRate = total > 0 ? (double) totalErrors.get() / total : 0.0;
        boolean lowErrorRate = errorRate < 0.1; // Menos del 10% de errores
        
        return recentActivity || lowErrorRate;
    }
    
    /**
     * Clase para encapsular métricas del consumidor.
     */
    public static class KafkaConsumerMetrics {
        private final long totalMessagesProcessed;
        private final long componenteMessagesProcessed;
        private final long promocionMessagesProcessed;
        private final long totalErrors;
        private final LocalDateTime lastMessageProcessed;
        
        public KafkaConsumerMetrics(long totalMessagesProcessed, long componenteMessagesProcessed,
                                   long promocionMessagesProcessed, long totalErrors,
                                   LocalDateTime lastMessageProcessed) {
            this.totalMessagesProcessed = totalMessagesProcessed;
            this.componenteMessagesProcessed = componenteMessagesProcessed;
            this.promocionMessagesProcessed = promocionMessagesProcessed;
            this.totalErrors = totalErrors;
            this.lastMessageProcessed = lastMessageProcessed;
        }
        
        // Getters
        public long getTotalMessagesProcessed() { return totalMessagesProcessed; }
        public long getComponenteMessagesProcessed() { return componenteMessagesProcessed; }
        public long getPromocionMessagesProcessed() { return promocionMessagesProcessed; }
        public long getTotalErrors() { return totalErrors; }
        public LocalDateTime getLastMessageProcessed() { return lastMessageProcessed; }
        
        public double getErrorRate() {
            return totalMessagesProcessed > 0 ? (double) totalErrors / totalMessagesProcessed : 0.0;
        }
    }
}