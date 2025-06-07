package mx.com.qtx.cotizadorv1ds.promos;

/**
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:14 p. m.
 */
public abstract class PromAcumulable extends Promocion {

	protected Promocion promoBase;


	public PromAcumulable(String descripcion, String nombre, Promocion promoBase) {
		super(descripcion, nombre);
		this.promoBase = promoBase;
	}


	/**
	 * 
	 * @param cant
	 * @param precioBase
	 */
//	public abstract BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);

}