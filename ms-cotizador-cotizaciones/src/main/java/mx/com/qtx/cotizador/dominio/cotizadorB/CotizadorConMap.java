package mx.com.qtx.cotizador.dominio.cotizadorB;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;
import mx.com.qtx.cotizador.dominio.core.ICotizador;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;

/**
 * Segunda implementación del cotizador (Cotizador B) que utiliza un Map para asociar componentes con cantidades.
 * <p>
 * Esta clase implementa la interfaz {@link ICotizador} utilizando el patrón Strategy para proporcionar
 * una estrategia alternativa de cotización. Utiliza un {@link HashMap} donde las claves son los
 * componentes y los valores son las cantidades correspondientes, ofreciendo una búsqueda más eficiente.
 * </p>
 *
 * <h3>Estrategia de implementación:</h3>
 * <ul>
 *   <li><strong>Mapa clave-valor:</strong> Componente como clave, cantidad como valor</li>
 *   <li><strong>Búsqueda por ID:</strong> Stream para encontrar componente por ID</li>
 *   <li><strong>Eliminación directa:</strong> Se elimina la entrada completa del mapa</li>
 *   <li><strong>Iteración eficiente:</strong> Iteración directa sobre las entradas del mapa</li>
 * </ul>
 *
 * <h3>Patrón Strategy:</h3>
 * <p>
 * Esta implementación concreta del patrón Strategy permite:
 * <ul>
 *   <li>Cambiar dinámicamente el algoritmo de cotización</li>
 *   <li>Comparar rendimiento con otras estrategias</li>
 *   <li>Probar diferentes estructuras de datos</li>
 *   <li>Mantener el código cliente independiente de la implementación</li>
 * </ul>
 * </p>
 *
 * <h3>Ventajas de esta estrategia:</h3>
 * <ul>
 *   <li><strong>Rendimiento superior:</strong> Búsqueda O(1) para componentes existentes</li>
 *   <li><strong>Integridad de datos:</strong> Asociación directa componente-cantidad</li>
 *   <li><strong>Menos código:</strong> Operaciones más concisas con streams</li>
 *   <li><strong>Escalabilidad:</strong> Mejor performance para cotizaciones grandes</li>
 * </ul>
 *
 * <h3>Desventajas:</h3>
 * <ul>
 *   <li><strong>Búsqueda por ID:</strong> Requiere stream para encontrar por ID (no tan eficiente)</li>
 *   <li><strong>Complejidad:</strong> Un poco más compleja que listas paralelas</li>
 *   <li><strong>Memoria:</strong> Overhead del HashMap</li>
 * </ul>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear cotizador
 * CotizadorConMap cotizador = new CotizadorConMap();
 *
 * // Agregar componentes (más eficiente que listas paralelas)
 * cotizador.agregarComponente(2, monitor);
 * cotizador.agregarComponente(1, discoDuro);
 *
 * // Generar cotización
 * Cotizacion cotizacion = cotizador.generarCotizacion(Arrays.asList(iva));
 * }</pre>
 *
 * <h3>Comparación con Cotizador A:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>Cotizador A (Listas)</th><th>Cotizador B (Mapa)</th></tr>
 *   <tr><td>Agregar componente</td><td>O(1)</td><td>O(1)</td></tr>
 *   <tr><td>Buscar por ID</td><td>O(n)</td><td>O(n) - stream</td></tr>
 *   <tr><td>Eliminar componente</td><td>O(n)</td><td>O(1) promedio</td></tr>
 *   <tr><td>Memoria</td><td>Mínima</td><td>HashMap overhead</td></tr>
 *   <tr><td>Simplicidad</td><td>Alta</td><td>Media</td></tr>
 * </table>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.cotizadorA.Cotizador
 * @see mx.com.qtx.cotizador.dominio.core.ICotizador
 * @see java.util.HashMap
 */
public class CotizadorConMap implements ICotizador {

