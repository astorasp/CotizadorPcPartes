package mx.com.qtx.cotizador.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.pedidos.IPresupuesto;

/**
 * Implementación mock de IPresupuesto para uso en pruebas unitarias.
 *
 * <p>Esta clase simula un presupuesto con datos predefinidos para facilitar
 * las pruebas de componentes que requieren instancias de IPresupuesto sin
 * depender de implementaciones reales como CotizacionPresupuestoAdapter.</p>
 *
 * <h3>Funcionalidades Mock:</h3>
 * <ul>
 *   <li>Datos predefinidos para componentes de test</li>
 *   <li>Soporte para múltiples artículos simultáneamente</li>
 *   <li>Configuración flexible de cantidades y precios</li>
 *   <li>Manejo de casos no encontrados</li>
 * </ul>
 *
 * <h3>Uso Típico:</h3>
 * <pre>{@code
 * // En una clase de test
 * IPresupuesto mockPresupuesto = new MockPresupuesto();
 * mockPresupuesto.agregarArticulo("COMP-001", "Monitor", 2,
 *     new BigDecimal("500.00"), new BigDecimal("1000.00"));
 *
 * GestorPedidos gestor = new GestorPedidos(proveedores);
 * gestor.agregarPresupuesto(mockPresupuesto);
 * }</pre>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 2.0.0
 * @see IPresupuesto Interfaz que implementa
 * @see TestUtils Utilidades de test relacionadas
 */
public class MockPresupuesto implements IPresupuesto {

    private final Map<String, String> descripciones;
    private final Map<String, Integer> cantidades;
    private final Map<String, Map<String, Object>> datosArticulos;

    /**
     * Constructor que inicializa el mock con datos por defecto.
     */
    public MockPresupuesto() {
        this.descripciones = new HashMap<>();
        this.cantidades = new HashMap<>();
        this.datosArticulos = new HashMap<>();

        // Agregar datos por defecto de TestUtils
        agregarArticulo(
            TestUtils.DEFAULT_COMPONENTE_ID,
            TestUtils.DEFAULT_COMPONENTE_DESC,
            TestUtils.DEFAULT_CANTIDAD,
            TestUtils.DEFAULT_PRECIO_BASE,
            TestUtils.DEFAULT_TOTAL_COTIZADO
        );
    }

    /**
     * Agrega un artículo al mock con todos sus datos.
     *
     * @param idArticulo ID del artículo
     * @param descripcion Descripción del artículo
     * @param cantidad Cantidad del artículo
     * @param precioBase Precio base unitario
     * @param importeTotal Importe total de la línea
     */
    public void agregarArticulo(String idArticulo, String descripcion, int cantidad,
                               BigDecimal precioBase, BigDecimal importeTotal) {
        descripciones.put(idArticulo, descripcion);
        cantidades.put(idArticulo, cantidad);

        Map<String, Object> datos = new HashMap<>();
        datos.put("descripcion", descripcion);
        datos.put("cantidad", cantidad);
        datos.put("precioBase", precioBase);
        datos.put("importeTotalLinea", importeTotal);

        datosArticulos.put(idArticulo, datos);
    }

    /**
     * Limpia todos los artículos del mock.
     */
    public void limpiar() {
        descripciones.clear();
        cantidades.clear();
        datosArticulos.clear();
    }

    /**
     * Obtiene el número de artículos en el mock.
     *
     * @return Cantidad de artículos configurados
     */
    public int getNumeroArticulos() {
        return cantidades.size();
    }

    @Override
    public String getDescripcionArticulo(String idArticulo) {
        return descripciones.getOrDefault(idArticulo, "Descripción no encontrada");
    }

    @Override
    public Map<String, Integer> getCantidadesXIdArticulo() {
        return new HashMap<>(cantidades);
    }

    @Override
    public Map<String, Object> getDatosArticulo(String idArticulo) {
        return datosArticulos.getOrDefault(idArticulo, new HashMap<>());
    }

    /**
     * Crea un MockPresupuesto con múltiples artículos para tests complejos.
     *
     * @return MockPresupuesto con varios artículos configurados
     */
    public static MockPresupuesto crearConMultiplesArticulos() {
        MockPresupuesto mock = new MockPresupuesto();
        mock.limpiar(); // Limpiar datos por defecto

        mock.agregarArticulo("COMP-001", "Monitor LED 24\"", 1,
                           new BigDecimal("500.00"), new BigDecimal("500.00"));
        mock.agregarArticulo("COMP-002", "Teclado Mecánico", 2,
                           new BigDecimal("150.00"), new BigDecimal("300.00"));
        mock.agregarArticulo("COMP-003", "Mouse Gaming", 1,
                           new BigDecimal("75.00"), new BigDecimal("75.00"));

        return mock;
    }

    /**
     * Crea un MockPresupuesto vacío para tests de edge cases.
     *
     * @return MockPresupuesto sin artículos
     */
    public static MockPresupuesto crearVacio() {
        MockPresupuesto mock = new MockPresupuesto();
        mock.limpiar();
        return mock;
    }

    /**
     * Crea un MockPresupuesto con valores límite para tests de edge cases.
     *
     * @return MockPresupuesto con valores extremos
     */
    public static MockPresupuesto crearConValoresLimite() {
        MockPresupuesto mock = new MockPresupuesto();
        mock.limpiar();

        mock.agregarArticulo(
            "COMP-EDGE-" + "X".repeat(20),
            "Descripción muy larga para probar límites de caracteres y manejo de strings extensos",
            Integer.MAX_VALUE,
            new BigDecimal("999999999.99"),
            new BigDecimal("999999999999999.99")
        );

        return mock;
    }
}