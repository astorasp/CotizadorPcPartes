package mx.com.qtx.seguridad.integration;

import mx.com.qtx.seguridad.dto.PublicKeyResponse;
import mx.com.qtx.seguridad.dto.KeyPairResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para KeyController
 * Valida endpoints de gestión de llaves RSA, seguridad y formato PEM
 */
@DisplayName("KeyController Integration Tests")
public class KeyControllerIntegrationTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE ENDPOINT PÚBLICO (/keys/public)
    // ===============================

    @Test
    @DisplayName("GET /keys/public - Debe retornar llave pública sin autenticación")
    void shouldReturnPublicKeyWithoutAuthentication() {
        // When
        ResponseEntity<PublicKeyResponse> response = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                PublicKeyResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        PublicKeyResponse publicKeyResponse = response.getBody();
        assertNotNull(publicKeyResponse.getPublicKey());
        assertEquals("RSA", publicKeyResponse.getAlgorithm());
        assertEquals("2048", publicKeyResponse.getKeySize());
        assertEquals("PEM", publicKeyResponse.getKeyFormat());
        assertNotNull(publicKeyResponse.getKeyId());
        assertNotNull(publicKeyResponse.getGeneratedAt());
        assertNotNull(publicKeyResponse.getUsage());
        
        // Verificar formato PEM
        String publicKey = publicKeyResponse.getPublicKey();
        assertTrue(publicKey.startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(publicKey.endsWith("-----END PUBLIC KEY-----"));
    }

    @Test
    @DisplayName("GET /keys/public - Debe ser accesible sin token de autorización")
    void shouldBeAccessibleWithoutAuthorizationToken() {
        // When - Sin header Authorization
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("publicKey"));
    }

    @Test
    @DisplayName("GET /keys/public - Debe incluir metadata completa")
    void shouldIncludeCompleteMetadata() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("publicKey"));
        assertNotNull(responseBody.get("algorithm"));
        assertNotNull(responseBody.get("keySize"));
        assertNotNull(responseBody.get("keyFormat"));
        assertNotNull(responseBody.get("keyId"));
        assertNotNull(responseBody.get("generatedAt"));
        assertNotNull(responseBody.get("usage"));
    }

    // ===============================
    // PRUEBAS DE ENDPOINTS PROTEGIDOS (ADMIN ROLE)
    // ===============================

    @Test
    @DisplayName("GET /keys/private - Debe requerir autenticación ADMIN")
    void shouldRequireAdminAuthenticationForPrivateKey() {
        // When & Then - Sin token
        ResponseEntity<Map> responseWithoutAuth = restTemplate.getForEntity(
                baseUrl + "/keys/private",
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, responseWithoutAuth.getStatusCode());

        // Con token de admin válido
        String adminToken = performTestLogin("admin", "admin123");
        
        ResponseEntity<Map> responseWithAuth = restTemplate.exchange(
                baseUrl + "/keys/private",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                Map.class
        );
        
        assertEquals(HttpStatus.OK, responseWithAuth.getStatusCode());
        assertNotNull(responseWithAuth.getBody());
        
        Map<String, Object> responseBody = responseWithAuth.getBody();
        assertNotNull(responseBody.get("privateKey"));
        assertEquals("RSA", responseBody.get("algorithm"));
        assertNotNull(responseBody.get("warning"));
    }

    @Test
    @DisplayName("GET /keys/keypair - Debe retornar par completo de llaves para ADMIN")
    void shouldReturnCompleteKeyPairForAdmin() {
        // Given
        String adminToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<KeyPairResponse> response = restTemplate.exchange(
                baseUrl + "/keys/keypair",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                KeyPairResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        KeyPairResponse keyPairResponse = response.getBody();
        assertTrue(keyPairResponse.hasPublicKey());
        assertTrue(keyPairResponse.hasPrivateKey());
        assertEquals("RSA", keyPairResponse.getAlgorithm());
        assertEquals("2048", keyPairResponse.getKeySize());
        
        // Verificar formato PEM de ambas llaves
        assertTrue(keyPairResponse.getPublicKey().startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(keyPairResponse.getPrivateKey().startsWith("-----BEGIN PRIVATE KEY-----"));
    }

    @Test
    @DisplayName("GET /keys/info - Debe retornar información de llaves sin contenido sensible")
    void shouldReturnKeyInfoWithoutSensitiveContent() {
        // Given
        String adminToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/keys/info",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("keyId"));
        assertEquals("RSA", responseBody.get("algorithm"));
        assertEquals("2048", responseBody.get("keySize"));
        assertEquals(true, responseBody.get("hasPublicKey"));
        assertEquals(true, responseBody.get("isValidFormat"));
        
        // No debe incluir las llaves reales
        assertNull(responseBody.get("publicKey"));
        assertNull(responseBody.get("privateKey"));
    }

    // ===============================
    // PRUEBAS DE GENERACIÓN DE LLAVES
    // ===============================

    @Test
    @DisplayName("POST /keys/generate - Debe generar nuevas llaves para ADMIN")
    void shouldGenerateNewKeysForAdmin() {
        // Given
        String adminToken = performTestLogin("admin", "admin123");
        
        // Obtener keyId anterior
        ResponseEntity<Map> beforeResponse = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                Map.class
        );
        Map<String, Object> beforeMap = beforeResponse.getBody();
        String previousKeyId = (String) beforeMap.get("keyId");
        String previousPublicKey = (String) beforeMap.get("publicKey");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/keys/generate",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(adminToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("Nuevas llaves RSA generadas exitosamente", responseBody.get("message"));
        assertEquals("success", responseBody.get("status"));
        assertNotNull(responseBody.get("newKeyId"));
        assertNotNull(responseBody.get("warning"));
        
        String newKeyId = (String) responseBody.get("newKeyId");
        assertNotEquals(previousKeyId, newKeyId, "El nuevo keyId debe ser diferente al anterior");

        // Verificar que la llave pública cambió
        ResponseEntity<Map> afterResponse = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                Map.class
        );
        Map<String, Object> afterMap = afterResponse.getBody();
        String afterPublicKey = (String) afterMap.get("publicKey");
        String afterKeyId = (String) afterMap.get("keyId");
        
        assertNotEquals(previousPublicKey, afterPublicKey, "La llave pública debe haber cambiado");
        assertEquals(newKeyId, afterKeyId, "El keyId debe coincidir");
    }

    @Test
    @DisplayName("POST /keys/generate - Debe rechazar usuario sin permisos ADMIN")
    void shouldRejectNonAdminUserForKeyGeneration() {
        // When & Then - Sin token
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/keys/generate",
                org.springframework.http.HttpMethod.POST,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ===============================
    // PRUEBAS DE HEALTH CHECK
    // ===============================

    @Test
    @DisplayName("GET /keys/health - Debe retornar estado del servicio de llaves")
    void shouldReturnKeyServiceHealth() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/keys/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("UP", responseBody.get("status"));
        assertEquals("key-management", responseBody.get("service"));
        assertEquals(true, responseBody.get("keysAvailable"));
        assertNotNull(responseBody.get("currentKeyId"));
        assertNotNull(responseBody.get("timestamp"));
    }

    @Test
    @DisplayName("GET /keys/health - Debe ser accesible sin autenticación")
    void shouldBeAccessibleWithoutAuthenticationForHealth() {
        // When - Sin header Authorization
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/keys/health",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("status"));
    }

    // ===============================
    // PRUEBAS DE SEGURIDAD
    // ===============================

    @Test
    @DisplayName("Debe rechazar métodos HTTP no permitidos")
    void shouldRejectUnsupportedHttpMethods() {
        String adminToken = performTestLogin("admin", "admin123");

        // Test métodos incorrectos en endpoints GET públicos - authentication takes precedence
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/keys/public",
                org.springframework.http.HttpMethod.POST,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response1.getStatusCode());

        // Test métodos incorrectos en endpoints protegidos
        ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/keys/info",
                org.springframework.http.HttpMethod.DELETE,
                createAuthEntity(adminToken),
                Map.class
        );
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response2.getStatusCode());

        // Test métodos incorrectos en endpoints POST
        ResponseEntity<Map> response3 = restTemplate.exchange(
                baseUrl + "/keys/generate",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                Map.class
        );
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response3.getStatusCode());
    }

    @Test
    @DisplayName("Debe validar autorización en todos los endpoints protegidos")
    void shouldValidateAuthorizationOnProtectedEndpoints() {
        String[] protectedEndpoints = {
            "/keys/private",
            "/keys/keypair", 
            "/keys/info"
        };

        for (String endpoint : protectedEndpoints) {
            // Sin token debe rechazar
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl + endpoint,
                    Map.class
            );
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        // POST endpoint
        ResponseEntity<Map> postResponse = restTemplate.exchange(
                baseUrl + "/keys/generate",
                org.springframework.http.HttpMethod.POST,
                null,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, postResponse.getStatusCode());
    }

    @Test
    @DisplayName("Debe incluir warnings en endpoints que exponen información sensible")
    void shouldIncludeWarningsOnSensitiveEndpoints() {
        String adminToken = performTestLogin("admin", "admin123");

        // Private key endpoint
        ResponseEntity<Map> privateResponse = restTemplate.exchange(
                baseUrl + "/keys/private",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                Map.class
        );
        
        assertEquals(HttpStatus.OK, privateResponse.getStatusCode());
        assertNotNull(privateResponse.getBody());
        
        String warning = (String) privateResponse.getBody().get("warning");
        assertNotNull(warning);
        assertTrue(warning.contains("INFORMACIÓN SENSIBLE"));

        // Key generation endpoint
        ResponseEntity<Map> generateResponse = restTemplate.exchange(
                baseUrl + "/keys/generate",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(adminToken),
                Map.class
        );
        
        assertEquals(HttpStatus.OK, generateResponse.getStatusCode());
        assertNotNull(generateResponse.getBody());
        
        String generateWarning = (String) generateResponse.getBody().get("warning");
        assertNotNull(generateWarning);
        assertTrue(generateWarning.contains("tokens firmados"));
    }

    // ===============================
    // PRUEBAS DE FORMATO Y VALIDACIÓN
    // ===============================

    @Test
    @DisplayName("Debe retornar llaves en formato PEM válido")
    void shouldReturnKeysInValidPemFormat() {
        String adminToken = performTestLogin("admin", "admin123");

        // Public key
        ResponseEntity<Map> publicResponse = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                Map.class
        );
        String publicKey = (String) publicResponse.getBody().get("publicKey");
        validatePemFormat(publicKey, "PUBLIC KEY");

        // Private key
        ResponseEntity<Map> privateResponse = restTemplate.exchange(
                baseUrl + "/keys/private",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                Map.class
        );
        String privateKey = (String) privateResponse.getBody().get("privateKey");
        validatePemFormat(privateKey, "PRIVATE KEY");
    }

    @Test
    @DisplayName("Debe mantener consistencia entre diferentes endpoints")
    void shouldMaintainConsistencyBetweenEndpoints() {
        String adminToken = performTestLogin("admin", "admin123");

        // Obtener llave pública del endpoint público
        ResponseEntity<Map> publicResponse = restTemplate.getForEntity(
                baseUrl + "/keys/public",
                Map.class
        );
        Map<String, Object> publicMap = publicResponse.getBody();

        // Obtener par de llaves del endpoint admin
        ResponseEntity<KeyPairResponse> keyPairResponse = restTemplate.exchange(
                baseUrl + "/keys/keypair",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(adminToken),
                KeyPairResponse.class
        );
        KeyPairResponse keyPair = keyPairResponse.getBody();

        // Verificar consistencia
        assertEquals(publicMap.get("keyId"), keyPair.getKeyId(), "Los keyId deben coincidir");
        assertEquals(publicMap.get("publicKey"), keyPair.getPublicKey(), "Las llaves públicas deben coincidir");
        assertEquals(publicMap.get("algorithm"), keyPair.getAlgorithm(), "Los algoritmos deben coincidir");
        assertEquals(publicMap.get("keySize"), keyPair.getKeySize(), "Los tamaños de llave deben coincidir");
    }

    // ===============================
    // MÉTODOS HELPER
    // ===============================

    /**
     * Helper para validar formato PEM
     */
    private void validatePemFormat(String pemKey, String keyType) {
        assertNotNull(pemKey, "La llave PEM no debe ser null");
        assertTrue(pemKey.startsWith("-----BEGIN " + keyType + "-----"), 
                   "La llave debe comenzar con el header PEM correcto");
        assertTrue(pemKey.endsWith("-----END " + keyType + "-----"), 
                   "La llave debe terminar con el footer PEM correcto");
        assertTrue(pemKey.contains("\n"), "La llave debe contener saltos de línea");
        
        // Verificar que el contenido entre headers no está vacío
        String content = pemKey.replace("-----BEGIN " + keyType + "-----", "")
                               .replace("-----END " + keyType + "-----", "")
                               .replaceAll("\\s", "");
        assertTrue(content.length() > 0, "El contenido de la llave no debe estar vacío");
    }
}