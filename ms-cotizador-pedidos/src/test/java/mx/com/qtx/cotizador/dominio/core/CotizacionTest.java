package mx.com.qtx.cotizador.dominio.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import mx.com.qtx.cotizador.dominio.ValidationUtils;

/**
 * Tests unitarios para la clase Cotizacion del dominio core.
 *
 * <p>Verifica la funcionalidad principal de la clase {@link Cotizacion} incluyendo:
 * <ul>
 * <li>Inicialización correcta y manejo de estado</li>
 * <li>Operaciones CRUD con detalles de cotización</li>
 * <li>Validaciones de entrada y reglas de negocio</li>
 * <li>Cálculos financieros y consistency de totales</li>
 * <li>Casos edge y manejo de errores</li>
 * <li>Funcionalidades de reporte y presentación</li>
 * </ul>
 * </p>
 *
 * <h3>Cobertura de testing:</h3>
 * <ul>
 * <li><strong>Constructor:</strong> Inicialización de campos y contador global</li>
 * <li><strong>Propiedades:</strong> Getters/setters de todos los campos</li>
 * <li><strong>Detalles:</strong> Agregar, obtener y manejar lista de detalles</li>
 * <li><strong>Validaciones:</strong> Usando ValidationUtils para validar parámetros</li>
 * <li><strong>Reportes:</strong> Generación de reportes formateados</li>
 * <li><strong>Edge cases:</strong> Límites, nulls, valores negativos</li>
 * </ul>
 *
 * @author Sistema Cotizador - Testing Team
 * @version 1.0
 * @since 2.0.0
 * @see Cotizacion
 * @see DetalleCotizacion
 * @see ValidationUtils
 */
@DisplayName("Cotizacion - Tests de Dominio Core")
class CotizacionTest {

    private Cotizacion cotizacion;
    private DetalleCotizacion detalleValido;

    @BeforeEach
    void setUp() {
        cotizacion = new Cotizacion();
        detalleValido = crearDetalleCotizacionValido();
    }

    // ==================== TESTS DE CONSTRUCTOR E INICIALIZACIÓN ====================

