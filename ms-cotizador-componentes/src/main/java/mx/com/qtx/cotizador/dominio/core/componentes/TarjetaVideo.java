package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Representa un componente de tipo tarjeta de video en el sistema de cotización.
 * Extiende ComponenteSimple y añade la propiedad de memoria de video.
 * Las tarjetas de video son componentes gráficos que pueden ser incluidos en configuraciones de PC.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public class TarjetaVideo extends ComponenteSimple {
	private String memoria;

	/**
	 * Constructor protegido para crear una tarjeta de video con sus propiedades específicas.
	 * Inicializa las propiedades heredadas y establece la memoria de video.
	 *
	 * @param id Identificador único de la tarjeta de video
	 * @param descripcion Descripción detallada de la tarjeta de video
	 * @param marca Marca de la tarjeta de video
	 * @param modelo Modelo específico de la tarjeta de video
	 * @param costo Costo de adquisición de la tarjeta de video
	 * @param precioBase Precio base de venta de la tarjeta de video
	 * @param memoria Cantidad de memoria de video de la tarjeta
	 */
	protected TarjetaVideo(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String memoria) {
		super(id, descripcion, marca, modelo, costo, precioBase);
		this.memoria = memoria;
	}

	/**
	 * Obtiene la memoria de video de la tarjeta.
	 *
	 * @return La memoria de video como cadena de texto
	 */
	public String getMemoria() {
		return memoria;
	}

	/**
	 * Establece la memoria de video de la tarjeta.
	 *
	 * @param memoria La memoria de video a establecer
	 */
	public void setMemoria(String memoria) {
		this.memoria = memoria;
	}
	
//	public BigDecimal cotizar(int cantidadI) {
//		return PromocionUtil.calcularPrecioPromocion3X2(cantidadI, this.precioBase);
//	}

	/**
	 * Obtiene la categoría de este componente.
	 *
	 * @return "Tarjeta de Video" como categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "Tarjeta de Video";
	}
	
}
