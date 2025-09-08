package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Clase que representa una tarjeta de video como componente de PC.
 * Extiende ComponenteSimple e incluye memoria.
 */
public class TarjetaVideo extends ComponenteSimple {
	private String memoria;

	/**
	 * Constructor protegido para crear una instancia de TarjetaVideo.
	 * Inicializa la tarjeta de video con sus propiedades específicas, incluyendo la cantidad de memoria.
	 * 
	 * @param id El identificador único asignado a la tarjeta de video para su identificación en el sistema
	 * @param descripcion Una descripción detallada que explica las características y especificaciones de la tarjeta de video
	 * @param marca El nombre de la marca fabricante de la tarjeta de video (ej: "NVIDIA", "AMD")
	 * @param modelo El modelo específico de la tarjeta de video proporcionado por el fabricante
	 * @param costo El costo monetario de adquisición de la tarjeta de video expresado en BigDecimal para precisión
	 * @param precioBase El precio base de venta de la tarjeta de video antes de aplicar promociones
	 * @param memoria La cantidad de memoria de la tarjeta de video expresada como cadena (ej: "8GB", "16GB")
	 */
	protected TarjetaVideo(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String memoria) {
		super(id, descripcion, marca, modelo, costo, precioBase);
		this.memoria = memoria;
	}

	/**
	 * Obtiene la cantidad de memoria de la tarjeta de video.
	 * Este método devuelve la memoria tal como fue configurada durante la creación del objeto.
	 * 
	 * @return La cantidad de memoria como una cadena de texto que representa el tamaño (ej: "8GB", "16GB")
	 */
	public String getMemoria() {
		return memoria;
	}

	/**
	 * Establece la cantidad de memoria de la tarjeta de video.
	 * Permite modificar la memoria después de la creación del objeto.
	 * 
	 * @param memoria La nueva cantidad de memoria a asignar, expresada como cadena de texto
	 */
	public void setMemoria(String memoria) {
		this.memoria = memoria;
	}
	
	/**
	 * Obtiene la categoría específica de este componente.
	 * Devuelve "Tarjeta de Video" para identificar que este componente es un dispositivo de procesamiento gráfico.
	 * 
	 * @return La cadena "Tarjeta de Video" que representa la categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "Tarjeta de Video";
	}
	
	/**
	 * Muestra las características específicas de la tarjeta de video en la consola.
	 * Primero invoca el método de la clase padre para mostrar las características generales del componente,
	 * y luego añade la información específica de la memoria de la tarjeta de video.
	 */
	@Override
	public void mostrarCaracteristicas() {
		super.mostrarCaracteristicas();
        System.out.println("Memoria: " + this.memoria);
		
	}
}
