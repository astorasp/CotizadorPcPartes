package mx.com.qtx.cotizadorv1ds.persistencia.entidades;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Table(name = "cocomponente")
@Inheritance(strategy = InheritanceType.JOINED)
public class Componente {
    
    @Id
    @Column(name = "id_componente")
    private String id;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "marca")
    private String marca;
    
    @Column(name = "modelo")
    private String modelo;
    
    @Column(name = "costo")
    private BigDecimal costo;
    
    @Column(name = "precio_base")
    private BigDecimal precioBase;
    
    @Column(name = "capacidad_alm")
    private String capacidadAlm;
    
    @Column(name = "memoria")
    private String memoria;
    
    @ManyToOne
    @JoinColumn(name = "id_tipo_componente")
    private TipoComponente tipoComponente;
    
    @ManyToOne
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;
    
    @OneToMany(mappedBy = "componente")
    private List<DetalleCotizacion> detallesCotizacion = new ArrayList<>();
    
    @OneToMany(mappedBy = "componente")
    private List<DetallePedido> detallesPedido = new ArrayList<>();
    
    // Constructores
    public Componente() {
        // Constructor vacío necesario para JPA
    }
    
    // Getters y setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public BigDecimal getCosto() {
        return costo;
    }
    
    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
    
    public BigDecimal getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }
    
    public String getCapacidadAlm() {
        return capacidadAlm;
    }
    
    public void setCapacidadAlm(String capacidadAlm) {
        this.capacidadAlm = capacidadAlm;
    }
    
    public String getMemoria() {
        return memoria;
    }
    
    public void setMemoria(String memoria) {
        this.memoria = memoria;
    }
    
    public TipoComponente getTipoComponente() {
        return tipoComponente;
    }
    
    public void setTipoComponente(TipoComponente tipoComponente) {
        this.tipoComponente = tipoComponente;
    }
    
    public Promocion getPromocion() {
        return promocion;
    }
    
    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }
    
    public List<DetalleCotizacion> getDetallesCotizacion() {
        return detallesCotizacion;
    }
    
    public void setDetallesCotizacion(List<DetalleCotizacion> detallesCotizacion) {
        this.detallesCotizacion = detallesCotizacion;
    }
    
    public List<DetallePedido> getDetallesPedido() {
        return detallesPedido;
    }
    
    public void setDetallesPedido(List<DetallePedido> detallesPedido) {
        this.detallesPedido = detallesPedido;
    }
}
