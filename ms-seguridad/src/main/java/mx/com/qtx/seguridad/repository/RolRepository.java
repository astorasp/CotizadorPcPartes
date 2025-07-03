package mx.com.qtx.seguridad.repository;

import mx.com.qtx.seguridad.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Obtener todos los roles activos
     */
    List<Rol> findByActivoTrue();

    /**
     * Obtener todos los roles inactivos
     */
    List<Rol> findByActivoFalse();

    /**
     * Obtener roles por estado de activación
     */
    List<Rol> findByActivo(Boolean activo);

    /**
     * Buscar rol por nombre
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Buscar rol por nombre y que esté activo
     */
    Optional<Rol> findByNombreAndActivo(String nombre, Boolean activo);

    /**
     * Verificar si existe un rol con el nombre especificado
     */
    boolean existsByNombre(String nombre);

    /**
     * Verificar si existe un rol activo con el nombre especificado
     */
    boolean existsByNombreAndActivo(String nombre, Boolean activo);

    /**
     * Buscar roles que contengan el texto en el nombre
     */
    List<Rol> findByNombreContainingIgnoreCase(String texto);

    /**
     * Buscar roles activos que contengan el texto en el nombre
     */
    List<Rol> findByNombreContainingIgnoreCaseAndActivo(String texto, Boolean activo);

    /**
     * Obtener roles ordenados por nombre
     */
    List<Rol> findByActivoTrueOrderByNombreAsc();

    /**
     * Contar roles activos
     */
    long countByActivo(Boolean activo);

    /**
     * Obtener roles que están asignados a al menos un usuario activo
     */
    @Query("SELECT DISTINCT r FROM Rol r " +
           "JOIN r.usuariosAsignados ra " +
           "WHERE r.activo = true " +
           "AND ra.activo = true " +
           "AND ra.usuario.activo = true")
    List<Rol> findRolesWithActiveUsers();

    /**
     * Obtener roles que NO están asignados a ningún usuario
     */
    @Query("SELECT r FROM Rol r " +
           "WHERE r.activo = true " +
           "AND NOT EXISTS (SELECT ra FROM RolAsignado ra " +
           "                WHERE ra.rol = r " +
           "                AND ra.activo = true " +
           "                AND ra.usuario.activo = true)")
    List<Rol> findRolesWithoutActiveUsers();

    /**
     * Obtener roles asignados a un usuario específico
     */
    @Query("SELECT DISTINCT r FROM Rol r " +
           "JOIN r.usuariosAsignados ra " +
           "WHERE ra.usuario.id = :usuarioId " +
           "AND r.activo = true " +
           "AND ra.activo = true " +
           "AND ra.usuario.activo = true")
    List<Rol> findRolesByUsuarioId(@Param("usuarioId") Integer usuarioId);

    /**
     * Obtener roles disponibles para asignar a un usuario (roles activos no asignados al usuario)
     */
    @Query("SELECT r FROM Rol r " +
           "WHERE r.activo = true " +
           "AND NOT EXISTS (SELECT ra FROM RolAsignado ra " +
           "                WHERE ra.rol = r " +
           "                AND ra.usuario.id = :usuarioId " +
           "                AND ra.activo = true)")
    List<Rol> findAvailableRolesForUser(@Param("usuarioId") Integer usuarioId);

    /**
     * Contar cuántos usuarios tienen asignado un rol específico
     */
    @Query("SELECT COUNT(DISTINCT ra.usuario) FROM RolAsignado ra " +
           "WHERE ra.rol.id = :rolId " +
           "AND ra.activo = true " +
           "AND ra.usuario.activo = true " +
           "AND ra.rol.activo = true")
    long countUsuariosByRolId(@Param("rolId") Integer rolId);
}