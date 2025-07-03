package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.config.RsaKeyProvider;
import mx.com.qtx.seguridad.dto.PublicKeyResponse;
import mx.com.qtx.seguridad.dto.KeyPairResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para KeyManagementService
 * Verifica generación de llaves, rotación y exportación en formato PEM
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KeyManagementService Tests")
class KeyManagementServiceTest {

    @Mock
    private RsaKeyProvider rsaKeyProvider;

    private KeyManagementService keyManagementService;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private String publicKeyPem;
    private String privateKeyPem;

    @BeforeEach
    void setUp() throws Exception {
        // Generar par de llaves RSA para testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        // Simular formato PEM
        publicKeyPem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----";
        privateKeyPem = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\n-----END PRIVATE KEY-----";

        keyManagementService = new KeyManagementService(rsaKeyProvider);
    }

    @Test
    @DisplayName("Debe inicializar llaves al startup")
    void shouldInitializeKeysOnStartup() {
        // When
        keyManagementService.initializeKeys();

        // Then
        verify(rsaKeyProvider).generateKeyPair();
    }

    @Test
    @DisplayName("Debe obtener llave pública")
    void shouldGetPublicKey() {
        // Given
        when(rsaKeyProvider.getPublicKey()).thenReturn(publicKey);

        // When
        RSAPublicKey result = keyManagementService.getPublicKey();

        // Then
        assertNotNull(result);
        assertEquals(publicKey, result);
        verify(rsaKeyProvider).getPublicKey();
    }

    @Test
    @DisplayName("Debe obtener llave privada")
    void shouldGetPrivateKey() {
        // Given
        when(rsaKeyProvider.getPrivateKey()).thenReturn(privateKey);

        // When
        RSAPrivateKey result = keyManagementService.getPrivateKey();

        // Then
        assertNotNull(result);
        assertEquals(privateKey, result);
        verify(rsaKeyProvider).getPrivateKey();
    }

    @Test
    @DisplayName("Debe obtener llave pública en formato PEM")
    void shouldGetPublicKeyAsPem() {
        // Given
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(publicKeyPem);

        // When
        String result = keyManagementService.getPublicKeyAsPem();

        // Then
        assertNotNull(result);
        assertEquals(publicKeyPem, result);
        assertTrue(result.contains("-----BEGIN PUBLIC KEY-----"));
        assertTrue(result.contains("-----END PUBLIC KEY-----"));
        verify(rsaKeyProvider).getPublicKeyAsPem();
    }

    @Test
    @DisplayName("Debe obtener llave privada en formato PEM")
    void shouldGetPrivateKeyAsPem() {
        // Given
        when(rsaKeyProvider.getPrivateKeyAsPem()).thenReturn(privateKeyPem);

        // When
        String result = keyManagementService.getPrivateKeyAsPem();

        // Then
        assertNotNull(result);
        assertEquals(privateKeyPem, result);
        assertTrue(result.contains("-----BEGIN PRIVATE KEY-----"));
        assertTrue(result.contains("-----END PRIVATE KEY-----"));
        verify(rsaKeyProvider).getPrivateKeyAsPem();
    }

    @Test
    @DisplayName("Debe obtener par de llaves en formato PEM")
    void shouldGetKeyPairAsPem() {
        // Given
        long timestamp = System.currentTimeMillis();
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(publicKeyPem);
        when(rsaKeyProvider.getPrivateKeyAsPem()).thenReturn(privateKeyPem);
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(timestamp);

        // When
        Map<String, String> result = keyManagementService.getKeyPairAsPem();

        // Then
        assertNotNull(result);
        assertEquals(publicKeyPem, result.get("publicKey"));
        assertEquals(privateKeyPem, result.get("privateKey"));
        assertEquals("RSA", result.get("algorithm"));
        assertEquals("2048", result.get("keySize"));
        assertNotNull(result.get("generatedAt"));

        verify(rsaKeyProvider).getPublicKeyAsPem();
        verify(rsaKeyProvider).getPrivateKeyAsPem();
        verify(rsaKeyProvider).getKeyGenerationTimestamp();
    }

