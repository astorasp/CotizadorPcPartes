package mx.com.qtx.cotizador.dominio.pedidos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Tests unitarios para la clase DetallePedido del dominio de pedidos.
 *
 * <p>Verifica la funcionalidad completa de la clase {@link DetallePedido} que representa
 * una línea de detalle dentro de un pedido. Esta clase es fundamental para transferir
 * datos desde un presupuesto hacia un pedido formal y debe mantener la integridad
 * de los datos de cada artículo incluido.</p>
 *
 * <h3>Cobertura de Testing:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Inicialización correcta con todos los parámetros</li>
 *   <li><strong>Getters:</strong> Acceso correcto a todas las propiedades</li>
 *   <li><strong>Setters:</strong> Modificación segura de propiedades editables</li>
 *   <li><strong>toString:</strong> Formateo correcto para representación tabular</li>
 *   <li><strong>getHeader:</strong> Generación correcta de encabezados de tabla</li>
 *   <li><strong>Validaciones:</strong> Manejo de valores límite y edge cases</li>
 *   <li><strong>Cálculos:</strong> Consistencia entre precios unitarios y totales</li>
 * </ul>
 *
 * <h3>Casos de Uso Validados:</h3>
 * <ul>
 *   <li>Creación de detalles de pedido desde presupuestos</li>
 *   <li>Representación formateda para reportes y visualización</li>
 *   <li>Modificación de totales durante procesamiento de pedidos</li>
 *   <li>Manejo de diferentes tipos de componentes y cantidades</li>
 * </ul>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 2.0.0
 * @see DetallePedido Clase bajo test
 * @see Pedido Clase contenedora de detalles
 * @see TestUtils Utilidades para crear objetos de test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DetallePedido - Tests de Dominio de Pedidos")
class DetallePedidoTest {

    private DetallePedido detallePedido;

    // Constantes para tests
    private static final String ID_ARTICULO_TEST = "COMP-TEST-001";
    private static final String DESCRIPCION_TEST = "Monitor LED 27 pulgadas Full HD";
    private static final int CANTIDAD_TEST = 2;
    private static final BigDecimal PRECIO_UNITARIO_TEST = new BigDecimal("750.50");
    private static final BigDecimal TOTAL_COTIZADO_TEST = new BigDecimal("1501.00");

    @BeforeEach
    void setUp() {
        detallePedido = new DetallePedido(
            ID_ARTICULO_TEST,
            DESCRIPCION_TEST,
            CANTIDAD_TEST,
            PRECIO_UNITARIO_TEST,
            TOTAL_COTIZADO_TEST
        );
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar correctamente con todos los parámetros")
        void constructor_deberiaInicializarCorrectamente() {
            // Arrange & Act
            DetallePedido detalle = new DetallePedido(
                ID_ARTICULO_TEST,
                DESCRIPCION_TEST,
                CANTIDAD_TEST,
                PRECIO_UNITARIO_TEST,
                TOTAL_COTIZADO_TEST
            );

            // Assert
            assertThat(detalle).isNotNull();
            assertThat(detalle.getIdArticulo()).isEqualTo(ID_ARTICULO_TEST);
            assertThat(detalle.getDescripcion()).isEqualTo(DESCRIPCION_TEST);
            assertThat(detalle.getCantidad()).isEqualTo(CANTIDAD_TEST);
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(PRECIO_UNITARIO_TEST);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(TOTAL_COTIZADO_TEST);
        }

        @Test
        @DisplayName("Debería crear detalle con datos de TestUtils")
        void constructor_deberiaCrearConDatosTestUtils() {
            // Arrange & Act
            DetallePedido detalle = TestUtils.crearDetallePedidoValido();

            // Assert
            assertThat(detalle).isNotNull();
            assertThat(detalle.getIdArticulo()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_ID);
            assertThat(detalle.getDescripcion()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_DESC);
            assertThat(detalle.getCantidad()).isEqualTo(TestUtils.DEFAULT_CANTIDAD);
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(TestUtils.DEFAULT_PRECIO_BASE);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(TestUtils.DEFAULT_TOTAL_COTIZADO);
        }

        @Test
        @DisplayName("Debería manejar valores decimales con precisión")
        void constructor_deberiaManejarDecimalesConPrecision() {
            // Arrange
            BigDecimal precioConPrecision = new BigDecimal("123.456789");
            BigDecimal totalConPrecision = new BigDecimal("246.913578");

            // Act
            DetallePedido detalle = new DetallePedido(
                "COMP-PRECISION",
                "Componente con decimales",
                2,
                precioConPrecision,
                totalConPrecision
            );

            // Assert
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(precioConPrecision);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(totalConPrecision);
        }

        @Test
        @DisplayName("Debería manejar cantidades grandes")
        void constructor_deberiaManejarCantidadesGrandes() {
            // Arrange
            int cantidadGrande = 10000;
            BigDecimal precioUnitario = new BigDecimal("1.50");
            BigDecimal totalEsperado = new BigDecimal("15000.00");

            // Act
            DetallePedido detalle = new DetallePedido(
                "COMP-BULK",
                "Componente a granel",
                cantidadGrande,
                precioUnitario,
                totalEsperado
            );

            // Assert
            assertThat(detalle.getCantidad()).isEqualTo(cantidadGrande);
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(precioUnitario);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(totalEsperado);
        }
    }

