package mx.com.qtx.cotizador.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del consumidor Kafka para ms-cotizador-cotizaciones.
 * 
 * Configura el consumidor Kafka para recibir eventos de componentes y promociones
 * desde ms-cotizador-componentes con:
 * - Deserialización JSON automática
 * - Manual acknowledgment para garantizar procesamiento
 * - Configuración optimizada para latencia baja
 * - Manejo de errores integrado
 * 
 * @author Subagente3E - [2025-01-17 18:45:00 MST] - Configuración de consumidor Kafka
 */
@Configuration
@EnableRetry
@Profile("!test")
public class KafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Value("${kafka.consumer.fetch-min-size}")
    private int fetchMinSize;

    @Value("${kafka.consumer.fetch-max-wait}")
    private int fetchMaxWait;

    @Value("${kafka.consumer.max-poll-records}")
    private int maxPollRecords;


    /**
     * Configuración del consumidor Kafka optimizada para eventos de cambio.
     * 
     * @return ConsumerFactory configurado para deserialización JSON
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Configuración básica de conexión
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        
        // Configuración de deserialización con ErrorHandlingDeserializer
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        
        // Configuración de commit manual para garantizar procesamiento
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        
        // Configuración de rendimiento
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinSize);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWait);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        
        // Configuración específica para deserialización JSON con polimorfismo
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "mx.com.qtx.cotizador.kafka.dto");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "COMPONENTE_CHANGE:mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent," +
            "PC_CHANGE:mx.com.qtx.cotizador.kafka.dto.PcChangeEvent," +
            "PROMOCION_CHANGE:mx.com.qtx.cotizador.kafka.dto.PromocionChangeEvent");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Factory para listeners de contenedores Kafka con acknowledgment manual.
     * 
     * @return ConcurrentKafkaListenerContainerFactory configurado
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Configurar acknowledgment manual para garantizar procesamiento
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Configurar concurrencia (1 consumer por partition por defecto)
        factory.setConcurrency(1);
        
        // Habilitar batch processing si es necesario
        factory.setBatchListener(false);
        
        // El manejo de errores se hace en los listeners con @Retryable
        
        return factory;
    }

    // Configuración de RetryTopic se maneja directamente en @RetryableTopic
    // No necesitamos bean separado para RetryTopicConfiguration
}