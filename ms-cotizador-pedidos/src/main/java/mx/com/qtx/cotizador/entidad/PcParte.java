package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.*;

/**
 * Entidad que representa la relación entre PC y sus componentes en ms-cotizador-pedidos.
 * Mapea la tabla copc_parte en la base de datos.
 */
@Entity
@Table(name = "copc_parte")
public class PcParte {

    @EmbeddedId
    private PcParteId id;

    // Relaciones opcionales para facilitar consultas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_componente", insertable = false, updatable = false)
    private Componente componente;

    // Constructor por defecto
    public PcParte() {}

    public PcParte(String idPc, String idComponente) {
        this.id = new PcParteId(idPc, idComponente);
    }

    public PcParte(PcParteId id) {
        this.id = id;
    }

    // Getters y Setters
    public PcParteId getId() {
        return id;
    }

    public void setId(PcParteId id) {
        this.id = id;
    }

    public Componente getComponente() {
        return componente;
    }

    public void setComponente(Componente componente) {
        this.componente = componente;
    }

    // Métodos de conveniencia
    public String getIdPc() {
        return id != null ? id.getIdPc() : null;
    }

    public String getIdComponente() {
        return id != null ? id.getIdComponente() : null;
    }
}