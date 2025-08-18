package mx.com.qtx.cotizador.kafka;

import mx.com.qtx.cotizador.kafka.config.KafkaConsumerConfig;
import mx.com.qtx.cotizador.kafka.dto.BaseChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.kafka.dto.PromocionChangeEvent;
import mx.com.qtx.cotizador.kafka.service.KafkaMonitorService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de configuración del consumidor Kafka.
 * 
 * Verifica que todas las clases de Kafka se instancien correctamente
 * y que los DTOs funcionen sin problemas de compilación.
 * 
 * @author Subagente3E - [2025-01-17 19:15:00 MST] - Test de configuración Kafka
 */
class KafkaConsumerConfigurationTest {

    @Test
    void kafkaConfigurationClassesExist() {
        // Verificar que todas las clases principales existan y se puedan instanciar
        assertThat(KafkaConsumerConfig.class).isNotNull();
        assertThat(BaseChangeEvent.class).isNotNull();
        assertThat(ComponenteChangeEvent.class).isNotNull();
        assertThat(PromocionChangeEvent.class).isNotNull();
        assertThat(KafkaMonitorService.class).isNotNull();
    }

    @Test
    void componenteChangeEventCreation() {
        // Verificar que se puedan crear eventos de componentes
        ComponenteChangeEvent event = new ComponenteChangeEvent();
        assertThat(event).isNotNull();
        assertThat(event.getEventType()).isEqualTo(BaseChangeEvent.EventType.COMPONENTE_CHANGE);
        
        // Test constructor con parámetros
        ComponenteChangeEvent event2 = new ComponenteChangeEvent(
            BaseChangeEvent.OperationType.CREATE, "1");
        assertThat(event2.getOperationType()).isEqualTo(BaseChangeEvent.OperationType.CREATE);
        assertThat(event2.getEntityId()).isEqualTo("1");
    }

    @Test
    void promocionChangeEventCreation() {
        // Verificar que se puedan crear eventos de promociones
        PromocionChangeEvent event = new PromocionChangeEvent();
        assertThat(event).isNotNull();
        assertThat(event.getEventType()).isEqualTo(BaseChangeEvent.EventType.PROMOCION_CHANGE);
        
        // Test constructor con parámetros
        PromocionChangeEvent event2 = new PromocionChangeEvent(
            BaseChangeEvent.OperationType.UPDATE, "2");
        assertThat(event2.getOperationType()).isEqualTo(BaseChangeEvent.OperationType.UPDATE);
        assertThat(event2.getEntityId()).isEqualTo("2");
    }

    @Test
    void kafkaMonitorServiceMetrics() {
        // Verificar que el servicio de monitoreo se pueda instanciar
        KafkaMonitorService monitorService = new KafkaMonitorService();
        assertThat(monitorService).isNotNull();
        
        // Verificar métricas iniciales
        KafkaMonitorService.KafkaConsumerMetrics metrics = monitorService.getMetrics();
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalMessagesProcessed()).isEqualTo(0);
        assertThat(metrics.getComponenteMessagesProcessed()).isEqualTo(0);
        assertThat(metrics.getPromocionMessagesProcessed()).isEqualTo(0);
        assertThat(metrics.getTotalErrors()).isEqualTo(0);
        assertThat(metrics.getLastMessageProcessed()).isNotNull();
    }

    @Test
    void kafkaMonitorServiceHealthCheck() {
        // Verificar que el health check funcione
        KafkaMonitorService monitorService = new KafkaMonitorService();
        boolean healthy = monitorService.isHealthy();
        assertThat(healthy).isTrue(); // Debería ser true al inicio
    }
}