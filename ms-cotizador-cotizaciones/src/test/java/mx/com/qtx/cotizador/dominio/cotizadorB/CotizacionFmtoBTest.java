package mx.com.qtx.cotizador.dominio.cotizadorB;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;

/**
 * Tests unitarios para la clase CotizacionFmtoB del dominio cotizadorB.
 *
 * <p>Verifica la funcionalidad específica de la clase {@link CotizacionFmtoB} incluyendo:
 * <ul>
 * <li>Herencia correcta de la clase base Cotizacion</li>
 * <li>Formato específico de reporte tabular profesional</li>
 * <li>Sobrescritura correcta del método emitirComoReporte()</li>
 * <li>Formato correcto de líneas individuales de cotización</li>
 * <li>Manejo de múltiples detalles con formato consistente</li>
 * <li>Captura y validación de salida de consola</li>
 * </ul>
 * </p>
 *
 * <h3>Cobertura de testing:</h3>
 * <ul>
 * <li><strong>Constructor:</strong> Herencia e inicialización correcta</li>
 * <li><strong>Reporte básico:</strong> Formato de encabezados y estructura</li>
 * <li><strong>Detalles únicos:</strong> Formato de línea individual</li>
 * <li><strong>Detalles múltiples:</strong> Consistencia en formato tabular</li>
 * <li><strong>Totales:</strong> Formato correcto de subtotal, impuestos y total</li>
 * <li><strong>Edge cases:</strong> Cotizaciones sin detalles, valores grandes</li>
 * </ul>
 *
 * <h3>Validación de formato de salida:</h3>
 * <p>Los tests capturan la salida de consola para validar que el formato
 * cumple con las especificaciones exactas del reporte tabular.</p>
 *
 * @author Sistema Cotizador - Testing Team
 * @version 1.0
 * @since 2.0.0
 * @see CotizacionFmtoB
 * @see mx.com.qtx.cotizador.dominio.core.Cotizacion
 * @see DetalleCotizacion
 */
@DisplayName("CotizacionFmtoB - Tests de Dominio CotizadorB")
class CotizacionFmtoBTest {

    private CotizacionFmtoB cotizacionFmtoB;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        cotizacionFmtoB = new CotizacionFmtoB();

