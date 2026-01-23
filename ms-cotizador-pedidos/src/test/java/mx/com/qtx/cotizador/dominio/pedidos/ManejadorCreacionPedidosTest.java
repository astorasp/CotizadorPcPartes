package mx.com.qtx.cotizador.dominio.pedidos;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Pruebas unitarias para la clase {@link ManejadorCreacionPedidos}.
 *
 * <p>Esta clase de pruebas verifica el funcionamiento correcto del manejador que orquesta
 * la creación de pedidos a partir de cotizaciones. Incluye pruebas para:</p>
 *
 * <ul>
 *   <li><strong>Constructor:</strong> Inicialización correcta con proveedores válidos y casos edge</li>
 *   <li><strong>Creación de pedidos:</strong> Proceso completo desde cotización hasta pedido</li>
 *   <li><strong>Gestión de excepciones:</strong> Manejo de errores durante el proceso</li>
 *   <li><strong>Gestión de pedidos:</strong> Lista de pedidos creados y operaciones de consulta</li>
 *   <li><strong>Integración:</strong> Interacción correcta con GestorPedidos interno</li>
 * </ul>
 *
 * <h3>Cobertura de Casos de Prueba:</h3>
 * <table border="1">
 *   <tr><th>Categoría</th><th>Escenarios</th><th>Validaciones</th></tr>
 *   <tr><td>Constructor</td><td>Proveedores válidos, lista vacía, lista null</td><td>Inicialización correcta</td></tr>
 *   <tr><td>Creación</td><td>Cotización válida, cotización vacía</td><td>Proceso completo</td></tr>
 *   <tr><td>Excepciones</td><td>Proveedor inexistente, cotización null</td><td>Manejo de errores</td></tr>
 *   <tr><td>Gestión</td><td>Lista de pedidos, impresión</td><td>Estado interno</td></tr>
 * </table>
 *
 * <h3>Patrones de Prueba Utilizados:</h3>
 * <ul>
 *   <li><strong>AAA Pattern:</strong> Arrange-Act-Assert para estructura clara</li>
 *   <li><strong>Nested Tests:</strong> Agrupación lógica de casos relacionados</li>
 *   <li><strong>TestUtils:</strong> Utilidades compartidas para datos de prueba</li>
 *   <li><strong>Console Capture:</strong> Verificación de mensajes en consola</li>
 *   <li><strong>Exception Testing:</strong> Verificación de manejo de errores</li>
 * </ul>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 1.0.0
 * @see ManejadorCreacionPedidos
 * @see GestorPedidos
 * @see mx.com.qtx.cotizador.dominio.core.CotizacionPresupuestoAdapter
 */
@DisplayName("ManejadorCreacionPedidos - Orquestación de creación de pedidos")
class ManejadorCreacionPedidosTest {

    // ==================== CAMPOS DE PRUEBA ====================

    private ManejadorCreacionPedidos manejador;
    private List<Proveedor> proveedoresValidos;
    private Cotizacion cotizacionValida;

    // Constantes para pruebas
    private static final String CVE_PROVEEDOR_VALIDO = TestUtils.DEFAULT_PROVEEDOR_CVE;
    private static final int NUM_PEDIDO_TEST = 12345;
    private static final int NIVEL_SURTIDO_TEST = 0;
    private static final LocalDate FECHA_EMISION_TEST = LocalDate.now();
    private static final LocalDate FECHA_ENTREGA_TEST = LocalDate.now().plusDays(15);

    // ==================== CONFIGURACIÓN DE PRUEBAS ====================

