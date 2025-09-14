package mx.com.qtx.cotizador.config;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PedidoRepositorio;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.servicio.pedido.ProveedorServicio;
import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Configuración base para mocking en pruebas unitarias del microservicio ms-cotizador-pedidos.
 *
 * <p>Esta clase proporciona una configuración estándar de mocks para los repositorios y servicios
 * más comúnmente utilizados en las pruebas unitarias. Facilita la reutilización de configuraciones
 * de mock y garantiza comportamientos consistentes a través de diferentes tests.</p>
 *
 * <h3>Mocks Configurados:</h3>
 * <ul>
 *   <li><strong>Repositorios:</strong> PedidoRepositorio, CotizacionRepositorio, ProveedorRepositorio, ComponenteRepositorio</li>
 *   <li><strong>Servicios:</strong> ProveedorServicio</li>
 *   <li><strong>Comportamientos:</strong> Respuestas exitosas por defecto para casos comunes</li>
 * </ul>
 *
 * <h3>Uso Típico:</h3>
 * <pre>{@code
 * @ExtendWith(MockitoExtension.class)
 * class PedidoServicioTest extends BaseMockConfiguration {
 *
 *     @InjectMocks
 *     private PedidoServicio pedidoServicio;
 *
 *     @Test
 *     void deberiaGenerarPedidoCorrectamente() {
 *         // Los mocks ya están configurados por BaseMockConfiguration
 *         // Solo necesitas configurar comportamientos específicos si es necesario
 *
 *         // Act & Assert
 *         // ... lógica de test
 *     }
 * }
 * }</pre>
 *
 * <h3>Métodos de Configuración:</h3>
 * <p>Proporciona métodos para configurar comportamientos específicos de mocks según las necesidades
 * de cada test, manteniendo la flexibilidad mientras reduce la duplicación de código.</p>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 2.0.0
 * @see TestUtils Utilidades para crear objetos de test
 */
public abstract class BaseMockConfiguration {

    // ==================== MOCKS DE REPOSITORIOS ====================

    /**
     * Mock del repositorio de pedidos.
     * Configurado con comportamientos básicos para operaciones CRUD.
     */
    @Mock
    protected PedidoRepositorio pedidoRepositorio;

    /**
     * Mock del repositorio de cotizaciones.
     * Configurado para retornar cotizaciones válidas en operaciones de búsqueda.
     */
    @Mock
    protected CotizacionRepositorio cotizacionRepositorio;

    /**
     * Mock del repositorio de proveedores.
     * Configurado para retornar proveedores válidos en operaciones de búsqueda.
     */
    @Mock
    protected ProveedorRepositorio proveedorRepositorio;

    /**
     * Mock del repositorio de componentes.
     * Configurado para retornar componentes válidos en operaciones de búsqueda.
     */
    @Mock
    protected ComponenteRepositorio componenteRepositorio;

    // ==================== MOCKS DE SERVICIOS ====================

    /**
     * Mock del servicio de proveedores.
     * Configurado para retornar respuestas exitosas en operaciones básicas.
     */
    @Mock
    protected ProveedorServicio proveedorServicio;

    // ==================== MÉTODOS DE CONFIGURACIÓN DE MOCKS ====================

    /**
     * Configura comportamientos básicos exitosos para todos los mocks.
     *
     * <p>Este método debe ser llamado en el método {@code @BeforeEach} de las clases de test
     * que extiendan esta configuración base. Configura respuestas por defecto que permiten
     * que los tests pasen sin configuración adicional para casos básicos.</p>
     *
     * <h4>Comportamientos configurados:</h4>
     * <ul>
     *   <li>CotizacionRepositorio: Retorna cotización válida por ID</li>
     *   <li>ProveedorServicio: Retorna respuesta exitosa para búsqueda por clave</li>
     *   <li>PedidoRepositorio: Permite operaciones save sin excepción</li>
     * </ul>
     */
    protected void configurarMocksBasicos() {
        // Configurar mock de CotizacionRepositorio
        Cotizacion cotizacionMockEntity = crearCotizacionEntityMock();
        when(cotizacionRepositorio.findById(anyInt()))
            .thenReturn(Optional.of(cotizacionMockEntity));

        // Configurar mock de ProveedorServicio
        ProveedorResponse proveedorResponse = TestUtils.crearProveedorResponseValido();
        ApiResponse<ProveedorResponse> responseExitoso = new ApiResponse<>("0", "Éxito", proveedorResponse);
        when(proveedorServicio.buscarPorClave(anyString()))
            .thenReturn(responseExitoso);

        // Configurar mock de PedidoRepositorio para operaciones save
        when(pedidoRepositorio.save(any()))
            .thenAnswer(invocation -> {
                mx.com.qtx.cotizador.entidad.Pedido pedido = invocation.getArgument(0);
                // Simular asignación de ID por la base de datos
                if (pedido.getId() == null) {
                    pedido.setNumPedido(1);
                }
                return pedido;
            });
    }

