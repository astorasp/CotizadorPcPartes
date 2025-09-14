package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implementación de promoción que aplica descuentos basados en umbrales de cantidad.
 * Utiliza un mapa que asocia cantidades mínimas con porcentajes de descuento,
 * aplicando el descuento correspondiente al umbral más alto que cumpla la cantidad solicitada.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:20 p. m.
 */
public class PromDsctoXcantidad extends PromAcumulable {

	/** Mapa que asocia cantidades mínimas con porcentajes de descuento */
	private Map<Integer,Double> mapCantidadVsDscto;


	/**
	 * Constructor que crea una promoción de descuento por cantidad con promoción base y mapa de descuentos.
	 *
	 * @param promoBase Promoción base sobre la cual aplicar los descuentos por cantidad
	 * @param mapCantidadVsDscto Mapa que asocia cantidades mínimas con porcentajes de descuento
	 * @throws IllegalArgumentException si los parámetros son inválidos
	 */
	public PromDsctoXcantidad(Promocion promoBase, Map<Integer, Double> mapCantidadVsDscto) {
		super("Dscto con base en tabla de cantidades y descuentos" + mapCantidadVsDscto, "Dscto x cantidad", promoBase);
		ValidationUtils.validateNotNull(promoBase, "promoBase");
		ValidationUtils.validateMapaDescuentosPorCantidad(mapCantidadVsDscto);
		this.mapCantidadVsDscto = mapCantidadVsDscto;
	}

	/**
	 * Calcula el importe total aplicando descuento según la cantidad usando la tabla de descuentos.
	 * Busca el umbral de cantidad más alto aplicable y aplica el porcentaje de descuento correspondiente.
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe total con el descuento por cantidad aplicado
	 * @throws IllegalArgumentException si los parámetros son inválidos
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		ValidationUtils.validateParametrosCalculoPromocion(cant, precioBase);
		
		BigDecimal baseCalculo = this.promoBase.calcularImportePromocion(cant, precioBase);
		
		// Si no hay cantidad, devolver el precio base sin descuento
		if (cant <= 0) {
			return baseCalculo;
		}
		
		// Buscar la escala de descuento aplicable
		Integer keyDscto = this.mapCantidadVsDscto.keySet()
											  .stream()
											  .sorted()                           // ordena asc
											  .filter(k -> k <= cant)             // elimina llaves mayores que la cantidad
											  .sorted((n,n2) -> n <= n2 ? 1 : -1) // Ordena elementos filtrados dsc
											  .findFirst()                        // toma el primero, devuele optional
											  .orElse(null);                      // devuelve null si no hay escala aplicable
		
		// Si no hay escala aplicable, devolver precio base sin descuento
		if (keyDscto == null) {
			return baseCalculo;
		}
		
		BigDecimal porcDscto = new BigDecimal(mapCantidadVsDscto.get(keyDscto)).divide(new BigDecimal(100));

		BigDecimal importeDscto = baseCalculo.multiply(porcDscto);
		return baseCalculo.subtract(importeDscto);

	}

	/**
	 * Devuelve el mapa de cantidad mínima vs descuento configurado.
	 * @return mapa de escalas {cantidadMinima -> porcentajeDescuento}
	 */
	public Map<Integer, Double> getMapCantidadVsDscto() {
		return this.mapCantidadVsDscto;
	}

}