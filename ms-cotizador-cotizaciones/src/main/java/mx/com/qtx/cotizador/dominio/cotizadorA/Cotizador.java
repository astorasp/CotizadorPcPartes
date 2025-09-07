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
    private List<Componente> componentes = new ArrayList<>();
    private List<Integer> cantidades = new ArrayList<>();
	private final CalculadorImpuesto calculadorImpuesto;

	public Cotizador(CalculadorImpuesto calculadorImpuesto) {
		this.calculadorImpuesto = calculadorImpuesto;
	}

    // Métodos de gestión de componentes
    public void agregarComponente(int cantidad, Componente componente) {
    	this.cantidades.add(cantidad);
    	this.componentes.add(componente);
    }

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

	public void listarComponentes() {
        System.out.println("=== Componentes a cotizar ===");
        for(int i=0; i<this.cantidades.size();i++) {
        	Componente c = this.componentes.get(i);
            System.out.println(this.cantidades.get(i) + " " + c.getDescripcion() 
            		 + ": $" + c.getPrecioBase() + " ID:" + c.getId());        	
        }
    }

}