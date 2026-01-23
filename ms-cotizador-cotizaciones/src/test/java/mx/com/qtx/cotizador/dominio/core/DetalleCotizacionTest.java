package mx.com.qtx.cotizador.dominio.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import mx.com.qtx.cotizador.dominio.ValidationUtils;

/**
 * Tests unitarios para la clase DetalleCotizacion del dominio core.
 *
 * <p>Verifica la funcionalidad principal de la clase {@link DetalleCotizacion} incluyendo:
 * <ul>
 * <li>Constructor completo con todos los parámetros</li>
 * <li>Getters y setters de todas las propiedades</li>
 * <li>Validaciones de entrada con ValidationUtils</li>
 * <li>Manejo de precisión con BigDecimal</li>
 * <li>Casos edge y validaciones de límites</li>
 * <li>Consistency entre cantidad, precio base e importe cotizado</li>
 * </ul>
 * </p>
 *
 * <h3>Cobertura de testing:</h3>
 * <ul>
 * <li><strong>Constructor:</strong> Inicialización correcta de todos los campos</li>
 * <li><strong>Propiedades:</strong> Getters/setters con validaciones apropiadas</li>
 * <li><strong>Tipos de datos:</strong> Manejo correcto de int, String y BigDecimal</li>
 * <li><strong>Validaciones:</strong> Parámetros obligatorios y rangos válidos</li>
 * <li><strong>Precision:</strong> Cálculos monetarios con BigDecimal</li>
 * <li><strong>Edge cases:</strong> Valores límite, strings vacíos, cantidades máximas</li>
 * </ul>
 *
 * @author Sistema Cotizador - Testing Team
 * @version 1.0
 * @since 2.0.0
 * @see DetalleCotizacion
 * @see Cotizacion
 * @see ValidationUtils
 */
@DisplayName("DetalleCotizacion - Tests de Dominio Core")
class DetalleCotizacionTest {

    private DetalleCotizacion detalleCotizacion;

    // Datos de prueba estándar
    private static final int NUM_DETALLE_VALIDO = 1;
    private static final String ID_COMPONENTE_VALIDO = "COMP-001";
    private static final String DESCRIPCION_VALIDA = "Monitor 24 pulgadas Full HD";
    private static final int CANTIDAD_VALIDA = 2;
    private static final BigDecimal PRECIO_BASE_VALIDO = new BigDecimal("500.00");
    private static final BigDecimal IMPORTE_COTIZADO_VALIDO = new BigDecimal("1000.00");
    private static final String CATEGORIA_VALIDA = "MONITOR";

