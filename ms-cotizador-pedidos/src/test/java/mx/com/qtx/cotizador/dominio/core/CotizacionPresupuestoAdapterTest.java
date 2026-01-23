package mx.com.qtx.cotizador.dominio.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.com.qtx.cotizador.dominio.pedidos.IPresupuesto;
import mx.com.qtx.cotizador.dominio.pedidos.excepciones.PresupuestoNoCargadoExcepcion;
import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Tests unitarios para la clase CotizacionPresupuestoAdapter.
 *
 * <p>Verifica la implementación del patrón Adapter que convierte objetos {@link Cotizacion}
 * al formato {@link IPresupuesto}. Esta clase es crítica para la integración entre el módulo
 * de cotizaciones y el módulo de pedidos.</p>
 *
 * <h3>Cobertura de Testing:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Validaciones de inicialización y manejo de nulls</li>
 *   <li><strong>getDescripcionArticulo:</strong> Búsqueda y retorno de descripciones</li>
 *   <li><strong>getCantidadesXIdArticulo:</strong> Extracción de cantidades por componente</li>
 *   <li><strong>getDatosArticulo:</strong> Recuperación de datos completos del componente</li>
 *   <li><strong>Casos Edge:</strong> Componentes no encontrados, cotizaciones vacías</li>
 *   <li><strong>Patrón Adapter:</strong> Verificación de adaptación correcta entre interfaces</li>
 * </ul>
 *
 * <h3>Comportamientos Críticos Validados:</h3>
 * <ul>
 *   <li>Conversión correcta de DetalleCotizacion a formato IPresupuesto</li>
 *   <li>Manejo seguro de componentes no encontrados</li>
 *   <li>Preservación de integridad de datos durante adaptación</li>
 *   <li>Validaciones de entrada y excepciones apropiadas</li>
 * </ul>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 2.0.0
 * @see CotizacionPresupuestoAdapter Clase bajo test
 * @see IPresupuesto Interfaz que implementa el adapter
 * @see Cotizacion Clase que se adapta
 * @see TestUtils Utilidades para crear objetos de test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CotizacionPresupuestoAdapter - Tests del Patrón Adapter")
class CotizacionPresupuestoAdapterTest {

    private CotizacionPresupuestoAdapter adapter;
    private Cotizacion cotizacionValida;

    @BeforeEach
    void setUp() throws PresupuestoNoCargadoExcepcion {
        cotizacionValida = TestUtils.crearCotizacionValida();
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Validaciones de Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería crear adapter correctamente con cotización válida")
        void constructor_deberiaCrearAdapterCorrectamente() throws PresupuestoNoCargadoExcepcion {
            // Act
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);

            // Assert
            assertThat(adapter).isNotNull();
            assertThat(adapter).isInstanceOf(IPresupuesto.class);
        }

