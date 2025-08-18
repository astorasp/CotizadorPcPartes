package mx.com.qtx.cotizador.entidad;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Embeddable;

@Entity
@Table(name = "codetalle_cotizacion")
public class DetalleCotizacion implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Llave primaria compuesta
    @EmbeddedId
    private DetalleCotizacionId id = new DetalleCotizacionId();
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "precio_base")
    private BigDecimal precioBase;
    
    @ManyToOne
    @MapsId("folio") // Este campo se mapea a partir de la clave embebida
    @JoinColumn(name = "folio")
    private Cotizacion cotizacion;
    
    @ManyToOne
    @JoinColumn(name = "id_componente", nullable = true)
    private Componente componente;
    
    // Constructores
    public DetalleCotizacion() {
        // Constructor vacío necesario para JPA
    }
    
    // Getters y setters
    public DetalleCotizacionId getId() {
        return id;
    }
    
    public void setId(DetalleCotizacionId id) {
        this.id = id;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleCotizacion that = (DetalleCotizacion) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Clase interna para la llave compuesta
    @Embeddable
    public static class DetalleCotizacionId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "folio")
        private Integer folio;
        
        @Column(name = "num_detalle")
        private Integer numDetalle;
        
        // Constructores
        public DetalleCotizacionId() {}
        
        public DetalleCotizacionId(Integer folio, Integer numDetalle) {
            this.folio = folio;
            this.numDetalle = numDetalle;
        }
        
        // Getters y setters
        public Integer getFolio() {
            return folio;
        }
        
        public void setFolio(Integer folio) {
            this.folio = folio;
        }
        
        public Integer getNumDetalle() {
            return numDetalle;
        }
        
        public void setNumDetalle(Integer numDetalle) {
            this.numDetalle = numDetalle;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetalleCotizacionId that = (DetalleCotizacionId) o;
            return Objects.equals(folio, that.folio) &&
                   Objects.equals(numDetalle, that.numDetalle);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(folio, numDetalle);
        }
    }
}
