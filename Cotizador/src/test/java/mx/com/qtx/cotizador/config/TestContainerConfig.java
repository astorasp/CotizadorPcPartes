package mx.com.qtx.cotizador.config;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de TestContainers para tests de integración.
 * 
 * Proporciona un contenedor MySQL 8.4.4 real para testing consistente
 * con la misma versión que se usará en producción.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {
    
    /**
     * Contenedor MySQL 8.4.4 para testing con configuración específica.
     * 
     * Características:
     * - Versión específica: mysql:8.4.4
     * - Base de datos: cotizador_test  
     * - Usuario: test_user
     * - Contraseña: test_password
     * - Configuración optimizada para testing
     * 
     * @return contenedor MySQL configurado y listo para usar
     */
    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.4.4"))
                .withDatabaseName("cotizador_test")
                .withUsername("test_user")
                .withPassword("test_password")
                .withInitScripts(List.of("sql/ddl.sql", "sql/dml.sql"))
                .withReuse(false); // Destruir contenedor al terminar tests
    }
} 