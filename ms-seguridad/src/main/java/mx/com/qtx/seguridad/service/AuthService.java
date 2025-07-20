package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.dto.TokenResponse;
import mx.com.qtx.seguridad.entity.RolAsignado;
import mx.com.qtx.seguridad.entity.Usuario;
import mx.com.qtx.seguridad.repository.UsuarioRepository;
import mx.com.qtx.seguridad.repository.RolAsignadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final long accessTokenExpiration;    
    private final UsuarioRepository usuarioRepository;
    private final RolAsignadoRepository rolAsignadoRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public AuthService(
            UsuarioRepository usuarioRepository,
            RolAsignadoRepository rolAsignadoRepository,
            JwtService jwtService,
            SessionService sessionService,
            @Value("${jwt.access-token.expiration:3600000}") long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
        this.usuarioRepository = usuarioRepository;
        this.rolAsignadoRepository = rolAsignadoRepository;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.passwordEncoder = new BCryptPasswordEncoder(12); // Strength 12 para mayor seguridad
    }

    /**
     * Autentica un usuario con usuario y contraseña
     * 
     * @param username Nombre de usuario
     * @param password Contraseña en texto plano
     * @return TokenResponse con tokens y información del usuario si la autenticación es exitosa
     * @throws RuntimeException si la autenticación falla
     */
    public TokenResponse authenticate(String username, String password) {
        return authenticate(username, password, null);
    }

    /**
     * Autentica un usuario con usuario y contraseña, incluyendo información de la sesión
     * 
     * @param username Nombre de usuario
     * @param password Contraseña en texto plano
     * @param request HttpServletRequest para obtener IP y User-Agent (opcional)
     * @return TokenResponse con tokens y información del usuario si la autenticación es exitosa
     * @throws RuntimeException si la autenticación falla
     */
    public TokenResponse authenticate(String username, String password, HttpServletRequest request) {
        logger.info("Intento de autenticación para usuario: {}", username);
        
        try {
            // Buscar usuario activo
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuarioAndActivo(username, true);
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario no encontrado o inactivo: {}", username);
                throw new RuntimeException("Credenciales inválidas");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar contraseña
            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                logger.warn("Contraseña incorrecta para usuario: {}", username);
                throw new RuntimeException("Credenciales inválidas");
            }
            
            // Verificar si el usuario ya tiene una sesión activa
            if (sessionService.hasActiveSession(usuario.getId())) {
                logger.warn("Usuario {} ya tiene una sesión activa", username);
                throw new RuntimeException("Ya existe una sesión activa para este usuario");
            }
            
            // Obtener roles asignados activos
            List<String> roles = getRolesAsignados(usuario.getId());
            
            if (roles.isEmpty()) {
                logger.warn("Usuario {} no tiene roles asignados", username);
                throw new RuntimeException("Usuario sin roles asignados");
            }
            
            // Obtener información de la sesión
            String ipAddress = getClientIpAddress(request);
            String userAgent = getUserAgent(request);
            
            // Crear nueva sesión
            String idSesion = sessionService.createSession(usuario.getId(), ipAddress, userAgent);
            logger.info("Sesión creada para usuario: {} con ID: {}", username, idSesion);
            
            // Generar tokens JWT con session ID
            String accessToken = jwtService.generateAccessToken(username, usuario.getId(), roles, idSesion);
            String refreshToken = jwtService.generateRefreshToken(username, usuario.getId(), idSesion);
            
            // Preparar respuesta
            TokenResponse authResponse = new TokenResponse();
            authResponse.setAccessToken(accessToken);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setTokenType("Bearer");
            authResponse.setExpiresIn(accessTokenExpiration); // 5 minutos en segundos
            
            logger.info("Autenticación exitosa para usuario: {} con roles: {}, sesión: {}", username, roles, idSesion);
            return authResponse;
            
        } catch (RuntimeException e) {
            logger.error("Error de autenticación para usuario {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado durante autenticación para usuario: {}", username, e);
            throw new RuntimeException("Error interno de autenticación", e);
        }
    }

    /**
     * Obtiene los roles asignados y activos de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return List<String> nombres de los roles
     */
    public List<String> getRolesAsignados(Integer usuarioId) {
        try {
            List<RolAsignado> asignaciones = rolAsignadoRepository
                    .findActiveAssignmentsByUsuarioWithRoleDetails(usuarioId);
            
            return asignaciones.stream()
                    .filter(ra -> ra.isActivo() && ra.getRol().isActivo())
                    .map(ra -> ra.getRol().getNombre())
                    .distinct()
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error al obtener roles para usuario ID: {}", usuarioId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Renueva un access token usando un refresh token válido
     * 
     * @param refreshToken Refresh token válido
     * @return TokenResponse con nuevo access token
     */
    public TokenResponse refreshToken(String refreshToken) {
        return refreshToken(refreshToken, null);
    }
    
    /**
     * Renueva un access token usando un refresh token válido
     * 
     * @param refreshToken Refresh token válido
     * @param request HttpServletRequest para obtener IP y User-Agent (opcional)
     * @return TokenResponse con nuevo access token
     */
    public TokenResponse refreshToken(String refreshToken, HttpServletRequest request) {
        logger.debug("Solicitud de renovación de token");
        
        try {
            // Validar refresh token
            String username = jwtService.extractUsername(refreshToken);
            Integer userId = jwtService.extractUserId(refreshToken);
            Date expiration = jwtService.extractExpiration(refreshToken);

            if (!jwtService.isRefreshToken(refreshToken)) {
                logger.warn("Token no es de tipo refresh para usuario: {}", username);
                throw new RuntimeException("Token no es de tipo refresh");
            }

            // Verificar que el refresh token es válido
            if (!jwtService.isTokenValid(refreshToken)) {
                sessionService.closeAllUserSessions(userId);
                logger.warn("Refresh token no es válido o ha expirado: {}", username);
                throw new RuntimeException("Refresh token no es válido o ha expirado");
            }
            
            // Verificar que el usuario sigue activo
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuarioAndActivo(username, true);
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario {} ya no está activo durante renovación", username);
                throw new RuntimeException("Usuario no activo");
            }
            
            // Obtener roles actualizados
            List<String> roles = getRolesAsignados(userId);
            
            if (roles.isEmpty()) {
                logger.warn("Usuario {} ya no tiene roles durante renovación", username);
                throw new RuntimeException("Usuario sin roles asignados");
            }
            
            // Cerrar sesión anterior si existe
            if(userId != null)
            {
                logger.info("Cerrando sesiones activas para usuario: {}", username);
                sessionService.closeAllUserSessions(userId);
            }

            // Verificar si el refresh token está próximo a expirar o ya expiró
            if (expiration != null) {
                Date now = new Date();
                if (expiration.before(now)) {
                    logger.warn("El refresh token ha expirado para usuario: {}", username);
                    throw new RuntimeException("El refresh token ha expirado completamente");
                }
                
                // VERIFICACIÓN CRÍTICA: ¿El próximo access token expiraría después del refresh token?
                Date newAccessTokenExpiration = new Date(now.getTime() + accessTokenExpiration);
                if (newAccessTokenExpiration.after(expiration)) {
                    long refreshTimeRemaining = expiration.getTime() - now.getTime();
                    logger.warn("No se puede renovar más - el nuevo access token ({}) expiraría después del refresh token ({}) para usuario: {}", 
                               newAccessTokenExpiration, expiration, username);
                    logger.warn("Tiempo restante del refresh token: {}ms, duración del access token: {}ms", 
                               refreshTimeRemaining, accessTokenExpiration);
                    throw new RuntimeException("No es posible renovar más la sesión - tiempo insuficiente en refresh token");
                }
                
                // Log informativo sobre tiempo restante
                long timeToExpiration = expiration.getTime() - now.getTime();
                if (timeToExpiration < 120000) { // Menos de 2 minutos
                    logger.info("Refresh token expirará en {}ms para usuario: {}", timeToExpiration, username);
                }
            }

            // Crear nueva sesión para el nuevo access token
            String ipAddress = getClientIpAddress(request);
            String userAgent = getUserAgent(request);
            String idSesionNueva = sessionService.createSession(userId, ipAddress, userAgent);
            logger.info("Nueva sesión creada durante renovación: {} para usuario: {}", idSesionNueva, username);
            
            // Generar nuevo access token con la nueva sesión
            String newAccessToken = jwtService.generateAccessToken(username, userId, roles, idSesionNueva);
            
            TokenResponse response = new TokenResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(null);
            response.setTokenType("Bearer");
            response.setExpiresIn(accessTokenExpiration);
            
            logger.debug("Token renovado exitosamente para usuario: {}, sesión nueva: {}", 
                username, idSesionNueva);
            return response;
            
        } catch (Exception e) {
            logger.error("Error al renovar token: {}", e.getMessage());
            throw new RuntimeException("Error al renovar token: " + e.getMessage(), e);
        }
    }

    /**
     * Logout del usuario cerrando la sesión activa
     * 
     * @param accessToken Access token del usuario
     * @param refreshToken Refresh token del usuario (opcional)
     * @return Map con resultado de la operación
     */
    public Map<String, Object> logout(String accessToken, String refreshToken) {
        try {
            String username = jwtService.extractUsername(accessToken);
            String idSesion = jwtService.extractSessionId(accessToken);
            logger.info("Logout solicitado para usuario: {}, sesión: {}", username, idSesion);
            
            Map<String, Object> response = new HashMap<>();
            
            // Cerrar sesión si existe
            if (idSesion != null) {
                boolean sessionClosed = sessionService.closeSession(idSesion);
                if (sessionClosed) {
                    logger.info("Sesión {} cerrada exitosamente para usuario: {}", idSesion, username);
                    response.put("sessionClosed", true);
                    response.put("sessionId", idSesion);
                } else {
                    logger.warn("No se pudo cerrar la sesión {} para usuario: {}", idSesion, username);
                    response.put("sessionClosed", false);
                }
            } else {
                logger.debug("Token no contiene ID de sesión para usuario: {}", username);
                response.put("sessionClosed", false);
                response.put("note", "Token no contiene información de sesión");
            }
            
            response.put("success", true);
            response.put("message", "Logout exitoso");
            response.put("loggedOutAt", new Date());
            
            logger.info("Logout exitoso para usuario: {}", username);
            return response;
            
        } catch (Exception e) {
            logger.error("Error durante logout: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error durante logout: " + e.getMessage());
            
            return response;
        }
    }


    /**
     * Valida un token
     * 
     * @param token Token a validar
     * @return boolean true si es válido
     */
    public boolean isTokenValid(String token) {
        try {
            jwtService.validateToken(token);
            return !jwtService.isTokenExpired(token);
            
        } catch (Exception e) {
            logger.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información del usuario desde un token válido
     * 
     * @param token Access token válido
     * @return Map con información del usuario
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        try {
            if (!isTokenValid(token)) {
                throw new RuntimeException("Token inválido o expirado");
            }
            
            String username = jwtService.extractUsername(token);
            Integer userId = jwtService.extractUserId(token);
            List<String> roles = jwtService.extractRoles(token);
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);
            if (usuarioOpt.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            return createUserInfo(usuarioOpt.get(), roles);
            
        } catch (Exception e) {
            logger.error("Error al obtener información de usuario desde token: {}", e.getMessage());
            throw new RuntimeException("Error al obtener información de usuario", e);
        }
    }

    // Métodos de utilidad privados

    private Map<String, Object> createUserInfo(Usuario usuario, List<String> roles) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", usuario.getId());
        userInfo.put("username", usuario.getUsuario());
        userInfo.put("active", usuario.getActivo());
        userInfo.put("roles", roles);
        userInfo.put("createdAt", usuario.getFechaCreacion());
        userInfo.put("lastModified", usuario.getFechaModificacion());
        
        return userInfo;
    }


    /**
     * Valida si una sesión está activa
     * 
     * @param idSesion ID de la sesión a validar
     * @return boolean true si la sesión está activa
     */
    public boolean validateSession(String idSesion) {
        try {
            if (idSesion == null || idSesion.trim().isEmpty()) {
                logger.debug("ID de sesión inválido o vacío");
                return false;
            }
            
            boolean isActive = sessionService.isSessionActive(idSesion);
            logger.debug("Validación de sesión {}: {}", idSesion, isActive ? "activa" : "inactiva");
            
            return isActive;
            
        } catch (Exception e) {
            logger.error("Error al validar sesión {}: {}", idSesion, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas del servicio de autenticación
     * 
     * @return Map con estadísticas
     */
    public Map<String, Object> getAuthStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("serviceStatus", "active");
        
        // Agregar estadísticas de sesiones
        try {
            Map<String, Object> sessionStats = sessionService.getSessionStats();
            stats.put("sessionStats", sessionStats);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de sesiones: {}", e.getMessage());
            stats.put("sessionStats", Map.of("error", "No disponible"));
        }
        
        return stats;
    }

    // Métodos de utilidad privados para obtener información de la request
    
    /**
     * Obtiene la dirección IP del cliente desde la request
     * 
     * @param request HttpServletRequest
     * @return String IP address o null si no está disponible
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Obtiene el User-Agent del cliente desde la request
     * 
     * @param request HttpServletRequest
     * @return String User-Agent o null si no está disponible
     */
    private String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        return request.getHeader("User-Agent");
    }
}