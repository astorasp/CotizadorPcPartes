package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO para respuesta del endpoint de vigencia del token (PRD 3.10)
 * Retorna el tiempo restante del token en formato hh:mm:ss
 */
public class TokenTtlResponse {

    @JsonProperty("timeRemaining")
    private String timeRemaining; // Formato hh:mm:ss

    @JsonProperty("expired")
    private Boolean expired;

    @JsonProperty("valid")
    private Boolean valid;

    @JsonProperty("remainingSeconds")
    private Long remainingSeconds;

    @JsonProperty("checkedAt")
    private LocalDateTime checkedAt;

    @JsonProperty("message")
    private String message;

    // Constructor por defecto
    public TokenTtlResponse() {
        this.checkedAt = LocalDateTime.now();
    }

    // Constructor para token válido con tiempo restante
    public TokenTtlResponse(String timeRemaining, Long remainingSeconds) {
        this();
        this.timeRemaining = timeRemaining;
        this.remainingSeconds = remainingSeconds;
        this.expired = remainingSeconds <= 0;
        this.valid = true;
    }

    // Constructor para token inválido
    public TokenTtlResponse(String message, Boolean valid) {
        this();
        this.message = message;
        this.valid = valid;
        this.expired = true;
        this.timeRemaining = "00:00:00";
        this.remainingSeconds = 0L;
    }

    // Getters y Setters
    public String getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Métodos de utilidad
    public boolean isExpired() {
        return expired != null && expired;
    }

    public boolean isValid() {
        return valid != null && valid;
    }

    public boolean hasTimeRemaining() {
        return remainingSeconds != null && remainingSeconds > 0;
    }

    // Métodos estáticos para crear respuestas específicas
    public static TokenTtlResponse success(String timeRemaining, Long remainingSeconds) {
        return new TokenTtlResponse(timeRemaining, remainingSeconds);
    }

    public static TokenTtlResponse expired() {
        TokenTtlResponse response = new TokenTtlResponse();
        response.setTimeRemaining("00:00:00");
        response.setRemainingSeconds(0L);
        response.setExpired(true);
        response.setValid(false);
        response.setMessage("Token expirado");
        return response;
    }

    public static TokenTtlResponse invalid(String message) {
        return new TokenTtlResponse(message, false);
    }

    public static TokenTtlResponse invalidToken() {
        return new TokenTtlResponse("Token inválido o malformado", false);
    }

    public static TokenTtlResponse missingToken() {
        return new TokenTtlResponse("Token no proporcionado", false);
    }

    // toString
    @Override
    public String toString() {
        return "TokenTtlResponse{" +
                "timeRemaining='" + timeRemaining + '\'' +
                ", expired=" + expired +
                ", valid=" + valid +
                ", remainingSeconds=" + remainingSeconds +
                ", checkedAt=" + checkedAt +
                ", message='" + message + '\'' +
                '}';
    }
}