    @Nested
    @DisplayName("Constructor y Estado Inicial")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar correctamente con valores por defecto")
        void constructor_deberiaInicializarCorrectamente() {
            // Arrange & Act
            Cotizacion nuevaCotizacion = new Cotizacion();

            // Assert
            assertThat(nuevaCotizacion.getNum()).isPositive();
            assertThat(nuevaCotizacion.getFecha()).isEqualTo(LocalDate.now());
            assertThat(nuevaCotizacion.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(nuevaCotizacion.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(nuevaCotizacion.getDetalles()).isEmpty();
        }

        @Test
        @DisplayName("Debería asignar números secuenciales únicos")
        void constructor_deberiaAsignarNumerosUnicos() {
            // Arrange & Act
            Cotizacion cotizacion1 = new Cotizacion();
            Cotizacion cotizacion2 = new Cotizacion();
            Cotizacion cotizacion3 = new Cotizacion();

            // Assert
            assertThat(cotizacion2.getNum()).isGreaterThan(cotizacion1.getNum());
            assertThat(cotizacion3.getNum()).isGreaterThan(cotizacion2.getNum());

            // Verificar que son consecutivos
            assertThat(cotizacion2.getNum()).isEqualTo(cotizacion1.getNum() + 1);
            assertThat(cotizacion3.getNum()).isEqualTo(cotizacion2.getNum() + 1);
        }

        @Test
        @DisplayName("Debería inicializar con mapa de detalles vacío pero no null")
        void constructor_deberiaInicializarMapaDetallesVacio() {
            // Arrange & Act
            Cotizacion nuevaCotizacion = new Cotizacion();

            // Assert
            assertThat(nuevaCotizacion.getDetalles()).isNotNull();
            assertThat(nuevaCotizacion.getDetalles()).isEmpty();
        }
    }

    // ==================== TESTS DE GETTERS Y SETTERS ====================

    @Nested
    @DisplayName("Getters y Setters")
    class GettersSettersTest {

        @Test
        @DisplayName("Debería manejar número de cotización correctamente")
        void deberiaManearNumero() {
            // Arrange
            long numeroTest = 12345L;

            // Act
            cotizacion.setNum(numeroTest);

            // Assert
            assertThat(cotizacion.getNum()).isEqualTo(numeroTest);
        }

        @Test
        @DisplayName("Debería manejar fecha correctamente")
        void deberiaManejarFecha() {
            // Arrange
            LocalDate fechaTest = LocalDate.of(2024, 12, 25);

            // Act
            cotizacion.setFecha(fechaTest);

            // Assert
            assertThat(cotizacion.getFecha()).isEqualTo(fechaTest);
        }

        @Test
        @DisplayName("Debería manejar total correctamente")
        void deberiaManejarTotal() {
            // Arrange
            BigDecimal totalTest = new BigDecimal("1500.75");

            // Act
            cotizacion.setTotal(totalTest);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(totalTest);
        }

        @Test
        @DisplayName("Debería manejar total de impuestos correctamente")
        void deberiaManejarTotalImpuestos() {
            // Arrange
            BigDecimal impuestosTest = new BigDecimal("240.12");

            // Act
            cotizacion.setTotalImpuestos(impuestosTest);

            // Assert
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(impuestosTest);
        }
    }

    // ==================== TESTS DE MANEJO DE DETALLES ====================

    @Nested
    @DisplayName("Manejo de Detalles")
    class DetallesTest {

        @Test
        @DisplayName("Debería agregar detalle correctamente")
        void deberiaAgregarDetalle() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleCotizacionValido();

            // Act
            cotizacion.agregarDetalle(detalle);

            // Assert
            List<DetalleCotizacion> detalles = cotizacion.getDetalles();
            assertThat(detalles).hasSize(1);
            assertThat(detalles.get(0)).isEqualTo(detalle);
        }

        @Test
        @DisplayName("Debería agregar múltiples detalles")
        void deberiaAgregarMultiplesDetalles() {
            // Arrange
            DetalleCotizacion detalle1 = crearDetalleCotizacion(1, "COMP-001", "Monitor", 2, "500.00", "1000.00");
            DetalleCotizacion detalle2 = crearDetalleCotizacion(2, "COMP-002", "Teclado", 1, "100.00", "100.00");
            DetalleCotizacion detalle3 = crearDetalleCotizacion(3, "COMP-003", "Mouse", 1, "50.00", "50.00");

            // Act
            cotizacion.agregarDetalle(detalle1);
            cotizacion.agregarDetalle(detalle2);
            cotizacion.agregarDetalle(detalle3);

            // Assert
            List<DetalleCotizacion> detalles = cotizacion.getDetalles();
            assertThat(detalles).hasSize(3);
            assertThat(detalles).containsExactlyInAnyOrder(detalle1, detalle2, detalle3);
        }

        @Test
        @DisplayName("Debería reemplazar detalle con mismo número")
        void deberiaReemplazarDetalleConMismoNumero() {
            // Arrange
            DetalleCotizacion detalle1 = crearDetalleCotizacion(1, "COMP-001", "Monitor Original", 1, "500.00", "500.00");
            DetalleCotizacion detalle1Actualizado = crearDetalleCotizacion(1, "COMP-001", "Monitor Actualizado", 2, "500.00", "1000.00");

            // Act
            cotizacion.agregarDetalle(detalle1);
            cotizacion.agregarDetalle(detalle1Actualizado);

            // Assert
            List<DetalleCotizacion> detalles = cotizacion.getDetalles();
            assertThat(detalles).hasSize(1);
            assertThat(detalles.get(0).getDescripcion()).isEqualTo("Monitor Actualizado");
            assertThat(detalles.get(0).getCantidad()).isEqualTo(2);
        }

        @Test
        @DisplayName("Debería retornar nueva lista para proteger estado interno")
        void deberiaRetornarNuevaListaDeDetalles() {
            // Arrange
            cotizacion.agregarDetalle(detalleValido);

            // Act
            List<DetalleCotizacion> detalles1 = cotizacion.getDetalles();
            List<DetalleCotizacion> detalles2 = cotizacion.getDetalles();

            // Assert
            assertThat(detalles1).isNotSameAs(detalles2);
            assertThat(detalles1).containsExactlyElementsOf(detalles2);
        }
    }

    // ==================== TESTS DE VALIDACIONES ====================

    @Nested
    @DisplayName("Validaciones con ValidationUtils")
    class ValidacionesTest {

