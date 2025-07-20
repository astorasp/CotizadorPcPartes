package mx.com.qtx.seguridad.service;

import io.jsonwebtoken.*;
import mx.com.qtx.seguridad.config.RsaKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // Constantes para tipos de token
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    
    // Claims personalizados
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_SESSION_ID = "session_id";
    
    // Issuer del token
    private static final String ISSUER = "ms-seguridad";

    private final RsaKeyProvider rsaKeyProvider;
    private final KeyManagementService keyManagementService;
    
    @Value("${jwt.access-token.expiration:3600000}") // 1 hora por defecto (para tests)
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:7200000}") // 2 horas por defecto
    private long refreshTokenExpiration;

    public JwtService(RsaKeyProvider rsaKeyProvider, KeyManagementService keyManagementService) {
        this.rsaKeyProvider = rsaKeyProvider;
        this.keyManagementService = keyManagementService;
    }

    /**
     * Obtiene el Key ID (kid) de la clave pública actual
     * El kid se genera de forma consistente basado en el contenido de la clave pública
     * 
     * @return String Key ID
     */
    private String getCurrentKeyId() {
        try {
            String publicKeyPem = rsaKeyProvider.getPublicKeyAsPem();
            return UUID.nameUUIDFromBytes(publicKeyPem.getBytes()).toString();
        } catch (Exception e) {
            logger.error("Error al obtener Key ID: {}", e.getMessage());
            return UUID.randomUUID().toString(); // Fallback a UUID aleatorio
        }
    }

    /**
     * Genera un access token JWT con roles incluidos
     * 
     * @param username Nombre de usuario
     * @param userId ID del usuario
     * @param roles Lista de roles asignados al usuario
     * @return String JWT access token
     */
    public String generateAccessToken(String username, Integer userId, List<String> roles) {
        return generateAccessToken(username, userId, roles, null);
    }

    /**
     * Genera un access token JWT con roles incluidos y session ID
     * 
     * @param username Nombre de usuario
     * @param userId ID del usuario
     * @param roles Lista de roles asignados al usuario
     * @param idSesion ID de sesión (puede ser null para compatibilidad)
     * @return String JWT access token
     */
    public String generateAccessToken(String username, Integer userId, List<String> roles, String idSesion) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + accessTokenExpiration);
            
            logger.debug("Configuración JWT - accessTokenExpiration: {} ms", accessTokenExpiration);
            logger.debug("Token times - now: {}, expiration: {}", now, expiration);
            
            RSAPrivateKey privateKey = rsaKeyProvider.getPrivateKey();
            String keyId = getCurrentKeyId();
            
            JwtBuilder builder = Jwts.builder()
                    .setHeaderParam("kid", keyId)
                    .setIssuer(ISSUER)
                    .setSubject(username)
                    .setAudience("ms-seguridad-client")
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .setNotBefore(now)
                    .setId(UUID.randomUUID().toString())
                    .claim(CLAIM_USER_ID, userId)
                    .claim(CLAIM_ROLES, roles)
                    .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
            
            // Agregar session ID si está presente
            if (idSesion != null && !idSesion.trim().isEmpty()) {
                builder.claim(CLAIM_SESSION_ID, idSesion);
                logger.debug("Session ID incluido en access token: {}", idSesion);
            }
            
            String token = builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
            
            logger.debug("Access token generado para usuario: {} con roles: {}, kid: {}", username, roles, keyId);
            return token;
            
        } catch (Exception e) {
            logger.error("Error al generar access token para usuario: {}", username, e);
            throw new RuntimeException("Error al generar access token", e);
        }
    }

    /**
     * Genera un refresh token JWT
     * 
     * @param username Nombre de usuario
     * @param userId ID del usuario
     * @return String JWT refresh token
     */
    public String generateRefreshToken(String username, Integer userId) {
        return generateRefreshToken(username, userId, null);
    }

    /**
     * Genera un refresh token JWT con session ID
     * 
     * @param username Nombre de usuario
     * @param userId ID del usuario
     * @param idSesion ID de sesión (puede ser null para compatibilidad)
     * @return String JWT refresh token
     */
    public String generateRefreshToken(String username, Integer userId, String idSesion) {
        try {
            Date now = new Date();
            logger.debug("Configuración JWT - refreshTokenExpiration: {} ms", refreshTokenExpiration);
            Date expiration = new Date(now.getTime() + refreshTokenExpiration);
            
            RSAPrivateKey privateKey = rsaKeyProvider.getPrivateKey();
            String keyId = getCurrentKeyId();
            
            JwtBuilder builder = Jwts.builder()
                    .setHeaderParam("kid", keyId)
                    .setIssuer(ISSUER)
                    .setSubject(username)
                    .setAudience("ms-seguridad-client")
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .setNotBefore(now)
                    .setId(UUID.randomUUID().toString())
                    .claim(CLAIM_USER_ID, userId)
                    .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
            
            // Agregar session ID si está presente
            if (idSesion != null && !idSesion.trim().isEmpty()) {
                builder.claim(CLAIM_SESSION_ID, idSesion);
                logger.debug("Session ID incluido en refresh token: {}", idSesion);
            }
            
            String token = builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
            
            logger.debug("Refresh token generado para usuario: {}, kid: {}", username, keyId);
            return token;
            
        } catch (Exception e) {
            logger.error("Error al generar refresh token para usuario: {}", username, e);
            throw new RuntimeException("Error al generar refresh token", e);
        }
    }

    /**
     * Valida un token JWT y retorna los claims si es válido
     * 
     * @param token JWT token a validar
     * @return Claims del token si es válido
     * @throws JwtException si el token es inválido
     */
    public Claims validateToken(String token) throws JwtException {
        try {
            RSAPublicKey publicKey = rsaKeyProvider.getPublicKey();
            
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            logger.debug("Token validado exitosamente para usuario: {}", claims.getSubject());
            return claims;
            
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado para usuario: {}", e.getClaims().getSubject());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("Error de seguridad en token JWT: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extrae el nombre de usuario del token
     * 
     * @param token JWT token
     * @return String username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID de usuario del token
     * 
     * @param token JWT token
     * @return Long user ID
     */
    public Integer extractUserId(String token) {
        Claims claims = validateToken(token);
        Object userIdClaim = claims.get(CLAIM_USER_ID);
        
        if (userIdClaim instanceof Integer) {
            return (Integer) userIdClaim;
        } else if (userIdClaim instanceof Long) {
            return ((Long) userIdClaim).intValue();
        } else {
            throw new JwtException("User ID claim inválido en token");
        }
    }

    /**
     * Extrae los roles del token
     * 
     * @param token JWT token
     * @return List<String> roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = validateToken(token);
        Object rolesClaim = claims.get(CLAIM_ROLES);
        
        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Extrae el tipo de token (access o refresh)
     * 
     * @param token JWT token
     * @return String token type
     */
    public String extractTokenType(String token) {
        Claims claims = validateToken(token);
        return (String) claims.get(CLAIM_TOKEN_TYPE);
    }

    /**
     * Extrae el session ID del token
     * 
     * @param token JWT token
     * @return String session ID o null si no existe
     */
    public String extractSessionId(String token) {
        try {
            Claims claims = validateToken(token);
            String sessionId = (String) claims.get(CLAIM_SESSION_ID);
            
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                logger.debug("Session ID extraído del token: {}", sessionId);
                return sessionId;
            } else {
                logger.debug("Token no contiene session ID válido");
                return null;
            }
        } catch (Exception e) {
            logger.debug("Error al extraer session ID del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae la fecha de expiración del token
     * 
     * @param token JWT token
     * @return Date expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Calcula el tiempo restante hasta la expiración del token
     * 
     * @param token JWT token
     * @return long tiempo restante en milisegundos
     */
    public long getTimeToExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            long timeRemaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, timeRemaining);
        } catch (Exception e) {
            logger.debug("Error al calcular tiempo de expiración: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Formatea el tiempo restante en formato hh:mm:ss
     * 
     * @param token JWT token
     * @return String tiempo en formato hh:mm:ss o 00:00:00 si expiró
     */
    public String getFormattedTimeToExpiration(String token) {
        try {
            long timeRemainingMs = getTimeToExpiration(token);
            
            if (timeRemainingMs <= 0) {
                return "00:00:00";
            }
            
            long hours = timeRemainingMs / (1000 * 60 * 60);
            long minutes = (timeRemainingMs % (1000 * 60 * 60)) / (1000 * 60);
            long seconds = (timeRemainingMs % (1000 * 60)) / 1000;
            
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            
        } catch (Exception e) {
            logger.debug("Error al formatear tiempo de expiración: {}", e.getMessage());
            return "00:00:00";
        }
    }

    /**
     * Verifica si el token ha expirado
     * 
     * @param token JWT token
     * @return boolean true si expiró
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.debug("Error al verificar expiración: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Verifica si el token es de tipo access
     * 
     * @param token JWT token
     * @return boolean true si es access token
     */
    public boolean isAccessToken(String token) {
        try {
            return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el token es de tipo refresh
     * 
     * @param token JWT token
     * @return boolean true si es refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            return TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida específicamente un refresh token
     * 
     * @param refreshToken Refresh token a validar
     * @return Claims si es válido
     * @throws JwtException si no es válido o no es refresh token
     */
    public Claims validateRefreshToken(String refreshToken) throws JwtException {
        Claims claims = validateToken(refreshToken);
        
        if (!isRefreshToken(refreshToken)) {
            throw new JwtException("Token no es de tipo refresh");
        }
        
        return claims;
    }

    /**
     * Genera un nuevo access token basado en un refresh token válido
     * 
     * @param refreshToken Refresh token válido
     * @param roles Roles actualizados del usuario
     * @return String nuevo access token
     */
    public String refreshAccessToken(String refreshToken, List<String> roles) {
        try {
            Claims claims = validateRefreshToken(refreshToken);
            String username = claims.getSubject();
            Integer userId = extractUserId(refreshToken);
            String sessionId = extractSessionId(refreshToken);
            
            return generateAccessToken(username, userId, roles, sessionId);
            
        } catch (Exception e) {
            logger.error("Error al renovar access token: {}", e.getMessage());
            throw new RuntimeException("Error al renovar access token", e);
        }
    }

    /**
     * Extrae un claim específico del token usando una función
     * 
     * @param token JWT token
     * @param claimsResolver Función para extraer el claim
     * @param <T> Tipo del claim
     * @return T valor del claim
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = validateToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene información completa del token (para debugging/auditoría)
     * 
     * @param token JWT token
     * @return Map con información del token
     */
    public Map<String, Object> getTokenInfo(String token) {
        try {
            Claims claims = validateToken(token);
            Map<String, Object> info = new HashMap<>();
            
            info.put("subject", claims.getSubject());
            info.put("issuer", claims.getIssuer());
            info.put("audience", claims.getAudience());
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
            info.put("notBefore", claims.getNotBefore());
            info.put("jwtId", claims.getId());
            info.put("userId", extractUserId(token));
            info.put("roles", extractRoles(token));
            info.put("tokenType", extractTokenType(token));
            info.put("sessionId", extractSessionId(token));
            info.put("timeToExpiration", getFormattedTimeToExpiration(token));
            info.put("expired", isTokenExpired(token));
            
            return info;
            
        } catch (Exception e) {
            logger.error("Error al obtener información del token: {}", e.getMessage());
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("valid", false);
            return errorInfo;
        }
    }

    /**
     * Verifica si un token es válido (no expirado y bien formado)
     * 
     * @param token Token a verificar
     * @return boolean true si es válido
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el tiempo restante de un token en segundos
     * 
     * @param token Token a verificar
     * @return long segundos restantes hasta la expiración
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = validateToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            if (expiration.before(now)) {
                return 0; // Token ya expirado
            }
            
            return (expiration.getTime() - now.getTime()) / 1000; // Convertir a segundos
            
        } catch (Exception e) {
            logger.debug("Error al calcular tiempo restante del token: {}", e.getMessage());
            return 0;
        }
    }
}