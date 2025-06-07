package mx.com.qtx.cotizadorv1ds.promos;

import java.math.BigDecimal;

/**
 * @author Alejandro Cruz Rojas
 * @version 1.0
 * @created 24-mar.-2025 11:20:57 p. m.
 */
public class PromSinDescto extends PromBase {


	public PromSinDescto() {
		super("No se aplica ningun descuento", "Precio regular");
	}

	/**
	 * 
	 * @param cant
	 * @param precioBase
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		return precioBase.multiply(new BigDecimal(cant));
	}

}