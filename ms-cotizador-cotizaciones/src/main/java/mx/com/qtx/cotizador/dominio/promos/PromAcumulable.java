package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase abstracta para promociones acumulables.
 * Las promociones acumulables se pueden combinar con otras promociones acumulables
 * para obtener mayores descuentos.
 */
public abstract class PromAcumulable extends Promocion {
    
    /**
     * Constructor para promociones acumulables
     */
    protected PromAcumulable(Integer idPromocion, String nombre, String descripcion, 
                            LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        super(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta);
    }
    
    /**
     * Las promociones acumulables pueden combinarse.
     * Este método define el comportamiento común para todas las promociones acumulables.
     */
    @Override
    protected final BigDecimal calcularDescuentoEspecifico(int cantidad, BigDecimal precioBase) {
        return aplicarDescuentoAcumulable(cantidad, precioBase);
    }
    
    /**
     * Método abstracto que define cómo cada promoción acumulable específica
     * aplica su descuento particular.
     */
    protected abstract BigDecimal aplicarDescuentoAcumulable(int cantidad, BigDecimal precioBase);
    
    /**
     * Indica que esta promoción es acumulable
     */
    public final boolean esAcumulable() {
        return true;
    }
    
    /**
     * Calcula el descuento aplicado (diferencia entre precio original y precio con descuento)
     * Útil para combinar múltiples promociones acumulables.
     */
    public BigDecimal calcularDescuentoAplicado(int cantidad, BigDecimal precioBase) {
        BigDecimal precioOriginal = precioBase.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal precioConDescuento = aplicarDescuentoAcumulable(cantidad, precioBase);
        return precioOriginal.subtract(precioConDescuento);
    }
}