package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

public abstract class ComponenteSimple extends Componente{

	public ComponenteSimple(String id, String descripcion, BigDecimal precioBase, String marca, String modelo) {
		super(id, descripcion, precioBase, marca, modelo);
	}
	
	public ComponenteSimple(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, IPromocion promocion) {
		super(id, descripcion, precioBase, marca, modelo, promocion);
	}


}