package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta para la entidad PcParte.
 * Representa la relación many-to-many entre PC y Componente.
 */
@Embeddable
public class PcParteId implements Serializable {

    @Column(name = "id_pc", length = 50)
    private String idPc;

    @Column(name = "id_componente", length = 50) 
    private String idComponente;

    // Constructor por defecto
    public PcParteId() {}

    public PcParteId(String idPc, String idComponente) {
        this.idPc = idPc;
        this.idComponente = idComponente;
    }

    // Getters y Setters
    public String getIdPc() {
        return idPc;
    }

    public void setIdPc(String idPc) {
        this.idPc = idPc;
    }

    public String getIdComponente() {
        return idComponente;
    }

    public void setIdComponente(String idComponente) {
        this.idComponente = idComponente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PcParteId pcParteId = (PcParteId) o;
        return Objects.equals(idPc, pcParteId.idPc) &&
               Objects.equals(idComponente, pcParteId.idComponente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPc, idComponente);
    }
}