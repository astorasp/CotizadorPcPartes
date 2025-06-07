package mx.com.qtx.cotizadorv1ds.persistencia.entidades;

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
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "dscto")
    private Double dscto;
    
    @ManyToOne
    @MapsId("numDetPromocion")
    @JoinColumn(name = "num_det_promocion")
    private DetallePromocion detallePromocion;
    
    // Constructores
    public DetallePromDsctoXCant() {
        // Constructor vacío requerido por JPA
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
    
    // Clase interna para la llave compuesta
    @Embeddable
    public static class DetallePromDsctoXCantId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "num_dscto")
        private Integer numDscto;
        
        @Column(name = "num_det_promocion")
        private Integer numDetPromocion;
        
        @Column(name = "num_promocion")
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
    }
} 