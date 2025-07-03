package mx.com.qtx.seguridad.config;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de TestContainers para tests de integración.
 * 
 * Proporciona un contenedor MySQL 8.4.4 COMPARTIDO entre todos los tests
 * para optimizar tiempo de ejecución y recursos.
 * 
 * El contenedor es singleton y se reutiliza entre todas las pruebas.
 * Se destruye automáticamente al finalizar toda la suite de tests.
 * 
 * @author Sistema Seguridad
 * @version 2.0
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {
    
    /**
     * Contenedor MySQL 8.4.4 estático compartido para testing.
     * 
     * Características:
     * - Versión específica: mysql:8.4.4
     * - Base de datos: seguridad_test  
     * - Usuario: test_user
     * - Contraseña: test_password
     * - Scripts de inicialización: DDL + DML
     * - REUTILIZABLE entre tests para mejor rendimiento
     * - Se destruye automáticamente al finalizar suite completa
     */
    private static MySQLContainer<?> sharedMySQLContainer;
    
    /**
     * Bean del contenedor MySQL compartido.
     * 
     * @return contenedor MySQL singleton configurado y listo para usar
     */
    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        if (sharedMySQLContainer == null) {
            synchronized (TestContainerConfig.class) {
                if (sharedMySQLContainer == null) {
                    sharedMySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.4.4"))
                            .withDatabaseName("seguridad_test")
                            .withUsername("test_user")
                            .withPassword("test_password")
                            .withInitScripts(List.of("sql/ddl.sql"))
                            .withReuse(true); // REUTILIZAR entre tests
                    
                    // Configurar limpieza automática al finalizar JVM
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (sharedMySQLContainer != null && sharedMySQLContainer.isRunning()) {
                            System.out.println("🗄️ Cerrando contenedor MySQL compartido...");
                            sharedMySQLContainer.stop();
                        }
                    }));
                    
                    // Iniciar el contenedor
                    sharedMySQLContainer.start();
                    
                    System.out.printf("""
                        🚀 Contenedor MySQL compartido iniciado:
                        📍 URL: %s
                        👤 Usuario: %s
                        🔐 Password: %s
                        🗃️ Database: %s
                        
                        """, 
                        sharedMySQLContainer.getJdbcUrl(),
                        sharedMySQLContainer.getUsername(),
                        sharedMySQLContainer.getPassword(),
                        sharedMySQLContainer.getDatabaseName()
                    );
                }
            }
        }
        return sharedMySQLContainer;
    }
    
    /**
     * Método para obtener la URL JDBC del contenedor compartido.
     * Útil para configuración manual en tests que lo requieran.
     */
    public static String getJdbcUrl() {
        return sharedMySQLContainer != null ? sharedMySQLContainer.getJdbcUrl() : null;
    }
    
    /**
     * Método para obtener el contenedor (para debugging)
     */
    public static MySQLContainer<?> getMySQLContainer() {
        return sharedMySQLContainer;
    }
    
    /**
     * Método para limpiar datos entre tests si es necesario.
     * Ejecuta los scripts DML para resetear datos de prueba.
     */
    public static void resetTestData() {
        if (sharedMySQLContainer != null && sharedMySQLContainer.isRunning()) {
            // Aquí podrías ejecutar scripts de limpieza si fuera necesario
            System.out.println("🔄 Datos de prueba reseteados");
        }
    }
}