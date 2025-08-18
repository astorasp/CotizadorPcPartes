package mx.com.qtx.cotizador.security;

import mx.com.qtx.cotizador.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simplificado para verificar la configuración de seguridad
 */
@SpringBootTest(classes = {mx.com.qtx.cotizador.PedidosApplication.class})
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class SecurityConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void securityBeansAreConfigured() {
        // Verificar que al menos los beans básicos de seguridad están configurados
        assertTrue(applicationContext.containsBean("securityFilterChain"));
        assertTrue(applicationContext.containsBean("testPasswordEncoder"));
    }

    @Test
    void testProfileIsActiveAndJwtIsDisabled() {
        // Verificar que estamos en perfil test
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        boolean testProfileActive = false;
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                testProfileActive = true;
                break;
            }
        }
        assertTrue(testProfileActive, "El perfil 'test' debe estar activo");
        
        // En perfil test, los componentes JWT no deben estar disponibles
        assertFalse(applicationContext.containsBean("jwtAuthenticationFilter"), 
                   "JWT components should not be available in test profile");
    }
}