    // ==================== TESTS DE GETTERS ====================

    @Nested
    @DisplayName("Métodos Getter")
    class GettersTest {

        @Test
        @DisplayName("getIdArticulo debería retornar el ID correcto")
        void getIdArticulo_deberiaRetornarIdCorrecto() {
            // Act
            String id = detallePedido.getIdArticulo();

            // Assert
            assertThat(id).isEqualTo(ID_ARTICULO_TEST);
            assertThat(id).isNotNull();
            assertThat(id).isNotBlank();
        }

        @Test
        @DisplayName("getDescripcion debería retornar la descripción correcta")
        void getDescripcion_deberiaRetornarDescripcionCorrecta() {
            // Act
            String descripcion = detallePedido.getDescripcion();

            // Assert
            assertThat(descripcion).isEqualTo(DESCRIPCION_TEST);
            assertThat(descripcion).isNotNull();
            assertThat(descripcion).isNotBlank();
        }

        @Test
        @DisplayName("getCantidad debería retornar la cantidad correcta")
        void getCantidad_deberiaRetornarCantidadCorrecta() {
            // Act
            int cantidad = detallePedido.getCantidad();

            // Assert
            assertThat(cantidad).isEqualTo(CANTIDAD_TEST);
            assertThat(cantidad).isPositive();
        }

        @Test
        @DisplayName("getPrecioUnitario debería retornar el precio correcto")
        void getPrecioUnitario_deberiaRetornarPrecioCorrecto() {
            // Act
            BigDecimal precio = detallePedido.getPrecioUnitario();

            // Assert
            assertThat(precio).isEqualByComparingTo(PRECIO_UNITARIO_TEST);
            assertThat(precio).isNotNull();
            assertThat(precio).isPositive();
        }

        @Test
        @DisplayName("getTotalCotizado debería retornar el total correcto")
        void getTotalCotizado_deberiaRetornarTotalCorrecto() {
            // Act
            BigDecimal total = detallePedido.getTotalCotizado();

            // Assert
            assertThat(total).isEqualByComparingTo(TOTAL_COTIZADO_TEST);
            assertThat(total).isNotNull();
            assertThat(total).isPositive();
        }

        @Test
        @DisplayName("Todos los getters deberían ser inmutables (retornar copias o inmutables)")
        void getters_deberianSerInmutables() {
            // Act - Intentar modificar strings (inmutables por naturaleza)
            String id = detallePedido.getIdArticulo();
            String descripcion = detallePedido.getDescripcion();

            // Assert - Los strings no pueden modificarse, pero verificamos que no son null
            assertThat(id).isNotNull();
            assertThat(descripcion).isNotNull();

            // Los primitivos (int) son inmutables por copia
            int cantidad1 = detallePedido.getCantidad();
            int cantidad2 = detallePedido.getCantidad();
            assertThat(cantidad1).isEqualTo(cantidad2);
        }
    }

    // ==================== TESTS DE SETTERS ====================

    @Nested
    @DisplayName("Métodos Setter")
    class SettersTest {

        @Test
        @DisplayName("setTotalCotizado debería actualizar el total correctamente")
        void setTotalCotizado_deberiaActualizarTotalCorrectamente() {
            // Arrange
            BigDecimal nuevoTotal = new BigDecimal("2000.75");

            // Act
            detallePedido.setTotalCotizado(nuevoTotal);

            // Assert
            assertThat(detallePedido.getTotalCotizado()).isEqualByComparingTo(nuevoTotal);
        }

        @Test
        @DisplayName("setTotalCotizado debería manejar valores decimales precisos")
        void setTotalCotizado_deberiaManejarDecimalesPrecisos() {
            // Arrange
            BigDecimal totalPreciso = new BigDecimal("1234.123456789");

            // Act
            detallePedido.setTotalCotizado(totalPreciso);

            // Assert
            assertThat(detallePedido.getTotalCotizado()).isEqualByComparingTo(totalPreciso);
        }

