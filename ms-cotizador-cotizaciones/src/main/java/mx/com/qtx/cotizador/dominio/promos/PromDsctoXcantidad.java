package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Promoción de descuento por cantidad (promoción acumulable).
 * Aplica diferentes porcentajes de descuento según la cantidad comprada.
 * Utiliza escalas de cantidad para determinar el descuento apropiado.
 */
public class PromDsctoXcantidad extends PromAcumulable {
    
    private final Map<Integer, Double> escalasDescuento; // Cantidad -> Porcentaje descuento
    
    /**
     * Constructor para promoción de descuento por cantidad
     */
    public PromDsctoXcantidad(Integer idPromocion, String nombre, String descripcion, 
                              LocalDate vigenciaDesde, LocalDate vigenciaHasta,
                              Map<Integer, Double> escalasDescuento) {
        super(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta);
        
        if (escalasDescuento == null || escalasDescuento.isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos una escala de descuento");
        }
        
        // Validar que todos los valores sean válidos
        for (Map.Entry<Integer, Double> entry : escalasDescuento.entrySet()) {
            if (entry.getKey() <= 0) {
                throw new IllegalArgumentException("Las cantidades deben ser positivas");
            }
            if (entry.getValue() < 0 || entry.getValue() > 100) {
                throw new IllegalArgumentException("Los porcentajes de descuento deben estar entre 0 y 100");
            }
        }
        
        // Usar TreeMap para mantener las escalas ordenadas por cantidad
        this.escalasDescuento = new TreeMap<>(escalasDescuento);
    }
    
    /**
     * Constructor alternativo que acepta listas paralelas
     */
    public PromDsctoXcantidad(Integer idPromocion, String nombre, String descripcion, 
                              LocalDate vigenciaDesde, LocalDate vigenciaHasta,
                              List<Integer> cantidades, List<Double> descuentos) {
        this(idPromocion, nombre, descripcion, vigenciaDesde, vigenciaHasta,
             crearMapaEscalas(cantidades, descuentos));
    }
    
    /**
     * Método helper para crear el mapa de escalas desde listas paralelas
     */
    private static Map<Integer, Double> crearMapaEscalas(List<Integer> cantidades, List<Double> descuentos) {
        if (cantidades == null || descuentos == null || cantidades.size() != descuentos.size()) {
            throw new IllegalArgumentException("Las listas de cantidades y descuentos deben tener el mismo tamaño");
        }
        
        Map<Integer, Double> mapa = new TreeMap<>();
        for (int i = 0; i < cantidades.size(); i++) {
            mapa.put(cantidades.get(i), descuentos.get(i));
        }
        return mapa;
    }
    
    /**
     * Aplica el descuento por cantidad según las escalas definidas.
     * Busca la escala más alta que aplique para la cantidad solicitada.
     */
    @Override
    protected BigDecimal aplicarDescuentoAcumulable(int cantidad, BigDecimal precioBase) {
        BigDecimal importeTotal = precioBase.multiply(BigDecimal.valueOf(cantidad));
        
        double porcentajeDescuento = obtenerPorcentajeDescuento(cantidad);
        
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
     * Obtiene el porcentaje de descuento que aplica para una cantidad específica.
     * Busca la escala más alta que sea menor o igual a la cantidad solicitada.
     */
    public double obtenerPorcentajeDescuento(int cantidad) {
        double descuentoAplicable = 0.0;
        
        for (Map.Entry<Integer, Double> entry : escalasDescuento.entrySet()) {
            if (cantidad >= entry.getKey()) {
                descuentoAplicable = entry.getValue();
            } else {
                break; // Las escalas están ordenadas, no encontraremos mejor descuento
            }
        }
        
        return descuentoAplicable;
    }
    
    /**
     * Obtiene la siguiente escala de descuento disponible
     */
    public Integer getSiguienteEscala(int cantidadActual) {
        for (Integer cantidadEscala : escalasDescuento.keySet()) {
            if (cantidadEscala > cantidadActual) {
                return cantidadEscala;
            }
        }
        return null; // No hay más escalas
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
     * Obtiene una descripción legible del descuento aplicado
     */
    public String getDescripcionDescuento(int cantidad) {
        double porcentaje = obtenerPorcentajeDescuento(cantidad);
        
        if (porcentaje == 0) {
            Integer siguienteEscala = getSiguienteEscala(cantidad);
            if (siguienteEscala != null) {
                return String.format("Sin descuento (compra %d para obtener %.1f%% de descuento)", 
                    siguienteEscala, escalasDescuento.get(siguienteEscala));
            } else {
                return "Sin descuento";
            }
        }
        
        return String.format("%.1f%% de descuento por cantidad", porcentaje);
    }
    
    /**
     * Obtiene todas las escalas de descuento configuradas
     */
    public String getDescripcionEscalas() {
        StringBuilder sb = new StringBuilder();
        sb.append("Escalas de descuento: ");
        
        boolean primera = true;
        for (Map.Entry<Integer, Double> entry : escalasDescuento.entrySet()) {
            if (!primera) {
                sb.append(", ");
            }
            sb.append(String.format("%d+ = %.1f%%", entry.getKey(), entry.getValue()));
            primera = false;
        }
        
        return sb.toString();
    }
    
    // Getter
    public Map<Integer, Double> getEscalasDescuento() {
        return new TreeMap<>(escalasDescuento); // Retorna copia inmutable
    }
}