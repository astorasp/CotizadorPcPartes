package mx.com.qtx.cotizador.dominio.core;

import java.util.List;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;

/**
 * Interfaz que define el contrato para los servicios de cotización de componentes.
 * <p>
 * Esta interfaz establece el patrón Strategy para diferentes algoritmos de cotización
 * utilizados en el sistema CotizadorPcPartes. Las implementaciones concretas pueden
 * variar en cómo manejan los componentes, calculan precios y aplican promociones.
 * </p>
 *
 * <h3>Implementaciones disponibles:</h3>
 * <ul>
 *   <li>{@link mx.com.qtx.cotizador.dominio.cotizadorA.Cotizador} - Cotizador A con listas separadas</li>
 *   <li>{@link mx.com.qtx.cotizador.dominio.cotizadorB.CotizadorConMap} - Cotizador B con mapa de componentes</li>
 * </ul>
 *
 * <h3>Patrón Strategy:</h3>
 * <p>
 * Esta interfaz implementa el patrón Strategy permitiendo cambiar dinámicamente
 * el algoritmo de cotización sin afectar al código cliente. Las diferentes estrategias
 * pueden optimizar el rendimiento según el volumen de componentes o complejidad de cálculos.
 * </p>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.cotizadorA.Cotizador
 * @see mx.com.qtx.cotizador.dominio.cotizadorB.CotizadorConMap
 */
public interface ICotizador {

    /**
     * Agrega un componente a la cotización actual.
     * <p>
     * Este método permite añadir componentes individuales a la cotización en proceso,
     * especificando la cantidad deseada. El componente debe existir en el catálogo
     * y estar disponible para cotización.
     * </p>
     *
     * @param cantidad Cantidad de unidades del componente a agregar (debe ser positiva)
     * @param componente El componente a agregar a la cotización
     * @throws IllegalArgumentException si la cantidad es menor o igual a cero
     * @throws IllegalArgumentException si el componente es null
     */
    void agregarComponente(int cantidad, Componente componente);

    /**
     * Elimina un componente de la cotización actual.
     * <p>
     * Este método remueve un componente específico de la cotización en proceso
     * basándose en su identificador único. Si el componente no existe en la
     * cotización actual, se lanza una excepción.
     * </p>
     *
     * @param idComponente Identificador único del componente a eliminar
     * @throws ComponenteInvalidoException si el componente no existe en la cotización
     * @throws IllegalArgumentException si el idComponente es null o vacío
     */
    void eliminarComponente(String idComponente) throws ComponenteInvalidoException;

    /**
     * Genera una cotización completa basada en los componentes agregados.
     * <p>
     * Este método procesa todos los componentes agregados, aplica los cálculos
     * de impuestos especificados, calcula totales y genera una cotización completa
     * lista para ser presentada al cliente o guardada en el sistema.
     * </p>
     *
     * @param calculadoresImpuestos Lista de calculadores de impuestos a aplicar
     *                              (pueden incluir IVA, impuestos locales, federales, etc.)
     * @return Una instancia de {@link Cotizacion} completa con todos los cálculos realizados
     * @throws IllegalStateException si no hay componentes agregados para cotizar
     */
    Cotizacion generarCotizacion(List<CalculadorImpuesto> calculadoresImpuestos);

    /**
     * Lista todos los componentes actualmente en la cotización.
     * <p>
     * Este método muestra en consola (o registra) todos los componentes que
     * han sido agregados a la cotización actual, incluyendo cantidades y
     * precios base. Es útil para debugging y verificación del estado actual.
     * </p>
     */
    void listarComponentes();
}
