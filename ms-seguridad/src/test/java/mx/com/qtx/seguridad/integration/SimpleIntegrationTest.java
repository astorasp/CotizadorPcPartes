package mx.com.qtx.seguridad.integration;

import mx.com.qtx.seguridad.config.TestContainerConfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple para verificar configuración de TestContainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Testcontainers
class SimpleIntegrationTest {

    @Test
    void contextLoads() {
        // Este test simplemente verifica que el contexto se carga correctamente
        assertTrue(true);
        System.out.println("¡Contexto de Spring cargado exitosamente!");
    }

    @Test
    void testContainerMySQLIsRunning() {
        var container = TestContainerConfig.getMySQLContainer();
        assertNotNull(container);
        assertTrue(container.isRunning());
        assertNotNull(container.getJdbcUrl());
        System.out.println("MySQL TestContainer está funcionando: " + container.getJdbcUrl());
    }
}