package mx.com.qtx.cotizador.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.restassured.RestAssured;
import mx.com.qtx.cotizador.config.TestContainerConfig;

/**
 * Clase base para todos los tests de integración.
 * 
 * Proporciona:
 * - Configuración compartida de TestContainers (MySQL)
 * - Configuración básica de RestAssured
 * - Perfil de test activado
 * - Puerto aleatorio para evitar conflictos
 * - Autenticación básica preconfigurada
 * 
 * Todos los tests de integración deben heredar de esta clase
 * para garantizar consistencia y uso de la misma base de datos.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
public abstract class BaseIntegrationTest {

    // Credenciales de autenticación para tests
    protected static final String USER_ADMIN = "test";
    protected static final String PASSWORD_ADMIN = "test123";
    
    @LocalServerPort
    protected int port;
    
    /**
     * Configuración dinámica de propiedades para el contenedor compartido.
     * Esto asegura que todas las pruebas usen la misma base de datos.
     */
    @DynamicPropertySource
    static void configureSharedTestProperties(DynamicPropertyRegistry registry) {
        // Las propiedades se configuran automáticamente por @ServiceConnection
        // pero podemos agregar configuraciones adicionales aquí si es necesario
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.data-locations", () -> "classpath:sql/dml.sql");
        registry.add("spring.jpa.show-sql", () -> "false"); // Menos ruido en logs
        registry.add("logging.level.org.springframework.web", () -> "INFO");
    }
    
    /**
     * Configuración global para todos los tests de integración.
     */
    @BeforeAll
    static void globalSetup() {
        // Habilitar logging de RestAssured solo en caso de fallos
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        System.out.println("""
            🧪 Iniciando suite de tests de integración
            📚 Base de datos compartida MySQL 8.4.4
            🔐 Autenticación: test/test123
            """);
    }
    
    /**
     * Configuración individual para cada test.
     * Se ejecuta antes de cada método @Test.
     */
    @BeforeEach
    protected void setUp() {
        // Resetear configuraciones que pueden cambiar entre tests
        RestAssured.reset();
        
        // Configurar RestAssured para cada test
        RestAssured.port = port;
        RestAssured.basePath = "/cotizador/v1/api";
        RestAssured.authentication = RestAssured.basic(USER_ADMIN, PASSWORD_ADMIN);
    }
    
    /**
     * Limpieza final al terminar toda la suite de tests.
     */
    @AfterAll
    static void globalCleanup() {
        System.out.println("""
            ✅ Suite de tests de integración completada
            🗄️ Contenedor MySQL será destruido automáticamente
            """);
        
        // Limpiar configuración de RestAssured
        RestAssured.reset();
    }
} 