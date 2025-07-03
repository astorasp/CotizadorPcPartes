package mx.com.qtx.seguridad.repository;

import mx.com.qtx.seguridad.domain.RolAsignado;
import mx.com.qtx.seguridad.domain.RolAsignadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolAsignadoRepository extends JpaRepository<RolAsignado, RolAsignadoId> {

    /**
     * Obtener todas las asignaciones activas de un usuario
     */
    List<RolAsignado> findByUsuarioIdAndActivo(Integer usuarioId, Boolean activo);

    /**
     * Obtener todas las asignaciones de un usuario (activas e inactivas)
     */
    List<RolAsignado> findByUsuarioId(Integer usuarioId);

    /**
     * Obtener todas las asignaciones activas de un rol
     */
    List<RolAsignado> findByRolIdAndActivo(Integer rolId, Boolean activo);

    /**
     * Obtener todas las asignaciones de un rol (activas e inactivas)
     */
    List<RolAsignado> findByRolId(Integer rolId);

    /**
     * Obtener todas las asignaciones activas
     */
    List<RolAsignado> findByActivoTrue();

    /**
     * Obtener todas las asignaciones inactivas
     */
    List<RolAsignado> findByActivoFalse();

    /**
     * Buscar una asignación específica por usuario y rol
     */
    Optional<RolAsignado> findByUsuarioIdAndRolId(Integer usuarioId, Integer rolId);

    /**
     * Buscar una asignación específica activa por usuario y rol
     */
    Optional<RolAsignado> findByUsuarioIdAndRolIdAndActivo(Integer usuarioId, Integer rolId, Boolean activo);

    /**
     * Verificar si existe una asignación específica
     */
    boolean existsByUsuarioIdAndRolId(Integer usuarioId, Integer rolId);

    /**
     * Verificar si existe una asignación específica activa
     */
    boolean existsByUsuarioIdAndRolIdAndActivo(Integer usuarioId, Integer rolId, Boolean activo);

    /**
     * Obtener todas las asignaciones con información de usuario y rol
     */
    @Query("SELECT ra FROM RolAsignado ra " +
           "JOIN FETCH ra.usuario u " +
           "JOIN FETCH ra.rol r " +
           "WHERE ra.activo = true " +
           "AND u.activo = true " +
           "AND r.activo = true")
    List<RolAsignado> findAllActiveAssignmentsWithDetails();

    /**
     * Obtener asignaciones de un usuario con detalles de roles
     */
    @Query("SELECT ra FROM RolAsignado ra " +
           "JOIN FETCH ra.rol r " +
           "WHERE ra.usuario.id = :usuarioId " +
           "AND ra.activo = true " +
           "AND r.activo = true")
    List<RolAsignado> findActiveAssignmentsByUsuarioWithRoleDetails(@Param("usuarioId") Integer usuarioId);

    /**
     * Obtener asignaciones de un rol con detalles de usuarios
     */
    @Query("SELECT ra FROM RolAsignado ra " +
           "JOIN FETCH ra.usuario u " +
           "WHERE ra.rol.id = :rolId " +
           "AND ra.activo = true " +
           "AND u.activo = true")
    List<RolAsignado> findActiveAssignmentsByRolWithUserDetails(@Param("rolId") Integer rolId);

    /**
     * Contar asignaciones activas por usuario
     */
    long countByUsuarioIdAndActivo(Integer usuarioId, Boolean activo);

    /**
     * Contar asignaciones activas por rol
     */
    long countByRolIdAndActivo(Integer rolId, Boolean activo);

    /**
     * Activar una asignación específica
     */
    @Modifying
    @Query("UPDATE RolAsignado ra SET ra.activo = true " +
           "WHERE ra.usuario.id = :usuarioId AND ra.rol.id = :rolId")
    int activateAssignment(@Param("usuarioId") Integer usuarioId, @Param("rolId") Integer rolId);

    /**
     * Desactivar una asignación específica
     */
    @Modifying
    @Query("UPDATE RolAsignado ra SET ra.activo = false " +
           "WHERE ra.usuario.id = :usuarioId AND ra.rol.id = :rolId")
    int deactivateAssignment(@Param("usuarioId") Integer usuarioId, @Param("rolId") Integer rolId);

    /**
     * Desactivar todas las asignaciones de un usuario
     */
    @Modifying
    @Query("UPDATE RolAsignado ra SET ra.activo = false " +
           "WHERE ra.usuario.id = :usuarioId")
    int deactivateAllUserAssignments(@Param("usuarioId") Integer usuarioId);

    /**
     * Desactivar todas las asignaciones de un rol
     */
    @Modifying
    @Query("UPDATE RolAsignado ra SET ra.activo = false " +
           "WHERE ra.rol.id = :rolId")
    int deactivateAllRoleAssignments(@Param("rolId") Integer rolId);

    /**
     * Obtener nombres de roles asignados a un usuario específico
     */
    @Query("SELECT DISTINCT r.nombre FROM RolAsignado ra " +
           "JOIN ra.rol r " +
           "WHERE ra.usuario.id = :usuarioId " +
           "AND ra.activo = true " +
           "AND r.activo = true " +
           "AND ra.usuario.activo = true")
    List<String> findRoleNamesByUsuarioId(@Param("usuarioId") Integer usuarioId);

    /**
     * Obtener nombres de usuarios que tienen un rol específico
     */
    @Query("SELECT DISTINCT u.usuario FROM RolAsignado ra " +
           "JOIN ra.usuario u " +
           "WHERE ra.rol.id = :rolId " +
           "AND ra.activo = true " +
           "AND u.activo = true " +
           "AND ra.rol.activo = true")
    List<String> findUsernamesByRolId(@Param("rolId") Integer rolId);
}