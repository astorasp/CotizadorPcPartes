package mx.com.qtx.cotizador.security.jwt.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Constructor de tokens JWT para testing con patrón Builder.
 *
 * Esta clase facilita la creación de tokens JWT para diferentes escenarios
 * de testing, permitiendo validar el comportamiento del sistema con tokens
 * válidos, expirados, con claims incorrectos, etc.
 *
 * <h3>Capacidades del builder:</h3>
 * <ul>
 *   <li>Tokens válidos con claims personalizados</li>
 *   <li>Tokens expirados para testing de validación temporal</li>
 *   <li>Tokens con issuer incorrecto</li>
 *   <li>Tokens con diferentes roles y permisos</li>
 *   <li>Tokens con session IDs específicos</li>
 * </ul>
 *
 * <h3>Claims incluidos automáticamente:</h3>
 * <ul>
 *   <li><b>iss</b>: Issuer (por defecto "ms-seguridad")</li>
 *   <li><b>sub</b>: Subject/username (por defecto "test-user")</li>
 *   <li><b>iat</b>: Issued At timestamp</li>
 *   <li><b>exp</b>: Expiration timestamp</li>
 *   <li><b>email</b>: Email del usuario</li>
 *   <li><b>roles</b>: Array de roles asignados</li>
 *   <li><b>sessionId</b>: ID de sesión para tracking</li>
 * </ul>
 *
 * <h3>Ejemplo de uso básico:</h3>
 * <pre>{@code
 * // Token básico válido
 * String token = new JwtTestTokenBuilder()
 *     .withSubject("john.doe")
 *     .withEmail("john@example.com")
 *     .withRoles("ADMIN", "USER")
 *     .build(keyGenerator);
 *
 * // Token expirado para testing de validación
 * String expiredToken = new JwtTestTokenBuilder()
 *     .expired()
 *     .build(keyGenerator);
 * }</pre>
 *
 * <h3>Métodos de conveniencia estáticos:</h3>
 * <pre>{@code
 * // Tokens predefinidos para casos comunes
 * String validToken = JwtTestTokenBuilder.createValidToken(keyGen);
 * String expiredToken = JwtTestTokenBuilder.createExpiredToken(keyGen);
 * String adminToken = JwtTestTokenBuilder.createAdminToken(keyGen);
 * }</pre>
 *
 * @author Sistema Cotizador - Equipo JWT Testing
 * @version 1.0
 * @since 2.0.0
 * @see JwtTestKeyGenerator
 * @see mx.com.qtx.cotizador.security.jwt.JwtBasicFunctionalityTest
 */
public class JwtTestTokenBuilder {

    private static final String DEFAULT_ISSUER = "ms-seguridad";
    private static final String DEFAULT_SUBJECT = "test-user";
    private static final List<String> DEFAULT_ROLES = List.of("ADMIN", "USER");

