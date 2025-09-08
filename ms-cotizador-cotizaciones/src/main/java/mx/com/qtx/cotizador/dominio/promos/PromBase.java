package mx.com.qtx.cotizador.dominio.promos;

/**
 * Clase abstracta base para promociones no acumulables en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase representa las promociones base que no pueden ser decoradas con promociones
 * adicionales. Son las promociones "fundamentales" que establecen el tipo básico de descuento
 * o beneficio a aplicar, sin posibilidad de composición con otras promociones.
 * </p>
 *
 * <h3>Características principales:</h3>
 * <ul>
 *   <li><strong>No acumulable:</strong> No puede tener promociones adicionales aplicadas sobre ella</li>
 *   <li><strong>Promoción base:</strong> Establece el tipo fundamental de descuento</li>
 *   <li><strong>Implementación directa:</strong> Calcula el descuento sin depender de otras promociones</li>
 *   <li><strong>Terminal en la cadena:</strong> Es el último elemento en la cadena de decoradores</li>
 * </ul>
 *
 * <h3>Subclases concretas:</h3>
 * <ul>
 *   <li><strong>{@link PromSinDescto}:</strong> Sin descuento (precio base estándar)</li>
 *   <li><strong>{@link PromNXM}:</strong> Promoción "lleve N, pague M"</li>
 * </ul>
 *
 * <h3>Diferencia con PromAcumulable:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>PromBase</th><th>PromAcumulable</th></tr>
 *   <tr><td>Composición</td><td>No permite</td><td>Permite múltiples decoradores</td></tr>
 *   <tr><td>Herencia</td><td>Directa de Promocion</td><td>Hereda de Promocion</td></tr>
 *   <tr><td>Uso</td><td>Promoción final</td><td>Decorador intermedio</td></tr>
 *   <tr><td>Flexibilidad</td><td>Limitada</td><td>Alta (composable)</td></tr>
 * </table>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear promoción base
 * Promocion promoBase = new PromNXM(3, 2); // 3x2
 *
 * // No se puede decorar con PromAcumulable sobre PromBase
 * // Esto NO es válido:
 * // Promocion promoCompuesta = new PromDsctoPlano(promoBase, 10.0f);
 * }</pre>
 *
 * <h3>Patrón aplicado:</h3>
 * <p>
 * Esta clase forma parte del patrón Decorator donde representa el
 * <strong>ConcreteComponent</strong> - la implementación concreta que puede ser decorada,
 * pero que por diseño no permite decoración adicional.
 * </p>
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:20:41 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.Promocion
 * @see mx.com.qtx.cotizador.dominio.promos.PromAcumulable
 * @see mx.com.qtx.cotizador.dominio.promos.PromSinDescto
 * @see mx.com.qtx.cotizador.dominio.promos.PromNXM
 */
public abstract class PromBase extends Promocion {

	/**
	 * Constructor que inicializa una promoción base.
	 * <p>
	 * Crea una nueva instancia de promoción base con la descripción y nombre
	 * especificados. Las promociones base son terminales y no pueden tener
	 * promociones adicionales aplicadas sobre ellas.
	 * </p>
	 *
	 * @param descripcion Descripción detallada de la promoción base
	 * @param nombre Nombre identificativo corto de la promoción
	 */
	public PromBase(String descripcion, String nombre) {
		super(descripcion, nombre);
	}

}