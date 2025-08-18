package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

public class DiscoDuro extends ComponenteSimple {
	private String capacidadAlm;

	protected DiscoDuro(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, String capacidadAlm) {
		super(id, descripcion, precioBase, marca, modelo);
		this.capacidadAlm = capacidadAlm;
	}
	
	protected DiscoDuro(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, String capacidadAlm, IPromocion promocion) {
		super(id, descripcion, precioBase, marca, modelo, promocion);
		this.capacidadAlm = capacidadAlm;
	}

	public String getCapacidadAlm() {
		return capacidadAlm;
	}

	public void setCapacidadAlm(String capacidadAlm) {
		this.capacidadAlm = capacidadAlm;
	}


	@Override
	public String getCategoria() {
		return "Disco Duro";
	}
	
}