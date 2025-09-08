package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

public class Monitor extends ComponenteSimple {
	
	protected Monitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}
	
	@Override
	public String getCategoria() {
		return "Monitor";
	}

}
