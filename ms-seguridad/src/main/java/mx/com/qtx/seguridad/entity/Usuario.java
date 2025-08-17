package mx.com.qtx.seguridad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "usuario", indexes = {
    @Index(name = "idx_usuario_activo", columnList = "activo"),
    @Index(name = "idx_usuario_fecha_creacion", columnList = "fecha_creacion")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(name = "usuario", length = 100, nullable = false, unique = true)
    private String usuario;

    @NotNull
    @Size(min = 8, max = 255)
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RolAsignado> rolesAsignados;

    // Constructor por defecto requerido por JPA
    public Usuario() {
    }

    // Constructor para crear nuevo usuario
    public Usuario(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
        this.activo = true;
    }

    // Métodos de ciclo de vida JPA
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.fechaCreacion = now;
        this.fechaModificacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
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

    public Set<RolAsignado> getRolesAsignados() {
        return rolesAsignados;
    }

    public void setRolesAsignados(Set<RolAsignado> rolesAsignados) {
        this.rolesAsignados = rolesAsignados;
    }

    // Métodos de utilidad
    public boolean isActivo() {
        return activo != null && activo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    // equals y hashCode basados en el usuario (business key)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario1 = (Usuario) o;
        return usuario != null && usuario.equals(usuario1.usuario);
    }

    @Override
    public int hashCode() {
        return usuario != null ? usuario.hashCode() : 0;
    }

    // toString para debugging
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", usuario='" + usuario + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}