    @BeforeEach
    void setUp() {
        detalleCotizacion = crearDetalleCotizacionValido();
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar correctamente con todos los parámetros")
        void constructor_deberiaInicializarCorrectamente() {
            // Arrange & Act
            DetalleCotizacion detalle = new DetalleCotizacion(
                NUM_DETALLE_VALIDO,
                ID_COMPONENTE_VALIDO,
                DESCRIPCION_VALIDA,
                CANTIDAD_VALIDA,
                PRECIO_BASE_VALIDO,
                IMPORTE_COTIZADO_VALIDO,
                CATEGORIA_VALIDA
            );

            // Assert
            assertThat(detalle.getNumDetalle()).isEqualTo(NUM_DETALLE_VALIDO);
            assertThat(detalle.getIdComponente()).isEqualTo(ID_COMPONENTE_VALIDO);
            assertThat(detalle.getDescripcion()).isEqualTo(DESCRIPCION_VALIDA);
            assertThat(detalle.getCantidad()).isEqualTo(CANTIDAD_VALIDA);
            assertThat(detalle.getPrecioBase()).isEqualByComparingTo(PRECIO_BASE_VALIDO);
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(IMPORTE_COTIZADO_VALIDO);
            assertThat(detalle.getCategoria()).isEqualTo(CATEGORIA_VALIDA);
        }

        @Test
        @DisplayName("Debería manejar valores mínimos correctamente")
        void constructor_deberiaManejarValoresMinimos() {
            // Arrange & Act
            DetalleCotizacion detalle = new DetalleCotizacion(
                1, // número mínimo
                "A", // ID mínimo
                "X", // descripción mínima
                1, // cantidad mínima
                BigDecimal.ZERO, // precio cero
                BigDecimal.ZERO, // importe cero
                "C" // categoría mínima
            );

            // Assert
            assertThat(detalle.getNumDetalle()).isEqualTo(1);
            assertThat(detalle.getIdComponente()).isEqualTo("A");
            assertThat(detalle.getDescripcion()).isEqualTo("X");
            assertThat(detalle.getCantidad()).isEqualTo(1);
            assertThat(detalle.getPrecioBase()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(detalle.getCategoria()).isEqualTo("C");
        }

        @Test
        @DisplayName("Debería manejar valores máximos sin overflow")
        void constructor_deberiaManejarValoresMaximos() {
            // Arrange
            int numeroGrande = Integer.MAX_VALUE;
            int cantidadGrande = 999999;
            BigDecimal precioGrande = new BigDecimal("999999999.99");
            BigDecimal importeGrande = new BigDecimal("999999999999.99");

            // Act
            DetalleCotizacion detalle = new DetalleCotizacion(
                numeroGrande,
                "COMP-VERY-LONG-ID-123456789",
                "Descripción muy larga que contiene muchos caracteres para probar límites",
                cantidadGrande,
                precioGrande,
                importeGrande,
                "CATEGORIA_LARGA_DE_COMPONENTE"
            );

            // Assert
            assertThat(detalle.getNumDetalle()).isEqualTo(numeroGrande);
            assertThat(detalle.getCantidad()).isEqualTo(cantidadGrande);
            assertThat(detalle.getPrecioBase()).isEqualByComparingTo(precioGrande);
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(importeGrande);
        }
    }

    // ==================== TESTS DE GETTERS Y SETTERS ====================

    @Nested
    @DisplayName("Getters y Setters")
    class GettersSettersTest {

        @Test
        @DisplayName("Debería manejar número de detalle correctamente")
        void deberiaManejarNumeroDetalle() {
            // Arrange
            int nuevoNumero = 99;

            // Act
            detalleCotizacion.setNumDetalle(nuevoNumero);

            // Assert
            assertThat(detalleCotizacion.getNumDetalle()).isEqualTo(nuevoNumero);
        }

        @Test
        @DisplayName("Debería manejar ID de componente correctamente")
        void deberiaManejarIdComponente() {
            // Arrange
            String nuevoId = "COMP-999";

            // Act
            detalleCotizacion.setIdComponente(nuevoId);

            // Assert
            assertThat(detalleCotizacion.getIdComponente()).isEqualTo(nuevoId);
        }

        @Test
        @DisplayName("Debería manejar descripción correctamente")
        void deberiaManejarDescripcion() {
            // Arrange
            String nuevaDescripcion = "Teclado mecánico RGB retroiluminado";

            // Act
            detalleCotizacion.setDescripcion(nuevaDescripcion);

            // Assert
            assertThat(detalleCotizacion.getDescripcion()).isEqualTo(nuevaDescripcion);
        }

        @Test
        @DisplayName("Debería manejar cantidad correctamente")
        void deberiaManejarCantidad() {
            // Arrange
            int nuevaCantidad = 5;

            // Act
            detalleCotizacion.setCantidad(nuevaCantidad);

            // Assert
            assertThat(detalleCotizacion.getCantidad()).isEqualTo(nuevaCantidad);
        }

        @Test
        @DisplayName("Debería manejar precio base correctamente")
        void deberiaManejarPrecioBase() {
            // Arrange
            BigDecimal nuevoPrecio = new BigDecimal("750.25");

            // Act
            detalleCotizacion.setPrecioBase(nuevoPrecio);

            // Assert
            assertThat(detalleCotizacion.getPrecioBase()).isEqualByComparingTo(nuevoPrecio);
        }

        @Test
        @DisplayName("Debería manejar importe cotizado correctamente")
        void deberiaManejarImporteCotizado() {
            // Arrange
            BigDecimal nuevoImporte = new BigDecimal("1500.75");

            // Act
            detalleCotizacion.setImporteCotizado(nuevoImporte);

            // Assert
            assertThat(detalleCotizacion.getImporteCotizado()).isEqualByComparingTo(nuevoImporte);
        }

        @Test
        @DisplayName("Debería manejar categoría correctamente")
        void deberiaManejarCategoria() {
            // Arrange
            String nuevaCategoria = "TECLADO";

            // Act
            detalleCotizacion.setCategoria(nuevaCategoria);

            // Assert
            assertThat(detalleCotizacion.getCategoria()).isEqualTo(nuevaCategoria);
        }
    }

    // ==================== TESTS DE VALIDACIONES ====================

    @Nested
    @DisplayName("Validaciones con ValidationUtils")
    class ValidacionesTest {

        @Test
        @DisplayName("Debería validar detalle correctamente con parámetros válidos")
        void deberiaValidarDetalleValido() {
            // Act & Assert - No debe lanzar excepción
            ValidationUtils.validateDetalleCotizacion(
                CANTIDAD_VALIDA,
                PRECIO_BASE_VALIDO,
                IMPORTE_COTIZADO_VALIDO
            );
        }

        @Test
        @DisplayName("Debería validar campos de texto válidos")
        void deberiaValidarCamposTextoValidos() {
            // Act & Assert - No debe lanzar excepción
            ValidationUtils.validateCamposTextoDetalle(
                ID_COMPONENTE_VALIDO,
                DESCRIPCION_VALIDA,
                CATEGORIA_VALIDA
            );
        }

        @Test
        @DisplayName("Debería rechazar cantidad cero")
        void deberiaRechazarCantidadCero() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateDetalleCotizacion(
                    0, // cantidad cero
                    PRECIO_BASE_VALIDO,
                    IMPORTE_COTIZADO_VALIDO
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");
        }

        @Test
        @DisplayName("Debería rechazar cantidad negativa")
        void deberiaRechazarCantidadNegativa() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateDetalleCotizacion(
                    -5, // cantidad negativa
                    PRECIO_BASE_VALIDO,
                    IMPORTE_COTIZADO_VALIDO
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");
        }

        @Test
        @DisplayName("Debería rechazar precio base negativo")
        void deberiaRechazarPrecioBaseNegativo() {
            // Arrange
            BigDecimal precioNegativo = new BigDecimal("-100.00");

            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateDetalleCotizacion(
                    CANTIDAD_VALIDA,
                    precioNegativo,
                    IMPORTE_COTIZADO_VALIDO
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativo");
        }

        @Test
        @DisplayName("Debería rechazar precio base null")
        void deberiaRechazarPrecioBaseNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateDetalleCotizacion(
                    CANTIDAD_VALIDA,
                    null, // precio null
                    IMPORTE_COTIZADO_VALIDO
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser null");
        }

        @Test
        @DisplayName("Debería rechazar importe cotizado negativo")
        void deberiaRechazarImporteCotizadoNegativo() {
            // Arrange
            BigDecimal importeNegativo = new BigDecimal("-50.00");

            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateDetalleCotizacion(
                    CANTIDAD_VALIDA,
                    PRECIO_BASE_VALIDO,
                    importeNegativo
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativo");
        }

        @Test
        @DisplayName("Debería rechazar ID de componente null")
        void deberiaRechazarIdComponenteNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateCamposTextoDetalle(
                    null, // ID null
                    DESCRIPCION_VALIDA,
                    CATEGORIA_VALIDA
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser null");
        }

        @Test
        @DisplayName("Debería rechazar ID de componente vacío")
        void deberiaRechazarIdComponenteVacio() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateCamposTextoDetalle(
                    "   ", // ID solo espacios
                    DESCRIPCION_VALIDA,
                    CATEGORIA_VALIDA
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede estar vacío");
        }

        @Test
        @DisplayName("Debería rechazar descripción vacía")
        void deberiaRechazarDescripcionVacia() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateCamposTextoDetalle(
                    ID_COMPONENTE_VALIDO,
                    "", // descripción vacía
                    CATEGORIA_VALIDA
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede estar vacío");
        }

        @Test
        @DisplayName("Debería rechazar categoría null")
        void deberiaRechazarCategoriaNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateCamposTextoDetalle(
                    ID_COMPONENTE_VALIDO,
                    DESCRIPCION_VALIDA,
                    null // categoría null
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser null");
        }

        @Test
        @DisplayName("Debería validar número de detalle positivo")
        void deberiaValidarNumeroDetallePositivo() {
            // Act & Assert - Número positivo válido
            ValidationUtils.validateNumeroDetalle(1);
            ValidationUtils.validateNumeroDetalle(999);

            // Número cero o negativo inválido
            assertThatThrownBy(() -> {
                ValidationUtils.validateNumeroDetalle(0);
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");

            assertThatThrownBy(() -> {
                ValidationUtils.validateNumeroDetalle(-1);
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");
        }
    }

    // ==================== TESTS DE PRECISION Y CÁLCULOS ====================

    @Nested
    @DisplayName("Precision y Cálculos Monetarios")
    class PrecisionCalculosTest {

        @Test
        @DisplayName("Debería manejar precisión decimal correctamente")
        void deberiaManejarPrecisionDecimal() {
            // Arrange
            BigDecimal precioConDecimales = new BigDecimal("123.456789");
            BigDecimal importeConDecimales = new BigDecimal("370.370367");

            // Act
            detalleCotizacion.setPrecioBase(precioConDecimales);
            detalleCotizacion.setImporteCotizado(importeConDecimales);

            // Assert
            assertThat(detalleCotizacion.getPrecioBase()).isEqualByComparingTo(precioConDecimales);
            assertThat(detalleCotizacion.getImporteCotizado()).isEqualByComparingTo(importeConDecimales);
        }

        @Test
        @DisplayName("Debería mantener precision con cálculos complejos")
        void deberiaMantenerPrecisionEnCalculos() {
            // Arrange
            BigDecimal precio = new BigDecimal("33.333333");
            int cantidad = 3;
            BigDecimal importeCalculado = precio.multiply(BigDecimal.valueOf(cantidad));

            // Act
            detalleCotizacion.setPrecioBase(precio);
            detalleCotizacion.setCantidad(cantidad);
            detalleCotizacion.setImporteCotizado(importeCalculado);

            // Assert
            BigDecimal importeEsperado = new BigDecimal("99.999999");
            assertThat(detalleCotizacion.getImporteCotizado()).isEqualByComparingTo(importeEsperado);
        }

        @Test
        @DisplayName("Debería ser consistente en múltiples cálculos")
        void deberiaSerConsistenteEnMultiplesCalculos() {
            // Arrange
            BigDecimal precio = new BigDecimal("12.34");
            int cantidad = 5;

            // Act - Repetir cálculo varias veces
            BigDecimal resultado1 = precio.multiply(BigDecimal.valueOf(cantidad));
            BigDecimal resultado2 = precio.multiply(BigDecimal.valueOf(cantidad));
            BigDecimal resultado3 = precio.multiply(BigDecimal.valueOf(cantidad));

            // Assert - Todos los resultados deben ser idénticos
            assertThat(resultado1).isEqualByComparingTo(resultado2);
            assertThat(resultado2).isEqualByComparingTo(resultado3);

            BigDecimal esperado = new BigDecimal("61.70");
            assertThat(resultado1).isEqualByComparingTo(esperado);
        }

        @Test
        @DisplayName("Debería manejar BigDecimal con diferentes escalas")
        void deberiaManejarDiferentesEscalas() {
            // Arrange
            BigDecimal precio1 = new BigDecimal("100.5"); // escala 1
            BigDecimal precio2 = new BigDecimal("100.50"); // escala 2
            BigDecimal precio3 = new BigDecimal("100.500"); // escala 3

            // Act
            detalleCotizacion.setPrecioBase(precio1);
            BigDecimal precioObtenido = detalleCotizacion.getPrecioBase();

            // Assert - Deben ser iguales en valor aunque diferentes en escala
            assertThat(precioObtenido).isEqualByComparingTo(precio2);
            assertThat(precioObtenido).isEqualByComparingTo(precio3);
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases y Límites")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar cantidad muy grande")
        void deberiaManejarCantidadGrande() {
            // Arrange
            int cantidadGrande = Integer.MAX_VALUE - 1;

            // Act
            detalleCotizacion.setCantidad(cantidadGrande);

            // Assert
            assertThat(detalleCotizacion.getCantidad()).isEqualTo(cantidadGrande);
        }

        @Test
        @DisplayName("Debería manejar strings con caracteres especiales")
        void deberiaManejarCaracteresEspeciales() {
            // Arrange
            String idEspecial = "COMP-ñáéíóú@#$%";
            String descripcionEspecial = "Monitor 4K ñáéíóú \"quotes\" & <tags>";
            String categoriaEspecial = "MONITOR_ÑÁÉÍÓÚ";

            // Act
            detalleCotizacion.setIdComponente(idEspecial);
            detalleCotizacion.setDescripcion(descripcionEspecial);
            detalleCotizacion.setCategoria(categoriaEspecial);

            // Assert
            assertThat(detalleCotizacion.getIdComponente()).isEqualTo(idEspecial);
            assertThat(detalleCotizacion.getDescripcion()).isEqualTo(descripcionEspecial);
            assertThat(detalleCotizacion.getCategoria()).isEqualTo(categoriaEspecial);
        }

        @Test
        @DisplayName("Debería manejar strings muy largos")
        void deberiaManejarStringsMuyLargos() {
            // Arrange
            String stringLargo = "a".repeat(1000); // 1000 caracteres

            // Act
            detalleCotizacion.setIdComponente(stringLargo);
            detalleCotizacion.setDescripcion(stringLargo);
            detalleCotizacion.setCategoria(stringLargo);

            // Assert
            assertThat(detalleCotizacion.getIdComponente()).hasSize(1000);
            assertThat(detalleCotizacion.getDescripcion()).hasSize(1000);
            assertThat(detalleCotizacion.getCategoria()).hasSize(1000);
        }

        @Test
        @DisplayName("Debería manejar montos muy pequeños")
        void deberiaManejarMontosPequenos() {
            // Arrange
            BigDecimal montoPequeno = new BigDecimal("0.01");

            // Act
            detalleCotizacion.setPrecioBase(montoPequeno);
            detalleCotizacion.setImporteCotizado(montoPequeno);

            // Assert
            assertThat(detalleCotizacion.getPrecioBase()).isEqualByComparingTo(montoPequeno);
            assertThat(detalleCotizacion.getImporteCotizado()).isEqualByComparingTo(montoPequeno);
        }

        @Test
        @DisplayName("Debería manejar ceros en diferentes representaciones")
        void deberiaManejarDiferentesRepresentacionesCero() {
            // Arrange
            BigDecimal cero1 = BigDecimal.ZERO;
            BigDecimal cero2 = new BigDecimal("0");
            BigDecimal cero3 = new BigDecimal("0.00");
            BigDecimal cero4 = new BigDecimal("0.000");

            // Act & Assert
            detalleCotizacion.setPrecioBase(cero1);
            assertThat(detalleCotizacion.getPrecioBase()).isEqualByComparingTo(cero2);
            assertThat(detalleCotizacion.getPrecioBase()).isEqualByComparingTo(cero3);
            assertThat(detalleCotizacion.getPrecioBase()).isEqualByComparingTo(cero4);
        }
    }

    // ==================== TESTS DE CASOS DE USO REALES ====================

    @Nested
    @DisplayName("Casos de Uso Reales")
    class CasosUsoRealesTest {

        @Test
        @DisplayName("Debería representar un monitor correctamente")
        void deberiaRepresentarMonitorCorrectamente() {
            // Arrange & Act
            DetalleCotizacion monitor = new DetalleCotizacion(
                1,
                "MON-24-001",
                "Monitor LG 24 pulgadas Full HD IPS",
                1,
                new BigDecimal("299.99"),
                new BigDecimal("299.99"),
                "MONITOR"
            );

            // Assert
            assertThat(monitor.getIdComponente()).startsWith("MON-");
            assertThat(monitor.getDescripcion()).contains("Monitor");
            assertThat(monitor.getCategoria()).isEqualTo("MONITOR");
            assertThat(monitor.getCantidad()).isEqualTo(1);
        }

        @Test
        @DisplayName("Debería representar múltiples componentes con descuento")
        void deberiaRepresentarComponentesConDescuento() {
            // Arrange & Act
            DetalleCotizacion memorias = new DetalleCotizacion(
                2,
                "MEM-DDR4-001",
                "Memoria RAM DDR4 8GB 3200MHz",
                4, // 4 unidades
                new BigDecimal("75.00"), // precio unitario
                new BigDecimal("270.00"), // precio con descuento (4 x 75 = 300, descuento de 30)
                "MEMORIA"
            );

            // Assert
            assertThat(memorias.getCantidad()).isEqualTo(4);
            BigDecimal precioSinDescuento = memorias.getPrecioBase().multiply(BigDecimal.valueOf(memorias.getCantidad()));
            assertThat(precioSinDescuento).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(memorias.getImporteCotizado()).isLessThan(precioSinDescuento); // Con descuento
        }

        @Test
        @DisplayName("Debería representar componente de alto valor correctamente")
        void deberiaRepresentarComponenteAltoValor() {
            // Arrange & Act
            DetalleCotizacion tarjetaVideo = new DetalleCotizacion(
                3,
                "GPU-RTX4090-001",
                "NVIDIA GeForce RTX 4090 24GB GDDR6X",
                1,
                new BigDecimal("1599.99"),
                new BigDecimal("1599.99"),
                "TARJETA_VIDEO"
            );

            // Assert
            assertThat(tarjetaVideo.getPrecioBase()).isGreaterThan(new BigDecimal("1000"));
            assertThat(tarjetaVideo.getCategoria()).isEqualTo("TARJETA_VIDEO");
            assertThat(tarjetaVideo.getDescripcion()).contains("RTX");
        }
    }

    // ==================== MÉTODOS HELPER PARA TESTS ====================

    /**
     * Crea un detalle de cotización válido para usar en los tests.
     */
    private DetalleCotizacion crearDetalleCotizacionValido() {
        return new DetalleCotizacion(
            NUM_DETALLE_VALIDO,
            ID_COMPONENTE_VALIDO,
            DESCRIPCION_VALIDA,
            CANTIDAD_VALIDA,
            PRECIO_BASE_VALIDO,
            IMPORTE_COTIZADO_VALIDO,
            CATEGORIA_VALIDA
        );
    }
}