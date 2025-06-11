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
