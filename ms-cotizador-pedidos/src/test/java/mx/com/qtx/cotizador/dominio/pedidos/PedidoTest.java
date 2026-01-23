package mx.com.qtx.cotizador.dominio.pedidos;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Pruebas unitarias para la clase {@link Pedido}.
 *
 * <p>Esta clase de pruebas verifica el funcionamiento correcto del objeto de dominio Pedido,
 * incluyendo todas sus operaciones, validaciones y comportamientos. Las pruebas cubren:</p>
 *
 * <ul>
 *   <li><strong>Constructor:</strong> Inicialización correcta con parámetros válidos</li>
 *   <li><strong>Getters:</strong> Acceso correcto a todos los campos del pedido</li>
 *   <li><strong>Gestión de detalles:</strong> Agregar y consultar detalles del pedido</li>
 *   <li><strong>Cálculos:</strong> Total del pedido basado en detalles</li>
 *   <li><strong>Nivel de surtido:</strong> Modificación del estado de surtido</li>
 *   <li><strong>Representación string:</strong> Formato de salida para logging e impresión</li>
 *   <li><strong>Inmutabilidad:</strong> Protección contra modificaciones externas</li>
 * </ul>
 *
 * <h3>Cobertura de Casos de Prueba:</h3>
 * <table border="1">
 *   <tr><th>Categoría</th><th>Escenarios</th><th>Validaciones</th></tr>
 *   <tr><td>Constructor</td><td>Parámetros válidos, valores límite</td><td>Inicialización correcta</td></tr>
 *   <tr><td>Getters</td><td>Todos los campos, consistencia</td><td>Valores correctos</td></tr>
 *   <tr><td>Detalles</td><td>Agregar, consultar, copias defensivas</td><td>Integridad de datos</td></tr>
 *   <tr><td>Cálculos</td><td>Total con/sin detalles, múltiples items</td><td>Sumas correctas</td></tr>
 *   <tr><td>Surtido</td><td>Modificación, valores límite</td><td>Actualizaciones válidas</td></tr>
 *   <tr><td>String</td><td>Formato, casos vacíos, formatting</td><td>Representación adecuada</td></tr>
 * </table>
 *
 * <h3>Patrones de Prueba Utilizados:</h3>
 * <ul>
 *   <li><strong>AAA Pattern:</strong> Arrange-Act-Assert para estructura clara</li>
 *   <li><strong>Nested Tests:</strong> Agrupación lógica por funcionalidad</li>
 *   <li><strong>TestUtils:</strong> Datos de prueba estandarizados</li>
 *   <li><strong>Defensive Copy Testing:</strong> Verificación de inmutabilidad</li>
 *   <li><strong>Edge Case Testing:</strong> Casos límite y valores extremos</li>
 * </ul>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 1.0.0
 * @see Pedido
 * @see DetallePedido
 * @see Proveedor
 */
@DisplayName("Pedido - Objeto de dominio para gestión de pedidos")
class PedidoTest {

    // ==================== CAMPOS DE PRUEBA ====================

    private Pedido pedido;
    private Proveedor proveedorValido;

    // Constantes para pruebas
    private static final long NUM_PEDIDO_TEST = 12345L;
    private static final LocalDate FECHA_EMISION_TEST = LocalDate.of(2024, 3, 15);
    private static final LocalDate FECHA_ENTREGA_TEST = LocalDate.of(2024, 3, 30);
    private static final int NIVEL_SURTIDO_INICIAL = 0;
    private static final int NIVEL_SURTIDO_ACTUALIZADO = 75;

    // ==================== CONFIGURACIÓN DE PRUEBAS ====================

