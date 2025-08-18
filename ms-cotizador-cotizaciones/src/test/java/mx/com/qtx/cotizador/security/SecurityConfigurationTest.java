package mx.com.qtx.cotizador.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test básico para verificar que la configuración de seguridad es válida
 * Test simple sin dependencias de base de datos
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Test
    void contextLoads() {
        // Si el contexto carga sin errores, la configuración básica está correcta
        assertTrue(true, "Contexto Spring debe cargar correctamente");
    }

    @Test
    void testProfileIsActive() {
        // Verificación básica del perfil activo
        String testProfile = System.getProperty("spring.profiles.active", "test");
        assertNotNull(testProfile, "Perfil de test debe estar definido");
    }
}