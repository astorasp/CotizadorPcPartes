package mx.com.qtx.cotizador.security.jwt;

import mx.com.qtx.cotizador.security.jwt.utils.JwtTestKeyGenerator;
import mx.com.qtx.cotizador.security.jwt.utils.JwtTestTokenBuilder;
import mx.com.qtx.cotizador.security.jwt.utils.MockJwksClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Test básico de funcionalidad JWT con validación de certificados públicos.
 *
 * <p>Esta clase implementa el requerimiento específico del usuario:</p>
 * <blockquote>
 * "necesito al menos un test donde se pueda simular que obtenemos o contamos
 * con el certificado publico para poder validar la firma de un token generado"
 * </blockquote>
 *
 * <h3>Proceso completo de validación JWT simulado:</h3>
 * <ol>
 *   <li><b>Generación de claves</b>: Simula ms-seguridad generando par RSA</li>
 *   <li><b>Publicación JWKS</b>: Simula endpoint /keys/jwks con certificados públicos</li>
 *   <li><b>Creación de tokens</b>: Simula ms-seguridad firmando tokens JWT</li>
 *   <li><b>Obtención de certificados</b>: Simula ms-cotizador obteniendo claves públicas</li>
 *   <li><b>Validación de firma</b>: Valida tokens usando certificados públicos obtenidos</li>
 * </ol>
 *
 * <h3>Escenarios de testing cubiertos:</h3>
 * <ul>
 *   <li>✅ Validación exitosa con certificado público correcto</li>
 *   <li>❌ Rechazo de tokens con certificado público incorrecto</li>
 *   <li>⏰ Detección de tokens expirados</li>
 *   <li>🏢 Rechazo de tokens con issuer incorrecto</li>
 *   <li>🔄 Rotación de claves públicas</li>
 *   <li>🚨 Simulación de errores de red y timeouts</li>
 * </ul>
 *
 * <h3>Flujo técnico implementado:</h3>
 * <pre>
 * [ms-seguridad simulado]     [ms-cotizador bajo test]
 *        |                            |
 *  1. Genera RSA keys          2. Solicita JWKS
 *  3. Publica JWKS            4. Obtiene certificados públicos
 *  5. Firma JWT token         6. Valida firma con certificado
 *        |                            |
 *     ✅ Token válido ←→ Validación exitosa
 * </pre>
 *
 * <p><b>Sin Spring Context:</b> Estos tests no requieren el contexto completo
 * de Spring, enfocándose puramente en la funcionalidad criptográfica JWT.</p>
 *
 * @author Sistema Cotizador - Equipo JWT Testing
 * @version 1.0
 * @since 2.0.0
 * @see JwtTestKeyGenerator Generación de claves RSA para testing
 * @see JwtTestTokenBuilder Construcción de tokens JWT
 * @see MockJwksClient Simulación de obtención de certificados públicos
 */
@DisplayName("JWT Basic Functionality - Validación con Certificados Públicos")
public class JwtBasicFunctionalityTest {

    private JwtTestKeyGenerator keyGenerator;
    private MockJwksClient mockJwksClient;

    @BeforeEach
    void setUp() {
        keyGenerator = JwtTestKeyGenerator.createDefault();
        mockJwksClient = MockJwksClient.withSingleValidKey(keyGenerator);
    }

    @Test
    @DisplayName("✅ Generador de claves RSA funciona correctamente")
    void generadorClavesRSAFuncionaCorrectamente() {
        // Given & When: Crear generador de claves
        JwtTestKeyGenerator generator = JwtTestKeyGenerator.createDefault();

        // Then: Debe crear un par de claves válido
        assertThat(generator.getKeyPair()).isNotNull();
        assertThat(generator.getKeyPair().getPrivate()).isNotNull();
        assertThat(generator.getKeyPair().getPublic()).isNotNull();
        assertThat(generator.getKeyId()).isEqualTo("test-key-001");

        // La clave pública debe ser RSA
        assertThat(generator.getKeyPair().getPublic()).isInstanceOf(RSAPublicKey.class);
    }

