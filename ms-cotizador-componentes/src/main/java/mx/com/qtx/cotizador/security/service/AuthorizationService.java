package mx.com.qtx.cotizador.security.service;

import mx.com.qtx.cotizador.security.dto.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de autorización que utiliza el SessionValidationClient
 * para validar sesiones de usuarios en el ms-cotizador
 */
@Service
@Profile({"default", "docker"})
public class AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final SessionCacheService sessionCacheService;

    public AuthorizationService(SessionCacheService sessionCacheService) {
        this.sessionCacheService = sessionCacheService;
    }

    /**
     * Valida si una sesión está activa y autorizada
     * 
     * @param sessionId ID de la sesión a validar
     * @return true si la sesión está activa y autorizada
     */
    public boolean isSessionAuthorized(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return false;
        }

        try {
            boolean isValid = sessionCacheService.validateSession(sessionId);
            logger.debug("Sesión {} validada: {}", sessionId, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error al validar sesión {}: {}", sessionId, e.getMessage());
            return false; // Fail closed - denegar acceso en caso de error
        }
    }

    /**
     * Obtiene información detallada de una sesión autorizada
     * 
     * @param sessionId ID de la sesión
     * @return Optional con información de la sesión si está autorizada
     */
    public Optional<SessionInfo> getAuthorizedSessionInfo(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return Optional.empty();
        }

        try {
            Optional<SessionInfo> sessionInfo = sessionCacheService.getSessionInfo(sessionId);
            
            if (sessionInfo.isPresent() && sessionInfo.get().isValid()) {
                logger.debug("Información de sesión autorizada obtenida para: {}", sessionId);
                return sessionInfo;
            } else {
                logger.debug("Sesión no autorizada o no encontrada: {}", sessionId);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error al obtener información de sesión {}: {}", sessionId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Valida una sesión y obtiene su información en una sola operación
     * 
     * @param sessionId ID de la sesión
     * @return Optional con información de la sesión si está autorizada
     */
    public Optional<SessionInfo> validateAndGetSessionInfo(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return Optional.empty();
        }

        try {
            // Primero validar la sesión
            if (!sessionCacheService.validateSession(sessionId)) {
                logger.debug("Sesión no válida: {}", sessionId);
                return Optional.empty();
            }

            // Luego obtener información
            Optional<SessionInfo> sessionInfo = sessionCacheService.getSessionInfo(sessionId);
            
            if (sessionInfo.isPresent()) {
                logger.debug("Sesión validada y información obtenida para: {}", sessionId);
                return sessionInfo;
            } else {
                logger.warn("Sesión válida pero sin información disponible: {}", sessionId);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error al validar y obtener información de sesión {}: {}", sessionId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cierra una sesión de usuario
     * 
     * @param sessionId ID de la sesión a cerrar
     * @return true si la sesión fue cerrada exitosamente
     */
    public boolean closeUserSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return false;
        }

        try {
            boolean closed = sessionCacheService.closeSession(sessionId);
            logger.info("Sesión {} cerrada: {}", sessionId, closed);
            return closed;
        } catch (Exception e) {
            logger.error("Error al cerrar sesión {}: {}", sessionId, e.getMessage());
            return false;
        }
    }

    /**
     * Invalida una sesión en el caché local (útil para logout local)
     * 
     * @param sessionId ID de la sesión a invalidar
     */
    public void invalidateLocalSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return;
        }

        try {
            sessionCacheService.invalidateSession(sessionId);
            logger.debug("Sesión invalidada localmente: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error al invalidar sesión local {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Verifica si el servicio de sesiones está disponible
     * 
     * @return true si el servicio está disponible
     */
    public boolean isSessionServiceAvailable() {
        try {
            SessionCacheService.HealthInfo healthInfo = sessionCacheService.getHealthInfo();
            return healthInfo.isServiceAvailable();
        } catch (Exception e) {
            logger.error("Error al verificar disponibilidad del servicio de sesiones: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas del servicio de autorización
     * 
     * @return Estadísticas del caché de sesiones
     */
    public SessionCacheService.CacheStats getAuthorizationStats() {
        try {
            return sessionCacheService.getCacheStats();
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de autorización: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Limpia el caché de sesiones (útil para mantenimiento)
     */
    public void clearSessionCache() {
        try {
            sessionCacheService.clearCache();
            logger.info("Caché de sesiones limpiado");
        } catch (Exception e) {
            logger.error("Error al limpiar caché de sesiones: {}", e.getMessage());
        }
    }
}