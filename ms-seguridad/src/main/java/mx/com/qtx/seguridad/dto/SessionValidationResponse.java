package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de validación de sesiones
 * Utilizado en el endpoint de validación de sesiones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionValidationResponse {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("success")
    private Boolean success;

    // Constructor por defecto
    public SessionValidationResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    // Constructor para respuesta exitosa
    public SessionValidationResponse(String sessionId, Boolean isActive) {
        this();
        this.sessionId = sessionId;
        this.isActive = isActive;
    }

    // Constructor para respuesta exitosa con mensaje
    public SessionValidationResponse(String sessionId, Boolean isActive, String message) {
        this(sessionId, isActive);
        this.message = message;
    }

    // Constructor para respuesta de error
    public SessionValidationResponse(String sessionId, String message, Boolean success) {
        this();
        this.sessionId = sessionId;
        this.message = message;
        this.success = success;
        this.isActive = false;
    }

    // Getters y Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    // Métodos de utilidad
    public boolean isSuccessful() {
        return success != null && success;
    }

    public boolean isSessionActive() {
        return isActive != null && isActive;
    }

    // Métodos para construir respuestas específicas
    public static SessionValidationResponse valid(String sessionId) {
        return new SessionValidationResponse(sessionId, true, "Sesión válida y activa");
    }

    public static SessionValidationResponse invalid(String sessionId) {
        return new SessionValidationResponse(sessionId, false, "Sesión inválida o inactiva");
    }

    public static SessionValidationResponse notFound(String sessionId) {
        return new SessionValidationResponse(sessionId, "Sesión no encontrada", false);
    }

    public static SessionValidationResponse error(String sessionId, String message) {
        return new SessionValidationResponse(sessionId, message, false);
    }

    public static SessionValidationResponse invalidId() {
        return new SessionValidationResponse(null, "ID de sesión inválido", false);
    }

    @Override
    public String toString() {
        return "SessionValidationResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", isActive=" + isActive +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", success=" + success +
                '}';
    }
}