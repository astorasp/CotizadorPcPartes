package mx.com.qtx.cotizador.security.jwt.utils;

import mx.com.qtx.cotizador.security.dto.JwkKey;
import mx.com.qtx.cotizador.security.dto.JwksResponse;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

/**
 * Generador de claves RSA para testing JWT con certificados públicos.
 *
 * Esta clase permite simular el proceso completo de obtención y validación
 * de certificados públicos para tokens JWT, tal como ocurre en producción
 * entre ms-seguridad (generador) y ms-cotizador (validador).
 *
 * <h3>Funcionalidades principales:</h3>
 * <ul>
 *   <li>Genera pares de claves RSA 2048-bit (estándar de seguridad)</li>
 *   <li>Convierte claves públicas al formato JWKS (RFC 7517)</li>
 *   <li>Simula rotación de claves para testing</li>
 *   <li>Proporciona múltiples escenarios de testing</li>
 * </ul>
 *
 * <h3>Por qué RSA 2048-bit:</h3>
 * - Estándar recomendado por NIST para JWT
 * - Compatible con algoritmo RS256 (RSA + SHA-256)
 * - Balance entre seguridad y rendimiento
 *
 * <h3>Formato JWKS (JSON Web Key Set):</h3>
 * Convierte las claves RSA al formato estándar que usa ms-seguridad
 * para distribuir certificados públicos via endpoint /keys/jwks
 *
 * <h3>Ejemplo de uso básico:</h3>
 * <pre>{@code
 * // Crear generador con clave por defecto
 * JwtTestKeyGenerator keyGen = JwtTestKeyGenerator.createDefault();
 *
 * // Obtener certificado público
 * RSAPublicKey publicKey = (RSAPublicKey) keyGen.getKeyPair().getPublic();
 *
 * // Validar token JWT con certificado público
 * Claims claims = Jwts.parser()
 *     .verifyWith(publicKey)
 *     .requireIssuer("ms-seguridad")
 *     .build()
 *     .parseSignedClaims(token)
 *     .getPayload();
 * }</pre>
 *
 * <h3>Ejemplo de rotación de claves:</h3>
 * <pre>{@code
 * // Crear múltiples claves para simular rotación
 * JwtTestKeyGenerator clave1 = JwtTestKeyGenerator.createForRotation("key-001");
 * JwtTestKeyGenerator clave2 = JwtTestKeyGenerator.createForRotation("key-002");
 *
 * // Crear respuesta JWKS con ambas claves
 * JwksResponse multiKeyJwks = JwtTestKeyGenerator.createMultiKeyJwksResponse(clave1, clave2);
 * }</pre>
 *
 * @author Sistema Cotizador - Equipo JWT Testing
 * @version 1.0
 * @since 2.0.0
 * @see mx.com.qtx.cotizador.security.dto.JwksResponse
 * @see mx.com.qtx.cotizador.security.dto.JwkKey
 */
public class JwtTestKeyGenerator {

    private static final String KEY_TYPE = "RSA";
    private static final String PUBLIC_KEY_USE = "sig";
    private static final String ALGORITHM = "RS256";
    private static final int KEY_SIZE = 2048;

    private final KeyPair keyPair;
    private final String keyId;

    public JwtTestKeyGenerator(String keyId) {
        this.keyId = keyId;
        this.keyPair = generateKeyPair();
    }

