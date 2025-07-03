package mx.com.qtx.cotizador.dto.promocion.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionAcumulable;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;

/**
 * DTO para configurar un detalle específico de promoción.
 * 
 * Maneja tanto promociones base (SIN_DESCUENTO, NXM) como acumulables
 * (DESCUENTO_PLANO, DESCUENTO_POR_CANTIDAD).
 */
public class DetallePromocionRequest {
    
    @NotBlank(message = "El nombre del detalle es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotNull(message = "Debe especificar si es promoción base")
    private Boolean esBase;
    
    // Campos para promoción base
    private TipoPromocionBase tipoBase;
    
    @Valid
    private ParametrosNxMRequest parametrosNxM;
    
    // Campos para promoción acumulable
    private TipoPromocionAcumulable tipoAcumulable;
    
    @DecimalMin(value = "0.0", message = "El descuento plano debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", message = "El descuento plano no puede ser mayor al 100%")
    private Double porcentajeDescuentoPlano;
    
    @Valid
    private List<EscalaDescuentoRequest> escalasDescuento;
    
    // Constructores
    public DetallePromocionRequest() {
        // Constructor vacío para Jackson
    }
    
    public DetallePromocionRequest(String nombre, Boolean esBase) {
        this.nombre = nombre;
        this.esBase = esBase;
    }
    
    // Getters y setters
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
    
    public TipoPromocionBase getTipoBase() {
        return tipoBase;
    }
    
    public void setTipoBase(TipoPromocionBase tipoBase) {
        this.tipoBase = tipoBase;
    }
    
    public ParametrosNxMRequest getParametrosNxM() {
        return parametrosNxM;
    }
    
    public void setParametrosNxM(ParametrosNxMRequest parametrosNxM) {
        this.parametrosNxM = parametrosNxM;
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
    
    public List<EscalaDescuentoRequest> getEscalasDescuento() {
        return escalasDescuento;
    }
    
    public void setEscalasDescuento(List<EscalaDescuentoRequest> escalasDescuento) {
        this.escalasDescuento = escalasDescuento;
    }
    
    /**
     * Valida la consistencia del detalle según su tipo
     */
    public boolean esValido() {
        if (esBase == null || nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        
        if (esBase) {
            // Validar promoción base
            return validarPromocionBase();
        } else {
            // Validar promoción acumulable
            return validarPromocionAcumulable();
        }
    }
    
    private boolean validarPromocionBase() {
        if (tipoBase == null) return false;
        
        switch (tipoBase) {
            case SIN_DESCUENTO:
                return true; // No requiere parámetros adicionales
            case NXM:
                return parametrosNxM != null && parametrosNxM.esValida();
            default:
                return false;
        }
    }
    
    private boolean validarPromocionAcumulable() {
        if (tipoAcumulable == null) return false;
        
        switch (tipoAcumulable) {
            case DESCUENTO_PLANO:
                return porcentajeDescuentoPlano != null && 
                       porcentajeDescuentoPlano >= 0.0 && 
                       porcentajeDescuentoPlano <= 100.0;
            case DESCUENTO_POR_CANTIDAD:
                return escalasDescuento != null && 
                       !escalasDescuento.isEmpty() &&
                       escalasDescuento.stream().allMatch(EscalaDescuentoRequest::esValida);
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return "DetallePromocionRequest{" +
                "nombre='" + nombre + '\'' +
                ", esBase=" + esBase +
                ", tipoBase=" + tipoBase +
                ", tipoAcumulable=" + tipoAcumulable +
                '}';
    }
} 