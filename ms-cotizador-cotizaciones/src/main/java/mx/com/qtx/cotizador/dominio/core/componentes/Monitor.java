package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Clase que representa un monitor como componente de PC.
 * Extiende ComponenteSimple.
 */
public class Monitor extends ComponenteSimple {
	
	/**
	 * Constructor protegido para crear una instancia de Monitor.
	 * Inicializa el monitor con sus propiedades básicas como componente de PC.
	 * 
	 * @param id El identificador único asignado al monitor para su identificación en el sistema
	 * @param descripcion Una descripción detallada que explica las características y especificaciones del monitor
	 * @param marca El nombre de la marca fabricante del monitor (ej: "LG", "Samsung")
	 * @param modelo El modelo específico del monitor proporcionado por el fabricante
	 * @param costo El costo monetario de adquisición del monitor expresado en BigDecimal para precisión
	 * @param precioBase El precio base de venta del monitor antes de aplicar promociones
	 */
	protected Monitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}
	
	/**
	 * Obtiene la categoría específica de este componente.
	 * Devuelve "Monitor" para identificar que este componente es un dispositivo de visualización.
	 * 
	 * @return La cadena "Monitor" que representa la categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "Monitor";
	}

}
