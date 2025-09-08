package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implementación de promoción que aplica descuentos basados en rangos de cantidad.
 * <p>
 * Esta clase implementa el patrón Decorator aplicando descuentos porcentuales variables
 * según la cantidad de unidades compradas. Utiliza una tabla de configuración que asocia
 * rangos de cantidad con porcentajes de descuento específicos, permitiendo crear
 * promociones escalonadas del tipo "compre más, pague menos".
 * </p>
 *
 * <h3>Funcionamiento del algoritmo:</h3>
 * <p>
 * El sistema busca en la tabla de descuentos la cantidad máxima que es menor o igual
 * a la cantidad solicitada, aplicando el descuento correspondiente a ese rango.
 * </p>
 *
 * <h3>Ejemplo de configuración:</h3>
 * <pre>{@code
 * Map<Integer, Double> descuentos = Map.of(
 *     5,  5.0,   // 5% descuento para 5+ unidades
 *     10, 10.0,  // 10% descuento para 10+ unidades
 *     20, 15.0   // 15% descuento para 20+ unidades
 * );
 *
 * Promocion promoCantidad = new PromDsctoXcantidad(basePromo, descuentos);
 * }</pre>
 *
 * <h3>Lógica de selección de descuento:</h3>
 * <table border="1">
 *   <tr><th>Cantidad</th><th>Descuento aplicado</th><th>Explicación</th></tr>
 *   <tr><td>3 unidades</td><td>0% (sin descuento)</td><td>No alcanza ningún umbral</td></tr>
 *   <tr><td>7 unidades</td><td>5%</td><td>7 ≥ 5, aplica descuento del nivel 5</td></tr>
 *   <tr><td>15 unidades</td><td>10%</td><td>15 ≥ 10, aplica descuento del nivel 10</td></tr>
 *   <tr><td>25 unidades</td><td>15%</td><td>25 ≥ 20, aplica descuento del nivel 20</td></tr>
 * </table>
 *
 * <h3>Algoritmo de búsqueda:</h3>
 * <ol>
 *   <li>Filtra las claves del mapa que son ≤ cantidad solicitada</li>
 *   <li>Ordena las claves filtradas de mayor a menor</li>
 *   <li>Selecciona la primera (mayor cantidad aplicable)</li>
 *   <li>Aplica el porcentaje de descuento correspondiente</li>
 * </ol>
 *
 * <h3>Casos de uso típicos:</h3>
 * <ul>
 *   <li><strong>Descuentos escalonados:</strong> "5% off en 10+, 10% off en 25+, 15% off en 50+"</li>
 *   <li><strong>Promociones por volumen:</strong> "Compre más, ahorre más"</li>
 *   <li><strong>Estrategias comerciales:</strong> Incentivar compras en mayor cantidad</li>
 * </ul>
 *
 * <h3>Consideraciones importantes:</h3>
 * <ul>
 *   <li>Si no hay cantidad en el mapa que sea ≤ cantidad solicitada, no se aplica descuento</li>
 *   <li>Se recomienda ordenar el mapa por cantidad ascendente para mejor legibilidad</li>
 *   <li>Los porcentajes deben ser valores positivos (0 < porcentaje ≤ 100)</li>
 *   <li>El mapa no puede ser null o vacío</li>
 * </ul>
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:20 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.PromAcumulable
 * @see java.util.Map
 */
public class PromDsctoXcantidad extends PromAcumulable {

	/**
	 * Tabla de configuración que asocia cantidades mínimas con porcentajes de descuento.
	 * <p>
	 * La clave representa la cantidad mínima para aplicar el descuento,
	 * y el valor representa el porcentaje de descuento a aplicar.
	 * Ejemplo: {5=5.0, 10=10.0, 20=15.0}
	 * </p>
	 */
	private Map<Integer,Double> mapCantidadVsDscto;

	/**
	 * Constructor que crea un descuento por cantidad sobre una promoción base.
	 * <p>
	 * Inicializa la promoción con la tabla de descuentos por cantidad especificada
	 * y la promoción base sobre la cual aplicar los descuentos adicionales.
	 * </p>
	 *
	 * @param promoBase Promoción base sobre la cual aplicar los descuentos por cantidad
	 * @param mapCantidadVsDscto Mapa que asocia cantidades mínimas con porcentajes de descuento
	 * @throws IllegalArgumentException si promoBase es null o mapCantidadVsDscto es null/vacío
	 */
	public PromDsctoXcantidad(Promocion promoBase, Map<Integer, Double> mapCantidadVsDscto) {
		super("Dscto con base en tabla de cantidades y descuentos" + mapCantidadVsDscto, "Dscto x cantidad", promoBase);
		this.mapCantidadVsDscto = mapCantidadVsDscto;
	}

	/**
	 * Calcula el importe final aplicando el descuento basado en cantidad.
	 * <p>
	 * Primero calcula el importe usando la promoción base, luego determina
	 * el porcentaje de descuento aplicable según la cantidad solicitada
	 * y finalmente aplica ese descuento sobre el importe base.
	 * </p>
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe final después de aplicar el descuento por cantidad
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){

		BigDecimal baseCalculo = this.promoBase.calcularImportePromocion(cant, precioBase);


		int keyDscto = this.mapCantidadVsDscto.keySet()
											  .stream()
											  .sorted()                           // ordena asc
											  .filter(k -> k <= cant)             // elimina llaves mayores que la cantidad
											  .sorted((n,n2) -> n <= n2 ? 1 : -1) // Ordena elementos filtrados dsc
											  .findFirst()                        // toma el primero, devuele optional
											  .get();                             // toma el valor

		BigDecimal porcDscto = new BigDecimal(mapCantidadVsDscto.get(keyDscto)).divide(new BigDecimal(100));

		BigDecimal importeDscto = baseCalculo.multiply(porcDscto);
		return baseCalculo.subtract(importeDscto);

	}

}