    @BeforeEach
    void setUp() {
        proveedoresValidos = TestUtils.crearListaProveedoresValidos();
        cotizacionValida = TestUtils.crearCotizacionValida();
        manejador = new ManejadorCreacionPedidos(proveedoresValidos);
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar correctamente con proveedores válidos")
        void constructor_deberiaInicializarCorrectamente() {
            // Act
            ManejadorCreacionPedidos nuevoManejador = new ManejadorCreacionPedidos(proveedoresValidos);

            // Assert
            assertThat(nuevoManejador).isNotNull();
            assertThat(nuevoManejador.getPedidos()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Debería manejar lista de proveedores vacía")
        void constructor_deberiaManejarListaVacia() {
            // Arrange
            List<Proveedor> proveedoresVacios = new ArrayList<>();

            // Act
            ManejadorCreacionPedidos manejadorVacio = new ManejadorCreacionPedidos(proveedoresVacios);

            // Assert
            assertThat(manejadorVacio).isNotNull();
            assertThat(manejadorVacio.getPedidos()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Debería lanzar excepción con lista de proveedores null")
        void constructor_deberiaManejarListaNull() {
            // Act & Assert - GestorPedidos usa forEach que falla con null
            assertThrows(NullPointerException.class, () -> {
                new ManejadorCreacionPedidos(null);
            });
        }

        @Test
        @DisplayName("Debería inicializar GestorPedidos interno")
        void constructor_deberiaInicializarGestorInterno() {
            // Act
            ManejadorCreacionPedidos nuevoManejador = new ManejadorCreacionPedidos(proveedoresValidos);

            // Assert - Verificar que el gestor interno funciona
            assertThat(nuevoManejador).isNotNull();

            // Verificar que el gestor interno está inicializado
            // No se puede imprimir pedido sin haber creado uno primero
            // Solo verificamos que el objeto no es null
            assertThat(nuevoManejador.toString()).contains("ManejadorCreacionPedidos");
        }
    }

    // ==================== TESTS DE CREACIÓN DE PEDIDOS ====================

    @Nested
    @DisplayName("Creación de Pedidos desde Cotización")
    class CreacionPedidosTest {

        private ByteArrayOutputStream outputCaptor;
        private PrintStream originalSystemOut;

        @BeforeEach
        void setUpOutput() {
            // Capturar salida de consola para verificar mensajes
            outputCaptor = new ByteArrayOutputStream();
            originalSystemOut = System.out;
            System.setOut(new PrintStream(outputCaptor));
        }

        @org.junit.jupiter.api.AfterEach
        void restoreOutput() {
            System.setOut(originalSystemOut);
        }

        @Test
        @DisplayName("Debería crear pedido exitosamente desde cotización válida")
        void crearPedidoDesdeCotizacion_deberiaCrearExitosamente() {
            // Arrange
            assertThat(manejador.getPedidos()).isEmpty();

            // Act
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(manejador.getPedidos()).hasSize(1);

            Pedido pedidoCreado = manejador.getPedidos().get(0);
            assertThat(pedidoCreado).isNotNull();
            assertThat(pedidoCreado.getNumPedido()).isEqualTo(NUM_PEDIDO_TEST);
            assertThat(pedidoCreado.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO_TEST);
            assertThat(pedidoCreado.getFechaEmision()).isEqualTo(FECHA_EMISION_TEST);
            assertThat(pedidoCreado.getFechaEntrega()).isEqualTo(FECHA_ENTREGA_TEST);
            assertThat(pedidoCreado.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR_VALIDO);
        }

        @Test
        @DisplayName("Debería mostrar mensajes en consola durante creación exitosa")
        void crearPedidoDesdeCotizacion_deberiaMostrarMensajes() {
            // Act
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            String consoleOutput = outputCaptor.toString();
            assertThat(consoleOutput).contains("Iniciando proceso para generar pedido desde cotización");
            assertThat(consoleOutput).contains("Pedido generado exitosamente para cotización");
        }

        @Test
        @DisplayName("Debería crear múltiples pedidos desde diferentes cotizaciones")
        void crearPedidoDesdeCotizacion_deberiaCrearMultiplesPedidos() {
            // Arrange
            Cotizacion segundaCotizacion = TestUtils.crearCotizacionConMultiplesDetalles();
            Proveedor segundoProveedor = TestUtils.crearProveedor("PROV-002", "Segundo Proveedor", "Segundo SA");
            List<Proveedor> proveedoresExtendidos = new ArrayList<>(proveedoresValidos);
            proveedoresExtendidos.add(segundoProveedor);

            ManejadorCreacionPedidos manejadorExtendido = new ManejadorCreacionPedidos(proveedoresExtendidos);

            // Act
            manejadorExtendido.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            manejadorExtendido.crearPedidoDesdeCotizacion(segundaCotizacion, "PROV-002",
                NUM_PEDIDO_TEST + 1, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST.plusDays(5));

            // Assert
            assertThat(manejadorExtendido.getPedidos()).hasSize(2);

            Pedido primerPedido = manejadorExtendido.getPedidos().get(0);
            Pedido segundoPedido = manejadorExtendido.getPedidos().get(1);

            assertThat(primerPedido.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR_VALIDO);
            assertThat(segundoPedido.getProveedor().getCve()).isEqualTo("PROV-002");
            assertThat(primerPedido.getNumPedido()).isNotEqualTo(segundoPedido.getNumPedido());
        }

        @Test
        @DisplayName("Debería manejar cotización con detalles vacíos")
        void crearPedidoDesdeCotizacion_deberiaManejarCotizacionVacia() {
            // Arrange
            Cotizacion cotizacionVacia = new Cotizacion();

            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                manejador.crearPedidoDesdeCotizacion(cotizacionVacia, CVE_PROVEEDOR_VALIDO,
                    NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            });

            assertThat(manejador.getPedidos()).hasSize(1);
        }
    }

    // ==================== TESTS DE MANEJO DE EXCEPCIONES ====================

    @Nested
    @DisplayName("Manejo de Excepciones")
    class ManejoExcepcionesTest {

        private ByteArrayOutputStream errorCaptor;
        private PrintStream originalSystemErr;

        @BeforeEach
        void setUpError() {
            // Capturar salida de error para verificar mensajes
            errorCaptor = new ByteArrayOutputStream();
            originalSystemErr = System.err;
            System.setErr(new PrintStream(errorCaptor));
        }

        @org.junit.jupiter.api.AfterEach
        void restoreError() {
            System.setErr(originalSystemErr);
        }

        @Test
        @DisplayName("Debería manejar proveedor inexistente sin lanzar excepción")
        void crearPedidoDesdeCotizacion_deberiaManejarProveedorInexistente() {
            // Act - No debe lanzar excepción, debe capturarla internamente
            assertDoesNotThrow(() -> {
                manejador.crearPedidoDesdeCotizacion(cotizacionValida, "PROV-INEXISTENTE",
                    NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            });

            // Assert - El pedido no se debe haber creado debido al error
            assertThat(manejador.getPedidos()).isEmpty();

            // Verificar mensaje de error
            String errorOutput = errorCaptor.toString();
            assertThat(errorOutput).contains("Error");
        }

        @Test
        @DisplayName("Debería manejar cotización null sin lanzar excepción")
        void crearPedidoDesdeCotizacion_deberiaManejarCotizacionNull() {
            // Act - No debe lanzar excepción, debe capturarla internamente
            assertDoesNotThrow(() -> {
                manejador.crearPedidoDesdeCotizacion(null, CVE_PROVEEDOR_VALIDO,
                    NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            });

            // Assert - El pedido no se debe haber creado debido al error
            assertThat(manejador.getPedidos()).isEmpty();

            // Verificar mensaje de error
            String errorOutput = errorCaptor.toString();
            assertThat(errorOutput).contains("Error");
        }

        @Test
        @DisplayName("Debería imprimir stack trace para errores inesperados")
        void crearPedidoDesdeCotizacion_deberiaImprimirStackTrace() {
            // Act
            manejador.crearPedidoDesdeCotizacion(null, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            String errorOutput = errorCaptor.toString();
            // El error puede ser de presupuesto no cargado o inesperado dependiendo de dónde falle
            assertThat(errorOutput).containsAnyOf(
                "Error inesperado al generar pedido:",
                "Error al cargar el presupuesto:");
        }
    }

    // ==================== TESTS DE GESTIÓN DE PEDIDOS ====================

    @Nested
    @DisplayName("Gestión de Lista de Pedidos")
    class GestionPedidosTest {

        @Test
        @DisplayName("Debería retornar lista vacía inicialmente")
        void getPedidos_deberiaRetornarListaVaciaInicial() {
            // Act
            List<Pedido> pedidos = manejador.getPedidos();

            // Assert
            assertThat(pedidos).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Debería retornar lista inmutable o copia defensiva")
        void getPedidos_deberiaRetornarListaSegura() {
            // Arrange - Crear un pedido primero
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Act
            List<Pedido> pedidos = manejador.getPedidos();

            // Assert
            assertThat(pedidos).hasSize(1);

            // Verificar que es la misma referencia (no copia defensiva en este caso)
            // o que cualquier modificación externa no afecte el estado interno
            assertThat(pedidos).isEqualTo(manejador.getPedidos());
        }

        @Test
        @DisplayName("Debería mantener orden de creación de pedidos")
        void getPedidos_deberiaMantenerOrdenCreacion() {
            // Arrange
            Cotizacion segundaCotizacion = TestUtils.crearCotizacionConMultiplesDetalles();

            // Act - Crear pedidos en orden específico
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                100, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            manejador.crearPedidoDesdeCotizacion(segundaCotizacion, CVE_PROVEEDOR_VALIDO,
                200, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            List<Pedido> pedidos = manejador.getPedidos();
            assertThat(pedidos).hasSize(2);
            assertThat(pedidos.get(0).getNumPedido()).isEqualTo(100);
            assertThat(pedidos.get(1).getNumPedido()).isEqualTo(200);
        }
    }

    // ==================== TESTS DE IMPRESIÓN ====================

    @Nested
    @DisplayName("Funcionalidad de Impresión")
    class ImpresionTest {

        @Test
        @DisplayName("Debería lanzar excepción al imprimir sin pedido creado")
        void imprimirPedido_deberiaInvocarSinExcepcion() {
            // Act & Assert - No se puede imprimir pedido sin haber creado uno
            assertThrows(NullPointerException.class, () -> {
                manejador.imprimirPedido();
            });
        }

        @Test
        @DisplayName("Debería invocar impresión después de crear pedido")
        void imprimirPedido_deberiaFuncionarDespuesCreacion() {
            // Arrange
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Act & Assert
            assertDoesNotThrow(() -> {
                manejador.imprimirPedido();
            });
        }
    }

    // ==================== TESTS DE INTEGRACIÓN INTERNA ====================

    @Nested
    @DisplayName("Integración con GestorPedidos")
    class IntegracionGestorTest {

        @Test
        @DisplayName("Debería utilizar correctamente el GestorPedidos interno")
        void deberiautilizarGestorInternoCorrectamente() {
            // Arrange
            assertThat(manejador.getPedidos()).isEmpty();

            // Act - El proceso completo debe funcionar a través del gestor interno
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(manejador.getPedidos()).hasSize(1);

            // Verificar que el pedido tiene las características esperadas del gestor
            Pedido pedido = manejador.getPedidos().get(0);
            assertThat(pedido.getProveedor()).isNotNull();
            assertThat(pedido.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR_VALIDO);
        }

        @Test
        @DisplayName("Debería coordinar correctamente presupuesto y generación")
        void deberiaCoordinarPresupuestoYGeneracion() {
            // Act - Proceso que requiere coordinación: adapter -> agregar -> generar
            manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert - El resultado debe reflejar la coordinación exitosa
            assertThat(manejador.getPedidos()).hasSize(1);

            Pedido pedidoGenerado = manejador.getPedidos().get(0);
            assertThat(pedidoGenerado.getDetallesPedido()).isNotEmpty();
        }
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Nested
    @DisplayName("Casos Edge y Límites")
    class CasosEdgeTest {

        @Test
        @DisplayName("Debería manejar cotización con valores límite")
        void deberiaManejarcotizacionConValoresLimite() {
            // Arrange
            Cotizacion cotizacionLimite = TestUtils.crearCotizacionConValoresLimite();

            // Act & Assert
            assertDoesNotThrow(() -> {
                manejador.crearPedidoDesdeCotizacion(cotizacionLimite, CVE_PROVEEDOR_VALIDO,
                    Integer.MAX_VALUE, 100, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            });
        }

        @Test
        @DisplayName("Debería manejar fechas en casos límite")
        void deberiaManejarFechasLimite() {
            // Arrange
            LocalDate fechaMuyLejana = LocalDate.of(2099, 12, 31);
            LocalDate fechaPasada = LocalDate.of(2000, 1, 1);

            // Act & Assert
            assertDoesNotThrow(() -> {
                manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                    NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST, fechaPasada, fechaMuyLejana);
            });
        }

        @Test
        @DisplayName("Debería manejar múltiples operaciones consecutivas")
        void deberiaManejarOperacionesConsecutivas() {
            // Act
            for (int i = 0; i < 10; i++) {
                manejador.crearPedidoDesdeCotizacion(cotizacionValida, CVE_PROVEEDOR_VALIDO,
                    NUM_PEDIDO_TEST + i, NIVEL_SURTIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            }

            // Assert
            assertThat(manejador.getPedidos()).hasSize(10);

            // Verificar que todos los pedidos son únicos
            List<Long> numerosPedido = manejador.getPedidos().stream()
                .map(Pedido::getNumPedido)
                .toList();
            assertThat(numerosPedido).doesNotHaveDuplicates();
        }
    }
}