package mx.com.qtx.cotizadorv1ds.persistencia.entidades;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "copromocion")
public class Promocion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Integer idPromocion;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "vigencia_desde")
    private LocalDate vigenciaDesde;
    
    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;
    
    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL)
    private List<DetallePromocion> detalles = new ArrayList<>();
    
    @OneToMany(mappedBy = "promocion")
    private List<Componente> componentes = new ArrayList<>();
    
    // Constructores
    public Promocion() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public Integer getIdPromocion() {
        return idPromocion;
    }
    
    public void setIdPromocion(Integer idPromocion) {
        this.idPromocion = idPromocion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public LocalDate getVigenciaDesde() {
        return vigenciaDesde;
    }
    
    public void setVigenciaDesde(LocalDate vigenciaDesde) {
        this.vigenciaDesde = vigenciaDesde;
    }
    
    public LocalDate getVigenciaHasta() {
        return vigenciaHasta;
    }
    
    public void setVigenciaHasta(LocalDate vigenciaHasta) {
        this.vigenciaHasta = vigenciaHasta;
    }
    
    public List<DetallePromocion> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePromocion> detalles) {
        this.detalles = detalles;
    }
    
    public List<Componente> getComponentes() {
        return componentes;
    }
    
    public void setComponentes(List<Componente> componentes) {
        this.componentes = componentes;
    }
    
    // Método helper para agregar un detalle
    public void addDetalle(DetallePromocion detalle) {
        detalles.add(detalle);
        detalle.setPromocion(this);
    }
} 