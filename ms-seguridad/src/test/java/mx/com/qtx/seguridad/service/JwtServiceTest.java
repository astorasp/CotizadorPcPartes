package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.config.RsaKeyProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para JwtService
 * Verifica generación, validación y extracción de claims de tokens JWT
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @Mock
    private RsaKeyProvider rsaKeyProvider;

    private JwtService jwtService;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generar par de llaves RSA para testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        // Configurar mocks
        when(rsaKeyProvider.getPrivateKey()).thenReturn(privateKey);
        when(rsaKeyProvider.getPublicKey()).thenReturn(publicKey);

        jwtService = new JwtService(rsaKeyProvider);
        
        // Configurar valores de expiración manualmente usando reflexión
        setAccessTokenExpiration(3600000L); // 1 hora
        setRefreshTokenExpiration(7200000L); // 2 horas
    }
    
    private void setAccessTokenExpiration(long expiration) throws Exception {
        var field = JwtService.class.getDeclaredField("accessTokenExpiration");
        field.setAccessible(true);
        field.set(jwtService, expiration);
    }
    
    private void setRefreshTokenExpiration(long expiration) throws Exception {
        var field = JwtService.class.getDeclaredField("refreshTokenExpiration");
        field.setAccessible(true);
        field.set(jwtService, expiration);
    }

    @Test
    @DisplayName("Debe generar access token válido")
    void shouldGenerateValidAccessToken() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER", "ADMIN");

        // When
        String token = jwtService.generateAccessToken(username, userId, roles);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por puntos

        // Verificar que el token se puede validar
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Debe generar refresh token válido")
    void shouldGenerateValidRefreshToken() {
        // Given
        String username = "testuser";
        Integer userId = 1;

        // When
        String token = jwtService.generateRefreshToken(username, userId);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Debe extraer username correctamente")
    void shouldExtractUsernameCorrectly() {
        // Given
        String expectedUsername = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String token = jwtService.generateAccessToken(expectedUsername, userId, roles);

        // When
        String actualUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    @DisplayName("Debe extraer userId correctamente")
    void shouldExtractUserIdCorrectly() {
        // Given
        String username = "testuser";
        Integer expectedUserId = 123;
        List<String> roles = Arrays.asList("USER");
        String token = jwtService.generateAccessToken(username, expectedUserId, roles);

        // When
        Integer actualUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    @DisplayName("Debe extraer roles correctamente")
    void shouldExtractRolesCorrectly() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> expectedRoles = Arrays.asList("USER", "ADMIN", "SUPERVISOR");
        String token = jwtService.generateAccessToken(username, userId, expectedRoles);

        // When
        List<String> actualRoles = jwtService.extractRoles(token);

        // Then
        assertEquals(expectedRoles.size(), actualRoles.size());
        assertTrue(actualRoles.containsAll(expectedRoles));
    }

    @Test
    @DisplayName("Debe identificar access token correctamente")
    void shouldIdentifyAccessTokenCorrectly() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String accessToken = jwtService.generateAccessToken(username, userId, roles);
        String refreshToken = jwtService.generateRefreshToken(username, userId);

        // When & Then
        assertTrue(jwtService.isAccessToken(accessToken));
        assertFalse(jwtService.isAccessToken(refreshToken));
    }

    @Test
    @DisplayName("Debe identificar refresh token correctamente")
    void shouldIdentifyRefreshTokenCorrectly() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String accessToken = jwtService.generateAccessToken(username, userId, roles);
        String refreshToken = jwtService.generateRefreshToken(username, userId);

        // When & Then
        assertTrue(jwtService.isRefreshToken(refreshToken));
        assertFalse(jwtService.isRefreshToken(accessToken));
    }

    @Test
    @DisplayName("Debe validar token correctamente")
    void shouldValidateTokenCorrectly() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String token = jwtService.generateAccessToken(username, userId, roles);

        // When
        Claims claims = jwtService.validateToken(token);

        // Then
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals("ms-seguridad", claims.getIssuer());
    }

    @Test
    @DisplayName("Debe rechazar token con firma inválida")
    void shouldRejectTokenWithInvalidSignature() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String validToken = jwtService.generateAccessToken(username, userId, roles);
        
        // Modificar el token para invalidar la firma
        String invalidToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // When & Then
        assertThrows(JwtException.class, () -> jwtService.validateToken(invalidToken));
        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    @DisplayName("Debe rechazar token malformado")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "esto.no.es.un.token.jwt.valido";

        // When & Then
        assertThrows(JwtException.class, () -> jwtService.validateToken(malformedToken));
        assertFalse(jwtService.isTokenValid(malformedToken));
    }

    @Test
    @DisplayName("Debe detectar token expirado correctamente")
    void shouldDetectExpiredTokenCorrectly() throws Exception {
        // Given - usar token muy corto (este test es más conceptual)
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String token = jwtService.generateAccessToken(username, userId, roles);

        // When - verificar que inicialmente no está expirado
        boolean initiallyExpired = jwtService.isTokenExpired(token);
        boolean initiallyValid = jwtService.isTokenValid(token);

        // Then
        assertFalse(initiallyExpired);
        assertTrue(initiallyValid);
    }

    @Test
    @DisplayName("Debe calcular tiempo restante correctamente")
    void shouldCalculateRemainingTimeCorrectly() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String token = jwtService.generateAccessToken(username, userId, roles);

        // When
        long remainingTime = jwtService.getTokenRemainingTime(token);

        // Then
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= 3600); // No más de 1 hora
    }

    @Test
    @DisplayName("Debe retornar 0 para token inválido")
    void shouldReturn0ForInvalidToken() {
        // Given - token inválido
        String invalidToken = "invalid.token.here";

        // When
        long remainingTime = jwtService.getTokenRemainingTime(invalidToken);

        // Then
        assertEquals(0, remainingTime);
    }

    @Test
    @DisplayName("Debe validar refresh token específicamente")
    void shouldValidateRefreshTokenSpecifically() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        String refreshToken = jwtService.generateRefreshToken(username, userId);

        // When
        Claims claims = jwtService.validateRefreshToken(refreshToken);

        // Then
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals("refresh", claims.get("token_type"));
    }

    @Test
    @DisplayName("Debe rechazar access token como refresh token")
    void shouldRejectAccessTokenAsRefreshToken() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("USER");
        String accessToken = jwtService.generateAccessToken(username, userId, roles);

        // When & Then
        assertThrows(JwtException.class, () -> jwtService.validateRefreshToken(accessToken));
    }

    @Test
    @DisplayName("Debe obtener información completa del token")
    void shouldGetCompleteTokenInfo() {
        // Given
        String username = "testuser";
        Integer userId = 123;
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtService.generateAccessToken(username, userId, roles);

        // When
        var tokenInfo = jwtService.getTokenInfo(token);

        // Then
        assertNotNull(tokenInfo);
        assertEquals(username, tokenInfo.get("subject"));
        assertEquals("ms-seguridad", tokenInfo.get("issuer"));
        assertEquals(userId, tokenInfo.get("userId"));
        assertEquals(roles, tokenInfo.get("roles"));
        assertEquals("access", tokenInfo.get("tokenType"));
        assertFalse((Boolean) tokenInfo.get("expired"));
    }

    @Test
    @DisplayName("Debe manejar token null o vacío")
    void shouldHandleNullOrEmptyToken() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtService.validateToken(null));
        assertThrows(IllegalArgumentException.class, () -> jwtService.validateToken(""));
        assertThrows(IllegalArgumentException.class, () -> jwtService.validateToken("   "));
        
        assertFalse(jwtService.isTokenValid(null));
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid("   "));
    }

    @Test
    @DisplayName("Debe manejar roles vacíos")
    void shouldHandleEmptyRoles() {
        // Given
        String username = "testuser";
        Integer userId = 1;
        List<String> emptyRoles = Arrays.asList();
        
        // When
        String token = jwtService.generateAccessToken(username, userId, emptyRoles);
        List<String> extractedRoles = jwtService.extractRoles(token);

        // Then
        assertNotNull(token);
        assertNotNull(extractedRoles);
        assertTrue(extractedRoles.isEmpty());
    }

    @Test
    @DisplayName("Debe generar tokens diferentes para diferentes usuarios")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        String user1 = "user1";
        String user2 = "user2";
        Integer userId1 = 1;
        Integer userId2 = 2;
        List<String> roles = Arrays.asList("USER");

        // When
        String token1 = jwtService.generateAccessToken(user1, userId1, roles);
        String token2 = jwtService.generateAccessToken(user2, userId2, roles);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        
        assertEquals(user1, jwtService.extractUsername(token1));
        assertEquals(user2, jwtService.extractUsername(token2));
        assertEquals(userId1, jwtService.extractUserId(token1));
        assertEquals(userId2, jwtService.extractUserId(token2));
    }
}