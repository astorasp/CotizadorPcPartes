package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

public class TarjetaVideo extends ComponenteSimple {
	private String memoria;

	protected TarjetaVideo(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, String memoria) {
		super(id, descripcion, precioBase, marca, modelo);
		this.memoria = memoria;
	}
	
	protected TarjetaVideo(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, String memoria, IPromocion promocion) {
		super(id, descripcion, precioBase, marca, modelo, promocion);
		this.memoria = memoria;
	}

	public String getMemoria() {
		return memoria;
	}

	public void setMemoria(String memoria) {
		this.memoria = memoria;
	}
	
//	public BigDecimal cotizar(int cantidadI) {
//		return PromocionUtil.calcularPrecioPromocion3X2(cantidadI, this.precioBase);
//	}

	@Override
	public String getCategoria() {
		return "Tarjeta de Video";
	}
	
}