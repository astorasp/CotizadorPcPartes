package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para respuestas de autenticación con tokens JWT
 * Utilizado en login y refresh token endpoints
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("expiresIn")
    private Long expiresIn; // En segundos

    @JsonProperty("refreshExpiresIn")
    private Long refreshExpiresIn; // En segundos

    @JsonProperty("issuedAt")
    private LocalDateTime issuedAt;

    @JsonProperty("usuario")
    private Map<String, Object> usuario;

    @JsonProperty("message")
    private String message;

    // Constructor por defecto
    public TokenResponse() {
        this.success = true;
        this.tokenType = "Bearer";
        this.issuedAt = LocalDateTime.now();
    }

    // Constructor para respuesta exitosa completa
    public TokenResponse(String accessToken, String refreshToken, Long expiresIn, 
                        Long refreshExpiresIn, Map<String, Object> usuario) {
        this();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.usuario = usuario;
    }

    // Constructor para refresh token (solo access token)
    public TokenResponse(String accessToken, Long expiresIn) {
        this();
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    // Constructor para respuesta de error
    public TokenResponse(String message, Boolean success) {
        this();
        this.message = message;
        this.success = success;
    }

    // Getters y Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(Long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Map<String, Object> getUsuario() {
        return usuario;
    }

    public void setUsuario(Map<String, Object> usuario) {
        this.usuario = usuario;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Métodos de utilidad
    public boolean isSuccessful() {
        return success != null && success;
    }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    public boolean hasUsuario() {
        return usuario != null && !usuario.isEmpty();
    }

    // Métodos para construir respuestas específicas
    public static TokenResponse success(String accessToken, String refreshToken, 
                                      Long expiresIn, Long refreshExpiresIn, 
                                      Map<String, Object> usuario) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, refreshExpiresIn, usuario);
    }

    public static TokenResponse refreshSuccess(String accessToken, Long expiresIn) {
        return new TokenResponse(accessToken, expiresIn);
    }

    public static TokenResponse error(String message) {
        return new TokenResponse(message, false);
    }

    public static TokenResponse logout() {
        TokenResponse response = new TokenResponse();
        response.setMessage("Logout exitoso");
        response.setSuccess(true);
        return response;
    }

    // toString sin exponer tokens completos
    @Override
    public String toString() {
        return "TokenResponse{" +
                "success=" + success +
                ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : "null") + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", refreshExpiresIn=" + refreshExpiresIn +
                ", issuedAt=" + issuedAt +
                ", usuario=" + usuario +
                ", message='" + message + '\'' +
                '}';
    }
}