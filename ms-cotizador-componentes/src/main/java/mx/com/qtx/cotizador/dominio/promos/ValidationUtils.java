package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Utilidades de validación para clases de promociones.
 *
 * Proporciona métodos centralizados para validar parámetros comunes
 * en los cálculos de promociones, garantizando consistencia y
 * evitando errores de cálculo por datos inválidos.
 *
 * @author Sistema Cotizador - Validaciones
 * @version 1.0
 * @since 2.0.0
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Clase utilitaria, no debe instanciarse
    }

    /**
     * Valida que una cadena no sea null ni vacía.
     *
     * @param value Valor a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el valor es null o vacío
     */
    public static void validateNotNullOrEmpty(String value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " no puede ser null");
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " no puede estar vacío");
        }
    }

    /**
     * Valida que un objeto no sea null.
     *
     * @param value Objeto a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el objeto es null
     */
    public static void validateNotNull(Object value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " no puede ser null");
        }
    }

    /**
     * Valida que una cantidad sea no negativa.
     *
     * @param cantidad Cantidad a validar
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    public static void validateCantidadNoNegativa(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa: " + cantidad);
        }
    }

    /**
     * Valida que un precio base sea válido (no null y no negativo).
     *
     * @param precioBase Precio base a validar
     * @throws IllegalArgumentException si el precio es null o negativo
     */
    public static void validatePrecioBase(BigDecimal precioBase) {
        validateNotNull(precioBase, "precioBase");
        if (precioBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo: " + precioBase);
        }
    }

    /**
     * Valida que un porcentaje de descuento esté en rango válido (0-100).
     *
     * @param porcentaje Porcentaje a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el porcentaje está fuera del rango válido
     */
    public static void validatePorcentajeDescuento(double porcentaje, String parameterName) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException(parameterName + " debe estar entre 0 y 100: " + porcentaje);
        }
    }

    /**
     * Valida que un número entero sea positivo.
     *
     * @param valor Valor a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el valor no es positivo
     */
    public static void validatePositive(int valor, String parameterName) {
        if (valor <= 0) {
            throw new IllegalArgumentException(parameterName + " debe ser positivo: " + valor);
        }
    }

    /**
     * Valida parámetros para promoción N X M.
     *
     * @param n Número de unidades que debe llevar
     * @param m Número de unidades que paga
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public static void validateNXMParameters(int n, int m) {
        validatePositive(n, "N (unidades a llevar)");
        validatePositive(m, "M (unidades a pagar)");
        if (m > n) {
            throw new IllegalArgumentException("M (unidades a pagar) no puede ser mayor que N (unidades a llevar): M=" + m + ", N=" + n);
        }
    }

    /**
     * Valida que un mapa de descuentos por cantidad sea válido.
     *
     * @param mapCantidadVsDscto Mapa a validar
     * @throws IllegalArgumentException si el mapa es inválido
     */
    public static void validateMapaDescuentosPorCantidad(Map<Integer, Double> mapCantidadVsDscto) {
        validateNotNull(mapCantidadVsDscto, "mapCantidadVsDscto");
        if (mapCantidadVsDscto.isEmpty()) {
            throw new IllegalArgumentException("El mapa de descuentos por cantidad no puede estar vacío");
        }

        for (Map.Entry<Integer, Double> entry : mapCantidadVsDscto.entrySet()) {
            Integer cantidad = entry.getKey();
            Double porcentaje = entry.getValue();

            validateNotNull(cantidad, "cantidad en mapa de descuentos");
            validateNotNull(porcentaje, "porcentaje en mapa de descuentos");
            validatePositive(cantidad, "cantidad en mapa de descuentos");
            validatePorcentajeDescuento(porcentaje, "porcentaje de descuento en mapa");
        }
    }

    /**
     * Valida los parámetros comunes para el cálculo de importe de promoción.
     *
     * @param cantidad Cantidad de unidades
     * @param precioBase Precio base unitario
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public static void validateParametrosCalculoPromocion(int cantidad, BigDecimal precioBase) {
        validateCantidadNoNegativa(cantidad);
        validatePrecioBase(precioBase);
    }
}