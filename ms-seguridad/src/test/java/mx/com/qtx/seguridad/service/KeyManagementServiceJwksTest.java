package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.config.RsaKeyProvider;
import mx.com.qtx.seguridad.dto.JwksResponse;
import mx.com.qtx.seguridad.dto.JwkKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para funcionalidad JWKS en KeyManagementService
 */
@DisplayName("KeyManagementService - JWKS Tests")
class KeyManagementServiceJwksTest {

    @Mock
    private RsaKeyProvider rsaKeyProvider;

    @InjectMocks
    private KeyManagementService keyManagementService;

    private RSAPublicKey testPublicKey;
    private RSAPrivateKey testPrivateKey;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Generar par de llaves RSA real para tests
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        testPublicKey = (RSAPublicKey) keyPair.getPublic();
        testPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Test
    @DisplayName("getPublicKeyAsJwks - Debe generar JWKS válido")
    void testGetPublicKeyAsJwks_Success() {
        // Arrange
        String testPem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----";
        
        when(rsaKeyProvider.getPublicKey()).thenReturn(testPublicKey);
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(testPem);

        // Act
        JwksResponse jwksResponse = keyManagementService.getPublicKeyAsJwks();

        // Assert
        assertNotNull(jwksResponse);
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
        
        // Verificar que el modulus y exponent están en Base64URL
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
        
        verify(rsaKeyProvider).getPublicKey();
        verify(rsaKeyProvider).getPublicKeyAsPem();
    }

    @Test
    @DisplayName("getPublicKeyAsJwks - Debe manejar error en RsaKeyProvider")
    void testGetPublicKeyAsJwks_RsaKeyProviderError() {
        // Arrange
        when(rsaKeyProvider.getPublicKey()).thenThrow(new RuntimeException("Error getting public key"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keyManagementService.getPublicKeyAsJwks();
        });
        
        assertEquals("Error al generar JWKS", exception.getMessage());
        verify(rsaKeyProvider).getPublicKey();
    }

    @Test
    @DisplayName("getPublicKeyAsJwks - Debe generar keyId consistente")
    void testGetPublicKeyAsJwks_ConsistentKeyId() {
        // Arrange
        String testPem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----";
        
        when(rsaKeyProvider.getPublicKey()).thenReturn(testPublicKey);
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(testPem);

        // Act
        JwksResponse jwksResponse1 = keyManagementService.getPublicKeyAsJwks();
        JwksResponse jwksResponse2 = keyManagementService.getPublicKeyAsJwks();

        // Assert
        assertNotNull(jwksResponse1);
        assertNotNull(jwksResponse2);
        
        String keyId1 = jwksResponse1.getKeys().get(0).getKeyId();
        String keyId2 = jwksResponse2.getKeys().get(0).getKeyId();
        
        assertEquals(keyId1, keyId2, "KeyId debe ser consistente para la misma llave");
    }

    @Test
    @DisplayName("getPublicKeyAsJwks - Debe generar componentes RSA válidos")
    void testGetPublicKeyAsJwks_ValidRsaComponents() {
        // Arrange
        String testPem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----";
        
        when(rsaKeyProvider.getPublicKey()).thenReturn(testPublicKey);
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(testPem);

        // Act
        JwksResponse jwksResponse = keyManagementService.getPublicKeyAsJwks();

        // Assert
        assertNotNull(jwksResponse);
        JwkKey jwkKey = jwksResponse.getKeys().get(0);
        
        // Verificar que los componentes RSA no están vacíos
        assertFalse(jwkKey.getModulus().isEmpty());
        assertFalse(jwkKey.getExponent().isEmpty());
        
        // Verificar que no contienen padding Base64
        assertFalse(jwkKey.getModulus().contains("="));
        assertFalse(jwkKey.getExponent().contains("="));
        
        // Verificar que el exponent es típicamente AQAB (65537)
        assertEquals("AQAB", jwkKey.getExponent());
    }

    @Test
    @DisplayName("getPublicKeyAsJwks - Debe encontrar key por ID")
    void testGetPublicKeyAsJwks_FindKeyById() {
        // Arrange
        String testPem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----";
        
        when(rsaKeyProvider.getPublicKey()).thenReturn(testPublicKey);
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(testPem);

        // Act
        JwksResponse jwksResponse = keyManagementService.getPublicKeyAsJwks();

        // Assert
        assertNotNull(jwksResponse);
        JwkKey originalKey = jwksResponse.getKeys().get(0);
        String keyId = originalKey.getKeyId();
        
        JwkKey foundKey = jwksResponse.findKeyById(keyId);
        assertNotNull(foundKey);
        assertEquals(originalKey.getKeyId(), foundKey.getKeyId());
        assertEquals(originalKey.getModulus(), foundKey.getModulus());
        assertEquals(originalKey.getExponent(), foundKey.getExponent());
        
        // Verificar que no encuentra keys inexistentes
        JwkKey notFoundKey = jwksResponse.findKeyById("nonexistent-key-id");
        assertNull(notFoundKey);
    }
}