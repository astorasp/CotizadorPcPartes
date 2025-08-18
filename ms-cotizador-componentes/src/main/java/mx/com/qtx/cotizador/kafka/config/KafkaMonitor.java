package mx.com.qtx.cotizador.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para monitorear el estado de Kafka.
 * 
 * Verifica la conectividad con el cluster de Kafka y proporciona información
 * de estado para monitoreo.
 * 
 * @author Subagente2E - [2025-01-17 18:05:00 MST] - Monitor para Kafka
 */
@Component
public class KafkaMonitor {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitor.class);

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Value("${kafka.producer.enabled:true}")
    private boolean kafkaEnabled;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> checkKafkaHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        if (!kafkaEnabled) {
            healthStatus.put("status", "Kafka disabled");
            healthStatus.put("enabled", false);
            return healthStatus;
        }

        try {
            // Crear cliente admin para verificar conectividad
            AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
            
            // Intentar describir el cluster con timeout
            DescribeClusterOptions options = new DescribeClusterOptions()
                .timeoutMs(5000);
            
            DescribeClusterResult clusterResult = adminClient.describeCluster(options);
            
            // Obtener información del cluster
            String clusterId = clusterResult.clusterId().get(5, TimeUnit.SECONDS);
            int nodeCount = clusterResult.nodes().get(5, TimeUnit.SECONDS).size();
            
            adminClient.close();
            
            healthStatus.put("status", "Kafka connected");
            healthStatus.put("enabled", true);
            healthStatus.put("bootstrap-servers", bootstrapServers);
            healthStatus.put("cluster-id", clusterId);
            healthStatus.put("node-count", nodeCount);
            
            logger.info("Kafka health check passed - Cluster: {}, Nodes: {}", clusterId, nodeCount);
                
        } catch (Exception e) {
            healthStatus.put("status", "Kafka connection failed");
            healthStatus.put("enabled", true);
            healthStatus.put("bootstrap-servers", bootstrapServers);
            healthStatus.put("error", e.getMessage());
            
            logger.warn("Kafka health check failed: {}", e.getMessage());
        }
        
        return healthStatus;
    }

    public boolean isKafkaHealthy() {
        if (!kafkaEnabled) {
            return false;
        }
        
        try {
            AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
            DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs(3000);
            adminClient.describeCluster(options).clusterId().get(3, TimeUnit.SECONDS);
            adminClient.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}