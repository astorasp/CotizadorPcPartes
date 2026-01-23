package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una computadora personal (PC) compuesta por varios subcomponentes.
 * Extiende Componente y calcula su precio y costo basado en la suma de sus componentes,
 * aplicando un descuento agregado por ensamblaje.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public class Pc extends Componente {
	/** Lista de subcomponentes que conforman la PC */
	private List<ComponenteSimple> subComponentes;
	/** Descuento porcentual aplicado al precio total por ensamblaje */
	private static final float DSCTO_PRECIO_AGREGADO = 20.0f;

	/**
	 * Constructor protegido para crear una PC con lista de subcomponentes.
	 * Calcula automáticamente el precio base y costo total basado en los subcomponentes.
	 *
	 * @param id Identificador único de la PC
	 * @param descripcion Descripción detallada de la PC
	 * @param marca Marca de la PC
	 * @param modelo Modelo específico de la PC
	 * @param subComponentes Lista de componentes simples que conforman la PC
	 */
	protected Pc(String id, String descripcion, String marca, String modelo, 
			List<ComponenteSimple> subComponentes) {
		super(id, descripcion, marca, modelo, new BigDecimal(0), new BigDecimal(0));
		this.subComponentes = subComponentes;
		this.setPrecioBase(this.calcularPrecioComponenteAgregado(0));
		this.setCosto(this.calcularCostoComponenteAgregado(0));
	}
	
	/**
	 * Constructor protegido que crea una PC a partir de un PcBuilder.
	 * Extrae los componentes del builder y calcula precio y costo.
	 *
	 * @param config El PcBuilder con la configuración de la PC
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
	 * Calcula el precio base de la PC aplicando descuento por ensamblaje.
	 * Suma los precios base de todos los subcomponentes y aplica el descuento.
	 *
	 * @return El precio base calculado de la PC
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
	
    /**
     * Calcula el precio total de los componentes aplicando el descuento de ensamblaje.
     * Método privado utilizado internamente para calcular el precio base.
     *
     * @param cantidadI Parámetro no utilizado en este cálculo
     * @return El precio total calculado con descuento
     */
    private BigDecimal calcularPrecioComponenteAgregado(int cantidadI) {
        BigDecimal total = BigDecimal.ZERO;
        for (Componente c : this.subComponentes) {
        	if(c == null)
        		continue;
            total = total.add(c.getPrecioBase());
        }
        return total.multiply( new BigDecimal(1)
        			                   .subtract( new BigDecimal(DSCTO_PRECIO_AGREGADO)
        							          .divide(new BigDecimal(100)) )
        			             );
    }
	
    /**
     * Calcula el costo total de los subcomponentes.
     * Suma los costos individuales de todos los componentes sin aplicar descuentos.
     *
     * @param cantidadI Parámetro no utilizado en este cálculo
     * @return El costo total calculado
     */
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
	 * Obtiene la categoría de este componente.
	 *
	 * @return "PC" como categoría del componente
	 */
	@Override
	public String getCategoria() {
		return "PC";
	}

	/**
	 * Obtiene la lista de subcomponentes que conforman la PC.
	 *
	 * @return Lista de componentes simples
	 */
	public List<ComponenteSimple> getSubComponentes() {
		return subComponentes;
	}
	
	/**
	 * Muestra las características de la PC y sus subcomponentes.
	 * Incluye información de la PC padre y detalles de discos, monitores y tarjetas de video.
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
		                   .filter(scI->scI instanceof TarjetaVideo)
		                   .forEach(tarI-> { tarI.mostrarCaracteristicas(); 
		                   	             System.out.println();});
	}
}
