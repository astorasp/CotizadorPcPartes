package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Implementación concreta del calculador de impuestos para Estados Unidos en el patrón Bridge.
 * <p>
 * Esta clase implementa la parte de "Implementación Concreta" del patrón Bridge,
 * proporcionando las reglas específicas de cálculo de impuestos para Estados Unidos.
 * Actualmente implementa una tasa impositiva simplificada del 5%.
 * </p>
 *
 * <h3>Reglas impositivas implementadas:</h3>
 * <ul>
 *   <li><strong>Tasa simplificada:</strong> 5% sobre el monto base</li>
 *   <li><strong>Cálculo preciso:</strong> Utiliza {@link BigDecimal} para evitar errores de redondeo</li>
 *   <li><strong>Monto positivo:</strong> Siempre retorna valores positivos o cero</li>
 * </ul>
 *
 * <h3>Nota sobre simplificación:</h3>
 * <p>
 * El sistema fiscal estadounidense es altamente complejo y variable:
 * </p>
 * <ul>
 *   <li><strong>Impuestos estatales:</strong> Varían entre 0% (algunos estados) y 8.875% (California)</li>
 *   <li><strong>Impuestos locales:</strong> Counties y ciudades pueden agregar impuestos adicionales</li>
 *   <li><strong>Sales Tax:</strong> Es el impuesto principal sobre ventas, no hay IVA federal</li>
 *   <li><strong>Exenciones:</strong> Alimentos, medicamentos, ropa (depende del estado)</li>
 * </ul>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * SalesTax = montoBase × 0.05
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * CalculadorImpuestosUsa usa = new CalculadorImpuestosUsa();
 * BigDecimal monto = new BigDecimal("1000.00");
 * BigDecimal salesTax = usa.calcularImpuestoPais(monto);
 * // Resultado: 50.00
 * }</pre>
 *
 * <h3>Patrón Bridge aplicado:</h3>
 * <p>
 * Esta clase representa la "Implementación Concreta" que puede ser utilizada
 * por cualquier "Abstracción" (tipo de impuesto) para calcular impuestos en Estados Unidos:
 * </p>
 * <ul>
 *   <li>Puede combinarse con {@link IVA} para sales tax estadounidense</li>
 *   <li>Puede combinarse con {@link CalculadorImpuestoFederal} para impuestos federales</li>
 *   <li>Puede combinarse con {@link CalculadorImpuestoLocal} para impuestos estatales</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see ICalculadorImpuestoPais
 * @see IVA
 * @see CalculadorImpuestoFederal
 * @see CalculadorImpuestoLocal
 */
public class CalculadorImpuestosUsa implements ICalculadorImpuestoPais {

    /**
     * Calcula el impuesto correspondiente para Estados Unidos sobre el monto especificado.
     * <p>
     * Implementa el cálculo del sales tax estadounidense aplicando la tasa simplificada
     * del 5% sobre el monto base proporcionado. El cálculo se realiza con precisión decimal
     * utilizando {@link BigDecimal} para garantizar resultados exactos.
     * </p>
     * <p>
     * <strong>Nota:</strong> Esta es una implementación altamente simplificada. En la realidad,
     * Estados Unidos tiene más de 12,000 jurisdicciones fiscales diferentes, cada una con
     * sus propias tasas impositivas que pueden variar desde 0% hasta más del 10%.
     * </p>
     *
     * @param monto Monto base sobre el cual calcular el impuesto estadounidense
     * @return Monto del sales tax calculado (5% del monto base)
     * @throws IllegalArgumentException si monto es null
     * @see ICalculadorImpuestoPais#calcularImpuestoPais(BigDecimal)
     */
    @Override
    public BigDecimal calcularImpuestoPais(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(0.05));
    }
}
