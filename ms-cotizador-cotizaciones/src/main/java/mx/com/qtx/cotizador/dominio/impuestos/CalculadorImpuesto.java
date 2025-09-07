package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Clase abstracta base para el cálculo de diferentes tipos de impuestos usando el patrón Bridge.
 * <p>
 * Esta clase implementa la parte de "Abstracción" del patrón Bridge, definiendo la interfaz
 * para diferentes tipos de impuestos (IVA, impuestos federales, locales, etc.) mientras
 * delega el cálculo específico por país a implementaciones concretas de
 * {@link ICalculadorImpuestoPais}.
 * </p>
 *
 * <h3>Patrón Bridge:</h3>
 * <p>
 * Esta clase establece el patrón Bridge permitiendo:
 * <ul>
 *   <li><strong>Separación de responsabilidades:</strong> Tipo de impuesto vs. reglas por país</li>
 *   <li><strong>Extensibilidad:</strong> Nuevos tipos de impuesto sin modificar países</li>
 *   <li><strong>Flexibilidad:</strong> Nuevos países sin modificar tipos de impuesto</li>
 *   <li><strong>Composición:</strong> Combinar cualquier tipo de impuesto con cualquier país</li>
 * </ul>
 * </p>
 *
 * <h3>Jerarquía de clases:</h3>
 * <pre>
 * CalculadorImpuesto (Abstracción)
 * ├── IVA (tipo específico)
 * ├── CalculadorImpuestoFederal (tipo específico)
 * └── CalculadorImpuestoLocal (tipo específico)
 *
 * ICalculadorImpuestoPais (Implementación)
 * ├── CalculadorImpuestoMexico (país específico)
 * ├── CalculadorImpuestosUsa (país específico)
 * └── CalculadorImpuestosCanada (país específico)
 * </pre>
 *
 * <h3>Subclases concretas:</h3>
 * <ul>
 *   <li><strong>{@link IVA}:</strong> Impuesto al Valor Agregado (IVA)</li>
 *   <li><strong>{@link CalculadorImpuestoFederal}:</strong> Impuestos federales</li>
 *   <li><strong>{@link CalculadorImpuestoLocal}:</strong> Impuestos locales/municipales</li>
 * </ul>
 *
 * <h3>Ejemplo de uso del patrón Bridge:</h3>
 * <pre>{@code
 * // Crear implementaciones por país
 * ICalculadorImpuestoPais mexico = new CalculadorImpuestoMexico();
 * ICalculadorImpuestoPais usa = new CalculadorImpuestosUsa();
 *
 * // Crear diferentes tipos de impuesto
 * CalculadorImpuesto ivaMexico = new IVA(mexico);
 * CalculadorImpuesto ivaUsa = new IVA(usa);
 * CalculadorImpuesto fedMexico = new CalculadorImpuestoFederal(mexico);
 *
 * // Calcular impuestos
 * BigDecimal subtotal = new BigDecimal("1000.00");
 * BigDecimal impuestoIvaMx = ivaMexico.calcularImpuesto(subtotal);
 * BigDecimal impuestoIvaUsa = ivaUsa.calcularImpuesto(subtotal);
 * BigDecimal impuestoFedMx = fedMexico.calcularImpuesto(subtotal);
 * }</pre>
 *
 * <h3>Beneficios del patrón Bridge:</h3>
 * <ul>
 *   <li><strong>Desacoplamiento:</strong> Cambios en reglas por país no afectan tipos de impuesto</li>
 *   <li><strong>Reutilización:</strong> Una implementación de país puede usarse con cualquier tipo de impuesto</li>
 *   <li><strong>Mantenibilidad:</strong> Código más modular y fácil de mantener</li>
 *   <li><strong>Extensibilidad:</strong> Fácil agregar nuevos países o tipos de impuesto</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.impuestos.ICalculadorImpuestoPais
 * @see mx.com.qtx.cotizador.dominio.impuestos.IVA
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestoFederal
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestoLocal
 */
public abstract class CalculadorImpuesto {
    protected ICalculadorImpuestoPais calculoImpuestoPais;

    /**
     * Constructor que establece la implementación específica por país.
     * <p>
     * Este constructor recibe la implementación concreta de cálculo de impuestos
     * por país, estableciendo el "puente" entre el tipo de impuesto (abstracción)
     * y las reglas específicas del país (implementación).
     * </p>
     *
     * @param calculoImpuestoPais Implementación concreta de cálculo de impuestos por país
     * @throws IllegalArgumentException si calculoImpuestoPais es null
     */
    protected CalculadorImpuesto(ICalculadorImpuestoPais calculoImpuestoPais) {
        this.calculoImpuestoPais = calculoImpuestoPais;
    }

    /**
     * Calcula el impuesto específico aplicable al monto proporcionado.
     * <p>
     * Este método abstracto debe ser implementado por cada subclase concreta
     * para definir cómo calcular el impuesto específico (IVA, federal, local, etc.)
     * utilizando las reglas del país establecido en el constructor.
     * </p>
     *
     * <h4>Consideraciones de implementación:</h4>
     * <ul>
     *   <li>Debe utilizar {@link #calculoImpuestoPais} para obtener la base imponible por país</li>
     *   <li>Los cálculos deben ser precisos usando {@link BigDecimal}</li>
     *   <li>Debe manejar casos especiales como exenciones o tasas cero</li>
     *   <li>Debe retornar cero o valores positivos (nunca negativos)</li>
     * </ul>
     *
     * @param monto Monto base sobre el cual calcular el impuesto
     * @return Monto del impuesto calculado (siempre >= 0)
     * @throws IllegalArgumentException si monto es null o negativo
     */
    public abstract BigDecimal calcularImpuesto(BigDecimal monto);
}
