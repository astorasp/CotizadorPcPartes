package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Clase que representa un disco duro como componente de PC.
 * Extiende ComponenteSimple e incluye capacidad de almacenamiento.
 */
public class DiscoDuro extends ComponenteSimple {
	private String capacidadAlm;

	/**
	 * Constructor protegido para crear una instancia de DiscoDuro.
	 * Inicializa el disco duro con sus propiedades específicas, incluyendo la capacidad de almacenamiento.
	 * 
	 * @param id El identificador único asignado al disco duro para su identificación en el sistema
	 * @param descripcion Una descripción detallada que explica las características y especificaciones del disco duro
	 * @param marca El nombre de la marca fabricante del disco duro (ej: "Western Digital", "Seagate")
	 * @param modelo El modelo específico del disco duro proporcionado por el fabricante
	 * @param costo El costo monetario de adquisición del disco duro expresado en BigDecimal para precisión
	 * @param precioBase El precio base de venta del disco duro antes de aplicar promociones
	 * @param capacidadAlm La capacidad de almacenamiento del disco duro expresada como cadena (ej: "1TB", "500GB")
	 */
	protected DiscoDuro(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String capacidadAlm) {
		super(id, descripcion, marca, modelo, costo, precioBase);
		this.capacidadAlm = capacidadAlm;
	}

	/**
	 * Obtiene la capacidad de almacenamiento del disco duro.
	 * Este método devuelve la capacidad de almacenamiento tal como fue configurada durante la creación del objeto.
	 * 
	 * @return La capacidad de almacenamiento como una cadena de texto que representa el tamaño (ej: "1TB", "500GB")
	 */
	public String getCapacidadAlm() {
		return capacidadAlm;
	}

	/**
	 * Establece la capacidad de almacenamiento del disco duro.
	 * Permite modificar la capacidad de almacenamiento después de la creación del objeto.
	 * 
	 * @param capacidadAlm La nueva capacidad de almacenamiento a asignar, expresada como cadena de texto
	 */
	public void setCapacidadAlm(String capacidadAlm) {
		this.capacidadAlm = capacidadAlm;
	}

	/**
	 * Muestra las características específicas del disco duro en la consola.
	 * Primero invoca el método de la clase padre para mostrar las características generales del componente,
	 * y luego añade la información específica de la capacidad de almacenamiento del disco duro.
	 */
	@Override
	public void mostrarCaracteristicas() {
		super.mostrarCaracteristicas();
        System.out.println("Capacidad almacenamiento: " + this.capacidadAlm);
		
	}

	/**
	 * Obtiene la categoría específica de este componente.
	 * Devuelve "Disco Duro" para identificar que este componente es un dispositivo de almacenamiento.
	 * 
	 * @return La cadena "Disco Duro" que representa la categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "Disco Duro";
	}
	
}
