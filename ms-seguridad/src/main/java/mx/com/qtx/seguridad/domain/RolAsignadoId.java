package mx.com.qtx.seguridad.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RolAsignadoId implements Serializable {

    private Integer idUsuario;
    private Integer idRol;

    // Constructor por defecto requerido por JPA
    public RolAsignadoId() {
    }

    // Constructor con parámetros
    public RolAsignadoId(Integer idUsuario, Integer idRol) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
    }

    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    // equals y hashCode son obligatorios para composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolAsignadoId)) return false;
        RolAsignadoId that = (RolAsignadoId) o;
        return Objects.equals(idUsuario, that.idUsuario) && 
               Objects.equals(idRol, that.idRol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idRol);
    }

    // toString para debugging
    @Override
    public String toString() {
        return "RolAsignadoId{" +
                "idUsuario=" + idUsuario +
                ", idRol=" + idRol +
                '}';
    }
}