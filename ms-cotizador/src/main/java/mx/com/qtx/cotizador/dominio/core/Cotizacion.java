package mx.com.qtx.cotizador.dominio.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;

/**
 * Representa una cotización de venta con sus detalles y cálculos totales.
 * <p>
 * Esta clase gestiona la información de una cotización, incluyendo su número único, fecha,
 * total, impuestos y los detalles de los componentes incluidos. Ofrece métodos para
 * agregar detalles, emitir reportes y guardar la cotización en la base de datos.
 * </p>
 */
public class Cotizacion {
	private static long nCotizaciones = 0;
	
	protected long num;
	protected LocalDate fecha;
	protected BigDecimal total;
	protected BigDecimal totalImpuestos;
	
	protected Map<Integer,DetalleCotizacion> detalles;
	
	/**
	 * Constructor que inicializa una nueva cotización.
	 * <p>
	 * Incrementa automáticamente el contador de cotizaciones para asignar un número único,
	 * establece la fecha actual, inicializa el total en cero y crea un mapa vacío para los detalles.
	 * </p>
	 */
	public Cotizacion() {
		super();
		nCotizaciones++;
		this.num = nCotizaciones;
		this.fecha = LocalDate.now();
		this.total = new BigDecimal(0);
		this.detalles = new TreeMap<>();
	}
	
	/**
	 * Agrega un detalle a la cotización.
	 * <p>
	 * El detalle se almacena en un mapa utilizando su número de detalle como clave.
	 * Si ya existe un detalle con el mismo número, se reemplaza con el nuevo.
	 * </p>
	 * 
	 * @param detI El detalle de cotización a agregar
	 */
	public void agregarDetalle(DetalleCotizacion detI) {
		this.detalles.put(detI.getNumDetalle(), detI);
	}

	/**
	 * Obtiene el número único de la cotización.
	 * 
	 * @return El número de la cotización
	 */
	public long getNum() {
		return num;
	}

	/**
	 * Establece el número de la cotización.
	 * 
	 * @param num El nuevo número de la cotización
	 */
	public void setNum(long num) {
		this.num = num;
	}

	/**
	 * Obtiene la fecha de creación de la cotización.
	 * 
	 * @return La fecha de la cotización
	 */
	public LocalDate getFecha() {
		return fecha;
	}

	/**
	 * Establece la fecha de la cotización.
	 * 
	 * @param fecha La nueva fecha de la cotización
	 */
	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	/**
	 * Obtiene el monto total de la cotización (incluyendo impuestos).
	 * 
	 * @return El monto total de la cotización
	 */
	public BigDecimal getTotal() {
		return total;
	}

	/**
	 * Establece el monto total de la cotización.
	 * 
	 * @param total El nuevo monto total
	 */
	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	/**
	 * Obtiene el monto total de impuestos de la cotización.
	 * 
	 * @return El monto total de impuestos
	 */
	public BigDecimal getTotalImpuestos() {
		return totalImpuestos;
	}

	/**
	 * Establece el monto total de impuestos de la cotización.
	 * 
	 * @param totalImpuestos El nuevo monto total de impuestos
	 */
	public void setTotalImpuestos(BigDecimal totalImpuestos) {
		this.totalImpuestos = totalImpuestos;
	}

	/**
	 * Obtiene la lista de detalles de la cotización.
	 * <p>
	 * Devuelve una nueva lista con los valores del mapa de detalles,
	 * protegiendo así la estructura interna de la clase.
	 * </p>
	 * 
	 * @return Una lista con los detalles de la cotización
	 */
	public List<DetalleCotizacion> getDetalles() {
		return new ArrayList<DetalleCotizacion>(detalles.values());
	}

	/**
	 * Emite un reporte formateado de la cotización en la consola.
	 * <p>
	 * Muestra un encabezado con el número y fecha de la cotización,
	 * seguido de cada detalle, y finaliza con un resumen de subtotal,
	 * impuestos y total, todo formateado con alineación adecuada.
	 * </p>
	 */
	public void emitirComoReporte() {
		System.out.println("================== Cotizacion número:" + this.num + " del " + this.fecha + " ==================\n");
		for(Integer k:this.detalles.keySet()) {
			this.desplegarLineaCotizacion(this.detalles.get(k));
		}

		System.out.printf("\n%72s","Subtotal:$" + String.format("%8.2f",this.getTotal().subtract(this.getTotalImpuestos())));
		System.out.printf("\n%72s","Impuestos:$" + String.format("%8.2f",this.getTotalImpuestos()));
		System.out.printf("\n%72s","Total:$" + String.format("%8.2f",this.getTotal()));
		System.out.println(" ");
		
	}
	
	/**
	 * Muestra una línea de detalle de la cotización en la consola.
	 * <p>
	 * Formatea y muestra la información del detalle de cotización,
	 * incluyendo cantidad, categoría, descripción, precio base e importe cotizado,
	 * con alineación y formato adecuados para una presentación clara.
	 * </p>
	 * 
	 * @param detI El detalle de cotización a mostrar
	 */
	protected void desplegarLineaCotizacion(DetalleCotizacion detI) {
		System.out.println(String.format("%3d",detI.getCantidad()) + " " 
							+ String.format("Categoría:%-20s", detI.getCategoria()) 
							+ String.format("%-20s", detI.getDescripcion())
							+ " con precio base de $" + String.format("%8.2f",detI.getPrecioBase())
							+ " cuesta(n) " + String.format("%8.2f",detI.getImporteCotizado()));
	}
}
