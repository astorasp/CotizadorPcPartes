package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Pc extends Componente {
	private List<ComponenteSimple> subComponentes;
	private static final float DSCTO_PRECIO_AGREGADO = 20.0f;

	protected Pc(String id, String descripcion, String marca, String modelo, 
			List<ComponenteSimple> subComponentes) {
		super(id, descripcion, marca, modelo, new BigDecimal(0), new BigDecimal(0));
		this.subComponentes = subComponentes;
		this.setPrecioBase(this.calcularPrecioComponenteAgregado(0));
		this.setCosto(this.calcularCostoComponenteAgregado(0));
	}
	
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

	@Override
	public String getCategoria() {
		return "PC";
	}

	public List<ComponenteSimple> getSubComponentes() {
		return subComponentes;
	}
	
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
