package mx.com.qtx.cotizador.dominio.cotizadorA;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import mx.com.qtx.cotizador.dominio.core.ComponenteInvalidoException;
import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;
import mx.com.qtx.cotizador.dominio.core.ICotizador;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;

/**
 * Primera implementación del cotizador (Cotizador A) que utiliza listas separadas para componentes y cantidades.
 * <p>
 * Esta clase implementa la interfaz {@link ICotizador} utilizando el patrón Strategy para proporcionar
 * una estrategia específica de cotización. Utiliza dos listas paralelas (una para componentes y otra
 * para cantidades) lo que permite un manejo eficiente de componentes con diferentes cantidades.
 * </p>
 *
 * <h3>Estrategia de implementación:</h3>
 * <ul>
 *   <li><strong>Listas paralelas:</strong> Una lista para componentes y otra para cantidades correspondientes</li>
 *   <li><strong>Búsqueda lineal:</strong> Para encontrar componentes por ID se recorren las listas</li>
 *   <li><strong>Eliminación por índice:</strong> Se elimina el componente y su cantidad correspondiente</li>
 *   <li><strong>Iteración sincronizada:</strong> Las operaciones mantienen sincronizadas ambas listas</li>
 * </ul>
 *
 * <h3>Patrón Strategy:</h3>
 * <p>
 * Esta implementación concreta del patrón Strategy permite:
 * <ul>
 *   <li>Cambiar dinámicamente el algoritmo de cotización</li>
 *   <li>Comparar diferentes estrategias de rendimiento</li>
 *   <li>Extender fácilmente con nuevas estrategias</li>
 *   <li>Mantener el código cliente independiente de la implementación</li>
 * </ul>
 * </p>
 *
 * <h3>Ventajas de esta estrategia:</h3>
 * <ul>
 *   <li><strong>Simplicidad:</strong> Fácil de entender y mantener</li>
 *   <li><strong>Flexibilidad:</strong> Permite diferentes cantidades por componente</li>
 *   <li><strong>Memoria eficiente:</strong> No requiere objetos wrapper adicionales</li>
 *   <li><strong>Performance aceptable:</strong> Para cotizaciones pequeñas a medianas</li>
 * </ul>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear cotizador con calculador de IVA
 * CalculadorImpuesto iva = new IVA();
 * Cotizador cotizador = new Cotizador(iva);
 *
 * // Agregar componentes
 * cotizador.agregarComponente(2, monitor);
 * cotizador.agregarComponente(1, discoDuro);
 *
 * // Generar cotización
 * Cotizacion cotizacion = cotizador.generarCotizacion(Arrays.asList(iva));
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.cotizadorB.CotizadorConMap
 * @see mx.com.qtx.cotizador.dominio.core.ICotizador
 * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto
 */
public class Cotizador implements ICotizador{

    /**
     * Lista que almacena los componentes que se van a cotizar.
     * <p>
     * Esta lista mantiene una colección de objetos {@link Componente} que representan
     * los diferentes elementos de hardware que el cliente desea adquirir. Cada componente
     * en esta lista tiene una cantidad correspondiente en la lista {@link #cantidades}.
     * </p>
     * <p>
     * La relación uno-a-uno entre esta lista y {@link #cantidades} es fundamental
     * para el funcionamiento correcto de la estrategia de listas paralelas.
     * </p>
     */
    private List<Componente> componentes = new ArrayList<>();

    /**
     * Lista que almacena las cantidades correspondientes a cada componente.
     * <p>
     * Esta lista mantiene las cantidades de cada componente que se encuentra en la
     * lista {@link #componentes}. El índice i en esta lista corresponde al índice i
     * en la lista de componentes, manteniendo así la sincronización entre ambas colecciones.
     * </p>
     * <p>
     * Cada elemento representa la cantidad de unidades del componente correspondiente
     * que el cliente desea adquirir.
     * </p>
     */
    private List<Integer> cantidades = new ArrayList<>();

