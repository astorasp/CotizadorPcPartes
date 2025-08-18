package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.*;

/**
 * Entidad JPA para tipos de componentes.
 */
@Entity
@Table(name = "cotipo_componente")
public class TipoComponente {
    
    @Id
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    // Constructores
    public TipoComponente() {}
    
    public TipoComponente(Integer id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}