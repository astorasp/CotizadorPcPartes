package mx.com.qtx.cotizador.dominio.promos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Constructor de promociones que implementa el patrón Builder.
 * Permite construir promociones complejas de manera fluida, combinando
 * promociones base con descuentos acumulables planos y por cantidad.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:17:34 p. m.
 */
public class PromocionBuilder {

	/** Constante para promoción base sin descuento */
	static final int PROM_BASE_SIN_DSCTO = 1;
	/** Constante para promoción base N X M */
	static final int PROM_BASE_NXM = 2;
	/** Tipo de promoción base seleccionada */
	private int tipoPromocionBase;
	/** Parámetro N para promoción N X M */
	private int n;
	/** Parámetro M para promoción N X M */
	private int m;
	
	/** Lista de descuentos planos porcentuales a acumular */
	private List<Float> lstDsctosPlanos;
	/** Lista de mapas de descuentos por cantidad a acumular */
	private List<Map<Integer,Double>> lstMapsCantVsDscto;

	/**
	 * Constructor que inicializa las listas de descuentos.
	 */
	public PromocionBuilder(){
		this.lstDsctosPlanos = new ArrayList<>();
		this.lstMapsCantVsDscto = new ArrayList<>();
	}
	
	/**
	 * Obtiene el tipo de promoción base.
	 *
	 * @return El tipo de promoción base
	 */
	int getTipoPromocionBase() {
		return tipoPromocionBase;
	}

	/**
	 * Obtiene el parámetro N para promoción N X M.
	 *
	 * @return El valor de N
	 */
	int getN() {
		return n;
	}

	/**
	 * Establece el parámetro N para promoción N X M.
	 *
	 * @param n El valor de N a establecer
	 */
	void setN(int n) {
		this.n = n;
	}

	/**
	 * Obtiene el parámetro M para promoción N X M.
	 *
	 * @return El valor de M
	 */
	int getM() {
		return m;
	}

	/**
	 * Establece el parámetro M para promoción N X M.
	 *
	 * @param m El valor de M a establecer
	 */
	void setM(int m) {
		this.m = m;
	}

	/**
	 * Obtiene la lista de descuentos planos.
	 *
	 * @return Lista de porcentajes de descuento plano
	 */
	List<Float> getLstDsctosPlanos() {
		return lstDsctosPlanos;
	}


	/**
	 * Obtiene la lista de mapas de descuentos por cantidad.
	 *
	 * @return Lista de mapas cantidad vs descuento
	 */
	List<Map<Integer, Double>> getLstMapsCantVsDscto() {
		return lstMapsCantVsDscto;
	}

	/**
	 * Configura la promoción base como sin descuento.
	 *
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PromocionBuilder conPromocionBaseSinDscto(){
		this.tipoPromocionBase = PROM_BASE_SIN_DSCTO;
		return this;
	}

	/**
	 * Configura la promoción base como N X M con los parámetros especificados.
	 *
	 * @param n Número de unidades que debe llevar el cliente
	 * @param m Número de unidades que paga el cliente
	 * @return Esta instancia del builder para encadenamiento
	 * @throws IllegalArgumentException si los parámetros son inválidos
	 */
	public PromocionBuilder conPromocionBaseNXM(int n, int m){
		ValidationUtils.validateNXMParameters(n, m);
		this.tipoPromocionBase = PROM_BASE_NXM;
		this.n = n;
		this.m = m;
		return this;
	}

	/**
	 * Agrega un descuento plano porcentual a la lista de acumulaciones.
	 *
	 * @param porcDscto Porcentaje de descuento a aplicar (ej: 7.5 para 7.5%)
	 * @return Esta instancia del builder para encadenamiento
	 * @throws IllegalArgumentException si el porcentaje es inválido
	 */
	public PromocionBuilder agregarDsctoPlano(float porcDscto){
		ValidationUtils.validatePorcentajeDescuento(porcDscto, "porcDscto");
		this.lstDsctosPlanos.add(porcDscto);
		return this;

	}

	/**
	 * Agrega un descuento por cantidad basado en el mapa proporcionado.
	 *
	 * @param mapCantVsDscto Mapa que asocia cantidades mínimas con porcentajes de descuento
	 * @return Esta instancia del builder para encadenamiento
	 * @throws IllegalArgumentException si el mapa es inválido
	 */
	public PromocionBuilder agregarDsctoXcantidad(Map<Integer,Double> mapCantVsDscto){
		ValidationUtils.validateMapaDescuentosPorCantidad(mapCantVsDscto);
		this.lstMapsCantVsDscto.add(mapCantVsDscto);
		return this;

	}

	/**
	 * Construye y devuelve la promoción con la configuración actual.
	 *
	 * @return La promoción construida
	 */
	public Promocion build() {
		return Promocion.crearPromocion(this);
	}

	/**
	 * Representación en cadena del estado actual del builder.
	 *
	 * @return Cadena con la información del builder
	 */
	@Override
	public String toString() {
		return "PromocionBuilder [tipoPromocionBase=" + tipoPromocionBase + ", n=" + n + ", m=" + m
				+ ", lstDsctosPlanos=" + lstDsctosPlanos + ", lstMapsCantVsDscto=" + lstMapsCantVsDscto + "]";
	}
	
	
}