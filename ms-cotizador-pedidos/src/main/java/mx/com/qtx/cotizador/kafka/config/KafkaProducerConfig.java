package mx.com.qtx.cotizador.kafka.config;

import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del productor Kafka para ms-cotizador-pedidos.
 * 
 * Configura el productor Kafka para enviar eventos de cambio de pedidos y proveedores
 * a otros microservicios con:
 * - Serialización JSON automática
 * - Configuración optimizada para throughput
 * - Garantías de entrega configurables
 * - Manejo de errores y retries
 * 
 * @author Subagente4E - [2025-08-17 11:05:00 MST] - Configuración de productor Kafka para ms-cotizador-pedidos
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.producer.acks}")
    private String acks;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Value("${kafka.producer.batch-size}")
    private int batchSize;

    @Value("${kafka.producer.linger-ms}")
    private int lingerMs;

    @Value("${kafka.producer.buffer-memory}")
    private int bufferMemory;

    /**
     * Configuración del productor Kafka optimizada para eventos de cambio.
     * 
     * @return ProducerFactory configurado para serialización JSON
     */
    @Bean
    public ProducerFactory<String, BaseChangeEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Configuración básica de conexión
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Configuración de serialización
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Configuración de garantías de entrega
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        
        // Configuración de rendimiento
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Configuración para idempotencia (evitar duplicados)
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Configuración de timeouts
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Template de Kafka para envío de mensajes.
     * 
     * @return KafkaTemplate configurado
     */
    @Bean
    public KafkaTemplate<String, BaseChangeEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}