    /**
     * Genera un nuevo par de claves RSA 2048-bit para testing JWT.
     *
     * <p>Utiliza el algoritmo RSA estándar con tamaño de clave de 2048 bits,
     * que es el mínimo recomendado por NIST para aplicaciones comerciales.
     * Este tamaño proporciona seguridad adecuada mientras mantiene un
     * rendimiento aceptable para testing.</p>
     *
     * @return Par de claves RSA (pública/privada) generado criptográficamente
     * @throws RuntimeException si el algoritmo RSA no está disponible en el JVM
     */
    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating RSA key pair for testing", e);
        }
    }

    /**
     * Obtiene el par de claves RSA generado.
     *
     * <p>Devuelve el par completo de claves (pública y privada) generado
     * durante la construcción del objeto. La clave privada se usa para firmar
     * tokens JWT, mientras que la clave pública se usa para validarlos.</p>
     *
     * @return KeyPair conteniendo la clave pública y privada RSA
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * Obtiene el identificador único de la clave (Key ID).
     *
     * <p>El Key ID es un identificador único que se incluye en el header
     * del token JWT y permite al validador seleccionar la clave correcta
     * cuando existen múltiples claves disponibles (rotación de claves).</p>
     *
     * @return String identificador único de esta clave (ej: "test-key-001")
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Convierte la clave pública RSA al formato JWK (JSON Web Key) estándar.
     *
     * <p>Implementa la conversión según RFC 7517 - JSON Web Key (JWK) format.
     * Extrae el módulo (n) y exponente (e) de la clave pública RSA y los
     * codifica en Base64 URL-safe sin padding, tal como requiere el estándar.</p>
     *
     * <p>Los campos del JWK generado son:</p>
     * <ul>
     *   <li><b>kty</b>: "RSA" (tipo de clave)</li>
     *   <li><b>use</b>: "sig" (uso para firmas)</li>
     *   <li><b>kid</b>: Key ID único</li>
     *   <li><b>alg</b>: "RS256" (algoritmo RSA + SHA-256)</li>
     *   <li><b>n</b>: Módulo RSA en Base64 URL-safe</li>
     *   <li><b>e</b>: Exponente público en Base64 URL-safe</li>
     * </ul>
     *
     * @return JwkKey objeto que representa esta clave en formato JWKS
     */
    public JwkKey toJwkKey() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // Convertir modulus y exponent a Base64 URL-safe
        String modulus = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(publicKey.getModulus().toByteArray());

        String exponent = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(publicKey.getPublicExponent().toByteArray());

        return new JwkKey(KEY_TYPE, PUBLIC_KEY_USE, keyId, ALGORITHM, modulus, exponent);
    }

    /**
     * Crea una respuesta JWKS (JSON Web Key Set) conteniendo esta clave.
     *
     * <p>Genera una respuesta completa del endpoint JWKS tal como la
     * proporcionaría ms-seguridad. Esta respuesta se puede usar para
     * simular la obtención de certificados públicos en tests.</p>
     *
     * @return JwksResponse con una sola clave para testing básico
     */
    public JwksResponse toJwksResponse() {
        return new JwksResponse(List.of(toJwkKey()));
    }

    /**
     * Crea una respuesta JWKS con múltiples claves para testing de rotación.
     *
     * <p>Simula el escenario real donde ms-seguridad mantiene múltiples claves
     * públicas activas durante el proceso de rotación. Esto permite que tokens
     * firmados con claves anteriores sigan siendo válidos durante el período
     * de transición.</p>
     *
     * <p>Caso de uso típico: Testing de rotación de claves donde tokens
     * antiguos y nuevos coexisten temporalmente.</p>
     *
     * @param generators Uno o más generadores de claves para incluir en el JWKS
     * @return JwksResponse conteniendo todas las claves proporcionadas
     */
    public static JwksResponse createMultiKeyJwksResponse(JwtTestKeyGenerator... generators) {
        List<JwkKey> keys = List.of(generators).stream()
            .map(JwtTestKeyGenerator::toJwkKey)
            .toList();
        return new JwksResponse(keys);
    }

    /**
     * Generador preconfigurado para testing básico con Key ID estándar.
     *
     * <p>Crea un generador con el Key ID "test-key-001" para casos de uso
     * simples donde no se requiere gestión manual de identificadores de clave.</p>
     *
     * <p>Ideal para tests unitarios básicos de validación JWT.</p>
     *
     * @return JwtTestKeyGenerator listo para usar con configuración estándar
     */
    public static JwtTestKeyGenerator createDefault() {
        return new JwtTestKeyGenerator("test-key-001");
    }

    /**
     * Generador para testing de rotación de claves con Key ID personalizado.
     *
     * <p>Permite crear múltiples generadores con diferentes Key IDs para
     * simular escenarios de rotación de claves donde coexisten múltiples
     * claves públicas simultáneamente.</p>
     *
     * <p>Ejemplo típico: crear "key-001" y "key-002" para testing de transición.</p>
     *
     * @param keyId Identificador único para esta clave (ej: "prod-key-2024-001")
     * @return JwtTestKeyGenerator con el Key ID especificado
     */
    public static JwtTestKeyGenerator createForRotation(String keyId) {
        return new JwtTestKeyGenerator(keyId);
    }
}