package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;

/**
 * Implementación de promoción que no aplica ningún descuento.
 * Calcula el precio total multiplicando simplemente la cantidad por el precio base,
 * sin aplicar descuentos adicionales.
 *
 * @author Alejandro Cruz Rojas
 * @version 1.0
 * @created 24-mar.-2025 11:20:57 p. m.
 */
public class PromSinDescto extends PromBase {


	/**
	 * Constructor que crea una promoción sin descuento.
	 * Inicializa con descripción y nombre que indican precio regular.
	 */
	public PromSinDescto() {
		super("No se aplica ningun descuento", "Precio regular");
	}

	/**
	 * Calcula el importe total sin aplicar ningún descuento.
	 * Simplemente multiplica la cantidad por el precio base.
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe total calculado (cantidad * precio base)
	 * @throws IllegalArgumentException si los parámetros son inválidos
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		ValidationUtils.validateParametrosCalculoPromocion(cant, precioBase);
		return precioBase.multiply(new BigDecimal(cant));
	}

}