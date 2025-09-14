package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Representa un componente de tipo monitor en el sistema de cotización.
 * Extiende ComponenteSimple y representa monitores de computadora que pueden ser
 * incluidos en configuraciones de PC.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public class Monitor extends ComponenteSimple {

	/**
	 * Constructor protegido para crear un monitor con sus propiedades básicas.
	 * Inicializa las propiedades heredadas de ComponenteSimple.
	 *
	 * @param id Identificador único del monitor
	 * @param descripcion Descripción detallada del monitor
	 * @param marca Marca del monitor
	 * @param modelo Modelo específico del monitor
	 * @param costo Costo de adquisición del monitor
	 * @param precioBase Precio base de venta del monitor
	 */
	protected Monitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}

	/**
	 * Obtiene la categoría de este componente.
	 *
	 * @return "Monitor" como categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "Monitor";
	}

}
