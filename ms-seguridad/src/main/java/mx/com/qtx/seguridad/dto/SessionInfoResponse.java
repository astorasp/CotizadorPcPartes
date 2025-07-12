package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de información de sesiones
 * Utilizado en el endpoint de información de sesiones (sin datos sensibles)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionInfoResponse {

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

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("success")
    private Boolean success;

    // Constructor por defecto
    public SessionInfoResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    // Constructor para respuesta exitosa
    public SessionInfoResponse(String sessionId, Boolean isActive, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.sessionId = sessionId;
        this.isActive = isActive;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = calculateDuration(startTime, endTime);
    }

    // Constructor para respuesta de error
    public SessionInfoResponse(String sessionId, String message, Boolean success) {
        this();
        this.sessionId = sessionId;
        this.message = message;
        this.success = success;
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
        this.duration = calculateDuration(startTime, this.endTime);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.duration = calculateDuration(this.startTime, endTime);
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return null;
        }
        
        LocalDateTime endTime = end != null ? end : LocalDateTime.now();
        long seconds = java.time.Duration.between(start, endTime).getSeconds();
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    // Métodos para construir respuestas específicas
    public static SessionInfoResponse found(String sessionId, Boolean isActive, LocalDateTime startTime, LocalDateTime endTime) {
        return new SessionInfoResponse(sessionId, isActive, startTime, endTime);
    }

    public static SessionInfoResponse notFound(String sessionId) {
        return new SessionInfoResponse(sessionId, "Sesión no encontrada", false);
    }

    public static SessionInfoResponse error(String sessionId, String message) {
        return new SessionInfoResponse(sessionId, message, false);
    }

    public static SessionInfoResponse invalidId() {
        return new SessionInfoResponse(null, "ID de sesión inválido", false);
    }

    @Override
    public String toString() {
        return "SessionInfoResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", isActive=" + isActive +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration='" + duration + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", success=" + success +
                '}';
    }
}