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
	private Map<Componente,Integer> mapCompsYcants;

	public CotizadorConMap() {
		this.mapCompsYcants = new HashMap<>();
	}

	@Override
	public void agregarComponente(int cantidad, Componente componente) {
		this.mapCompsYcants.put(componente, cantidad);
	}

	@Override
	public void eliminarComponente(String idComponente) {
    	   Componente llave = this.mapCompsYcants.keySet()
    			                    .stream()
    			                    .filter(k->k.getId().equals(idComponente))
    			                     .findFirst()
    			                     .get();
               
		
		this.mapCompsYcants.remove(llave);
	}

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
