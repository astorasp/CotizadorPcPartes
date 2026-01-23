package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Implementación concreta del calculador de impuestos para Canadá en el patrón Bridge.
 * <p>
 * Esta clase implementa la parte de "Implementación Concreta" del patrón Bridge,
 * proporcionando las reglas específicas de cálculo de impuestos para Canadá.
 * Actualmente implementa el GST (Goods and Services Tax) con una tasa simplificada del 15%.
 * </p>
 *
 * <h3>Reglas impositivas implementadas:</h3>
 * <ul>
 *   <li><strong>GST simplificado:</strong> 15% sobre el monto base</li>
 *   <li><strong>Cálculo preciso:</strong> Utiliza {@link BigDecimal} para evitar errores de redondeo</li>
 *   <li><strong>Monto positivo:</strong> Siempre retorna valores positivos o cero</li>
 * </ul>
 *
 * <h3>Nota sobre simplificación:</h3>
 * <p>
 * El sistema fiscal canadiense es más complejo que esta implementación simplificada:
 * </p>
 * <ul>
 *   <li><strong>GST federal:</strong> 5% (esta implementación usa 15% como simplificación)</li>
 *   <li><strong>HST provincial:</strong> GST + PST en algunas provincias</li>
 *   <li><strong>PST provincial:</strong> Impuestos provinciales variables</li>
 *   <li><strong>Exenciones:</strong> Alimentos básicos, servicios médicos, etc.</li>
 * </ul>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * GST = montoBase × 0.15
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * CalculadorImpuestosCanada canada = new CalculadorImpuestosCanada();
 * BigDecimal monto = new BigDecimal("1000.00");
 * BigDecimal gst = canada.calcularImpuestoPais(monto);
 * // Resultado: 150.00
 * }</pre>
 *
 * <h3>Patrón Bridge aplicado:</h3>
 * <p>
 * Esta clase representa la "Implementación Concreta" que puede ser utilizada
 * por cualquier "Abstracción" (tipo de impuesto) para calcular impuestos en Canadá:
 * </p>
 * <ul>
 *   <li>Puede combinarse con {@link IVA} para GST canadiense</li>
 *   <li>Puede combinarse con {@link CalculadorImpuestoFederal} para impuestos federales</li>
 *   <li>Puede combinarse con {@link CalculadorImpuestoLocal} para impuestos provinciales</li>
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
public class CalculadorImpuestosCanada implements ICalculadorImpuestoPais {

    /**
     * Calcula el impuesto correspondiente para Canadá sobre el monto especificado.
     * <p>
     * Implementa el cálculo del GST canadiense aplicando la tasa simplificada del 15%
     * sobre el monto base proporcionado. El cálculo se realiza con precisión decimal
     * utilizando {@link BigDecimal} para garantizar resultados exactos.
     * </p>
     * <p>
     * <strong>Nota:</strong> Esta es una implementación simplificada. El sistema fiscal
     * canadiense real incluye GST federal (5%) más impuestos provinciales variables
     * (PST/HST) que pueden variar entre 0% y 15% dependiendo de la provincia.
     * </p>
     *
     * @param monto Monto base sobre el cual calcular el impuesto canadiense
     * @return Monto del GST calculado (15% del monto base)
     * @throws IllegalArgumentException si monto es null
     * @see ICalculadorImpuestoPais#calcularImpuestoPais(BigDecimal)
     */
    @Override
    public BigDecimal calcularImpuestoPais(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(0.15));
    }
}
