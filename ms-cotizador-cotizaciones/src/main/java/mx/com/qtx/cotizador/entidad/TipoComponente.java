package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.*;

/**
 * Entidad JPA para tipos de componentes.
 */
@Entity
@Table(name = "cotipo_componente")
public class TipoComponente {
    
    @Id
    private Short id;
    
    @Column(name = "nombre")
    private String nombre;
    
    // Constructores
    public TipoComponente() {}
    
    public TipoComponente(Short id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}