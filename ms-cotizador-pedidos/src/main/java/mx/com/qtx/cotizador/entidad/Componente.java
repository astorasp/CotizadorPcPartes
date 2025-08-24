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
 * Entidad representación de un componente de hardware
 * Sincronizada con ms-cotizador-componentes via eventos Kafka
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
    
    @ManyToOne
    @JoinColumn(name = "id_tipo_componente")
    private TipoComponente tipoComponente;
    
    @Column(name = "id_promocion")
    private Integer promocion;
    
    // Constructores
    public Componente() {
        // Constructor vacío requerido por JPA
    }
    
    public Componente(String id, String descripcion, String marca, String modelo) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.descripcion = descripcion;
    }
    
    // Getters y setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCapacidadAlm() {
        return capacidadAlm;
    }
    
    public void setCapacidadAlm(String capacidadAlm) {
        this.capacidadAlm = capacidadAlm;
    }
    
    public BigDecimal getCosto() {
        return costo;
    }
    
    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
    
    public String getMemoria() {
        return memoria;
    }
    
    public void setMemoria(String memoria) {
        this.memoria = memoria;
    }
    
    public BigDecimal getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }
    
    public Integer getPromocion() {
        return promocion;
    }

    public void setPromocion(Integer promocion) {
        this.promocion = promocion;
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
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
    public TipoComponente getTipoComponente() {
        return tipoComponente;
    }
    
    public void setTipoComponente(TipoComponente tipoComponente) {
        this.tipoComponente = tipoComponente;
    }
}