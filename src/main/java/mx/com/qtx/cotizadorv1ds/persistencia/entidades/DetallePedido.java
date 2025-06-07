package mx.com.qtx.cotizadorv1ds.persistencia.entidades;

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
@Table(name = "codetalle_pedido")
public class DetallePedido implements Serializable {    
    
    private static final long serialVersionUID = 1L;
    
    // Llave primaria compuesta
    @EmbeddedId
    private DetallePedidoId id = new DetallePedidoId();
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    
    @Column(name = "total_cotizado")
    private BigDecimal totalCotizado;
    
    @ManyToOne
    @MapsId("idPedido") // Este campo se mapea a partir de la clave embebida
    @JoinColumn(name = "num_pedido")
    private Pedido pedido;
    
    @ManyToOne
    @JoinColumn(name = "id_componente")
    private Componente componente;
    
    // Constructores
    public DetallePedido() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public DetallePedidoId getId() {
        return id;
    }
    
    public void setId(DetallePedidoId id) {
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
    
    public BigDecimal getTotalCotizado() {
        return totalCotizado;
    }
    
    public void setTotalCotizado(BigDecimal totalCotizado) {
        this.totalCotizado = totalCotizado;
    }
    
    public Pedido getPedido() {
        return pedido;
    }
    
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
        DetallePedido that = (DetallePedido) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Clase interna para la llave compuesta
    @Embeddable
    public static class DetallePedidoId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "num_pedido")
        private Integer idPedido;
        
        @Column(name = "num_detalle")
        private Integer numDetalle;
        
        // Constructores
        public DetallePedidoId() {}
        
        public DetallePedidoId(Integer idPedido, Integer numDetalle) {
            this.idPedido = idPedido;
            this.numDetalle = numDetalle;
        }
        
        // Getters y setters
        public Integer getIdPedido() {
            return idPedido;
        }
        
        public void setIdPedido(Integer idPedido) {
            this.idPedido = idPedido;
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
            DetallePedidoId that = (DetallePedidoId) o;
            return Objects.equals(idPedido, that.idPedido) &&
                   Objects.equals(numDetalle, that.numDetalle);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(idPedido, numDetalle);
        }
    }
}
