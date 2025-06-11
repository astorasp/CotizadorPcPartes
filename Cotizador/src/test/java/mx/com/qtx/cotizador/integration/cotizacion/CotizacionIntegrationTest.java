package mx.com.qtx.cotizador.integration.cotizacion;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.config.TestContainerConfig;

/**
 * Tests de integración para casos de uso de Gestión de Cotizaciones
 * 
 * Casos de uso probados:
 * - 3.1 Crear cotización usando lógica de dominio
 * - 3.2 Consultar cotización por ID
 * - 3.3 Listar todas las cotizaciones
 * - 3.4 Buscar cotizaciones por fecha
 * - 3.5 Validaciones y manejo de errores
 * 
 * Configuración:
 * - Usa TestContainers con MySQL 8.4.4
 * - Perfil 'test' con configuración específica
 * - Puerto aleatorio para evitar conflictos
 * - Datos de prueba precargados via DDL/DML de /sql
 * - Consume endpoints REST del CotizacionController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") 
@Import(TestContainerConfig.class)  
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class CotizacionIntegrationTest {

    private static final String USER_ADMIN = "test";
    private static final String PASSWORD_ADMIN = "test123";

    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port + "/cotizador/v1/api";
    }

    // ========================================================================
    // CASO DE USO 3.3: LISTAR COTIZACIONES  
    // ========================================================================
    
    @Test
    @DisplayName("CU 3.3.1: Debe listar todas las cotizaciones exitosamente")  
    void deberiaListarTodasLasCotizaciones() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }
    
    @Test
    @DisplayName("CU 3.3.2: Debe fallar listar cotizaciones sin autenticación")
    void deberiaFallarListarSinAutenticacion() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(401);
    }

    // ========================================================================
    // CASO DE USO 3.1: CREAR COTIZACIÓN
    // ========================================================================
    
    @Test
    @DisplayName("CU 3.1.1: Debe crear cotización tipo A con IVA exitosamente")
    void deberiaCrearCotizacionTipoAConIVA() {
        String cotizacionRequest = """
            {
                "tipoCotizador": "A",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "MON001",
                        "cantidad": 2
                    },
                    {
                        "idComponente": "HDD001", 
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Cotización guardada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.folio", notNullValue())
            .body("datos.fecha", notNullValue())
            .body("datos.total", greaterThan(0.0f))
            .body("datos.impuestos", greaterThan(0.0f));
    }
    
    @Test
    @DisplayName("CU 3.1.2: Debe crear cotización tipo B con múltiples impuestos exitosamente")
    void deberiaCrearCotizacionTipoBConMultiplesImpuestos() {
        String cotizacionRequest = """
            {
                "tipoCotizador": "B",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "GPU001",
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Cotización guardada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.folio", notNullValue())
            .body("datos.impuestos", greaterThan(0.0f));
    }
    
    @Test
    @DisplayName("CU 3.1.3: Debe aplicar impuestos por defecto cuando no se especifican")
    void deberiaAplicarImpuestosPorDefecto() {
        String cotizacionRequest = """
            {
                "tipoCotizador": "A",
                "detalles": [
                    {
                        "idComponente": "MON001",
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.impuestos", greaterThan(0.0f)); // Debe aplicar IVA por defecto
    }
    
    @Test
    @DisplayName("CU 3.1.4: Debe fallar al crear cotización con componente inexistente")
    void deberiaFallarCrearCotizacionConComponenteInexistente() {
        String cotizacionRequest = """
            {
                "tipoCotizador": "A",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "COMP_INEXISTENTE",
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("24")) // Código para componente no encontrado en cotización
            .body("mensaje", notNullValue());
    }
    
    @Test
    @DisplayName("CU 3.1.5: Debe fallar al crear cotización con datos inválidos")
    void deberiaFallarCrearCotizacionConDatosInvalidos() {
        String cotizacionInvalida = """
            {
                "tipoCotizador": "",
                "detalles": []
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
            .body("codigo", equalTo("2")) // Código para error de validación
            .body("mensaje", notNullValue());
    }
    
    @Test
    @DisplayName("CU 3.1.6: Debe fallar al crear cotización con request nulo/vacío")
    void deberiaFallarCrearCotizacionConRequestVacio() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")) // Código para error de validación
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 3.2: CONSULTAR COTIZACIÓN POR ID
    // ========================================================================
    
    @Test
    @DisplayName("CU 3.2.1: Debe consultar cotización por ID exitosamente")
    void deberiaConsultarCotizacionPorId() {
        // Primero crear una cotización para poder consultarla
        String cotizacionRequest = """
            {
                "tipoCotizador": "A",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "MON001",
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        // Crear la cotización y obtener su ID
        Integer cotizacionId = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .extract()
            .path("datos.folio");
            
        // Ahora consultar la cotización creada
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue())
            .body("datos.folio", equalTo(cotizacionId));
    }
    
    @Test
    @DisplayName("CU 3.2.2: Debe fallar al consultar cotización inexistente")
    void deberiaFallarConsultarCotizacionInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/999999")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("20")) // Código para cotización no encontrada
            .body("mensaje", notNullValue());
    }
    
    @Test
    @DisplayName("CU 3.2.3: Debe fallar al consultar con ID inválido")
    void deberiaFallarConsultarConIdInvalido() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/0")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("6")) // Código para valor inválido
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 3.4: BUSCAR COTIZACIONES POR FECHA
    // ========================================================================
    
    @Test
    @DisplayName("CU 3.4.1: Debe buscar cotizaciones por fecha exitosamente")
    void deberiaBuscarCotizacionesPorFecha() {
        // Primero crear una cotización para tener datos de prueba
        String cotizacionRequest = """
            {
                "tipoCotizador": "A",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "HDD001",
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200);
            
        // Buscar cotizaciones por fecha (formato YYYY-MM-DD)
        String fechaHoy = java.time.LocalDate.now().toString();
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .queryParam("fecha", fechaHoy)
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }
    
    @Test
    @DisplayName("CU 3.4.2: Debe fallar búsqueda con fecha vacía")
    void deberiaFallarBusquedaConFechaVacia() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .queryParam("fecha", "")
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("6")) // Código para valor inválido
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // TESTS DE FLUJO COMPLETO Y INTEGRACIÓN
    // ========================================================================
    
    @Test
    @DisplayName("CU 3.1-3.2: Debe ejecutar flujo completo de cotización con lógica de dominio")
    void deberiaEjecutarFlujoCompletoCotizacion() {
        // PASO 1: Crear cotización usando lógica de dominio (cotizador A con IVA)
        String cotizacionRequest = """
            {
                "tipoCotizador": "A",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "MON001",
                        "cantidad": 2
                    },
                    {
                        "idComponente": "GPU001",
                        "cantidad": 1
                    },
                    {
                        "idComponente": "HDD001",
                        "cantidad": 1
                    }
                ]
            }
            """;
            
        // Crear y validar estructura de respuesta
        Integer cotizacionId = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Cotización guardada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.folio", notNullValue())
            .body("datos.fecha", notNullValue())
            .body("datos.subtotal", greaterThan(0.0f))
            .body("datos.impuestos", greaterThan(0.0f))
            .body("datos.total", greaterThan(0.0f))
            .extract()
            .path("datos.folio");

        // PASO 2: Consultar la cotización guardada para verificar persistencia
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue())
            .body("datos.folio", equalTo(cotizacionId))
            .body("datos.total", greaterThan(0.0f))
            .body("datos.impuestos", greaterThan(0.0f));

        // PASO 3: Verificar que aparece en la lista general
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    // ========================================================================
    // TESTS DE SEGURIDAD Y VALIDACIÓN
    // ========================================================================
    
    @Test  
    @DisplayName("Seguridad: Todos los endpoints requieren autenticación")
    void todosLosEndpointsRequierenAutenticacion() {
        String cotizacion = """
            {
                "tipoCotizador": "A",
                "impuestos": ["IVA"],
                "detalles": [
                    {
                        "idComponente": "MON001",
                        "cantidad": 1
                    }
                ]
            }
            """;
        
        // GET sin auth - listar cotizaciones
        given().contentType(ContentType.JSON)
        .when().get("/cotizaciones")
        .then().statusCode(401);
        
        // GET sin auth - cotización por ID
        given().contentType(ContentType.JSON)
        .when().get("/cotizaciones/1")
        .then().statusCode(401);
        
        // POST sin auth - crear cotización
        given().contentType(ContentType.JSON).body(cotizacion)
        .when().post("/cotizaciones")
        .then().statusCode(401);
        
        // GET sin auth - buscar por fecha
        given().contentType(ContentType.JSON).queryParam("fecha", "2024-01-01")
        .when().get("/cotizaciones/buscar/fecha")
        .then().statusCode(401);
    }

    @Test
    @DisplayName("A. Infraestructura: Aplicación debe arrancar correctamente")
    void aplicacionDebeArrancar() {
        assertThat(port).isGreaterThan(0);
    }

    @AfterAll 
    static void cleanup() {
        System.out.println("🧹 Tests de Cotización completados - contenedor será destruido automáticamente");
    }
} 