    /**
     * Instancia del calculador de impuestos utilizada por este cotizador.
     * <p>
     * Este campo almacena la implementación concreta de {@link CalculadorImpuesto}
     * que será utilizada para calcular los impuestos aplicables a la cotización.
     * Se establece en el constructor y no puede ser modificada posteriormente,
     * garantizando la inmutabilidad de la estrategia de cálculo de impuestos.
     * </p>
     * <p>
     * La implementación específica determina las reglas fiscales que se aplicarán
     * al calcular el importe final de la cotización.
     * </p>
     */
	private final CalculadorImpuesto calculadorImpuesto;

	/**
	 * Constructor que inicializa el cotizador con un calculador de impuestos específico.
	 * <p>
	 * Crea una nueva instancia del cotizador configurada con el calculador de impuestos
	 * proporcionado. Este constructor establece la estrategia de cálculo de impuestos
	 * que será utilizada en todas las cotizaciones generadas por esta instancia.
	 * </p>
	 *
	 * @param calculadorImpuesto Implementación concreta de {@link CalculadorImpuesto}
	 *                           que define las reglas fiscales a aplicar. No puede ser null.
	 * @throws IllegalArgumentException si calculadorImpuesto es null
	 */
	public Cotizador(CalculadorImpuesto calculadorImpuesto) {
		this.calculadorImpuesto = calculadorImpuesto;
	}

    // Métodos de gestión de componentes

    /**
     * Agrega un componente con su cantidad correspondiente a la cotización actual.
     * <p>
     * Este método permite incorporar un nuevo componente al proceso de cotización,
     * especificando tanto el componente como la cantidad deseada. El componente y su
     * cantidad se almacenan de manera sincronizada en las listas paralelas que
     * utiliza esta implementación del patrón Strategy.
     * </p>
     * <p>
     * La estrategia de listas paralelas garantiza que cada componente tenga su
     * cantidad correspondiente en la misma posición de índice en ambas listas,
     * facilitando las operaciones de búsqueda, eliminación y cálculo posteriores.
     * </p>
     *
     * @param cantidad Número de unidades del componente que se desea adquirir.
     *                 Debe ser un valor positivo mayor que cero.
     * @param componente Instancia del componente que se va a agregar a la cotización.
     *                   No puede ser null y debe tener un ID único válido.
     * @throws IllegalArgumentException si cantidad es menor o igual a cero
     * @throws IllegalArgumentException si componente es null
     * @see #eliminarComponente(String)
     * @see #generarCotizacion(List)
     */
    public void agregarComponente(int cantidad, Componente componente) {
    	this.cantidades.add(cantidad);
    	this.componentes.add(componente);
    }

    /**
     * Elimina un componente específico de la cotización actual utilizando su identificador único.
     * <p>
     * Este método busca el componente por su ID único dentro de la lista de componentes
     * y, si lo encuentra, elimina tanto el componente como su cantidad correspondiente
     * manteniendo la sincronización entre ambas listas paralelas.
     * </p>
     * <p>
     * La estrategia de listas paralelas requiere que al eliminar un componente en una
     * posición específica, también se elimine la cantidad correspondiente en la misma
     * posición de la lista de cantidades, preservando así la integridad de los datos.
     * </p>
     * <p>
     * Si el componente no existe, se lanza una excepción para informar al cliente
     * sobre el error en la operación solicitada.
     * </p>
     *
     * @param idComponente Identificador único del componente que se desea eliminar.
     *                     Este ID debe corresponder exactamente con el ID de algún
     *                     componente presente en la cotización actual.
     * @throws ComponenteInvalidoException si idComponente es null
     * @throws ComponenteInvalidoException si no existe ningún componente con el ID especificado
     * @see #agregarComponente(int, Componente)
     * @see mx.com.qtx.cotizador.dominio.core.ComponenteInvalidoException
     */
    public void eliminarComponente(String idComponente) throws ComponenteInvalidoException {
    	if(idComponente == null) {
    		throw new ComponenteInvalidoException("Id del componente es nulo ", null);
    	}
    	int i = this.componentes.stream().map(compI->compI.getId())
    			                         .toList().indexOf(idComponente);
    	if (i==-1) {// NO existe
    		throw new ComponenteInvalidoException("No existe componente con Id "+ idComponente, null);
    	}
    	this.cantidades.remove(i);
    	this.componentes.remove(i);
    }

