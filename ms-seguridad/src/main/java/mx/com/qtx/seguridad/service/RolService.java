package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.repository.RolRepository;
import mx.com.qtx.seguridad.entity.Rol;
import mx.com.qtx.seguridad.entity.RolAsignado;
import mx.com.qtx.seguridad.entity.RolAsignadoId;
import mx.com.qtx.seguridad.entity.Usuario;
import mx.com.qtx.seguridad.repository.RolAsignadoRepository;
import mx.com.qtx.seguridad.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de roles y asignaciones
 * Encapsula la lógica de negocio relacionada con roles
 */
@Service
@Transactional
public class RolService {

    private final RolRepository rolRepository;
    private final RolAsignadoRepository rolAsignadoRepository;
    private final UsuarioRepository usuarioRepository;

    public RolService(RolRepository rolRepository,
                     RolAsignadoRepository rolAsignadoRepository,
                     UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.rolAsignadoRepository = rolAsignadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtener todos los roles activos
     * 
     * @return Lista de roles activos
     */
    @Transactional(readOnly = true)
    public List<Rol> obtenerRolesActivos() {
        return rolRepository.findByActivoTrue();
    }

    /**
     * Obtener roles asignados a un usuario específico
     * 
     * @param usuarioId ID del usuario
     * @return Lista de roles asignados al usuario
     */
    @Transactional(readOnly = true)
    public List<RolAsignado> obtenerRolesPorUsuario(Integer usuarioId) {
        return rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true);
    }

    /**
     * Verificar si un usuario existe
     * 
     * @param usuarioId ID del usuario
     * @return Optional con el usuario si existe
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorId(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId);
    }

    /**
     * Verificar si un rol existe y está activo
     * 
     * @param rolId ID del rol
     * @return Optional con el rol si existe y está activo
     */
    @Transactional(readOnly = true)
    public Optional<Rol> buscarRolActivoPorId(Integer rolId) {
        return rolRepository.findById(rolId)
                .filter(Rol::isActivo);
    }

    /**
     * Verificar si un rol existe (activo o inactivo)
     * 
     * @param rolId ID del rol
     * @return Optional con el rol si existe
     */
    @Transactional(readOnly = true)
    public Optional<Rol> buscarRolPorId(Integer rolId) {
        return rolRepository.findById(rolId);
    }

    /**
     * Asignar un rol a un usuario
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @param usuario Entidad del usuario
     * @param rol Entidad del rol
     * @return true si se asignó correctamente, false si ya estaba asignado
     */
    public boolean asignarRol(Integer usuarioId, Integer rolId, Usuario usuario, Rol rol) {
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);
        Optional<RolAsignado> existingAssignment = rolAsignadoRepository.findById(rolAsignadoId);
        
        if (existingAssignment.isPresent()) {
            RolAsignado existing = existingAssignment.get();
            if (existing.isActivo()) {
                return false; // Ya estaba asignado
            } else {
                // Reactivar asignación existente
                existing.setActivo(true);
                rolAsignadoRepository.save(existing);
                return true;
            }
        } else {
            // Crear nueva asignación
            RolAsignado nuevaAsignacion = new RolAsignado();
            nuevaAsignacion.setId(rolAsignadoId);
            nuevaAsignacion.setUsuario(usuario);
            nuevaAsignacion.setRol(rol);
            nuevaAsignacion.setActivo(true);
            
            rolAsignadoRepository.save(nuevaAsignacion);
            return true;
        }
    }

    /**
     * Revocar un rol de un usuario
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return true si se revocó correctamente, false si no estaba asignado
     */
    public boolean revocarRol(Integer usuarioId, Integer rolId) {
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);
        Optional<RolAsignado> assignmentOpt = rolAsignadoRepository.findById(rolAsignadoId);
        
        if (assignmentOpt.isEmpty() || !assignmentOpt.get().isActivo()) {
            return false; // No estaba asignado
        }

        // Desactivar la asignación (soft delete)
        RolAsignado assignment = assignmentOpt.get();
        assignment.setActivo(false);
        rolAsignadoRepository.save(assignment);
        
        return true;
    }

    /**
     * Obtener estadísticas de roles
     * 
     * @return Lista de roles con el número de usuarios asignados
     */
    @Transactional(readOnly = true)
    public List<Rol> obtenerRolesConEstadisticas() {
        return rolRepository.findByActivoTrue();
    }

    /**
     * Contar usuarios asignados a un rol específico
     * 
     * @param rolId ID del rol
     * @return Número de usuarios asignados al rol
     */
    @Transactional(readOnly = true)
    public long contarUsuariosAsignados(Integer rolId) {
        return rolAsignadoRepository.countByRolIdAndActivo(rolId, true);
    }
}