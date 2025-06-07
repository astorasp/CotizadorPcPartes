package mx.com.qtx.cotizadorv1ds.persistencia.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.io.Serializable;

@Entity
@Table(name = "cotarjeta_video")
@PrimaryKeyJoinColumn(name = "id_componente")
public class TarjetaVideo extends Componente implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "memoria")
    private String memoria;
    
    // Constructores
    public TarjetaVideo() {}
    
    public TarjetaVideo(String id, String descripcion, String memoria) {
        super();
        this.setId(id);
        this.setDescripcion(descripcion);
        this.memoria = memoria;
    }
    
    // Getters y setters
    public String getMemoria() {
        return memoria;
    }
    
    public void setMemoria(String memoria) {
        this.memoria = memoria;
    }
}
