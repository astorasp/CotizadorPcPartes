package mx.com.qtx.cotizador.dominio.cotizadorB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;

/**
 * Tests unitarios para la clase CotizadorConMap del dominio cotizadorB.
 *
 * <p>Verifica la funcionalidad específica de la implementación Map-based de ICotizador,
 * incluyendo:
 * <ul>
 * <li>Operaciones CRUD con estructura HashMap eficiente</li>
 * <li>Generación de cotizaciones usando CotizacionFmtoB</li>
 * <li>Cálculos correctos con múltiples componentes e impuestos</li>
 * <li>Manejo de edge cases y validaciones de error</li>
 * <li>Performance superior comparado con listas paralelas</li>
 * <li>Integración correcta con el patrón Strategy</li>
 * </ul>
 * </p>
 *
 * <h3>Cobertura de testing:</h3>
 * <ul>
 * <li><strong>Constructor:</strong> Inicialización correcta del HashMap</li>
 * <li><strong>Agregar:</strong> Componentes nuevos y reemplazo de existentes</li>
 * <li><strong>Eliminar:</strong> Por ID con manejo de errores</li>
 * <li><strong>Generar:</strong> Cotizaciones completas con impuestos</li>
 * <li><strong>Listar:</strong> Salida formateada de componentes</li>
 * <li><strong>Edge cases:</strong> Mapa vacío, IDs inválidos, nulls</li>
 * </ul>
 *
 * <h3>Mocks utilizados:</h3>
 * <p>Utiliza mocks de Mockito para simular {@link Componente} y {@link CalculadorImpuesto}
 * sin dependencias reales, permitiendo tests unitarios puros y predecibles.</p>
 *
 * @author Sistema Cotizador - Testing Team
 * @version 1.0
 * @since 2.0.0
 * @see CotizadorConMap
 * @see mx.com.qtx.cotizador.dominio.core.ICotizador
 * @see CotizacionFmtoB
 */
@DisplayName("CotizadorConMap - Tests de Dominio CotizadorB")
class CotizadorConMapTest {

    private CotizadorConMap cotizadorConMap;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable closeable;

    @Mock
    private Componente componenteMock1;

    @Mock
    private Componente componenteMock2;

    @Mock
    private Componente componenteMock3;

    @Mock
    private CalculadorImpuesto calculadorImpuestoMock;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        cotizadorConMap = new CotizadorConMap();

