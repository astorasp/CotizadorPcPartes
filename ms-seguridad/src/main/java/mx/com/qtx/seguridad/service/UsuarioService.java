package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.dto.UsuarioDto;
import mx.com.qtx.seguridad.dto.UsuarioMapper;
import mx.com.qtx.seguridad.entity.RolAsignado;
import mx.com.qtx.seguridad.entity.Usuario;
import mx.com.qtx.seguridad.repository.RolAsignadoRepository;
import mx.com.qtx.seguridad.repository.UsuarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de usuarios
 * Encapsula la lógica de negocio relacionada con usuarios
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolAsignadoRepository rolAsignadoRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                         RolAsignadoRepository rolAsignadoRepository,
                         UsuarioMapper usuarioMapper,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolAsignadoRepository = rolAsignadoRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtener usuarios paginados
     * 
     * @param pageable Información de paginación
     * @return Page con usuarios y información de paginación
     */
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerUsuariosPaginados(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Obtener roles de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return Lista de nombres de roles
     */
    @Transactional(readOnly = true)
    public List<String> obtenerRolesDeUsuario(Integer usuarioId) {
        return rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true)
                .stream()
                .map(ra -> ra.getRol().getNombre())
                .distinct()
                .toList();
    }

    /**
     * Convertir usuario a DTO con roles
     * 
     * @param usuario Entidad usuario
     * @param roles Lista de roles
     * @return UsuarioDto
     */
    @Transactional(readOnly = true)
    public UsuarioDto convertirADtoConRoles(Usuario usuario, List<String> roles) {
        return usuarioMapper.toDto(usuario, roles);
    }

    /**
     * Convertir usuario a DTO sin roles
     * 
     * @param usuario Entidad usuario
     * @return UsuarioDto
     */
    @Transactional(readOnly = true)
    public UsuarioDto convertirADto(Usuario usuario) {
        return usuarioMapper.toDto(usuario);
    }

    /**
     * Buscar usuario por ID
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Buscar usuario por nombre de usuario
     * 
     * @param nombreUsuario Nombre del usuario
     * @return Optional con el usuario si existe
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByUsuario(nombreUsuario);
    }

    /**
     * Validar si los datos del DTO son válidos para creación
     * 
     * @param usuarioDto DTO del usuario
     * @return true si es válido para creación
     */
    public boolean esValidoParaCreacion(UsuarioDto usuarioDto) {
        return usuarioMapper.isValidForCreation(usuarioDto);
    }

    /**
     * Crear nuevo usuario
     * 
     * @param usuarioDto Datos del usuario
     * @return Usuario creado
     */
    public Usuario crearUsuario(UsuarioDto usuarioDto) {
        // Crear nueva entidad Usuario
        Usuario nuevoUsuario = usuarioMapper.toEntity(usuarioDto);
        
        // Encriptar contraseña
        String passwordHash = passwordEncoder.encode(usuarioDto.getPassword());
        nuevoUsuario.setPassword(passwordHash);
        
        // Guardar usuario
        return usuarioRepository.save(nuevoUsuario);
    }

    /**
     * Verificar si existe otro usuario con el mismo nombre (excluyendo uno específico)
     * 
     * @param nombreUsuario Nombre del usuario
     * @param idExcluir ID del usuario a excluir de la búsqueda
     * @return true si existe otro usuario con ese nombre
     */
    @Transactional(readOnly = true)
    public boolean existeOtroUsuarioConNombre(String nombreUsuario, Integer idExcluir) {
        Optional<Usuario> existingUser = usuarioRepository.findByUsuario(nombreUsuario);
        return existingUser.isPresent() && !existingUser.get().getId().equals(idExcluir);
    }

    /**
     * Actualizar usuario existente
     * 
     * @param usuarioExistente Usuario actual
     * @param usuarioDto Datos actualizados
     * @return Usuario actualizado
     */
    public Usuario actualizarUsuario(Usuario usuarioExistente, UsuarioDto usuarioDto) {
        // Actualizar campos del usuario
        Usuario usuarioActualizado = usuarioMapper.updateEntity(usuarioExistente, usuarioDto);
        
        // Si se proporcionó nueva contraseña, encriptarla
        if (usuarioDto.hasPassword()) {
            String passwordHash = passwordEncoder.encode(usuarioDto.getPassword());
            usuarioActualizado.setPassword(passwordHash);
        }
        
        // Guardar cambios
        return usuarioRepository.save(usuarioActualizado);
    }

    /**
     * Desactivar usuario (soft delete)
     * 
     * @param usuario Usuario a desactivar
     * @return Usuario desactivado
     */
    public Usuario desactivarUsuario(Usuario usuario) {
        // Soft delete - marcar como inactivo
        usuario.setActivo(false);
        Usuario usuarioDesactivado = usuarioRepository.save(usuario);
        
        // También desactivar todas las asignaciones de roles
        List<RolAsignado> rolesAsignados = rolAsignadoRepository.findByUsuarioId(usuario.getId());
        rolesAsignados.forEach(ra -> ra.setActivo(false));
        rolAsignadoRepository.saveAll(rolesAsignados);
        
        return usuarioDesactivado;
    }

    /**
     * Obtener estadísticas de usuarios
     * 
     * @return Array con [totalUsers, activeUsers, inactiveUsers]
     */
    @Transactional(readOnly = true)
    public long[] obtenerEstadisticasUsuarios() {
        long totalUsers = usuarioRepository.count();
        long activeUsers = usuarioRepository.countByActivoTrue();
        long inactiveUsers = totalUsers - activeUsers;
        
        return new long[]{totalUsers, activeUsers, inactiveUsers};
    }
}