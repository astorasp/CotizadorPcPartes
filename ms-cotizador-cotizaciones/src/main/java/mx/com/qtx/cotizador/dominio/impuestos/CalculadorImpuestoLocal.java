package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Implementación del calculador de impuestos locales/municipales para el patrón Bridge.
 * <p>
 * Esta clase concreta del patrón Bridge implementa el cálculo de impuestos locales
 * aplicando una tasa fija del 3% sobre el monto base, además de los impuestos
 * específicos del país calculados por la implementación de {@link ICalculadorImpuestoPais}.
 * </p>
 *
 * <h3>Funcionamiento del cálculo:</h3>
 * <p>
 * El cálculo de impuestos locales se realiza en dos etapas:
 * </p>
 * <ol>
 *   <li>Se calcula el impuesto específico del país utilizando la implementación puente</li>
 *   <li>Se agrega el impuesto local fijo del 3% sobre el monto original</li>
 * </ol>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * impuestoTotal = impuestoPais + (montoBase × 0.03)
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear calculador local para México
 * ICalculadorImpuestoPais mexico = new CalculadorImpuestoMexico();
 * CalculadorImpuestoLocal localMexico = new CalculadorImpuestoLocal(mexico);
 *
 * // Calcular impuesto local sobre $1000.00
 * BigDecimal monto = new BigDecimal("1000.00");
 * BigDecimal impuestoLocal = localMexico.calcularImpuesto(monto);
 * // Resultado: 160.00 (IVA 16%) + 30.00 (Local 3%) = 190.00
 * }</pre>
 *
 * <h3>Patrón Bridge aplicado:</h3>
 * <p>
 * Esta clase representa la parte de "Abstracción Refinada" del patrón Bridge:
 * <ul>
 *   <li><strong>Abstracción:</strong> Define el tipo específico de impuesto (local/municipal)</li>
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
 * @see CalculadorImpuestoFederal
 */
public class CalculadorImpuestoLocal extends CalculadorImpuesto {

    /**
     * Tasa impositiva local fija del 3%.
     * <p>
     * Esta constante define la tasa impositiva local/municipal que se aplica uniformemente
     * sobre todos los montos, independientemente del país. Representa el 3% (0.03)
     * como {@link BigDecimal} para garantizar precisión decimal exacta en los cálculos.
     * </p>
     * <p>
     * Los impuestos locales pueden variar significativamente entre diferentes
     * municipios o estados, pero en este sistema se utiliza una tasa fija simplificada.
     * </p>
     */
    private static final BigDecimal IMPUESTO_LOCAL = BigDecimal.valueOf(0.03);

    /**
     * Constructor que crea un calculador de impuestos locales.
     * <p>
     * Inicializa el calculador de impuestos locales con la implementación
     * específica del país proporcionada. Esta implementación aplicará tanto
     * los impuestos específicos del país como el impuesto local fijo del 3%.
     * </p>
     *
     * @param calculoImpuestoPais Implementación concreta de {@link ICalculadorImpuestoPais}
     *                           que define las reglas impositivas específicas del país
     * @throws IllegalArgumentException si calculoImpuestoPais es null
     * @see CalculadorImpuesto#CalculadorImpuesto(ICalculadorImpuestoPais)
     */
    public CalculadorImpuestoLocal(ICalculadorImpuestoPais calculoImpuestoPais) {
        super(calculoImpuestoPais);
    }

    /**
     * Calcula el impuesto local total sobre el monto especificado.
     * <p>
     * Este método implementa la lógica específica para el cálculo de impuestos locales,
     * combinando el impuesto específico del país con el impuesto local fijo del 3%.
     * Primero calcula el impuesto del país utilizando la implementación puente, luego
     * agrega el 3% local sobre el monto original.
     * </p>
     * <p>
     * <strong>Proceso de cálculo:</strong>
     * <ol>
     *   <li>Calcula impuesto específico del país: {@code impuestoPais = calculoImpuestoPais.calcularImpuestoPais(monto)}</li>
     *   <li>Calcula impuesto local: {@code impuestoLocal = monto × 0.03}</li>
     *   <li>Retorna suma total: {@code impuestoPais + impuestoLocal}</li>
     * </ol>
     * </p>
     *
     * @param monto Monto base sobre el cual calcular los impuestos locales
     * @return Monto total del impuesto local (impuesto país + 3% local)
     * @throws IllegalArgumentException si monto es null o negativo
     * @see CalculadorImpuesto#calcularImpuesto(BigDecimal)
     * @see ICalculadorImpuestoPais#calcularImpuestoPais(BigDecimal)
     */
    @Override
    public BigDecimal calcularImpuesto(BigDecimal monto) {
        return calculoImpuestoPais.calcularImpuestoPais(monto)
            .add(monto.multiply(IMPUESTO_LOCAL));
    }
}