    private String issuer = DEFAULT_ISSUER;
    private String subject = DEFAULT_SUBJECT;
    private List<String> roles = DEFAULT_ROLES;
    private String email = "test@example.com";
    private String sessionId = "test-session-123";
    private Instant issuedAt = Instant.now();
    private Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);
    private Map<String, Object> additionalClaims = Map.of();

    /**
     * Constructor por defecto con valores predefinidos para testing.
     *
     * <p>Inicializa el builder con valores estándar que funcionan para
     * la mayoría de casos de testing básico:</p>
     * <ul>
     *   <li>Issuer: "ms-seguridad"</li>
     *   <li>Subject: "test-user"</li>
     *   <li>Roles: ["ADMIN", "USER"]</li>
     *   <li>Email: "test@example.com"</li>
     *   <li>Duración: 1 hora</li>
     * </ul>
     */
    public JwtTestTokenBuilder() {
        // Valores por defecto ya establecidos
    }

    /**
     * Establece el issuer del token
     */
    public JwtTestTokenBuilder withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * Establece el subject (username) del token
     */
    public JwtTestTokenBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Establece los roles del usuario
     */
    public JwtTestTokenBuilder withRoles(String... roles) {
        this.roles = List.of(roles);
        return this;
    }

    /**
     * Establece los roles del usuario
     */
    public JwtTestTokenBuilder withRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    /**
     * Establece el email del usuario
     */
    public JwtTestTokenBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Establece el session ID
     */
    public JwtTestTokenBuilder withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Establece la fecha de emisión
     */
    public JwtTestTokenBuilder withIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    /**
     * Establece la fecha de expiración
     */
    public JwtTestTokenBuilder withExpiration(Instant expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Crea un token expirado (expirado hace 1 hora)
     */
    public JwtTestTokenBuilder expired() {
        this.issuedAt = Instant.now().minus(2, ChronoUnit.HOURS);
        this.expiration = Instant.now().minus(1, ChronoUnit.HOURS);
        return this;
    }

    /**
     * Crea un token de larga duración (válido por 24 horas)
     */
    public JwtTestTokenBuilder longLived() {
        this.issuedAt = Instant.now();
        this.expiration = Instant.now().plus(24, ChronoUnit.HOURS);
        return this;
    }

    /**
     * Añade claims adicionales
     */
    public JwtTestTokenBuilder withAdditionalClaims(Map<String, Object> claims) {
        this.additionalClaims = claims;
        return this;
    }

    /**
     * Construye el token JWT firmado con la clave privada proporcionada.
     *
     * <p>Genera un token JWT completo utilizando el algoritmo RS256 (RSA + SHA-256).
     * El token incluye todos los claims configurados y es firmado criptográficamente
     * con la clave privada especificada.</p>
     *
     * <p>El token resultante tiene el formato estándar JWT: header.payload.signature</p>
     *
     * @param privateKey Clave privada RSA para firmar el token
     * @param keyId Identificador de clave que se incluye en el header JWT
     * @return String token JWT completo y firmado, listo para usar
     */
    public String build(PrivateKey privateKey, String keyId) {
        JwtBuilder builder = Jwts.builder()
            .header()
                .keyId(keyId)
                .and()
            .issuer(issuer)
            .subject(subject)
            .claim("email", email)
            .claim("roles", roles)
            .claim("sessionId", sessionId)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiration));

        // Añadir claims adicionales
        additionalClaims.forEach(builder::claim);

        return builder.signWith(privateKey).compact();
    }

    /**
     * Construye el token usando un JwtTestKeyGenerator
     */
    public String build(JwtTestKeyGenerator keyGenerator) {
        return build(keyGenerator.getKeyPair().getPrivate(), keyGenerator.getKeyId());
    }

    // === MÉTODOS ESTÁTICOS PARA CASOS COMUNES ===

    /**
     * Crea un token válido básico
     */
    public static String createValidToken(JwtTestKeyGenerator keyGenerator) {
        return new JwtTestTokenBuilder().build(keyGenerator);
    }

    /**
     * Crea un token expirado
     */
    public static String createExpiredToken(JwtTestKeyGenerator keyGenerator) {
        return new JwtTestTokenBuilder()
            .expired()
            .build(keyGenerator);
    }

    /**
     * Crea un token con issuer incorrecto
     */
    public static String createTokenWithWrongIssuer(JwtTestKeyGenerator keyGenerator) {
        return new JwtTestTokenBuilder()
            .withIssuer("wrong-issuer")
            .build(keyGenerator);
    }

    /**
     * Crea un token para un usuario administrador
     */
    public static String createAdminToken(JwtTestKeyGenerator keyGenerator) {
        return new JwtTestTokenBuilder()
            .withSubject("admin-user")
            .withRoles("ADMIN")
            .withEmail("admin@example.com")
            .build(keyGenerator);
    }

    /**
     * Crea un token para un usuario con roles específicos
     */
    public static String createTokenWithRoles(JwtTestKeyGenerator keyGenerator, String... roles) {
        return new JwtTestTokenBuilder()
            .withRoles(roles)
            .build(keyGenerator);
    }

    /**
     * Crea un token con session ID específico
     */
    public static String createTokenWithSession(JwtTestKeyGenerator keyGenerator, String sessionId) {
        return new JwtTestTokenBuilder()
            .withSessionId(sessionId)
            .build(keyGenerator);
    }
}