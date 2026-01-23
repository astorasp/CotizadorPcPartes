package mx.com.qtx.cotizador.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Primary;

import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;

/**
 * Configuración de mocks para tests de servicios.
 *
 * <p>Esta configuración proporciona mocks de las principales dependencias
 * necesarias para testear la capa de servicios, particularmente CotizacionServicio.</p>
 *
 * <h3>Mocks proporcionados:</h3>
 * <ul>
 * <li>{@link CotizacionRepositorio} - Mock del repositorio de cotizaciones</li>
 * <li>{@link ComponenteRepositorio} - Mock del repositorio de componentes</li>
 * </ul>
 *
 * <h3>Uso típico:</h3>
 * <pre>{@code
 * @SpringBootTest
 * @Import(TestServiceConfig.class)
 * class CotizacionServicioTest {
 *
 *     @MockBean
 *     private CotizacionRepositorio cotizacionRepo;
 *
 *     @MockBean
 *     private ComponenteRepositorio componenteRepo;
 *
 *     @Test
 *     void test_guardarCotizacion() {
 *         // Configurar mocks
 *         when(componenteRepo.findById(anyString())).thenReturn(Optional.of(componenteMock));
 *         when(cotizacionRepo.save(any())).thenReturn(cotizacionMock);
 *
 *         // Ejecutar test
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <h3>Beneficios:</h3>
 * <ul>
 * <li><strong>Aislamiento:</strong> Tests de servicio sin dependencias de BD</li>
 * <li><strong>Performance:</strong> Tests rápidos sin I/O real</li>
 * <li><strong>Control:</strong> Comportamiento predecible de dependencias</li>
 * <li><strong>Flexibilidad:</strong> Fácil simulación de escenarios de error</li>
 * </ul>
 *
 * <h3>Configuración recomendada por test:</h3>
 * <pre>{@code
 * @BeforeEach
 * void setUp() {
 *     // Reset de mocks antes de cada test
 *     Mockito.reset(cotizacionRepo, componenteRepo);
 *
 *     // Configuración común de mocks
 *     configurarMocksComunes();
 * }
 *
 * private void configurarMocksComunes() {
 *     // Comportamiento estándar de mocks
 *     when(componenteRepo.findById(anyString()))
 *         .thenReturn(Optional.of(crearComponenteMock()));
 *
 *     when(cotizacionRepo.save(any()))
 *         .thenAnswer(invocation -> invocation.getArgument(0));
 * }
 * }</pre>
 *
 * @author Sistema Cotizador - Testing Team
 * @version 1.0
 * @since 2.0.0
 * @see CotizacionServicio
 * @see TestContainerConfig
 * @see TestSecurityConfig
 */
@TestConfiguration
public class TestServiceConfig {

    // ==================== BEAN MOCKS PARA TESTS ====================

    /**
     * Bean mock del repositorio de cotizaciones.
     *
     * <p>Este bean mock permite simular todas las operaciones CRUD y consultas
     * específicas del repositorio de cotizaciones sin acceder a la base de datos real.</p>
     *
     * <h4>Métodos principales a mockear:</h4>
     * <ul>
     * <li>{@code save(Cotizacion)} - Simulación de guardado</li>
     * <li>{@code findById(Integer)} - Búsqueda por ID</li>
     * <li>{@code findAll()} - Listado completo</li>
     * <li>{@code findByFechaContaining(String)} - Búsqueda por fecha</li>
     * <li>{@code findByTotalBetween(BigDecimal, BigDecimal)} - Búsqueda por rango</li>
     * <li>{@code findCotizacionesByComponente(String)} - Búsqueda por componente</li>
     * <li>{@code findByTotalGreaterThan(BigDecimal)} - Búsqueda por monto mínimo</li>
     * </ul>
     *
     * @return Mock del repositorio de cotizaciones
     */
    @MockBean
    public CotizacionRepositorio cotizacionRepositorio;

    /**
     * Bean mock del repositorio de componentes.
     *
     * <p>Este bean mock permite simular la búsqueda y validación de componentes
     * necesarios para crear cotizaciones, sin acceder a datos replicados reales.</p>
     *
     * <h4>Métodos principales a mockear:</h4>
     * <ul>
     * <li>{@code findById(String)} - Búsqueda de componente por ID</li>
     * <li>{@code findAll()} - Listado de todos los componentes</li>
     * <li>{@code existsById(String)} - Verificación de existencia</li>
     * </ul>
     *
     * @return Mock del repositorio de componentes
     */
    @MockBean
    public ComponenteRepositorio componenteRepositorio;
}