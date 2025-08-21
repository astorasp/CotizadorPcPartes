package mx.com.qtx.cotizador.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración principal de Kafka para el microservicio de componentes.
 * 
 * Configura el productor Kafka con configuraciones optimizadas para:
 * - Alta disponibilidad (acks=all, retries, idempotencia)
 * - Rendimiento (compresión, batching, buffering)
 * - Consistencia (enable.idempotence=true)
 * 
 * @author Subagente2E - [2025-01-17 16:30:00 MST] - Configuración de productor Kafka
 */
@Configuration
@EnableRetry
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.producer.batch-size}")
    private int batchSize;

    @Value("${kafka.producer.linger-ms}")
    private int lingerMs;

    @Value("${kafka.producer.buffer-memory}")
    private long bufferMemory;

    @Value("${kafka.producer.compression-type}")
    private String compressionType;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Value("${kafka.producer.retry-backoff-ms}")
    private int retryBackoffMs;

    @Value("${kafka.producer.max-in-flight}")
    private int maxInFlight;

    @Value("${kafka.producer.acks}")
    private String acks;

    @Value("${kafka.producer.enable-idempotence}")
    private boolean enableIdempotence;

    @Value("${kafka.producer.request-timeout-ms}")
    private int requestTimeoutMs;

    @Value("${kafka.producer.delivery-timeout-ms}")
    private int deliveryTimeoutMs;

    /**
     * Configuración del productor Kafka con configuraciones optimizadas.
     * 
     * @return ProducerFactory configurado para envío de eventos
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Configuración básica de conexión
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Configuración de rendimiento
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        
        // Configuración de confiabilidad
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlight);
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        
        // Configuración de timeouts
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        
        // Configuración adicional para JSON con type headers para polimorfismo
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate principal para envío de eventos.
     * 
     * @return KafkaTemplate configurado
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}