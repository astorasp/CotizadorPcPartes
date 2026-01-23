package mx.com.qtx.cotizador.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Utilidades de validación centralizadas para el microservicio de cotizaciones.
 *
 * <p>Esta clase proporciona métodos estáticos de validación específicos para
 * el dominio de cotizaciones, incluyendo validaciones para:
 * <ul>
 * <li>Cotizaciones y detalles de cotización</li>
 * <li>Cálculos de impuestos por país</li>
 * <li>Montos financieros y fechas</li>
 * <li>Validaciones generales de parámetros</li>
 * </ul>
 * </p>
 *
 * <p>Todos los métodos lanzan {@link IllegalArgumentException} con mensajes
 * descriptivos cuando los parámetros no cumplen con los criterios de validación.</p>
 *
 * <h3>Casos de uso típicos:</h3>
 * <pre>{@code
 * // Validar una cotización
 * ValidationUtils.validateCotizacion(fecha, total, totalImpuestos, detalles);
 *
 * // Validar un detalle
 * ValidationUtils.validateDetalleCotizacion(cantidad, precioBase, importeCotizado);
 *
 * // Validar parámetros para cálculo de impuestos
 * ValidationUtils.validateParametrosImpuesto("Mexico", new BigDecimal("1000"));
 * }</pre>
 *
 * @author Sistema Cotizador - Testing Team
 * @version 1.0
 * @since 2.0.0
 */
public final class ValidationUtils {

    /**
     * Países soportados para cálculos de impuestos.
     */
    private static final Set<String> PAISES_VALIDOS = Set.of(
        "Mexico", "Canada", "USA", "Estados Unidos", "Canadá", "México"
    );

    /**
     * Constructor privado para prevenir instanciación.
     * Esta clase solo contiene métodos estáticos de utilidad.
     */
    private ValidationUtils() {
        // Clase utilitaria, no debe instanciarse
    }

