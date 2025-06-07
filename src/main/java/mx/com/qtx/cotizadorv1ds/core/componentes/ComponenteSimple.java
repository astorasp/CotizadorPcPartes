package mx.com.qtx.cotizadorv1ds.core.componentes;

import java.math.BigDecimal;

public abstract class ComponenteSimple extends Componente{

	public ComponenteSimple(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}


}