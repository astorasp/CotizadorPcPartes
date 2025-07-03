package mx.com.qtx.seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para operaciones CRUD de usuarios
 * Utilizado tanto para requests como responses
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDto {

    @JsonProperty("id")
    private Integer id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres")
    @JsonProperty("usuario")
    private String usuario;

    // Password solo para creación/actualización, no se retorna en responses
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
    @JsonProperty("password")
    private String password;

    @NotNull(message = "El estado activo es obligatorio")
    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fechaCreacion")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fechaModificacion")
    private LocalDateTime fechaModificacion;

    @JsonProperty("roles")
    private List<String> roles;

    // Constructor por defecto
    public UsuarioDto() {
    }

    // Constructor para creación (sin ID ni fechas)
    public UsuarioDto(String usuario, String password, Boolean activo) {
        this.usuario = usuario;
        this.password = password;
        this.activo = activo;
    }

    // Constructor completo para responses
    public UsuarioDto(Integer id, String usuario, Boolean activo, 
                     LocalDateTime fechaCreacion, LocalDateTime fechaModificacion, 
                     List<String> roles) {
        this.id = id;
        this.usuario = usuario;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
        this.roles = roles;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // Métodos de utilidad
    public boolean isActivo() {
        return activo != null && activo;
    }

    public boolean hasPassword() {
        return password != null && !password.trim().isEmpty();
    }

    public boolean hasRoles() {
        return roles != null && !roles.isEmpty();
    }

    // Método para limpiar password de respuestas
    public UsuarioDto withoutPassword() {
        UsuarioDto dto = new UsuarioDto();
        dto.id = this.id;
        dto.usuario = this.usuario;
        dto.activo = this.activo;
        dto.fechaCreacion = this.fechaCreacion;
        dto.fechaModificacion = this.fechaModificacion;
        dto.roles = this.roles;
        // Intencionalmente no copiamos password
        return dto;
    }

    // toString sin exponer password
    @Override
    public String toString() {
        return "UsuarioDto{" +
                "id=" + id +
                ", usuario='" + usuario + '\'' +
                ", password=" + (password != null ? "'[PROTECTED]'" : "null") +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaModificacion=" + fechaModificacion +
                ", roles=" + roles +
                '}';
    }

    // equals y hashCode basados en usuario
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioDto)) return false;
        UsuarioDto that = (UsuarioDto) o;
        return usuario != null ? usuario.equals(that.usuario) : that.usuario == null;
    }

    @Override
    public int hashCode() {
        return usuario != null ? usuario.hashCode() : 0;
    }
}