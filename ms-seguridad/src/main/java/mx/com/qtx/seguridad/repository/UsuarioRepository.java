package mx.com.qtx.seguridad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.com.qtx.seguridad.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Buscar usuario por nombre de usuario y que esté activo
     */
    Optional<Usuario> findByUsuarioAndActivo(String usuario, Boolean activo);

    /**
     * Buscar usuario por nombre de usuario (independiente del estado)
     */
    Optional<Usuario> findByUsuario(String usuario);

    /**
     * Verificar si existe un usuario con el nombre especificado
     */
    boolean existsByUsuario(String usuario);

    /**
     * Verificar si existe un usuario activo con el nombre especificado
     */
    boolean existsByUsuarioAndActivo(String usuario, Boolean activo);

    /**
     * Obtener todos los usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Obtener todos los usuarios inactivos
     */
    List<Usuario> findByActivoFalse();

    /**
     * Obtener usuarios por estado de activación
     */
    List<Usuario> findByActivo(Boolean activo);

    /**
     * Buscar usuarios que contengan el texto en el nombre de usuario
     */
    List<Usuario> findByUsuarioContainingIgnoreCase(String texto);

    /**
     * Buscar usuarios activos que contengan el texto en el nombre de usuario
     */
    List<Usuario> findByUsuarioContainingIgnoreCaseAndActivo(String texto, Boolean activo);

    /**
     * Obtener usuario con sus roles asignados activos
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.rolesAsignados ra " +
           "LEFT JOIN FETCH ra.rol r " +
           "WHERE u.usuario = :usuario " +
           "AND u.activo = true " +
           "AND (ra.activo = true OR ra.activo IS NULL) " +
           "AND (r.activo = true OR r.activo IS NULL)")
    Optional<Usuario> findUsuarioWithActiveRoles(@Param("usuario") String usuario);

    /**
     * Obtener todos los usuarios con sus roles asignados
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.rolesAsignados ra " +
           "LEFT JOIN FETCH ra.rol r " +
           "WHERE u.activo = true")
    List<Usuario> findAllActiveUsersWithRoles();

    /**
     * Contar usuarios activos
     */
    long countByActivo(Boolean activo);
    
    /**
     * Contar solo usuarios activos (método de conveniencia)
     */
    long countByActivoTrue();

    /**
     * Verificar si un usuario tiene un rol específico asignado y activo
     */
    @Query("SELECT COUNT(ra) > 0 FROM RolAsignado ra " +
           "WHERE ra.usuario.id = :usuarioId " +
           "AND ra.rol.id = :rolId " +
           "AND ra.activo = true " +
           "AND ra.usuario.activo = true " +
           "AND ra.rol.activo = true")
    boolean hasActiveRole(@Param("usuarioId") Long usuarioId, @Param("rolId") Long rolId);

    /**
     * Obtener usuarios que tienen un rol específico asignado
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "JOIN u.rolesAsignados ra " +
           "WHERE ra.rol.nombre = :nombreRol " +
           "AND u.activo = true " +
           "AND ra.activo = true " +
           "AND ra.rol.activo = true")
    List<Usuario> findUsuariosByRolNombre(@Param("nombreRol") String nombreRol);
}