        @Test
        @DisplayName("setTotalCotizado debería manejar valores cero")
        void setTotalCotizado_deberiaManejarValoresCero() {
            // Act
            detallePedido.setTotalCotizado(BigDecimal.ZERO);

            // Assert
            assertThat(detallePedido.getTotalCotizado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("setTotalCotizado debería manejar valores grandes")
        void setTotalCotizado_deberiaManejarValoresGrandes() {
            // Arrange
            BigDecimal totalGrande = new BigDecimal("999999999.99");

            // Act
            detallePedido.setTotalCotizado(totalGrande);

            // Assert
            assertThat(detallePedido.getTotalCotizado()).isEqualByComparingTo(totalGrande);
        }
    }

    // ==================== TESTS DE toString ====================

    @Nested
    @DisplayName("Representación String y Formato")
    class ToStringTest {

        @Test
        @DisplayName("toString debería generar formato tabular correcto")
        void toString_deberiaGenerarFormatoTabular() {
            // Act
            String resultado = detallePedido.toString();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado).isNotBlank();

            // Verificar que contiene los datos esperados
            assertThat(resultado).contains(ID_ARTICULO_TEST);
            assertThat(resultado).contains(DESCRIPCION_TEST);
            assertThat(resultado).contains(String.valueOf(CANTIDAD_TEST));

            // Verificar formato tabular (debe contener barras verticales)
            assertThat(resultado).contains("|");
            assertThat(resultado).startsWith("|");
            assertThat(resultado).endsWith("|");
        }

        @Test
        @DisplayName("toString debería formatear números correctamente")
        void toString_deberiaFormatearNumerosCorrectamente() {
            // Arrange
            DetallePedido detalleNumeros = new DetallePedido(
                "COMP-NUM",
                "Componente para test números",
                5,
                new BigDecimal("1234.56"),
                new BigDecimal("6172.80")
            );

            // Act
            String resultado = detalleNumeros.toString();

            // Assert
            assertThat(resultado).contains("1,234.56");  // Formato con comas
            assertThat(resultado).contains("6,172.80");
            assertThat(resultado).contains("5");  // Cantidad sin formato especial
        }

        @Test
        @DisplayName("toString debería manejar descripciones largas")
        void toString_deberiaManejarDescripcionesLargas() {
            // Arrange
            String descripcionLarga = "Descripción muy larga que podría exceder el límite de caracteres establecido";
            DetallePedido detalleDescripcionLarga = new DetallePedido(
                "COMP-LONG",
                descripcionLarga,
                1,
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
            );

            // Act
            String resultado = detalleDescripcionLarga.toString();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado).contains("COMP-LONG");
            // El formato trunca a 30 caracteres según el código
            assertThat(resultado).contains(descripcionLarga.substring(0, 30));
        }

        @Test
        @DisplayName("toString debería ser consistente en múltiples llamadas")
        void toString_deberiaSerConsistente() {
            // Act
            String resultado1 = detallePedido.toString();
            String resultado2 = detallePedido.toString();

            // Assert
            assertThat(resultado1).isEqualTo(resultado2);
        }
    }

    // ==================== TESTS DE getHeader ====================

    @Nested
    @DisplayName("Generación de Encabezados")
    class GetHeaderTest {

        @Test
        @DisplayName("getHeader debería generar encabezado tabular correcto")
        void getHeader_deberiaGenerarEncabezadoTabular() {
            // Act
            String header = DetallePedido.getHeader();

            // Assert
            assertThat(header).isNotNull();
            assertThat(header).isNotBlank();

            // Verificar que contiene los nombres de columna esperados
            assertThat(header).contains("idArticulo");
            assertThat(header).contains("descripcion");
            assertThat(header).contains("cant.");
            assertThat(header).contains("precio Unitario");
            assertThat(header).contains("total Cotizado");

            // Verificar formato tabular
            assertThat(header).contains("|");
            assertThat(header).startsWith("|");
            assertThat(header).endsWith("|");
        }

        @Test
        @DisplayName("getHeader debería ser método estático")
        void getHeader_deberiaSerMetodoEstatico() {
            // Act - Llamar sin instancia
            String header = DetallePedido.getHeader();

            // Assert
            assertThat(header).isNotNull();
            assertThat(header).contains("idArticulo");
        }

        @Test
        @DisplayName("getHeader debería coincidir con formato de toString")
        void getHeader_deberiaCoincidirConFormatoToString() {
            // Act
            String header = DetallePedido.getHeader();
            String detalleString = detallePedido.toString();

            // Assert - Verificar que tienen el mismo número de columnas
            long headerColumns = header.chars().filter(ch -> ch == '|').count();
            long detalleColumns = detalleString.chars().filter(ch -> ch == '|').count();

            assertThat(headerColumns).isEqualTo(detalleColumns);
        }

