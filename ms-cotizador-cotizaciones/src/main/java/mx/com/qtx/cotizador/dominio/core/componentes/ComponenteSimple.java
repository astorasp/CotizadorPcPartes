package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Clase abstracta para componentes simples.
 * Extiende Componente para representar componentes individuales no compuestos.
 */
public abstract class ComponenteSimple extends Componente{

	/**
	 * Constructor para crear una instancia de ComponenteSimple.
	 * Inicializa el componente simple con sus propiedades básicas.
	 * 
	 * @param id El identificador único asignado al componente para su identificación en el sistema
	 * @param descripcion Una descripción detallada que explica las características y especificaciones del componente
	 * @param marca El nombre de la marca fabricante del componente
	 * @param modelo El modelo específico del componente proporcionado por el fabricante
	 * @param costo El costo monetario de adquisición del componente expresado en BigDecimal para precisión
	 * @param precioBase El precio base de venta del componente antes de aplicar promociones
	 */
	public ComponenteSimple(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}


}