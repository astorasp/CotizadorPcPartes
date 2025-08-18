package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Promoción tipo "Lleva N, Paga M" (promoción base).
 * Por ejemplo: "Lleva 3, Paga 2" o "2x1".
 */
public class PromNXM extends PromBase {
    
    private final int llevent; // Cantidad que lleva el cliente
    private final int paguen;  // Cantidad que paga el cliente
    
    /**
     * Constructor para promoción NxM
     */
    public PromNXM(Integer idPromocion, String nombre, String descripcion, 
                   LocalDate vigenciaDesde, LocalDate vigenciaHasta,
                   int llevent, int paguen) {
        super(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta);
        
        if (llevent <= 0 || paguen <= 0) {
            throw new IllegalArgumentException("Los valores de llevent y paguen deben ser positivos");
        }
        
        if (paguen >= llevent) {
            throw new IllegalArgumentException("paguen debe ser menor que llevent para que exista descuento");
        }
        
        this.llevent = llevent;
        this.paguen = paguen;
    }
    
    /**
     * Aplica el descuento tipo NxM.
     * Calcula cuántos grupos completos de "llevent" se pueden formar
     * y cobra solo "paguen" por cada grupo.
     */
    @Override
    protected BigDecimal aplicarDescuentoBase(int cantidad, BigDecimal precioBase) {
        if (cantidad < llevent) {
            // No hay suficiente cantidad para aplicar la promoción
            return precioBase.multiply(BigDecimal.valueOf(cantidad));
        }
        
        // Calcular grupos completos que aplican para la promoción
        int gruposCompletos = cantidad / llevent;
        int cantidadRestante = cantidad % llevent;
        
        // Cantidad que realmente se cobra
        int cantidadACobrar = (gruposCompletos * paguen) + cantidadRestante;
        
        return precioBase.multiply(BigDecimal.valueOf(cantidadACobrar));
    }
    
    /**
     * Calcula el ahorro obtenido por la promoción
     */
    public BigDecimal calcularAhorro(int cantidad, BigDecimal precioBase) {
        BigDecimal precioSinPromocion = precioBase.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal precioConPromocion = calcularImportePromocion(cantidad, precioBase);
        return precioSinPromocion.subtract(precioConPromocion);
    }
    
    /**
     * Obtiene una descripción legible de la promoción
     */
    public String getDescripcionPromocion() {
        if (paguen == 1 && llevent == 2) {
            return "2x1";
        } else if (paguen == 2 && llevent == 3) {
            return "3x2";
        } else {
            return String.format("Lleva %d, Paga %d", llevent, paguen);
        }
    }
    
    // Getters
    public int getLlevent() {
        return llevent;
    }
    
    public int getPaguen() {
        return paguen;
    }
}