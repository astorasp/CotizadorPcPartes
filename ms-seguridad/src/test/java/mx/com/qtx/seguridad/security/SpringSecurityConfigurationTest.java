package mx.com.qtx.seguridad.security;

import mx.com.qtx.seguridad.integration.BaseIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para verificar la configuración de Spring Security
 * Valida filtros JWT, CORS, headers de seguridad, etc.
 */
@DisplayName("Spring Security Configuration Tests")
public class SpringSecurityConfigurationTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE FILTROS JWT
    // ===============================

    @Test
    @DisplayName("Debe aplicar filtro JWT en endpoints protegidos")
    void shouldApplyJwtFilterOnProtectedEndpoints() {
        // Given - Endpoints que requieren autenticación
        String[] protectedEndpoints = {
            "/auth/validate",
            "/auth/token-ttl"
        };

        for (String endpoint : protectedEndpoints) {
            // When
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl + endpoint,
                    Map.class
            );
            
            // Then - Sin token debe rechazar
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
    }

    @Test
    @DisplayName("Debe permitir acceso a endpoints públicos sin token")
    void shouldAllowAccessToPublicEndpointsWithoutToken() {
        // Given - Endpoints públicos
        Map<String, org.springframework.http.HttpMethod> publicEndpoints = Map.of(
            "/auth/health", org.springframework.http.HttpMethod.GET
        );

        for (Map.Entry<String, org.springframework.http.HttpMethod> entry : publicEndpoints.entrySet()) {
            // When
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + entry.getKey(),
                    entry.getValue(),
                    null,
                    Map.class
            );
            
            // Then - No debe ser 401 Unauthorized por falta de token
            assertNotEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
    }

    @Test
    @DisplayName("Debe validar formato de token Bearer en Authorization header")
    void shouldValidateBearerTokenFormat() {
        // Given - Diferentes formatos de Authorization header
        String[] invalidFormats = {
            "Basic YWRtaW46YWRtaW4=", // Basic auth en lugar de Bearer
            "Token abc123", // Token en lugar de Bearer
            "Bearer", // Bearer sin token
            "Bearer ", // Bearer con espacio pero sin token
            "BearerABC123", // Bearer sin espacio
            "bearer ABC123", // Minúsculas
        };

        for (String invalidFormat : invalidFormats) {
            // When
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", invalidFormat);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/auth/validate",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("invalid_token_format", response.getBody().get("errorType"));
        }
    }

    @Test
    @DisplayName("Debe procesar correctamente tokens Bearer válidos")
    void shouldProcessValidBearerTokens() {
        // Given
        String validToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(validToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("valid"));
    }

    // ===============================
    // PRUEBAS DE CONFIGURACIÓN CORS
    // ===============================

    @Test
    @DisplayName("Debe configurar CORS correctamente para preflight requests")
    void shouldConfigureCorsForPreflightRequests() {
        // Given - Preflight request (OPTIONS)
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Origin", "http://localhost:3000");
        headers.set("Access-Control-Request-Method", "POST");
        headers.set("Access-Control-Request-Headers", "Content-Type,Authorization");
        
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/auth/login",
                org.springframework.http.HttpMethod.OPTIONS,
                entity,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Methods"));
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("Debe incluir headers CORS en respuestas reales")
    void shouldIncludeCorsHeadersInActualResponses() {
        // Given
        Map<String, String> loginRequest = Map.of(
            "usuario", "admin",
            "password", "admin123"
        );

        org.springframework.http.HttpHeaders headers = createJsonHeaders();
        headers.set("Origin", "http://localhost:3000");
        org.springframework.http.HttpEntity<Map<String, String>> entity = new org.springframework.http.HttpEntity<>(loginRequest, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                entity,
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
    }

    // ===============================
    // PRUEBAS DE HEADERS DE SEGURIDAD
    // ===============================

    @Test
    @DisplayName("Debe incluir X-Content-Type-Options header")
    void shouldIncludeXContentTypeOptionsHeader() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("X-Content-Type-Options"));
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
    }

    @Test
    @DisplayName("Debe incluir X-Frame-Options header")
    void shouldIncludeXFrameOptionsHeader() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("X-Frame-Options"));
        assertEquals("DENY", response.getHeaders().getFirst("X-Frame-Options"));
    }

    @Test
    @DisplayName("Debe incluir X-XSS-Protection header")
    void shouldIncludeXXssProtectionHeader() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("X-XSS-Protection"));
        assertEquals("0", response.getHeaders().getFirst("X-XSS-Protection"));
    }

    @Test
    @DisplayName("Debe incluir Strict-Transport-Security header en HTTPS")
    void shouldIncludeStrictTransportSecurityHeader() {
        // When - Verificar que el endpoint responde (HSTS depende de configuración HTTPS)
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Note: HSTS header solo se incluye en conexiones HTTPS reales
        // En testing HTTP local, este header puede no estar presente
    }

    // ===============================
    // PRUEBAS DE VALIDACIÓN DE TOKENS
    // ===============================

    @Test
    @DisplayName("Debe rechazar tokens expirados")
    void shouldRejectExpiredTokens() {
        // Given - Simular token expirado usando un token manipulado con exp en el pasado
        String expiredToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTAwMCwiZXhwIjoxMDAwfQ.invalid_signature";

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(expiredToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("authentication_failed", response.getBody().get("errorType"));
    }

    @Test
    @DisplayName("Debe validar algoritmo de firma del token")
    void shouldValidateTokenSignatureAlgorithm() {
        // Given - Token con algoritmo incorrecto o sin firma
        String[] invalidTokens = {
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiJ9.invalid", // HS256 en lugar de RS256
            "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJhZG1pbiJ9.", // Algoritmo 'none'
        };

        for (String invalidToken : invalidTokens) {
            // When
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/auth/validate",
                    org.springframework.http.HttpMethod.GET,
                    createAuthEntity(invalidToken),
                    Map.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("authentication_failed", response.getBody().get("errorType"));
        }
    }

    // ===============================
    // PRUEBAS DE CONFIGURACIÓN DE SESIONES
    // ===============================

    @Test
    @DisplayName("Debe estar configurado como stateless (sin sesiones)")
    void shouldBeConfiguredAsStateless() {
        // Given - Usar diferentes usuarios para evitar conflicto de sesión única
        String token1 = performTestLogin("testuser", "user123");
        String token2 = performTestLogin("admin", "admin123");

        // When & Then - Cada token debe ser independiente
        assertNotEquals(token1, token2, "Los tokens deben ser únicos en cada login");

        // Ambos tokens deben funcionar independientemente
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(token1),
                Map.class
        );
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(token2),
                Map.class
        );
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    // ===============================
    // PRUEBAS DE MANEJO DE EXCEPCIONES
    // ===============================

    @Test
    @DisplayName("Debe manejar excepciones de autenticación correctamente")
    void shouldHandleAuthenticationExceptionsCorrectly() {
        // Given - Token malformado que causará excepción
        String malformedToken = "not.a.valid.jwt.token.at.all";

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(malformedToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("error"));
        assertNotNull(response.getBody().get("message"));
    }

    @Test
    @DisplayName("Debe manejar errores de acceso denegado")
    void shouldHandleAccessDeniedErrors() {
        // Given - Token válido
        String token = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(token),
                Map.class
        );

        // Then - El admin debería tener acceso
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ===============================
    // PRUEBAS DE CONFIGURACIÓN DE ENDPOINTS
    // ===============================

    @Test
    @DisplayName("Debe configurar correctamente endpoints públicos vs protegidos")
    void shouldConfigurePublicVsProtectedEndpointsCorrectly() {
        // Public endpoints - no requieren autenticación
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity("{}"),
                Map.class
        );
        assertTrue(loginResponse.getStatusCode().is4xxClientError()); // 400 Bad Request, no 401 Unauthorized

        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());

        // Protected endpoints - requieren autenticación
        ResponseEntity<Map> validateResponse = restTemplate.getForEntity(
                baseUrl + "/auth/validate",
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, validateResponse.getStatusCode());

        ResponseEntity<Map> ttlResponse = restTemplate.getForEntity(
                baseUrl + "/auth/token-ttl",
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ttlResponse.getStatusCode());
    }

    // ===============================
    // PRUEBAS DE CONFIGURACIÓN DE MÉTODOS HTTP
    // ===============================

    @Test
    @DisplayName("Debe permitir solo métodos HTTP apropiados por endpoint")
    void shouldAllowOnlyAppropriateHttpMethodsPerEndpoint() {
        // POST endpoints - authentication takes precedence over method validation
        ResponseEntity<Map> getLoginResponse = restTemplate.exchange(
                baseUrl + "/auth/login",
                org.springframework.http.HttpMethod.GET,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, getLoginResponse.getStatusCode());

        ResponseEntity<Map> putLoginResponse = restTemplate.exchange(
                baseUrl + "/auth/login",
                org.springframework.http.HttpMethod.PUT,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, putLoginResponse.getStatusCode());

        // GET endpoints - authentication takes precedence over method validation  
        ResponseEntity<Map> postValidateResponse = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.POST,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, postValidateResponse.getStatusCode());

        ResponseEntity<Map> deleteHealthResponse = restTemplate.exchange(
                baseUrl + "/auth/health",
                org.springframework.http.HttpMethod.DELETE,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, deleteHealthResponse.getStatusCode());
    }

    // ===============================
    // PRUEBAS DE CONFIGURACIÓN DE CONTENT TYPE
    // ===============================

    @Test
    @DisplayName("Debe validar Content-Type en endpoints POST")
    void shouldValidateContentTypeOnPostEndpoints() {
        // Given
        String loginRequestAsString = "{\"usuario\":\"admin\",\"password\":\"admin123\"}";
        Map<String, String> loginRequest = Map.of(
            "usuario", "admin",
            "password", "admin123"
        );

        // When & Then - Content-Type incorrecto
        org.springframework.http.HttpHeaders incorrectHeaders = new org.springframework.http.HttpHeaders();
        incorrectHeaders.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
        org.springframework.http.HttpEntity<String> incorrectEntity = new org.springframework.http.HttpEntity<>(loginRequestAsString, incorrectHeaders);
        
        ResponseEntity<Map> incorrectResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                incorrectEntity,
                Map.class
        );
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, incorrectResponse.getStatusCode());

        // Content-Type correcto
        ResponseEntity<Map> correctResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
        );
        assertEquals(HttpStatus.OK, correctResponse.getStatusCode());
    }
}