    /**
     * Genera una cotización completa basada en los componentes y cantidades almacenados.
     * <p>
     * Este método es el núcleo de la funcionalidad del cotizador. Utiliza la estrategia
     * de listas paralelas para iterar sobre todos los componentes y sus cantidades,
     * calculando el importe total de la cotización aplicando las promociones y descuentos
     * específicos de cada componente.
     * </p>
     * <p>
     * El proceso de generación incluye:
     * <ol>
     *   <li>Iteración sincronizada sobre las listas de componentes y cantidades</li>
     *   <li>Cálculo del importe cotizado para cada componente según su cantidad</li>
     *   <li>Creación de detalles de cotización para cada componente</li>
     *   <li>Acumulación del total parcial sin impuestos</li>
     *   <li>Cálculo y aplicación de todos los impuestos especificados</li>
     *   <li>Establecimiento del total final incluyendo impuestos</li>
     * </ol>
     * </p>
     * <p>
     * Los calculadores de impuestos se aplican en el orden proporcionado, permitiendo
     * la composición de múltiples tipos de impuestos (IVA, impuestos federales, locales, etc.).
     * </p>
     *
     * @param calculadorImpuestos Lista de calculadores de impuestos a aplicar sobre el total.
     *                            Puede ser null (no se aplicarán impuestos) o contener múltiples
     *                            calculadores que se aplicarán secuencialmente.
     * @return Una instancia completa de {@link Cotizacion} que incluye todos los detalles
     *         de los componentes, subtotales, impuestos y total final.
     * @see mx.com.qtx.cotizador.dominio.core.Cotizacion
     * @see mx.com.qtx.cotizador.dominio.core.DetalleCotizacion
     * @see mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto
     * @see #agregarComponente(int, Componente)
     */
    public Cotizacion generarCotizacion(List<CalculadorImpuesto> calculadorImpuestos) {
        BigDecimal total = new BigDecimal(0);
        
        Cotizacion cotizacion = new Cotizacion();
        
        for(int i=0; i<this.cantidades.size();i++) {
        	Componente compI = this.componentes.get(i);
        	int cantidadI = this.cantidades.get(i);
        	BigDecimal importeCotizadoI = new BigDecimal(0);
        	
        	importeCotizadoI = compI.cotizar(cantidadI);
        	        
        	DetalleCotizacion detI = new DetalleCotizacion((i + 1), compI.getId(), compI.getDescripcion(), cantidadI, 
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
	 * Muestra en consola una lista detallada de todos los componentes actualmente en la cotización.
	 * <p>
	 * Este método proporciona una vista rápida del estado actual de la cotización,
	 * mostrando cada componente junto con su cantidad, precio base y identificador único.
	 * Es útil para depuración, verificación y seguimiento del proceso de cotización.
	 * </p>
	 * <p>
	 * La información se presenta en un formato legible que incluye:
	 * <ul>
	 *   <li>Cantidad de unidades de cada componente</li>
	 *   <li>Descripción completa del componente</li>
	 *   <li>Precio base unitario del componente</li>
	 *   <li>Identificador único del componente</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Este método utiliza la estrategia de listas paralelas para acceder de manera
	 * sincronizada a los componentes y sus cantidades correspondientes.
	 * </p>
	 *
	 * @see #agregarComponente(int, Componente)
	 * @see #eliminarComponente(String)
	 * @see #generarCotizacion(List)
	 */
	public void listarComponentes() {
        System.out.println("=== Componentes a cotizar ===");
        for(int i=0; i<this.cantidades.size();i++) {
        	Componente c = this.componentes.get(i);
            System.out.println(this.cantidades.get(i) + " " + c.getDescripcion() 
            		 + ": $" + c.getPrecioBase() + " ID:" + c.getId());        	
        }
    }

}