package mx.com.qtx.cotizador.dominio.promos;

/**
 * Clase abstracta base para promociones acumulables.
 * Permite combinar múltiples promociones aplicando una promoción base
 * y luego acumulando descuentos adicionales sobre ella.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:14 p. m.
 */
public abstract class PromAcumulable extends Promocion {

	/** Promoción base sobre la cual se acumulan descuentos adicionales */
	protected Promocion promoBase;


	/**
	 * Constructor que inicializa una promoción acumulable con descripción, nombre y promoción base.
	 *
	 * @param descripcion Descripción detallada de la promoción
	 * @param nombre Nombre identificativo de la promoción
	 * @param promoBase Promoción base sobre la cual se aplicarán acumulaciones
	 * @throws IllegalArgumentException si promoBase es null o otros parámetros son inválidos
	 */
	public PromAcumulable(String descripcion, String nombre, Promocion promoBase) {
		super(descripcion, nombre);
		ValidationUtils.validateNotNull(promoBase, "promoBase");
		this.promoBase = promoBase;
	}

	/**
	 * Obtiene la promoción base sobre la cual se acumulan descuentos.
	 *
	 * @return Promoción base
	 */
	public Promocion getPromoBase() {
		return this.promoBase;
	}


	/**
	 * Método abstracto para calcular el importe de la promoción acumulable.
	 * Debe ser implementado por las subclases para definir la lógica específica
	 * de acumulación de descuentos.
	 *
	 * @param cant Cantidad de unidades a las que aplicar la promoción
	 * @param precioBase Precio base unitario del componente
	 */
//	public abstract BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);

}