package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;

/**
 * Implementación de promoción que aplica un descuento plano porcentual
 * sobre el resultado de una promoción base. Permite acumular un descuento
 * fijo adicional al cálculo de la promoción base.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:17 p. m.
 */
public class PromDsctoPlano extends PromAcumulable {

	/** Porcentaje de descuento a aplicar (ej: 7.5 para 7.5%) */
	private float porcDescto; // Se recibe como 7.5 para 7.5%


	/**
	 * Constructor que crea una promoción de descuento plano con promoción base y porcentaje.
	 *
	 * @param promoBase Promoción base sobre la cual aplicar el descuento adicional
	 * @param porcDescto Porcentaje de descuento a aplicar (ej: 7.5 para 7.5%)
	 */
	public PromDsctoPlano(Promocion promoBase, float porcDescto) {
		super(String.format("Descuento Plano del %4.2f %%",porcDescto), "Dscto Plano", promoBase);
		this.porcDescto = porcDescto;
	}


	/**
	 * Calcula el importe total aplicando el descuento plano sobre la promoción base.
	 * Primero calcula el importe usando la promoción base, luego aplica el descuento porcentual.
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe total con el descuento plano aplicado
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		BigDecimal baseCalculo = this.promoBase.calcularImportePromocion(cant, precioBase);
		BigDecimal porcDscto = new BigDecimal(porcDescto).divide(new BigDecimal(100));
		BigDecimal importeDscto = baseCalculo.multiply(porcDscto);
		return baseCalculo.subtract(importeDscto);
	}

}