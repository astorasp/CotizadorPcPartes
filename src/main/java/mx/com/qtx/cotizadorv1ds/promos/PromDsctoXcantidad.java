package mx.com.qtx.cotizadorv1ds.promos;

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