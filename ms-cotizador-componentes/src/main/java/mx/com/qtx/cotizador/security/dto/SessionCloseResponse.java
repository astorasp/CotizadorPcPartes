package mx.com.qtx.cotizador.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para mapear respuestas de cierre de sesiones desde ms-seguridad
 * Utilizado en la comunicación entre microservicios
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionCloseResponse {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("closed")
    private Boolean closed;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("success")
    private Boolean success;

    // Constructor por defecto
    public SessionCloseResponse() {
    }

    // Getters y Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
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

    public boolean isClosed() {
        return closed != null && closed;
    }

    @Override
    public String toString() {
        return "SessionCloseResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", closed=" + closed +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", success=" + success +
                '}';
    }
}