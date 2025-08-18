package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "cotipo_componente")
public class TipoComponente {
    
    @Id
    private Short id;
    
    @Column(name = "nombre")
    private String nombre;
    
    @OneToMany(mappedBy = "tipoComponente", cascade = CascadeType.ALL)
    private List<Componente> componentes = new ArrayList<>();
    
    // Constructores
    public TipoComponente() {}
    
    // Getters y setters
    public Short getId() {
        return id;
    }
    
    public void setId(Short id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
