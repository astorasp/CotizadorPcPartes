package mx.com.qtx.cotizador.entidad;

import java.io.Serializable;
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
@Table(name = "codetalle_prom_dscto_x_cant")
public class DetallePromDsctoXCant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    private DetallePromDsctoXCantId id = new DetallePromDsctoXCantId();
    
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    @Column(name = "dscto", nullable = false)
    private Double dscto;
    
    @ManyToOne
    @MapsId("numDetPromocion")
    @JoinColumn(name = "num_det_promocion", nullable = false)
    private DetallePromocion detallePromocion;
    
    @ManyToOne
    @MapsId("numPromocion")
    @JoinColumn(name = "num_promocion", nullable = false)
    private Promocion promocion;
    
    // Constructores
    public DetallePromDsctoXCant() {
        // Constructor vacío requerido por JPA
    }
    
    public DetallePromDsctoXCant(Integer numDscto, Integer cantidad, Double dscto, 
                                Integer numPromocion, DetallePromocion detallePromocion) {
        this.id = new DetallePromDsctoXCantId(numDscto, detallePromocion.getIdDetallePromocion(), numPromocion);
        this.cantidad = cantidad;
        this.dscto = dscto;
        this.detallePromocion = detallePromocion;
    }
    
    // Getters y setters
    public DetallePromDsctoXCantId getId() {
        return id;
    }
    
    public void setId(DetallePromDsctoXCantId id) {
        this.id = id;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Double getDscto() {
        return dscto;
    }
    
    public void setDscto(Double dscto) {
        this.dscto = dscto;
    }
    
    public DetallePromocion getDetallePromocion() {
        return detallePromocion;
    }
    
    public void setDetallePromocion(DetallePromocion detallePromocion) {
        this.detallePromocion = detallePromocion;
        if (detallePromocion != null && this.id != null) {
            this.id.setNumDetPromocion(detallePromocion.getIdDetallePromocion());
        }
    }
    
    public Promocion getPromocion() {
        return promocion;
    }
    
    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
        if (promocion != null && this.id != null) {
            this.id.setNumPromocion(promocion.getIdPromocion());
        }
    }
    
    // Métodos de conveniencia para acceder a los IDs
    public Integer getNumDscto() {
        return id != null ? id.getNumDscto() : null;
    }
    
    public void setNumDscto(Integer numDscto) {
        if (this.id == null) {
            this.id = new DetallePromDsctoXCantId();
        }
        this.id.setNumDscto(numDscto);
    }
    
    public Integer getNumPromocion() {
        return id != null ? id.getNumPromocion() : null;
    }
    
    public void setNumPromocion(Integer numPromocion) {
        if (this.id == null) {
            this.id = new DetallePromDsctoXCantId();
        }
        this.id.setNumPromocion(numPromocion);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetallePromDsctoXCant that = (DetallePromDsctoXCant) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DetallePromDsctoXCant{" +
                "numDscto=" + getNumDscto() +
                ", cantidad=" + cantidad +
                ", dscto=" + dscto +
                ", numPromocion=" + getNumPromocion() +
                '}';
    }
    
    // Clase interna para la llave compuesta
    @Embeddable
    public static class DetallePromDsctoXCantId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "num_dscto", nullable = false)
        private Integer numDscto;
        
        @Column(name = "num_det_promocion", nullable = false)
        private Integer numDetPromocion;
        
        @Column(name = "num_promocion", nullable = false)
        private Integer numPromocion;
        
        // Constructores
        public DetallePromDsctoXCantId() {}
        
        public DetallePromDsctoXCantId(Integer numDscto, Integer numDetPromocion, Integer numPromocion) {
            this.numDscto = numDscto;
            this.numDetPromocion = numDetPromocion;
            this.numPromocion = numPromocion;
        }
        
        // Getters y setters
        public Integer getNumDscto() {
            return numDscto;
        }
        
        public void setNumDscto(Integer numDscto) {
            this.numDscto = numDscto;
        }
        
        public Integer getNumDetPromocion() {
            return numDetPromocion;
        }
        
        public void setNumDetPromocion(Integer numDetPromocion) {
            this.numDetPromocion = numDetPromocion;
        }
        
        public Integer getNumPromocion() {
            return numPromocion;
        }
        
        public void setNumPromocion(Integer numPromocion) {
            this.numPromocion = numPromocion;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetallePromDsctoXCantId that = (DetallePromDsctoXCantId) o;
            return Objects.equals(numDscto, that.numDscto) &&
                   Objects.equals(numDetPromocion, that.numDetPromocion) &&
                   Objects.equals(numPromocion, that.numPromocion);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(numDscto, numDetPromocion, numPromocion);
        }
        
        @Override
        public String toString() {
            return "DetallePromDsctoXCantId{" +
                    "numDscto=" + numDscto +
                    ", numDetPromocion=" + numDetPromocion +
                    ", numPromocion=" + numPromocion +
                    '}';
        }
    }
} 