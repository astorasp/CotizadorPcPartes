package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Interfaz que define el contrato para calcular promociones en componentes.
 * <p>
 * Esta interfaz establece el patrón Strategy para diferentes tipos de promociones
 * que pueden aplicarse a los componentes del sistema CotizadorPcPartes. Las
 * implementaciones concretas pueden calcular descuentos planos, promociones
 * por cantidad, ofertas N×M, u otros tipos de promociones comerciales.
 * </p>
 *
 * <h3>Implementaciones disponibles:</h3>
 * <ul>
 *   <li>{@link mx.com.qtx.cotizador.dominio.promos.PromSinDescto} - Sin descuento (precio base)</li>
 *   <li>{@link mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano} - Descuento plano porcentual</li>
 *   <li>{@link mx.com.qtx.cotizador.dominio.promos.PromDsctoXcantidad} - Descuento por cantidad</li>
 *   <li>{@link mx.com.qtx.cotizador.dominio.promos.PromNXM} - Promoción N×M (lleve N, pague M)</li>
 * </ul>
 *
 * <h3>Patrón Strategy:</h3>
 * <p>
 * Esta interfaz implementa el patrón Strategy permitiendo cambiar dinámicamente
 * el algoritmo de cálculo de promociones sin afectar al código cliente. Las
 * diferentes estrategias pueden combinarse usando el patrón Decorator para
 * promociones acumulables.
 * </p>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * IPromocion promocion = new PromDsctoPlano(10.0); // 10% de descuento
 * BigDecimal precioFinal = promocion.calcularImportePromocion(5, new BigDecimal("100.00"));
 * // Resultado: 450.00 (5 unidades × 90.00 cada una)
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente#cotizar(int)
 */
public interface IPromocion {

    /**
     * Calcula el importe total de una promoción aplicada a una cantidad específica de componentes.
     * <p>
     * Este método calcula el precio final después de aplicar la promoción correspondiente
     * al precio base del componente. El cálculo puede incluir descuentos porcentuales,
     * descuentos por cantidad, promociones N×M u otros algoritmos de promoción.
     * </p>
     *
     * <h4>Consideraciones importantes:</h4>
     * <ul>
     *   <li>El cálculo debe considerar la cantidad total de unidades</li>
     *   <li>Debe retornar el importe total (cantidad × precio_unitario_con_promoción)</li>
     *   <li>Si no hay promoción, debe retornar cantidad × precioBase</li>
     *   <li>Los cálculos deben ser precisos usando BigDecimal para evitar errores de redondeo</li>
     * </ul>
     *
     * @param cantidad Cantidad de unidades del componente a las que aplicar la promoción
     * @param precioBase Precio base unitario del componente antes de aplicar promociones
     * @return El importe total después de aplicar la promoción (cantidad × precio_con_descuento)
     * @throws IllegalArgumentException si cantidad es menor o igual a cero
     * @throws IllegalArgumentException si precioBase es null o menor o igual a cero
     */
    BigDecimal calcularImportePromocion(int cantidad, BigDecimal precioBase);
}
