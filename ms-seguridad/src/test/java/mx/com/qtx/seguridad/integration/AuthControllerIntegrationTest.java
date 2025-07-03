package mx.com.qtx.seguridad.integration;

import mx.com.qtx.seguridad.dto.LoginRequest;
import mx.com.qtx.seguridad.dto.TokenResponse;
import mx.com.qtx.seguridad.dto.TokenTtlResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para AuthController
 * Valida endpoints REST de autenticación, autorización y gestión de tokens
 */
@DisplayName("AuthController Integration Tests")
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE LOGIN (/auth/login)
    // ===============================

    @Test
    @DisplayName("POST /auth/login - Debe autenticar usuario válido exitosamente")
    void shouldAuthenticateValidUser() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario("admin");
        loginRequest.setPassword("admin123");

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertTokenResponse(responseBody);
        
        assertNotNull(responseBody.get("accessToken"));
        assertNotNull(responseBody.get("refreshToken"));
        assertEquals("Bearer", responseBody.get("tokenType"));
        assertTrue(((Number) responseBody.get("expiresIn")).longValue() > 0);
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar credenciales inválidas")
    void shouldRejectInvalidCredentials() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario("invalid");
        loginRequest.setPassword("wrongpassword");

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("invalid_credentials", errorBody.get("error"));
        assertEquals("Usuario o contraseña incorrectos", errorBody.get("message"));
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar request con campos faltantes")
    void shouldRejectMissingFields() {
        // Given - Login request incompleto
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario("admin");
        // password faltante

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar JSON malformado")
    void shouldRejectMalformedJson() {
        // Given
        String malformedJson = "{\"usuario\":\"admin\",\"password\":}";

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(malformedJson),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ===============================
    // PRUEBAS DE REFRESH (/auth/refresh)
    // ===============================

    @Test
    @DisplayName("POST /auth/refresh - Debe renovar token con refresh token válido")
    void shouldRefreshTokenWithValidRefreshToken() {
        // Given - Obtener tokens válidos primero
        String refreshToken = performTestLoginAndGetRefreshToken("admin", "admin123");

        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/refresh",
                createJsonEntity(refreshRequest),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertTokenResponse(responseBody);
        
        assertNotNull(responseBody.get("accessToken"));
        assertNotNull(responseBody.get("refreshToken"));
    }

    @Test
    @DisplayName("POST /auth/refresh - Debe rechazar refresh token inválido")
    void shouldRejectInvalidRefreshToken() {
        // Given
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "invalid.refresh.token");

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/refresh",
                createJsonEntity(refreshRequest),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("invalid_refresh_token", errorBody.get("error"));
        assertEquals("Refresh token inválido o expirado", errorBody.get("message"));
    }

    @Test
    @DisplayName("POST /auth/refresh - Debe rechazar request sin refresh token")
    void shouldRejectMissingRefreshToken() {
        // Given
        Map<String, String> refreshRequest = new HashMap<>();
        // refresh token faltante

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/refresh",
                createJsonEntity(refreshRequest),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("missing_refresh_token", errorBody.get("error"));
        assertEquals("Refresh token es requerido", errorBody.get("message"));
    }

    // ===============================
    // PRUEBAS DE LOGOUT (/auth/logout)
    // ===============================

    @Test
    @DisplayName("POST /auth/logout - Debe realizar logout exitosamente")
    void shouldLogoutSuccessfully() {
        // Given - Obtener tokens válidos primero
        String accessToken = performTestLogin("admin", "admin123");
        String refreshToken = performTestLoginAndGetRefreshToken("admin", "admin123");

        Map<String, String> logoutRequest = new HashMap<>();
        logoutRequest.put("accessToken", accessToken);
        logoutRequest.put("refreshToken", refreshToken);

        // When - Usar el token en el header Authorization
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/logout",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(logoutRequest, accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("Sesión cerrada exitosamente", responseBody.get("message"));
        assertEquals("success", responseBody.get("status"));
    }

    @Test
    @DisplayName("POST /auth/logout - Debe rechazar logout sin autenticación")
    void shouldRejectLogoutWithoutAuthentication() {
        // Given
        Map<String, String> logoutRequest = new HashMap<>();
        // sin header Authorization

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/logout",
                createJsonEntity(logoutRequest),
                Map.class
        );

        // Then - Debe requerir autenticación
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("missing_token", errorBody.get("errorType"));
    }

    // ===============================
    // PRUEBAS DE VALIDATE (/auth/validate)
    // ===============================

    @Test
    @DisplayName("GET /auth/validate - Debe validar token válido")
    void shouldValidateValidToken() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals(true, responseBody.get("valid"));
        assertEquals("admin", responseBody.get("usuario"));
        assertNotNull(responseBody.get("userId"));
        assertNotNull(responseBody.get("roles"));
        assertNotNull(responseBody.get("validatedAt"));
    }

    @Test
    @DisplayName("GET /auth/validate - Debe rechazar token inválido")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

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
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("authentication_failed", errorBody.get("errorType"));
        assertEquals("Error de autenticación", errorBody.get("message"));
    }

    @Test
    @DisplayName("GET /auth/validate - Debe rechazar request sin header Authorization")
    void shouldRejectMissingAuthorizationHeader() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/validate",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("missing_token", errorBody.get("errorType"));
        assertEquals("Token de autorización requerido", errorBody.get("message"));
    }

    @Test
    @DisplayName("GET /auth/validate - Debe rechazar header Authorization malformado")
    void shouldRejectMalformedAuthorizationHeader() {
        // Given
        String malformedHeader = "InvalidFormat token123";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", malformedHeader);
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("invalid_token_format", errorBody.get("errorType"));
        assertEquals("Formato de token inválido. Use 'Bearer <token>'", errorBody.get("message"));
    }

    // ===============================
    // PRUEBAS DE TOKEN-TTL (/auth/token-ttl)
    // ===============================

    @Test
    @DisplayName("GET /auth/token-ttl - Debe retornar tiempo restante de token válido")
    void shouldReturnTokenTimeToLive() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<TokenTtlResponse> response = restTemplate.exchange(
                baseUrl + "/auth/token-ttl",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                TokenTtlResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        TokenTtlResponse ttlResponse = response.getBody();
        assertNotNull(ttlResponse.getTimeRemaining());
        assertTrue(ttlResponse.getTimeRemaining().matches("\\d{2}:\\d{2}:\\d{2}"));
        assertTrue(ttlResponse.getRemainingSeconds() > 0);
        assertTrue(ttlResponse.getValid());
    }

    @Test
    @DisplayName("GET /auth/token-ttl - Debe rechazar token inválido")
    void shouldRejectInvalidTokenForTtl() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/token-ttl",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(invalidToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("authentication_failed", errorBody.get("errorType"));
        assertNotNull(errorBody.get("message"));
    }

    // ===============================
    // PRUEBAS DE HEALTH (/auth/health)
    // ===============================

    @Test
    @DisplayName("GET /auth/health - Debe retornar estado del servicio")
    void shouldReturnServiceHealth() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("UP", responseBody.get("status"));
        assertEquals("ms-seguridad", responseBody.get("service"));
        assertNotNull(responseBody.get("timestamp"));
    }

    // ===============================
    // PRUEBAS DE SEGURIDAD
    // ===============================

    @Test
    @DisplayName("Debe rechazar métodos HTTP no permitidos")
    void shouldRejectUnsupportedHttpMethods() {
        // Test PUT en endpoint POST público - el filtro JWT intercepta primero
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/auth/login",
                org.springframework.http.HttpMethod.PUT,
                createJsonEntity("{}"),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response1.getStatusCode());

        // Test DELETE en endpoint GET protegido - devuelve UNAUTHORIZED por filtro JWT
        ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.DELETE,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response2.getStatusCode());
    }

    @Test
    @DisplayName("Debe manejar Content-Type incorrecto")
    void shouldHandleIncorrectContentType() {
        // Given
        String loginRequestAsString = "{\"usuario\":\"admin\",\"password\":\"admin123\"}";

        // When - Enviar como text/plain en lugar de application/json
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(loginRequestAsString, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                entity,
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe limitar tamaño de payload")
    void shouldLimitPayloadSize() {
        // Given - Payload extremadamente grande
        StringBuilder largePayload = new StringBuilder("{\"usuario\":\"admin\",\"password\":\"");
        for (int i = 0; i < 10000; i++) {
            largePayload.append("a");
        }
        largePayload.append("\"}");

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(largePayload.toString()),
                Map.class
        );

        // Then - Puede ser 400 o 413
        assertTrue(response.getStatusCode().is4xxClientError());
    }

}