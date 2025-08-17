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
 * Test de integración para verificar que la configuración de seguridad funciona correctamente
 * En perfil 'test' solo debe haber Basic Auth, sin componentes JWT
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class JwtIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void jwtBeansAreNotConfiguredInTestProfile() {
        // En perfil test, los beans JWT NO deben estar configurados
        assertFalse(applicationContext.containsBean("jwksClient"), 
                   "JwksClient no debe estar disponible en perfil test");
        assertFalse(applicationContext.containsBean("jwtValidationService"), 
                   "JwtValidationService no debe estar disponible en perfil test");
        assertFalse(applicationContext.containsBean("jwtAuthenticationFilter"), 
                   "JwtAuthenticationFilter no debe estar disponible en perfil test");
        assertFalse(applicationContext.containsBean("jwksCacheService"), 
                   "JwksCacheService no debe estar disponible en perfil test");
    }

    @Test
    void basicAuthSecurityConfigurationIsValid() {
        // Verificar que la configuración de seguridad básica está presente
        assertTrue(applicationContext.containsBean("testSecurityFilterChain"), 
                  "TestSecurityFilterChain debe estar configurado");
        assertTrue(applicationContext.containsBean("testPasswordEncoder"), 
                  "TestPasswordEncoder debe estar configurado");
        assertTrue(applicationContext.containsBean("testUserDetailsService"), 
                  "TestUserDetailsService debe estar configurado");
        assertTrue(applicationContext.containsBean("testCorsConfigurationSource"), 
                  "TestCorsConfigurationSource debe estar configurado");
    }

    @Test
    void testProfileIsActive() {
        // Verificar que el perfil 'test' está activo
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        boolean testProfileActive = false;
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                testProfileActive = true;
                break;
            }
        }
        assertTrue(testProfileActive, "El perfil 'test' debe estar activo");
    }
}