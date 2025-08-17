package mx.com.qtx.seguridad.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para estandarizar respuestas de error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de JWT expirado
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(ExpiredJwtException ex) {
        logger.warn("Token JWT expirado: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "token_expired", "Token expirado");
    }

    /**
     * Maneja excepciones de JWT malformado
     */
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJwtException(MalformedJwtException ex) {
        logger.warn("Token JWT malformado: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "invalid_token", "Token malformado");
    }

    /**
     * Maneja excepciones de JWT no soportado
     */
    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedJwtException(UnsupportedJwtException ex) {
        logger.warn("Token JWT no soportado: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "invalid_token", "Token no soportado");
    }

    /**
     * Maneja excepciones generales de JWT
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        logger.warn("Error de JWT: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "invalid_token", "Token inválido");
    }

    /**
     * Crea una respuesta de error estructurada
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}