package mx.com.qtx.cotizador.dominio.promos;

/**
 * Clase abstracta base para promociones simples.
 * Proporciona la estructura básica para promociones que no requieren
 * acumulación con otras promociones, sirviendo como punto de partida
 * para implementaciones específicas de descuentos.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:20:41 p. m.
 */
public abstract class PromBase extends Promocion {


	/**
	 * Constructor que inicializa una promoción base con descripción y nombre.
	 *
	 * @param descripcion Descripción detallada de la promoción
	 * @param nombre Nombre identificativo de la promoción
	 */
	public PromBase(String descripcion, String nombre) {
		super(descripcion, nombre);
	}

}