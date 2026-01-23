package mx.com.qtx.cotizador.dominio.cotizadorA;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;

import mx.com.qtx.cotizador.dominio.core.ComponenteInvalidoException;
import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;
import mx.com.qtx.cotizador.dominio.core.ICotizador;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;

/**
 * Pruebas unitarias para la clase {@link Cotizador}.
 * <p>
 * Esta suite de pruebas verifica el correcto funcionamiento del cotizador A,
 * incluyendo la validación del patrón Strategy, la gestión de listas paralelas,
 * la precisión en los cálculos financieros y la integración con diferentes
 * tipos de componentes y calculadores de impuestos.
 * </p>
 *
 * <h3>Cobertura de pruebas:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Validación de parámetros e inicialización</li>
 *   <li><strong>Gestión de componentes:</strong> Agregar, eliminar, sincronización de listas</li>
 *   <li><strong>Generación de cotizaciones:</strong> Cálculos, impuestos, casos complejos</li>
 *   <li><strong>Casos límite:</strong> Listas vacías, componentes nulos, errores</li>
 *   <li><strong>Integración:</strong> Funcionamiento con diferentes mocks y escenarios reales</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Cotizador (CotizadorA) - Pruebas Unitarias")
class CotizadorTest {

    @Mock
    private CalculadorImpuesto calculadorImpuestoMock;

    @Mock
    private Componente componenteMock1;

    @Mock
    private Componente componenteMock2;

    @Mock
    private Componente componenteMock3;

    @Mock
    private CalculadorImpuesto impuesto1Mock;

    @Mock
    private CalculadorImpuesto impuesto2Mock;

    private Cotizador cotizador;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    /**
     * Configuración inicial para cada prueba.
     */
    @BeforeEach
    void setUp() {
        cotizador = new Cotizador(calculadorImpuestoMock);

        // Configurar mocks de componentes básicos
        configurarComponenteMock1();
        configurarComponenteMock2();
        configurarComponenteMock3();

        // Configurar captura de salida estándar
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    /**
     * Configura el mock del componente 1.
     */
    private void configurarComponenteMock1() {
        when(componenteMock1.getId()).thenReturn("MON-001");
        when(componenteMock1.getDescripcion()).thenReturn("Monitor 24 pulgadas");
        when(componenteMock1.getPrecioBase()).thenReturn(new BigDecimal("500.00"));
        when(componenteMock1.getCategoria()).thenReturn("MONITOR");
        when(componenteMock1.cotizar(anyInt())).thenAnswer(invocation -> {
            int cantidad = invocation.getArgument(0);
            return new BigDecimal("500.00").multiply(BigDecimal.valueOf(cantidad));
        });
    }

    /**
     * Configura el mock del componente 2.
     */
    private void configurarComponenteMock2() {
        when(componenteMock2.getId()).thenReturn("CPU-001");
        when(componenteMock2.getDescripcion()).thenReturn("Procesador Intel i7");
        when(componenteMock2.getPrecioBase()).thenReturn(new BigDecimal("2500.00"));
        when(componenteMock2.getCategoria()).thenReturn("PROCESADOR");
        when(componenteMock2.cotizar(anyInt())).thenAnswer(invocation -> {
            int cantidad = invocation.getArgument(0);
            return new BigDecimal("2500.00").multiply(BigDecimal.valueOf(cantidad));
        });
    }

    /**
     * Configura el mock del componente 3.
     */
    private void configurarComponenteMock3() {
        when(componenteMock3.getId()).thenReturn("RAM-001");
        when(componenteMock3.getDescripcion()).thenReturn("Memoria DDR4 16GB");
        when(componenteMock3.getPrecioBase()).thenReturn(new BigDecimal("800.00"));
        when(componenteMock3.getCategoria()).thenReturn("MEMORIA");
        when(componenteMock3.cotizar(anyInt())).thenAnswer(invocation -> {
            int cantidad = invocation.getArgument(0);
            return new BigDecimal("800.00").multiply(BigDecimal.valueOf(cantidad));
        });
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("Debería crear instancia con calculador de impuesto válido")
        void deberiaCrearInstanciaConCalculadorImpuestoValido() {
            // Given
            CalculadorImpuesto calculador = mock(CalculadorImpuesto.class);

            // When
            Cotizador resultado = new Cotizador(calculador);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).isInstanceOf(ICotizador.class);
        }

        @Test
        @DisplayName("Debería permitir calculador de impuesto null")
        void deberiaPermitirCalculadorImpuestoNull() {
            // When
            Cotizador resultado = new Cotizador(null);

            // Then
            assertThat(resultado).isNotNull();
        }
    }

    @Nested
    @DisplayName("Agregar Componentes")
    class AgregarComponentesTest {

        @Test
        @DisplayName("Debería agregar componente único correctamente")
        void deberiaAgregarComponenteUnicoCorrectamente() {
            // Given
            int cantidad = 2;

            // When
            cotizador.agregarComponente(cantidad, componenteMock1);

            // Then - Verificar que se puede generar cotización
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDetalles()).hasSize(1);

            DetalleCotizacion detalle = resultado.getDetalles().get(0);
            assertThat(detalle.getIdComponente()).isEqualTo("MON-001");
            assertThat(detalle.getCantidad()).isEqualTo(cantidad);
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Debería agregar múltiples componentes manteniendo orden")
        void deberiaAgregarMultiplesComponentesManteniendoOrden() {
            // Given
            // When
            cotizador.agregarComponente(2, componenteMock1); // Monitor
            cotizador.agregarComponente(1, componenteMock2); // CPU
            cotizador.agregarComponente(4, componenteMock3); // RAM

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(3);

            // Verificar primer componente (Monitor)
            DetalleCotizacion detalle1 = resultado.getDetalles().get(0);
            assertThat(detalle1.getIdComponente()).isEqualTo("MON-001");
            assertThat(detalle1.getCantidad()).isEqualTo(2);

            // Verificar segundo componente (CPU)
            DetalleCotizacion detalle2 = resultado.getDetalles().get(1);
            assertThat(detalle2.getIdComponente()).isEqualTo("CPU-001");
            assertThat(detalle2.getCantidad()).isEqualTo(1);

            // Verificar tercer componente (RAM)
            DetalleCotizacion detalle3 = resultado.getDetalles().get(2);
            assertThat(detalle3.getIdComponente()).isEqualTo("RAM-001");
            assertThat(detalle3.getCantidad()).isEqualTo(4);
        }

        @Test
        @DisplayName("Debería agregar mismo componente múltiples veces")
        void deberiaAgregarMismoComponenteMultiplesVeces() {
            // Given & When
            cotizador.agregarComponente(1, componenteMock1);
            cotizador.agregarComponente(2, componenteMock1); // Mismo componente

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(2);

            // Primera entrada
            DetalleCotizacion detalle1 = resultado.getDetalles().get(0);
            assertThat(detalle1.getIdComponente()).isEqualTo("MON-001");
            assertThat(detalle1.getCantidad()).isEqualTo(1);
            assertThat(detalle1.getImporteCotizado()).isEqualByComparingTo(new BigDecimal("500.00"));

            // Segunda entrada
            DetalleCotizacion detalle2 = resultado.getDetalles().get(1);
            assertThat(detalle2.getIdComponente()).isEqualTo("MON-001");
            assertThat(detalle2.getCantidad()).isEqualTo(2);
            assertThat(detalle2.getImporteCotizado()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Debería agregar componente con cantidad cero")
        void deberiaAgregarComponenteConCantidadCero() {
            // Given & When
            cotizador.agregarComponente(0, componenteMock1);

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(1);
            DetalleCotizacion detalle = resultado.getDetalles().get(0);
            assertThat(detalle.getCantidad()).isEqualTo(0);
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería agregar componente con cantidad negativa")
        void deberiaAgregarComponenteConCantidadNegativa() {
            // Given & When
            cotizador.agregarComponente(-1, componenteMock1);

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(1);
            DetalleCotizacion detalle = resultado.getDetalles().get(0);
            assertThat(detalle.getCantidad()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Eliminar Componentes")
    class EliminarComponentesTest {

        @BeforeEach
        void setUpEliminar() {
            // Agregar componentes para las pruebas de eliminación
            cotizador.agregarComponente(2, componenteMock1);
            cotizador.agregarComponente(1, componenteMock2);
            cotizador.agregarComponente(3, componenteMock3);
        }

        @Test
        @DisplayName("Debería eliminar componente por ID correctamente")
        void deberiaEliminarComponentePorIdCorrectamente() throws ComponenteInvalidoException {
            // Given
            String idAEliminar = "CPU-001";

            // When
            cotizador.eliminarComponente(idAEliminar);

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(2); // Solo quedan 2

            // Verificar que se eliminó el CPU y quedaron Monitor y RAM
            List<String> idsPresentes = resultado.getDetalles().stream()
                .map(DetalleCotizacion::getIdComponente)
                .toList();

            assertThat(idsPresentes).containsExactly("MON-001", "RAM-001");
            assertThat(idsPresentes).doesNotContain("CPU-001");
        }

        @Test
        @DisplayName("Debería eliminar primer componente manteniendo sincronización")
        void deberiaEliminarPrimerComponenteMantiendoSincronizacion() throws ComponenteInvalidoException {
            // Given
            String idAEliminar = "MON-001"; // Primer componente

            // When
            cotizador.eliminarComponente(idAEliminar);

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(2);

            // El primer detalle ahora debería ser el CPU (que era el segundo)
            DetalleCotizacion primerDetalle = resultado.getDetalles().get(0);
            assertThat(primerDetalle.getIdComponente()).isEqualTo("CPU-001");
            assertThat(primerDetalle.getCantidad()).isEqualTo(1);

            // El segundo detalle debería ser la RAM (que era el tercero)
            DetalleCotizacion segundoDetalle = resultado.getDetalles().get(1);
            assertThat(segundoDetalle.getIdComponente()).isEqualTo("RAM-001");
            assertThat(segundoDetalle.getCantidad()).isEqualTo(3);
        }

        @Test
        @DisplayName("Debería eliminar último componente correctamente")
        void deberiaEliminarUltimoComponenteCorrectamente() throws ComponenteInvalidoException {
            // Given
            String idAEliminar = "RAM-001"; // Último componente

            // When
            cotizador.eliminarComponente(idAEliminar);

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).hasSize(2);

            List<String> idsPresentes = resultado.getDetalles().stream()
                .map(DetalleCotizacion::getIdComponente)
                .toList();

            assertThat(idsPresentes).containsExactly("MON-001", "CPU-001");
            assertThat(idsPresentes).doesNotContain("RAM-001");
        }

        @Test
        @DisplayName("Debería lanzar excepción con ID null")
        void deberiaLanzarExcepcionConIdNull() {
            // When & Then
            assertThatThrownBy(() -> cotizador.eliminarComponente(null))
                .isInstanceOf(ComponenteInvalidoException.class)
                .hasMessageContaining("Id del componente es nulo");
        }

        @Test
        @DisplayName("Debería lanzar excepción con ID inexistente")
        void deberiaLanzarExcepcionConIdInexistente() {
            // Given
            String idInexistente = "INEXISTENTE-999";

            // When & Then
            assertThatThrownBy(() -> cotizador.eliminarComponente(idInexistente))
                .isInstanceOf(ComponenteInvalidoException.class)
                .hasMessageContaining("No existe componente con Id INEXISTENTE-999");
        }

        @Test
        @DisplayName("Debería eliminar todos los componentes uno por uno")
        void deberiaEliminarTodosLosComponentesUnoPorUno() throws ComponenteInvalidoException {
            // When
            cotizador.eliminarComponente("MON-001");
            cotizador.eliminarComponente("CPU-001");
            cotizador.eliminarComponente("RAM-001");

            // Then
            Cotizacion resultado = cotizador.generarCotizacion(null);

            assertThat(resultado.getDetalles()).isEmpty();
            assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Generar Cotización")
    class GenerarCotizacionTest {

        @Test
        @DisplayName("Debería generar cotización sin componentes")
        void deberiaGenerarCotizacionSinComponentes() {
            // When
            Cotizacion resultado = cotizador.generarCotizacion(null);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDetalles()).isEmpty();
            assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería generar cotización con un componente sin impuestos")
        void deberiaGenerarCotizacionConUnComponenteSinImpuestos() {
            // Given
            cotizador.agregarComponente(2, componenteMock1);

            // When
            Cotizacion resultado = cotizador.generarCotizacion(null);

            // Then
            assertThat(resultado.getDetalles()).hasSize(1);
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("1000.00")); // 2 * 500
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);

            DetalleCotizacion detalle = resultado.getDetalles().get(0);
            assertThat(detalle.getNumDetalle()).isEqualTo(1);
            assertThat(detalle.getIdComponente()).isEqualTo("MON-001");
            assertThat(detalle.getCantidad()).isEqualTo(2);
            assertThat(detalle.getPrecioBase()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(detalle.getImporteCotizado()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(detalle.getCategoria()).isEqualTo("MONITOR");
        }

        @Test
        @DisplayName("Debería generar cotización con múltiples componentes sin impuestos")
        void deberiaGenerarCotizacionConMultiplesComponentesSinImpuestos() {
            // Given
            cotizador.agregarComponente(1, componenteMock1);  // 500
            cotizador.agregarComponente(1, componenteMock2);  // 2500
            cotizador.agregarComponente(2, componenteMock3);  // 1600

            // When
            Cotizacion resultado = cotizador.generarCotizacion(null);

            // Then
            assertThat(resultado.getDetalles()).hasSize(3);
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("4600.00"));
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería generar cotización con un impuesto")
        void deberiaGenerarCotizacionConUnImpuesto() {
            // Given
            cotizador.agregarComponente(1, componenteMock1); // 500
            when(impuesto1Mock.calcularImpuesto(new BigDecimal("500.00")))
                .thenReturn(new BigDecimal("80.00")); // 16% IVA

            // When
            Cotizacion resultado = cotizador.generarCotizacion(Arrays.asList(impuesto1Mock));

            // Then
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("580.00")); // 500 + 80
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(new BigDecimal("80.00"));

            verify(impuesto1Mock).calcularImpuesto(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Debería generar cotización con múltiples impuestos")
        void deberiaGenerarCotizacionConMultiplesImpuestos() {
            // Given
            cotizador.agregarComponente(1, componenteMock1); // 500
            when(impuesto1Mock.calcularImpuesto(new BigDecimal("500.00")))
                .thenReturn(new BigDecimal("80.00")); // IVA 16%
            when(impuesto2Mock.calcularImpuesto(any(BigDecimal.class)))
                .thenReturn(new BigDecimal("25.00")); // Federal 5%

            // When
            List<CalculadorImpuesto> impuestos = Arrays.asList(impuesto1Mock, impuesto2Mock);
            Cotizacion resultado = cotizador.generarCotizacion(impuestos);

            // Then
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("605.00")); // 500 + 80 + 25
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(new BigDecimal("105.00"));

            verify(impuesto1Mock).calcularImpuesto(new BigDecimal("500.00"));
            verify(impuesto2Mock).calcularImpuesto(any(BigDecimal.class));
        }

        @Test
        @DisplayName("Debería generar cotización compleja con múltiples componentes e impuestos")
        void deberiaGenerarCotizacionComplejaConMultiplesComponentesEImpuestos() {
            // Given
            cotizador.agregarComponente(2, componenteMock1);  // 1000
            cotizador.agregarComponente(1, componenteMock2);  // 2500
            cotizador.agregarComponente(1, componenteMock3);  // 800
            // Subtotal: 4300

            when(impuesto1Mock.calcularImpuesto(new BigDecimal("4300.00")))
                .thenReturn(new BigDecimal("688.00")); // IVA 16%
            when(impuesto2Mock.calcularImpuesto(any(BigDecimal.class)))
                .thenReturn(new BigDecimal("215.00")); // Federal 5%

            // When
            List<CalculadorImpuesto> impuestos = Arrays.asList(impuesto1Mock, impuesto2Mock);
            Cotizacion resultado = cotizador.generarCotizacion(impuestos);

            // Then
            assertThat(resultado.getDetalles()).hasSize(3);
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("5203.00")); // 4300 + 688 + 215
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(new BigDecimal("903.00"));
        }

        @Test
        @DisplayName("Debería generar cotización con impuestos en lista vacía")
        void deberiaGenerarCotizacionConImpuestosEnListaVacia() {
            // Given
            cotizador.agregarComponente(1, componenteMock1);

            // When
            Cotizacion resultado = cotizador.generarCotizacion(Arrays.asList());

            // Then
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Listar Componentes")
    class ListarComponentesTest {

        @Test
        @DisplayName("Debería listar componentes sin elementos")
        void deberiaListarComponentesSinElementos() {
            // When
            cotizador.listarComponentes();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("=== Componentes a cotizar ===");
            // Solo debe aparecer el encabezado, no componentes
            assertThat(output.split("\\n")).hasSize(1); // Solo encabezado
        }

        @Test
        @DisplayName("Debería listar un componente correctamente")
        void deberiaListarUnComponenteCorrectamente() {
            // Given
            cotizador.agregarComponente(2, componenteMock1);

            // When
            cotizador.listarComponentes();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("=== Componentes a cotizar ===");
            assertThat(output).contains("2 Monitor 24 pulgadas: $500.00 ID:MON-001");
        }

        @Test
        @DisplayName("Debería listar múltiples componentes en orden")
        void deberiaListarMultiplesComponentesEnOrden() {
            // Given
            cotizador.agregarComponente(2, componenteMock1);
            cotizador.agregarComponente(1, componenteMock2);
            cotizador.agregarComponente(4, componenteMock3);

            // When
            cotizador.listarComponentes();

            // Then
            String output = outputStream.toString();
            String[] lines = output.split("\\n");

            assertThat(output).contains("=== Componentes a cotizar ===");
            assertThat(lines).contains("2 Monitor 24 pulgadas: $500.00 ID:MON-001");
            assertThat(lines).contains("1 Procesador Intel i7: $2500.00 ID:CPU-001");
            assertThat(lines).contains("4 Memoria DDR4 16GB: $800.00 ID:RAM-001");
        }

        @Test
        @DisplayName("Debería listar componentes después de eliminaciones")
        void deberiaListarComponentesDespuesDeEliminaciones() throws ComponenteInvalidoException {
            // Given
            cotizador.agregarComponente(2, componenteMock1);
            cotizador.agregarComponente(1, componenteMock2);
            cotizador.agregarComponente(4, componenteMock3);

            // When
            cotizador.eliminarComponente("CPU-001");
            cotizador.listarComponentes();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("2 Monitor 24 pulgadas: $500.00 ID:MON-001");
            assertThat(output).contains("4 Memoria DDR4 16GB: $800.00 ID:RAM-001");
            assertThat(output).doesNotContain("Procesador Intel i7");
        }
    }

    @Nested
    @DisplayName("Casos Límite y Excepciones")
    class CasosLimiteExcepcionesTest {

        @Test
        @DisplayName("Debería manejar componente null sin error inmediato")
        void deberiaManejarComponenteNullSinErrorInmediato() {
            // When
            cotizador.agregarComponente(1, null);

            // Then - No debería fallar hasta que se intente usar
            assertThatThrownBy(() -> cotizador.generarCotizacion(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería manejar cantidades extremas")
        void deberiaManejarCantidadesExtremas() {
            // Given
            when(componenteMock1.cotizar(Integer.MAX_VALUE))
                .thenReturn(new BigDecimal("1000000000000.00"));

            // When
            cotizador.agregarComponente(Integer.MAX_VALUE, componenteMock1);
            Cotizacion resultado = cotizador.generarCotizacion(null);

            // Then
            assertThat(resultado.getDetalles()).hasSize(1);
            DetalleCotizacion detalle = resultado.getDetalles().get(0);
            assertThat(detalle.getCantidad()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Debería manejar eliminación de lista vacía")
        void deberiaManejarEliminacionDeListaVacia() {
            // When & Then
            assertThatThrownBy(() -> cotizador.eliminarComponente("CUALQUIER-ID"))
                .isInstanceOf(ComponenteInvalidoException.class)
                .hasMessageContaining("No existe componente con Id CUALQUIER-ID");
        }

        @Test
        @DisplayName("Debería manejar múltiples eliminaciones del mismo ID")
        void deberiaManejarMultiplesEliminacionesDelMismoId() throws ComponenteInvalidoException {
            // Given
            cotizador.agregarComponente(1, componenteMock1);

            // When
            cotizador.eliminarComponente("MON-001"); // Primera eliminación OK

            // Then
            assertThatThrownBy(() -> cotizador.eliminarComponente("MON-001")) // Segunda eliminación falla
                .isInstanceOf(ComponenteInvalidoException.class)
                .hasMessageContaining("No existe componente con Id MON-001");
        }

        @Test
        @DisplayName("Debería manejar impuesto que retorna null")
        void deberiaManejarImpuestoQueRetornaNull() {
            // Given
            cotizador.agregarComponente(1, componenteMock1);
            when(impuesto1Mock.calcularImpuesto(any(BigDecimal.class)))
                .thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> cotizador.generarCotizacion(Arrays.asList(impuesto1Mock)))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería manejar componente con precio null")
        void deberiaManejarComponenteConPrecioNull() {
            // Given
            when(componenteMock1.getPrecioBase()).thenReturn(null);
            when(componenteMock1.cotizar(1)).thenReturn(BigDecimal.ZERO); // Mock maneja precio null
            cotizador.agregarComponente(1, componenteMock1);

            // When
            Cotizacion resultado = cotizador.generarCotizacion(null);

            // Then - El sistema debe poder generar cotización con componente de precio null
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDetalles()).hasSize(1);
            assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Integración y Escenarios Reales")
    class IntegracionEscenariosRealesTest {

        @Test
        @DisplayName("Debería manejar flujo completo de cotización")
        void deberiaManejarFlujoCompletoDeCotizacion() throws ComponenteInvalidoException {
            // Given - Agregar componentes
            cotizador.agregarComponente(1, componenteMock1);  // Monitor
            cotizador.agregarComponente(1, componenteMock2);  // CPU
            cotizador.agregarComponente(2, componenteMock3);  // RAM

            // Eliminar uno
            cotizador.eliminarComponente("CPU-001");

            // Agregar de nuevo
            cotizador.agregarComponente(1, componenteMock2);

            // Configurar impuestos
            when(impuesto1Mock.calcularImpuesto(any(BigDecimal.class)))
                .thenReturn(new BigDecimal("400.00"));

            // When
            Cotizacion resultado = cotizador.generarCotizacion(Arrays.asList(impuesto1Mock));

            // Then - Monitor (500) + RAM (1600) + CPU (2500) = 4600 + 400 impuesto = 5000
            assertThat(resultado.getDetalles()).hasSize(3);
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(resultado.getTotalImpuestos()).isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("Debería permitir reutilizar cotizador para múltiples cotizaciones")
        void deberiaPermitirReutilizarCotizadorParaMultiplesCotizaciones() {
            // Primera cotización
            cotizador.agregarComponente(1, componenteMock1);
            Cotizacion primera = cotizador.generarCotizacion(null);
            assertThat(primera.getTotal()).isEqualByComparingTo(new BigDecimal("500.00"));

            // Segunda cotización (agregando más componentes)
            cotizador.agregarComponente(1, componenteMock2);
            Cotizacion segunda = cotizador.generarCotizacion(null);
            assertThat(segunda.getTotal()).isEqualByComparingTo(new BigDecimal("3000.00")); // 500 + 2500

            // Las cotizaciones son independientes
            assertThat(primera.getTotal()).isEqualByComparingTo(new BigDecimal("500.00")); // No cambió
        }

        @Test
        @DisplayName("Debería mantener consistencia después de múltiples operaciones")
        void deberiaMantenerConsistenciaDespuesDeMultiplesOperaciones() throws ComponenteInvalidoException {
            // Given - Secuencia compleja de operaciones
            cotizador.agregarComponente(2, componenteMock1);  // +2 Monitor (1000)
            cotizador.agregarComponente(1, componenteMock2);  // +1 CPU (2500)
            cotizador.agregarComponente(3, componenteMock3);  // +3 RAM (2400)
            cotizador.agregarComponente(1, componenteMock1);  // +1 Monitor más (500)

            cotizador.eliminarComponente("CPU-001");          // -1 CPU (2500)
            cotizador.eliminarComponente("MON-001");          // -primera entrada Monitor (1000)

            // When
            Cotizacion resultado = cotizador.generarCotizacion(null);

            // Then - Quedan: 3 RAM (2400) + 1 Monitor (500) = 2900
            assertThat(resultado.getDetalles()).hasSize(2);
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("2900.00"));

            List<String> idsPresentes = resultado.getDetalles().stream()
                .map(DetalleCotizacion::getIdComponente)
                .toList();
            assertThat(idsPresentes).containsExactly("RAM-001", "MON-001");
        }

        @Test
        @DisplayName("Debería implementar correctamente el patrón Strategy")
        void deberiaImplementarCorrectamenteElPatronStrategy() {
            // Given
            assertThat(cotizador).isInstanceOf(ICotizador.class);

            // When - Usar como estrategia
            ICotizador estrategia = cotizador;
            estrategia.agregarComponente(1, componenteMock1);
            Cotizacion resultado = estrategia.generarCotizacion(null);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDetalles()).hasSize(1);
        }
    }
}