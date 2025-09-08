package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;

/**
 * Implementación de promoción que aplica un descuento porcentual plano sobre otra promoción.
 * <p>
 * Esta clase implementa el patrón Decorator aplicando un descuento porcentual fijo
 * sobre el resultado de una promoción base. El descuento se calcula como un porcentaje
 * del importe total resultante de la promoción base, permitiendo crear descuentos
 * acumulables de manera flexible.
 * </p>
 *
 * <h3>Funcionamiento:</h3>
 * <p>
 * El cálculo se realiza en dos pasos:
 * </p>
 * <ol>
 *   <li>Primero calcula el importe usando la promoción base</li>
 *   <li>Luego aplica el descuento porcentual sobre ese importe</li>
 * </ol>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * importeFinal = importeBase - (importeBase × porcentajeDescuento / 100)
 * </pre>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Promoción base: 3x2
 * Promocion base = new PromNXM(3, 2);
 *
 * // Aplicar 10% de descuento adicional
 * Promocion descuento = new PromDsctoPlano(base, 10.0f);
 *
 * // Calcular precio para 6 unidades a $100 cada una
 * BigDecimal precioFinal = descuento.calcularImportePromocion(6, new BigDecimal("100.00"));
 * // Resultado: (6 × 100 × 2/3) × 0.9 = 360 × 0.9 = 324.00
 * }</pre>
 *
 * <h3>Casos de uso típicos:</h3>
 * <ul>
 *   <li><strong>Descuentos adicionales:</strong> "3x2 con 5% adicional"</li>
 *   <li><strong>Promociones combinadas:</strong> "Lleve 5 pague 4 + 15% descuento"</li>
 *   <li><strong>Ofertas especiales:</strong> "Precio regular con 20% off"</li>
 * </ul>
 *
 * <h3>Consideraciones:</h3>
 * <ul>
 *   <li>El porcentaje debe ser positivo (0 < porcentaje ≤ 100)</li>
 *   <li>El descuento se aplica sobre el importe ya calculado por la promoción base</li>
 *   <li>Mantiene la precisión decimal usando {@link BigDecimal}</li>
 *   <li>Es compatible con cualquier tipo de promoción base</li>
 * </ul>
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:17 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.PromAcumulable
 * @see mx.com.qtx.cotizador.dominio.promos.Promocion
 */
public class PromDsctoPlano extends PromAcumulable {

	/**
	 * Porcentaje de descuento a aplicar (ej: 10.5 para 10.5%).
	 * <p>
	 * Este valor representa el porcentaje que se descontará del importe
	 * calculado por la promoción base. Debe ser un valor positivo entre
	 * 0 y 100 (aunque 0 no tendría efecto práctico).
	 * </p>
	 */
	private float porcDescto;

	/**
	 * Constructor que crea un descuento plano sobre una promoción base.
	 * <p>
	 * Inicializa la promoción con el porcentaje de descuento especificado
	 * y la promoción base sobre la cual aplicar el descuento.
	 * </p>
	 *
	 * @param promoBase Promoción base sobre la cual aplicar el descuento (no puede ser null)
	 * @param porcDescto Porcentaje de descuento a aplicar (debe ser positivo y ≤ 100)
	 * @throws IllegalArgumentException si promoBase es null o porcDescto es negativo
	 */
	public PromDsctoPlano(Promocion promoBase, float porcDescto) {
		super(String.format("Descuento Plano del %4.2f %%",porcDescto), "Dscto Plano", promoBase);
		this.porcDescto = porcDescto;
	}

	/**
	 * Calcula el importe final aplicando el descuento porcentual plano.
	 * <p>
	 * Primero calcula el importe usando la promoción base, luego aplica
	 * el descuento porcentual sobre ese importe resultante.
	 * </p>
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe final después de aplicar el descuento plano
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		BigDecimal baseCalculo = this.promoBase.calcularImportePromocion(cant, precioBase);
		BigDecimal porcDscto = new BigDecimal(porcDescto).divide(new BigDecimal(100));
		BigDecimal importeDscto = baseCalculo.multiply(porcDscto);
		return baseCalculo.subtract(importeDscto);
	}

}