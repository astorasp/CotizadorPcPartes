package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una PC compuesta por varios subcomponentes.
 * Extiende Componente y calcula precio basado en componentes.
 */
public class Pc extends Componente {
	private List<ComponenteSimple> subComponentes;
	private static final float DSCTO_PRECIO_AGREGADO = 20.0f;

	/**
	 * Constructor protegido para crear una instancia de Pc con una lista de subcomponentes.
	 * Inicializa la PC con sus propiedades básicas y calcula automáticamente el precio base y costo basándose en los subcomponentes.
	 * 
	 * @param id El identificador único asignado a la PC para su identificación en el sistema
	 * @param descripcion Una descripción detallada que explica las características y especificaciones de la PC
	 * @param marca El nombre de la marca fabricante de la PC
	 * @param modelo El modelo específico de la PC proporcionado por el fabricante
	 * @param subComponentes La lista de componentes simples que forman parte de esta PC
	 */
	protected Pc(String id, String descripcion, String marca, String modelo, 
			List<ComponenteSimple> subComponentes) {
		super(id, descripcion, marca, modelo, new BigDecimal(0), new BigDecimal(0));
		this.subComponentes = subComponentes;
		this.setPrecioBase(this.calcularPrecioComponenteAgregado(0));
		this.setCosto(this.calcularCostoComponenteAgregado(0));
	}
	
	/**
	 * Constructor protegido para crear una instancia de Pc utilizando un PcBuilder.
	 * Inicializa la PC con la configuración proporcionada por el builder, incluyendo todos los subcomponentes agregados.
	 * Calcula automáticamente el precio base y costo basándose en los subcomponentes del builder.
	 * 
	 * @param config El PcBuilder que contiene la configuración completa de la PC, incluyendo subcomponentes
	 */
	protected Pc(PcBuilder config) {
		super(config.getIdPc(), config.getDescripcionPc(), 
			  config.getMarcaPc(), config.getModeloPc(), new BigDecimal(0), new BigDecimal(0));
		
		List<ComponenteSimple> lstDispositivosPc = new ArrayList<>();
		lstDispositivosPc.addAll(config.getDiscos());
		lstDispositivosPc.addAll(config.getMonitores());
		lstDispositivosPc.addAll(config.getTarjetas());
		
		this.subComponentes = lstDispositivosPc;
		this.setPrecioBase(this.calcularPrecioComponenteAgregado(0));
		this.setCosto(this.calcularCostoComponenteAgregado(0));
	}
	
	/**
	 * Obtiene el precio base de la PC, calculado como la suma de los precios base de todos los subcomponentes
	 * con un descuento aplicado por el ensamblaje de la PC.
	 * El descuento se aplica como un porcentaje fijo sobre el total de los componentes.
	 * 
	 * @return El precio base calculado de la PC, expresado en BigDecimal
	 */
	@Override
	public BigDecimal getPrecioBase() {
        BigDecimal total = BigDecimal.ZERO;
        for (Componente c : this.subComponentes) {
        	if(c == null)
        		continue;
            total = total.add(c.getPrecioBase());
        }
        return total.multiply( new BigDecimal(1).subtract( new BigDecimal(DSCTO_PRECIO_AGREGADO).divide(new BigDecimal(100)) )
	             );
	}
	
    private BigDecimal calcularPrecioComponenteAgregado(int cantidadI) {
        BigDecimal total = BigDecimal.ZERO;
        for (Componente c : this.subComponentes) {
        	if(c == null)
        		continue;
            total = total.add(c.getPrecioBase());
        }
//      return total.multiply(BigDecimal.valueOf(1 - (DSCTO_PRECIO_AGREGADO / 100)));
        return total.multiply( new BigDecimal(1)
        		                   .subtract( new BigDecimal(DSCTO_PRECIO_AGREGADO)
        		                		          .divide(new BigDecimal(100)) )
        		             );
    }
	
    private BigDecimal calcularCostoComponenteAgregado(int cantidadI) {
        BigDecimal costoPc = BigDecimal.ZERO;
        for (Componente c : this.subComponentes) {
        	if(c == null)
        		continue;
        	costoPc = costoPc.add(c.getCosto());
        }
        return costoPc;
    }

	/**
	 * Obtiene la categoría específica de este componente.
	 * Devuelve "PC" para identificar que este componente es una computadora personal compuesta.
	 * 
	 * @return La cadena "PC" que representa la categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "PC";
	}

	/**
	 * Obtiene la lista de subcomponentes que forman parte de esta PC.
	 * Devuelve la lista de componentes simples (discos, monitores, tarjetas) que han sido agregados a la PC.
	 * 
	 * @return La lista de subcomponentes de la PC
	 */
	public List<ComponenteSimple> getSubComponentes() {
		return subComponentes;
	}
	
	/**
	 * Muestra las características específicas de la PC en la consola.
	 * Primero invoca el método de la clase padre para mostrar las características generales del componente,
	 * y luego muestra los detalles de cada tipo de subcomponente agrupados por categoría.
	 */
	@Override
	public void mostrarCaracteristicas() {
		super.mostrarCaracteristicas();
		System.out.println("\n==== Disco(s) ====");
		this.subComponentes.stream()
		                   .filter(scI->scI instanceof DiscoDuro)
		                   .forEach(dscI-> { dscI.mostrarCaracteristicas(); 
		                   		             System.out.println();
		                   		             });
		System.out.println("==== Monitor(es) ====");
		this.subComponentes.stream()
		                   .filter(scI->scI instanceof Monitor)
		                   .forEach(monI-> { monI.mostrarCaracteristicas(); 
		                   		             System.out.println();});
		System.out.println("==== Tarjeta(s) de Video ====");
		this.subComponentes.stream()
		                   .filter(scI->scI instanceof Monitor)
		                   .forEach(tarI-> { tarI.mostrarCaracteristicas(); 
		                   		             System.out.println();});
	}
}
