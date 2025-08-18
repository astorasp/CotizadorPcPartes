package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase abstracta para promociones base.
 * Las promociones base no se pueden acumular con otras promociones.
 */
public abstract class PromBase extends Promocion {
    
    /**
     * Constructor para promociones base
     */
    protected PromBase(Integer idPromocion, String nombre, String descripcion, 
                      LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        super(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta);
    }
    
    /**
     * Las promociones base operan de manera independiente y no se acumulan.
     * Este método define el comportamiento común para todas las promociones base.
     */
    @Override
    protected final BigDecimal calcularDescuentoEspecifico(int cantidad, BigDecimal precioBase) {
        return aplicarDescuentoBase(cantidad, precioBase);
    }
    
    /**
     * Método abstracto que define cómo cada promoción base específica
     * aplica su descuento particular.
     */
    protected abstract BigDecimal aplicarDescuentoBase(int cantidad, BigDecimal precioBase);
    
    /**
     * Indica que esta promoción no es acumulable
     */
    public final boolean esAcumulable() {
        return false;
    }
}