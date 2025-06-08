package mx.com.qtx.cotizador.entidad;

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
    
    @Column(name = "es_base", nullable = false)
    private Boolean esBase;
    
    @Column(name = "llevent", nullable = false)
    private Integer llevent;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "paguen", nullable = false)
    private Integer paguen;
    
    @Column(name = "porc_dcto_plano", nullable = false)
    private Double porcDctoPlano;
    
    @Column(name = "tipo_prom_acumulable", length = 50)
    private String tipoPromAcumulable;
    
    @Column(name = "tipo_prom_base", length = 50)
    private String tipoPromBase;
    
    @ManyToOne
    @JoinColumn(name = "id_promocion", nullable = false)
    private Promocion promocion;
    
    @OneToMany(mappedBy = "detallePromocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePromDsctoXCant> descuentosPorCantidad = new ArrayList<>();
    
    // Constructores
    public DetallePromocion() {
        // Constructor vacío requerido por JPA
    }
    
    public DetallePromocion(Boolean esBase, Integer llevent, String nombre, Integer paguen, 
                           Double porcDctoPlano, Promocion promocion) {
        this.esBase = esBase;
        this.llevent = llevent;
        this.nombre = nombre;
        this.paguen = paguen;
        this.porcDctoPlano = porcDctoPlano;
        this.promocion = promocion;
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
    
    // Métodos helper para manejar la relación bidireccional
    public void addDescuentoPorCantidad(DetallePromDsctoXCant descuento) {
        descuentosPorCantidad.add(descuento);
        descuento.setDetallePromocion(this);
    }
    
    public void removeDescuentoPorCantidad(DetallePromDsctoXCant descuento) {
        descuentosPorCantidad.remove(descuento);
        descuento.setDetallePromocion(null);
    }
    
    @Override
    public String toString() {
        return "DetallePromocion{" +
                "idDetallePromocion=" + idDetallePromocion +
                ", nombre='" + nombre + '\'' +
                ", esBase=" + esBase +
                ", llevent=" + llevent +
                ", paguen=" + paguen +
                ", porcDctoPlano=" + porcDctoPlano +
                '}';
    }
} 