        // Configurar captura de salida de consola
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        // Restaurar salida de consola original
        System.setOut(originalOut);
    }

    // ==================== TESTS DE CONSTRUCTOR Y HERENCIA ====================

    @Nested
    @DisplayName("Constructor y Herencia")
    class ConstructorHerenciaTest {

        @Test
        @DisplayName("Debería extender correctamente de Cotizacion")
        void deberiaExtenderCorrectamenteDeCotizacion() {
            // Act & Assert
            assertThat(cotizacionFmtoB).isInstanceOf(mx.com.qtx.cotizador.dominio.core.Cotizacion.class);
        }

        @Test
        @DisplayName("Debería inicializar con valores heredados correctos")
        void deberiaInicializarConValoresHeredados() {
            // Assert
            assertThat(cotizacionFmtoB.getNum()).isPositive();
            assertThat(cotizacionFmtoB.getFecha()).isEqualTo(LocalDate.now());
            assertThat(cotizacionFmtoB.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(cotizacionFmtoB.getDetalles()).isEmpty();
        }

        @Test
        @DisplayName("Debería mantener funcionalidad base de agregar detalles")
        void deberiaMantenerFuncionalidadBaseAgregarDetalles() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(1, "COMP-001", "Monitor", 1, "500.00", "500.00");

            // Act
            cotizacionFmtoB.agregarDetalle(detalle);

            // Assert
            assertThat(cotizacionFmtoB.getDetalles()).hasSize(1);
            assertThat(cotizacionFmtoB.getDetalles().get(0)).isEqualTo(detalle);
        }
    }

    // ==================== TESTS DE FORMATO DE REPORTE ====================

    @Nested
    @DisplayName("Formato de Reporte")
    class FormatoReporteTest {

        @Test
        @DisplayName("Debería generar encabezado correcto sin detalles")
        void deberiaGenerarEncabezadoCorrectoSinDetalles() {
            // Arrange
            cotizacionFmtoB.setTotal(BigDecimal.ZERO);
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("===========================================================================================");
            assertThat(output).contains("Cotizacion número:" + cotizacionFmtoB.getNum());
            assertThat(output).contains("Fecha:" + cotizacionFmtoB.getFecha());
            assertThat(output).contains("#");
            assertThat(output).contains("Cantidad");
            assertThat(output).contains("Id");
            assertThat(output).contains("Descripcion");
            assertThat(output).contains("Base");
            assertThat(output).contains("Total");
        }

        @Test
        @DisplayName("Debería generar formato correcto con un detalle")
        void deberiaGenerarFormatoCorrectoConUnDetalle() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(1, "MON-001", "Monitor 24 pulgadas", 1, "500.00", "500.00");
            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("580.00"));
            cotizacionFmtoB.setTotalImpuestos(new BigDecimal("80.00"));

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();

            // Verificar línea de detalle
            assertThat(output).contains("1"); // número de detalle
            assertThat(output).contains("MON-001");
            assertThat(output).contains("Monitor 24 pulgadas");
            assertThat(output).contains("500.00");

            // Verificar totales
            assertThat(output).contains("Subtotal:");
            assertThat(output).contains("Impuestos:");
            assertThat(output).contains("Total:");
            assertThat(output).contains("500.00"); // subtotal
            assertThat(output).contains("80.00"); // impuestos
            assertThat(output).contains("580.00"); // total
        }

        @Test
        @DisplayName("Debería generar formato correcto con múltiples detalles")
        void deberiaGenerarFormatoCorrectoConMultiplesDetalles() {
            // Arrange
            DetalleCotizacion detalle1 = crearDetalleValido(1, "MON-001", "Monitor 24\"", 1, "500.00", "500.00");
            DetalleCotizacion detalle2 = crearDetalleValido(2, "CPU-001", "Procesador Intel i7", 1, "400.00", "400.00");
            DetalleCotizacion detalle3 = crearDetalleValido(3, "MEM-001", "Memoria RAM 8GB", 2, "100.00", "200.00");

            cotizacionFmtoB.agregarDetalle(detalle1);
            cotizacionFmtoB.agregarDetalle(detalle2);
            cotizacionFmtoB.agregarDetalle(detalle3);

            cotizacionFmtoB.setTotal(new BigDecimal("1276.00"));
            cotizacionFmtoB.setTotalImpuestos(new BigDecimal("176.00"));

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();

            // Verificar todos los detalles
            assertThat(output).contains("MON-001");
            assertThat(output).contains("CPU-001");
            assertThat(output).contains("MEM-001");
            assertThat(output).contains("Monitor 24\"");
            assertThat(output).contains("Procesador Intel i7");
            assertThat(output).contains("Memoria RAM 8GB");

            // Verificar cantidades
            assertThat(output).contains("1"); // cantidad monitor y CPU
            assertThat(output).contains("2"); // cantidad memoria

            // Verificar totales
            assertThat(output).contains("1100.00"); // subtotal (500+400+200)
            assertThat(output).contains("176.00"); // impuestos
            assertThat(output).contains("1276.00"); // total
        }
    }

    // ==================== TESTS DE FORMATO DE LÍNEAS ====================

    @Nested
    @DisplayName("Formato de Líneas Individuales")
    class FormatoLineasTest {

        @Test
        @DisplayName("Debería formatear correctamente línea con precios enteros")
        void deberiaFormatearCorrectamenteLineaConPreciosEnteros() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(5, "COMP-123", "Componente Test", 3, "100.00", "300.00");
            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("300.00"));
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            String[] lines = output.split("\n");

            // Buscar la línea del detalle (debe contener el número de detalle)
            String detalleLinea = null;
            for (String line : lines) {
                if (line.contains("COMP-123")) {
                    detalleLinea = line;
                    break;
                }
            }

            assertThat(detalleLinea).isNotNull();
            assertThat(detalleLinea).contains("5"); // número de detalle
            assertThat(detalleLinea).contains("3"); // cantidad
            assertThat(detalleLinea).contains("COMP-123");
            assertThat(detalleLinea).contains("Componente Test");
            assertThat(detalleLinea).contains("100.00");
            assertThat(detalleLinea).contains("300.00");
        }

        @Test
        @DisplayName("Debería formatear correctamente línea con precios decimales")
        void deberiaFormatearCorrectamenteLineaConPreciosDecimales() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(1, "DEC-001", "Componente Decimal", 1, "123.45", "123.45");
            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("123.45"));
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("123.45");
            assertThat(output).contains("DEC-001");
            assertThat(output).contains("Componente Decimal");
        }

        @Test
        @DisplayName("Debería manejar IDs y descripciones largas sin romper formato")
        void deberiaManejarTextoLargoSinRomperFormato() {
            // Arrange
            String idLargo = "COMPONENTE-LARGO-ID-123";
            String descripcionLarga = "Descripción muy larga de componente que podría afectar formato";
            DetalleCotizacion detalle = crearDetalleValido(1, idLargo, descripcionLarga, 1, "999.99", "999.99");

            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("999.99"));
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains(idLargo);
            assertThat(output).contains("999.99");
            // La descripción larga se truncará pero no debe romper el formato
            assertThat(output).contains("Descripción muy larga de comp"); // parte de la descripción
        }
    }

    // ==================== TESTS DE TOTALES Y CÁLCULOS ====================

    @Nested
    @DisplayName("Formato de Totales")
    class FormatoTotalesTest {

        @Test
        @DisplayName("Debería mostrar subtotal, impuestos y total correctamente")
        void deberiaMostrarTotalesCorrectamente() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(1, "COMP-001", "Componente", 1, "1000.00", "1000.00");
            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("1160.00"));
            cotizacionFmtoB.setTotalImpuestos(new BigDecimal("160.00"));

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            String[] lines = output.split("\n");

            // Verificar líneas de totales (están al final)
            boolean foundSubtotal = false, foundImpuestos = false, foundTotal = false;

            for (String line : lines) {
                if (line.contains("Subtotal:") && line.contains("1000.00")) {
                    foundSubtotal = true;
                }
                if (line.contains("Impuestos:") && line.contains("160.00")) {
                    foundImpuestos = true;
                }
                if (line.contains("Total:") && line.contains("1160.00")) {
                    foundTotal = true;
                }
            }

            assertThat(foundSubtotal).isTrue();
            assertThat(foundImpuestos).isTrue();
            assertThat(foundTotal).isTrue();
        }

        @Test
        @DisplayName("Debería manejar impuestos cero correctamente")
        void deberiaManejarImpuestosCeroCorrectamente() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(1, "COMP-001", "Componente", 1, "500.00", "500.00");
            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("500.00"));
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("Subtotal:");
            assertThat(output).contains("500.00");
            assertThat(output).contains("Impuestos:");
            assertThat(output).contains("0.00");
            assertThat(output).contains("Total:");
        }

        @Test
        @DisplayName("Debería formatear correctamente montos grandes")
        void deberiaFormatearCorrectamenteMontosGrandes() {
            // Arrange
            DetalleCotizacion detalle = crearDetalleValido(1, "COMP-001", "Componente Caro", 1, "999999.99", "999999.99");
            cotizacionFmtoB.agregarDetalle(detalle);
            cotizacionFmtoB.setTotal(new BigDecimal("1099999.99"));
            cotizacionFmtoB.setTotalImpuestos(new BigDecimal("100000.00"));

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("999999.99");
            assertThat(output).contains("1099999.99");
            assertThat(output).contains("100000.00");
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería generar reporte correcto con cotización vacía")
        void deberiaGenerarReporteCorrectoConCotizacionVacia() {
            // Arrange
            cotizacionFmtoB.setTotal(BigDecimal.ZERO);
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("Cotizacion número:");
            assertThat(output).contains("Fecha:");
            assertThat(output).contains("Subtotal:");
            assertThat(output).contains("0.00");
        }

        @Test
        @DisplayName("Debería manejar fecha personalizada en reporte")
        void deberiaManejarFechaPersonalizadaEnReporte() {
            // Arrange
            LocalDate fechaPersonalizada = LocalDate.of(2024, 12, 25);
            cotizacionFmtoB.setFecha(fechaPersonalizada);
            cotizacionFmtoB.setTotal(BigDecimal.ZERO);
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("2024-12-25");
        }

        @Test
        @DisplayName("Debería funcionar con número de cotización muy grande")
        void deberiaFuncionarConNumeroCotizacionGrande() {
            // Arrange
            long numeroGrande = 999999999L;
            cotizacionFmtoB.setNum(numeroGrande);
            cotizacionFmtoB.setTotal(BigDecimal.ZERO);
            cotizacionFmtoB.setTotalImpuestos(BigDecimal.ZERO);

            // Act
            cotizacionFmtoB.emitirComoReporte();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("999999999");
        }
    }

    // ==================== TESTS DE COMPATIBILIDAD ====================

    @Nested
    @DisplayName("Compatibilidad con Clase Base")
    class CompatibilidadClaseBaseTest {

        @Test
        @DisplayName("Debería mantener funcionalidad de setters heredados")
        void deberiaMantenerFuncionalidadSettersHeredados() {
            // Arrange
            LocalDate nuevaFecha = LocalDate.of(2024, 6, 15);
            BigDecimal nuevoTotal = new BigDecimal("1500.00");
            BigDecimal nuevosImpuestos = new BigDecimal("240.00");

            // Act
            cotizacionFmtoB.setFecha(nuevaFecha);
            cotizacionFmtoB.setTotal(nuevoTotal);
            cotizacionFmtoB.setTotalImpuestos(nuevosImpuestos);

            // Assert
            assertThat(cotizacionFmtoB.getFecha()).isEqualTo(nuevaFecha);
            assertThat(cotizacionFmtoB.getTotal()).isEqualByComparingTo(nuevoTotal);
            assertThat(cotizacionFmtoB.getTotalImpuestos()).isEqualByComparingTo(nuevosImpuestos);
        }

        @Test
        @DisplayName("Debería trabajar correctamente con polimorfismo")
        void deberiaTrabajjarCorrectamenteConPolimorfismo() {
            // Arrange
            mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionPolimorfica = new CotizacionFmtoB();
            DetalleCotizacion detalle = crearDetalleValido(1, "COMP-001", "Test", 1, "100.00", "100.00");

            // Act
            cotizacionPolimorfica.agregarDetalle(detalle);

            // Assert
            assertThat(cotizacionPolimorfica.getDetalles()).hasSize(1);
            assertThat(cotizacionPolimorfica).isInstanceOf(CotizacionFmtoB.class);
        }
    }

    // ==================== MÉTODOS HELPER PARA TESTS ====================

    /**
     * Crea un detalle de cotización válido para usar en los tests.
     */
    private DetalleCotizacion crearDetalleValido(int numDetalle, String idComponente,
                                               String descripcion, int cantidad,
                                               String precioBase, String importeCotizado) {
        return new DetalleCotizacion(
            numDetalle,
            idComponente,
            descripcion,
            cantidad,
            new BigDecimal(precioBase),
            new BigDecimal(importeCotizado),
            "MONITOR" // categoría por defecto
        );
    }
}