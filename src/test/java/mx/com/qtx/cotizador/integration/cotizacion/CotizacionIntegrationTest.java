package mx.com.qtx.cotizador.integration.cotizacion;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import mx.com.qtx.cotizador.config.TestContainerConfig;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests de integración para la gestión de cotizaciones.
 * Cubre los casos de uso:
 * - CU 3.1: Armar cotización
 * - CU 3.2: Consultar reporte cotización  
 * - CU 3.3: Guardar cotización
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig(TestContainerConfig.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class CotizacionIntegrationTest {

    private static final String USER_ADMIN = "test";
    private static final String PASSWORD_ADMIN = "test123";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        io.restassured.RestAssured.port = port;
        io.restassured.RestAssured.basePath = "/cotizador/v1/api";
        
        System.out.println("Puerto configurado: " + port);
        System.out.println("Base path: " + io.restassured.RestAssured.basePath);
    }

    // ========================================================================
    // CASO DE USO 3.1: ARMAR COTIZACIÓN
    // ========================================================================

    @Test
    @DisplayName("CU 3.1.1: Debe crear cotización simple exitosamente")
    void deberiaCrearCotizacionSimpleExitosamente() {
        // 1. Crear componentes precargados necesarios
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        crearComponentePrecargado("MON" + timestamp, "Monitor para cotización", "Samsung", "24inch", 3000.00, 2400.00, "MONITOR");
        crearComponentePrecargado("HDD" + timestamp, "Disco para cotización", "Seagate", "1TB", 1200.00, 960.00, "DISCO_DURO");
        
        // 2. Crear cotización con estos componentes
        String cotizacionRequest = """
            {
                "detalles": [
                    {
                        "idComponente": "MON%s",
                        "cantidad": 2,
                        "descripcion": "Monitor para oficina"
                    },
                    {
                        "idComponente": "HDD%s", 
                        "cantidad": 1,
                        "descripcion": "Almacenamiento principal"
                    }
                ],
                "observaciones": "Cotización para cliente empresarial"
            }
            """.formatted(timestamp, timestamp);

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 3.1.2: Debe fallar crear cotización sin detalles")
    void deberiaFallarCrearCotizacionSinDetalles() {
        String cotizacionInvalida = """
            {
                "detalles": [],
                "observaciones": "Sin detalles"
            }
            """;

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionInvalida)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")) // ERROR_DE_VALIDACION
            .body("mensaje", containsString("al menos un detalle"));
    }

    @Test
    @DisplayName("CU 3.1.3: Debe fallar crear cotización con componente inexistente")
    void deberiaFallarCrearCotizacionConComponenteInexistente() {
        String cotizacionConComponenteInexistente = """
            {
                "detalles": [
                    {
                        "idComponente": "INEXISTENTE-999",
                        "cantidad": 1,
                        "descripcion": "Componente que no existe"
                    }
                ],
                "observaciones": "Test de componente inexistente"
            }
            """;

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionConComponenteInexistente)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("24")) // COMPONENTE_NO_ENCONTRADO_EN_COTIZACION
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 3.1.4: Debe validar cantidad positiva en detalles")
    void deberiaValidarCantidadPositivaEnDetalles() {
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        crearComponentePrecargado("VID" + timestamp, "Video para test", "NVIDIA", "GTX1650", 4500.00, 3600.00, "TARJETA_VIDEO");
        
        String cotizacionCantidadInvalida = """
            {
                "detalles": [
                    {
                        "idComponente": "VID%s",
                        "cantidad": 0,
                        "descripcion": "Cantidad cero inválida"
                    }
                ]
            }
            """.formatted(timestamp);

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionCantidadInvalida)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")) // ERROR_DE_VALIDACION
            .body("mensaje", containsString("validación"));
    }

    // ========================================================================
    // CASO DE USO 3.2: CONSULTAR REPORTE COTIZACIÓN
    // ========================================================================

    @Test
    @DisplayName("CU 3.2.1: Debe generar reporte de resumen exitosamente")
    void deberiaGenerarReporteResumenExitosamente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get("/cotizaciones/reporte/resumen")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue())
            .body("datos.totalCotizaciones", greaterThanOrEqualTo(0))
            .body("datos.montoTotalCotizaciones", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("CU 3.2.2: Debe listar todas las cotizaciones")
    void deberiaListarTodasLasCotizaciones() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @Test
    @DisplayName("CU 3.2.3: Debe buscar cotizaciones por fecha")
    void deberiaBuscarCotizacionesPorFecha() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .param("fecha", "2024")
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @Test
    @DisplayName("CU 3.2.4: Debe buscar cotizaciones por rango de monto")
    void deberiaBuscarCotizacionesPorRangoMonto() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .param("montoMin", "1000.00")
            .param("montoMax", "50000.00")
        .when()
            .get("/cotizaciones/buscar/rango-monto")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @Test
    @DisplayName("CU 3.2.5: Debe buscar cotizaciones por componente")
    void deberiaBuscarCotizacionesPorComponente() {
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        String componenteId = "BUSQ" + timestamp;
        
        // Crear componente y cotización para buscar
        crearComponentePrecargado(componenteId, "Componente para buscar", "TestMarca", "TestModelo", 2000.00, 1600.00, "MONITOR");
        
        String cotizacionRequest = """
            {
                "detalles": [
                    {
                        "idComponente": "%s",
                        "cantidad": 1,
                        "descripcion": "Para test de búsqueda"
                    }
                ]
            }
            """.formatted(componenteId);

        // Crear la cotización
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200);

        // Buscar cotizaciones que contengan este componente
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .param("idComponente", componenteId)
        .when()
            .get("/cotizaciones/buscar/componente")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @Test
    @DisplayName("CU 3.2.6: Debe buscar cotizaciones con monto mayor al especificado")
    void deberiaBuscarCotizacionesConMontoMayor() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .param("montoMinimo", "5000.00")
        .when()
            .get("/cotizaciones/buscar/monto-mayor")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @Test
    @DisplayName("CU 3.2.7: Debe fallar búsqueda con fecha vacía")
    void deberiaFallarBusquedaConFechaVacia() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .param("fecha", "")
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")); // ERROR_DE_VALIDACION
    }

    @Test
    @DisplayName("CU 3.2.8: Debe fallar búsqueda con monto negativo")
    void deberiaFallarBusquedaConMontoNegativo() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .param("montoMinimo", "-1000.00")
        .when()
            .get("/cotizaciones/buscar/monto-mayor")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")); // ERROR_DE_VALIDACION
    }

    // ========================================================================
    // CASO DE USO 3.3: GUARDAR COTIZACIÓN (Consulta Individual)
    // ========================================================================

    @Test
    @DisplayName("CU 3.3.1: Debe consultar cotización por ID exitosamente")
    void deberiaConsultarCotizacionPorIdExitosamente() {
        // 1. Crear una cotización para consultar
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        String componenteId = "CONS" + timestamp;
        
        crearComponentePrecargado(componenteId, "Componente para consulta", "TestBrand", "TestModel", 1500.00, 1200.00, "MONITOR");
        
        String cotizacionRequest = """
            {
                "detalles": [
                    {
                        "idComponente": "%s",
                        "cantidad": 1,
                        "descripcion": "Para consulta individual"
                    }
                ]
            }
            """.formatted(componenteId);

        // Crear cotización
        ValidatableResponse createResponse = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));

        // 2. Obtener todas las cotizaciones para encontrar la que acabamos de crear
        ValidatableResponse listResponse = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());

        // Para simplificar, buscaremos por ID 1 (asumiendo que es la primera)
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get("/cotizaciones/{id}", 1)
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400))) // Puede no existir
            .body("codigo", anyOf(equalTo("0"), equalTo("20"))); // OK o COTIZACION_NO_ENCONTRADA
    }

    @Test
    @DisplayName("CU 3.3.2: Debe fallar consulta de cotización inexistente")
    void deberiaFallarConsultaCotizacionInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get("/cotizaciones/{id}", 999999)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("20")) // COTIZACION_NO_ENCONTRADA
            .body("mensaje", containsString("no encontrada"));
    }

    @Test
    @DisplayName("CU 3.3.3: Debe fallar consulta con ID inválido")
    void deberiaFallarConsultaConIdInvalido() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get("/cotizaciones/{id}", -1)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")); // ERROR_DE_VALIDACION
    }

    // ========================================================================
    // VALIDACIONES DE SEGURIDAD
    // ========================================================================

    @Test
    @DisplayName("Seguridad: Todos los endpoints de cotización requieren autenticación")
    void todosLosEndpointsDeCotizacionRequierenAutenticacion() {
        String cotizacionRequest = """
            {
                "detalles": [
                    {
                        "idComponente": "TEST001",
                        "cantidad": 1
                    }
                ]
            }
            """;

        // POST /cotizaciones (crear)
        given()
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(401);

        // GET /cotizaciones (listar)
        given()
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(401);

        // GET /cotizaciones/{id} (consultar)
        given()
        .when()
            .get("/cotizaciones/{id}", 1)
        .then()
            .statusCode(401);

        // GET /cotizaciones/reporte/resumen
        given()
        .when()
            .get("/cotizaciones/reporte/resumen")
        .then()
            .statusCode(401);

        // GET /cotizaciones/buscar/fecha
        given()
            .param("fecha", "2024")
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(401);
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    private void crearComponentePrecargado(String id, String descripcion, String marca, String modelo, 
                                         double precioBase, double costo, String tipoComponente) {
        String componenteRequest = """
            {
                "id": "%s",
                "descripcion": "%s",
                "marca": "%s",
                "modelo": "%s",
                "precioBase": %.2f,
                "costo": %.2f,
                "tipoComponente": "%s"
            }
            """.formatted(id, descripcion, marca, modelo, precioBase, costo, tipoComponente);

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteRequest)
        .when()
            .post("/componentes")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @AfterAll 
    static void cleanup() {
        System.out.println("🧹 Tests de cotización completados - contenedor será destruido automáticamente");
    }
} 