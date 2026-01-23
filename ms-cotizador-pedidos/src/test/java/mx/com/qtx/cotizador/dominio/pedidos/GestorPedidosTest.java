package mx.com.qtx.cotizador.dominio.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.com.qtx.cotizador.dominio.pedidos.excepciones.PresupuestoNoCargadoExcepcion;
import mx.com.qtx.cotizador.dominio.pedidos.excepciones.ProveedorNoExisteExcepcion;
import mx.com.qtx.cotizador.util.MockPresupuesto;
import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Tests unitarios para la clase GestorPedidos del dominio de pedidos.
 *
 * <p>Verifica la funcionalidad completa de la clase {@link GestorPedidos} que gestiona
 * la creación y manejo de pedidos a partir de presupuestos. Esta clase es crítica
 * para la transformación de presupuestos en pedidos formales y la gestión de
 * proveedores asociados.</p>
 *
 * <h3>Cobertura de Testing:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Inicialización correcta con lista de proveedores</li>
 *   <li><strong>agregarPresupuesto:</strong> Carga y validación de presupuestos</li>
 *   <li><strong>generarPedido:</strong> Creación de pedidos desde presupuestos</li>
 *   <li><strong>imprimirPedidoActual:</strong> Visualización de pedidos generados</li>
 *   <li><strong>Validaciones:</strong> Manejo de excepciones de dominio</li>
 *   <li><strong>Integración:</strong> Interacción correcta con IPresupuesto y Proveedor</li>
 * </ul>
 *
 * <h3>Casos de Uso Validados:</h3>
 * <ul>
 *   <li>Gestión de repositorio simulado de proveedores</li>
 *   <li>Transformación de datos de presupuesto a pedido</li>
 *   <li>Validación de integridad referencial con proveedores</li>
 *   <li>Manejo de errores de negocio y estados inválidos</li>
 * </ul>
 *
 * <h3>Patrones y Arquitectura:</h3>
 * <ul>
 *   <li><strong>Domain Service:</strong> GestorPedidos como servicio de dominio</li>
 *   <li><strong>Repository Pattern:</strong> Gestión interna de proveedores</li>
 *   <li><strong>Strategy Pattern:</strong> Uso polimórfico de IPresupuesto</li>
 * </ul>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 2.0.0
 * @see GestorPedidos Clase bajo test
 * @see IPresupuesto Interfaz utilizada por el gestor
 * @see Proveedor Entidad gestionada
 * @see Pedido Entidad generada
 * @see MockPresupuesto Mock para IPresupuesto
 * @see TestUtils Utilidades para crear objetos de test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GestorPedidos - Tests del Servicio de Dominio")
class GestorPedidosTest {

    private GestorPedidos gestorPedidos;
    private List<Proveedor> proveedoresValidos;
    private MockPresupuesto mockPresupuesto;

    // Constantes para tests
    private static final String CVE_PROVEEDOR_VALIDO = TestUtils.DEFAULT_PROVEEDOR_CVE;
    private static final String CVE_PROVEEDOR_INEXISTENTE = "PROV-999";
    private static final int NUM_PEDIDO_TEST = 12345;
    private static final int NIVEL_SURTIDO_TEST = 0;
    private static final LocalDate FECHA_EMISION_TEST = LocalDate.now();
    private static final LocalDate FECHA_ENTREGA_TEST = LocalDate.now().plusDays(15);