        @Test
        @DisplayName("getHeader debería ser inmutable entre llamadas")
        void getHeader_deberiaSerInmutable() {
            // Act
            String header1 = DetallePedido.getHeader();
            String header2 = DetallePedido.getHeader();

            // Assert
            assertThat(header1).isEqualTo(header2);
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Casos Edge y Límites")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar IDs muy largos")
        void deberiaManejarIdsLargos() {
            // Arrange
            String idLargo = "COMP-" + "X".repeat(50);

            // Act
            DetallePedido detalle = new DetallePedido(
                idLargo,
                "Descripción normal",
                1,
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
            );

            // Assert
            assertThat(detalle.getIdArticulo()).isEqualTo(idLargo);
            assertThat(detalle.toString()).contains(idLargo.substring(0, 10)); // Formato trunca a 10
        }

        @Test
        @DisplayName("Debería manejar cantidades de valor uno")
        void deberiaManejarCantidadUno() {
            // Act
            DetallePedido detalle = new DetallePedido(
                "COMP-UNO",
                "Un componente",
                1,
                new BigDecimal("500.00"),
                new BigDecimal("500.00")
            );

            // Assert
            assertThat(detalle.getCantidad()).isEqualTo(1);
            assertThat(detalle.toString()).contains("1");
        }

        @Test
        @DisplayName("Debería manejar precios con muchos decimales")
        void deberiaManejarPreciosConMuchosDecimales() {
            // Arrange
            BigDecimal precioConMuchosDecimales = new BigDecimal("123.123456789012345");
            BigDecimal totalConMuchosDecimales = new BigDecimal("246.246913578024690");

            // Act
            DetallePedido detalle = new DetallePedido(
                "COMP-DECIMAL",
                "Decimales precisos",
                2,
                precioConMuchosDecimales,
                totalConMuchosDecimales
            );

            // Assert
            assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo(precioConMuchosDecimales);
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(totalConMuchosDecimales);
        }

        @Test
        @DisplayName("Debería manejar descripciones con caracteres especiales")
        void deberiaManejarDescripcionesConCaracteresEspeciales() {
            // Arrange
            String descripcionEspecial = "Monitor 24\" Full-HD (1920x1080) @ 60Hz - Marca®";

            // Act
            DetallePedido detalle = new DetallePedido(
                "COMP-ESPECIAL",
                descripcionEspecial,
                1,
                new BigDecimal("450.00"),
                new BigDecimal("450.00")
            );

            // Assert
            assertThat(detalle.getDescripcion()).isEqualTo(descripcionEspecial);
            assertThat(detalle.toString()).contains("Monitor 24\" Full-HD (1920x108"); // Truncado a 30 chars
        }
    }

    // ==================== TESTS DE VALIDACIÓN DE CONSISTENCIA ====================

    @Nested
    @DisplayName("Validación de Consistencia de Datos")
    class ConsistenciaTest {

        @Test
        @DisplayName("Debería mantener consistencia después de modificaciones")
        void deberiaMantenerConsistenciaDespuesDeModificaciones() {
            // Arrange
            DetallePedido detalle = TestUtils.crearDetallePedidoValido();
            BigDecimal nuevoTotal = new BigDecimal("1200.00");

            // Act
            detalle.setTotalCotizado(nuevoTotal);

            // Assert
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(nuevoTotal);
            assertThat(detalle.getIdArticulo()).isEqualTo(TestUtils.DEFAULT_COMPONENTE_ID); // No cambia
            assertThat(detalle.getCantidad()).isEqualTo(TestUtils.DEFAULT_CANTIDAD); // No cambia
        }

        @Test
        @DisplayName("Debería permitir cálculo de total teórico vs total cotizado")
        void deberiaPermitirCalculoDeTotalTeorico() {
            // Arrange
            DetallePedido detalle = new DetallePedido(
                "COMP-CALC",
                "Componente para cálculo",
                3,
                new BigDecimal("100.00"),
                new BigDecimal("290.00") // Total diferente del cálculo exacto (podría incluir descuentos)
            );

            // Act - Calcular total teórico
            BigDecimal totalTeorico = detalle.getPrecioUnitario()
                .multiply(new BigDecimal(detalle.getCantidad()));

            // Assert
            assertThat(totalTeorico).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(detalle.getTotalCotizado()).isEqualByComparingTo(new BigDecimal("290.00"));
            // El total cotizado puede diferir del teórico por descuentos, impuestos, etc.
        }
    }
}