    @BeforeEach
    void setUp() {
        proveedorValido = TestUtils.crearProveedorValido();
        pedido = new Pedido(NUM_PEDIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST,
                           NIVEL_SURTIDO_INICIAL, proveedorValido);
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería crear pedido correctamente con parámetros válidos")
        void constructor_deberiaCrearPedidoCorrectamente() {
            // Act & Assert
            assertThat(pedido).isNotNull();
            assertThat(pedido.getNumPedido()).isEqualTo(NUM_PEDIDO_TEST);
            assertThat(pedido.getFechaEmision()).isEqualTo(FECHA_EMISION_TEST);
            assertThat(pedido.getFechaEntrega()).isEqualTo(FECHA_ENTREGA_TEST);
            assertThat(pedido.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO_INICIAL);
            assertThat(pedido.getProveedor()).isEqualTo(proveedorValido);
        }

        @Test
        @DisplayName("Debería inicializar lista de detalles vacía")
        void constructor_deberiaInicializarDetallesVacios() {
            // Act
            List<DetallePedido> detalles = pedido.getDetallesPedido();

            // Assert
            assertThat(detalles).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Debería aceptar valores límite válidos")
        void constructor_deberiaAceptarValoresLimite() {
            // Arrange
            long numPedidoMaximo = Long.MAX_VALUE;
            LocalDate fechaMuyLejana = LocalDate.of(2099, 12, 31);
            LocalDate fechaPasada = LocalDate.of(1900, 1, 1);
            int nivelMaximo = Integer.MAX_VALUE;

            // Act
            Pedido pedidoLimite = new Pedido(numPedidoMaximo, fechaPasada, fechaMuyLejana,
                                            nivelMaximo, proveedorValido);

            // Assert
            assertThat(pedidoLimite.getNumPedido()).isEqualTo(numPedidoMaximo);
            assertThat(pedidoLimite.getFechaEmision()).isEqualTo(fechaPasada);
            assertThat(pedidoLimite.getFechaEntrega()).isEqualTo(fechaMuyLejana);
            assertThat(pedidoLimite.getNivelSurtido()).isEqualTo(nivelMaximo);
        }

        @Test
        @DisplayName("Debería aceptar proveedor null sin fallar")
        void constructor_deberiaAceptarProveedorNull() {
            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                new Pedido(NUM_PEDIDO_TEST, FECHA_EMISION_TEST, FECHA_ENTREGA_TEST,
                          NIVEL_SURTIDO_INICIAL, null);
            });
        }

