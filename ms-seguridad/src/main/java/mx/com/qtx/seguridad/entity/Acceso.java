package mx.com.qtx.seguridad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "acceso", indexes = {
    @Index(name = "idx_acceso_id_sesion", columnList = "id_sesion"),
    @Index(name = "idx_acceso_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_acceso_activo", columnList = "activo"),
    @Index(name = "idx_acceso_fecha_inicio", columnList = "fecha_inicio"),
    @Index(name = "idx_acceso_fecha_fin", columnList = "fecha_fin")
})
public class Acceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "id_sesion", length = 255, nullable = false, unique = true)
    private String idSesion;

    @NotNull
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    // Relación ManyToOne con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", insertable = false, updatable = false)
    private mx.com.qtx.seguridad.entity.Usuario usuario;

    // Constructor por defecto requerido por JPA
    public Acceso() {
    }

    // Constructor para crear nuevo acceso
    public Acceso(String idSesion, Integer usuarioId) {
        this.idSesion = idSesion;
        this.usuarioId = usuarioId;
        this.activo = true;
        this.fechaInicio = LocalDateTime.now();
    }

    // Constructor completo
    public Acceso(String idSesion, Integer usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.idSesion = idSesion;
        this.usuarioId = usuarioId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = true;
    }

    // Métodos de ciclo de vida JPA
    @PrePersist
    protected void onCreate() {
        if (this.fechaInicio == null) {
            this.fechaInicio = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(String idSesion) {
        this.idSesion = idSesion;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public mx.com.qtx.seguridad.entity.Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(mx.com.qtx.seguridad.entity.Usuario usuario) {
        this.usuario = usuario;
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
        if (this.fechaFin == null) {
            this.fechaFin = LocalDateTime.now();
        }
    }

    public boolean isVencido() {
        return fechaFin != null && fechaFin.isBefore(LocalDateTime.now());
    }

    public boolean isSesionVigente() {
        return isActivo() && !isVencido();
    }

    public void cerrarSesion() {
        this.activo = false;
        this.fechaFin = LocalDateTime.now();
    }

    // equals y hashCode basados en idSesion (business key)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Acceso)) return false;
        Acceso acceso = (Acceso) o;
        return idSesion != null && idSesion.equals(acceso.idSesion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSesion);
    }

    // toString para debugging
    @Override
    public String toString() {
        return "Acceso{" +
                "id=" + id +
                ", activo=" + activo +
                ", fechaFin=" + fechaFin +
                ", fechaInicio=" + fechaInicio +
                ", idSesion='" + idSesion + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}