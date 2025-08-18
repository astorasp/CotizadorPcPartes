package mx.com.qtx.cotizador.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test básico para verificar que las clases de seguridad están presentes
 * Test de unidad que no requiere contexto Spring ni base de datos
 */
class SecurityComponentsTest {

    @Test
    @DisplayName("SecurityConfig class should exist and be properly structured")
    void securityConfigClassExists() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.SecurityConfig");
        }, "SecurityConfig class debe existir");
    }

    @Test
    @DisplayName("JwtAuthenticationFilter class should exist")
    void jwtAuthenticationFilterExists() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.filter.JwtAuthenticationFilter");
        }, "JwtAuthenticationFilter class debe existir");
    }

    @Test
    @DisplayName("JwtValidationService class should exist")
    void jwtValidationServiceExists() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.service.JwtValidationService");
        }, "JwtValidationService class debe existir");
    }

    @Test
    @DisplayName("JwksClient class should exist")
    void jwksClientExists() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.client.JwksClient");
        }, "JwksClient class debe existir");
    }

    @Test
    @DisplayName("SessionValidationClient class should exist")
    void sessionValidationClientExists() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.client.SessionValidationClient");
        }, "SessionValidationClient class debe existir");
    }

    @Test
    @DisplayName("SessionCacheService class should exist")
    void sessionCacheServiceExists() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.service.SessionCacheService");
        }, "SessionCacheService class debe existir");
    }

    @Test
    @DisplayName("All required JWT DTOs should exist")
    void jwtDtosExist() {
        assertDoesNotThrow(() -> {
            Class.forName("mx.com.qtx.cotizador.security.dto.JwkKey");
            Class.forName("mx.com.qtx.cotizador.security.dto.JwksResponse");
            Class.forName("mx.com.qtx.cotizador.security.dto.SessionInfo");
            Class.forName("mx.com.qtx.cotizador.security.dto.SessionValidationResponse");
            Class.forName("mx.com.qtx.cotizador.security.dto.SessionInfoResponse");
            Class.forName("mx.com.qtx.cotizador.security.dto.SessionCloseResponse");
        }, "Todos los DTOs de JWT deben existir");
    }

    @Test
    @DisplayName("Security roles constants should be defined")
    void securityRolesConstantsExist() {
        assertDoesNotThrow(() -> {
            Class<?> securityConfig = Class.forName("mx.com.qtx.cotizador.security.SecurityConfig");
            
            // Verificar que existen las constantes de roles
            securityConfig.getDeclaredField("ROLE_ADMIN");
            securityConfig.getDeclaredField("ROLE_GERENTE");
            securityConfig.getDeclaredField("ROLE_VENDEDOR");
            securityConfig.getDeclaredField("ROLE_INVENTARIO");
            securityConfig.getDeclaredField("ROLE_CONSULTOR");
            
        }, "Constantes de roles de seguridad deben estar definidas");
    }
}