package mx.com.qtx.seguridad.controller;

import mx.com.qtx.seguridad.dto.SessionValidationResponse;
import mx.com.qtx.seguridad.dto.SessionCloseResponse;
import mx.com.qtx.seguridad.dto.SessionInfoResponse;
import mx.com.qtx.seguridad.entity.Acceso;
import mx.com.qtx.seguridad.service.SessionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.Optional;

/**
 * Controlador REST para gestión pública de sesiones
 * Proporciona endpoints públicos para validar y cerrar sesiones
 */
@RestController
@RequestMapping("/session")
public class SessionController {

    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Endpoint público para validar si una sesión está activa
     * 
     * @param sessionId ID de la sesión a validar
     * @return SessionValidationResponse con información de validación
     */
    @GetMapping("/validate/{sessionId}")
    public ResponseEntity<SessionValidationResponse> validateSession(
            @PathVariable String sessionId) {
        
        logger.info("Validando sesión: {}", sessionId);
        
        try {
            // Validar parámetros
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("Intento de validación con ID de sesión vacío");
                return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noCache())
                    .body(SessionValidationResponse.invalidId());
            }

            // Validar sesión
            boolean isActive = sessionService.isSessionActive(sessionId);
            
            SessionValidationResponse response;
            if (isActive) {
                response = SessionValidationResponse.valid(sessionId);
                logger.info("Sesión {} validada como activa", sessionId);
            } else {
                response = SessionValidationResponse.invalid(sessionId);
                logger.info("Sesión {} validada como inactiva", sessionId);
            }
            
            return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(response);
                
        } catch (Exception e) {
            logger.error("Error al validar sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .cacheControl(CacheControl.noCache())
                .body(SessionValidationResponse.error(sessionId, "Error interno al validar sesión"));
        }
    }

    /**
     * Endpoint público para cerrar una sesión específica
     * 
     * @param sessionId ID de la sesión a cerrar
     * @return SessionCloseResponse con información del cierre
     */
    @PostMapping("/close/{sessionId}")
    public ResponseEntity<SessionCloseResponse> closeSession(
            @PathVariable String sessionId) {
        
        logger.info("Cerrando sesión: {}", sessionId);
        
        try {
            // Validar parámetros
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("Intento de cerrar sesión con ID vacío");
                return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noCache())
                    .body(SessionCloseResponse.invalidId());
            }

            // Cerrar sesión
            boolean closed = sessionService.closeSession(sessionId);
            
            SessionCloseResponse response;
            HttpStatus status;
            
            if (closed) {
                response = SessionCloseResponse.closed(sessionId);
                status = HttpStatus.OK;
                logger.info("Sesión {} cerrada exitosamente", sessionId);
            } else {
                response = SessionCloseResponse.notFound(sessionId);
                status = HttpStatus.NOT_FOUND;
                logger.warn("Sesión {} no encontrada para cerrar", sessionId);
            }
            
            return ResponseEntity.status(status)
                .cacheControl(CacheControl.noCache())
                .body(response);
                
        } catch (Exception e) {
            logger.error("Error al cerrar sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .cacheControl(CacheControl.noCache())
                .body(SessionCloseResponse.error(sessionId, "Error interno al cerrar sesión"));
        }
    }

    /**
     * Endpoint público para obtener información básica de una sesión
     * 
     * @param sessionId ID de la sesión
     * @return SessionInfoResponse con información básica (sin datos sensibles)
     */
    @GetMapping("/info/{sessionId}")
    public ResponseEntity<SessionInfoResponse> getSessionInfo(
            @PathVariable String sessionId) {
        
        logger.info("Obteniendo información de sesión: {}", sessionId);
        
        try {
            // Validar parámetros
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("Intento de obtener información con ID de sesión vacío");
                return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                    .body(SessionInfoResponse.invalidId());
            }

            // Obtener información de sesión
            Optional<Acceso> sessionOpt = sessionService.getSessionInfo(sessionId);
            
            if (sessionOpt.isPresent()) {
                Acceso session = sessionOpt.get();
                
                SessionInfoResponse response = SessionInfoResponse.found(
                    session.getIdSesion(),
                    session.isActivo(),
                    session.getFechaInicio(),
                    session.getFechaFin()
                );
                
                logger.info("Información de sesión {} obtenida exitosamente", sessionId);
                
                return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                    .body(response);
                    
            } else {
                logger.warn("Sesión {} no encontrada", sessionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                    .body(SessionInfoResponse.notFound(sessionId));
            }
            
        } catch (Exception e) {
            logger.error("Error al obtener información de sesión {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .cacheControl(CacheControl.noCache())
                .body(SessionInfoResponse.error(sessionId, "Error interno al obtener información"));
        }
    }

    /**
     * Endpoint de salud para verificar que el controlador de sesiones está activo
     * 
     * @return Estado del controlador de sesiones
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, Object>> health() {
        logger.debug("Verificando salud del controlador de sesiones");
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", "UP");
        response.put("controller", "SessionController");
        response.put("service", "ms-seguridad");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        // Obtener estadísticas básicas del servicio
        try {
            java.util.Map<String, Object> stats = sessionService.getSessionStats();
            response.put("stats", stats);
        } catch (Exception e) {
            logger.warn("Error al obtener estadísticas de sesiones: {}", e.getMessage());
            response.put("stats", "Error al obtener estadísticas");
        }
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
            .body(response);
    }
}