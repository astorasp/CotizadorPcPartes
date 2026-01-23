package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Implementación del calculador de impuestos federales para el patrón Bridge.
 * <p>
 * Esta clase concreta del patrón Bridge implementa el cálculo de impuestos federales
 * aplicando una tasa fija del 5% sobre el monto base, además de los impuestos
 * específicos del país calculados por la implementación de {@link ICalculadorImpuestoPais}.
 * </p>
 *
 * <h3>Funcionamiento del cálculo:</h3>
 * <p>
 * El cálculo de impuestos federales se realiza en dos etapas:
 * </p>
 * <ol>
 *   <li>Se calcula el impuesto específico del país utilizando la implementación puente</li>
 *   <li>Se agrega el impuesto federal fijo del 5% sobre el monto original</li>
 * </ol>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * impuestoTotal = impuestoPais + (montoBase × 0.05)
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear calculador federal para México
 * ICalculadorImpuestoPais mexico = new CalculadorImpuestoMexico();
 * CalculadorImpuestoFederal fedMexico = new CalculadorImpuestoFederal(mexico);
 *
 * // Calcular impuesto federal sobre $1000.00
 * BigDecimal monto = new BigDecimal("1000.00");
 * BigDecimal impuestoFederal = fedMexico.calcularImpuesto(monto);
 * // Resultado: 160.00 (IVA 16%) + 50.00 (Federal 5%) = 210.00
 * }</pre>
 *
 * <h3>Patrón Bridge aplicado:</h3>
 * <p>
 * Esta clase representa la parte de "Abstracción Refinada" del patrón Bridge:
 * <ul>
 *   <li><strong>Abstracción:</strong> Define el tipo específico de impuesto (federal)</li>
 *   <li><strong>Implementación:</strong> Delega las reglas por país a ICalculadorImpuestoPais</li>
 *   <li><strong>Composición:</strong> Puede combinarse con cualquier país soportado</li>
 * </ul>
 * </p>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see CalculadorImpuesto
 * @see ICalculadorImpuestoPais
 * @see CalculadorImpuestoLocal
 */
public class CalculadorImpuestoFederal extends CalculadorImpuesto {

    /**
     * Tasa impositiva federal fija del 5%.
     * <p>
     * Esta constante define la tasa impositiva federal que se aplica uniformemente
     * sobre todos los montos, independientemente del país. Representa el 5% (0.05)
     * como {@link BigDecimal} para garantizar precisión decimal exacta en los cálculos.
     * </p>
     * <p>
     * El uso de una constante garantiza que la tasa federal sea consistente
     * en todos los cálculos y facilita su mantenimiento si fuera necesario
     * modificarla en el futuro.
     * </p>
     */
    private static final BigDecimal IMPUESTO_FEDERAL = BigDecimal.valueOf(0.05);

    /**
     * Constructor que crea un calculador de impuestos federales.
     * <p>
     * Inicializa el calculador de impuestos federales con la implementación
     * específica del país proporcionada. Esta implementación aplicará tanto
     * los impuestos específicos del país como el impuesto federal fijo del 5%.
     * </p>
     *
     * @param calculoImpuestoPais Implementación concreta de {@link ICalculadorImpuestoPais}
     *                           que define las reglas impositivas específicas del país
     * @throws IllegalArgumentException si calculoImpuestoPais es null
     * @see CalculadorImpuesto#CalculadorImpuesto(ICalculadorImpuestoPais)
     */
    public CalculadorImpuestoFederal(ICalculadorImpuestoPais calculoImpuestoPais) {
        super(calculoImpuestoPais);
    }

    /**
     * Calcula el impuesto federal total sobre el monto especificado.
     * <p>
     * Este método implementa la lógica específica para el cálculo de impuestos federales,
     * combinando el impuesto específico del país con el impuesto federal fijo del 5%.
     * Primero calcula el impuesto del país utilizando la implementación puente, luego
     * agrega el 5% federal sobre el monto original.
     * </p>
     * <p>
     * <strong>Proceso de cálculo:</strong>
     * <ol>
     *   <li>Calcula impuesto específico del país: {@code impuestoPais = calculoImpuestoPais.calcularImpuestoPais(monto)}</li>
     *   <li>Calcula impuesto federal: {@code impuestoFederal = monto × 0.05}</li>
     *   <li>Retorna suma total: {@code impuestoPais + impuestoFederal}</li>
     * </ol>
     * </p>
     *
     * @param monto Monto base sobre el cual calcular los impuestos federales
     * @return Monto total del impuesto federal (impuesto país + 5% federal)
     * @throws IllegalArgumentException si monto es null o negativo
     * @see CalculadorImpuesto#calcularImpuesto(BigDecimal)
     * @see ICalculadorImpuestoPais#calcularImpuestoPais(BigDecimal)
     */
    @Override
    public BigDecimal calcularImpuesto(BigDecimal monto) {
        return calculoImpuestoPais.calcularImpuestoPais(monto) 
            .add(monto.multiply(IMPUESTO_FEDERAL));
    }
}