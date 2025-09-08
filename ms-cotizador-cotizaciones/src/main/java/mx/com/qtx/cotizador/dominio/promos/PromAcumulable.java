package mx.com.qtx.cotizador.dominio.promos;

/**
 * Clase abstracta base para promociones acumulables en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase implementa el patrón Decorator para permitir la composición de múltiples
 * promociones sobre una promoción base. Las promociones acumulables pueden aplicarse
 * sobre otras promociones existentes, creando una cadena de descuentos y beneficios
 * que se calculan de manera secuencial.
 * </p>
 *
 * <h3>Patrón Decorator:</h3>
 * <ul>
 *   <li><strong>Component:</strong> {@link Promocion} - Interfaz base de promociones</li>
 *   <li><strong>ConcreteComponent:</strong> {@link PromBase} - Promociones base (no decoradas)</li>
 *   <li><strong>Decorator:</strong> {@link PromAcumulable} - Esta clase que envuelve promociones</li>
 *   <li><strong>ConcreteDecorator:</strong> {@link PromDsctoPlano}, {@link PromDsctoXcantidad}</li>
 * </ul>
 *
 * <h3>Funcionamiento:</h3>
 * <p>
 * Las promociones acumulables funcionan aplicando primero la promoción base ({@link #promoBase})
 * y luego aplicando el descuento o beneficio adicional definido por la subclase concreta.
 * Este proceso permite crear promociones complejas como:
 * </p>
 * <pre>
 * Promocion compleja = new PromDsctoPlano(
 *     new PromDsctoXcantidad(
 *         new PromNXM(3, 2), // Base: 3x2
 *         mapaDescuentos    // Primer decorador
 *     ),
 *     10.0f               // Segundo decorador: 10% adicional
 * );
 * </pre>
 *
 * <h3>Subclases concretas:</h3>
 * <ul>
 *   <li><strong>{@link PromDsctoPlano}:</strong> Aplica un descuento porcentual plano</li>
 *   <li><strong>{@link PromDsctoXcantidad}:</strong> Aplica descuentos basados en cantidad</li>
 * </ul>
 *
 * <h3>Consideraciones de diseño:</h3>
 * <ul>
 *   <li>La promoción base nunca puede ser null</li>
 *   <li>El cálculo se realiza en cadena: base → decorador → decorador</li>
 *   <li>Mantiene la compatibilidad con la interfaz {@link Promocion}</li>
 *   <li>Permite composición recursiva de promociones</li>
 * </ul>
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:14 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.Promocion
 * @see mx.com.qtx.cotizador.dominio.promos.PromBase
 * @see mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano
 * @see mx.com.qtx.cotizador.dominio.promos.PromDsctoXcantidad
 * @see mx.com.qtx.cotizador.dominio.core.componentes.IPromocion
 */
public abstract class PromAcumulable extends Promocion {

	/**
	 * Promoción base sobre la cual se aplicará esta promoción acumulable.
	 * <p>
	 * Esta promoción se ejecuta primero en la cadena de cálculo, y su resultado
	 * sirve como base para aplicar el descuento o beneficio adicional de esta
	 * promoción acumulable.
	 * </p>
	 */
	protected Promocion promoBase;

	/**
	 * Constructor que inicializa una promoción acumulable.
	 * <p>
	 * Crea una nueva instancia de promoción acumulable configurada con la descripción,
	 * nombre y promoción base especificados. La promoción base no puede ser null
	 * ya que es necesaria para el funcionamiento del patrón Decorator.
	 * </p>
	 *
	 * @param descripcion Descripción textual de la promoción acumulable
	 * @param nombre Nombre corto identificativo de la promoción
	 * @param promoBase Promoción base sobre la cual aplicar esta promoción (no puede ser null)
	 * @throws IllegalArgumentException si promoBase es null
	 */
	public PromAcumulable(String descripcion, String nombre, Promocion promoBase) {
		super(descripcion, nombre);
		this.promoBase = promoBase;
	}

}