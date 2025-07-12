package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.entity.Acceso;
import mx.com.qtx.seguridad.repository.AccesoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final AccesoRepository accesoRepository;

    public SessionService(AccesoRepository accesoRepository) {
        this.accesoRepository = accesoRepository;
    }

    /**
     * Crear una nueva sesión para un usuario
     * 
     * @param usuarioId ID del usuario
     * @param ipAddress Dirección IP del cliente (opcional)
     * @param userAgent User-Agent del cliente (opcional)
     * @return String ID único de la sesión creada
     * @throws RuntimeException si ocurre un error al crear la sesión
     */
    public String createSession(Integer usuarioId, String ipAddress, String userAgent) {
        logger.info("Creando nueva sesión para usuario ID: {}", usuarioId);
        
        try {
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                throw new IllegalArgumentException("Usuario ID es requerido y debe ser mayor a 0");
            }

            // Generar ID único para la sesión
            String idSesion = UUID.randomUUID().toString();
            
            // Crear nueva instancia de Acceso
            Acceso nuevaSession = new Acceso();
            nuevaSession.setIdSesion(idSesion);
            nuevaSession.setUsuarioId(usuarioId);
            nuevaSession.setActivo(true);
            nuevaSession.setFechaInicio(LocalDateTime.now());
            
            // Guardar en base de datos
            Acceso sessionGuardada = accesoRepository.save(nuevaSession);
            
            logger.info("Sesión creada exitosamente - ID: {}, Usuario: {}, IP: {}", 
                       idSesion, usuarioId, ipAddress != null ? ipAddress : "N/A");
            
            return sessionGuardada.getIdSesion();
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al crear sesión para usuario {}: {}", usuarioId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear sesión para usuario {}: {}", usuarioId, e.getMessage(), e);
            throw new RuntimeException("Error al crear sesión: " + e.getMessage(), e);
        }
    }

    /**
     * Cerrar una sesión específica
     * 
     * @param idSesion ID de la sesión a cerrar
     * @return boolean true si se cerró exitosamente, false si no se encontró
     * @throws RuntimeException si ocurre un error al cerrar la sesión
     */
    public boolean closeSession(String idSesion) {
        logger.info("Cerrando sesión con ID: {}", idSesion);
        
        try {
            // Validar parámetros
            if (idSesion == null || idSesion.trim().isEmpty()) {
                throw new IllegalArgumentException("ID de sesión es requerido");
            }

            // Buscar la sesión
            Optional<Acceso> sessionOpt = accesoRepository.findByIdSesion(idSesion);
            
            if (sessionOpt.isEmpty()) {
                logger.warn("Sesión no encontrada para cerrar: {}", idSesion);
                return false;
            }
            
            Acceso session = sessionOpt.get();
            
            // Verificar si ya está cerrada
            if (!session.isActivo()) {
                logger.info("Sesión {} ya estaba cerrada", idSesion);
                return true;
            }
            
            // Cerrar sesión
            session.cerrarSesion();
            accesoRepository.save(session);
            
            logger.info("Sesión cerrada exitosamente - ID: {}, Usuario: {}", 
                       idSesion, session.getUsuarioId());
            
            return true;
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al cerrar sesión {}: {}", idSesion, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al cerrar sesión {}: {}", idSesion, e.getMessage(), e);
            throw new RuntimeException("Error al cerrar sesión: " + e.getMessage(), e);
        }
    }

    /**
     * Cerrar todas las sesiones activas de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return int número de sesiones cerradas
     * @throws RuntimeException si ocurre un error al cerrar las sesiones
     */
    public int closeAllUserSessions(Integer usuarioId) {
        logger.info("Cerrando todas las sesiones activas para usuario ID: {}", usuarioId);
        
        try {
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                throw new IllegalArgumentException("Usuario ID es requerido y debe ser mayor a 0");
            }

            // Obtener todas las sesiones activas del usuario
            List<Acceso> sessionesActivas = accesoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
            
            if (sessionesActivas.isEmpty()) {
                logger.info("No se encontraron sesiones activas para usuario ID: {}", usuarioId);
                return 0;
            }
            
            // Cerrar todas las sesiones
            int sesionesTotales = sessionesActivas.size();
            LocalDateTime ahora = LocalDateTime.now();
            
            for (Acceso session : sessionesActivas) {
                session.setActivo(false);
                session.setFechaFin(ahora);
            }
            
            // Guardar cambios en lote
            accesoRepository.saveAll(sessionesActivas);
            
            logger.info("Se cerraron {} sesiones activas para usuario ID: {}", sesionesTotales, usuarioId);
            
            return sesionesTotales;
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al cerrar sesiones de usuario {}: {}", usuarioId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al cerrar sesiones de usuario {}: {}", usuarioId, e.getMessage(), e);
            throw new RuntimeException("Error al cerrar sesiones del usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar si una sesión está activa
     * 
     * @param idSesion ID de la sesión
     * @return boolean true si la sesión está activa, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean isSessionActive(String idSesion) {
        logger.debug("Verificando si sesión está activa: {}", idSesion);
        
        try {
            // Validar parámetros
            if (idSesion == null || idSesion.trim().isEmpty()) {
                logger.debug("ID de sesión inválido: {}", idSesion);
                return false;
            }

            // Buscar la sesión
            Optional<Acceso> sessionOpt = accesoRepository.findByIdSesion(idSesion);
            
            if (sessionOpt.isEmpty()) {
                logger.debug("Sesión no encontrada: {}", idSesion);
                return false;
            }
            
            Acceso session = sessionOpt.get();
            boolean isActive = session.isSesionVigente();
            
            logger.debug("Sesión {} - Activa: {}", idSesion, isActive);
            return isActive;
            
        } catch (Exception e) {
            logger.error("Error al verificar si sesión está activa {}: {}", idSesion, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verificar si un usuario tiene al menos una sesión activa
     * 
     * @param usuarioId ID del usuario
     * @return boolean true si tiene al menos una sesión activa
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSession(Integer usuarioId) {
        logger.debug("Verificando si usuario {} tiene sesiones activas", usuarioId);
        
        try {
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                logger.debug("Usuario ID inválido: {}", usuarioId);
                return false;
            }

            boolean hasActive = accesoRepository.existsByUsuarioIdAndActivoTrue(usuarioId);
            
            logger.debug("Usuario {} tiene sesiones activas: {}", usuarioId, hasActive);
            return hasActive;
            
        } catch (Exception e) {
            logger.error("Error al verificar sesiones activas de usuario {}: {}", usuarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtener información de una sesión específica
     * 
     * @param idSesion ID de la sesión
     * @return Optional<Acceso> con la información de la sesión
     */
    @Transactional(readOnly = true)
    public Optional<Acceso> getSessionInfo(String idSesion) {
        logger.debug("Obteniendo información de sesión: {}", idSesion);
        
        try {
            // Validar parámetros
            if (idSesion == null || idSesion.trim().isEmpty()) {
                logger.debug("ID de sesión inválido: {}", idSesion);
                return Optional.empty();
            }

            Optional<Acceso> sessionOpt = accesoRepository.findByIdSesion(idSesion);
            
            if (sessionOpt.isPresent()) {
                logger.debug("Información de sesión encontrada: {}", idSesion);
            } else {
                logger.debug("Sesión no encontrada: {}", idSesion);
            }
            
            return sessionOpt;
            
        } catch (Exception e) {
            logger.error("Error al obtener información de sesión {}: {}", idSesion, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Obtener todas las sesiones activas de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> con las sesiones activas del usuario
     */
    @Transactional(readOnly = true)
    public List<Acceso> getActiveUserSessions(Integer usuarioId) {
        logger.debug("Obteniendo sesiones activas de usuario: {}", usuarioId);
        
        try {
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                throw new IllegalArgumentException("Usuario ID es requerido y debe ser mayor a 0");
            }

            List<Acceso> sessionesActivas = accesoRepository.findByUsuarioIdAndActivoTrueOrderByFechaInicioDesc(usuarioId);
            
            logger.debug("Encontradas {} sesiones activas para usuario {}", sessionesActivas.size(), usuarioId);
            
            return sessionesActivas;
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al obtener sesiones activas de usuario {}: {}", usuarioId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al obtener sesiones activas de usuario {}: {}", usuarioId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener sesiones activas: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener historial de sesiones de un usuario (todas, activas e inactivas)
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> con el historial de sesiones ordenado por fecha más reciente
     */
    @Transactional(readOnly = true)
    public List<Acceso> getUserSessionHistory(Integer usuarioId) {
        logger.debug("Obteniendo historial de sesiones de usuario: {}", usuarioId);
        
        try {
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                throw new IllegalArgumentException("Usuario ID es requerido y debe ser mayor a 0");
            }

            List<Acceso> historial = accesoRepository.findByUsuarioIdOrderByFechaInicioDesc(usuarioId);
            
            logger.debug("Encontradas {} sesiones en historial para usuario {}", historial.size(), usuarioId);
            
            return historial;
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al obtener historial de usuario {}: {}", usuarioId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al obtener historial de usuario {}: {}", usuarioId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener historial de sesiones: " + e.getMessage(), e);
        }
    }

    /**
     * Contar sesiones activas de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return long número de sesiones activas
     */
    @Transactional(readOnly = true)
    public long countActiveUserSessions(Integer usuarioId) {
        logger.debug("Contando sesiones activas de usuario: {}", usuarioId);
        
        try {
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                throw new IllegalArgumentException("Usuario ID es requerido y debe ser mayor a 0");
            }

            long count = accesoRepository.countByUsuarioIdAndActivoTrue(usuarioId);
            
            logger.debug("Usuario {} tiene {} sesiones activas", usuarioId, count);
            
            return count;
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al contar sesiones activas de usuario {}: {}", usuarioId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al contar sesiones activas de usuario {}: {}", usuarioId, e.getMessage(), e);
            throw new RuntimeException("Error al contar sesiones activas: " + e.getMessage(), e);
        }
    }

    /**
     * Limpiar sesiones expiradas (que están marcadas como activas pero han expirado)
     * 
     * @return int número de sesiones limpiadas
     */
    public int cleanupExpiredSessions() {
        logger.info("Iniciando limpieza de sesiones expiradas");
        
        try {
            // Obtener sesiones expiradas pero marcadas como activas
            List<Acceso> sessionesExpiradas = accesoRepository.findExpiredActiveSessions();
            
            if (sessionesExpiradas.isEmpty()) {
                logger.info("No se encontraron sesiones expiradas para limpiar");
                return 0;
            }
            
            // Marcar como inactivas
            LocalDateTime ahora = LocalDateTime.now();
            for (Acceso session : sessionesExpiradas) {
                session.setActivo(false);
                // Si no tiene fechaFin, se la asignamos
                if (session.getFechaFin() == null) {
                    session.setFechaFin(ahora);
                }
            }
            
            // Guardar cambios
            accesoRepository.saveAll(sessionesExpiradas);
            
            int sessionesLimpiadas = sessionesExpiradas.size();
            logger.info("Se limpiaron {} sesiones expiradas", sessionesLimpiadas);
            
            return sessionesLimpiadas;
            
        } catch (Exception e) {
            logger.error("Error durante limpieza de sesiones expiradas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al limpiar sesiones expiradas: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener estadísticas del servicio de sesiones
     * 
     * @return java.util.Map con estadísticas del servicio
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getSessionStats() {
        logger.debug("Obteniendo estadísticas del servicio de sesiones");
        
        try {
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            
            // Contar sesiones activas
            long totalActiveSessions = accesoRepository.countByActivoTrue();
            stats.put("totalActiveSessions", totalActiveSessions);
            
            // Contar total de sesiones
            long totalSessions = accesoRepository.count();
            stats.put("totalSessions", totalSessions);
            
            // Obtener sesiones expiradas
            List<Acceso> expiredSessions = accesoRepository.findExpiredActiveSessions();
            stats.put("expiredActiveSessions", expiredSessions.size());
            
            stats.put("serviceStatus", "active");
            stats.put("timestamp", LocalDateTime.now());
            
            logger.debug("Estadísticas generadas: {} sesiones activas, {} sesiones totales", 
                        totalActiveSessions, totalSessions);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas del servicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }
}