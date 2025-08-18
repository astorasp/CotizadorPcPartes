package mx.com.qtx.cotizador.entidad;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "cocotizacion")
public class Cotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folio;
    
    @Column(name = "fecha")
    private String fecha;
    
    @Column(name = "impuestos")
    private BigDecimal impuestos;
    
    @Column(name = "subtotal")
    private BigDecimal subtotal;
    
    @Column(name = "total")
    private BigDecimal total;
    
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL)
    private List<DetalleCotizacion> detalles = new ArrayList<>();
    
    // Constructores
    public Cotizacion() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public String getFecha() {
        return fecha;
    }
    
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    
    public Integer getFolio() {
        return folio;
    }
    
    public void setFolio(Integer folio) {
        this.folio = folio;
    }
    
    public BigDecimal getImpuestos() {
        return impuestos;
    }
    
    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public List<DetalleCotizacion> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetalleCotizacion> detalles) {
        this.detalles = detalles;
    }
    
    // Método helper para agregar un detalle
    public void addDetalle(DetalleCotizacion detalle) {
        detalles.add(detalle);
        detalle.setCotizacion(this);
    }
}
