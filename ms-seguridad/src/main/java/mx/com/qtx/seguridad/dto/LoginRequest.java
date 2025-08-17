package mx.com.qtx.seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para solicitud de autenticación (login)
 * Contiene las credenciales del usuario para autenticación
 */
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres")
    @JsonProperty("usuario")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
    @JsonProperty("password")
    private String password;

    // Constructor por defecto requerido para deserialización JSON
    public LoginRequest() {
    }

    // Constructor con parámetros
    public LoginRequest(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    // Getters y Setters
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // toString sin exponer la contraseña
    @Override
    public String toString() {
        return "LoginRequest{" +
                "usuario='" + usuario + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }

    // equals y hashCode basados solo en el usuario (no incluir password)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginRequest)) return false;
        LoginRequest that = (LoginRequest) o;
        return usuario != null ? usuario.equals(that.usuario) : that.usuario == null;
    }

    @Override
    public int hashCode() {
        return usuario != null ? usuario.hashCode() : 0;
    }
}