    @Test
    @DisplayName("Debe obtener información de llaves")
    void shouldGetKeyInfo() {
        // Given
        long timestamp = System.currentTimeMillis();
        String keyInfo = "RSA, 2048 bits";
        
        when(rsaKeyProvider.areKeysAvailable()).thenReturn(true);
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(timestamp);
        when(rsaKeyProvider.getKeyInfo()).thenReturn(keyInfo);

        // When
        Map<String, Object> result = keyManagementService.getKeyInfo();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("available"));
        assertNotNull(result.get("generatedAt"));
        assertEquals(timestamp, result.get("timestamp"));
        assertEquals(keyInfo, result.get("details"));

        verify(rsaKeyProvider).areKeysAvailable();
        verify(rsaKeyProvider, times(2)).getKeyGenerationTimestamp();
        verify(rsaKeyProvider).getKeyInfo();
    }

    @Test
    @DisplayName("Debe rotar llaves existentes")
    void shouldRotateExistingKeys() {
        // Given
        long oldTimestamp = System.currentTimeMillis() - 1000;
        long newTimestamp = System.currentTimeMillis();
        
        when(rsaKeyProvider.getKeyGenerationTimestamp())
                .thenReturn(oldTimestamp)
                .thenReturn(newTimestamp);

        // When
        Map<String, Object> result = keyManagementService.rotateKeys();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Llaves rotadas exitosamente", result.get("message"));
        assertEquals(oldTimestamp, result.get("oldKeyTimestamp"));
        assertEquals(newTimestamp, result.get("newKeyTimestamp"));
        assertNotNull(result.get("rotatedAt"));

        verify(rsaKeyProvider).rotateKeys();
        verify(rsaKeyProvider, times(2)).getKeyGenerationTimestamp();
    }

    @Test
    @DisplayName("Debe manejar error durante rotación de llaves")
    void shouldHandleErrorDuringKeyRotation() {
        // Given
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(System.currentTimeMillis());
        doThrow(new RuntimeException("Key rotation failed")).when(rsaKeyProvider).rotateKeys();

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> keyManagementService.rotateKeys());
        
        assertTrue(exception.getMessage().contains("Error crítico durante la rotación de llaves"));
        verify(rsaKeyProvider).rotateKeys();
    }

    @Test
    @DisplayName("Debe realizar health check correctamente")
    void shouldPerformHealthCheckCorrectly() {
        // Given
        long timestamp = System.currentTimeMillis();
        String keyInfo = "RSA, 2048 bits";
        
        when(rsaKeyProvider.areKeysAvailable()).thenReturn(true);
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(timestamp);
        when(rsaKeyProvider.getKeyInfo()).thenReturn(keyInfo);

        // When
        Map<String, Object> result = keyManagementService.healthCheck();

        // Then
        assertNotNull(result);
        assertEquals("UP", result.get("status"));
        assertTrue((Boolean) result.get("keysAvailable"));
        assertNotNull(result.get("keyAgeMs"));
        assertNotNull(result.get("keyAgeDays"));
        assertNotNull(result.get("lastCheck"));
        assertEquals(keyInfo, result.get("keyInfo"));

        verify(rsaKeyProvider).areKeysAvailable();
        verify(rsaKeyProvider, times(2)).getKeyGenerationTimestamp();
        verify(rsaKeyProvider).getKeyInfo();
    }

    @Test
    @DisplayName("Debe reportar DOWN cuando no hay llaves disponibles")
    void shouldReportDownWhenKeysNotAvailable() {
        // Given
        when(rsaKeyProvider.areKeysAvailable()).thenReturn(false);
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(System.currentTimeMillis());

        // When
        Map<String, Object> result = keyManagementService.healthCheck();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertFalse((Boolean) result.get("keysAvailable"));
        assertNotNull(result.get("lastCheck"));

        verify(rsaKeyProvider).areKeysAvailable();
    }

    @Test
    @DisplayName("Debe manejar error durante health check")
    void shouldHandleErrorDuringHealthCheck() {
        // Given
        when(rsaKeyProvider.areKeysAvailable()).thenThrow(new RuntimeException("Health check failed"));

        // When
        Map<String, Object> result = keyManagementService.healthCheck();

        // Then
        assertNotNull(result);
        assertEquals("ERROR", result.get("status"));
        assertEquals("Health check failed", result.get("error"));
        // lastCheck is not set in error case, so it will be null

        verify(rsaKeyProvider).areKeysAvailable();
    }

    @Test
    @DisplayName("Debe obtener llave pública como DTO")
    void shouldGetPublicKeyPemAsDto() {
        // Given
        long timestamp = System.currentTimeMillis();
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(publicKeyPem);
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(timestamp);

        // When
        PublicKeyResponse result = keyManagementService.getPublicKeyPem();

        // Then
        assertNotNull(result);
        assertTrue(result.hasPublicKey());
        assertTrue(result.isValidFormat());
        assertEquals("RSA", result.getAlgorithm());
        assertEquals("2048", result.getKeySize());
        assertEquals("PEM", result.getKeyFormat());
        assertNotNull(result.getKeyId());
        assertNotNull(result.getGeneratedAt());

        verify(rsaKeyProvider).getPublicKeyAsPem();
        verify(rsaKeyProvider).getKeyGenerationTimestamp();
    }

    @Test
    @DisplayName("Debe obtener par de llaves como DTO")
    void shouldGetKeyPairPemAsDto() {
        // Given
        long timestamp = System.currentTimeMillis();
        when(rsaKeyProvider.getPublicKeyAsPem()).thenReturn(publicKeyPem);
        when(rsaKeyProvider.getPrivateKeyAsPem()).thenReturn(privateKeyPem);
        when(rsaKeyProvider.getKeyGenerationTimestamp()).thenReturn(timestamp);

        // When
        KeyPairResponse result = keyManagementService.getKeyPairPem();

        // Then
        assertNotNull(result);
        assertTrue(result.hasPublicKey());
        assertTrue(result.hasPrivateKey());
        assertTrue(result.isCompleteKeyPair());
        assertTrue(result.isValidFormat());
        assertEquals("RSA", result.getAlgorithm());
        assertEquals("2048", result.getKeySize());
        assertNotNull(result.getKeyId());
        assertNotNull(result.getGeneratedAt());
        assertTrue(result.getWarning().contains("ALERTA"));

        verify(rsaKeyProvider).getPublicKeyAsPem();
        verify(rsaKeyProvider).getPrivateKeyAsPem();
        verify(rsaKeyProvider).getKeyGenerationTimestamp();
    }

    @Test
    @DisplayName("Debe generar nuevas llaves exitosamente")
    void shouldGenerateNewKeysSuccessfully() {
        // Given
        doNothing().when(rsaKeyProvider).rotateKeys();

        // When
        boolean result = keyManagementService.generateNewKeys();

        // Then
        assertTrue(result);
        verify(rsaKeyProvider).rotateKeys();
    }

    @Test
    @DisplayName("Debe manejar error al generar nuevas llaves")
    void shouldHandleErrorWhenGeneratingNewKeys() {
        // Given
        doThrow(new RuntimeException("Generation failed")).when(rsaKeyProvider).rotateKeys();

        // When
        boolean result = keyManagementService.generateNewKeys();

        // Then
        assertFalse(result);
        verify(rsaKeyProvider).rotateKeys();
    }

    @Test
    @DisplayName("Debe retornar error cuando falla obtener llave pública PEM")
    void shouldReturnErrorWhenPublicKeyPemFails() {
        // Given
        when(rsaKeyProvider.getPublicKeyAsPem()).thenThrow(new RuntimeException("PEM generation failed"));

        // When
        PublicKeyResponse result = keyManagementService.getPublicKeyPem();

        // Then
        assertNotNull(result);
        assertFalse(result.hasPublicKey());
        assertTrue(result.getUsage().contains("Error"));
        
        verify(rsaKeyProvider).getPublicKeyAsPem();
    }

    @Test
    @DisplayName("Debe retornar error cuando falla obtener par de llaves PEM")
    void shouldReturnErrorWhenKeyPairPemFails() {
        // Given
        when(rsaKeyProvider.getPublicKeyAsPem()).thenThrow(new RuntimeException("PEM generation failed"));

        // When
        KeyPairResponse result = keyManagementService.getKeyPairPem();

        // Then
        assertNotNull(result);
        assertFalse(result.hasPublicKey());
        assertFalse(result.hasPrivateKey());
        assertTrue(result.getWarning().contains("Error"));
        
        verify(rsaKeyProvider).getPublicKeyAsPem();
    }

    @Test
    @DisplayName("Debe manejar error al obtener información de llaves")
    void shouldHandleErrorWhenGettingKeyInfo() {
        // Given
        when(rsaKeyProvider.areKeysAvailable()).thenThrow(new RuntimeException("Info retrieval failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> keyManagementService.getKeyInfo());
        
        assertTrue(exception.getMessage().contains("Error al obtener información de llaves"));
        verify(rsaKeyProvider).areKeysAvailable();
    }
}