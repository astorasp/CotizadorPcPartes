package mx.com.qtx.seguridad.controller;

import mx.com.qtx.seguridad.config.TestContainerConfig;
import mx.com.qtx.seguridad.dto.JwksResponse;
import mx.com.qtx.seguridad.dto.JwkKey;
import mx.com.qtx.seguridad.service.KeyManagementService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para endpoint JWKS
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@DisplayName("KeyController - JWKS Integration Tests")
class KeyControllerJwksIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KeyManagementService keyManagementService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/seguridad/keys";
    }

    @Test
    @DisplayName("GET /keys/jwks - Debe retornar JWKS válido")
    void testGetJwks_Success() {
        // Act
        ResponseEntity<JwksResponse> response = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            JwksResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        JwksResponse jwksResponse = response.getBody();
        assertTrue(jwksResponse.hasValidKeys());
        assertEquals(1, jwksResponse.getKeyCount());
        
        JwkKey jwkKey = jwksResponse.getKeys().get(0);
        assertNotNull(jwkKey);
        assertEquals("RSA", jwkKey.getKeyType());
        assertEquals("sig", jwkKey.getPublicKeyUse());
        assertEquals("RS256", jwkKey.getAlgorithm());
        assertNotNull(jwkKey.getKeyId());
        assertNotNull(jwkKey.getModulus());
        assertNotNull(jwkKey.getExponent());
        
        // Verificar formato Base64URL
        assertDoesNotThrow(() -> {
            // Base64URL sin padding - agregar padding si es necesario
            String modulus = jwkKey.getModulus();
            String exponent = jwkKey.getExponent();
            
            // Agregar padding si es necesario
            while (modulus.length() % 4 != 0) {
                modulus += "=";
            }
            while (exponent.length() % 4 != 0) {
                exponent += "=";
            }
            
            Base64.getUrlDecoder().decode(modulus);
            Base64.getUrlDecoder().decode(exponent);
        });
    }

    @Test
    @DisplayName("GET /keys/jwks - Debe ser accesible públicamente")
    void testGetJwks_PublicAccess() {
        // Act
        ResponseEntity<JwksResponse> response = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            JwksResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verificar que es accesible sin autenticación
        assertTrue(response.getBody().hasValidKeys());
    }

    @Test
    @DisplayName("GET /keys/jwks - Debe retornar keyId consistente entre peticiones")
    void testGetJwks_ConsistentKeyId() {
        // Act
        ResponseEntity<JwksResponse> jwksResponse1 = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            JwksResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, jwksResponse1.getStatusCode());
        assertNotNull(jwksResponse1.getBody());
        
        JwkKey jwkKey = jwksResponse1.getBody().getKeys().get(0);
        assertNotNull(jwkKey.getKeyId());
        
        // Verificar que el keyId es consistente
        String keyId1 = jwkKey.getKeyId();
        
        // Hacer segunda petición
        ResponseEntity<JwksResponse> jwksResponse2 = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            JwksResponse.class
        );
        
        assertEquals(HttpStatus.OK, jwksResponse2.getStatusCode());
        String keyId2 = jwksResponse2.getBody().getKeys().get(0).getKeyId();
        
        assertEquals(keyId1, keyId2, "KeyId debe ser consistente entre peticiones");
    }

    @Test
    @DisplayName("GET /keys/jwks - Debe tener componentes RSA válidos")
    void testGetJwks_ValidRsaComponents() {
        // Act
        ResponseEntity<JwksResponse> response = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            JwksResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        JwkKey jwkKey = response.getBody().getKeys().get(0);
        
        // Verificar componentes RSA
        assertFalse(jwkKey.getModulus().isEmpty());
        assertFalse(jwkKey.getExponent().isEmpty());
        
        // Verificar que no contienen padding Base64
        assertFalse(jwkKey.getModulus().contains("="));
        assertFalse(jwkKey.getExponent().contains("="));
        
        // Verificar que el exponent es típicamente AQAB (65537)
        assertEquals("AQAB", jwkKey.getExponent());
        
        // Verificar que el modulus tiene longitud apropiada para RSA-2048
        // Base64URL de 2048 bits debería tener ~344 caracteres
        assertTrue(jwkKey.getModulus().length() > 300);
    }

    @Test
    @DisplayName("GET /keys/jwks - Debe permitir búsqueda por keyId")
    void testGetJwks_FindKeyById() {
        // Act
        ResponseEntity<JwksResponse> response = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            JwksResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        JwksResponse jwksResponse = response.getBody();
        JwkKey originalKey = jwksResponse.getKeys().get(0);
        String keyId = originalKey.getKeyId();
        
        // Buscar por keyId
        JwkKey foundKey = jwksResponse.findKeyById(keyId);
        assertNotNull(foundKey);
        assertEquals(originalKey.getKeyId(), foundKey.getKeyId());
        assertEquals(originalKey.getModulus(), foundKey.getModulus());
        assertEquals(originalKey.getExponent(), foundKey.getExponent());
        
        // Verificar que no encuentra keys inexistentes
        JwkKey notFoundKey = jwksResponse.findKeyById("nonexistent-key-id");
        assertNull(notFoundKey);
    }

    @Test
    @DisplayName("GET /keys/jwks - Debe seguir formato estándar JWKS")
    void testGetJwks_StandardJwksFormat() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/jwks", 
            String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String jsonResponse = response.getBody();
        
        // Verificar que contiene campos obligatorios JWKS
        assertTrue(jsonResponse.contains("\"keys\""));
        assertTrue(jsonResponse.contains("\"kty\""));
        assertTrue(jsonResponse.contains("\"use\""));
        assertTrue(jsonResponse.contains("\"kid\""));
        assertTrue(jsonResponse.contains("\"alg\""));
        assertTrue(jsonResponse.contains("\"n\""));
        assertTrue(jsonResponse.contains("\"e\""));
        
        // Verificar valores esperados
        assertTrue(jsonResponse.contains("\"kty\":\"RSA\""));
        assertTrue(jsonResponse.contains("\"use\":\"sig\""));
        assertTrue(jsonResponse.contains("\"alg\":\"RS256\""));
        assertTrue(jsonResponse.contains("\"e\":\"AQAB\""));
    }
}