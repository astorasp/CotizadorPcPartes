package mx.com.qtx.seguridad.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador de excepciones JWT para Spring Security
 * Maneja tanto errores de autenticación como de autorización
 * Retorna respuestas JSON estructuradas con información del error
 */
@Component
public class JwtExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtExceptionHandler.class);
    
    private final ObjectMapper objectMapper;

    public JwtExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Maneja errores de autenticación (401 Unauthorized)
     * Se ejecuta cuando no hay credenciales válidas
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        logger.warn("Error de autenticación para {}: {}", 
                   request.getRequestURI(), authException.getMessage());

        String errorType = determineAuthenticationErrorType(request, authException);
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            errorType,
            getAuthenticationErrorMessage(errorType),
            request.getRequestURI()
        );

        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
    }

    /**
     * Maneja errores de autorización (403 Forbidden)
     * Se ejecuta cuando hay credenciales válidas pero sin permisos suficientes
     */
    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        logger.warn("Error de autorización para {}: {}", 
                   request.getRequestURI(), accessDeniedException.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.FORBIDDEN,
            "access_denied",
            "No tiene permisos suficientes para acceder a este recurso",
            request.getRequestURI()
        );

        writeErrorResponse(response, HttpStatus.FORBIDDEN, errorResponse);
    }

    /**
     * Determina el tipo específico de error de autenticación
     */
    private String determineAuthenticationErrorType(HttpServletRequest request, 
                                                   AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null) {
            return "missing_token";
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            return "invalid_token_format";
        }
        
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return "empty_token";
        }
        
        // Si llegamos aquí, probablemente es un token inválido o expirado
        String exceptionMessage = authException.getMessage().toLowerCase();
        
        if (exceptionMessage.contains("expired")) {
            return "token_expired";
        }
        
        if (exceptionMessage.contains("malformed") || exceptionMessage.contains("invalid")) {
            return "token_invalid";
        }
        
        if (exceptionMessage.contains("signature")) {
            return "token_signature_invalid";
        }
        
        return "authentication_failed";
    }

    /**
     * Obtiene el mensaje de error apropiado según el tipo
     */
    private String getAuthenticationErrorMessage(String errorType) {
        return switch (errorType) {
            case "missing_token" -> "Token de autorización requerido";
            case "invalid_token_format" -> "Formato de token inválido. Use 'Bearer <token>'";
            case "empty_token" -> "Token de autorización vacío";
            case "token_expired" -> "Token expirado. Renueve su sesión";
            case "token_invalid" -> "Token inválido o malformado";
            case "token_signature_invalid" -> "Firma de token inválida";
            case "authentication_failed" -> "Error de autenticación";
            default -> "Credenciales inválidas";
        };
    }

    /**
     * Crea un objeto de respuesta de error estructurado
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, 
                                                   String errorType, 
                                                   String message, 
                                                   String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", errorType);
        errorResponse.put("errorType", errorType);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        
        return errorResponse;
    }

    /**
     * Escribe la respuesta de error en formato JSON
     */
    private void writeErrorResponse(HttpServletResponse response, 
                                   HttpStatus status, 
                                   Map<String, Object> errorResponse) throws IOException {
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Headers adicionales para seguridad
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        } catch (Exception e) {
            logger.error("Error al escribir respuesta JSON de error", e);
            // Fallback a respuesta simple
            response.getWriter().write("{\"error\":\"" + status.getReasonPhrase() + "\"}");
            response.getWriter().flush();
        }
    }

    /**
     * Método de utilidad para manejo de errores específicos de JWT
     * Puede ser usado por otros componentes del sistema
     */
    public void handleJwtException(HttpServletResponse response, 
                                  String errorType, 
                                  String message, 
                                  String path) throws IOException {
        
        HttpStatus status = "access_denied".equals(errorType) ? 
                           HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        
        Map<String, Object> errorResponse = createErrorResponse(status, errorType, message, path);
        writeErrorResponse(response, status, errorResponse);
    }

    /**
     * Método para crear respuestas de error personalizadas
     */
    public static Map<String, Object> createCustomErrorResponse(String errorType, 
                                                               String message, 
                                                               String details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("errorType", errorType);
        errorResponse.put("message", message);
        errorResponse.put("details", details);
        
        return errorResponse;
    }

    /**
     * Método para logging estructurado de errores de seguridad
     */
    public void logSecurityEvent(String eventType, 
                                String username, 
                                String ipAddress, 
                                String details) {
        logger.warn("SECURITY_EVENT - Type: {}, User: {}, IP: {}, Details: {}", 
                   eventType, username != null ? username : "anonymous", ipAddress, details);
    }
}