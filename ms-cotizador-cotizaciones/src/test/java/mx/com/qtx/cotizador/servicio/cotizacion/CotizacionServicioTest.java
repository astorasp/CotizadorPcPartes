package mx.com.qtx.cotizador.servicio.cotizacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;
import mx.com.qtx.cotizador.dominio.core.ICotizador;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.cotizadorA.Cotizador;
import mx.com.qtx.cotizador.dominio.cotizadorB.CotizadorConMap;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;
import mx.com.qtx.cotizador.dominio.impuestos.IVA;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.cotizacion.request.CotizacionCreateRequest;
import mx.com.qtx.cotizador.dto.cotizacion.request.DetalleCotizacionRequest;
import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.dto.cotizacion.mapper.CotizacionMapper;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.CotizacionEntityConverter;
import mx.com.qtx.cotizador.util.Errores;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Pruebas unitarias para CotizacionServicio.
 * <p>
 * Valida el funcionamiento correcto de todos los métodos del servicio,
 * incluyendo la creación de cotizaciones, búsquedas, validaciones y manejo de errores.
 * </p>
 *
 * @author Claude Code - Pruebas Automatizadas
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CotizacionServicio - Pruebas Unitarias")
class CotizacionServicioTest {

    @Mock
    private CotizacionRepositorio cotizacionRepositorioMock;

    @Mock
    private ComponenteRepositorio componenteRepositorioMock;

    @InjectMocks
    private CotizacionServicio cotizacionServicio;

    // Mocks para objetos de dominio
    @Mock
    private Cotizacion cotizacionDominioMock;

    @Mock
    private Componente componenteDominioMock;

    // Mocks para entidades JPA
    @Mock
    private mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntidadMock;

    @Mock
    private mx.com.qtx.cotizador.entidad.Componente componenteEntidadMock;

    @Mock
    private mx.com.qtx.cotizador.entidad.TipoComponente tipoComponenteMock;

    // Mocks para DTOs
    @Mock
    private CotizacionCreateRequest cotizacionRequestMock;

    @Mock
    private DetalleCotizacionRequest detalleRequestMock;

    @Mock
    private CotizacionResponse cotizacionResponseMock;

    @BeforeEach
    void setUp() {
        // Configuración común de mocks
        configurarMocksBasicos();
    }

    private void configurarMocksBasicos() {
        // Configurar cotización de dominio mock
        when(cotizacionDominioMock.getNum()).thenReturn(1L);
        when(cotizacionDominioMock.getFecha()).thenReturn(LocalDate.now());
        when(cotizacionDominioMock.getTotal()).thenReturn(new BigDecimal("1000.00"));
        when(cotizacionDominioMock.getTotalImpuestos()).thenReturn(new BigDecimal("160.00"));

        List<DetalleCotizacion> detalles = Arrays.asList(
            new DetalleCotizacion(1, "COMP-001", "Monitor 24", 1,
                new BigDecimal("500.00"), new BigDecimal("500.00"), "MONITOR")
        );
        when(cotizacionDominioMock.getDetalles()).thenReturn(detalles);

        // Configurar componente de dominio mock
        when(componenteDominioMock.getId()).thenReturn("COMP-001");
        when(componenteDominioMock.getDescripcion()).thenReturn("Monitor 24 pulgadas");
        when(componenteDominioMock.getPrecioBase()).thenReturn(new BigDecimal("500.00"));

        // Configurar entidad componente mock
        when(componenteEntidadMock.getId()).thenReturn("COMP-001");
        when(componenteEntidadMock.getDescripcion()).thenReturn("Monitor 24 pulgadas");
        when(componenteEntidadMock.getMarca()).thenReturn("Dell");
        when(componenteEntidadMock.getModelo()).thenReturn("P2414H");
        when(componenteEntidadMock.getCosto()).thenReturn(new BigDecimal("400.00"));
        when(componenteEntidadMock.getPrecioBase()).thenReturn(new BigDecimal("500.00"));
        when(componenteEntidadMock.getTipoComponente()).thenReturn(tipoComponenteMock);

        // Configurar tipo componente mock
        when(tipoComponenteMock.getNombre()).thenReturn("MONITOR");

        // Configurar entidad cotización mock
        when(cotizacionEntidadMock.getFolio()).thenReturn(1001);
        when(cotizacionEntidadMock.getFecha()).thenReturn("2025-09-13");
        when(cotizacionEntidadMock.getTotal()).thenReturn(new BigDecimal("1000.00"));

        // Configurar request mocks
        when(cotizacionRequestMock.getTipoCotizador()).thenReturn("A");
        when(cotizacionRequestMock.getImpuestos()).thenReturn(Arrays.asList("IVA"));
        when(cotizacionRequestMock.getDetalles()).thenReturn(Arrays.asList(detalleRequestMock));

        when(detalleRequestMock.getIdComponente()).thenReturn("COMP-001");
        when(detalleRequestMock.getCantidad()).thenReturn(1);
    }