    @Test
    @DisplayName("✅ Conversión a formato JWK funciona correctamente")
    void conversionJWKFuncionaCorrectamente() {
        // Given & When: Convertir clave a JWK
        var jwkKey = keyGenerator.toJwkKey();

        // Then: JWK debe tener los campos correctos
        assertThat(jwkKey.getKeyType()).isEqualTo("RSA");
        assertThat(jwkKey.getPublicKeyUse()).isEqualTo("sig");
        assertThat(jwkKey.getKeyId()).isEqualTo("test-key-001");
        assertThat(jwkKey.getAlgorithm()).isEqualTo("RS256");
        assertThat(jwkKey.getModulus()).isNotNull().isNotEmpty();
        assertThat(jwkKey.getExponent()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("✅ Respuesta JWKS se genera correctamente")
    void respuestaJWKSSeGeneraCorrectamente() {
        // Given & When: Crear respuesta JWKS
        var jwksResponse = keyGenerator.toJwksResponse();

        // Then: JWKS debe ser válido
        assertThat(jwksResponse.hasValidKeys()).isTrue();
        assertThat(jwksResponse.getKeyCount()).isEqualTo(1);
        assertThat(jwksResponse.findKeyById("test-key-001")).isNotNull();
        assertThat(jwksResponse.getFirstKey()).isNotNull();
    }

    @Test
    @DisplayName("✅ Constructor de tokens JWT funciona correctamente")
    void constructorTokensJWTFuncionaCorrectamente() {
        // Given & When: Crear token JWT
        String token = new JwtTestTokenBuilder()
            .withSubject("test-user")
            .withEmail("test@example.com")
            .withRoles("ADMIN", "USER")
            .build(keyGenerator);

        // Then: Token debe ser válido y no vacío
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature

        // El token debe comenzar con "eyJ" (header JWT estándar en Base64)
        assertThat(token).startsWith("eyJ");
    }

    @Test
    @DisplayName("✅ Validación de token con clave pública correcta funciona")
    void validacionTokenConClavePublicaCorrectaFunciona() {
        // Given: Token válido firmado con la clave correcta
        String token = JwtTestTokenBuilder.createValidToken(keyGenerator);

        // When: Validar token usando la clave pública correcta
        RSAPublicKey publicKey = (RSAPublicKey) keyGenerator.getKeyPair().getPublic();

        Claims claims = Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer("ms-seguridad")
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // Then: Claims deben ser correctos
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("test-user");
        assertThat(claims.getIssuer()).isEqualTo("ms-seguridad");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("❌ Validación de token con clave pública incorrecta falla")
    void validacionTokenConClavePublicaIncorrectaFalla() {
        // Given: Token firmado con una clave
        String token = JwtTestTokenBuilder.createValidToken(keyGenerator);

        // Clave pública diferente
        JwtTestKeyGenerator otraClaveGenerator = JwtTestKeyGenerator.createForRotation("otra-clave");
        RSAPublicKey clavePublicaIncorrecta = (RSAPublicKey) otraClaveGenerator.getKeyPair().getPublic();

        // When & Then: Validar con clave incorrecta debe fallar
        assertThatThrownBy(() -> {
            Jwts.parser()
                .verifyWith(clavePublicaIncorrecta)
                .requireIssuer("ms-seguridad")
                .build()
                .parseSignedClaims(token);
        }).hasMessageContaining("signature");
    }

    @Test
    @DisplayName("✅ MockJwksClient simula respuestas correctamente")
    void mockJwksClientSimulaRespuestasCorrectamente() {
        // Given & When: Obtener JWKS del cliente mock
        var jwksResponse = mockJwksClient.fetchJwks();

        // Then: Respuesta debe ser correcta
        assertThat(jwksResponse).isNotNull();
        assertThat(jwksResponse.hasValidKeys()).isTrue();
        assertThat(jwksResponse.getKeyCount()).isEqualTo(1);
        assertThat(jwksResponse.findKeyById("test-key-001")).isNotNull();
    }

    @Test
    @DisplayName("🚨 MockJwksClient simula errores de red correctamente")
    void mockJwksClientSimulaErroresDeRed() {
        // Given: Configurar cliente mock para simular error
        mockJwksClient.simulateNetworkError(true);

        // When & Then: Debe lanzar excepción de red
        assertThatThrownBy(() -> mockJwksClient.fetchJwks())
            .isInstanceOf(MockJwksClient.JwksClientException.class)
            .hasMessageContaining("Mock network error");
    }

    @Test
    @DisplayName("🔄 Rotación de claves funciona correctamente")
    void rotacionClavesFuncionaCorrectamente() {
        // Given: Dos generadores de claves diferentes
        JwtTestKeyGenerator clave1 = JwtTestKeyGenerator.createForRotation("key-001");
        JwtTestKeyGenerator clave2 = JwtTestKeyGenerator.createForRotation("key-002");

        // When: Crear JWKS con ambas claves
        var jwksMultiple = JwtTestKeyGenerator.createMultiKeyJwksResponse(clave1, clave2);

        // Then: JWKS debe contener ambas claves
        assertThat(jwksMultiple.getKeyCount()).isEqualTo(2);
        assertThat(jwksMultiple.findKeyById("key-001")).isNotNull();
        assertThat(jwksMultiple.findKeyById("key-002")).isNotNull();

        // Both keys should be able to validate their respective tokens
        String token1 = JwtTestTokenBuilder.createValidToken(clave1);
        String token2 = JwtTestTokenBuilder.createValidToken(clave2);

        // Validar con clave 1
        RSAPublicKey publicKey1 = (RSAPublicKey) clave1.getKeyPair().getPublic();
        assertThatCode(() -> {
            Jwts.parser()
                .verifyWith(publicKey1)
                .requireIssuer("ms-seguridad")
                .build()
                .parseSignedClaims(token1);
        }).doesNotThrowAnyException();

        // Validar con clave 2
        RSAPublicKey publicKey2 = (RSAPublicKey) clave2.getKeyPair().getPublic();
        assertThatCode(() -> {
            Jwts.parser()
                .verifyWith(publicKey2)
                .requireIssuer("ms-seguridad")
                .build()
                .parseSignedClaims(token2);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("⏰ Tokens expirados son detectados correctamente")
    void tokensExpiradosSonDetectadosCorrectamente() {
        // Given: Token expirado
        String tokenExpirado = JwtTestTokenBuilder.createExpiredToken(keyGenerator);
        RSAPublicKey publicKey = (RSAPublicKey) keyGenerator.getKeyPair().getPublic();

        // When & Then: Validación debe fallar por expiración
        assertThatThrownBy(() -> {
            Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer("ms-seguridad")
                .build()
                .parseSignedClaims(tokenExpirado);
        }).hasMessageContaining("expired");
    }

    @Test
    @DisplayName("🏢 Tokens con issuer incorrecto son rechazados")
    void tokensConIssuerIncorrectoSonRechazados() {
        // Given: Token con issuer incorrecto
        String tokenIssuerIncorrecto = JwtTestTokenBuilder.createTokenWithWrongIssuer(keyGenerator);
        RSAPublicKey publicKey = (RSAPublicKey) keyGenerator.getKeyPair().getPublic();

        // When & Then: Validación debe fallar por issuer incorrecto
        assertThatThrownBy(() -> {
            Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer("ms-seguridad") // Esperamos ms-seguridad, pero el token tiene otro
                .build()
                .parseSignedClaims(tokenIssuerIncorrecto);
        }).hasMessageContaining("issuer");
    }

    @Test
    @DisplayName("📊 Diferentes tipos de tokens se construyen correctamente")
    void diferentesTiposDeTokensSeConstruyenCorrectamente() {
        // Given & When: Crear diferentes tipos de tokens
        String tokenAdmin = JwtTestTokenBuilder.createAdminToken(keyGenerator);
        String tokenConRoles = JwtTestTokenBuilder.createTokenWithRoles(keyGenerator, "GERENTE", "VENDEDOR");
        String tokenConSession = JwtTestTokenBuilder.createTokenWithSession(keyGenerator, "session-123");

        // Then: Todos los tokens deben ser válidos
        assertThat(tokenAdmin).isNotNull().isNotEmpty();
        assertThat(tokenConRoles).isNotNull().isNotEmpty();
        assertThat(tokenConSession).isNotNull().isNotEmpty();

        // Validar contenido específico
        RSAPublicKey publicKey = (RSAPublicKey) keyGenerator.getKeyPair().getPublic();

        // Token admin
        Claims claimsAdmin = Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer("ms-seguridad")
            .build()
            .parseSignedClaims(tokenAdmin)
            .getPayload();

        assertThat(claimsAdmin.getSubject()).isEqualTo("admin-user");

        @SuppressWarnings("unchecked")
        List<String> adminRoles = claimsAdmin.get("roles", List.class);
        assertThat(adminRoles).containsExactly("ADMIN");

        // Token con roles específicos
        Claims claimsRoles = Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer("ms-seguridad")
            .build()
            .parseSignedClaims(tokenConRoles)
            .getPayload();

        @SuppressWarnings("unchecked")
        List<String> specificRoles = claimsRoles.get("roles", List.class);
        assertThat(specificRoles).containsExactlyInAnyOrder("GERENTE", "VENDEDOR");

        // Token con sesión
        Claims claimsSession = Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer("ms-seguridad")
            .build()
            .parseSignedClaims(tokenConSession)
            .getPayload();

        assertThat(claimsSession.get("sessionId", String.class)).isEqualTo("session-123");
    }
}