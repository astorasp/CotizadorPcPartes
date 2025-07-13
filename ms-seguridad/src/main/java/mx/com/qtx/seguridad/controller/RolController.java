package mx.com.qtx.seguridad.controller;

import mx.com.qtx.seguridad.domain.Rol;
import mx.com.qtx.seguridad.domain.RolAsignado;
import mx.com.qtx.seguridad.domain.Usuario;
import mx.com.qtx.seguridad.service.RolService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión de roles
 * Requiere rol ADMIN para todas las operaciones
 */
@RestController
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    /**
     * Listar roles disponibles (requiere ADMIN)
     * 
     * @return Lista de roles activos
     */
    @GetMapping
    public ResponseEntity<?> getRoles() {
        try {
            List<Rol> roles = rolService.obtenerRolesActivos();
            
            List<Map<String, Object>> rolesResponse = roles.stream()
                    .map(rol -> {
                        Map<String, Object> rolMap = new HashMap<>();
                        rolMap.put("id", rol.getId());
                        rolMap.put("nombre", rol.getNombre());
                        rolMap.put("activo", rol.isActivo());
                        rolMap.put("fechaCreacion", rol.getFechaCreacion());
                        return rolMap;
                    })
                    .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("roles", rolesResponse);
            response.put("total", rolesResponse.size());
            response.put("retrievedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "fetch_error");
            error.put("message", "Error al obtener la lista de roles");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener roles de un usuario específico
     * 
     * @param usuarioId ID del usuario
     * @return Lista de roles asignados al usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> getRolesByUsuario(@PathVariable Integer usuarioId) {
        try {
            Optional<Usuario> usuarioOpt = rolService.buscarUsuarioPorId(usuarioId);
            
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_not_found");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            List<RolAsignado> rolesAsignados = rolService.obtenerRolesPorUsuario(usuarioId);
            
            List<Map<String, Object>> rolesResponse = rolesAsignados.stream()
                    .map(ra -> {
                        Map<String, Object> rolMap = new HashMap<>();
                        rolMap.put("rolId", ra.getRol().getId());
                        rolMap.put("nombre", ra.getRol().getNombre());
                        rolMap.put("fechaAsignacion", ra.getFechaCreacion());
                        return rolMap;
                    })
                    .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("usuarioId", usuarioId);
            response.put("usuario", usuarioOpt.get().getUsuario());
            response.put("roles", rolesResponse);
            response.put("totalRoles", rolesResponse.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "fetch_error");
            error.put("message", "Error al obtener roles del usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Asignar rol a usuario (requiere ADMIN)
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return Confirmación de asignación
     */
    @PostMapping("/usuario/{usuarioId}/rol/{rolId}")
    public ResponseEntity<?> asignarRol(@PathVariable Integer usuarioId, @PathVariable Integer rolId) {
        try {
            // Verificar que el usuario existe
            Optional<Usuario> usuarioOpt = rolService.buscarUsuarioPorId(usuarioId);
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_not_found");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Verificar que el rol existe y está activo
            Optional<Rol> rolOpt = rolService.buscarRolActivoPorId(rolId);
            if (rolOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "role_not_found");
                error.put("message", "Rol no encontrado o inactivo");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Usuario usuario = usuarioOpt.get();
            Rol rol = rolOpt.get();

            // Asignar rol usando el servicio
            boolean asignado = rolService.asignarRol(usuarioId, rolId, usuario, rol);
            
            if (!asignado) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "role_already_assigned");
                error.put("message", "El rol ya está asignado al usuario");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rol asignado exitosamente");
            response.put("status", "success");
            response.put("usuarioId", usuarioId);
            response.put("usuario", usuario.getUsuario());
            response.put("rolId", rolId);
            response.put("rol", rol.getNombre());
            response.put("assignedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "assignment_error");
            error.put("message", "Error al asignar el rol");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Revocar rol de usuario (requiere ADMIN)
     * 
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     * @return Confirmación de revocación
     */
    @DeleteMapping("/usuario/{usuarioId}/rol/{rolId}")
    public ResponseEntity<?> revocarRol(@PathVariable Integer usuarioId, @PathVariable Integer rolId) {
        try {
            // Verificar que el usuario existe
            Optional<Usuario> usuarioOpt = rolService.buscarUsuarioPorId(usuarioId);
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "user_not_found");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Verificar que el rol existe
            Optional<Rol> rolOpt = rolService.buscarRolPorId(rolId);
            if (rolOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "role_not_found");
                error.put("message", "Rol no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Revocar rol usando el servicio
            boolean revocado = rolService.revocarRol(usuarioId, rolId);
            
            if (!revocado) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "role_not_assigned");
                error.put("message", "El rol no está asignado al usuario");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Usuario usuario = usuarioOpt.get();
            Rol rol = rolOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rol revocado exitosamente");
            response.put("status", "success");
            response.put("usuarioId", usuarioId);
            response.put("usuario", usuario.getUsuario());
            response.put("rolId", rolId);
            response.put("rol", rol.getNombre());
            response.put("revokedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "revocation_error");
            error.put("message", "Error al revocar el rol");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener estadísticas de asignación de roles
     * 
     * @return Estadísticas de roles y asignaciones
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getRoleStats() {
        try {
            List<Rol> rolesActivos = rolService.obtenerRolesConEstadisticas();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRoles", rolesActivos.size());
            
            List<Map<String, Object>> roleDetails = rolesActivos.stream()
                    .map(rol -> {
                        long assignedUsers = rolService.contarUsuariosAsignados(rol.getId());
                        
                        Map<String, Object> roleStat = new HashMap<>();
                        roleStat.put("rolId", rol.getId());
                        roleStat.put("nombre", rol.getNombre());
                        roleStat.put("usuariosAsignados", assignedUsers);
                        return roleStat;
                    })
                    .toList();
            
            stats.put("roles", roleDetails);
            stats.put("generatedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "stats_error");
            error.put("message", "Error al obtener estadísticas de roles");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}