        @Test
        @DisplayName("Debería lanzar PresupuestoNoCargadoExcepcion con cotización null")
        void constructor_deberiaLanzarExcepcionConCotizacionNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                new CotizacionPresupuestoAdapter(null);
            })
            .isInstanceOf(PresupuestoNoCargadoExcepcion.class);
        }

        @Test
        @DisplayName("Debería validar que la excepción sea del tipo correcto")
        void constructor_deberiaValidarTipoExcepcion() {
            // Act & Assert
            PresupuestoNoCargadoExcepcion excepcion = assertThrows(
                PresupuestoNoCargadoExcepcion.class,
                () -> new CotizacionPresupuestoAdapter(null)
            );

            assertThat(excepcion).isNotNull();
        }

        @Test
        @DisplayName("Debería aceptar cotización con detalles vacíos")
        void constructor_deberiaAceptarCotizacionConDetallesVacios() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            Cotizacion cotizacionVacia = new Cotizacion();

            // Act & Assert - No debe lanzar excepción
            CotizacionPresupuestoAdapter adapterVacio = new CotizacionPresupuestoAdapter(cotizacionVacia);

            assertThat(adapterVacio).isNotNull();
        }
    }

    // ==================== TESTS DE getDescripcionArticulo ====================

    @Nested
    @DisplayName("Obtención de Descripciones de Artículos")
    class DescripcionArticuloTest {

        @BeforeEach
        void setUp() throws PresupuestoNoCargadoExcepcion {
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);
        }

        @Test
        @DisplayName("Debería retornar descripción correcta para componente existente")
        void getDescripcionArticulo_deberiaRetornarDescripcionCorrecta() throws PresupuestoNoCargadoExcepcion {
            // Act
            String descripcion = adapter.getDescripcionArticulo(TestUtils.DEFAULT_COMPONENTE_ID);

            // Assert
            assertThat(descripcion).isNotNull();
            assertThat(descripcion).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
        }

        @Test
        @DisplayName("Debería retornar mensaje por defecto para componente no existente")
        void getDescripcionArticulo_deberiaRetornarMensajePorDefecto() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            String componenteInexistente = "COMP-999";

            // Act
            String descripcion = adapter.getDescripcionArticulo(componenteInexistente);

            // Assert
            assertThat(descripcion).isNotNull();
            assertThat(descripcion).isEqualTo("Descripción no encontrada");
        }

        @Test
        @DisplayName("Debería manejar IDs null sin excepción")
        void getDescripcionArticulo_deberiaManejarIdNull() throws PresupuestoNoCargadoExcepcion {
            // Act
            String descripcion = adapter.getDescripcionArticulo(null);

            // Assert
            assertThat(descripcion).isEqualTo("Descripción no encontrada");
        }

        @Test
        @DisplayName("Debería manejar IDs vacíos sin excepción")
        void getDescripcionArticulo_deberiaManejarIdVacio() throws PresupuestoNoCargadoExcepcion {
            // Act
            String descripcion = adapter.getDescripcionArticulo("");

            // Assert
            assertThat(descripcion).isEqualTo("Descripción no encontrada");
        }

        @Test
        @DisplayName("Debería buscar correctamente en cotización con múltiples detalles")
        void getDescripcionArticulo_deberiaBuscarEnMultiplesDetalles() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            Cotizacion cotizacionMultiple = TestUtils.crearCotizacionConMultiplesDetalles();
            CotizacionPresupuestoAdapter adapterMultiple = new CotizacionPresupuestoAdapter(cotizacionMultiple);

            // Act
            String descripcionMonitor = adapterMultiple.getDescripcionArticulo("COMP-001");
            String descripcionTeclado = adapterMultiple.getDescripcionArticulo("COMP-002");
            String descripcionMouse = adapterMultiple.getDescripcionArticulo("COMP-003");

            // Assert
            assertThat(descripcionMonitor).isEqualTo("Monitor");
            assertThat(descripcionTeclado).isEqualTo("Teclado");
            assertThat(descripcionMouse).isEqualTo("Mouse");
        }
    }

    // ==================== TESTS DE getCantidadesXIdArticulo ====================

    @Nested
    @DisplayName("Obtención de Cantidades por ID de Artículo")
    class CantidadesXIdArticuloTest {

        @BeforeEach
        void setUp() throws PresupuestoNoCargadoExcepcion {
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);
        }

        @Test
        @DisplayName("Debería retornar mapa de cantidades correctamente")
        void getCantidadesXIdArticulo_deberiaRetornarMapaCorrecto() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Integer> cantidades = adapter.getCantidadesXIdArticulo();

            // Assert
            assertThat(cantidades).isNotNull();
            assertThat(cantidades).hasSize(1);
            assertThat(cantidades).containsKey(TestUtils.DEFAULT_COMPONENTE_ID);
            assertThat(cantidades.get(TestUtils.DEFAULT_COMPONENTE_ID)).isEqualTo(TestUtils.DEFAULT_CANTIDAD);
        }

        @Test
        @DisplayName("Debería retornar mapa vacío para cotización sin detalles")
        void getCantidadesXIdArticulo_deberiaRetornarMapaVacio() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            Cotizacion cotizacionVacia = new Cotizacion();
            CotizacionPresupuestoAdapter adapterVacio = new CotizacionPresupuestoAdapter(cotizacionVacia);

            // Act
            Map<String, Integer> cantidades = adapterVacio.getCantidadesXIdArticulo();

            // Assert
            assertThat(cantidades).isNotNull();
            assertThat(cantidades).isEmpty();
        }

        @Test
        @DisplayName("Debería manejar múltiples componentes correctamente")
        void getCantidadesXIdArticulo_deberiaManejarMultiplesComponentes() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            Cotizacion cotizacionMultiple = TestUtils.crearCotizacionConMultiplesDetalles();
            CotizacionPresupuestoAdapter adapterMultiple = new CotizacionPresupuestoAdapter(cotizacionMultiple);

            // Act
            Map<String, Integer> cantidades = adapterMultiple.getCantidadesXIdArticulo();

            // Assert
            assertThat(cantidades).hasSize(3);
            assertThat(cantidades.get("COMP-001")).isEqualTo(1);  // Monitor
            assertThat(cantidades.get("COMP-002")).isEqualTo(2);  // Teclado x2
            assertThat(cantidades.get("COMP-003")).isEqualTo(1);  // Mouse
        }

        @Test
        @DisplayName("Debería retornar nueva instancia del mapa cada vez")
        void getCantidadesXIdArticulo_deberiaRetornarNuevaInstancia() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Integer> cantidades1 = adapter.getCantidadesXIdArticulo();
            Map<String, Integer> cantidades2 = adapter.getCantidadesXIdArticulo();

            // Assert
            assertThat(cantidades1).isNotSameAs(cantidades2);
            assertThat(cantidades1).isEqualTo(cantidades2);
        }

        @Test
        @DisplayName("Debería manejar cantidades grandes correctamente")
        void getCantidadesXIdArticulo_deberiaManejarCantidadesGrandes() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            DetalleCotizacion detalleCantidadGrande = TestUtils.crearDetalleCotizacion(
                1, "COMP-HIGH", "Componente Alta Cantidad", 1000, "1.00", "1000.00"
            );
            Cotizacion cotizacionCantidadGrande = new Cotizacion();
            cotizacionCantidadGrande.agregarDetalle(detalleCantidadGrande);
            CotizacionPresupuestoAdapter adapterCantidadGrande =
                new CotizacionPresupuestoAdapter(cotizacionCantidadGrande);

            // Act
            Map<String, Integer> cantidades = adapterCantidadGrande.getCantidadesXIdArticulo();

            // Assert
            assertThat(cantidades.get("COMP-HIGH")).isEqualTo(1000);
        }
    }

    // ==================== TESTS DE getDatosArticulo ====================

    @Nested
    @DisplayName("Obtención de Datos Completos de Artículos")
    class DatosArticuloTest {

        @BeforeEach
        void setUp() throws PresupuestoNoCargadoExcepcion {
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);
        }

        @Test
        @DisplayName("Debería retornar datos completos para componente existente")
        void getDatosArticulo_deberiaRetornarDatosCompletos() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Object> datos = adapter.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);

            // Assert
            assertThat(datos).isNotNull();
            assertThat(datos).hasSize(4);
            assertThat(datos).containsKey("descripcion");
            assertThat(datos).containsKey("cantidad");
            assertThat(datos).containsKey("precioBase");
            assertThat(datos).containsKey("importeTotalLinea");

            // Verificar valores específicos
            assertThat(datos.get("descripcion")).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
            assertThat(datos.get("cantidad")).isEqualTo(TestUtils.DEFAULT_CANTIDAD);
            assertThat(datos.get("precioBase")).isEqualTo(TestUtils.DEFAULT_PRECIO_BASE);
            assertThat(datos.get("importeTotalLinea")).isEqualTo(TestUtils.DEFAULT_TOTAL_COTIZADO);
        }

        @Test
        @DisplayName("Debería retornar mapa vacío para componente no existente")
        void getDatosArticulo_deberiaRetornarMapaVacio() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            String componenteInexistente = "COMP-999";

            // Act
            Map<String, Object> datos = adapter.getDatosArticulo(componenteInexistente);

            // Assert
            assertThat(datos).isNotNull();
            assertThat(datos).isEmpty();
        }

        @Test
        @DisplayName("Debería manejar ID null retornando mapa vacío")
        void getDatosArticulo_deberiaManejarIdNull() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Object> datos = adapter.getDatosArticulo(null);

            // Assert
            assertThat(datos).isNotNull();
            assertThat(datos).isEmpty();
        }

        @Test
        @DisplayName("Debería manejar ID vacío retornando mapa vacío")
        void getDatosArticulo_deberiaManejarIdVacio() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Object> datos = adapter.getDatosArticulo("");

            // Assert
            assertThat(datos).isNotNull();
            assertThat(datos).isEmpty();
        }

        @Test
        @DisplayName("Debería preservar tipos de datos correctos")
        void getDatosArticulo_deberiaPreservarTiposDeDatos() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Object> datos = adapter.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);

            // Assert
            assertThat(datos.get("descripcion")).isInstanceOf(String.class);
            assertThat(datos.get("cantidad")).isInstanceOf(Integer.class);
            assertThat(datos.get("precioBase")).isInstanceOf(BigDecimal.class);
            assertThat(datos.get("importeTotalLinea")).isInstanceOf(BigDecimal.class);
        }

        @Test
        @DisplayName("Debería retornar nueva instancia del mapa cada vez")
        void getDatosArticulo_deberiaRetornarNuevaInstancia() throws PresupuestoNoCargadoExcepcion {
            // Act
            Map<String, Object> datos1 = adapter.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);
            Map<String, Object> datos2 = adapter.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);

            // Assert
            assertThat(datos1).isNotSameAs(datos2);
            assertThat(datos1).isEqualTo(datos2);
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Casos Edge y Límites")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar cotización con valores límite")
        void deberiaManejarCotizacionConValoresLimite() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            Cotizacion cotizacionLimite = TestUtils.crearCotizacionConValoresLimite();
            CotizacionPresupuestoAdapter adapterLimite = new CotizacionPresupuestoAdapter(cotizacionLimite);

            // Act
            String descripcion = adapterLimite.getDescripcionArticulo("COMP-EDGE-CASE-12345");
            Map<String, Integer> cantidades = adapterLimite.getCantidadesXIdArticulo();
            Map<String, Object> datos = adapterLimite.getDatosArticulo("COMP-EDGE-CASE-12345");

            // Assert
            assertThat(descripcion).contains("Descripción muy larga");
            assertThat(cantidades.get("COMP-EDGE-CASE-12345")).isEqualTo(1000);
            assertThat(datos.get("cantidad")).isEqualTo(1000);
            assertThat((BigDecimal) datos.get("precioBase"))
                .isEqualByComparingTo(new BigDecimal("999999.99"));
        }

        @Test
        @DisplayName("Debería funcionar con IDs muy largos")
        void deberiaFuncionarConIdsLargos() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            String idMuyLargo = "COMP-" + "X".repeat(100);
            DetalleCotizacion detalleIdLargo = TestUtils.crearDetalleCotizacion(
                1, idMuyLargo, "Componente ID Largo", 1, "100.00", "100.00"
            );
            Cotizacion cotizacionIdLargo = new Cotizacion();
            cotizacionIdLargo.agregarDetalle(detalleIdLargo);
            CotizacionPresupuestoAdapter adapterIdLargo = new CotizacionPresupuestoAdapter(cotizacionIdLargo);

            // Act
            String descripcion = adapterIdLargo.getDescripcionArticulo(idMuyLargo);
            Map<String, Integer> cantidades = adapterIdLargo.getCantidadesXIdArticulo();

            // Assert
            assertThat(descripcion).isEqualTo("Componente ID Largo");
            assertThat(cantidades).containsKey(idMuyLargo);
            assertThat(cantidades.get(idMuyLargo)).isEqualTo(1);
        }

        @Test
        @DisplayName("Debería manejar cotización con muchos detalles")
        void deberiaManejarCotizacionConMuchosDetalles() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            Cotizacion cotizacionMuchosDetalles = new Cotizacion();
            for (int i = 1; i <= 100; i++) {
                DetalleCotizacion detalle = TestUtils.crearDetalleCotizacion(
                    i, "COMP-" + i, "Componente " + i, 1, "10.00", "10.00"
                );
                cotizacionMuchosDetalles.agregarDetalle(detalle);
            }
            CotizacionPresupuestoAdapter adapterMuchosDetalles =
                new CotizacionPresupuestoAdapter(cotizacionMuchosDetalles);

            // Act
            Map<String, Integer> cantidades = adapterMuchosDetalles.getCantidadesXIdArticulo();
            String descripcionPrimero = adapterMuchosDetalles.getDescripcionArticulo("COMP-1");
            String descripcionUltimo = adapterMuchosDetalles.getDescripcionArticulo("COMP-100");

            // Assert
            assertThat(cantidades).hasSize(100);
            assertThat(descripcionPrimero).isEqualTo("Componente 1");
            assertThat(descripcionUltimo).isEqualTo("Componente 100");
        }
    }

    // ==================== TESTS DE COMPORTAMIENTO DEL PATRÓN ADAPTER ====================

    @Nested
    @DisplayName("Verificación del Patrón Adapter")
    class PatronAdapterTest {

        @Test
        @DisplayName("Debería implementar correctamente la interfaz IPresupuesto")
        void deberiaImplementarInterfazIPresupuesto() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);

            // Act & Assert
            assertThat(adapter).isInstanceOf(IPresupuesto.class);

            // Verificar que todos los métodos de la interfaz están implementados
            IPresupuesto presupuesto = adapter;
            assertThat(presupuesto.getDescripcionArticulo(TestUtils.DEFAULT_COMPONENTE_ID)).isNotNull();
            assertThat(presupuesto.getCantidadesXIdArticulo()).isNotNull();
            assertThat(presupuesto.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID)).isNotNull();
        }

        @Test
        @DisplayName("Debería adaptar correctamente Cotizacion a IPresupuesto")
        void deberiaAdaptarCotizacionAIPresupuesto() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);

            // Act - Usar como IPresupuesto
            IPresupuesto presupuesto = adapter;
            String descripcion = presupuesto.getDescripcionArticulo(TestUtils.DEFAULT_COMPONENTE_ID);
            Map<String, Integer> cantidades = presupuesto.getCantidadesXIdArticulo();
            Map<String, Object> datos = presupuesto.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);

            // Assert - Verificar que la adaptación preserva los datos originales
            assertThat(descripcion).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
            assertThat(cantidades.get(TestUtils.DEFAULT_COMPONENTE_ID)).isEqualTo(TestUtils.DEFAULT_CANTIDAD);
            assertThat(datos.get("descripcion")).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
        }

        @Test
        @DisplayName("Debería mantener consistencia entre diferentes métodos de acceso")
        void deberiaMantenerConsistenciaEntreMetodos() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            adapter = new CotizacionPresupuestoAdapter(cotizacionValida);

            // Act
            String descripcionDirecta = adapter.getDescripcionArticulo(TestUtils.DEFAULT_COMPONENTE_ID);
            Map<String, Object> datosCompletos = adapter.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);
            String descripcionDesdeDatos = (String) datosCompletos.get("descripcion");

            Map<String, Integer> cantidades = adapter.getCantidadesXIdArticulo();
            Integer cantidadDesdeCantidades = cantidades.get(TestUtils.DEFAULT_COMPONENTE_ID);
            Integer cantidadDesdeDatos = (Integer) datosCompletos.get("cantidad");

            // Assert - Mismos datos por diferentes vías
            assertThat(descripcionDirecta).isEqualTo(descripcionDesdeDatos);
            assertThat(cantidadDesdeCantidades).isEqualTo(cantidadDesdeDatos);
        }
    }
}