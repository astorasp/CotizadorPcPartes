package mx.com.qtx.seguridad.dto;

import org.springframework.stereotype.Component;

import mx.com.qtx.seguridad.entity.RolAsignado;
import mx.com.qtx.seguridad.entity.Usuario;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Usuario y DTOs
 * Maneja conversiones bidireccionales con consideraciones de seguridad
 */
@Component
public class UsuarioMapper {

    /**
     * Convierte entidad Usuario a DTO (para responses)
     * Excluye automáticamente la contraseña por seguridad
     * 
     * @param usuario Entidad Usuario
     * @param roles Lista de nombres de roles (opcional)
     * @return UsuarioDto sin contraseña
     */
    public UsuarioDto toDto(Usuario usuario, List<String> roles) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioDto(
            usuario.getId(),
            usuario.getUsuario(),
            usuario.getActivo(),
            usuario.getFechaCreacion(),
            usuario.getFechaModificacion(),
            roles
        );
    }

    /**
     * Convierte entidad Usuario a DTO extrayendo roles de la entidad
     * 
     * @param usuario Entidad Usuario con roles cargados
     * @return UsuarioDto sin contraseña
     */
    public UsuarioDto toDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        List<String> roles = null;
        if (usuario.getRolesAsignados() != null) {
            roles = usuario.getRolesAsignados().stream()
                    .filter(ra -> ra.isActivo() && ra.getRol().isActivo())
                    .map(ra -> ra.getRol().getNombre())
                    .distinct()
                    .collect(Collectors.toList());
        }

        return toDto(usuario, roles);
    }

    /**
     * Convierte DTO a entidad Usuario (para creación)
     * Solo mapea campos básicos, no incluye ID ni fechas
     * 
     * @param dto UsuarioDto con datos de entrada
     * @return Usuario nueva entidad
     */
    public Usuario toEntity(UsuarioDto dto) {
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(dto.getUsuario());
        usuario.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        
        // La contraseña se manejará separadamente con encoding
        // No se mapea directamente por seguridad
        
        return usuario;
    }

    /**
     * Actualiza entidad existente con datos del DTO
     * Preserva ID, fechas y contraseña existente si no se proporciona nueva
     * 
     * @param existingUsuario Usuario existente
     * @param dto UsuarioDto con datos actualizados
     * @return Usuario actualizado
     */
    public Usuario updateEntity(Usuario existingUsuario, UsuarioDto dto) {
        if (existingUsuario == null || dto == null) {
            return existingUsuario;
        }

        // Actualizar solo campos modificables
        if (dto.getUsuario() != null) {
            existingUsuario.setUsuario(dto.getUsuario());
        }
        
        if (dto.getActivo() != null) {
            existingUsuario.setActivo(dto.getActivo());
        }

        // La contraseña se actualiza separadamente si se proporciona
        // No se hace aquí por temas de seguridad y encoding

        return existingUsuario;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     * 
     * @param usuarios Lista de entidades Usuario
     * @return Lista de UsuarioDto
     */
    public List<UsuarioDto> toDtoList(List<Usuario> usuarios) {
        if (usuarios == null) {
            return null;
        }

        return usuarios.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Crea DTO básico solo con información esencial
     * Útil para respuestas ligeras sin roles
     * 
     * @param usuario Entidad Usuario
     * @return UsuarioDto básico
     */
    public UsuarioDto toBasicDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setUsuario(usuario.getUsuario());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaModificacion(usuario.getFechaModificacion());
        
        return dto;
    }

    /**
     * Valida que un DTO tenga los campos mínimos requeridos para creación
     * 
     * @param dto UsuarioDto a validar
     * @return boolean true si es válido para creación
     */
    public boolean isValidForCreation(UsuarioDto dto) {
        return dto != null &&
               dto.getUsuario() != null && !dto.getUsuario().trim().isEmpty() &&
               dto.hasPassword();
    }

    /**
     * Valida que un DTO tenga los campos mínimos requeridos para actualización
     * 
     * @param dto UsuarioDto a validar
     * @return boolean true si es válido para actualización
     */
    public boolean isValidForUpdate(UsuarioDto dto) {
        return dto != null &&
               (dto.getUsuario() != null && !dto.getUsuario().trim().isEmpty() ||
                dto.getActivo() != null ||
                dto.hasPassword());
    }
}