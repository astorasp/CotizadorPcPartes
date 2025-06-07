package mx.com.qtx.cotizadorv1ds.core.componentes;

import java.math.BigDecimal;

public class Monitor extends ComponenteSimple {
	
//	private static Map<Integer, Double> mapDsctos = Map.of(0,  0.0,
//														   3,  5.0,
//														   6, 10.0,
//														   9, 12.0);

	protected Monitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		super(id, descripcion, marca, modelo, costo, precioBase);
	}
	
//	public BigDecimal cotizar(int cantidadI) {
//		return PromocionUtil.calcularPrecioPromocionDsctoXcant(cantidadI, this.precioBase, mapDsctos);
//	}

	@Override
	public String getCategoria() {
		return "Monitor";
	}

}