        @Test
        @DisplayName("Debería validar correctamente cotización válida")
        void deberiaValidarCotizacionValida() {
            // Arrange
            cotizacion.setFecha(LocalDate.now().minusDays(1));
            cotizacion.setTotal(new BigDecimal("1000.00"));
            cotizacion.setTotalImpuestos(new BigDecimal("160.00"));
            cotizacion.agregarDetalle(detalleValido);

            // Act & Assert - No debe lanzar excepción
            ValidationUtils.validateCotizacion(
                cotizacion.getFecha(),
                cotizacion.getTotal(),
                cotizacion.getTotalImpuestos(),
                cotizacion.getDetalles()
            );
        }

        @Test
        @DisplayName("Debería rechazar fecha futura")
        void deberiaRechazarFechaFutura() {
            // Arrange
            LocalDate fechaFutura = LocalDate.now().plusDays(1);

            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateFechaCotizacion(fechaFutura, "fecha");
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("fecha futura");
        }

        @Test
        @DisplayName("Debería rechazar fecha null")
        void deberiaRechazarFechaNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateFechaCotizacion(null, "fecha");
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser null");
        }

        @Test
        @DisplayName("Debería rechazar total negativo")
        void deberiaRechazarTotalNegativo() {
            // Arrange
            BigDecimal totalNegativo = new BigDecimal("-100.00");

            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateMontoFinanciero(totalNegativo, "total");
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativo");
        }

