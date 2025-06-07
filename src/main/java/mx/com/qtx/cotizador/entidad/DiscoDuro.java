package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "codisco_duro")
@PrimaryKeyJoinColumn(name = "id_componente")
public class DiscoDuro extends Componente {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "capacidad_alm")
    private String capacidadAlm;
    
    // Constructores
    public DiscoDuro() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public String getCapacidadAlm() {
        return capacidadAlm;
    }
    
    public void setCapacidadAlm(String capacidadAlm) {
        this.capacidadAlm = capacidadAlm;
    }
}