        // Configurar captura de salida de consola
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Configurar mocks de componentes
        configurarComponenteMock1();
        configurarComponenteMock2();
        configurarComponenteMock3();
        configurarCalculadorImpuestoMock();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restaurar salida de consola original
        System.setOut(originalOut);
        closeable.close();
    }

    // ==================== TESTS DE CONSTRUCTOR E INICIALIZACIÓN ====================

    @Nested
    @DisplayName("Constructor e Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar con HashMap vacío")
        void deberiaInicializarConHashMapVacio() {
            // Arrange & Act
            CotizadorConMap nuevoCotizador = new CotizadorConMap();

            // Assert - Verificar que se puede usar inmediatamente
            nuevoCotizador.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("CotizadorConMap");
        }

        @Test
        @DisplayName("Debería ser instancia de ICotizador")
        void deberiaSerInstanciaDeICotizador() {
            // Assert
            assertThat(cotizadorConMap).isInstanceOf(mx.com.qtx.cotizador.dominio.core.ICotizador.class);
        }
    }

    // ==================== TESTS DE AGREGAR COMPONENTE ====================

    @Nested
    @DisplayName("Agregar Componente")
    class AgregarComponenteTest {

        @Test
        @DisplayName("Debería agregar componente correctamente")
        void deberiaAgregarComponenteCorrectamente() {
            // Act
            cotizadorConMap.agregarComponente(2, componenteMock1);

            // Assert - Verificar que se agregó listando componentes
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("Monitor 24 pulgadas");
            assertThat(output).contains("2"); // cantidad
            assertThat(output).contains("500.00"); // precio
            assertThat(output).contains("MON-001"); // ID
        }

        @Test
        @DisplayName("Debería agregar múltiples componentes distintos")
        void deberiaAgregarMultiplesComponentesDistintos() {
            // Act
            cotizadorConMap.agregarComponente(1, componenteMock1);
            cotizadorConMap.agregarComponente(2, componenteMock2);
            cotizadorConMap.agregarComponente(1, componenteMock3);

            // Assert
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("Monitor 24 pulgadas");
            assertThat(output).contains("Procesador Intel i7");
            assertThat(output).contains("Memoria RAM 16GB");
        }

        @Test
        @DisplayName("Debería reemplazar cantidad de componente existente")
        void deberiaReemplazarCantidadComponenteExistente() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1);

            // Act - Agregar mismo componente con diferente cantidad
            cotizadorConMap.agregarComponente(5, componenteMock1);

            // Assert
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("5 Monitor 24 pulgadas"); // Nueva cantidad
            // No debe aparecer la cantidad anterior (1)
            long countMonitor = output.lines()
                .filter(line -> line.contains("Monitor 24 pulgadas"))
                .count();
            assertThat(countMonitor).isEqualTo(1); // Solo una entrada
        }

        @Test
        @DisplayName("Debería manejar cantidad cero")
        void deberiaManejarCantidadCero() {
            // Act
            cotizadorConMap.agregarComponente(0, componenteMock1);

            // Assert
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("0 Monitor 24 pulgadas");
        }

        @Test
        @DisplayName("Debería manejar cantidades grandes")
        void deberiaManejarCantidadesGrandes() {
            // Arrange
            int cantidadGrande = 999999;

            // Act
            cotizadorConMap.agregarComponente(cantidadGrande, componenteMock1);

            // Assert
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("999999");
        }
    }

    // ==================== TESTS DE ELIMINAR COMPONENTE ====================

    @Nested
    @DisplayName("Eliminar Componente")
    class EliminarComponenteTest {

        @Test
        @DisplayName("Debería eliminar componente existente por ID")
        void deberiaEliminarComponenteExistentePorId() {
            // Arrange
            cotizadorConMap.agregarComponente(2, componenteMock1);
            cotizadorConMap.agregarComponente(1, componenteMock2);

            // Act
            cotizadorConMap.eliminarComponente("MON-001");

            // Assert
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).doesNotContain("Monitor 24 pulgadas");
            assertThat(output).contains("Procesador Intel i7"); // Este debe seguir
        }

        @Test
        @DisplayName("Debería eliminar correctamente cuando hay múltiples componentes")
        void deberiaEliminarCorrectamenteConMultiplesComponentes() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1);
            cotizadorConMap.agregarComponente(2, componenteMock2);
            cotizadorConMap.agregarComponente(1, componenteMock3);

            // Act
            cotizadorConMap.eliminarComponente("CPU-002"); // Eliminar el del medio

            // Assert
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("Monitor 24 pulgadas");
            assertThat(output).doesNotContain("Procesador Intel i7");
            assertThat(output).contains("Memoria RAM 16GB");
        }

        @Test
        @DisplayName("Debería lanzar excepción si componente no existe")
        void deberiaLanzarExcepcionSiComponenteNoExiste() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1);

            // Act & Assert
            assertThatThrownBy(() -> {
                cotizadorConMap.eliminarComponente("ID-INEXISTENTE");
            })
            .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Debería lanzar excepción si cotizador está vacío")
        void deberiaLanzarExcepcionSiCotizadorEstaVacio() {
            // Act & Assert
            assertThatThrownBy(() -> {
                cotizadorConMap.eliminarComponente("CUALQUIER-ID");
            })
            .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Debería manejar null como ID correctamente")
        void deberiaManejarNullComoIdCorrectamente() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1);

            // Act & Assert
            assertThatThrownBy(() -> {
                cotizadorConMap.eliminarComponente(null);
            })
            .isInstanceOf(Exception.class); // NullPointerException o NoSuchElementException
        }
    }

    // ==================== TESTS DE GENERAR COTIZACIÓN ====================

    @Nested
    @DisplayName("Generar Cotización")
    class GenerarCotizacionTest {

        @Test
        @DisplayName("Debería generar cotización vacía sin componentes")
        void deberiaGenerarCotizacionVaciaSinComponentes() {
            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(null);

            // Assert
            assertThat(cotizacion).isInstanceOf(CotizacionFmtoB.class);
            assertThat(cotizacion.getDetalles()).isEmpty();
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería generar cotización con un componente sin impuestos")
        void deberiaGenerarCotizacionConUnComponenteSinImpuestos() {
            // Arrange
            cotizadorConMap.agregarComponente(2, componenteMock1);

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(null);

            // Assert
            assertThat(cotizacion).isInstanceOf(CotizacionFmtoB.class);
            assertThat(cotizacion.getDetalles()).hasSize(1);
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(new BigDecimal("1000.00")); // 2 * 500
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);

            // Verificar detalle
            DetalleCotizacion detalle = cotizacion.getDetalles().get(0);
            assertThat(detalle.getIdComponente()).isEqualTo("MON-001");
            assertThat(detalle.getDescripcion()).isEqualTo("Monitor 24 pulgadas");
            assertThat(detalle.getCantidad()).isEqualTo(2);
            assertThat(detalle.getPrecioBase()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Debería generar cotización con múltiples componentes sin impuestos")
        void deberiaGenerarCotizacionConMultiplesComponentesSinImpuestos() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1); // 500.00
            cotizadorConMap.agregarComponente(1, componenteMock2); // 400.00
            cotizadorConMap.agregarComponente(2, componenteMock3); // 2 * 150.00 = 300.00

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(null);

            // Assert
            assertThat(cotizacion.getDetalles()).hasSize(3);
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(new BigDecimal("1200.00")); // 500+400+300
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);

            // Verificar que todos los componentes están incluidos
            List<String> ids = cotizacion.getDetalles().stream()
                .map(DetalleCotizacion::getIdComponente)
                .toList();
            assertThat(ids).containsExactlyInAnyOrder("MON-001", "CPU-002", "MEM-003");
        }

        @Test
        @DisplayName("Debería generar cotización con un componente e impuesto")
        void deberiaGenerarCotizacionConUnComponenteEImpuesto() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1); // 500.00
            List<CalculadorImpuesto> impuestos = Arrays.asList(calculadorImpuestoMock); // 16% = 80.00

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(impuestos);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(new BigDecimal("580.00")); // 500 + 80
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(new BigDecimal("80.00"));

            // Verificar que el calculador fue llamado con el subtotal correcto
            verify(calculadorImpuestoMock).calcularImpuesto(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Debería generar cotización con múltiples impuestos")
        void deberiaGenerarCotizacionConMultiplesImpuestos() {
            // Arrange
            CalculadorImpuesto impuesto2Mock = mock(CalculadorImpuesto.class);
            when(impuesto2Mock.calcularImpuesto(any(BigDecimal.class))).thenReturn(new BigDecimal("25.00"));

            cotizadorConMap.agregarComponente(1, componenteMock1); // 500.00
            List<CalculadorImpuesto> impuestos = Arrays.asList(calculadorImpuestoMock, impuesto2Mock);

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(impuestos);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(new BigDecimal("605.00")); // 500 + 80 + 25
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(new BigDecimal("105.00")); // 80 + 25

            // Verificar que ambos calculadores fueron llamados
            // (el primero con 500.00, el segundo con el total actualizado)
            verify(calculadorImpuestoMock).calcularImpuesto(new BigDecimal("500.00"));
            verify(impuesto2Mock).calcularImpuesto(any(BigDecimal.class)); // No importa la escala exacta
        }

        @Test
        @DisplayName("Debería manejar lista vacía de impuestos")
        void deberiaManejarListaVaciaDeImpuestos() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1);
            List<CalculadorImpuesto> impuestosVacios = Collections.emptyList();

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(impuestosVacios);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ==================== TESTS DE LISTAR COMPONENTES ====================

    @Nested
    @DisplayName("Listar Componentes")
    class ListarComponentesTest {

        @Test
        @DisplayName("Debería mostrar encabezado cuando no hay componentes")
        void deberiaMostrarEncabezadoCuandoNoHayComponentes() {
            // Act
            cotizadorConMap.listarComponentes();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("=== Componentes a cotizar en CotizadorConMap ===");
        }

        @Test
        @DisplayName("Debería listar un componente correctamente")
        void deberiaListarUnComponenteCorrectamente() {
            // Arrange
            cotizadorConMap.agregarComponente(3, componenteMock1);

            // Act
            cotizadorConMap.listarComponentes();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("CotizadorConMap");
            assertThat(output).contains("3 Monitor 24 pulgadas: $500.00 ID:MON-001");
        }

        @Test
        @DisplayName("Debería listar múltiples componentes correctamente")
        void deberiaListarMultiplesComponentesCorrectamente() {
            // Arrange
            cotizadorConMap.agregarComponente(1, componenteMock1);
            cotizadorConMap.agregarComponente(2, componenteMock2);
            cotizadorConMap.agregarComponente(4, componenteMock3);

            // Act
            cotizadorConMap.listarComponentes();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("1 Monitor 24 pulgadas: $500.00 ID:MON-001");
            assertThat(output).contains("2 Procesador Intel i7: $400.00 ID:CPU-002");
            assertThat(output).contains("4 Memoria RAM 16GB: $150.00 ID:MEM-003");
        }

        @Test
        @DisplayName("Debería manejar componentes con precios decimales en listado")
        void deberiaManejarComponentesConPreciosDecimalesEnListado() {
            // Arrange - Configurar mock con precio decimal
            Componente componenteDecimal = mock(Componente.class);
            when(componenteDecimal.getId()).thenReturn("DEC-001");
            when(componenteDecimal.getDescripcion()).thenReturn("Componente Decimal");
            when(componenteDecimal.getPrecioBase()).thenReturn(new BigDecimal("123.45"));

            cotizadorConMap.agregarComponente(1, componenteDecimal);

            // Act
            cotizadorConMap.listarComponentes();

            // Assert
            String output = outputStream.toString();
            assertThat(output).contains("1 Componente Decimal: $123.45 ID:DEC-001");
        }
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases y Casos Límite")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar componente con ID null")
        void deberiaManejarComponenteConIdNull() {
            // Arrange
            Componente componenteNullId = mock(Componente.class);
            when(componenteNullId.getId()).thenReturn(null);
            when(componenteNullId.getDescripcion()).thenReturn("Componente sin ID");
            when(componenteNullId.getPrecioBase()).thenReturn(new BigDecimal("100.00"));

            // Act
            cotizadorConMap.agregarComponente(1, componenteNullId);

            // Assert - No debe lanzar excepción al agregar
            cotizadorConMap.listarComponentes();
            String output = outputStream.toString();
            assertThat(output).contains("Componente sin ID");
        }

        @Test
        @DisplayName("Debería manejar cotización con componente que cotiza cero")
        void deberiaManejarCotizacionConComponenteQueCotizaCero() {
            // Arrange
            Componente componenteCero = mock(Componente.class);
            when(componenteCero.getId()).thenReturn("ZERO-001");
            when(componenteCero.getDescripcion()).thenReturn("Componente Gratis");
            when(componenteCero.getPrecioBase()).thenReturn(BigDecimal.ZERO);
            when(componenteCero.cotizar(anyInt())).thenReturn(BigDecimal.ZERO);
            when(componenteCero.getCategoria()).thenReturn("GRATIS");

            cotizadorConMap.agregarComponente(5, componenteCero);

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(null);

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(cotizacion.getDetalles()).hasSize(1);
            assertThat(cotizacion.getDetalles().get(0).getImporteCotizado())
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería manejar impuesto que retorna cero")
        void deberiaManejarImpuestoQueRetornaCero() {
            // Arrange
            CalculadorImpuesto impuestoCero = mock(CalculadorImpuesto.class);
            when(impuestoCero.calcularImpuesto(any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);

            cotizadorConMap.agregarComponente(1, componenteMock1);

            // Act
            Cotizacion cotizacion = cotizadorConMap.generarCotizacion(Arrays.asList(impuestoCero));

            // Assert
            assertThat(cotizacion.getTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(cotizacion.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería ser consistente en múltiples generaciones")
        void deberiaSerConsistenteEnMultiplesGeneraciones() {
            // Arrange
            cotizadorConMap.agregarComponente(2, componenteMock1);
            cotizadorConMap.agregarComponente(1, componenteMock2);

            // Act - Generar múltiples veces
            Cotizacion cotizacion1 = cotizadorConMap.generarCotizacion(null);
            Cotizacion cotizacion2 = cotizadorConMap.generarCotizacion(null);
            Cotizacion cotizacion3 = cotizadorConMap.generarCotizacion(null);

            // Assert - Resultados deben ser consistentes
            assertThat(cotizacion1.getTotal()).isEqualByComparingTo(cotizacion2.getTotal());
            assertThat(cotizacion2.getTotal()).isEqualByComparingTo(cotizacion3.getTotal());
            assertThat(cotizacion1.getDetalles()).hasSameSizeAs(cotizacion2.getDetalles());
            assertThat(cotizacion2.getDetalles()).hasSameSizeAs(cotizacion3.getDetalles());
        }
    }

    // ==================== MÉTODOS HELPER PARA CONFIGURAR MOCKS ====================

    private void configurarComponenteMock1() {
        when(componenteMock1.getId()).thenReturn("MON-001");
        when(componenteMock1.getDescripcion()).thenReturn("Monitor 24 pulgadas");
        when(componenteMock1.getPrecioBase()).thenReturn(new BigDecimal("500.00"));
        when(componenteMock1.cotizar(anyInt())).thenAnswer(invocation -> {
            int cantidad = invocation.getArgument(0);
            return new BigDecimal("500.00").multiply(BigDecimal.valueOf(cantidad));
        });
        when(componenteMock1.getCategoria()).thenReturn("MONITOR");
    }

    private void configurarComponenteMock2() {
        when(componenteMock2.getId()).thenReturn("CPU-002");
        when(componenteMock2.getDescripcion()).thenReturn("Procesador Intel i7");
        when(componenteMock2.getPrecioBase()).thenReturn(new BigDecimal("400.00"));
        when(componenteMock2.cotizar(anyInt())).thenAnswer(invocation -> {
            int cantidad = invocation.getArgument(0);
            return new BigDecimal("400.00").multiply(BigDecimal.valueOf(cantidad));
        });
        when(componenteMock2.getCategoria()).thenReturn("PROCESADOR");
    }

    private void configurarComponenteMock3() {
        when(componenteMock3.getId()).thenReturn("MEM-003");
        when(componenteMock3.getDescripcion()).thenReturn("Memoria RAM 16GB");
        when(componenteMock3.getPrecioBase()).thenReturn(new BigDecimal("150.00"));
        when(componenteMock3.cotizar(anyInt())).thenAnswer(invocation -> {
            int cantidad = invocation.getArgument(0);
            return new BigDecimal("150.00").multiply(BigDecimal.valueOf(cantidad));
        });
        when(componenteMock3.getCategoria()).thenReturn("MEMORIA");
    }

    private void configurarCalculadorImpuestoMock() {
        // Mock que calcula 16% de impuesto (80.00 sobre 500.00)
        when(calculadorImpuestoMock.calcularImpuesto(any(BigDecimal.class))).thenAnswer(invocation -> {
            BigDecimal monto = invocation.getArgument(0);
            return monto.multiply(new BigDecimal("0.16"));
        });
    }
}