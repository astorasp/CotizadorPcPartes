package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Interfaz que define el contrato para calcular promociones en componentes.
 * Las clases que implementan esta interfaz proporcionan lógica específica
 * para calcular el importe total de un componente considerando descuentos o promociones.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public interface IPromocion {
	/**
	 * Calcula el importe total de un componente aplicando la promoción específica.
	 * El cálculo depende del tipo de promoción implementada por la clase concreta.
	 *
	 * @param cant Cantidad de unidades del componente a cotizar
	 * @param precioBase Precio base unitario del componente sin promoción
	 * @return El importe total calculado aplicando la promoción
	 */
	BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);
}
