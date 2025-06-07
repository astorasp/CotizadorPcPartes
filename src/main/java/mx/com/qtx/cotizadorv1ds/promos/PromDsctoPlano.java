package mx.com.qtx.cotizadorv1ds.promos;

import java.math.BigDecimal;

/**
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:17 p. m.
 */
public class PromDsctoPlano extends PromAcumulable {

	private float porcDescto; // Se recibe como 7.5 para 7.5%


	public PromDsctoPlano(Promocion promoBase, float porcDescto) {
		super(String.format("Descuento Plano del %4.2f %%",porcDescto), "Dscto Plano", promoBase);
		this.porcDescto = porcDescto;
	}


	/**
	 * 
	 * @param cant
	 * @param precioBase
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		BigDecimal baseCalculo = this.promoBase.calcularImportePromocion(cant, precioBase);
		BigDecimal porcDscto = new BigDecimal(porcDescto).divide(new BigDecimal(100));
		BigDecimal importeDscto = baseCalculo.multiply(porcDscto);
		return baseCalculo.subtract(importeDscto);
	}

}