    /**
     * Mapa que asocia componentes con sus cantidades correspondientes.
     * <p>
     * Esta estructura de datos es el corazón de la estrategia de implementación Map-based.
     * Las claves del mapa son instancias de {@link Componente}, mientras que los valores
     * representan las cantidades de cada componente que el cliente desea adquirir.
     * </p>
     * <p>
     * Esta aproximación ofrece varias ventajas sobre las listas paralelas:
     * <ul>
     *   <li><strong>Asociación directa:</strong> Componente y cantidad están inherentemente ligados</li>
     *   <li><strong>Integridad de datos:</strong> No hay riesgo de desincronización entre listas</li>
     *   <li><strong>Búsqueda eficiente:</strong> Acceso O(1) promedio para componentes conocidos</li>
     *   <li><strong>Eliminación directa:</strong> Remoción inmediata sin búsqueda lineal</li>
     * </ul>
     * </p>
     * <p>
     * El mapa se inicializa como un {@link HashMap} vacío en el constructor y se
     * mantiene durante todo el ciclo de vida del cotizador.
     * </p>
     */
	private Map<Componente,Integer> mapCompsYcants;

	/**
	 * Constructor que inicializa el cotizador con una estructura de mapa vacía.
	 * <p>
	 * Crea una nueva instancia del CotizadorConMap preparada para almacenar
	 * componentes y sus cantidades utilizando un {@link HashMap}. Esta estructura
	 * proporciona una alternativa más eficiente a las listas paralelas utilizadas
	 * en el Cotizador A, especialmente para operaciones de búsqueda y eliminación.
	 * </p>
	 * <p>
	 * El constructor inicializa el mapa interno como un HashMap vacío, listo
	 * para recibir componentes a través del método {@link #agregarComponente(int, Componente)}.
	 * </p>
	 *
	 * @see #agregarComponente(int, Componente)
	 * @see java.util.HashMap
	 */
	public CotizadorConMap() {
		this.mapCompsYcants = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * En esta implementación Map-based, el método utiliza {@link Map#put(Object, Object)}
	 * para asociar directamente el componente con su cantidad. Esta operación es O(1)
	 * promedio, lo que la hace más eficiente que la estrategia de listas paralelas
	 * para cotizaciones grandes.
	 * </p>
	 * <p>
	 * Si el componente ya existe en el mapa, su cantidad anterior será reemplazada
	 * por la nueva cantidad especificada. Esta implementación no permite cantidades
	 * acumulativas para el mismo componente.
	 * </p>
	 *
	 * @param cantidad {@inheritDoc}
	 * @param componente {@inheritDoc}
	 * @see Map#put(Object, Object)
	 */
	@Override
	public void agregarComponente(int cantidad, Componente componente) {
		this.mapCompsYcants.put(componente, cantidad);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Esta implementación utiliza streams para buscar el componente por su ID único
	 * dentro del conjunto de claves del mapa. Una vez encontrado el componente,
	 * utiliza {@link Map#remove(Object)} para eliminar la entrada completa del mapa.
	 * </p>
	 * <p>
	 * La búsqueda por ID requiere O(n) operaciones donde n es el número de componentes,
	 * sin embargo, la eliminación propiamente dicha es O(1) promedio. Esta es una
	 * mejora significativa respecto a la implementación con listas paralelas.
	 * </p>
	 * <p>
	 * <strong>Nota:</strong> Esta implementación puede lanzar {@link java.util.NoSuchElementException}
	 * si el stream no encuentra ningún componente con el ID especificado.
	 * </p>
	 *
	 * @param idComponente {@inheritDoc}
	 * @see Map#remove(Object)
	 * @see java.util.stream.Stream#filter(java.util.function.Predicate)
	 * @see java.util.Optional#get()
	 */
	@Override
	public void eliminarComponente(String idComponente) {
    	   Componente llave = this.mapCompsYcants.keySet()
    			                    .stream()
    			                    .filter(k->k.getId().equals(idComponente))
    			                     .findFirst()
    			                     .get();
               
		
		this.mapCompsYcants.remove(llave);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Esta implementación Map-based itera directamente sobre las claves del mapa
	 * ({@link Map#keySet()}) para acceder a cada componente y su cantidad correspondiente.
	 * Esta aproximación es más eficiente que las listas paralelas ya que no requiere
	 * mantener índices sincronizados.
	 * </p>
	 * <p>
	 * El proceso de generación incluye los mismos pasos que la implementación base,
	 * pero utiliza {@link CotizacionFmtoB} como tipo de cotización resultante,
	 * proporcionando un formato de reporte más profesional y detallado.
	 * </p>
	 * <p>
	 * <strong>Características específicas de esta implementación:</strong>
	 * <ul>
	 *   <li>Iteración directa sobre componentes sin índices manuales</li>
	 *   <li>Uso de {@link CotizacionFmtoB} para reportes con formato tabular</li>
	 *   <li>Acceso O(1) promedio a las cantidades a través del mapa</li>
	 *   <li>Mayor eficiencia en memoria para cotizaciones grandes</li>
	 * </ul>
	 * </p>
	 *
	 * @param calculadorImpuestos {@inheritDoc}
	 * @return {@inheritDoc} Específicamente, una instancia de {@link CotizacionFmtoB}
	 *         que proporciona formato de reporte mejorado
	 * @see CotizacionFmtoB
	 * @see Map#keySet()
	 * @see Map#get(Object)
	 */
	@Override
	public Cotizacion generarCotizacion(List<CalculadorImpuesto> calculadorImpuestos) {
        BigDecimal total = new BigDecimal(0);
        
        Cotizacion cotizacion = new CotizacionFmtoB();
        int i=0;
        for(Componente compI:this.mapCompsYcants.keySet()) {
        	int cantidadI = this.mapCompsYcants.get(compI);
        	BigDecimal importeCotizadoI = new BigDecimal(0);
        	i++;
        	
        	importeCotizadoI = compI.cotizar(cantidadI);
        	        
        	DetalleCotizacion detI = new DetalleCotizacion((i), compI.getId(), compI.getDescripcion(), cantidadI, 
        			                                        compI.getPrecioBase(), importeCotizadoI, compI.getCategoria());
        	cotizacion.agregarDetalle(detI);
            total = total.add(importeCotizadoI);
        }
		/*Calculamos el impuesto total de la cotización*/
		BigDecimal totalImpuestos = new BigDecimal(0);
		if( calculadorImpuestos != null) {
			for(CalculadorImpuesto calculadorImpuesto : calculadorImpuestos) {
				BigDecimal impuesto = calculadorImpuesto.calcularImpuesto(total);
				total = total.add(impuesto);
				totalImpuestos = totalImpuestos.add(impuesto);
			}
		}
		cotizacion.setTotalImpuestos(totalImpuestos);
        cotizacion.setTotal(total);
   	
		return cotizacion;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Esta implementación Map-based itera sobre el conjunto de claves del mapa
	 * ({@link Map#keySet()}) para acceder a cada componente. Para cada componente,
	 * obtiene su cantidad correspondiente utilizando {@link Map#get(Object)}, que
	 * proporciona acceso O(1) promedio.
	 * </p>
	 * <p>
	 * El formato de salida incluye una identificación específica "CotizadorConMap"
	 * para distinguir esta implementación de otras estrategias de cotización.
	 * </p>
	 *
	 * @see Map#keySet()
	 * @see Map#get(Object)
	 */
	@Override
	public void listarComponentes() {
        System.out.println("=== Componentes a cotizar en CotizadorConMap ===");
        for(Componente compI:this.mapCompsYcants.keySet())  {
        	int cantidad = this.mapCompsYcants.get(compI);
            System.out.println(cantidad + " " + compI.getDescripcion() 
            		 + ": $" + compI.getPrecioBase() + " ID:" + compI.getId());        	
        }
	}

}
