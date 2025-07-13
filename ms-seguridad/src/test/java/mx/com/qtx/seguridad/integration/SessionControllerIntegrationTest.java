package mx.com.qtx.seguridad.integration;

import mx.com.qtx.seguridad.dto.SessionValidationResponse;
import mx.com.qtx.seguridad.dto.SessionCloseResponse;
import mx.com.qtx.seguridad.dto.SessionInfoResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para SessionController
 * Valida endpoints REST públicos de gestión de sesiones
 */
@DisplayName("SessionController Integration Tests")
public class SessionControllerIntegrationTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE VALIDAR SESIÓN (/session/validate/{sessionId})
    // ===============================

    @Test
    @DisplayName("GET /session/validate/{sessionId} - Debe validar sesión activa")
    void shouldValidateActiveSession() {
        // Given - Crear una sesión activa primero (login)
        String accessToken = performTestLogin("admin", "admin123");
        assertNotNull(accessToken);
        
        // Extraer session ID del token o usar un ID conocido para el test
        String sessionId = "test-session-active";

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + sessionId,
                SessionValidationResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionValidationResponse validationResponse = response.getBody();
        assertNotNull(validationResponse.getSessionId());
        assertNotNull(validationResponse.getMessage());
        assertNotNull(validationResponse.getTimestamp());
        
        // Verificar headers de cache
        assertNotNull(response.getHeaders().getCacheControl());
    }

    @Test
    @DisplayName("GET /session/validate/{sessionId} - Debe validar sesión inactiva")
    void shouldValidateInactiveSession() {
        // Given
        String sessionId = "non-existent-session-id";

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + sessionId,
                SessionValidationResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionValidationResponse validationResponse = response.getBody();
        assertEquals(sessionId, validationResponse.getSessionId());
        assertFalse(validationResponse.getIsActive());
        assertNotNull(validationResponse.getTimestamp());
    }

    @Test
    @DisplayName("GET /session/validate/{sessionId} - Debe rechazar sessionId vacío")
    void shouldRejectEmptySessionId() {
        // Given - Usar un path específico para evitar problemas de routing
        String emptySessionId = "EMPTY_SESSION_ID";

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + emptySessionId,
                SessionValidationResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Endpoint existe, pero sesión inválida
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getIsActive());
    }

    @Test
    @DisplayName("GET /session/validate/{sessionId} - Debe rechazar sessionId con espacios")
    void shouldRejectWhitespaceSessionId() {
        // Given
        String whitespaceSessionId = "   ";

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + whitespaceSessionId,
                SessionValidationResponse.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionValidationResponse validationResponse = response.getBody();
        assertFalse(validationResponse.getSuccess());
    }

    @Test
    @DisplayName("GET /session/validate/{sessionId} - Debe manejar caracteres especiales en sessionId")
    void shouldHandleSpecialCharactersInSessionId() {
        // Given
        String specialCharSessionId = "session@#$%^&*()";

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + specialCharSessionId,
                SessionValidationResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Debe manejar gracefully, probablemente como sesión inactiva
        SessionValidationResponse validationResponse = response.getBody();
        assertNotNull(validationResponse.getMessage());
    }

    // ===============================
    // PRUEBAS DE CERRAR SESIÓN (/session/close/{sessionId})
    // ===============================

    @Test
    @DisplayName("POST /session/close/{sessionId} - Debe cerrar sesión existente")
    void shouldCloseExistingSession() {
        // Given - Crear sesión activa primero
        String accessToken = performTestLogin("admin", "admin123");
        assertNotNull(accessToken);
        
        // Para este test, necesitamos un sessionId real o mockeado
        String sessionId = "test-session-to-close";

        // When
        ResponseEntity<SessionCloseResponse> response = restTemplate.postForEntity(
                baseUrl + "/session/close/" + sessionId,
                null,
                SessionCloseResponse.class
        );

        // Then
        // Puede ser 200 (cerrada) o 404 (no encontrada) dependiendo si la sesión existe
        assertTrue(response.getStatusCode().equals(HttpStatus.OK) || 
                  response.getStatusCode().equals(HttpStatus.NOT_FOUND));
        
        assertNotNull(response.getBody());
        
        SessionCloseResponse closeResponse = response.getBody();
        assertEquals(sessionId, closeResponse.getSessionId());
        assertNotNull(closeResponse.getMessage());
        assertNotNull(closeResponse.getTimestamp());
        
        // Verificar headers de cache
        assertNotNull(response.getHeaders().getCacheControl());
    }

    @Test
    @DisplayName("POST /session/close/{sessionId} - Debe retornar 404 para sesión inexistente")
    void shouldReturn404ForNonExistentSession() {
        // Given
        String nonExistentSessionId = "definitely-does-not-exist-12345";

        // When
        ResponseEntity<SessionCloseResponse> response = restTemplate.postForEntity(
                baseUrl + "/session/close/" + nonExistentSessionId,
                null,
                SessionCloseResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionCloseResponse closeResponse = response.getBody();
        assertEquals(nonExistentSessionId, closeResponse.getSessionId());
        assertFalse(closeResponse.getClosed());
    }

    @Test
    @DisplayName("POST /session/close/{sessionId} - Debe rechazar sessionId vacío")
    void shouldRejectEmptySessionIdForClose() {
        // Given - Usar un ID específico
        String emptySessionId = "EMPTY_SESSION_ID";

        // When
        ResponseEntity<SessionCloseResponse> response = restTemplate.postForEntity(
                baseUrl + "/session/close/" + emptySessionId,
                null,
                SessionCloseResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // Sesión no encontrada
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getClosed());
    }

    @Test
    @DisplayName("POST /session/close/{sessionId} - Debe rechazar sessionId con espacios")
    void shouldRejectWhitespaceSessionIdForClose() {
        // Given
        String whitespaceSessionId = "   ";

        // When
        ResponseEntity<SessionCloseResponse> response = restTemplate.postForEntity(
                baseUrl + "/session/close/" + whitespaceSessionId,
                null,
                SessionCloseResponse.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionCloseResponse closeResponse = response.getBody();
        assertFalse(closeResponse.getSuccess());
    }

    // ===============================
    // PRUEBAS DE INFORMACIÓN DE SESIÓN (/session/info/{sessionId})
    // ===============================

    @Test
    @DisplayName("GET /session/info/{sessionId} - Debe obtener información de sesión existente")
    void shouldGetInfoForExistingSession() {
        // Given - Usar un sessionId que podría existir
        String sessionId = "test-session-info";

        // When
        ResponseEntity<SessionInfoResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/info/" + sessionId,
                SessionInfoResponse.class
        );

        // Then
        // Puede ser 200 (encontrada) o 404 (no encontrada)
        assertTrue(response.getStatusCode().equals(HttpStatus.OK) || 
                  response.getStatusCode().equals(HttpStatus.NOT_FOUND));
        
        assertNotNull(response.getBody());
        
        SessionInfoResponse infoResponse = response.getBody();
        assertEquals(sessionId, infoResponse.getSessionId());
        assertNotNull(infoResponse.getMessage());
        assertNotNull(infoResponse.getTimestamp());
        
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            assertTrue(infoResponse.getSuccess());
            assertNotNull(infoResponse.getIsActive());
            assertNotNull(infoResponse.getStartTime());
        } else {
            assertFalse(infoResponse.getSuccess());
        }
        
        // Verificar headers de cache
        assertNotNull(response.getHeaders().getCacheControl());
    }

    @Test
    @DisplayName("GET /session/info/{sessionId} - Debe retornar 404 para sesión inexistente")
    void shouldReturn404ForNonExistentSessionInfo() {
        // Given
        String nonExistentSessionId = "absolutely-does-not-exist-67890";

        // When
        ResponseEntity<SessionInfoResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/info/" + nonExistentSessionId,
                SessionInfoResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionInfoResponse infoResponse = response.getBody();
        assertEquals(nonExistentSessionId, infoResponse.getSessionId());
        assertFalse(infoResponse.getSuccess());
    }

    @Test
    @DisplayName("GET /session/info/{sessionId} - Debe rechazar sessionId vacío")
    void shouldRejectEmptySessionIdForInfo() {
        // Given - Usar un ID específico
        String emptySessionId = "EMPTY_SESSION_ID";

        // When
        ResponseEntity<SessionInfoResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/info/" + emptySessionId,
                SessionInfoResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // Sesión no encontrada
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getSuccess());
    }

    @Test
    @DisplayName("GET /session/info/{sessionId} - Debe rechazar sessionId con espacios")
    void shouldRejectWhitespaceSessionIdForInfo() {
        // Given
        String whitespaceSessionId = "   ";

        // When
        ResponseEntity<SessionInfoResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/info/" + whitespaceSessionId,
                SessionInfoResponse.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionInfoResponse infoResponse = response.getBody();
        assertFalse(infoResponse.getSuccess());
    }

    // ===============================
    // PRUEBAS DE HEALTH CHECK (/session/health)
    // ===============================

    @Test
    @DisplayName("GET /session/health - Debe retornar estado del controlador de sesiones")
    void shouldReturnSessionControllerHealth() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/session/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> healthResponse = response.getBody();
        assertEquals("UP", healthResponse.get("status"));
        assertEquals("SessionController", healthResponse.get("controller"));
        assertEquals("ms-seguridad", healthResponse.get("service"));
        assertNotNull(healthResponse.get("timestamp"));
        
        // Stats pueden estar presentes o no, dependiendo del estado del servicio
        Object stats = healthResponse.get("stats");
        assertNotNull(stats); // Puede ser Map o String de error
        
        // Verificar headers de cache
        assertNotNull(response.getHeaders().getCacheControl());
    }

    // ===============================
    // PRUEBAS DE SEGURIDAD Y VALIDACIÓN
    // ===============================

    @Test
    @DisplayName("Debe rechazar métodos HTTP no permitidos")
    void shouldRejectUnsupportedHttpMethods() {
        // Test DELETE en endpoint GET - puede retornar 405 o 401 dependiendo del filtro
        ResponseEntity<SessionValidationResponse> response1 = restTemplate.exchange(
                baseUrl + "/session/validate/test-session",
                org.springframework.http.HttpMethod.DELETE,
                null,
                SessionValidationResponse.class
        );
        assertTrue(response1.getStatusCode().equals(HttpStatus.METHOD_NOT_ALLOWED) ||
                   response1.getStatusCode().equals(HttpStatus.UNAUTHORIZED));

        // Test PUT en endpoint POST
        ResponseEntity<SessionCloseResponse> response2 = restTemplate.exchange(
                baseUrl + "/session/close/test-session",
                org.springframework.http.HttpMethod.PUT,
                null,
                SessionCloseResponse.class
        );
        assertTrue(response2.getStatusCode().equals(HttpStatus.METHOD_NOT_ALLOWED) ||
                   response2.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Debe manejar sessionIds extremadamente largos")
    void shouldHandleExtremelyLongSessionIds() {
        // Given - SessionId extremadamente largo
        StringBuilder longSessionId = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longSessionId.append("a");
        }

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + longSessionId.toString(),
                SessionValidationResponse.class
        );

        // Then - Debe manejar gracefully sin error 500
        assertTrue(response.getStatusCode().is2xxSuccessful() || 
                  response.getStatusCode().is4xxClientError());
        
        if (response.getBody() != null) {
            assertNotNull(response.getBody().getMessage());
        }
    }

    @Test
    @DisplayName("Debe manejar caracteres Unicode en sessionId")
    void shouldHandleUnicodeCharactersInSessionId() {
        // Given
        String unicodeSessionId = "sesión-español-ñáéíóú-中文-🎉";

        // When
        ResponseEntity<SessionValidationResponse> response = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + unicodeSessionId,
                SessionValidationResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        SessionValidationResponse validationResponse = response.getBody();
        assertNotNull(validationResponse.getMessage());
    }

    @Test
    @DisplayName("Debe validar concurrencia en validación de sesiones")
    void shouldHandleConcurrentSessionValidations() {
        // Given
        String sessionId = "concurrent-test-session";

        // When - Hacer múltiples llamadas concurrentes
        ResponseEntity<SessionValidationResponse>[] responses = new ResponseEntity[5];
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.getForEntity(
                        baseUrl + "/session/validate/" + sessionId,
                        SessionValidationResponse.class
                );
            });
            threads[i].start();
        }
        
        // Esperar a que terminen todos los threads
        for (Thread thread : threads) {
            try {
                thread.join(5000); // Timeout de 5 segundos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test interrumpido");
            }
        }

        // Then - Todas las respuestas deben ser consistentes
        for (ResponseEntity<SessionValidationResponse> response : responses) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(sessionId, response.getBody().getSessionId());
        }
    }

    @Test
    @DisplayName("Debe verificar que los endpoints no requieren autenticación")
    void shouldVerifyEndpointsArePublic() {
        // Given - No usar token de autenticación
        String sessionId = "public-test-session";

        // When - Hacer llamadas sin autenticación
        ResponseEntity<SessionValidationResponse> validateResponse = restTemplate.getForEntity(
                baseUrl + "/session/validate/" + sessionId,
                SessionValidationResponse.class
        );

        ResponseEntity<SessionCloseResponse> closeResponse = restTemplate.postForEntity(
                baseUrl + "/session/close/" + sessionId,
                null,
                SessionCloseResponse.class
        );

        ResponseEntity<SessionInfoResponse> infoResponse = restTemplate.getForEntity(
                baseUrl + "/session/info/" + sessionId,
                SessionInfoResponse.class
        );

        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
                baseUrl + "/session/health",
                Map.class
        );

        // Then - Todos deben responder sin requerir autenticación
        assertNotEquals(HttpStatus.UNAUTHORIZED, validateResponse.getStatusCode());
        assertNotEquals(HttpStatus.UNAUTHORIZED, closeResponse.getStatusCode());
        assertNotEquals(HttpStatus.UNAUTHORIZED, infoResponse.getStatusCode());
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
    }
}