package mx.com.qtx.cotizadorv1ds.persistencia.entidades;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "codetalle_promocion")
public class DetallePromocion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_promocion")
    private Integer idDetallePromocion;
    
    @Column(name = "es_base")
    private Boolean esBase;
    
    @Column(name = "llevent")
    private Integer llevent;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "paguen")
    private Integer paguen;
    
    @Column(name = "porc_dcto_plano")
    private Double porcDctoPlano;
    
    @Column(name = "tipo_prom_acumulable")
    private String tipoPromAcumulable;
    
    @Column(name = "tipo_prom_base")
    private String tipoPromBase;
    
    @ManyToOne
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;
    
    @OneToMany(mappedBy = "detallePromocion", cascade = CascadeType.ALL)
    private List<DetallePromDsctoXCant> descuentosPorCantidad = new ArrayList<>();
    
    // Constructores
    public DetallePromocion() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public Integer getIdDetallePromocion() {
        return idDetallePromocion;
    }
    
    public void setIdDetallePromocion(Integer idDetallePromocion) {
        this.idDetallePromocion = idDetallePromocion;
    }
    
    public Boolean getEsBase() {
        return esBase;
    }
    
    public void setEsBase(Boolean esBase) {
        this.esBase = esBase;
    }
    
    public Integer getLlevent() {
        return llevent;
    }
    
    public void setLlevent(Integer llevent) {
        this.llevent = llevent;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getPaguen() {
        return paguen;
    }
    
    public void setPaguen(Integer paguen) {
        this.paguen = paguen;
    }
    
    public Double getPorcDctoPlano() {
        return porcDctoPlano;
    }
    
    public void setPorcDctoPlano(Double porcDctoPlano) {
        this.porcDctoPlano = porcDctoPlano;
    }
    
    public String getTipoPromAcumulable() {
        return tipoPromAcumulable;
    }
    
    public void setTipoPromAcumulable(String tipoPromAcumulable) {
        this.tipoPromAcumulable = tipoPromAcumulable;
    }
    
    public String getTipoPromBase() {
        return tipoPromBase;
    }
    
    public void setTipoPromBase(String tipoPromBase) {
        this.tipoPromBase = tipoPromBase;
    }
    
    public Promocion getPromocion() {
        return promocion;
    }
    
    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }
    
    public List<DetallePromDsctoXCant> getDescuentosPorCantidad() {
        return descuentosPorCantidad;
    }
    
    public void setDescuentosPorCantidad(List<DetallePromDsctoXCant> descuentosPorCantidad) {
        this.descuentosPorCantidad = descuentosPorCantidad;
    }
    
    // Método helper para agregar un descuento por cantidad
    public void addDescuentoPorCantidad(DetallePromDsctoXCant descuento) {
        descuentosPorCantidad.add(descuento);
        descuento.setDetallePromocion(this);
    }
} 