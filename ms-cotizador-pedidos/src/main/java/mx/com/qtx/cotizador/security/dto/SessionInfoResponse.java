package mx.com.qtx.cotizador.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO para mapear respuestas de información de sesiones desde ms-seguridad
 * Utilizado en la comunicación entre microservicios
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

    /**
     * Convierte esta respuesta a un objeto SessionInfo
     */
    public SessionInfo toSessionInfo() {
        if (isSuccessful()) {
            SessionInfo sessionInfo = new SessionInfo(sessionId, isActive, startTime, endTime);
            sessionInfo.setDuration(duration);
            sessionInfo.setTimestamp(timestamp);
            return sessionInfo;
        } else {
            return SessionInfo.error(sessionId, message);
        }
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