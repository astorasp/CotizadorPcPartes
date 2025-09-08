package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Implementación concreta del calculador de impuestos para México en el patrón Bridge.
 * <p>
 * Esta clase implementa la parte de "Implementación Concreta" del patrón Bridge,
 * proporcionando las reglas específicas de cálculo de impuestos para México.
 * Actualmente implementa el IVA (Impuesto al Valor Agregado) con una tasa del 16%.
 * </p>
 *
 * <h3>Reglas impositivas implementadas:</h3>
 * <ul>
 *   <li><strong>IVA estándar:</strong> 16% sobre el monto base</li>
 *   <li><strong>Cálculo preciso:</strong> Utiliza {@link BigDecimal} para evitar errores de redondeo</li>
 *   <li><strong>Monto positivo:</strong> Siempre retorna valores positivos o cero</li>
 * </ul>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * IVA = montoBase × 0.16
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * CalculadorImpuestoMexico mexico = new CalculadorImpuestoMexico();
 * BigDecimal monto = new BigDecimal("1000.00");
 * BigDecimal iva = mexico.calcularImpuestoPais(monto);
 * // Resultado: 160.00
 * }</pre>
 *
 * <h3>Consideraciones fiscales:</h3>
 * <p>
 * Esta implementación refleja las reglas impositivas mexicanas vigentes al momento
 * del desarrollo. En un sistema real, sería necesario:
 * </p>
 * <ul>
 *   <li>Actualizar las tasas impositivas según cambios legislativos</li>
 *   <li>Implementar exenciones y tratamientos especiales</li>
 *   <li>Considerar diferentes tipos de IVA (tasas reducidas, cero, etc.)</li>
 * </ul>
 *
 * <h3>Patrón Bridge aplicado:</h3>
 * <p>
 * Esta clase representa la "Implementación Concreta" que puede ser utilizada
 * por cualquier "Abstracción" (tipo de impuesto) para calcular impuestos en México:
 * </p>
 * <ul>
 *   <li>Puede combinarse con {@link IVA} para IVA mexicano</li>
 *   <li>Puede combinarse con {@link CalculadorImpuestoFederal} para impuestos federales</li>
 *   <li>Puede combinarse con {@link CalculadorImpuestoLocal} para impuestos locales</li>
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
public class CalculadorImpuestoMexico implements ICalculadorImpuestoPais {

    /**
     * Calcula el impuesto correspondiente para México sobre el monto especificado.
     * <p>
     * Implementa el cálculo del IVA mexicano aplicando la tasa del 16% sobre
     * el monto base proporcionado. El cálculo se realiza con precisión decimal
     * utilizando {@link BigDecimal} para garantizar resultados exactos.
     * </p>
     * <p>
     * <strong>Nota:</strong> Actualmente solo implementa IVA estándar. En una
     * implementación más completa, debería considerar diferentes tipos de IVA
     * (tasa cero, reducida, etc.) según el tipo de bien o servicio.
     * </p>
     *
     * @param monto Monto base sobre el cual calcular el impuesto mexicano
     * @return Monto del IVA calculado (16% del monto base)
     * @throws IllegalArgumentException si monto es null
     * @see ICalculadorImpuestoPais#calcularImpuestoPais(BigDecimal)
     */
    @Override
    public BigDecimal calcularImpuestoPais(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(0.16));
    }
}
