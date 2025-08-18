package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

public class Monitor extends ComponenteSimple {
	
//	private static Map<Integer, Double> mapDsctos = Map.of(0,  0.0,
//														   3,  5.0,
//														   6, 10.0,
//														   9, 12.0);

	protected Monitor(String id, String descripcion, BigDecimal precioBase, String marca, String modelo) {
		super(id, descripcion, precioBase, marca, modelo);
	}
	
	protected Monitor(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, IPromocion promocion) {
		super(id, descripcion, precioBase, marca, modelo, promocion);
	}
	
//	public BigDecimal cotizar(int cantidadI) {
//		return PromocionUtil.calcularPrecioPromocionDsctoXcant(cantidadI, this.precioBase, mapDsctos);
//	}

	@Override
	public String getCategoria() {
		return "Monitor";
	}

}