package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Calculador especializado de IVA para México en el patrón Bridge.
 * <p>
 * Esta clase representa una implementación especializada del patrón Bridge que combina
 * específicamente el cálculo de IVA (Impuesto al Valor Agregado) con las reglas
 * impositivas mexicanas. Es un ejemplo de "Abstracción Refinada" que proporciona
 * una interfaz simplificada para el caso de uso más común: calcular IVA mexicano.
 * </p>
 *
 * <h3>Características principales:</h3>
 * <ul>
 *   <li><strong>IVA mexicano:</strong> Implementa específicamente la tasa del 16%</li>
 *   <li><strong>Constructor simplificado:</strong> No requiere parámetros, configura automáticamente México</li>
 *   <li><strong>Interfaz directa:</strong> Oculta la complejidad del patrón Bridge</li>
 *   <li><strong>Configuración fija:</strong> Siempre utiliza {@link CalculadorImpuestoMexico}</li>
 * </ul>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * IVA = montoBase × 0.16
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Uso directo y simple
 * IVA iva = new IVA();
 * BigDecimal monto = new BigDecimal("1000.00");
 * BigDecimal impuestoIVA = iva.calcularImpuesto(monto);
 * // Resultado: 160.00
 *
 * // Comparación con implementación Bridge completa
 * CalculadorImpuesto ivaCompleto = new IVA();
 * BigDecimal impuestoIVA2 = ivaCompleto.calcularImpuesto(monto);
 * // Resultado idéntico: 160.00
 * }</pre>
 *
 * <h3>Patrón Bridge aplicado:</h3>
 * <p>
 * Esta clase demuestra cómo el patrón Bridge puede proporcionar interfaces
 * especializadas para casos de uso comunes:
 * </p>
 * <ul>
 *   <li><strong>Abstracción especializada:</strong> IVA como tipo específico de impuesto</li>
 *   <li><strong>Implementación fija:</strong> México como país predeterminado</li>
 *   <li><strong>Facilidad de uso:</strong> Constructor sin parámetros</li>
 *   <li><strong>Flexibilidad oculta:</strong> Puede cambiarse a otros países si es necesario</li>
 * </ul>
 *
 * <h3>Diferencia con otras implementaciones:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>IVA</th><th>CalculadorImpuestoFederal</th><th>CalculadorImpuestoLocal</th></tr>
 *   <tr><td>Constructor</td><td>Sin parámetros</td><td>Requiere país</td><td>Requiere país</td></tr>
 *   <tr><td>País fijo</td><td>México</td><td>Configurable</td><td>Configurable</td></tr>
 *   <tr><td>Uso típico</td><td>IVA mexicano directo</td><td>Impuestos federales</td><td>Impuestos locales</td></tr>
 *   <tr><td>Flexibilidad</td><td>Baja</td><td>Alta</td><td>Alta</td></tr>
 * </table>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see CalculadorImpuesto
 * @see CalculadorImpuestoMexico
 * @see ICalculadorImpuestoPais
 */
public class IVA extends CalculadorImpuesto {

    /**
     * Constructor que inicializa el calculador de IVA para México.
     * <p>
     * Crea una nueva instancia del calculador de IVA configurada automáticamente
     * para calcular impuestos según las reglas mexicanas. Utiliza internamente
     * {@link CalculadorImpuestoMexico} para realizar los cálculos específicos del país.
     * </p>
     * <p>
     * Este constructor simplificado oculta la complejidad del patrón Bridge,
     * proporcionando una interfaz directa para el caso de uso más común: calcular
     * IVA en México sin necesidad de especificar el país explícitamente.
     * </p>
     *
     * @see CalculadorImpuestoMexico
     * @see #calcularImpuesto(BigDecimal)
     */
    public IVA() {
        super(new CalculadorImpuestoMexico());
    }

    /**
     * Calcula el IVA mexicano sobre el monto especificado.
     * <p>
     * Este método calcula el Impuesto al Valor Agregado aplicando la tasa
     * vigente en México (16%) sobre el monto base proporcionado. El cálculo
     * se delega a la implementación mexicana del patrón Bridge.
     * </p>
     * <p>
     * <strong>Proceso de cálculo:</strong>
     * <ol>
     *   <li>Utiliza {@link CalculadorImpuestoMexico} para calcular el IVA</li>
     *   <li>Aplica la tasa del 16% sobre el monto base</li>
     *   <li>Retorna el resultado con precisión decimal</li>
     * </ol>
     * </p>
     *
     * @param monto Monto base sobre el cual calcular el IVA mexicano
     * @return Monto del IVA calculado (16% del monto base)
     * @throws IllegalArgumentException si monto es null o negativo
     * @see CalculadorImpuesto#calcularImpuesto(BigDecimal)
     * @see CalculadorImpuestoMexico#calcularImpuestoPais(BigDecimal)
     */
    @Override
    public BigDecimal calcularImpuesto(BigDecimal monto) {
        return calculoImpuestoPais.calcularImpuestoPais(monto);
    }
} 