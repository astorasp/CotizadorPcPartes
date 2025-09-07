package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Representa un componente de tipo disco duro en el sistema de cotización.
 * Extiende ComponenteSimple y añade la propiedad de capacidad de almacenamiento.
 * Los discos duros son componentes de almacenamiento que pueden ser incluidos en configuraciones de PC.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public class DiscoDuro extends ComponenteSimple {
	private String capacidadAlm;

	/**
	 * Constructor protegido para crear un disco duro con sus propiedades específicas.
	 * Inicializa las propiedades heredadas y establece la capacidad de almacenamiento.
	 *
	 * @param id Identificador único del disco duro
	 * @param descripcion Descripción detallada del disco duro
	 * @param marca Marca del disco duro
	 * @param modelo Modelo específico del disco duro
	 * @param costo Costo de adquisición del disco duro
	 * @param precioBase Precio base de venta del disco duro
	 * @param capacidadAlm Capacidad de almacenamiento del disco duro
	 */
	protected DiscoDuro(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String capacidadAlm) {
		super(id, descripcion, marca, modelo, costo, precioBase);
		this.capacidadAlm = capacidadAlm;
	}

	/**
	 * Obtiene la capacidad de almacenamiento del disco duro.
	 *
	 * @return La capacidad de almacenamiento como cadena de texto
	 */
	public String getCapacidadAlm() {
		return capacidadAlm;
	}

	/**
	 * Establece la capacidad de almacenamiento del disco duro.
	 *
	 * @param capacidadAlm La capacidad de almacenamiento a establecer
	 */
	public void setCapacidadAlm(String capacidadAlm) {
		this.capacidadAlm = capacidadAlm;
	}

	/**
	 * Muestra las características específicas del disco duro.
	 * Incluye la información del componente padre y la capacidad de almacenamiento.
	 */
	@Override
	public void mostrarCaracteristicas() {
		super.mostrarCaracteristicas();
        System.out.println("Capacidad almacenamiento: " + this.capacidadAlm);
		
	}

	/**
	 * Obtiene la categoría de este componente.
	 *
	 * @return "Disco Duro" como categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "Disco Duro";
	}
	
}