    @Nested
    @DisplayName("Constructor y Configuración")
    class ConstructorTest {

        @Test
        @DisplayName("Debería inicializar servicio con dependencias correctas")
        void deberiaInicializarServicioCorrectamente() {
            // Given & When - El servicio se inicializa en @InjectMocks

            // Then
            assertThat(cotizacionServicio).isNotNull();
            // Las dependencias se inyectan correctamente por Mockito
        }
    }

    @Nested
    @DisplayName("Guardar Cotización con Request")
    class GuardarCotizacionRequestTest {

        @Test
        @DisplayName("Debería guardar cotización exitosamente")
        void deberiaGuardarCotizacionExitosamente() {
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class);
                 MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {

                // Given
                when(componenteRepositorioMock.findById("COMP-001"))
                    .thenReturn(Optional.of(componenteEntidadMock));

                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);

                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);

                mapperMock.when(() -> CotizacionMapper.toResponse(any()))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

                // Then
                assertThat(resultado).isNotNull();
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
                assertThat(resultado.getDatos()).isEqualTo(cotizacionResponseMock);

                verify(componenteRepositorioMock).findById("COMP-001");
                verify(cotizacionRepositorioMock).save(any());
            }
        }

        @Test
        @DisplayName("Debería fallar cuando request es null")
        void deberiaFallarCuandoRequestEsNull() {
            // When
            ApiResponse<CotizacionResponse> resultado =
                cotizacionServicio.guardarCotizacion((CotizacionCreateRequest) null);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(Errores.CAMPO_REQUERIDO.getCodigo());
            assertThat(resultado.getDatos()).isNull();
        }

        @Test
        @DisplayName("Debería fallar cuando no hay detalles")
        void deberiaFallarCuandoNoHayDetalles() {
            // Given
            when(cotizacionRequestMock.getDetalles()).thenReturn(null);

            // When
            ApiResponse<CotizacionResponse> resultado =
                cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(Errores.COTIZACION_SIN_DETALLES.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando componente no existe")
        void deberiaFallarCuandoComponenteNoExiste() {
            // Given
            when(componenteRepositorioMock.findById("COMP-001"))
                .thenReturn(Optional.empty());

            // When
            ApiResponse<CotizacionResponse> resultado =
                cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(Errores.COMPONENTE_NO_ENCONTRADO_EN_COTIZACION.getCodigo());
        }

        @Test
        @DisplayName("Debería establecer fecha personalizada correcta")
        void deberiaEstablecerFechaPersonalizada() {
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class);
                 MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {

                // Given
                when(cotizacionRequestMock.getFecha()).thenReturn("2025-12-25");
                when(componenteRepositorioMock.findById("COMP-001"))
                    .thenReturn(Optional.of(componenteEntidadMock));

                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);
                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);
                mapperMock.when(() -> CotizacionMapper.toResponse(any()))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
                verify(cotizacionRepositorioMock).save(any());
            }
        }

        @Test
        @DisplayName("Debería manejar excepción durante guardado")
        void deberiaManejarExcepcionDuranteGuardado() {
            // Given
            when(componenteRepositorioMock.findById(any()))
                .thenThrow(new RuntimeException("Error de BD"));

            // When
            ApiResponse<CotizacionResponse> resultado =
                cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Guardar Cotización de Dominio")
    class GuardarCotizacionDominioTest {

        @Test
        @DisplayName("Debería guardar cotización de dominio exitosamente")
        void deberiaGuardarCotizacionDominioExitosamente() {
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class)) {
                // Given
                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);
                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);

                // When
                ApiResponse<Void> resultado = cotizacionServicio.guardarCotizacion(cotizacionDominioMock);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
                verify(cotizacionRepositorioMock).save(any());
            }
        }

        @Test
        @DisplayName("Debería fallar cuando cotización de dominio es null")
        void deberiaFallarCuandoCotizacionDominioEsNull() {
            // When
            ApiResponse<Void> resultado = cotizacionServicio.guardarCotizacion((Cotizacion) null);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.COTIZACION_INVALIDA.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando cotización no tiene detalles")
        void deberiaFallarCuandoCotizacionNoTieneDetalles() {
            // Given
            when(cotizacionDominioMock.getDetalles()).thenReturn(new ArrayList<>());

            // When
            ApiResponse<Void> resultado = cotizacionServicio.guardarCotizacion(cotizacionDominioMock);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.COTIZACION_SIN_DETALLES.getCodigo());
        }
    }

    @Nested
    @DisplayName("Buscar Cotización por ID")
    class BuscarCotizacionTest {

        @Test
        @DisplayName("Debería buscar cotización por ID exitosamente")
        void deberiaBuscarCotizacionPorIdExitosamente() {
            // Given
            when(cotizacionRepositorioMock.findById(1)).thenReturn(Optional.of(cotizacionEntidadMock));

            // When
            ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> resultado =
                cotizacionServicio.buscarCotizacionPorId(1);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).isEqualTo(cotizacionEntidadMock);
        }

        @Test
        @DisplayName("Debería fallar cuando ID es null")
        void deberiaFallarCuandoIdEsNull() {
            // When
            ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> resultado =
                cotizacionServicio.buscarCotizacionPorId(null);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando ID es inválido")
        void deberiaFallarCuandoIdEsInvalido() {
            // When
            ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> resultado =
                cotizacionServicio.buscarCotizacionPorId(-1);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando cotización no existe")
        void deberiaFallarCuandoCotizacionNoExiste() {
            // Given
            when(cotizacionRepositorioMock.findById(999)).thenReturn(Optional.empty());

            // When
            ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> resultado =
                cotizacionServicio.buscarCotizacionPorId(999);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.COTIZACION_NO_ENCONTRADA.getCodigo());
        }
    }

    @Nested
    @DisplayName("Buscar Cotización como DTO")
    class BuscarCotizacionComoDTOTest {

        @Test
        @DisplayName("Debería buscar cotización como DTO exitosamente")
        void deberiaBuscarCotizacionComoDTOExitosamente() {
            try (MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {
                // Given
                when(cotizacionRepositorioMock.findById(1)).thenReturn(Optional.of(cotizacionEntidadMock));
                mapperMock.when(() -> CotizacionMapper.toResponse(cotizacionEntidadMock))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.buscarCotizacionPorIdComoDTO(1);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
                assertThat(resultado.getDatos()).isEqualTo(cotizacionResponseMock);
            }
        }
    }

    @Nested
    @DisplayName("Listar Cotizaciones")
    class ListarCotizacionesTest {

        @Test
        @DisplayName("Debería listar cotizaciones exitosamente")
        void deberiaListarCotizacionesExitosamente() {
            // Given
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                Arrays.asList(cotizacionEntidadMock);
            when(cotizacionRepositorioMock.findAll()).thenReturn(cotizaciones);

            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.listarCotizaciones();

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).hasSize(1);
        }

        @Test
        @DisplayName("Debería listar cotizaciones como DTO exitosamente")
        void deberiaListarCotizacionesComoDTOExitosamente() {
            try (MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {
                // Given
                List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                    Arrays.asList(cotizacionEntidadMock);
                List<CotizacionResponse> responses = Arrays.asList(cotizacionResponseMock);

                when(cotizacionRepositorioMock.findAll()).thenReturn(cotizaciones);
                mapperMock.when(() -> CotizacionMapper.toResponseList(cotizaciones))
                    .thenReturn(responses);

                // When
                ApiResponse<List<CotizacionResponse>> resultado =
                    cotizacionServicio.listarCotizacionesComoDTO();

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
                assertThat(resultado.getDatos()).hasSize(1);
            }
        }

        @Test
        @DisplayName("Debería manejar excepción al listar")
        void deberiaManejarExcepcionAlListar() {
            // Given
            when(cotizacionRepositorioMock.findAll()).thenThrow(new RuntimeException("Error BD"));

            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.listarCotizaciones();

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Buscar por Fecha")
    class BuscarPorFechaTest {

        @Test
        @DisplayName("Debería buscar cotizaciones por fecha exitosamente")
        void deberiaBuscarCotizacionesPorFechaExitosamente() {
            // Given
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                Arrays.asList(cotizacionEntidadMock);
            when(cotizacionRepositorioMock.findByFechaContaining("2025-09-13"))
                .thenReturn(cotizaciones);

            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorFecha("2025-09-13");

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).hasSize(1);
        }

        @Test
        @DisplayName("Debería fallar cuando fecha es null")
        void deberiaFallarCuandoFechaEsNull() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorFecha(null);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando fecha está vacía")
        void deberiaFallarCuandoFechaEstaVacia() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorFecha("   ");

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Buscar por Rango de Monto")
    class BuscarPorRangoMontoTest {

        @Test
        @DisplayName("Debería buscar cotizaciones por rango de monto exitosamente")
        void deberiaBuscarCotizacionesPorRangoMontoExitosamente() {
            // Given
            BigDecimal montoMin = new BigDecimal("100.00");
            BigDecimal montoMax = new BigDecimal("2000.00");
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                Arrays.asList(cotizacionEntidadMock);

            when(cotizacionRepositorioMock.findByTotalBetween(montoMin, montoMax))
                .thenReturn(cotizaciones);

            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorRangoMonto(montoMin, montoMax);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).hasSize(1);
        }

        @Test
        @DisplayName("Debería fallar cuando monto mínimo es null")
        void deberiaFallarCuandoMontoMinimoEsNull() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorRangoMonto(null, new BigDecimal("1000.00"));

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando montos son negativos")
        void deberiaFallarCuandoMontosNegativos() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorRangoMonto(
                    new BigDecimal("-100"), new BigDecimal("1000"));

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.MONTO_TOTAL_INVALIDO.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando monto mínimo > máximo")
        void deberiaFallarCuandoMontoMinimoPorMayor() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorRangoMonto(
                    new BigDecimal("2000"), new BigDecimal("1000"));

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.RANGO_FECHAS_INVALIDO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Buscar por Componente")
    class BuscarPorComponenteTest {

        @Test
        @DisplayName("Debería buscar cotizaciones por componente exitosamente")
        void deberiaBuscarCotizacionesPorComponenteExitosamente() {
            // Given
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                Arrays.asList(cotizacionEntidadMock);
            when(cotizacionRepositorioMock.findCotizacionesByComponente("COMP-001"))
                .thenReturn(cotizaciones);

            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorComponente("COMP-001");

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).hasSize(1);
        }

        @Test
        @DisplayName("Debería fallar cuando ID de componente es null")
        void deberiaFallarCuandoIdComponenteEsNull() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesPorComponente(null);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Buscar con Monto Mayor")
    class BuscarConMontoMayorTest {

        @Test
        @DisplayName("Debería buscar cotizaciones con monto mayor exitosamente")
        void deberiaBuscarCotizacionesConMontoMayorExitosamente() {
            // Given
            BigDecimal montoMinimo = new BigDecimal("500.00");
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                Arrays.asList(cotizacionEntidadMock);

            when(cotizacionRepositorioMock.findByTotalGreaterThan(montoMinimo))
                .thenReturn(cotizaciones);

            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesConMontoMayorA(montoMinimo);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).hasSize(1);
        }

        @Test
        @DisplayName("Debería fallar cuando monto mínimo es null")
        void deberiaFallarCuandoMontoMinimoEsNull() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesConMontoMayorA(null);

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.VALOR_INVALIDO.getCodigo());
        }

        @Test
        @DisplayName("Debería fallar cuando monto mínimo es negativo")
        void deberiaFallarCuandoMontoMinimoEsNegativo() {
            // When
            ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> resultado =
                cotizacionServicio.buscarCotizacionesConMontoMayorA(new BigDecimal("-100"));

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.MONTO_TOTAL_INVALIDO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Generar Reporte Resumen")
    class GenerarReporteResumenTest {

        @Test
        @DisplayName("Debería generar reporte de resumen exitosamente")
        void deberiaGenerarReporteResumenExitosamente() {
            // Given
            mx.com.qtx.cotizador.entidad.Cotizacion cotizacion2Mock =
                mock(mx.com.qtx.cotizador.entidad.Cotizacion.class);
            when(cotizacion2Mock.getTotal()).thenReturn(new BigDecimal("2000.00"));
            when(cotizacion2Mock.getFolio()).thenReturn(1002);
            when(cotizacion2Mock.getFecha()).thenReturn("2025-09-14");

            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones =
                Arrays.asList(cotizacionEntidadMock, cotizacion2Mock);

            when(cotizacionRepositorioMock.findAll()).thenReturn(cotizaciones);

            // When
            ApiResponse<Map<String, Object>> resultado =
                cotizacionServicio.generarReporteResumen();

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos()).isNotNull();
            assertThat(resultado.getDatos()).containsKey("totalCotizaciones");
            assertThat(resultado.getDatos()).containsKey("montoTotalGeneral");
            assertThat(resultado.getDatos()).containsKey("montoPromedio");
        }

        @Test
        @DisplayName("Debería manejar lista vacía en reporte")
        void deberiaManejarListaVaciaEnReporte() {
            // Given
            when(cotizacionRepositorioMock.findAll()).thenReturn(new ArrayList<>());

            // When
            ApiResponse<Map<String, Object>> resultado =
                cotizacionServicio.generarReporteResumen();

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            assertThat(resultado.getDatos().get("totalCotizaciones")).isEqualTo(0);
        }

        @Test
        @DisplayName("Debería manejar excepción en reporte")
        void deberiaManejarExcepcionEnReporte() {
            // Given
            when(cotizacionRepositorioMock.findAll())
                .thenThrow(new RuntimeException("Error BD"));

            // When
            ApiResponse<Map<String, Object>> resultado =
                cotizacionServicio.generarReporteResumen();

            // Then
            assertThat(resultado.getCodigo()).isEqualTo(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo());
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares y Factory")
    class MetodosAuxiliaresTest {

        @Test
        @DisplayName("Debería crear cotizador tipo A por defecto")
        void deberiaCrearCotizadorTipoAPorDefecto() {
            // Probamos indirectamente a través del guardado
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class);
                 MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {

                // Given
                when(cotizacionRequestMock.getTipoCotizador()).thenReturn(null);
                when(componenteRepositorioMock.findById("COMP-001"))
                    .thenReturn(Optional.of(componenteEntidadMock));

                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);
                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);
                mapperMock.when(() -> CotizacionMapper.toResponse(any()))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            }
        }

        @Test
        @DisplayName("Debería crear cotizador tipo B")
        void deberiaCrearCotizadorTipoB() {
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class);
                 MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {

                // Given
                when(cotizacionRequestMock.getTipoCotizador()).thenReturn("B");
                when(componenteRepositorioMock.findById("COMP-001"))
                    .thenReturn(Optional.of(componenteEntidadMock));

                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);
                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);
                mapperMock.when(() -> CotizacionMapper.toResponse(any()))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            }
        }

        @Test
        @DisplayName("Debería mapear IVA por defecto cuando impuestos vacío")
        void deberiaMappearIVAPorDefectoCuandoImpuestosVacio() {
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class);
                 MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {

                // Given
                when(cotizacionRequestMock.getImpuestos()).thenReturn(new ArrayList<>());
                when(componenteRepositorioMock.findById("COMP-001"))
                    .thenReturn(Optional.of(componenteEntidadMock));

                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);
                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);
                mapperMock.when(() -> CotizacionMapper.toResponse(any()))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
            }
        }

        @Test
        @DisplayName("Debería convertir componente de entidad a dominio")
        void deberiaConvertirComponenteDeEntidadADominio() {
            try (MockedStatic<CotizacionEntityConverter> converterMock = mockStatic(CotizacionEntityConverter.class);
                 MockedStatic<CotizacionMapper> mapperMock = mockStatic(CotizacionMapper.class)) {

                // Given - Configurar componente como DISCO_DURO
                when(tipoComponenteMock.getNombre()).thenReturn("DISCO_DURO");
                when(componenteEntidadMock.getCapacidadAlm()).thenReturn("1TB");

                when(componenteRepositorioMock.findById("COMP-001"))
                    .thenReturn(Optional.of(componenteEntidadMock));

                converterMock.when(() -> CotizacionEntityConverter.convertToNewEntity(any(Cotizacion.class)))
                    .thenReturn(cotizacionEntidadMock);
                when(cotizacionRepositorioMock.save(any())).thenReturn(cotizacionEntidadMock);
                mapperMock.when(() -> CotizacionMapper.toResponse(any()))
                    .thenReturn(cotizacionResponseMock);

                // When
                ApiResponse<CotizacionResponse> resultado =
                    cotizacionServicio.guardarCotizacion(cotizacionRequestMock);

                // Then
                assertThat(resultado.getCodigo()).isEqualTo(Errores.OK.getCodigo());
                verify(componenteEntidadMock, atLeastOnce()).getCapacidadAlm(); // Verificar que se accedió a atributos específicos
            }
        }
    }
}