    /**
     * Configura el mock de CotizacionRepositorio para retornar null (cotización no encontrada).
     * Útil para tests de casos de error.
     */
    protected void configurarCotizacionNoEncontrada() {
        when(cotizacionRepositorio.findById(anyInt()))
            .thenReturn(Optional.empty());
    }

    /**
     * Configura el mock de ProveedorServicio para retornar error (proveedor no encontrado).
     * Útil para tests de casos de error.
     */
    protected void configurarProveedorNoEncontrado() {
        ApiResponse<ProveedorResponse> responseError = new ApiResponse<>("404", "Proveedor no encontrado");
        when(proveedorServicio.buscarPorClave(anyString()))
            .thenReturn(responseError);
    }

    /**
     * Configura comportamientos para simular excepciones en repositorios.
     * Útil para tests de manejo de errores de base de datos.
     *
     * @param excepcion La excepción a lanzar
     */
    protected void configurarExcepcionEnRepositorio(RuntimeException excepcion) {
        when(pedidoRepositorio.save(any())).thenThrow(excepcion);
        when(cotizacionRepositorio.findById(anyInt())).thenThrow(excepcion);
        when(proveedorRepositorio.findById(anyString())).thenThrow(excepcion);
    }

    /**
     * Configura mocks específicos para tests de roles y permisos.
     *
     * <p>Este método configura comportamientos que simulan respuestas del sistema de autenticación
     * y autorización, útil para tests que validan permisos por roles.</p>
     *
     * @param rolUsuario El rol del usuario simulado
     * @param tienePermiso Si el usuario tiene permisos para la operación
     */
    protected void configurarMocksParaRoles(String rolUsuario, boolean tienePermiso) {
        // Este método puede ser extendido según las necesidades específicas
        // de validación de roles en el sistema

        if (!tienePermiso) {
            // Simular falta de permisos lanzando excepción de seguridad
            when(proveedorServicio.buscarPorClave(anyString()))
                .thenThrow(new SecurityException("Acceso denegado para rol: " + rolUsuario));
        }
    }

    // ==================== MÉTODOS HELPER PARA CREAR ENTIDADES MOCK ====================

    /**
     * Crea una entidad Cotizacion mock para usar en tests.
     *
     * @return Entidad Cotizacion configurada con datos válidos
     */
    private Cotizacion crearCotizacionEntityMock() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFolio(TestUtils.DEFAULT_COTIZACION_ID);
        cotizacion.setFecha(java.time.LocalDate.now().toString());
        cotizacion.setTotal(TestUtils.DEFAULT_TOTAL_COTIZADO);
        cotizacion.setImpuestos(new java.math.BigDecimal("160.00"));

        // Agregar detalles si es necesario
        // En este caso, los detalles se manejarán a través del dominio

        return cotizacion;
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica que los mocks hayan sido llamados con los parámetros esperados.
     *
     * <p>Método de utilidad para verificar interacciones comunes con mocks
     * al final de los tests.</p>
     *
     * @param cotizacionId ID de cotización esperado
     * @param claveProveedor Clave de proveedor esperada
     */
    protected void verificarInteraccionesBasicas(Integer cotizacionId, String claveProveedor) {
        verify(cotizacionRepositorio).findById(cotizacionId);
        verify(proveedorServicio).buscarPorClave(claveProveedor);
    }

    /**
     * Verifica que se haya guardado un pedido en el repositorio.
     */
    protected void verificarPedidoGuardado() {
        verify(pedidoRepositorio, times(1)).save(any(mx.com.qtx.cotizador.entidad.Pedido.class));
    }

    /**
     * Verifica que no se hayan realizado operaciones de guardado.
     * Útil para tests de casos de error donde no se debe persistir.
     */
    protected void verificarNingunGuardado() {
        verify(pedidoRepositorio, never()).save(any());
    }

    // ==================== LIMPIEZA DE MOCKS ====================

    /**
     * Resetea todos los mocks a su estado inicial.
     *
     * <p>Este método puede ser útil si se necesita limpiar el estado de mocks
     * entre diferentes tests dentro de la misma clase.</p>
     */
    protected void resetearTodosLosMocks() {
        reset(pedidoRepositorio);
        reset(cotizacionRepositorio);
        reset(proveedorRepositorio);
        reset(componenteRepositorio);
        reset(proveedorServicio);
    }
}