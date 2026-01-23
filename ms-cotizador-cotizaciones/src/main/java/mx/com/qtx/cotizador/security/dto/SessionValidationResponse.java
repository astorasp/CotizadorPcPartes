package mx.com.qtx.cotizador.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para mapear respuestas de validación de sesiones desde ms-seguridad
 * Utilizado en la comunicación entre microservicios
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

    public boolean isValid() {
        return isSuccessful() && isSessionActive();
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