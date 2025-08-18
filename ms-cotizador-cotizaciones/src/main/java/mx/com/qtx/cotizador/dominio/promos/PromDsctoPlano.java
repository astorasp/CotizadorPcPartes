package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Promoción de descuento plano (promoción acumulable).
 * Aplica un porcentaje fijo de descuento sobre el precio total.
 */
public class PromDsctoPlano extends PromAcumulable {
    
    private final double porcentajeDescuento; // Porcentaje de descuento (0-100)
    
    /**
     * Constructor para promoción de descuento plano
     */
    public PromDsctoPlano(Integer idPromocion, String nombre, String descripcion, 
                          LocalDate vigenciaDesde, LocalDate vigenciaHasta,
                          double porcentajeDescuento) {
        super(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta);
        
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        
        this.porcentajeDescuento = porcentajeDescuento;
    }
    
    /**
     * Aplica el descuento plano sobre el importe total.
     */
    @Override
    protected BigDecimal aplicarDescuentoAcumulable(int cantidad, BigDecimal precioBase) {
        BigDecimal importeTotal = precioBase.multiply(BigDecimal.valueOf(cantidad));
        
        if (porcentajeDescuento == 0) {
            return importeTotal;
        }
        
        // Calcular el descuento
        BigDecimal factorDescuento = BigDecimal.valueOf(porcentajeDescuento).divide(
            BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP
        );
        
        BigDecimal montoDescuento = importeTotal.multiply(factorDescuento);
        BigDecimal importeConDescuento = importeTotal.subtract(montoDescuento);
        
        // Redondear a 2 decimales
        return importeConDescuento.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula el monto exacto del descuento aplicado
     */
    public BigDecimal calcularMontoDescuento(int cantidad, BigDecimal precioBase) {
        BigDecimal importeTotal = precioBase.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal importeConDescuento = aplicarDescuentoAcumulable(cantidad, precioBase);
        return importeTotal.subtract(importeConDescuento);
    }
    
    /**
     * Obtiene una descripción legible del descuento
     */
    public String getDescripcionDescuento() {
        if (porcentajeDescuento == 0) {
            return "Sin descuento";
        }
        
        // Formatear el porcentaje sin decimales innecesarios
        if (porcentajeDescuento == Math.floor(porcentajeDescuento)) {
            return String.format("%.0f%% de descuento", porcentajeDescuento);
        } else {
            return String.format("%.2f%% de descuento", porcentajeDescuento);
        }
    }
    
    /**
     * Verifica si realmente hay descuento
     */
    public boolean tieneDescuento() {
        return porcentajeDescuento > 0;
    }
    
    // Getter
    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }
}