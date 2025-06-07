package mx.com.qtx.cotizadorv1ds.core;

import java.math.BigDecimal;

/**
 * Representa un detalle individual dentro de una cotización.
 * <p>
 * Esta clase almacena la información detallada de cada componente incluido en una cotización,
 * como su identificador, descripción, cantidad, precio base e importe cotizado.
 * Cada instancia de esta clase corresponde a una línea en la cotización.
 * </p>
 */
public class DetalleCotizacion {
	private int numDetalle;
	private String idComponente;
	private String descripcion;
	private int cantidad;
	private BigDecimal precioBase;
	private BigDecimal importeCotizado;
	private String categoria;
	
	/**
	 * Constructor que inicializa todos los campos de un detalle de cotización.
	 * 
	 * @param numDetalle Número secuencial que identifica este detalle dentro de la cotización
	 * @param idComponente Identificador único del componente cotizado
	 * @param descripcion Descripción textual del componente
	 * @param cantidad Cantidad de unidades del componente incluidas en este detalle
	 * @param precioBase Precio base unitario del componente
	 * @param importeCotizado Importe total cotizado para este detalle (precio final por cantidad)
	 * @param categoria Categoría a la que pertenece el componente
	 */
	public DetalleCotizacion(int numDetalle, String idComponente, String descripcion, int cantidad,
			BigDecimal precioBase, BigDecimal importeCotizado, String categoria) {
		super();
		this.numDetalle = numDetalle;
		this.idComponente = idComponente;
		this.descripcion = descripcion;
		this.cantidad = cantidad;
		this.precioBase = precioBase;
		this.importeCotizado = importeCotizado;
		this.categoria = categoria;
	}

	/**
	 * Obtiene el número de detalle que identifica este ítem en la cotización.
	 * 
	 * @return El número secuencial de este detalle
	 */
	public int getNumDetalle() {
		return numDetalle;
	}

	/**
	 * Establece el número de detalle que identifica este ítem en la cotización.
	 * 
	 * @param numDetalle El nuevo número de detalle
	 */
	public void setNumDetalle(int numDetalle) {
		this.numDetalle = numDetalle;
	}

	/**
	 * Obtiene el identificador único del componente cotizado.
	 * 
	 * @return El identificador del componente
	 */
	public String getIdComponente() {
		return idComponente;
	}

	/**
	 * Establece el identificador único del componente cotizado.
	 * 
	 * @param idComponente El nuevo identificador del componente
	 */
	public void setIdComponente(String idComponente) {
		this.idComponente = idComponente;
	}

	/**
	 * Obtiene la descripción textual del componente cotizado.
	 * 
	 * @return La descripción del componente
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Establece la descripción textual del componente cotizado.
	 * 
	 * @param descripcion La nueva descripción del componente
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Obtiene la cantidad de unidades del componente incluidas en este detalle.
	 * 
	 * @return La cantidad de unidades
	 */
	public int getCantidad() {
		return cantidad;
	}

	/**
	 * Establece la cantidad de unidades del componente incluidas en este detalle.
	 * 
	 * @param cantidad La nueva cantidad de unidades
	 */
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	/**
	 * Obtiene el precio base unitario del componente.
	 * 
	 * @return El precio base unitario
	 */
	public BigDecimal getPrecioBase() {
		return precioBase;
	}

	/**
	 * Establece el precio base unitario del componente.
	 * 
	 * @param precioBase El nuevo precio base unitario
	 */
	public void setPrecioBase(BigDecimal precioBase) {
		this.precioBase = precioBase;
	}

	/**
	 * Obtiene el importe total cotizado para este detalle.
	 * <p>
	 * Este valor representa el precio final multiplicado por la cantidad,
	 * incluyendo cualquier descuento o cargo adicional aplicado.
	 * </p>
	 * 
	 * @return El importe total cotizado
	 */
	public BigDecimal getImporteCotizado() {
		return importeCotizado;
	}

	/**
	 * Establece el importe total cotizado para este detalle.
	 * 
	 * @param importeCotizado El nuevo importe total cotizado
	 */
	public void setImporteCotizado(BigDecimal importeCotizado) {
		this.importeCotizado = importeCotizado;
	}

	/**
	 * Obtiene la categoría a la que pertenece el componente cotizado.
	 * 
	 * @return La categoría del componente
	 */
	public String getCategoria() {
		return categoria;
	}

	/**
	 * Establece la categoría a la que pertenece el componente cotizado.
	 * 
	 * @param categoria La nueva categoría del componente
	 */
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	
}
