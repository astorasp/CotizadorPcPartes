package mx.com.qtx.cotizador.dominio.promos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Constructor fluido (Builder Pattern) para crear promociones complejas en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase implementa el patrón Builder para facilitar la construcción de promociones
 * complejas de manera fluida y legible. Permite crear promociones que combinan
 * múltiples tipos de descuentos (N×M, descuentos planos, descuentos por cantidad)
 * en una única cadena de llamadas de método.
 * </p>
 *
 * <h3>Patrón Builder aplicado:</h3>
 * <ul>
 *   <li><strong>Constructores simples:</strong> Métodos que configuran aspectos individuales</li>
 *   <li><strong>Encadenamiento fluido:</strong> Cada método retorna this para permitir chaining</li>
 *   <li><strong>Construcción final:</strong> Método build() que crea el objeto final</li>
 *   <li><strong>Separación de construcción:</strong> Construcción compleja separada de representación</li>
 * </ul>
 *
 * <h3>Proceso de construcción:</h3>
 * <ol>
 *   <li><strong>Seleccionar promoción base:</strong> Elegir tipo fundamental (sin descuento, N×M)</li>
 *   <li><strong>Agregar decoradores:</strong> Aplicar descuentos adicionales en capas</li>
 *   <li><strong>Configurar parámetros:</strong> Establecer valores específicos para cada promoción</li>
 *   <li><strong>Construir objeto:</strong> Crear la instancia final de Promocion</li>
 * </ol>
 *
 * <h3>Ejemplos de uso:</h3>
 * <pre>{@code
 * // Ejemplo 1: 3x2 simple
 * Promocion promoSimple = Promocion.getBuilder()
 *     .conPromocionBaseNXM(3, 2)
 *     .build();
 *
 * // Ejemplo 2: 3x2 con 10% adicional
 * Promocion promoCompuesta = Promocion.getBuilder()
 *     .conPromocionBaseNXM(3, 2)
 *     .agregarDsctoPlano(10.0f)
 *     .build();
 *
 * // Ejemplo 3: Promoción compleja multi-nivel
 * Map<Integer, Double> descuentos = Map.of(5, 5.0, 10, 10.0, 20, 15.0);
 * Promocion promoCompleja = Promocion.getBuilder()
 *     .conPromocionBaseSinDscto()
 *     .agregarDsctoXcantidad(descuentos)
 *     .agregarDsctoPlano(5.0f)
 *     .build();
 * }</pre>
 *
 * <h3>Tipos de promociones base soportadas:</h3>
 * <ul>
 *   <li><strong>Sin descuento:</strong> Precio regular como base</li>
 *   <li><strong>N×M:</strong> Promoción "lleve N, pague M"</li>
 * </ul>
 *
 * <h3>Decoradores disponibles:</h3>
 * <ul>
 *   <li><strong>Descuento plano:</strong> Porcentaje fijo adicional</li>
 *   <li><strong>Descuento por cantidad:</strong> Escalas de descuento según cantidad</li>
 * </ul>
 *
 * <h3>Arquitectura interna:</h3>
 * <p>
 * El builder mantiene listas separadas para cada tipo de decorador:
 * </p>
 * <ul>
 *   <li>{@link #lstDsctosPlanos} - Lista de porcentajes de descuento plano</li>
 *   <li>{@link #lstMapsCantVsDscto} - Lista de mapas de descuento por cantidad</li>
 * </ul>
 *
 * <h3>Proceso de construcción final:</h3>
 * <p>
 * El método {@link #build()} delega la construcción final a
 * {@link Promocion#crearPromocion(PromocionBuilder)} que:
 * </p>
 * <ol>
 *   <li>Crea la promoción base según {@link #tipoPromocionBase}</li>
 *   <li>Aplica cada decorador en orden usando el patrón Decorator</li>
 *   <li>Retorna la promoción completamente configurada</li>
 * </ol>
 *
 * <h3>Beneficios del patrón Builder:</h3>
 * <ul>
 *   <li><strong>Legibilidad:</strong> Código expresivo y fácil de entender</li>
 *   <li><strong>Flexibilidad:</strong> Permite configuraciones complejas</li>
 *   <li><strong>Seguridad:</strong> Evita estados inválidos del objeto</li>
 *   <li><strong>Mantenibilidad:</strong> Fácil agregar nuevos tipos de promociones</li>
 *   <li><strong>Fluencia:</strong> Interfaz fluida que mejora la experiencia del desarrollador</li>
 * </ul>
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:17:34 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.Promocion
 * @see mx.com.qtx.cotizador.dominio.promos.PromSinDescto
 * @see mx.com.qtx.cotizador.dominio.promos.PromNXM
 * @see mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano
 * @see mx.com.qtx.cotizador.dominio.promos.PromDsctoXcantidad
 */
public class PromocionBuilder {

	static final int PROM_BASE_SIN_DSCTO = 1;
	static final int PROM_BASE_NXM = 2;
	private int tipoPromocionBase;
	private int n;
	private int m;
	
	private List<Float> lstDsctosPlanos;
	private List<Map<Integer,Double>> lstMapsCantVsDscto;

	public PromocionBuilder(){
		this.lstDsctosPlanos = new ArrayList<>();
		this.lstMapsCantVsDscto = new ArrayList<>();
	}	

	int getTipoPromocionBase() {
		return tipoPromocionBase;
	}

	int getN() {
		return n;
	}

	void setN(int n) {
		this.n = n;
	}

	int getM() {
		return m;
	}

	void setM(int m) {
		this.m = m;
	}

	List<Float> getLstDsctosPlanos() {
		return lstDsctosPlanos;
	}


	List<Map<Integer, Double>> getLstMapsCantVsDscto() {
		return lstMapsCantVsDscto;
	}

	public PromocionBuilder conPromocionBaseSinDscto(){
		this.tipoPromocionBase = PROM_BASE_SIN_DSCTO;
		return this;
	}

	/**
	 * 
	 * @param n
	 * @param m
	 */
	public PromocionBuilder conPromocionBaseNXM(int n, int m){
		this.tipoPromocionBase = PROM_BASE_NXM;
		this.n = n;
		this.m = m;
		return this;
	}

	/**
	 * 
	 * @param procDscto
	 */
	public PromocionBuilder agregarDsctoPlano(float porcDscto){
		this.lstDsctosPlanos.add(porcDscto);
		return this;

	}

	/**
	 * 
	 * @param mapCantVsDscto
	 */
	public PromocionBuilder agregarDsctoXcantidad(Map<Integer,Double> mapCantVsDscto){
		this.lstMapsCantVsDscto.add(mapCantVsDscto);
		return this;

	}

	public Promocion build() {
		return Promocion.crearPromocion(this);
	}

	@Override
	public String toString() {
		return "PromocionBuilder [tipoPromocionBase=" + tipoPromocionBase + ", n=" + n + ", m=" + m
				+ ", lstDsctosPlanos=" + lstDsctosPlanos + ", lstMapsCantVsDscto=" + lstMapsCantVsDscto + "]";
	}

	
	
}