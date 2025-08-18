package mx.com.qtx.cotizador.entidad;

import java.math.BigDecimal;

import jakarta.persistence.Entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Table(name = "cocomponente")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Componente.completo",
        attributeNodes = {
            @NamedAttributeNode("tipoComponente"),
            @NamedAttributeNode(value = "promocion", subgraph = "promocion-completa")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "promocion-completa",
                attributeNodes = {
                    @NamedAttributeNode(value = "detalles", subgraph = "detalles-completos")
                }
            ),
            @NamedSubgraph(
                name = "detalles-completos",
                attributeNodes = {
                    @NamedAttributeNode("descuentosPorCantidad")
                }
            )
        }
    )
})
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
    
    // Nota: Las relaciones con DetalleCotizacion y DetallePedido se manejan 
    // en los microservicios correspondientes (ms-cotizador-cotizaciones y ms-cotizador-pedidos)
    
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
    
    // Métodos para las relaciones con otros microservicios removidos
    // para mantener la independencia del microservicio de componentes
}
