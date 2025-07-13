package mx.com.qtx.seguridad.controller;

import mx.com.qtx.seguridad.dto.UsuarioDto;
import mx.com.qtx.seguridad.domain.Usuario;
import mx.com.qtx.seguridad.service.UsuarioService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión de usuarios
 * Requiere rol ADMIN para todas las operaciones
 */
@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Listar usuarios con paginación (requiere ADMIN)
     * 
     * @param page Número de página (default: 0)
     * @param size Tamaño de página (default: 10)
     * @param sort Campo de ordenamiento (default: id)
     * @param direction Dirección de ordenamiento (default: ASC)
     * @return Page<UsuarioDto> con usuarios paginados
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<Usuario> usuariosPage = usuarioService.obtenerUsuariosPaginados(pageable);
            
            List<UsuarioDto> usuariosDto = usuariosPage.getContent().stream()
                    .map(usuario -> {
                        List<String> roles = usuarioService.obtenerRolesDeUsuario(usuario.getId());
                        return usuarioService.convertirADtoConRoles(usuario, roles);
                    })
                    .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("usuarios", usuariosDto);
            response.put("currentPage", usuariosPage.getNumber());
            response.put("totalItems", usuariosPage.getTotalElements());
            response.put("totalPages", usuariosPage.getTotalPages());
            response.put("pageSize", usuariosPage.getSize());
            response.put("hasNext", usuariosPage.hasNext());
            response.put("hasPrevious", usuariosPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "fetch_error");
            error.put("message", "Error al obtener la lista de usuarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener usuario por ID (requiere ADMIN)
     * 
     * @param id ID del usuario
     * @return UsuarioDto con información del usuario
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuario(@PathVariable Integer id) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_not_found");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Usuario usuario = usuarioOpt.get();
            List<String> roles = usuarioService.obtenerRolesDeUsuario(id);
            
            UsuarioDto usuarioDto = usuarioService.convertirADtoConRoles(usuario, roles);
            
            return ResponseEntity.ok(usuarioDto);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "fetch_error");
            error.put("message", "Error al obtener el usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crear nuevo usuario (requiere ADMIN)
     * 
     * @param usuarioDto Datos del nuevo usuario
     * @return UsuarioDto del usuario creado
     */
    @PostMapping
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            // Validar que el DTO sea válido para creación
            if (!usuarioService.esValidoParaCreacion(usuarioDto)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "validation_error");
                error.put("message", "Datos inválidos para crear usuario. Usuario y contraseña son requeridos.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Verificar que el usuario no exista
            Optional<Usuario> existingUser = usuarioService.buscarPorNombreUsuario(usuarioDto.getUsuario());
            if (existingUser.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_exists");
                error.put("message", "Ya existe un usuario con ese nombre");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Crear usuario usando el servicio
            Usuario usuarioGuardado = usuarioService.crearUsuario(usuarioDto);
            
            // Crear respuesta DTO (sin contraseña)
            UsuarioDto responseDto = usuarioService.convertirADto(usuarioGuardado);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "creation_error");
            error.put("message", "Error al crear el usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar usuario existente (requiere ADMIN)
     * 
     * @param id ID del usuario a actualizar
     * @param usuarioDto Datos actualizados
     * @return UsuarioDto del usuario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_not_found");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Usuario usuarioExistente = usuarioOpt.get();
            
            // Verificar si se va a cambiar el nombre de usuario y que no exista otro con ese nombre
            if (usuarioDto.getUsuario() != null && 
                !usuarioDto.getUsuario().equals(usuarioExistente.getUsuario())) {
                
                if (usuarioService.existeOtroUsuarioConNombre(usuarioDto.getUsuario(), id)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "user_exists");
                    error.put("message", "Ya existe otro usuario con ese nombre");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
                }
            }

            // Actualizar usuario usando el servicio
            Usuario usuarioGuardado = usuarioService.actualizarUsuario(usuarioExistente, usuarioDto);
            
            // Obtener roles actuales para la respuesta
            List<String> roles = usuarioService.obtenerRolesDeUsuario(id);
            
            UsuarioDto responseDto = usuarioService.convertirADtoConRoles(usuarioGuardado, roles);
            
            return ResponseEntity.ok(responseDto);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "update_error");
            error.put("message", "Error al actualizar el usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Desactivar usuario (soft delete) (requiere ADMIN)
     * 
     * @param id ID del usuario a desactivar
     * @return Confirmación de desactivación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Integer id) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_not_found");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Usuario usuario = usuarioOpt.get();
            
            // Desactivar usuario usando el servicio
            usuarioService.desactivarUsuario(usuario);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario desactivado exitosamente");
            response.put("status", "success");
            response.put("usuarioId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "deletion_error");
            error.put("message", "Error al desactivar el usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener estadísticas de usuarios (requiere ADMIN)
     * 
     * @return Estadísticas básicas de usuarios
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            long[] estadisticas = usuarioService.obtenerEstadisticasUsuarios();
            long totalUsers = estadisticas[0];
            long activeUsers = estadisticas[1];
            long inactiveUsers = estadisticas[2];
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);
            stats.put("generatedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "stats_error");
            error.put("message", "Error al obtener estadísticas de usuarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}