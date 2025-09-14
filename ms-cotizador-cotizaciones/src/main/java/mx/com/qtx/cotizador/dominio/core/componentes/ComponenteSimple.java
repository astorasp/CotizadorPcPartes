package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Clase abstracta base para componentes simples del sistema.
 * Representa componentes de hardware individuales que no contienen subcomponentes,
 * como monitores, discos duros, tarjetas de video, etc.
 * Extiende la clase Componente y proporciona una base común para estos tipos de componentes.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public abstract class ComponenteSimple extends Componente{

	/**
	 * Constructor que inicializa un componente simple con sus propiedades básicas.
	 * Delega la inicialización al constructor de la clase padre Componente.
	 *
	 * @param id Identificador único del componente
	 * @param descripcion Descripción detallada del componente
	 * @param marca Marca del componente
	 * @param modelo Modelo específico del componente
	 * @param costo Costo de adquisición del componente
	 * @param precioBase Precio base de venta del componente
	 */
	public ComponenteSimple(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}


}