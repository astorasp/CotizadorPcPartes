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
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import mx.com.qtx.cotizador.security.service.SessionCacheService;

/**
 * Servicio para validación de tokens JWT usando claves JWKS
 * Implementa cache de claves públicas para optimizar rendimiento
 */
@Service
@Profile({"default", "docker"})
public class JwtValidationService {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationService.class);

    private final JwksClient jwksClient;
    private final SessionCacheService sessionCacheService;
    private final String expectedIssuer;
    private final ConcurrentMap<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> keyCacheTimestamps = new ConcurrentHashMap<>();
    private final long cacheTimeoutMs;
    
    // Configuración de rotación reactiva
    private final boolean keyRotationEnabled;
    private final int securityAlertThreshold;
    private final boolean logRotationEvents;
    private final boolean cleanupOldKeys;
    
    // Configuración de validación de sesiones
    private final boolean sessionValidationEnabled;
    
    // Contador de intentos con KIDs inválidos
    private final AtomicInteger invalidKidAttempts = new AtomicInteger(0);

    public JwtValidationService(
            JwksClient jwksClient,
            SessionCacheService sessionCacheService,
            @Value("${jwt.expected-issuer:ms-seguridad}") String expectedIssuer,
            @Value("${jwt.cache-timeout-ms:300000}") long cacheTimeoutMs,
            @Value("${jwt.key-rotation.enabled:true}") boolean keyRotationEnabled,
            @Value("${jwt.key-rotation.security-alert-threshold:3}") int securityAlertThreshold,
            @Value("${jwt.key-rotation.log-rotation-events:true}") boolean logRotationEvents,
            @Value("${jwt.key-rotation.cleanup-old-keys:true}") boolean cleanupOldKeys,
            @Value("${jwt.session-validation.enabled:true}") boolean sessionValidationEnabled) {
        this.jwksClient = jwksClient;
        this.sessionCacheService = sessionCacheService;
        this.expectedIssuer = expectedIssuer;
        this.cacheTimeoutMs = cacheTimeoutMs;
        this.keyRotationEnabled = keyRotationEnabled;
        this.securityAlertThreshold = securityAlertThreshold;
        this.logRotationEvents = logRotationEvents;
        this.cleanupOldKeys = cleanupOldKeys;
        this.sessionValidationEnabled = sessionValidationEnabled;
        
        logger.info("JwtValidationService inicializado - Issuer: {}, Cache timeout: {}ms, " +
                   "Rotación reactiva: {}, Alert threshold: {}, Log events: {}, Cleanup: {}, " +
                   "Validación sesiones: {}", 
                   expectedIssuer, cacheTimeoutMs, keyRotationEnabled, securityAlertThreshold, 
                   logRotationEvents, cleanupOldKeys, sessionValidationEnabled);
    }

    /**
     * Valida un token JWT completo incluyendo validación de sesión
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
            
            // Validar estructura y firma del token
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(expectedIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Validar sesión si está habilitada
            if (sessionValidationEnabled) {
                validateTokenSession(claims);
            }
            
            return claims;
                    
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
     * Valida la sesión asociada al token
     * 
     * @param claims Claims del token ya validado
     * @throws JwtValidationException si la sesión no es válida
     */
    private void validateTokenSession(Claims claims) throws JwtValidationException {
        try {
            // Extraer sessionId del token
            String sessionId = claims.get("session_id", String.class);
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                // Token sin sesión - permitir por compatibilidad hacia atrás
                logger.debug("Token sin session_id - permitido por compatibilidad");
                return;
            }
            
            // Validar sesión usando SessionCacheService
            boolean isSessionActive = sessionCacheService.validateSession(sessionId);
            
            if (!isSessionActive) {
                logger.warn("Sesión inválida o expirada: {}", sessionId);
                throw new JwtValidationException("Sesión inválida o expirada");
            }
            
            logger.debug("Sesión válida: {}", sessionId);
            
        } catch (JwtValidationException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            logger.error("Error validando sesión del token", e);
            throw new JwtValidationException("Error validando sesión", e);
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
        List<String> roles = claims.get("roles", List.class);
        Date expiration = claims.getExpiration();
        
        return new UserInfo(username, email, roles, expiration);
    }

    /**
     * Obtiene la clave pública por ID desde cache o JWKS con rotación reactiva
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

        // Si el KID no está en cache o expiró, verificar si es diferente al actual
        boolean isNewKeyId = !keyCache.containsKey(keyId);
        
        if (isNewKeyId && !keyCache.isEmpty() && keyRotationEnabled) {
            if (logRotationEvents) {
                logger.info("🔄 Detectado nuevo KID: {}. Iniciando rotación reactiva de llaves", keyId);
            }
        }

        // Obtener desde JWKS
        try {
            JwksResponse jwksResponse = jwksClient.fetchJwks();
            JwkKey jwkKey = jwksResponse.findKeyById(keyId);
            
            if (jwkKey == null) {
                // KID del token no existe en JWKS - ALERTA DE SEGURIDAD
                int currentAttempts = invalidKidAttempts.incrementAndGet();
                
                logger.error("🚨 SECURITY ALERT: Token contiene KID '{}' que NO existe en JWKS del ms-seguridad. " +
                           "Intento #{} - Posible vulneración de seguridad!", keyId, currentAttempts);
                           
                // Listar KIDs disponibles para debugging
                List<String> availableKids = jwksResponse.getKeys().stream()
                    .map(JwkKey::getKeyId)
                    .toList();
                logger.error("KIDs disponibles en JWKS: {}", availableKids);
                
                // Alerta crítica si se supera el threshold
                if (currentAttempts >= securityAlertThreshold) {
                    logger.error("🚨🚨 CRITICAL SECURITY ALERT: {} intentos consecutivos con KIDs inválidos detectados! " +
                               "Posible ataque en curso. Revisar logs de seguridad inmediatamente.", currentAttempts);
                    
                    // Reset counter después de alerta crítica
                    invalidKidAttempts.set(0);
                }
                
                throw new JwtValidationException("KID del token '" + keyId + "' no encontrado en JWKS. Posible token malicioso.");
            }
            
            // Reset counter en caso de KID válido
            invalidKidAttempts.set(0);
            
            // KID encontrado - proceder con rotación
            PublicKey publicKey = buildPublicKey(jwkKey);
            
            if (isNewKeyId && !keyCache.isEmpty() && keyRotationEnabled) {
                // Rotación exitosa
                if (logRotationEvents) {
                    logger.info("✅ Rotación de llave exitosa. Reemplazando llave actual con nueva llave para KID: {}", keyId);
                }
                
                // Limpiar cache anterior si está habilitado
                if (cleanupOldKeys) {
                    String oldKid = keyCache.keySet().iterator().next(); // Obtener primer KID anterior
                    if (!oldKid.equals(keyId)) {
                        keyCache.remove(oldKid);
                        keyCacheTimestamps.remove(oldKid);
                        if (logRotationEvents) {
                            logger.info("🗑️ Llave anterior removida del cache. KID anterior: {}, KID nuevo: {}", oldKid, keyId);
                        }
                    }
                }
            }
            
            // Actualizar cache con nueva llave
            keyCache.put(keyId, publicKey);
            keyCacheTimestamps.put(keyId, System.currentTimeMillis());
            
            logger.debug("Clave pública obtenida y cacheada para keyId: {}", keyId);
            return publicKey;
            
        } catch (JwtValidationException e) {
            // Re-lanzar excepciones de validación sin modificar
            throw e;
        } catch (Exception e) {
            logger.error("Error obteniendo clave pública para keyId: {}", keyId, e);
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
     * Fuerza la rotación de llave para un KID específico
     * Útil para testing o rotación manual
     */
    public boolean forceKeyRotation(String newKeyId) {
        try {
            logger.info("🔄 Forzando rotación de llave para KID: {}", newKeyId);
            
            // Remover de cache para forzar recarga
            keyCache.remove(newKeyId);
            keyCacheTimestamps.remove(newKeyId);
            
            // Intentar obtener la nueva llave
            JwksResponse jwksResponse = jwksClient.fetchJwks();
            JwkKey jwkKey = jwksResponse.findKeyById(newKeyId);
            
            if (jwkKey == null) {
                logger.error("❌ Rotación fallida: KID '{}' no encontrado en JWKS", newKeyId);
                return false;
            }
            
            // Construir y cachear nueva llave
            PublicKey publicKey = buildPublicKey(jwkKey);
            keyCache.put(newKeyId, publicKey);
            keyCacheTimestamps.put(newKeyId, System.currentTimeMillis());
            
            logger.info("✅ Rotación manual exitosa para KID: {}", newKeyId);
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error en rotación manual para KID: {}", newKeyId, e);
            return false;
        }
    }

    /**
     * Obtiene información detallada del estado actual de llaves
     */
    public KeyRotationStatus getKeyRotationStatus() {
        return new KeyRotationStatus(
            keyCache.keySet(),
            keyCacheTimestamps,
            cacheTimeoutMs,
            System.currentTimeMillis()
        );
    }

    /**
     * Obtiene configuración y estadísticas de rotación reactiva
     */
    public KeyRotationConfig getKeyRotationConfig() {
        return new KeyRotationConfig(
            keyRotationEnabled,
            securityAlertThreshold,
            logRotationEvents,
            cleanupOldKeys,
            invalidKidAttempts.get()
        );
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
        private final List<String> roles;
        private final Date expiration;

        public UserInfo(String username, String email, List<String> roles, Date expiration) {
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

        public List<String> getRoles() {
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
                    ", roles=" + roles +
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
     * Estado de rotación de llaves JWT
     */
    public static class KeyRotationStatus {
        private final Set<String> cachedKeyIds;
        private final Map<String, Long> keyTimestamps;
        private final long cacheTimeoutMs;
        private final long currentTime;

        public KeyRotationStatus(Set<String> cachedKeyIds, Map<String, Long> keyTimestamps, 
                               long cacheTimeoutMs, long currentTime) {
            this.cachedKeyIds = Set.copyOf(cachedKeyIds);
            this.keyTimestamps = Map.copyOf(keyTimestamps);
            this.cacheTimeoutMs = cacheTimeoutMs;
            this.currentTime = currentTime;
        }

        public Set<String> getCachedKeyIds() {
            return cachedKeyIds;
        }

        public Map<String, Long> getKeyTimestamps() {
            return keyTimestamps;
        }

        public boolean isKeyExpired(String keyId) {
            Long timestamp = keyTimestamps.get(keyId);
            return timestamp == null || (currentTime - timestamp) >= cacheTimeoutMs;
        }

        public long getKeyAgeMs(String keyId) {
            Long timestamp = keyTimestamps.get(keyId);
            return timestamp == null ? -1 : currentTime - timestamp;
        }

        public String getCurrentActiveKeyId() {
            return cachedKeyIds.isEmpty() ? null : cachedKeyIds.iterator().next();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("KeyRotationStatus{\n");
            sb.append("  cachedKeys=").append(cachedKeyIds.size()).append("\n");
            
            for (String keyId : cachedKeyIds) {
                long ageMs = getKeyAgeMs(keyId);
                boolean expired = isKeyExpired(keyId);
                sb.append("  - KID: ").append(keyId)
                  .append(", age: ").append(ageMs).append("ms")
                  .append(", expired: ").append(expired).append("\n");
            }
            
            sb.append("  cacheTimeout: ").append(cacheTimeoutMs).append("ms\n");
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * Configuración y estadísticas de rotación reactiva
     */
    public static class KeyRotationConfig {
        private final boolean enabled;
        private final int securityAlertThreshold;
        private final boolean logRotationEvents;
        private final boolean cleanupOldKeys;
        private final int currentInvalidAttempts;

        public KeyRotationConfig(boolean enabled, int securityAlertThreshold, 
                               boolean logRotationEvents, boolean cleanupOldKeys, 
                               int currentInvalidAttempts) {
            this.enabled = enabled;
            this.securityAlertThreshold = securityAlertThreshold;
            this.logRotationEvents = logRotationEvents;
            this.cleanupOldKeys = cleanupOldKeys;
            this.currentInvalidAttempts = currentInvalidAttempts;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getSecurityAlertThreshold() {
            return securityAlertThreshold;
        }

        public boolean isLogRotationEvents() {
            return logRotationEvents;
        }

        public boolean isCleanupOldKeys() {
            return cleanupOldKeys;
        }

        public int getCurrentInvalidAttempts() {
            return currentInvalidAttempts;
        }

        public boolean isNearSecurityThreshold() {
            return currentInvalidAttempts >= (securityAlertThreshold - 1);
        }

        @Override
        public String toString() {
            return "KeyRotationConfig{" +
                    "enabled=" + enabled +
                    ", securityAlertThreshold=" + securityAlertThreshold +
                    ", logRotationEvents=" + logRotationEvents +
                    ", cleanupOldKeys=" + cleanupOldKeys +
                    ", currentInvalidAttempts=" + currentInvalidAttempts +
                    ", nearThreshold=" + isNearSecurityThreshold() +
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