package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;

/**
 * Implementación de promoción tipo "Lleve N, Pague M".
 * Para cada grupo de N unidades, el cliente paga solo por M unidades,
 * aplicando un descuento implícito en las unidades adicionales del grupo.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:01 p. m.
 */
public class PromNXM extends PromBase {

	/** Número de unidades que debe llevar el cliente */
	private int lleveN;
	/** Número de unidades que paga el cliente por cada grupo de N */
	private int pagueM;


	/**
	 * Constructor que crea una promoción N X M con los parámetros especificados.
	 *
	 * @param n Número de unidades que debe llevar el cliente
	 * @param m Número de unidades que paga el cliente por cada grupo de N
	 * @throws IllegalArgumentException si los parámetros son inválidos
	 */
	public PromNXM(int n, int m) {
		super(n + " X " + m, "Lleve " + n + ", pague " + m);
		ValidationUtils.validateNXMParameters(n, m);
		this.lleveN = n;
		this.pagueM = m;
	}


	/**
	 * Obtiene el número de unidades que debe llevar el cliente.
	 *
	 * @return Número de unidades para el grupo
	 */
	public int getLleveN() {
		return lleveN;
	}


	/**
	 * Establece el número de unidades que debe llevar el cliente.
	 *
	 * @param lleveN Número de unidades para el grupo
	 * @throws IllegalArgumentException si el valor es inválido
	 */
	public void setLleveN(int lleveN) {
		ValidationUtils.validateNXMParameters(lleveN, this.pagueM);
		this.lleveN = lleveN;
	}


	/**
	 * Obtiene el número de unidades que paga el cliente por cada grupo.
	 *
	 * @return Número de unidades a pagar por grupo
	 */
	public int getPagueM() {
		return pagueM;
	}


	/**
	 * Establece el número de unidades que paga el cliente por cada grupo.
	 *
	 * @param pagueM Número de unidades a pagar por grupo
	 * @throws IllegalArgumentException si el valor es inválido
	 */
	public void setPagueM(int pagueM) {
		ValidationUtils.validateNXMParameters(this.lleveN, pagueM);
		this.pagueM = pagueM;
	}


	/**
	 * Calcula el importe total aplicando la promoción "Lleve N, Pague M".
	 * Divide las unidades en grupos completos y calcula el precio basado en
	 * pagar solo M unidades por cada grupo de N.
	 *
	 * @param nUnidades Cantidad total de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe total calculado con la promoción aplicada
	 * @throws IllegalArgumentException si los parámetros son inválidos
	 */
	public BigDecimal calcularImportePromocion(int nUnidades, BigDecimal precioBase){
	    // Validar parámetros de entrada
	    ValidationUtils.validateParametrosCalculoPromocion(nUnidades, precioBase);

	    // Calcular grupos completos de N unidades y unidades restantes
	    int gruposCompletos = nUnidades / this.lleveN;
	    int unidadesRestantes = nUnidades % this.lleveN;

	    // Calcular total: (M * grupos) + restantes
	    BigDecimal total = precioBase
	        .multiply(BigDecimal.valueOf(gruposCompletos * this.pagueM + unidadesRestantes));

	    return total;
	}

}