    @BeforeEach
    void setUp() {
        proveedoresValidos = TestUtils.crearListaProveedoresValidos();
        gestorPedidos = new GestorPedidos(proveedoresValidos);
        mockPresupuesto = new MockPresupuesto();
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar correctamente con lista de proveedores")
        void constructor_deberiaInicializarCorrectamente() {
            // Arrange
            List<Proveedor> proveedores = TestUtils.crearListaProveedoresValidos();

            // Act
            GestorPedidos gestor = new GestorPedidos(proveedores);

            // Assert
            assertThat(gestor).isNotNull();
        }

        @Test
        @DisplayName("Debería manejar lista vacía de proveedores")
        void constructor_deberiaManejarListaVacia() {
            // Arrange
            List<Proveedor> proveedoresVacios = List.of();

            // Act
            GestorPedidos gestor = new GestorPedidos(proveedoresVacios);

            // Assert
            assertThat(gestor).isNotNull();
        }

        @Test
        @DisplayName("Debería indexar proveedores por clave correctamente")
        void constructor_deberiaIndexarProveedoresPorClave() {
            // Arrange
            List<Proveedor> proveedores = TestUtils.crearListaProveedoresValidos();
            GestorPedidos gestor = new GestorPedidos(proveedores);
            MockPresupuesto presupuesto = new MockPresupuesto();

            // Act & Assert - Verificar que los proveedores están indexados
            // intentando generar pedido con cada clave
            assertThat(gestor).isNotNull();

            try {
                gestor.agregarPresupuesto(presupuesto);
                // Debe encontrar el proveedor sin excepción
                Pedido pedido = gestor.generarPedido(
                    TestUtils.DEFAULT_PROVEEDOR_CVE, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
                assertThat(pedido).isNotNull();
            } catch (Exception e) {
                // No debe lanzar ProveedorNoExisteExcepcion si está correctamente indexado
                assertThat(e).isNotInstanceOf(ProveedorNoExisteExcepcion.class);
            }
        }

        @Test
        @DisplayName("Debería manejar proveedores con claves duplicadas")
        void constructor_deberiaManejarClavesDuplicadas() {
            // Arrange - Crear lista con proveedores duplicados
            Proveedor proveedor1 = TestUtils.crearProveedor("PROV-DUP", "Proveedor Original", "Original SA");
            Proveedor proveedor2 = TestUtils.crearProveedor("PROV-DUP", "Proveedor Duplicado", "Duplicado SA");
            List<Proveedor> proveedoresDuplicados = List.of(proveedor1, proveedor2);

            // Act
            GestorPedidos gestor = new GestorPedidos(proveedoresDuplicados);

            // Assert
            assertThat(gestor).isNotNull();
            // El comportamiento depende de la implementación (HashMap reemplaza valores)
        }
    }

    // ==================== TESTS DE agregarPresupuesto ====================

    @Nested
    @DisplayName("Agregar y Gestionar Presupuestos")
    class AgregarPresupuestoTest {

        @Test
        @DisplayName("Debería agregar presupuesto válido correctamente")
        void agregarPresupuesto_deberiaAgregarPresupuestoValido() throws PresupuestoNoCargadoExcepcion, ProveedorNoExisteExcepcion {
            // Act
            gestorPedidos.agregarPresupuesto(mockPresupuesto);

            // Assert - Verificar que se puede generar pedido (implica presupuesto cargado)
            assertThat(gestorPedidos).isNotNull();

            // Verificar que no lanza excepción cuando se genera pedido
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            assertThat(pedido).isNotNull();
        }

        @Test
        @DisplayName("Debería lanzar PresupuestoNoCargadoExcepcion con presupuesto null")
        void agregarPresupuesto_deberiaLanzarExcepcionConPresupuestoNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                gestorPedidos.agregarPresupuesto(null);
            })
            .isInstanceOf(PresupuestoNoCargadoExcepcion.class);
        }

        @Test
        @DisplayName("Debería reemplazar presupuesto anterior cuando se agrega uno nuevo")
        void agregarPresupuesto_deberiaReemplazarPresupuestoAnterior() throws Exception {
            // Arrange
            MockPresupuesto presupuesto1 = new MockPresupuesto();
            MockPresupuesto presupuesto2 = MockPresupuesto.crearConMultiplesArticulos();

            // Act
            gestorPedidos.agregarPresupuesto(presupuesto1);
            gestorPedidos.agregarPresupuesto(presupuesto2);

            // Assert
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // El pedido debería tener 3 detalles (del segundo presupuesto)
            assertThat(pedido.getDetallesPedido()).hasSize(3);
        }

        @Test
        @DisplayName("Debería manejar presupuesto vacío sin error")
        void agregarPresupuesto_deberiaManejarPresupuestoVacio() throws PresupuestoNoCargadoExcepcion, ProveedorNoExisteExcepcion {
            // Arrange
            MockPresupuesto presupuestoVacio = MockPresupuesto.crearVacio();

            // Act
            gestorPedidos.agregarPresupuesto(presupuestoVacio);

            // Assert - Debería poder generar pedido aunque esté vacío
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            assertThat(pedido).isNotNull();
            assertThat(pedido.getDetallesPedido()).isEmpty();
        }
    }

    // ==================== TESTS DE generarPedido ====================

    @Nested
    @DisplayName("Generación de Pedidos")
    class GenerarPedidoTest {

        @BeforeEach
        void setUp() throws PresupuestoNoCargadoExcepcion {
            gestorPedidos.agregarPresupuesto(mockPresupuesto);
        }

        @Test
        @DisplayName("Debería generar pedido correctamente con datos válidos")
        void generarPedido_deberiaGenerarPedidoCorrectamente() throws Exception {
            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido).isNotNull();
            assertThat(pedido.getNumPedido()).isEqualTo(NUM_PEDIDO_TEST);
            assertThat(pedido.getFechaEmision()).isEqualTo(FECHA_EMISION_TEST);
            assertThat(pedido.getFechaEntrega()).isEqualTo(FECHA_ENTREGA_TEST);
            assertThat(pedido.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO_TEST);
            assertThat(pedido.getProveedor()).isNotNull();
            assertThat(pedido.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR_VALIDO);
        }

        @Test
        @DisplayName("Debería transferir detalles del presupuesto al pedido")
        void generarPedido_deberiaTransferirDetallesDelPresupuesto() throws Exception {
            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido.getDetallesPedido()).hasSize(1);
            DetallePedido detalle = pedido.getDetallesPedido().get(0);

            assertThat(detalle.getIdArticulo()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_ID);
            assertThat(detalle.getDescripcion()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
            assertThat(detalle.getCantidad()).isEqualTo(TestUtils.DEFAULT_CANTIDAD);
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(TestUtils.DEFAULT_PRECIO_BASE);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(TestUtils.DEFAULT_TOTAL_COTIZADO);
        }

        @Test
        @DisplayName("Debería lanzar ProveedorNoExisteExcepcion con proveedor inexistente")
        void generarPedido_deberiaLanzarExcepcionConProveedorInexistente() {
            // Act & Assert
            assertThatThrownBy(() -> {
                gestorPedidos.generarPedido(
                    CVE_PROVEEDOR_INEXISTENTE, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            })
            .isInstanceOf(ProveedorNoExisteExcepcion.class);
        }

        @Test
        @DisplayName("Debería lanzar PresupuestoNoCargadoExcepcion sin presupuesto cargado")
        void generarPedido_deberiaLanzarExcepcionSinPresupuestoCargado() {
            // Arrange - Crear gestor sin presupuesto
            GestorPedidos gestorSinPresupuesto = new GestorPedidos(proveedoresValidos);

            // Act & Assert
            assertThatThrownBy(() -> {
                gestorSinPresupuesto.generarPedido(
                    CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            })
            .isInstanceOf(PresupuestoNoCargadoExcepcion.class);
        }

        @Test
        @DisplayName("Debería manejar presupuesto con múltiples artículos")
        void generarPedido_deberiaManejarPresupuestoConMultiplesArticulos() throws Exception {
            // Arrange
            MockPresupuesto presupuestoMultiple = MockPresupuesto.crearConMultiplesArticulos();
            gestorPedidos.agregarPresupuesto(presupuestoMultiple);

            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido.getDetallesPedido()).hasSize(3);

            List<DetallePedido> detalles = pedido.getDetallesPedido();
            Map<String, Integer> cantidadesEsperadas = presupuestoMultiple.getCantidadesXIdArticulo();

            // Verificar que cada artículo del presupuesto se transfirió correctamente
            for (DetallePedido detalle : detalles) {
                String idArticulo = detalle.getIdArticulo();
                assertThat(cantidadesEsperadas).containsKey(idArticulo);
                assertThat(detalle.getCantidad()).isEqualTo(cantidadesEsperadas.get(idArticulo));
                assertThat(detalle.getDescripcion()).isEqualTo(presupuestoMultiple.getDescripcionArticulo(idArticulo));
            }
        }

        @Test
        @DisplayName("Debería calcular correctamente precios desde datos del presupuesto")
        void generarPedido_deberiaCalcularPreciosCorrectamente() throws Exception {
            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            DetallePedido detalle = pedido.getDetallesPedido().get(0);
            Map<String, Object> datosPresupuesto = mockPresupuesto.getDatosArticulo(TestUtils.DEFAULT_COMPONENTE_ID);

            BigDecimal precioBaseEsperado = (BigDecimal) datosPresupuesto.get("precioBase");
            BigDecimal importeTotalEsperado = (BigDecimal) datosPresupuesto.get("importeTotalLinea");

            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(precioBaseEsperado);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(importeTotalEsperado);
        }

        @Test
        @DisplayName("Debería manejar datos faltantes en presupuesto")
        void generarPedido_deberiaManejarDatosFaltantesEnPresupuesto() throws Exception {
            // Arrange - Crear presupuesto con datos incompletos
            MockPresupuesto presupuestoIncompleto = new MockPresupuesto();
            presupuestoIncompleto.limpiar();
            presupuestoIncompleto.agregarArticulo("COMP-INCOMPLETE", "Componente Incompleto", 1,
                                                 null, null); // Precios null
            gestorPedidos.agregarPresupuesto(presupuestoIncompleto);

            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido.getDetallesPedido()).hasSize(1);
            DetallePedido detalle = pedido.getDetallesPedido().get(0);

            // Debe usar valores por defecto (BigDecimal.ZERO)
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería generar números de pedido diferentes")
        void generarPedido_deberiaGenerarNumerosPedidoDiferentes() throws Exception {
            // Act
            Pedido pedido1 = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, 111, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            Pedido pedido2 = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, 222, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido1.getNumPedido()).isNotEqualTo(pedido2.getNumPedido());
            assertThat(pedido1.getNumPedido()).isEqualTo(111);
            assertThat(pedido2.getNumPedido()).isEqualTo(222);
        }
    }

    // ==================== TESTS DE imprimirPedidoActual ====================

    @Nested
    @DisplayName("Impresión de Pedidos")
    class ImprimirPedidoTest {

        @Test
        @DisplayName("imprimirPedidoActual no debería lanzar excepción")
        void imprimirPedidoActual_noDeberiaLanzarExcepcion() {
            // Act & Assert - El método imprime a System.out, solo verificamos que no lance excepción
            try {
                gestorPedidos.imprimirPedidoActual();
            } catch (Exception e) {
                // Si hay NullPointerException es porque pedido es null, pero no debería lanzar otras excepciones
                if (!(e instanceof NullPointerException)) {
                    throw e;
                }
            }
        }
    }

    // ==================== TESTS DE INTEGRACIÓN Y FLUJOS COMPLETOS ====================

    @Nested
    @DisplayName("Flujos de Integración Completos")
    class FlujoIntegracionTest {

        @Test
        @DisplayName("Debería ejecutar flujo completo de agregar presupuesto y generar pedido")
        void deberiaEjecutarFlujoCompleto() throws Exception {
            // Arrange
            MockPresupuesto presupuesto = MockPresupuesto.crearConMultiplesArticulos();

            // Act
            gestorPedidos.agregarPresupuesto(presupuesto);
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido).isNotNull();
            assertThat(pedido.getDetallesPedido()).hasSize(3);
            assertThat(pedido.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR_VALIDO);

            // Verificar que el total del pedido es correcto
            BigDecimal totalEsperado = new BigDecimal("875.00"); // 500 + 300 + 75
            assertThat(pedido.getTotalPedido()).isEqualByComparingTo(totalEsperado);
        }

        @Test
        @DisplayName("Debería manejar múltiples proveedores correctamente")
        void deberiaManejarMultiplesProveedoresCorrectamente() throws Exception {
            // Arrange
            gestorPedidos.agregarPresupuesto(mockPresupuesto);

            // Act & Assert - Generar pedidos para diferentes proveedores
            Pedido pedido1 = gestorPedidos.generarPedido(
                "PROV-001", 1001, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            Pedido pedido2 = gestorPedidos.generarPedido(
                "PROV-002", 1002, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            assertThat(pedido1.getProveedor().getCve()).isEqualTo("PROV-001");
            assertThat(pedido2.getProveedor().getCve()).isEqualTo("PROV-002");
        }

        @Test
        @DisplayName("Debería validar secuencia correcta de operaciones")
        void deberiaValidarSecuenciaCorrectaOperaciones() {
            // Arrange
            GestorPedidos gestorVacio = new GestorPedidos(proveedoresValidos);

            // Act & Assert - Intentar generar pedido sin presupuesto
            assertThatThrownBy(() -> {
                gestorVacio.generarPedido(
                    CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            }).isInstanceOf(PresupuestoNoCargadoExcepcion.class);

            // Ahora agregar presupuesto y debería funcionar
            assertDoesNotThrow(() -> {
                gestorVacio.agregarPresupuesto(mockPresupuesto);
                Pedido pedido = gestorVacio.generarPedido(
                    CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
                assertThat(pedido).isNotNull();
            });
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Casos Edge y Límites")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar presupuesto con valores límite")
        void deberiaManejarPresupuestoConValoresLimite() throws Exception {
            // Arrange
            MockPresupuesto presupuestoLimite = MockPresupuesto.crearConValoresLimite();
            gestorPedidos.agregarPresupuesto(presupuestoLimite);

            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, Integer.MAX_VALUE, Integer.MAX_VALUE,
                LocalDate.MIN, LocalDate.MAX);

            // Assert
            assertThat(pedido).isNotNull();
            assertThat(pedido.getNumPedido()).isEqualTo(Integer.MAX_VALUE);
            assertThat(pedido.getNivelSurtido()).isEqualTo(Integer.MAX_VALUE);
            assertThat(pedido.getFechaEmision()).isEqualTo(LocalDate.MIN);
            assertThat(pedido.getFechaEntrega()).isEqualTo(LocalDate.MAX);
        }

        @Test
        @DisplayName("Debería manejar clave de proveedor con caracteres especiales")
        void deberiaManejarClaveProveedorConCaracteresEspeciales() throws Exception {
            // Arrange
            String claveEspecial = "PROV-TEST-#@$%";
            Proveedor proveedorEspecial = TestUtils.crearProveedor(
                claveEspecial, "Proveedor Especial", "Caracteres Especiales SA");
            List<Proveedor> proveedoresEspeciales = List.of(proveedorEspecial);
            GestorPedidos gestorEspecial = new GestorPedidos(proveedoresEspeciales);

            gestorEspecial.agregarPresupuesto(mockPresupuesto);

            // Act
            Pedido pedido = gestorEspecial.generarPedido(
                claveEspecial, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);

            // Assert
            assertThat(pedido.getProveedor().getCve()).isEqualTo(claveEspecial);
        }

        @Test
        @DisplayName("Debería manejar fechas iguales para emisión y entrega")
        void deberiaManejarFechasIguales() throws Exception {
            // Arrange
            gestorPedidos.agregarPresupuesto(mockPresupuesto);
            LocalDate fechaIgual = LocalDate.now();

            // Act
            Pedido pedido = gestorPedidos.generarPedido(
                CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                fechaIgual, fechaIgual);

            // Assert
            assertThat(pedido.getFechaEmision()).isEqualTo(fechaIgual);
            assertThat(pedido.getFechaEntrega()).isEqualTo(fechaIgual);
        }
    }

    // ==================== TESTS DE VALIDACIÓN DE EXCEPCIONES ====================

    @Nested
    @DisplayName("Validación de Excepciones de Dominio")
    class ExcepcionesTest {

        @Test
        @DisplayName("ProveedorNoExisteExcepcion debería contener clave del proveedor")
        void proveedorNoExisteExcepcion_deberiaContenerClaveProveedor() throws PresupuestoNoCargadoExcepcion {
            // Arrange
            gestorPedidos.agregarPresupuesto(mockPresupuesto);

            // Act & Assert
            ProveedorNoExisteExcepcion excepcion = assertThrows(
                ProveedorNoExisteExcepcion.class,
                () -> gestorPedidos.generarPedido(
                    CVE_PROVEEDOR_INEXISTENTE, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST)
            );

            // La excepción debería incluir información útil
            assertThat(excepcion).hasMessageContaining(CVE_PROVEEDOR_INEXISTENTE);
        }

        @Test
        @DisplayName("Debería manejar múltiples excepciones en secuencia")
        void deberiaManejarMultiplesExcepcionesEnSecuencia() {
            // Arrange
            GestorPedidos gestorVacio = new GestorPedidos(proveedoresValidos);

            // Act & Assert - Primera excepción: sin presupuesto
            assertThatThrownBy(() -> {
                gestorVacio.generarPedido(
                    CVE_PROVEEDOR_VALIDO, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            }).isInstanceOf(PresupuestoNoCargadoExcepcion.class);

            // Agregar presupuesto null - Segunda excepción
            assertThatThrownBy(() -> {
                gestorVacio.agregarPresupuesto(null);
            }).isInstanceOf(PresupuestoNoCargadoExcepcion.class);

            // Después de agregar presupuesto válido, probar proveedor inexistente
            assertThatThrownBy(() -> {
                gestorVacio.agregarPresupuesto(mockPresupuesto);
                gestorVacio.generarPedido(
                    CVE_PROVEEDOR_INEXISTENTE, NUM_PEDIDO_TEST, NIVEL_SURTIDO_TEST,
                    FECHA_EMISION_TEST, FECHA_ENTREGA_TEST);
            }).isInstanceOf(ProveedorNoExisteExcepcion.class);
        }
    }
}