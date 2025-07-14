package mx.com.qtx.seguridad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "rol_asignado", indexes = {
    @Index(name = "idx_rol_asignado_usuario", columnList = "id_usuario"),
    @Index(name = "idx_rol_asignado_rol", columnList = "id_rol"),
    @Index(name = "idx_rol_asignado_activo", columnList = "activo"),
    @Index(name = "idx_rol_asignado_fecha_creacion", columnList = "fecha_creacion")
})
public class RolAsignado {

    @EmbeddedId
    private RolAsignadoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol", referencedColumnName = "id")
    private Rol rol;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    // Constructor por defecto requerido por JPA
    public RolAsignado() {
    }

    // Constructor para crear nueva asignación
    public RolAsignado(Usuario usuario, Rol rol) {
        this.id = new RolAsignadoId(usuario.getId(), rol.getId());
        this.usuario = usuario;
        this.rol = rol;
        this.activo = true;
    }

    // Constructor con parámetros completos
    public RolAsignado(RolAsignadoId id, Usuario usuario, Rol rol) {
        this.id = id;
        this.usuario = usuario;
        this.rol = rol;
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
    public RolAsignadoId getId() {
        return id;
    }

    public void setId(RolAsignadoId id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (this.id == null) {
            this.id = new RolAsignadoId();
        }
        this.id.setIdUsuario(usuario != null ? usuario.getId() : null);
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
        if (this.id == null) {
            this.id = new RolAsignadoId();
        }
        this.id.setIdRol(rol != null ? rol.getId() : null);
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

    // equals y hashCode basados en el ID composite
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolAsignado)) return false;
        RolAsignado that = (RolAsignado) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // toString para debugging
    @Override
    public String toString() {
        return "RolAsignado{" +
                "id=" + id +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}