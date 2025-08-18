package mx.com.qtx.cotizador.entidad;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad que representa el detalle de una cotización
 */
@Entity
@Table(name = "codetalle_cotizacion")
public class DetalleCotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    
    @Column(name = "subtotal")
    private BigDecimal subtotal;
    
    @ManyToOne
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;
    
    @ManyToOne
    @JoinColumn(name = "componente_id")
    private Componente componente;
    
    // Constructores
    public DetalleCotizacion() {
        // Constructor vacío requerido por JPA
    }
    
    public DetalleCotizacion(Integer cantidad, BigDecimal precioUnitario, Componente componente) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.componente = componente;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
    
    // Getters y setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public Cotizacion getCotizacion() {
        return cotizacion;
    }
    
    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }
    
    public Componente getComponente() {
        return componente;
    }
    
    public void setComponente(Componente componente) {
        this.componente = componente;
    }
    
    /**
     * Método para obtener el ID del componente asociado como String
     * @return ID del componente como String o null si no hay componente asociado
     */
    public String getIdComponente() {
        return this.componente != null ? this.componente.getId().toString() : null;
    }
}