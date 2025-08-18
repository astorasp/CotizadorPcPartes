package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Promoción sin descuento (promoción base).
 * Representa componentes que no tienen promoción activa o que específicamente
 * no deben tener descuento aplicado.
 */
public class PromSinDescto extends PromBase {
    
    /**
     * Constructor para promoción sin descuento
     */
    public PromSinDescto(Integer idPromocion, String nombre, String descripcion, 
                         LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        super(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta);
    }
    
    /**
     * Constructor simplificado para promoción sin descuento
     */
    public PromSinDescto() {
        super(null, "Sin promoción", "Precio regular sin descuentos", null, null);
    }
    
    /**
     * No aplica ningún descuento, retorna el precio base multiplicado por la cantidad.
     */
    @Override
    protected BigDecimal aplicarDescuentoBase(int cantidad, BigDecimal precioBase) {
        return precioBase.multiply(BigDecimal.valueOf(cantidad));
    }
    
    /**
     * Esta promoción nunca genera ahorro
     */
    public BigDecimal calcularAhorro(int cantidad, BigDecimal precioBase) {
        return BigDecimal.ZERO;
    }
    
    /**
     * Siempre está vigente ya que representa la ausencia de promoción
     */
    @Override
    protected boolean esVigente() {
        // Si es la promoción por defecto (sin ID), siempre está vigente
        if (getIdPromocion() == null) {
            return true;
        }
        
        // Si tiene ID, validar vigencia normalmente
        return super.esVigente();
    }
    
    /**
     * Obtiene una descripción de que no hay promoción activa
     */
    public String getDescripcionPromocion() {
        return "Precio regular - Sin promoción";
    }
    
    /**
     * Indica si esta instancia es la promoción por defecto (sin ID)
     */
    public boolean esPromocionPorDefecto() {
        return getIdPromocion() == null;
    }
    
    /**
     * Factory method para crear una instancia de promoción sin descuento por defecto
     */
    public static PromSinDescto crearPorDefecto() {
        return new PromSinDescto();
    }
}