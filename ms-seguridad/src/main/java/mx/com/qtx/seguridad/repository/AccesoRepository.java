package mx.com.qtx.seguridad.repository;

import mx.com.qtx.seguridad.entity.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccesoRepository extends JpaRepository<Acceso, Long> {

    /**
     * Buscar acceso por ID de sesión
     * 
     * @param idSesion ID único de la sesión
     * @return Optional<Acceso> con el acceso encontrado
     */
    Optional<Acceso> findByIdSesion(String idSesion);

    /**
     * Buscar accesos activos por usuario ID
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> con los accesos activos del usuario
     */
    List<Acceso> findByUsuarioIdAndActivoTrue(Integer usuarioId);

    /**
     * Verificar si existe una sesión activa para un usuario
     * 
     * @param usuarioId ID del usuario
     * @return boolean true si existe al menos una sesión activa
     */
    boolean existsByUsuarioIdAndActivoTrue(Integer usuarioId);

    /**
     * Obtener todos los accesos activos
     * 
     * @return List<Acceso> con todos los accesos activos
     */
    List<Acceso> findByActivoTrue();

    /**
     * Obtener accesos de un usuario ordenados por fecha de inicio descendente
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> ordenados por fecha de inicio más reciente primero
     */
    List<Acceso> findByUsuarioIdOrderByFechaInicioDesc(Integer usuarioId);

    /**
     * Obtener accesos activos de un usuario ordenados por fecha de inicio descendente
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> accesos activos ordenados por fecha más reciente
     */
    List<Acceso> findByUsuarioIdAndActivoTrueOrderByFechaInicioDesc(Integer usuarioId);

    /**
     * Buscar accesos por usuario ID (todos los estados)
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> con todos los accesos del usuario
     */
    List<Acceso> findByUsuarioId(Integer usuarioId);

    /**
     * Contar sesiones activas por usuario
     * 
     * @param usuarioId ID del usuario
     * @return long número de sesiones activas
     */
    long countByUsuarioIdAndActivoTrue(Integer usuarioId);

    /**
     * Obtener la sesión activa más reciente de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return Optional<Acceso> con la sesión activa más reciente
     */
    @Query("SELECT a FROM Acceso a WHERE a.usuarioId = :usuarioId " +
           "AND a.activo = true " +
           "ORDER BY a.fechaInicio DESC")
    Optional<Acceso> findMostRecentActiveSession(@Param("usuarioId") Integer usuarioId);

    /**
     * Obtener sesiones activas con información del usuario
     * 
     * @return List<Acceso> con sesiones activas y datos del usuario cargados
     */
    @Query("SELECT a FROM Acceso a " +
           "JOIN FETCH a.usuario u " +
           "WHERE a.activo = true " +
           "ORDER BY a.fechaInicio DESC")
    List<Acceso> findActiveSessionsWithUserInfo();

    /**
     * Obtener sesiones activas de un usuario específico con información del usuario
     * 
     * @param usuarioId ID del usuario
     * @return List<Acceso> con sesiones activas y datos del usuario cargados
     */
    @Query("SELECT a FROM Acceso a " +
           "JOIN FETCH a.usuario u " +
           "WHERE a.usuarioId = :usuarioId " +
           "AND a.activo = true " +
           "ORDER BY a.fechaInicio DESC")
    List<Acceso> findActiveSessionsWithUserInfoByUserId(@Param("usuarioId") Integer usuarioId);

    /**
     * Buscar sesiones por rango de fechas
     * 
     * @param fechaDesde Fecha de inicio del rango
     * @param fechaHasta Fecha final del rango
     * @return List<Acceso> con sesiones en el rango especificado
     */
    @Query("SELECT a FROM Acceso a " +
           "WHERE a.fechaInicio >= :fechaDesde " +
           "AND a.fechaInicio <= :fechaHasta " +
           "ORDER BY a.fechaInicio DESC")
    List<Acceso> findByFechaInicioRange(@Param("fechaDesde") java.time.LocalDateTime fechaDesde,
                                        @Param("fechaHasta") java.time.LocalDateTime fechaHasta);

    /**
     * Contar total de sesiones activas en el sistema
     * 
     * @return long número total de sesiones activas
     */
    long countByActivoTrue();

    /**
     * Obtener sesiones que han expirado pero siguen marcadas como activas
     * (para limpieza de datos)
     * 
     * @return List<Acceso> con sesiones expiradas pero activas
     */
    @Query("SELECT a FROM Acceso a " +
           "WHERE a.activo = true " +
           "AND a.fechaFin IS NOT NULL " +
           "AND a.fechaFin < CURRENT_TIMESTAMP")
    List<Acceso> findExpiredActiveSessions();

    /**
     * Obtener sesiones activas sin fecha de fin establecida
     * 
     * @return List<Acceso> con sesiones activas sin fecha de fin
     */
    @Query("SELECT a FROM Acceso a " +
           "WHERE a.activo = true " +
           "AND a.fechaFin IS NULL")
    List<Acceso> findActiveSessionsWithoutEndDate();

    /**
     * Buscar sesiones activas que iniciaron antes de una fecha específica
     * (usado para limpiar sesiones expiradas basándose en el tiempo de vida del token)
     * 
     * @param fechaLimite Fecha límite para considerar sesiones expiradas
     * @return List<Acceso> con sesiones activas que iniciaron antes de la fecha límite
     */
    List<Acceso> findByActivoTrueAndFechaInicioBefore(java.time.LocalDateTime fechaLimite);
}