    // ==================== VALIDACIONES GENERALES ====================

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
     * Valida que una cadena no sea null ni vacía.
     *
     * @param value Cadena a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si la cadena es null o vacía
     */
    public static void validateNotNullOrEmpty(String value, String parameterName) {
        validateNotNull(value, parameterName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " no puede estar vacío");
        }
    }

    /**
     * Valida que un número entero sea positivo (mayor que cero).
     *
     * @param value Valor a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el valor no es positivo
     */
    public static void validatePositive(int value, String parameterName) {
        if (value <= 0) {
            throw new IllegalArgumentException(parameterName + " debe ser positivo: " + value);
        }
    }

    /**
     * Valida que un número entero no sea negativo.
     *
     * @param value Valor a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el valor es negativo
     */
    public static void validateNonNegative(int value, String parameterName) {
        if (value < 0) {
            throw new IllegalArgumentException(parameterName + " no puede ser negativo: " + value);
        }
    }

    // ==================== VALIDACIONES FINANCIERAS ====================

    /**
     * Valida que un monto BigDecimal sea válido para operaciones financieras.
     *
     * @param monto Monto a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el monto es null o negativo
     */
    public static void validateMontoFinanciero(BigDecimal monto, String parameterName) {
        validateNotNull(monto, parameterName);
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(parameterName + " no puede ser negativo: " + monto);
        }
    }

    /**
     * Valida que un monto sea positivo (mayor que cero).
     *
     * @param monto Monto a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el monto es null, negativo o cero
     */
    public static void validateMontoPositivo(BigDecimal monto, String parameterName) {
        validateNotNull(monto, parameterName);
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(parameterName + " debe ser mayor que cero: " + monto);
        }
    }

    /**
     * Valida que un porcentaje esté en el rango válido (0-100%).
     *
     * @param porcentaje Porcentaje a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si el porcentaje está fuera del rango válido
     */
    public static void validatePorcentajeImpuesto(double porcentaje, String parameterName) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException(parameterName + " debe estar entre 0 y 100: " + porcentaje);
        }
    }

    // ==================== VALIDACIONES DE FECHA ====================

    /**
     * Valida que una fecha sea válida para cotizaciones.
     *
     * @param fecha Fecha a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si la fecha es null o futura
     */
    public static void validateFechaCotizacion(LocalDate fecha, String parameterName) {
        validateNotNull(fecha, parameterName);
        LocalDate hoy = LocalDate.now();
        if (fecha.isAfter(hoy)) {
            throw new IllegalArgumentException(parameterName + " no puede ser una fecha futura: " + fecha);
        }
    }

    // ==================== VALIDACIONES DE COTIZACIÓN ====================

    /**
     * Valida los parámetros principales de una cotización.
     *
     * @param fecha Fecha de la cotización
     * @param total Total de la cotización
     * @param totalImpuestos Total de impuestos
     * @param detalles Lista de detalles de cotización
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public static void validateCotizacion(LocalDate fecha, BigDecimal total,
                                        BigDecimal totalImpuestos, List<?> detalles) {
        validateFechaCotizacion(fecha, "fecha");
        validateMontoFinanciero(total, "total");
        validateMontoFinanciero(totalImpuestos, "totalImpuestos");
        validateNotNull(detalles, "detalles");

        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La cotización debe tener al menos un detalle");
        }

        // Validar que los impuestos no excedan el total
        if (totalImpuestos.compareTo(total) > 0) {
            throw new IllegalArgumentException("Los impuestos no pueden ser mayores que el total: "
                + "impuestos=" + totalImpuestos + ", total=" + total);
        }
    }

    /**
     * Valida que el número de cotización sea válido.
     *
     * @param numeroCotizacion Número a validar
     * @throws IllegalArgumentException si el número no es positivo
     */
    public static void validateNumeroCotizacion(long numeroCotizacion) {
        if (numeroCotizacion <= 0) {
            throw new IllegalArgumentException("El número de cotización debe ser positivo: " + numeroCotizacion);
        }
    }

    // ==================== VALIDACIONES DE DETALLE DE COTIZACIÓN ====================

    /**
     * Valida los parámetros de un detalle de cotización.
     *
     * @param cantidad Cantidad de unidades
     * @param precioBase Precio base unitario
     * @param importeCotizado Importe total cotizado
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public static void validateDetalleCotizacion(int cantidad, BigDecimal precioBase,
                                               BigDecimal importeCotizado) {
        validatePositive(cantidad, "cantidad");
        validateMontoFinanciero(precioBase, "precioBase");
        validateMontoFinanciero(importeCotizado, "importeCotizado");
    }

    /**
     * Valida los campos de texto de un detalle de cotización.
     *
     * @param idComponente ID del componente
     * @param descripcion Descripción del componente
     * @param categoria Categoría del componente
     * @throws IllegalArgumentException si algún campo es null o vacío
     */
    public static void validateCamposTextoDetalle(String idComponente, String descripcion,
                                                String categoria) {
        validateNotNullOrEmpty(idComponente, "idComponente");
        validateNotNullOrEmpty(descripcion, "descripcion");
        validateNotNullOrEmpty(categoria, "categoria");
    }

    /**
     * Valida que el número de detalle sea válido.
     *
     * @param numeroDetalle Número a validar
     * @throws IllegalArgumentException si el número no es positivo
     */
    public static void validateNumeroDetalle(int numeroDetalle) {
        validatePositive(numeroDetalle, "numeroDetalle");
    }

    // ==================== VALIDACIONES DE IMPUESTOS ====================

    /**
     * Valida los parámetros para cálculo de impuestos.
     *
     * @param pais País para el cual calcular impuestos
     * @param monto Monto base para el cálculo
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public static void validateParametrosImpuesto(String pais, BigDecimal monto) {
        validateNotNullOrEmpty(pais, "pais");
        validateMontoFinanciero(monto, "monto");

        if (!PAISES_VALIDOS.contains(pais)) {
            throw new IllegalArgumentException("País no soportado: " + pais +
                ". Países válidos: " + PAISES_VALIDOS);
        }
    }

    /**
     * Valida que un país sea soportado para cálculos de impuestos.
     *
     * @param pais País a validar
     * @throws IllegalArgumentException si el país no es soportado
     */
    public static void validatePaisSoportado(String pais) {
        validateNotNullOrEmpty(pais, "pais");
        if (!PAISES_VALIDOS.contains(pais)) {
            throw new IllegalArgumentException("País no soportado: " + pais +
                ". Países válidos: " + PAISES_VALIDOS);
        }
    }

    /**
     * Valida que una tasa de impuesto sea válida.
     *
     * @param tasa Tasa a validar (como decimal, ej: 0.16 para 16%)
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si la tasa no está en rango válido
     */
    public static void validateTasaImpuesto(double tasa, String parameterName) {
        if (tasa < 0 || tasa > 1) {
            throw new IllegalArgumentException(parameterName + " debe estar entre 0 y 1: " + tasa);
        }
    }

    // ==================== VALIDACIONES DE LISTAS Y COLECCIONES ====================

    /**
     * Valida que una lista no sea null ni vacía.
     *
     * @param lista Lista a validar
     * @param parameterName Nombre del parámetro para mensajes de error
     * @throws IllegalArgumentException si la lista es null o vacía
     */
    public static void validateListaNoVacia(List<?> lista, String parameterName) {
        validateNotNull(lista, parameterName);
        if (lista.isEmpty()) {
            throw new IllegalArgumentException(parameterName + " no puede estar vacía");
        }
    }

    // ==================== VALIDACIONES DE CONSISTENCIA ====================

    /**
     * Valida la consistencia entre totales de una cotización.
     *
     * @param subtotal Subtotal calculado
     * @param impuestos Impuestos calculados
     * @param total Total final
     * @throws IllegalArgumentException si hay inconsistencias
     */
    public static void validateConsistenciaTotales(BigDecimal subtotal, BigDecimal impuestos,
                                                 BigDecimal total) {
        validateMontoFinanciero(subtotal, "subtotal");
        validateMontoFinanciero(impuestos, "impuestos");
        validateMontoFinanciero(total, "total");

        BigDecimal totalCalculado = subtotal.add(impuestos);
        if (totalCalculado.compareTo(total) != 0) {
            throw new IllegalArgumentException("Inconsistencia en totales: " +
                "subtotal=" + subtotal + " + impuestos=" + impuestos +
                " != total=" + total + " (calculado=" + totalCalculado + ")");
        }
    }
}