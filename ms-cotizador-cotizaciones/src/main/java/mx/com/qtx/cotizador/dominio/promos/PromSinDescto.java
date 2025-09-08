package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;

/**
 * Implementación de promoción que representa "Sin descuento" (precio regular).
 * <p>
 * Esta clase implementa la promoción base más simple: el precio regular sin
 * ningún tipo de descuento. Sirve como caso base en el sistema de promociones
 * y como punto de comparación para calcular el ahorro de otras promociones.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Caso base:</strong> Representa el precio estándar sin promociones</li>
 *   <li><strong>Referencia:</strong> Permite comparar el precio regular vs precio con descuento</li>
 *   <li><strong>Compatibilidad:</strong> Se puede usar como base para promociones acumulables</li>
 *   <li><strong>Transparencia:</strong> Asegura que siempre haya un precio de referencia</li>
 * </ul>
 *
 * <h3>Cálculo aplicado:</h3>
 * <p>
 * La fórmula es la más simple posible: precio final = cantidad × precio unitario.
 * No hay ningún descuento o modificación aplicada.
 * </p>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * // Crear promoción sin descuento
 * Promocion precioRegular = new PromSinDescto();
 *
 * // Calcular precio para 5 unidades a $100 cada una
 * BigDecimal precioFinal = precioRegular.calcularImportePromocion(5, new BigDecimal("100.00"));
 * // Resultado: 5 × 100 = 500.00 (precio regular, sin descuento)
 * }</pre>
 *
 * <h3>Comparación con otras promociones:</h3>
 * <table border="1">
 *   <tr><th>Promoción</th><th>Precio para 6 unidades a $100</th><th>Descuento</th></tr>
 *   <tr><td>Sin descuento</td><td>$600.00</td><td>0%</td></tr>
 *   <tr><td>3×2</td><td>$400.00</td><td>33.3%</td></tr>
 *   <tr><td>10% descuento</td><td>$540.00</td><td>10%</td></tr>
 *   <tr><td>2×1</td><td>$300.00</td><td>50%</td></tr>
 * </table>
 *
 * <h3>Uso como base para promociones acumulables:</h3>
 * <p>
 * Aunque es una promoción "sin descuento", puede servir como base para
 * aplicar descuentos adicionales:
 * </p>
 * <pre>{@code
 * // Precio regular con 15% de descuento adicional
 * Promocion combo = new PromDsctoPlano(new PromSinDescto(), 15.0f);
 * }</pre>
 *
 * <h3>Casos de uso típicos:</h3>
 * <ul>
 *   <li><strong>Producto sin promoción:</strong> Artículos con precio regular</li>
 *   <li><strong>Referencia de precio:</strong> Calcular precio base para comparaciones</li>
 *   <li><strong>Base para decoradores:</strong> Punto de partida para descuentos acumulables</li>
 *   <li><strong>Producto nuevo:</strong> Artículos sin promociones activas</li>
 * </ul>
 *
 * <h3>Características técnicas:</h3>
 * <ul>
 *   <li><strong>Constructor único:</strong> No requiere parámetros</li>
 *   <li><strong>Cálculo directo:</strong> Multiplicación simple sin lógica adicional</li>
 *   <li><strong>Alta performance:</strong> Operación matemática básica</li>
 *   <li><strong>Precisión decimal:</strong> Usa BigDecimal para evitar errores de redondeo</li>
 * </ul>
 *
 * @author Alejandro Cruz Rojas
 * @version 1.0
 * @created 24-mar.-2025 11:20:57 p. m.
 * @see mx.com.qtx.cotizador.dominio.promos.PromBase
 * @see mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano
 * @see java.math.BigDecimal
 */
public class PromSinDescto extends PromBase {

	/**
	 * Constructor que crea una promoción sin descuento.
	 * <p>
	 * Inicializa la promoción con la descripción "No se aplica ningún descuento"
	 * y nombre "Precio regular". Esta promoción representa el precio estándar
	 * sin ningún tipo de descuento aplicado.
	 * </p>
	 */
	public PromSinDescto() {
		super("No se aplica ningun descuento", "Precio regular");
	}

	/**
	 * Calcula el importe total multiplicando cantidad por precio base.
	 * <p>
	 * Esta implementación calcula el precio regular sin aplicar ningún
	 * descuento o modificación. Es la fórmula más básica: cantidad × precio unitario.
	 * </p>
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return Importe total (cantidad × precioBase) sin descuento aplicado
	 */
	public BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase){
		return precioBase.multiply(new BigDecimal(cant));
	}

}