        @Test
        @DisplayName("Debería rechazar total null")
        void deberiaRechazarTotalNull() {
            // Act & Assert
            assertThatThrownBy(() -> {
                ValidationUtils.validateMontoFinanciero(null, "total");
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser null");
        }

        @Test
        @DisplayName("Debería rechazar impuestos mayores que total")
        void deberiaRechazarImpuestosMayoresQueTotal() {
            // Arrange
            BigDecimal total = new BigDecimal("100.00");
            BigDecimal impuestos = new BigDecimal("150.00");
            BigDecimal subtotal = total.subtract(impuestos); // subtotal será negativo

            // Act & Assert - El subtotal negativo debe ser rechazado
            assertThatThrownBy(() -> {
                ValidationUtils.validateConsistenciaTotales(
                    subtotal,
                    impuestos,
                    total
                );
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativo");
        }

        @Test
        @DisplayName("Debería validar número de cotización positivo")
        void deberiaValidarNumeroCotizacionPositivo() {
            // Act & Assert - Número positivo válido
            ValidationUtils.validateNumeroCotizacion(123L);

            // Número cero o negativo inválido
            assertThatThrownBy(() -> {
                ValidationUtils.validateNumeroCotizacion(0L);
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");

            assertThatThrownBy(() -> {
                ValidationUtils.validateNumeroCotizacion(-5L);
            })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");
        }
    }

    // ==================== TESTS DE CÁLCULOS Y LÓGICA DE NEGOCIO ====================

    @Nested
    @DisplayName("Cálculos Financieros")
    class CalculosTest {

        @Test
        @DisplayName("Debería mantener consistency entre subtotal, impuestos y total")
        void deberiaMantenerConsistenciaEntreMontos() {
            // Arrange
            BigDecimal subtotal = new BigDecimal("1000.00");
            BigDecimal impuestos = new BigDecimal("160.00");
            BigDecimal total = new BigDecimal("1160.00");

            cotizacion.setTotal(total);
            cotizacion.setTotalImpuestos(impuestos);

            // Act & Assert
            ValidationUtils.validateConsistenciaTotales(subtotal, impuestos, total);
        }

        @Test
        @DisplayName("Debería calcular subtotal correctamente")
        void deberiaCalcularSubtotalCorrectamente() {
            // Arrange
            BigDecimal total = new BigDecimal("1160.00");
            BigDecimal impuestos = new BigDecimal("160.00");
            BigDecimal subtotalEsperado = new BigDecimal("1000.00");

            cotizacion.setTotal(total);
            cotizacion.setTotalImpuestos(impuestos);

            // Act
            BigDecimal subtotalCalculado = cotizacion.getTotal().subtract(cotizacion.getTotalImpuestos());

            // Assert
            assertThat(subtotalCalculado).isEqualByComparingTo(subtotalEsperado);
        }

        @Test
        @DisplayName("Debería manejar impuestos cero correctamente")
        void deberiaManejarImpuestosCero() {
            // Arrange
            BigDecimal total = new BigDecimal("1000.00");
            BigDecimal impuestosCero = BigDecimal.ZERO;

            cotizacion.setTotal(total);
            cotizacion.setTotalImpuestos(impuestosCero);

            // Act & Assert
            ValidationUtils.validateConsistenciaTotales(total, impuestosCero, total);

            BigDecimal subtotal = cotizacion.getTotal().subtract(cotizacion.getTotalImpuestos());
            assertThat(subtotal).isEqualByComparingTo(total);
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases y Límites")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar montos con muchos decimales")
        void deberiaManejarMontosConMuchosDecimales() {
            // Arrange
            BigDecimal totalPreciso = new BigDecimal("1234.123456789");
            BigDecimal impuestosPreciso = new BigDecimal("197.459876543");

            // Act
            cotizacion.setTotal(totalPreciso);
            cotizacion.setTotalImpuestos(impuestosPreciso);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(totalPreciso);
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(impuestosPreciso);
        }

        @Test
        @DisplayName("Debería manejar montos muy grandes")
        void deberiaManejarMontosGrandes() {
            // Arrange
            BigDecimal montoGrande = new BigDecimal("999999999999.99");

            // Act
            cotizacion.setTotal(montoGrande);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(montoGrande);
        }

        @Test
        @DisplayName("Debería manejar fecha límite (hoy)")
        void deberiaManejarFechaLimiteHoy() {
            // Arrange
            LocalDate hoy = LocalDate.now();

            // Act
            cotizacion.setFecha(hoy);

            // Assert
            assertThat(cotizacion.getFecha()).isEqualTo(hoy);
            // No debe lanzar excepción en validación
            ValidationUtils.validateFechaCotizacion(hoy, "fecha");
        }

        @Test
        @DisplayName("Debería manejar gran cantidad de detalles")
        void deberiaManejarGranCantidadDeDetalles() {
            // Arrange & Act
            for (int i = 1; i <= 1000; i++) {
                DetalleCotizacion detalle = crearDetalleCotizacion(
                    i, "COMP-" + i, "Componente " + i, 1, "10.00", "10.00");
                cotizacion.agregarDetalle(detalle);
            }

            // Assert
            assertThat(cotizacion.getDetalles()).hasSize(1000);
        }
    }

    // ==================== TESTS DE FUNCIONALIDADES ADICIONALES ====================

    @Nested
    @DisplayName("Funcionalidades de Reporte")
    class ReporteTest {

        @Test
        @DisplayName("Debería generar reporte sin errores con detalles")
        void deberiaGenerarReporteSinErrores() {
            // Arrange
            cotizacion.agregarDetalle(detalleValido);
            cotizacion.setTotal(new BigDecimal("1160.00"));
            cotizacion.setTotalImpuestos(new BigDecimal("160.00"));

            // Act & Assert - No debe lanzar excepción
            cotizacion.emitirComoReporte();
        }

        @Test
        @DisplayName("Debería generar reporte sin errores con múltiples detalles")
        void deberiaGenerarReporteConMultiplesDetalles() {
            // Arrange
            cotizacion.agregarDetalle(crearDetalleCotizacion(1, "COMP-001", "Monitor", 1, "500.00", "500.00"));
            cotizacion.agregarDetalle(crearDetalleCotizacion(2, "COMP-002", "Teclado", 2, "50.00", "100.00"));
            cotizacion.agregarDetalle(crearDetalleCotizacion(3, "COMP-003", "Mouse", 1, "30.00", "30.00"));
            cotizacion.setTotal(new BigDecimal("730.80"));
            cotizacion.setTotalImpuestos(new BigDecimal("100.80"));

            // Act & Assert - No debe lanzar excepción
            cotizacion.emitirComoReporte();
        }
    }

    // ==================== MÉTODOS HELPER PARA TESTS ====================

    /**
     * Crea un detalle de cotización válido para usar en los tests.
     */
    private DetalleCotizacion crearDetalleCotizacionValido() {
        return crearDetalleCotizacion(1, "COMP-001", "Monitor 24 pulgadas", 1, "500.00", "500.00");
    }

    /**
     * Crea un detalle de cotización con parámetros específicos.
     */
    private DetalleCotizacion crearDetalleCotizacion(int numDetalle, String idComponente,
                                                   String descripcion, int cantidad,
                                                   String precioBase, String importeCotizado) {
        return new DetalleCotizacion(
            numDetalle,
            idComponente,
            descripcion,
            cantidad,
            new BigDecimal(precioBase),
            new BigDecimal(importeCotizado),
            "MONITOR"
        );
    }
}