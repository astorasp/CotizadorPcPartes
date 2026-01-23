package mx.com.qtx.cotizador.dominio.cotizadorB;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;

/**
 * Implementación concreta de cotización con formato específico para el Cotizador B.
 * <p>
 * Esta clase extiende {@link Cotizacion} proporcionando una implementación especializada
 * del método {@link #emitirComoReporte()} que genera un reporte de cotización con un
 * formato específico diseñado para el CotizadorConMap (Cotizador B). El formato incluye
 * una presentación tabular detallada con columnas alineadas y información completa
 * sobre cada componente de la cotización.
 * </p>
 *
 * <h3>Características del formato de reporte:</h3>
 * <ul>
 *   <li><strong>Encabezado informativo:</strong> Incluye número de cotización y fecha</li>
 *   <li><strong>Tabla estructurada:</strong> Columnas para número, cantidad, ID, descripción, precio base y total</li>
 *   <li><strong>Resumen financiero:</strong> Subtotal, impuestos y total final</li>
 *   <li><strong>Formato tabular:</strong> Alineación precisa con anchos de columna fijos</li>
 *   <li><strong>Precisión decimal:</strong> Formato de dos decimales para montos</li>
 * </ul>
 *
 * <h3>Uso en el patrón Strategy:</h3>
 * <p>
 * Esta clase se utiliza específicamente con {@link CotizadorConMap}, que es la segunda
 * implementación del patrón Strategy. Mientras que {@link Cotizacion} proporciona el
 * formato base, esta clase ofrece una presentación más elaborada y profesional
 * del reporte de cotización.
 * </p>
 *
 * <h3>Ejemplo de salida:</h3>
 * <pre>
 * ===========================================================================================
 * Cotizacion número: 123
 * Fecha: 2025-01-17
 * ==========================================================================================
 *
 *     # Cantidad  Id             Descripcion                    Base         Total
 *
 *     1        2  MON001         Monitor 24" LED               $  1500.00   $  3000.00
 *     2        1  CPU001         Procesador Intel i7           $  2500.00   $  2500.00
 *
 *                                                                             Subtotal: $  5500.00
 *                                                                             Impuestos: $   880.00
 *                                                                             Total: $  6380.00
 * </pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.core.Cotizacion
 * @see mx.com.qtx.cotizador.dominio.cotizadorB.CotizadorConMap
 * @see mx.com.qtx.cotizador.dominio.core.DetalleCotizacion
 */
public class CotizacionFmtoB extends Cotizacion {

	/**
	 * Constructor que crea una nueva instancia de cotización con formato B.
	 * <p>
	 * Inicializa una cotización que utilizará el formato específico de reporte
	 * definido por esta clase. El constructor delega la inicialización básica
	 * a la clase padre {@link Cotizacion} y configura cualquier estado adicional
	 * específico necesario para el formato B.
	 * </p>
	 */
	public CotizacionFmtoB() {
		super();
	}

	/**
	 * Genera y emite un reporte completo de la cotización en formato tabular profesional.
	 * <p>
	 * Este método sobrescribe la implementación base para proporcionar un formato
	 * de reporte más elaborado y profesional. El reporte incluye:
	 * </p>
	 * <ul>
	 *   <li><strong>Encabezado con información básica:</strong> Número de cotización y fecha</li>
	 *   <li><strong>Tabla detallada de componentes:</strong> Con columnas alineadas para fácil lectura</li>
	 *   <li><strong>Resumen financiero:</strong> Subtotal, impuestos desglosados y total final</li>
	 *   <li><strong>Formato consistente:</strong> Alineación precisa y formato de números</li>
	 * </ul>
	 * <p>
	 * El método utiliza el patrón Template Method, llamando a {@link #desplegarLineaCotizacion(DetalleCotizacion)}
	 * para cada detalle, permitiendo así la customización del formato de cada línea.
	 * </p>
	 *
	 * @see #desplegarLineaCotizacion(DetalleCotizacion)
	 * @see mx.com.qtx.cotizador.dominio.core.Cotizacion#emitirComoReporte()
	 */
	@Override
	public void emitirComoReporte() {
		System.out.println("===========================================================================================");
		System.out.println("Cotizacion número:" + this.num );
		System.out.println("Fecha:" + this.fecha );
		System.out.println("===========================================================================================\n");
		System.out.printf("%5s %-10s %-15s %-30s    %-12s %-12s\n\n","#", "Cantidad", "Id", "Descripcion", "Base", "Total"  );
		
		for(Integer k:this.detalles.keySet()) {
			this.desplegarLineaCotizacion(this.detalles.get(k));
		}
		System.out.printf("\n%88s","Subtotal: $" + String.format("%10.2f",this.getTotal().subtract(this.getTotalImpuestos())));
		System.out.printf("\n%88s","Impuestos: $" + String.format("%10.2f",this.getTotalImpuestos()));
		System.out.printf("\n%88s","Total: $" + String.format("%10.2f",this.getTotal()));
		System.out.println(" ");
	}
	
	/**
	 * Método protegido que formatea y muestra una línea individual del detalle de cotización.
	 * <p>
	 * Este método implementa el patrón Template Method, siendo llamado por {@link #emitirComoReporte()}
	 * para cada detalle de cotización. Formatea la información de un componente específico
	 * en una línea tabular con columnas perfectamente alineadas.
	 * </p>
	 * <p>
	 * El formato de línea incluye:
	 * <ul>
	 *   <li><strong>Número de detalle:</strong> Posición del componente en la cotización</li>
	 *   <li><strong>Cantidad:</strong> Número de unidades del componente</li>
	 *   <li><strong>ID del componente:</strong> Identificador único del componente</li>
	 *   <li><strong>Descripción:</strong> Nombre completo del componente</li>
	 *   <li><strong>Precio base:</strong> Costo unitario formateado con símbolo de moneda</li>
	 *   <li><strong>Importe total:</strong> Costo total (cantidad × precio base) del componente</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Utiliza formato printf con especificadores de ancho para garantizar la alineación
	 * perfecta de todas las columnas, facilitando la lectura del reporte.
	 * </p>
	 *
	 * @param detI El detalle de cotización que se va a formatear y mostrar.
	 *              Contiene toda la información necesaria del componente.
	 * @see #emitirComoReporte()
	 * @see mx.com.qtx.cotizador.dominio.core.DetalleCotizacion
	 */
	@Override
	protected void desplegarLineaCotizacion(DetalleCotizacion detI) {
		System.out.printf("%5d     %4d  %-15s %-30s $%10.2f   $%10.2f\n", detI.getNumDetalle(), detI.getCantidad(), detI.getIdComponente(),
				detI.getDescripcion(), detI.getPrecioBase(), detI.getImporteCotizado());
	}

}
