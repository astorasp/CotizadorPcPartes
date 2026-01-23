package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Interfaz que define el contrato para calcular impuestos específicos por país.
 * <p>
 * Esta interfaz establece el patrón Bridge para separar la lógica de cálculo de impuestos
 * por país de los diferentes tipos de impuestos (IVA, impuestos federales, locales, etc.).
 * Las implementaciones concretas manejan las reglas impositivas específicas de cada país
 * soportado por el sistema CotizadorPcPartes.
 * </p>
 *
 * <h3>Patrón Bridge:</h3>
 * <p>
 * Esta interfaz implementa la parte de "Implementación" del patrón Bridge, permitiendo:
 * <ul>
 *   <li>Separar la abstracción (tipo de impuesto) de la implementación (país)</li>
 *   <li>Cambiar dinámicamente las reglas impositivas por país</li>
 *   <li>Extender fácilmente con nuevos países sin modificar tipos de impuesto</li>
 *   <li>Componer diferentes tipos de impuesto con diferentes países</li>
 * </ul>
 * </p>
 *
 * <h3>Implementaciones disponibles:</h3>
 * <ul>
 *   <li>{@link CalculadorImpuestoMexico} - Impuestos mexicanos (IVA 16%)</li>
 *   <li>{@link CalculadorImpuestosUsa} - Impuestos estadounidenses (varían por estado)</li>
 *   <li>{@link CalculadorImpuestosCanada} - Impuestos canadienses (GST + provinciales)</li>
 * </ul>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear calculador para México
 * ICalculadorImpuestoPais mexico = new CalculadorImpuestoMexico();
 * BigDecimal impuestoMexico = mexico.calcularImpuestoPais(new BigDecimal("1000.00"));
 * // Resultado: 160.00 (16% de IVA)
 *
 * // Crear calculador para USA
 * ICalculadorImpuestoPais usa = new CalculadorImpuestosUsa();
 * BigDecimal impuestoUsa = usa.calcularImpuestoPais(new BigDecimal("1000.00"));
 * // Resultado: varía según el estado
 * }</pre>
 *
 * <h3>Consideraciones de precisión:</h3>
 * <ul>
 *   <li>Los cálculos deben usar {@link BigDecimal} para evitar errores de redondeo</li>
 *   <li>Las tasas impositivas deben ser consistentes con las regulaciones locales</li>
 *   <li>Deben manejar casos especiales como exenciones o tasas reducidas</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestoMexico
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestosUsa
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestosCanada
 */
public interface ICalculadorImpuestoPais {
    BigDecimal calcularImpuestoPais(BigDecimal monto);
}
