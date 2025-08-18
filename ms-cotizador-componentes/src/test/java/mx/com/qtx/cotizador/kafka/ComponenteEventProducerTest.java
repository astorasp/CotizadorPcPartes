package mx.com.qtx.cotizador.kafka;

import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.producer.ComponenteEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el ComponenteEventProducer.
 * 
 * Verifica que los eventos se envían correctamente usando Kafka embebido.
 * 
 * @author Subagente2E - [2025-01-17 18:00:00 MST] - Pruebas para productor Kafka
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
    },
    topics = {
        "test.componentes.changes",
        "test.promociones.changes"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9093",
    "kafka.bootstrap-servers=localhost:9093",
    "kafka.producer.enabled=true",
    "kafka.topics.componentes-changes=test.componentes.changes",
    "kafka.topics.promociones-changes=test.promociones.changes",
    "kafka.monitoring.log-events=true"
})
class ComponenteEventProducerTest {

    @Autowired
    private ComponenteEventProducer eventProducer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        // Configuración inicial si es necesaria
        assertNotNull(eventProducer);
        assertNotNull(kafkaTemplate);
    }

    @Test
    void testSendComponenteChangeEvent_Create_Success() throws Exception {
        // Arrange
        ComponenteChangeEvent event = new ComponenteChangeEvent(
            BaseChangeEvent.OperationType.CREATE,
            1L,
            "Test Component",
            "Component for testing",
            150.0,
            "TestBrand",
            "TestModel",
            "MONITOR",
            1L,
            "Test specifications",
            true
        );

        // Act
        CompletableFuture<SendResult<String, Object>> future = eventProducer.sendComponenteChangeEvent(event);

        // Assert
        assertNotNull(future);
        SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);
        assertNotNull(result);
        assertNotNull(result.getRecordMetadata());
        assertEquals("test.componentes.changes", result.getRecordMetadata().topic());
        assertTrue(result.getRecordMetadata().offset() >= 0);
    }

    @Test
    void testSendComponenteChangeEvent_Update_Success() throws Exception {
        // Arrange
        ComponenteChangeEvent event = new ComponenteChangeEvent(
            BaseChangeEvent.OperationType.UPDATE,
            2L,
            "Updated Component",
            "Updated description",
            200.0,
            "UpdatedBrand",
            "UpdatedModel",
            "DISCO_DURO",
            2L,
            "Updated specifications",
            true
        );

        // Act
        CompletableFuture<SendResult<String, Object>> future = eventProducer.sendComponenteChangeEvent(event);

        // Assert
        assertNotNull(future);
        SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals("test.componentes.changes", result.getRecordMetadata().topic());
    }

    @Test
    void testSendComponenteChangeEvent_Delete_Success() throws Exception {
        // Arrange
        ComponenteChangeEvent event = new ComponenteChangeEvent(
            BaseChangeEvent.OperationType.DELETE,
            3L
        );

        // Act
        CompletableFuture<SendResult<String, Object>> future = eventProducer.sendComponenteChangeEvent(event);

        // Assert
        assertNotNull(future);
        SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals("test.componentes.changes", result.getRecordMetadata().topic());
    }

    @Test
    void testKafkaEnabled() {
        // Act & Assert
        assertTrue(eventProducer.isKafkaEnabled());
    }

    @Test
    void testEventKeyGeneration() throws Exception {
        // Arrange
        ComponenteChangeEvent event = new ComponenteChangeEvent(
            BaseChangeEvent.OperationType.CREATE,
            123L
        );

        // Act
        CompletableFuture<SendResult<String, Object>> future = eventProducer.sendComponenteChangeEvent(event);
        SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);

        // Assert
        // Verificamos que se envió correctamente
        assertNotNull(result);
        assertTrue(result.getRecordMetadata().offset() >= 0);
    }
}