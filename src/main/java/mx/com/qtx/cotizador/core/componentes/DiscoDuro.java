package mx.com.qtx.cotizadorv1ds.core.componentes;

import java.math.BigDecimal;

public class DiscoDuro extends ComponenteSimple {
	private String capacidadAlm;

	protected DiscoDuro(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String capacidadAlm) {
		super(id, descripcion, marca, modelo, costo, precioBase);
		this.capacidadAlm = capacidadAlm;
	}

	public String getCapacidadAlm() {
		return capacidadAlm;
	}

	public void setCapacidadAlm(String capacidadAlm) {
		this.capacidadAlm = capacidadAlm;
	}

	@Override
	public void mostrarCaracteristicas() {
		super.mostrarCaracteristicas();
        System.out.println("Capacidad almacenamiento: " + this.capacidadAlm);
		
	}

	@Override
	public String getCategoria() {
		return "Disco Duro";
	}
	
}
