package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad JPA para componentes.
 * Incluye relación con promociones para cálculos de cotización.
 */
@Entity
@Table(name = "cocomponente")
public class Componente {
    
    @Id
    @Column(name = "id_componente")
    private String id;
    
    @Column(name = "capacidad_alm")
    private String capacidadAlm;
    
    @Column(name = "costo")
    private BigDecimal costo;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "marca")
    private String marca;
    
    @Column(name = "memoria")
    private String memoria;
    
    @Column(name = "modelo")
    private String modelo;
    
    @Column(name = "precio_base")
    private BigDecimal precioBase;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_componente")
    private TipoComponente tipoComponente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;
    
    // Constructores
    public Componente() {}
    
    public Componente(String id, String descripcion, BigDecimal costo, BigDecimal precioBase, String marca, String modelo) {
        this.id = id;
        this.descripcion = descripcion;
        this.costo = costo;
        this.precioBase = precioBase;
        this.marca = marca;
        this.modelo = modelo;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCapacidadAlm() { return capacidadAlm; }
    public void setCapacidadAlm(String capacidadAlm) { this.capacidadAlm = capacidadAlm; }
    
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getMemoria() { return memoria; }
    public void setMemoria(String memoria) { this.memoria = memoria; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }
    
    public TipoComponente getTipoComponente() { return tipoComponente; }
    public void setTipoComponente(TipoComponente tipoComponente) { this.tipoComponente = tipoComponente; }
    
    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
}