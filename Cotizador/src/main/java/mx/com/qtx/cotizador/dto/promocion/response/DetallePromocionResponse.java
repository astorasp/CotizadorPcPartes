package mx.com.qtx.cotizador.dto.promocion.response;

import java.util.List;

import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionAcumulable;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;

/**
 * DTO de respuesta para detalles de promoción.
 * 
 * Contiene toda la información de configuración de un detalle específico
 * de promoción, incluyendo parámetros y descripciones calculadas.
 */
public class DetallePromocionResponse {
    
    private Integer idDetalle;
    private String nombre;
    private Boolean esBase;
    private String descripcionTipo; // Descripción calculada del tipo
    
    // Campos para promoción base
    private TipoPromocionBase tipoBase;
    private Integer llevent; // Para NxM
    private Integer paguen;  // Para NxM
    
    // Campos para promoción acumulable
    private TipoPromocionAcumulable tipoAcumulable;
    private Double porcentajeDescuentoPlano;
    private List<EscalaDescuentoResponse> escalasDescuento;
    
    // Constructores
    public DetallePromocionResponse() {
        // Constructor vacío para Jackson
    }
    
    // Getters y setters
    public Integer getIdDetalle() {
        return idDetalle;
    }
    
    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Boolean getEsBase() {
        return esBase;
    }
    
    public void setEsBase(Boolean esBase) {
        this.esBase = esBase;
    }
    
    public String getDescripcionTipo() {
        return descripcionTipo;
    }
    
    public void setDescripcionTipo(String descripcionTipo) {
        this.descripcionTipo = descripcionTipo;
    }
    
    public TipoPromocionBase getTipoBase() {
        return tipoBase;
    }
    
    public void setTipoBase(TipoPromocionBase tipoBase) {
        this.tipoBase = tipoBase;
    }
    
    public Integer getLlevent() {
        return llevent;
    }
    
    public void setLlevent(Integer llevent) {
        this.llevent = llevent;
    }
    
    public Integer getPaguen() {
        return paguen;
    }
    
    public void setPaguen(Integer paguen) {
        this.paguen = paguen;
    }
    
    public TipoPromocionAcumulable getTipoAcumulable() {
        return tipoAcumulable;
    }
    
    public void setTipoAcumulable(TipoPromocionAcumulable tipoAcumulable) {
        this.tipoAcumulable = tipoAcumulable;
    }
    
    public Double getPorcentajeDescuentoPlano() {
        return porcentajeDescuentoPlano;
    }
    
    public void setPorcentajeDescuentoPlano(Double porcentajeDescuentoPlano) {
        this.porcentajeDescuentoPlano = porcentajeDescuentoPlano;
    }
    
    public List<EscalaDescuentoResponse> getEscalasDescuento() {
        return escalasDescuento;
    }
    
    public void setEscalasDescuento(List<EscalaDescuentoResponse> escalasDescuento) {
        this.escalasDescuento = escalasDescuento;
    }
    
    /**
     * Calcula una descripción detallada del tipo de promoción
     */
    public void calcularDescripcionTipo() {
        if (esBase != null && esBase) {
            calcularDescripcionBase();
        } else {
            calcularDescripcionAcumulable();
        }
    }
    
    private void calcularDescripcionBase() {
        if (tipoBase == null) {
            descripcionTipo = "Promoción base no configurada";
            return;
        }
        
        switch (tipoBase) {
            case SIN_DESCUENTO:
                descripcionTipo = "Promoción regular sin descuento";
                break;
            case NXM:
                if (llevent != null && paguen != null) {
                    double porcentaje = ((double)(llevent - paguen) / llevent) * 100;
                    descripcionTipo = String.format("Compra %d, Paga %d (%.1f%% de descuento)", 
                                                   llevent, paguen, porcentaje);
                } else {
                    descripcionTipo = "Promoción NxM sin configurar";
                }
                break;
            default:
                descripcionTipo = "Tipo de promoción base desconocido";
        }
    }
    
    private void calcularDescripcionAcumulable() {
        if (tipoAcumulable == null) {
            descripcionTipo = "Promoción acumulable no configurada";
            return;
        }
        
        switch (tipoAcumulable) {
            case DESCUENTO_PLANO:
                if (porcentajeDescuentoPlano != null) {
                    descripcionTipo = String.format("Descuento plano del %.1f%%", 
                                                   porcentajeDescuentoPlano);
                } else {
                    descripcionTipo = "Descuento plano sin configurar";
                }
                break;
            case DESCUENTO_POR_CANTIDAD:
                if (escalasDescuento != null && !escalasDescuento.isEmpty()) {
                    descripcionTipo = String.format("Descuento por cantidad (%d escalas)", 
                                                   escalasDescuento.size());
                } else {
                    descripcionTipo = "Descuento por cantidad sin escalas";
                }
                break;
            default:
                descripcionTipo = "Tipo de promoción acumulable desconocido";
        }
    }
    
    @Override
    public String toString() {
        return "DetallePromocionResponse{" +
                "idDetalle=" + idDetalle +
                ", nombre='" + nombre + '\'' +
                ", esBase=" + esBase +
                ", descripcionTipo='" + descripcionTipo + '\'' +
                '}';
    }
} 