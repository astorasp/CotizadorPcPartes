package mx.com.qtx.seguridad.controller;

import mx.com.qtx.seguridad.dto.TokenResponse;
import mx.com.qtx.seguridad.dto.TokenTtlResponse;
import mx.com.qtx.seguridad.service.AuthService;
import mx.com.qtx.seguridad.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;

/**
 * Controlador REST para autenticación y gestión de tokens JWT
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Endpoint de autenticación (PRD 3.1)
     * Valida credenciales e incluye roles en JWT claims
     * 
     * @param loginRequest Credenciales de usuario
     * @return TokenResponse con access y refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        try {
            String usuario = loginRequest.get("usuario");
            String password = loginRequest.get("password");
            
            if (usuario == null || usuario.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "missing_credentials");
                error.put("message", "Usuario y contraseña son requeridos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            TokenResponse tokenResponse = authService.authenticate(
                usuario, 
                password,
                request
            );
            
            return ResponseEntity.ok(tokenResponse);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            
            // Verificar si es un error de sesión activa
            if (e.getMessage() != null && e.getMessage().contains("Ya existe una sesión activa")) {
                error.put("error", "active_session");
                error.put("message", "Usuario cuenta con una sesión activa");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            error.put("error", "invalid_credentials");
            error.put("message", "Usuario o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "authentication_error");
            error.put("message", "Error en el proceso de autenticación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para renovar tokens (PRD 3.2)
     * Valida refresh token y genera nuevo access token
     * 
     * @param requestBody Objeto con refresh token
     * @param httpRequest HTTP request para obtener IP y User-Agent
     * @return TokenResponse con nuevo access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> requestBody, HttpServletRequest httpRequest) {
        try {
            String refreshToken = requestBody.get("refreshToken");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "missing_refresh_token");
                error.put("message", "Refresh token es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            TokenResponse tokenResponse = authService.refreshToken(refreshToken, httpRequest);
            return ResponseEntity.ok(tokenResponse);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "invalid_refresh_token");
            error.put("message", "Refresh token inválido o expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "refresh_error");
            error.put("message", "Error al renovar el token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para cerrar sesión (PRD 3.3)
     * Invalida tokens del usuario
     * 
     * @param request Objeto con access y refresh tokens
     * @return Confirmación de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        try {
            String accessToken = request.get("accessToken");
            String refreshToken = request.get("refreshToken");
            
            authService.logout(accessToken, refreshToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sesión cerrada exitosamente");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "logout_error");
            error.put("message", "Error al cerrar sesión");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para validar token JWT (PRD 3.4)
     * Retorna información del usuario y roles si el token es válido
     * 
     * @param request HTTP request con header Authorization
     * @return Información del usuario y roles
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "missing_token");
                error.put("message", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String token = authHeader.substring(7);
            
            if (!jwtService.isTokenValid(token)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "invalid_token");
                error.put("message", "Token inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Extraer información del token
            String username = jwtService.extractUsername(token);
            Integer userId = jwtService.extractUserId(token);
            var roles = jwtService.extractRoles(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("usuario", username);
            response.put("userId", userId);
            response.put("roles", roles);
            response.put("validatedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "validation_error");
            error.put("message", "Error al validar el token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para obtener tiempo restante del token (PRD 3.10)
     * Extrae token del header Authorization y calcula tiempo hasta expiración
     * Retorna formato hh:mm:ss o 00:00:00 si ya expiró
     * 
     * @param request HTTP request con header Authorization
     * @return TokenTtlResponse con tiempo restante
     */
    @GetMapping("/token-ttl")
    public ResponseEntity<TokenTtlResponse> getTokenTtl(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenTtlResponse.missingToken());
            }

            String token = authHeader.substring(7);
            
            if (!jwtService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenTtlResponse.invalidToken());
            }

            // Calcular tiempo restante
            long remainingSeconds = jwtService.getTokenRemainingTime(token);
            
            if (remainingSeconds <= 0) {
                return ResponseEntity.ok(TokenTtlResponse.expired());
            }

            // Convertir segundos a formato hh:mm:ss
            long hours = remainingSeconds / 3600;
            long minutes = (remainingSeconds % 3600) / 60;
            long seconds = remainingSeconds % 60;
            
            String timeRemaining = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            
            return ResponseEntity.ok(TokenTtlResponse.success(timeRemaining, remainingSeconds));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenTtlResponse.invalid("Error al procesar la solicitud"));
        }
    }

    /**
     * Endpoint de salud para verificar que el servicio está activo
     * 
     * @return Estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ms-seguridad");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}