        @Test
        @DisplayName("Debería aceptar fechas null sin fallar")
        void constructor_deberiaAceptarFechasNull() {
            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                new Pedido(NUM_PEDIDO_TEST, null, null, NIVEL_SURTIDO_INICIAL, proveedorValido);
            });
        }
    }

    // ==================== TESTS DE GETTERS ====================

    @Nested
    @DisplayName("Métodos Getter")
    class GettersTest {

        @Test
        @DisplayName("getNumPedido() debería retornar número correcto")
        void getNumPedido_deberiaRetornarNumeroCorrect() {
            // Act & Assert
            assertThat(pedido.getNumPedido()).isEqualTo(NUM_PEDIDO_TEST);
        }

        @Test
        @DisplayName("getFechaEmision() debería retornar fecha correcta")
        void getFechaEmision_deberiaRetornarFechaCorrecta() {
            // Act & Assert
            assertThat(pedido.getFechaEmision()).isEqualTo(FECHA_EMISION_TEST);
        }

        @Test
        @DisplayName("getFechaEntrega() debería retornar fecha correcta")
        void getFechaEntrega_deberiaRetornarFechaCorrecta() {
            // Act & Assert
            assertThat(pedido.getFechaEntrega()).isEqualTo(FECHA_ENTREGA_TEST);
        }

        @Test
        @DisplayName("getNivelSurtido() debería retornar nivel inicial")
        void getNivelSurtido_deberiaRetornarNivelInicial() {
            // Act & Assert
            assertThat(pedido.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO_INICIAL);
        }

        @Test
        @DisplayName("getProveedor() debería retornar proveedor correcto")
        void getProveedor_deberiaRetornarProveedorCorrecto() {
            // Act & Assert
            assertThat(pedido.getProveedor()).isEqualTo(proveedorValido);
            assertThat(pedido.getProveedor().getCve()).isEqualTo(TestUtils.DEFAULT_PROVEEDOR_CVE);
        }

        @Test
        @DisplayName("Getters deberían ser consistentes en múltiples llamadas")
        void getters_deberianSerConsistentes() {
            // Act
            long numPedido1 = pedido.getNumPedido();
            long numPedido2 = pedido.getNumPedido();
            LocalDate fecha1 = pedido.getFechaEmision();
            LocalDate fecha2 = pedido.getFechaEmision();

            // Assert
            assertThat(numPedido1).isEqualTo(numPedido2);
            assertThat(fecha1).isEqualTo(fecha2);
        }
    }

    // ==================== TESTS DE NIVEL DE SURTIDO ====================

    @Nested
    @DisplayName("Gestión de Nivel de Surtido")
    class NivelSurtidoTest {

        @Test
        @DisplayName("setNivelSurtido() debería actualizar nivel correctamente")
        void setNivelSurtido_deberiaActualizarCorrectamente() {
            // Act
            pedido.setNivelSurtido(NIVEL_SURTIDO_ACTUALIZADO);

            // Assert
            assertThat(pedido.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO_ACTUALIZADO);
        }

        @Test
        @DisplayName("Debería manejar valores de nivel de surtido límite")
        void deberiaManejarValoresLimite() {
            // Act & Assert
            pedido.setNivelSurtido(0);
            assertThat(pedido.getNivelSurtido()).isZero();

            pedido.setNivelSurtido(100);
            assertThat(pedido.getNivelSurtido()).isEqualTo(100);

            pedido.setNivelSurtido(Integer.MAX_VALUE);
            assertThat(pedido.getNivelSurtido()).isEqualTo(Integer.MAX_VALUE);

            pedido.setNivelSurtido(Integer.MIN_VALUE);
            assertThat(pedido.getNivelSurtido()).isEqualTo(Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("Debería permitir múltiples actualizaciones")
        void deberiaPermitirMultiplesActualizaciones() {
            // Act
            pedido.setNivelSurtido(25);
            pedido.setNivelSurtido(50);
            pedido.setNivelSurtido(75);
            pedido.setNivelSurtido(100);

            // Assert
            assertThat(pedido.getNivelSurtido()).isEqualTo(100);
        }
    }

    // ==================== TESTS DE GESTIÓN DE DETALLES ====================

    @Nested
    @DisplayName("Gestión de Detalles del Pedido")
    class GestionDetallesTest {

        @Test
        @DisplayName("agregarDetallePedido() debería agregar detalle correctamente")
        void agregarDetallePedido_deberiaAgregarCorrectamente() {
            // Act
            pedido.agregarDetallePedido(TestUtils.DEFAULT_COMPONENTE_ID, TestUtils.DEFAULT_COMPONENTE_DESC,
                TestUtils.DEFAULT_CANTIDAD, TestUtils.DEFAULT_PRECIO_BASE, TestUtils.DEFAULT_TOTAL_COTIZADO);

            // Assert
            List<DetallePedido> detalles = pedido.getDetallesPedido();
            assertThat(detalles).hasSize(1);

            DetallePedido detalle = detalles.get(0);
            assertThat(detalle.getIdArticulo()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_ID);
            assertThat(detalle.getDescripcion()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
            assertThat(detalle.getCantidad()).isEqualTo(TestUtils.DEFAULT_CANTIDAD);
            assertThat(detalle.getPrecioUnitario()).isEqualTo(TestUtils.DEFAULT_PRECIO_BASE);
            assertThat(detalle.getTotalCotizado()).isEqualTo(TestUtils.DEFAULT_TOTAL_COTIZADO);
        }

        @Test
        @DisplayName("Debería agregar múltiples detalles correctamente")
        void deberiaAgregarMultiplesDetalles() {
            // Act
            pedido.agregarDetallePedido("COMP-001", "Componente 1", 1, new BigDecimal("100.00"), new BigDecimal("100.00"));
            pedido.agregarDetallePedido("COMP-002", "Componente 2", 2, new BigDecimal("50.00"), new BigDecimal("100.00"));
            pedido.agregarDetallePedido("COMP-003", "Componente 3", 1, new BigDecimal("300.00"), new BigDecimal("300.00"));

            // Assert
            List<DetallePedido> detalles = pedido.getDetallesPedido();
            assertThat(detalles).hasSize(3);

            // Verificar orden de inserción
            assertThat(detalles.get(0).getIdArticulo()).isEqualTo("COMP-001");
            assertThat(detalles.get(1).getIdArticulo()).isEqualTo("COMP-002");
            assertThat(detalles.get(2).getIdArticulo()).isEqualTo("COMP-003");
        }

        @Test
        @DisplayName("getDetallesPedido() debería retornar copia defensiva")
        void getDetallesPedido_deberiaRetornarCopiaDefensiva() {
            // Arrange
            pedido.agregarDetallePedido(TestUtils.DEFAULT_COMPONENTE_ID, TestUtils.DEFAULT_COMPONENTE_DESC,
                TestUtils.DEFAULT_CANTIDAD, TestUtils.DEFAULT_PRECIO_BASE, TestUtils.DEFAULT_TOTAL_COTIZADO);

            // Act
            List<DetallePedido> detalles1 = pedido.getDetallesPedido();
            List<DetallePedido> detalles2 = pedido.getDetallesPedido();

            // Assert
            assertThat(detalles1).isNotSameAs(detalles2); // Diferentes instancias
            assertThat(detalles1).isEqualTo(detalles2);   // Pero mismo contenido

            // Modificar la lista externa no debe afectar el pedido
            detalles1.clear();
            assertThat(pedido.getDetallesPedido()).hasSize(1); // Lista original intacta
        }

        @Test
        @DisplayName("Debería manejar detalles con valores límite")
        void deberiaManejarDetallesConValoresLimite() {
            // Act
            pedido.agregarDetallePedido("ID-VERY-LONG-COMPONENT-IDENTIFIER-123456789",
                "Descripción muy larga para probar límites de caracteres en el sistema de cotizaciones",
                Integer.MAX_VALUE, new BigDecimal("999999999.99"), new BigDecimal("999999999999999.99"));

            // Assert
            List<DetallePedido> detalles = pedido.getDetallesPedido();
            assertThat(detalles).hasSize(1);

            DetallePedido detalle = detalles.get(0);
            assertThat(detalle.getCantidad()).isEqualTo(Integer.MAX_VALUE);
            assertThat(detalle.getPrecioUnitario()).isEqualTo(new BigDecimal("999999999.99"));
        }
    }

    // ==================== TESTS DE CÁLCULO DE TOTAL ====================

    @Nested
    @DisplayName("Cálculo de Total del Pedido")
    class CalculoTotalTest {

        @Test
        @DisplayName("getTotalPedido() debería retornar cero para pedido sin detalles")
        void getTotalPedido_deberiaRetornarCeroSinDetalles() {
            // Act
            BigDecimal total = pedido.getTotalPedido();

            // Assert
            assertThat(total).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("getTotalPedido() debería calcular total correctamente con un detalle")
        void getTotalPedido_deberiaCalcularTotalConUnDetalle() {
            // Arrange
            BigDecimal totalEsperado = new BigDecimal("500.00");
            pedido.agregarDetallePedido(TestUtils.DEFAULT_COMPONENTE_ID, TestUtils.DEFAULT_COMPONENTE_DESC,
                TestUtils.DEFAULT_CANTIDAD, TestUtils.DEFAULT_PRECIO_BASE, totalEsperado);

            // Act
            BigDecimal total = pedido.getTotalPedido();

            // Assert
            assertThat(total).isEqualTo(totalEsperado);
        }

        @Test
        @DisplayName("getTotalPedido() debería sumar múltiples detalles correctamente")
        void getTotalPedido_deberiaSumarMultiplesDetalles() {
            // Arrange
            pedido.agregarDetallePedido("COMP-001", "Componente 1", 1, new BigDecimal("100.00"), new BigDecimal("100.00"));
            pedido.agregarDetallePedido("COMP-002", "Componente 2", 2, new BigDecimal("50.00"), new BigDecimal("100.00"));
            pedido.agregarDetallePedido("COMP-003", "Componente 3", 1, new BigDecimal("300.00"), new BigDecimal("300.00"));

            // Act
            BigDecimal total = pedido.getTotalPedido();

            // Assert
            BigDecimal totalEsperado = new BigDecimal("500.00"); // 100 + 100 + 300
            assertThat(total).isEqualTo(totalEsperado);
        }

        @Test
        @DisplayName("getTotalPedido() debería manejar valores decimales correctamente")
        void getTotalPedido_deberiaManejarDecimales() {
            // Arrange
            pedido.agregarDetallePedido("COMP-001", "Componente 1", 1, new BigDecimal("99.99"), new BigDecimal("99.99"));
            pedido.agregarDetallePedido("COMP-002", "Componente 2", 1, new BigDecimal("0.01"), new BigDecimal("0.01"));

            // Act
            BigDecimal total = pedido.getTotalPedido();

            // Assert
            assertThat(total).isEqualTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("getTotalPedido() debería manejar cálculos con valores grandes")
        void getTotalPedido_deberiaManejarValoresGrandes() {
            // Arrange
            BigDecimal valorGrande = new BigDecimal("999999999.99");
            pedido.agregarDetallePedido("COMP-001", "Componente 1", 1, valorGrande, valorGrande);
            pedido.agregarDetallePedido("COMP-002", "Componente 2", 1, valorGrande, valorGrande);

            // Act
            BigDecimal total = pedido.getTotalPedido();

            // Assert
            BigDecimal totalEsperado = new BigDecimal("1999999999.98");
            assertThat(total).isEqualTo(totalEsperado);
        }

        @Test
        @DisplayName("getTotalPedido() debería ser consistente en múltiples llamadas")
        void getTotalPedido_deberiaSerConsistente() {
            // Arrange
            pedido.agregarDetallePedido(TestUtils.DEFAULT_COMPONENTE_ID, TestUtils.DEFAULT_COMPONENTE_DESC,
                TestUtils.DEFAULT_CANTIDAD, TestUtils.DEFAULT_PRECIO_BASE, TestUtils.DEFAULT_TOTAL_COTIZADO);

            // Act
            BigDecimal total1 = pedido.getTotalPedido();
            BigDecimal total2 = pedido.getTotalPedido();

            // Assert
            assertThat(total1).isEqualTo(total2);
        }
    }

    // ==================== TESTS DE toString ====================

    @Nested
    @DisplayName("Representación String")
    class ToStringTest {

        @Test
        @DisplayName("toString() debería incluir datos básicos del pedido")
        void toString_deberiaIncluirDatosBasicos() {
            // Act
            String resultado = pedido.toString();

            // Assert
            assertThat(resultado).contains("Datos del Pedido:");
            assertThat(resultado).contains("numPedido=" + NUM_PEDIDO_TEST);
            assertThat(resultado).contains("fechaEmision=" + FECHA_EMISION_TEST);
            assertThat(resultado).contains("fechaEntrega=" + FECHA_ENTREGA_TEST);
            assertThat(resultado).contains("nivelSurtido=" + NIVEL_SURTIDO_INICIAL);
            assertThat(resultado).contains("proveedor=" + TestUtils.DEFAULT_PROVEEDOR_CVE);
        }

        @Test
        @DisplayName("toString() debería incluir sección de detalles")
        void toString_deberiaIncluirSeccionDetalles() {
            // Act
            String resultado = pedido.toString();

            // Assert
            assertThat(resultado).contains("Detalle Pedido:");
            assertThat(resultado).contains("=".repeat(30)); // Separadores
        }

        @Test
        @DisplayName("toString() debería mostrar detalles cuando existen")
        void toString_deberiaMostrarDetallesExistentes() {
            // Arrange
            pedido.agregarDetallePedido(TestUtils.DEFAULT_COMPONENTE_ID, TestUtils.DEFAULT_COMPONENTE_DESC,
                TestUtils.DEFAULT_CANTIDAD, TestUtils.DEFAULT_PRECIO_BASE, TestUtils.DEFAULT_TOTAL_COTIZADO);

            // Act
            String resultado = pedido.toString();

            // Assert
            assertThat(resultado).contains(TestUtils.DEFAULT_COMPONENTE_ID);
            assertThat(resultado).contains(TestUtils.DEFAULT_COMPONENTE_DESC);
        }

        @Test
        @DisplayName("toString() debería manejar proveedor null")
        void toString_deberiaManejarProveedorNull() {
            // Arrange
            Pedido pedidoSinProveedor = new Pedido(NUM_PEDIDO_TEST, FECHA_EMISION_TEST,
                FECHA_ENTREGA_TEST, NIVEL_SURTIDO_INICIAL, null);

            // Act
            String resultado = pedidoSinProveedor.toString();

            // Assert
            assertThat(resultado).contains("proveedor=N/A");
        }

        @Test
        @DisplayName("toString() debería generar formato consistente")
        void toString_deberiaGenerarFormatoConsistente() {
            // Act
            String resultado1 = pedido.toString();
            String resultado2 = pedido.toString();

            // Assert
            assertThat(resultado1).isEqualTo(resultado2);
        }

        @Test
        @DisplayName("toString() debería manejar múltiples detalles")
        void toString_deberiaManejarMultiplesDetalles() {
            // Arrange
            pedido.agregarDetallePedido("COMP-001", "Componente 1", 1, new BigDecimal("100.00"), new BigDecimal("100.00"));
            pedido.agregarDetallePedido("COMP-002", "Componente 2", 2, new BigDecimal("50.00"), new BigDecimal("100.00"));

            // Act
            String resultado = pedido.toString();

            // Assert
            assertThat(resultado).contains("COMP-001");
            assertThat(resultado).contains("COMP-002");
            assertThat(resultado).contains("Componente 1");
            assertThat(resultado).contains("Componente 2");
        }
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Nested
    @DisplayName("Casos Edge y Comportamientos Límite")
    class CasosEdgeTest {

        @Test
        @DisplayName("Debería manejar operaciones repetidas sin efectos secundarios")
        void deberiaManejarOperacionesRepetidas() {
            // Act
            for (int i = 0; i < 100; i++) {
                pedido.agregarDetallePedido("COMP-" + i, "Descripción " + i,
                    1, new BigDecimal("10.00"), new BigDecimal("10.00"));
            }

            // Assert
            assertThat(pedido.getDetallesPedido()).hasSize(100);
            assertThat(pedido.getTotalPedido()).isEqualTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Debería mantener integridad después de múltiples modificaciones")
        void deberiaMantenerIntegridadDespuesModificaciones() {
            // Act
            pedido.agregarDetallePedido("COMP-001", "Componente 1", 1, new BigDecimal("100.00"), new BigDecimal("100.00"));
            pedido.setNivelSurtido(50);
            pedido.agregarDetallePedido("COMP-002", "Componente 2", 1, new BigDecimal("200.00"), new BigDecimal("200.00"));
            pedido.setNivelSurtido(75);

            // Assert
            assertThat(pedido.getDetallesPedido()).hasSize(2);
            assertThat(pedido.getTotalPedido()).isEqualTo(new BigDecimal("300.00"));
            assertThat(pedido.getNivelSurtido()).isEqualTo(75);
            assertThat(pedido.getNumPedido()).isEqualTo(NUM_PEDIDO_TEST); // Inmutable
        }

        @Test
        @DisplayName("Debería manejar strings largos sin problema")
        void deberiaManejarStringsLargos() {
            // Arrange
            String descripcionLarga = "A".repeat(1000);
            String idLargo = "COMPONENT-ID-VERY-LONG-" + "X".repeat(100);

            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                pedido.agregarDetallePedido(idLargo, descripcionLarga, 1,
                    new BigDecimal("100.00"), new BigDecimal("100.00"));
            });

            assertThat(pedido.getDetallesPedido()).hasSize(1);
            assertThat(pedido.getDetallesPedido().get(0).getDescripcion()).isEqualTo(descripcionLarga);
        }

        @Test
        @DisplayName("Debería manejar fechas en el pasado y futuro lejano")
        void deberiaManejarFechasExtremas() {
            // Arrange
            LocalDate fechaMuyPasada = LocalDate.of(1800, 1, 1);
            LocalDate fechaMuyFutura = LocalDate.of(3000, 12, 31);

            // Act
            Pedido pedidoFechasExtremas = new Pedido(999999L, fechaMuyPasada, fechaMuyFutura,
                                                    100, proveedorValido);

            // Assert
            assertThat(pedidoFechasExtremas.getFechaEmision()).isEqualTo(fechaMuyPasada);
            assertThat(pedidoFechasExtremas.getFechaEntrega()).isEqualTo(fechaMuyFutura);
        }
    }
}