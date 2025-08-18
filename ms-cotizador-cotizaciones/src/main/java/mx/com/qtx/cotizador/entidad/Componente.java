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
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "precio")
    private BigDecimal precio;
    
    @Column(name = "marca")
    private String marca;
    
    @Column(name = "modelo")
    private String modelo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_componente")
    private TipoComponente tipoComponente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    // Constructores
    public Componente() {}
    
    public Componente(String id, String descripcion, BigDecimal precio, String marca, String modelo) {
        this.id = id;
        this.descripcion = descripcion;
        this.precio = precio;
        this.marca = marca;
        this.modelo = modelo;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public TipoComponente getTipoComponente() { return tipoComponente; }
    public void setTipoComponente(TipoComponente tipoComponente) { this.tipoComponente = tipoComponente; }
    
    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}