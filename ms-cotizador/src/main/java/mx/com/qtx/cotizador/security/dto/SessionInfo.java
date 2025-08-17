package mx.com.qtx.cotizador.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para información de sesión obtenida desde ms-seguridad
 * Contiene solo los campos necesarios para el ms-cotizador (no sensibles)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionInfo {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("startTime")
    private LocalDateTime startTime;

    @JsonProperty("endTime")
    private LocalDateTime endTime;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Constructor por defecto
    public SessionInfo() {
    }

    // Constructor para respuesta exitosa
    public SessionInfo(String sessionId, Boolean isActive, LocalDateTime startTime, LocalDateTime endTime) {
        this.sessionId = sessionId;
        this.isActive = isActive;
        this.startTime = startTime;
        this.endTime = endTime;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor para respuesta de error
    public SessionInfo(String sessionId, String message, Boolean success) {
        this.sessionId = sessionId;
        this.message = message;
        this.success = success;
        this.isActive = false;
        this.timestamp = LocalDateTime.now();
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
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

    // Métodos estáticos para crear instancias
    public static SessionInfo fromResponse(String sessionId, Boolean isActive, LocalDateTime startTime, LocalDateTime endTime) {
        return new SessionInfo(sessionId, isActive, startTime, endTime);
    }

    public static SessionInfo error(String sessionId, String message) {
        return new SessionInfo(sessionId, message, false);
    }

    public static SessionInfo notFound(String sessionId) {
        return new SessionInfo(sessionId, "Sesión no encontrada", false);
    }

    @Override
    public String toString() {
        return "SessionInfo{" +
                "sessionId='" + sessionId + '\'' +
                ", isActive=" + isActive +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration='" + duration + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}