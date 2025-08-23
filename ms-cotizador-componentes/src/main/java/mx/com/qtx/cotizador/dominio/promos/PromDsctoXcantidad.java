package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:20 p. m.
 */
public class PromDsctoXcantidad extends PromAcumulable {

	private Map<Integer,Double> mapCantidadVsDscto;


	public PromDsctoXcantidad(Promocion promoBase, Map<Integer, Double> mapCantidadVsDscto) {
		super("Dscto con base en tabla de cantidades y descuentos" + mapCantidadVsDscto, "Dscto x cantidad", promoBase);
		this.mapCantidadVsDscto = mapCantidadVsDscto;
	}

	/**
	 * 
	 * @param cant
	 * @param precioBase
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		
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

}