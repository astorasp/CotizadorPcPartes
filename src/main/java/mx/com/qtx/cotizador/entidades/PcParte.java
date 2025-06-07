package mx.com.qtx.cotizador.persistencia.entidades;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "copc_parte")
@IdClass(PcParte.PcPartesId.class)
public class PcParte implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "id_pc")
    private String idPc;
    
    @Id
    @Column(name = "id_componente")
    private String idComponente;
    
    @ManyToOne
    @JoinColumn(name = "id_componente", insertable = false, updatable = false)
    private Componente componente;
    
    // Constructores
    public PcParte() {
        // Constructor vacío requerido por JPA
    }
    
    public PcParte(String idPc, String idComponente) {
        this.idPc = idPc;
        this.idComponente = idComponente;
    }
    
    // Getters y setters
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

    public Componente getComponente() {
        return componente;
    }
    
    public void setComponente(Componente componente) {
        this.componente = componente;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PcParte pcPartes = (PcParte) o;
        return Objects.equals(idPc, pcPartes.idPc) && 
               Objects.equals(idComponente, pcPartes.idComponente);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idPc, idComponente);
    }
    
    // Clase para la llave compuesta
    public static class PcPartesId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String idPc;
        private String idComponente;
        
        public PcPartesId() {
            // Constructor vacío requerido por JPA
        }
        
        public PcPartesId(String idPc, String idComponente) {
            this.idPc = idPc;
            this.idComponente = idComponente;
        }
        
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
            PcPartesId that = (PcPartesId) o;
            return Objects.equals(idPc, that.idPc) && 
                   Objects.equals(idComponente, that.idComponente);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(idPc, idComponente);
        }
    }
}
