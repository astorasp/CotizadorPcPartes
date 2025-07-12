package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de cierre de sesiones
 * Utilizado en el endpoint de cierre de sesiones
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
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    // Constructor para respuesta exitosa
    public SessionCloseResponse(String sessionId, Boolean closed) {
        this();
        this.sessionId = sessionId;
        this.closed = closed;
    }

    // Constructor para respuesta exitosa con mensaje
    public SessionCloseResponse(String sessionId, Boolean closed, String message) {
        this(sessionId, closed);
        this.message = message;
    }

    // Constructor para respuesta de error
    public SessionCloseResponse(String sessionId, String message, Boolean success) {
        this();
        this.sessionId = sessionId;
        this.message = message;
        this.success = success;
        this.closed = false;
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

    // Métodos para construir respuestas específicas
    public static SessionCloseResponse closed(String sessionId) {
        return new SessionCloseResponse(sessionId, true, "Sesión cerrada exitosamente");
    }

    public static SessionCloseResponse alreadyClosed(String sessionId) {
        return new SessionCloseResponse(sessionId, true, "Sesión ya estaba cerrada");
    }

    public static SessionCloseResponse notFound(String sessionId) {
        return new SessionCloseResponse(sessionId, "Sesión no encontrada", false);
    }

    public static SessionCloseResponse error(String sessionId, String message) {
        return new SessionCloseResponse(sessionId, message, false);
    }

    public static SessionCloseResponse invalidId() {
        return new SessionCloseResponse(null, "ID de sesión inválido", false);
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