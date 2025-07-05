package mx.com.qtx.cotizador.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import mx.com.qtx.cotizador.security.client.JwksClient;
import mx.com.qtx.cotizador.security.dto.JwkKey;
import mx.com.qtx.cotizador.security.dto.JwksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Servicio para validación de tokens JWT usando claves JWKS
 * Implementa cache de claves públicas para optimizar rendimiento
 */
@Service
@Profile({"default", "docker"})
public class JwtValidationService {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationService.class);

    private final JwksClient jwksClient;
    private final String expectedIssuer;
    private final ConcurrentMap<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> keyCacheTimestamps = new ConcurrentHashMap<>();
    private final long cacheTimeoutMs;

    public JwtValidationService(
            JwksClient jwksClient,
            @Value("${jwt.expected-issuer:ms-seguridad}") String expectedIssuer,
            @Value("${jwt.cache-timeout-ms:300000}") long cacheTimeoutMs) {
        this.jwksClient = jwksClient;
        this.expectedIssuer = expectedIssuer;
        this.cacheTimeoutMs = cacheTimeoutMs;
        
        logger.info("JwtValidationService inicializado con issuer: {} y cache timeout: {}ms", 
                   expectedIssuer, cacheTimeoutMs);
    }

    /**
     * Valida un token JWT completo
     * 
     * @param token El token JWT a validar
     * @return Claims del token si es válido
     * @throws JwtValidationException si el token no es válido
     */
    public Claims validateToken(String token) throws JwtValidationException {
        try {
            // Obtener el header del token para extraer el key ID
            String keyId = extractKeyIdFromToken(token);
            
            // Obtener la clave pública
            PublicKey publicKey = getPublicKey(keyId);
            
            // Validar el token
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(expectedIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
                    
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("Token expirado", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtValidationException("Token no soportado", e);
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("Token malformado", e);
        } catch (SecurityException e) {
            throw new JwtValidationException("Error de seguridad en token", e);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException("Token vacío o nulo", e);
        } catch (Exception e) {
            throw new JwtValidationException("Error inesperado validando token", e);
        }
    }

    /**
     * Verifica si un token es válido sin parsear los claims
     * 
     * @param token El token JWT a verificar
     * @return true si el token es válido
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtValidationException e) {
            logger.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae información del usuario del token JWT
     * 
     * @param token El token JWT
     * @return UserInfo con datos del usuario
     */
    public UserInfo extractUserInfo(String token) throws JwtValidationException {
        Claims claims = validateToken(token);
        
        String username = claims.getSubject();
        String email = claims.get("email", String.class);
        String roles = claims.get("roles", String.class);
        Date expiration = claims.getExpiration();
        
        return new UserInfo(username, email, roles, expiration);
    }

    /**
     * Obtiene la clave pública por ID desde cache o JWKS
     */
    private PublicKey getPublicKey(String keyId) throws JwtValidationException {
        // Verificar cache
        PublicKey cachedKey = keyCache.get(keyId);
        Long cacheTime = keyCacheTimestamps.get(keyId);
        
        if (cachedKey != null && cacheTime != null && 
            (System.currentTimeMillis() - cacheTime) < cacheTimeoutMs) {
            logger.debug("Usando clave pública desde cache para keyId: {}", keyId);
            return cachedKey;
        }

        // Obtener desde JWKS
        try {
            JwksResponse jwksResponse = jwksClient.fetchJwks();
            JwkKey jwkKey = jwksResponse.findKeyById(keyId);
            
            if (jwkKey == null) {
                // Intentar con la primera clave disponible
                jwkKey = jwksResponse.getFirstKey();
                if (jwkKey == null) {
                    throw new JwtValidationException("No se encontró clave pública para keyId: " + keyId);
                }
                logger.warn("KeyId {} no encontrado, usando primera clave disponible: {}", 
                           keyId, jwkKey.getKeyId());
            }
            
            PublicKey publicKey = buildPublicKey(jwkKey);
            
            // Actualizar cache
            keyCache.put(keyId, publicKey);
            keyCacheTimestamps.put(keyId, System.currentTimeMillis());
            
            logger.debug("Clave pública obtenida y cacheada para keyId: {}", keyId);
            return publicKey;
            
        } catch (Exception e) {
            throw new JwtValidationException("Error obteniendo clave pública para keyId: " + keyId, e);
        }
    }

    /**
     * Construye una PublicKey RSA desde JwkKey
     */
    private PublicKey buildPublicKey(JwkKey jwkKey) throws Exception {
        if (!"RSA".equals(jwkKey.getKeyType())) {
            throw new IllegalArgumentException("Solo se soportan claves RSA");
        }

        // Decodificar modulus y exponent desde Base64URL
        byte[] modulusBytes = Base64.getUrlDecoder().decode(jwkKey.getModulus());
        byte[] exponentBytes = Base64.getUrlDecoder().decode(jwkKey.getExponent());
        
        BigInteger modulus = new BigInteger(1, modulusBytes);
        BigInteger exponent = new BigInteger(1, exponentBytes);
        
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        
        return factory.generatePublic(spec);
    }

    /**
     * Extrae el Key ID del header del token JWT
     */
    private String extractKeyIdFromToken(String token) {
        try {
            // Separar header
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Token JWT malformado");
            }
            
            // Decodificar header
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            
            // Extraer kid (key ID) del header
            // Implementación simple - en producción usar un parser JSON
            if (headerJson.contains("\"kid\"")) {
                int kidStart = headerJson.indexOf("\"kid\":\"") + 7;
                int kidEnd = headerJson.indexOf("\"", kidStart);
                return headerJson.substring(kidStart, kidEnd);
            }
            
            return null; // No hay kid especificado
            
        } catch (Exception e) {
            logger.warn("Error extrayendo keyId del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Limpia el cache de claves públicas
     */
    public void clearCache() {
        keyCache.clear();
        keyCacheTimestamps.clear();
        logger.info("Cache de claves públicas limpiado");
    }

    /**
     * Obtiene estadísticas del cache
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            keyCache.size(),
            keyCacheTimestamps.size(),
            cacheTimeoutMs
        );
    }

    /**
     * Información del usuario extraída del token
     */
    public static class UserInfo {
        private final String username;
        private final String email;
        private final String roles;
        private final Date expiration;

        public UserInfo(String username, String email, String roles, Date expiration) {
            this.username = username;
            this.email = email;
            this.roles = roles;
            this.expiration = expiration;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getRoles() {
            return roles;
        }

        public Date getExpiration() {
            return expiration;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", roles='" + roles + '\'' +
                    ", expiration=" + expiration +
                    '}';
        }
    }

    /**
     * Estadísticas del cache de claves
     */
    public static class CacheStats {
        private final int keyCount;
        private final int timestampCount;
        private final long timeoutMs;

        public CacheStats(int keyCount, int timestampCount, long timeoutMs) {
            this.keyCount = keyCount;
            this.timestampCount = timestampCount;
            this.timeoutMs = timeoutMs;
        }

        public int getKeyCount() {
            return keyCount;
        }

        public int getTimestampCount() {
            return timestampCount;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        @Override
        public String toString() {
            return "CacheStats{" +
                    "keyCount=" + keyCount +
                    ", timestampCount=" + timestampCount +
                    ", timeoutMs=" + timeoutMs +
                    '}';
        }
    }

    /**
     * Excepción específica para errores de validación JWT
     */
    public static class JwtValidationException extends Exception {
        public JwtValidationException(String message) {
            super(message);
        }

        public JwtValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}