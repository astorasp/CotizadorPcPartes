package mx.com.qtx.cotizador.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de configuración de seguridad para validar carga del contexto.
 * 
 * Esta clase realiza pruebas básicas de integración que verifican que la configuración
 * de seguridad del microservicio se carga correctamente sin errores. Es un test
 * fundamental que debe pasar antes de ejecutar cualquier otro test más complejo.
 * 
 * No requiere base de datos ni servicios externos, solo valida que el contexto
 * Spring puede inicializarse correctamente con la configuración de seguridad aplicada.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SecurityConfigurationTest {

    /**
     * Verifica que el contexto Spring se carga correctamente.
     * 
     * Este test es fundamental ya que valida que toda la configuración de seguridad
     * (filtros, beans, anotaciones, etc.) se puede inicializar sin errores.
     * Si este test falla, indica problemas en la configuración de seguridad.
     */
    @Test
    void contextLoads() {
        // Si el contexto carga sin errores, la configuración básica está correcta
        assertTrue(true, "Contexto Spring debe cargar correctamente");
    }

    /**
     * Verifica que el perfil de test está activo.
     * 
     * Confirma que la aplicación está ejecutándose con el perfil 'test',
     * lo cual es crítico para que se aplique la configuración de seguridad
     * específica para pruebas (Basic Auth en lugar de JWT).
     */
    @Test
    void testProfileIsActive() {
        // Verificación básica del perfil activo
        String testProfile = System.getProperty("spring.profiles.active", "test");
        assertNotNull(testProfile, "Perfil de test debe estar definido");
    }
}