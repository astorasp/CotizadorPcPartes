package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;

/**
 * Implementación de promoción "Lleve N, Pague M" en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase implementa una de las promociones más populares en el comercio minorista:
 * "lleve N unidades, pague solo M unidades". Es una promoción base que no puede
 * ser decorada con promociones adicionales, pero puede servir como base para
 * descuentos acumulables.
 * </p>
 *
 * <h3>Funcionamiento matemático:</h3>
 * <p>
 * Para una promoción N×M, el cálculo se basa en grupos de N unidades donde solo
 * se pagan M unidades por cada grupo completo.
 * </p>
 *
 * <h3>Fórmula de cálculo:</h3>
 * <pre>
 * gruposCompletos = cantidad ÷ N
 * unidadesRestantes = cantidad mod N
 * unidadesPagadas = (gruposCompletos × M) + unidadesRestantes
 * precioFinal = unidadesPagadas × precioUnitario
 * </pre>
 *
 * <h3>Ejemplos de aplicación:</h3>
 * <table border="1">
 *   <tr><th>Promoción</th><th>Cantidad</th><th>Cálculo</th><th>Resultado</th></tr>
 *   <tr><td>3×2</td><td>5 unidades</td><td>(1×2) + (5%3=2) = 4 unidades</td><td>Paga 4 unidades</td></tr>
 *   <tr><td>3×2</td><td>7 unidades</td><td>(2×2) + (7%3=1) = 5 unidades</td><td>Paga 5 unidades</td></tr>
 *   <tr><td>2×1</td><td>5 unidades</td><td>(2×1) + (5%2=1) = 3 unidades</td><td>Paga 3 unidades</td></tr>
 * </table>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear promoción 3x2
 * Promocion promo3x2 = new PromNXM(3, 2);
 *
 * // Calcular precio para 7 unidades a $100 cada una
 * BigDecimal precioFinal = promo3x2.calcularImportePromocion(7, new BigDecimal("100.00"));
 * // Resultado: 5 × 100 = 500.00 (en lugar de 7 × 100 = 700.00)
 * }</pre>
 *
 * <h3>Restricciones y validaciones:</h3>
 * <ul>
 *   <li><strong>N > M:</strong> Debe cumplirse que N > M para que haya descuento</li>
 *   <li><strong>N > 0:</strong> N debe ser un número positivo mayor que cero</li>
 *   <li><strong>M ≥ 0:</strong> M puede ser cero (regalo) o positivo</li>
 *   <li><strong>M ≤ N:</strong> No tiene sentido que M > N</li>
 * </ul>
 *
 * <h3>Casos de uso típicos:</h3>
 * <ul>
 *   <li><strong>3×2:</strong> "Lleve 3, pague 2" (promoción más común)</li>
 *   <li><strong>2×1:</strong> "Lleve 2, pague 1" (50% de descuento)</li>
 *   <li><strong>5×4:</strong> "Lleve 5, pague 4" (20% de descuento)</li>
 *   <li><strong>4×3:</strong> "Lleve 4, pague 3" (25% de descuento)</li>
 * </ul>
 *
 * <h3>Combinación con otras promociones:</h3>
 * <p>
 * Como promoción base, puede ser decorada con descuentos adicionales:
 * </p>
 * <pre>{@code
 * // 3x2 con 10% adicional
 * Promocion combo = new PromDsctoPlano(new PromNXM(3, 2), 10.0f);
 * }</pre>
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:01 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.PromBase
 * @see mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano
 */
public class PromNXM extends PromBase {

	/**
	 * Número de unidades que debe llevar el cliente (N en N×M).
	 * <p>
	 * Representa la cantidad de unidades que el cliente debe adquirir
	 * para poder aplicar la promoción.
	 * </p>
	 */
	private int lleveN;

	/**
	 * Número de unidades que paga el cliente (M en N×M).
	 * <p>
	 * Representa la cantidad efectiva de unidades que el cliente paga
	 * por cada grupo de N unidades.
	 * </p>
	 */
	private int pagueM;

	/**
	 * Constructor que crea una promoción "Lleve N, Pague M".
	 * <p>
	 * Inicializa la promoción con los parámetros N y M especificados.
	 * Crea automáticamente la descripción y nombre de la promoción.
	 * </p>
	 *
	 * @param n Cantidad de unidades que debe llevar (debe ser > 0)
	 * @param m Cantidad de unidades que paga (debe ser ≥ 0 y ≤ n)
	 * @throws IllegalArgumentException si n ≤ 0, m < 0, o m > n
	 */
	public PromNXM(int n, int m) {
		super(n + " X " + m, "Lleve " + n + ", pague " + m);
		this.lleveN = n;
		this.pagueM = m;
	}

	/**
	 * Obtiene el valor de N (unidades que debe llevar).
	 *
	 * @return El número de unidades que debe llevar el cliente
	 */
	public int getLleveN() {
		return lleveN;
	}

	/**
	 * Establece el valor de N (unidades que debe llevar).
	 *
	 * @param lleveN El nuevo valor de N
	 */
	public void setLleveN(int lleveN) {
		this.lleveN = lleveN;
	}

	/**
	 * Obtiene el valor de M (unidades que paga).
	 *
	 * @return El número de unidades que paga el cliente
	 */
	public int getPagueM() {
		return pagueM;
	}

	/**
	 * Establece el valor de M (unidades que paga).
	 *
	 * @param pagueM El nuevo valor de M
	 */
	public void setPagueM(int pagueM) {
		this.pagueM = pagueM;
	}

	/**
	 * Calcula el importe final aplicando la promoción "Lleve N, Pague M".
	 * <p>
	 * Divide la cantidad total en grupos de N unidades y calcula el precio
	 * basado en pagar solo M unidades por cada grupo completo, más las
	 * unidades restantes que no completan un grupo.
	 * </p>
	 *
	 * @param nUnidades Cantidad total de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe final después de aplicar la promoción N×M
	 */
	public BigDecimal calcularImportePromocion(int nUnidades, BigDecimal precioBase){

	    // Calcular grupos completos de N unidades y unidades restantes
	    int gruposCompletos = nUnidades / this.lleveN;
	    int unidadesRestantes = nUnidades % this.lleveN;

	    // Calcular total: (M * grupos) + restantes
	    BigDecimal total = precioBase
	        .multiply(BigDecimal.valueOf(gruposCompletos * this.pagueM + unidadesRestantes));

	    return total;
	}

}