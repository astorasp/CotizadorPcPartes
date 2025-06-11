package mx.com.qtx.cotizador.integration.proveedor;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;

/**
 * Tests de integración para ProveedorController
 * 
 * Configuración:
 * - Usa TestContainers con MySQL 8.4.4
 * - Puerto aleatorio para evitar conflictos
 * - Perfil de pruebas activo
 * - Autenticación básica (test/test123)
 * 
 * Casos de uso cubiertos:
 * - 4.1 Crear proveedor
 * - 4.2 Actualizar proveedor
 * - 4.3 Consultar proveedores
 * - 4.4 Eliminar proveedor
 * - Búsquedas adicionales
 * - Validaciones y manejo de errores
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ProveedorIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.4")
            .withDatabaseName("cotizador_test")
            .withUsername("cotizador_user")
            .withPassword("cotizador_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.authentication = RestAssured.basic("test", "test123");
        RestAssured.basePath = "/cotizador/v1/api";
    }

    @AfterAll
    static void stopContainer() {
        mysql.stop();
    }

    // ==================== CASO DE USO 4.1: CREAR PROVEEDOR ====================

    @Test
    @DisplayName("CU 4.1.1 - Debería crear un proveedor correctamente")
    void deberiaCrearProveedorCorrectamente() {
        String requestBody = """
            {
                "cve": "PROV001",
                "nombre": "Proveedor Test",
                "razonSocial": "Proveedor Test S.A. de C.V."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor creado exitosamente"))
            .body("datos", notNullValue())
            .log().all();
    }

    @Test
    @DisplayName("CU 4.1.2 - Debería rechazar creación con datos faltantes")
    void deberiaRechazarCreacionConDatosFaltantes() {
        String requestBody = """
            {
                "cve": "PROV002",
                "nombre": ""
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CU 4.1.3 - Debería rechazar clave duplicada")
    void deberiaRechazarClaveDuplicada() {
        // Primero crear un proveedor
        String requestBody = """
            {
                "cve": "PROV003",
                "nombre": "Proveedor Original",
                "razonSocial": "Proveedor Original S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200);

        // Intentar crear otro con la misma clave
        String requestBodyDuplicado = """
            {
                "cve": "PROV003",
                "nombre": "Proveedor Duplicado",
                "razonSocial": "Proveedor Duplicado S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBodyDuplicado)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("31"));
    }

    // ==================== CASO DE USO 4.2: ACTUALIZAR PROVEEDOR ====================

    @Test
    @DisplayName("CU 4.2.1 - Debería actualizar un proveedor correctamente")
    void deberiaActualizarProveedorCorrectamente() {
        // Primero crear un proveedor
        String requestCreacion = """
            {
                "cve": "PROV004",
                "nombre": "Proveedor Original",
                "razonSocial": "Proveedor Original S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestCreacion)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200);

        // Actualizar el proveedor
        String requestActualizacion = """
            {
                "nombre": "Proveedor Actualizado",
                "razonSocial": "Proveedor Actualizado S.A. de C.V."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestActualizacion)
        .when()
            .put("/proveedores/PROV004")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor actualizado exitosamente"))
            .body("datos.cve", equalTo("PROV004"))
            .body("datos.nombre", equalTo("Proveedor Actualizado"))
            .body("datos.razonSocial", equalTo("Proveedor Actualizado S.A. de C.V."));
    }

    @Test
    @DisplayName("CU 4.2.2 - Debería rechazar actualización de proveedor inexistente")
    void deberiaRechazarActualizacionProveedorInexistente() {
        String requestActualizacion = """
            {
                "nombre": "Proveedor Inexistente",
                "razonSocial": "Proveedor Inexistente S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestActualizacion)
        .when()
            .put("/proveedores/NOEXISTE")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("30"));
    }

    // ==================== CASO DE USO 4.3: CONSULTAR PROVEEDORES ====================

    @Test
    @DisplayName("CU 4.3.1 - Debería consultar proveedor por clave correctamente")
    void deberiaConsultarProveedorPorClave() {
        // Crear un proveedor
        String requestCreacion = """
            {
                "cve": "PROV005",
                "nombre": "Proveedor Consulta",
                "razonSocial": "Proveedor Consulta S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestCreacion)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200);

        // Consultar el proveedor
        given()
        .when()
            .get("/proveedores/PROV005")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor encontrado"))
            .body("datos.cve", equalTo("PROV005"))
            .body("datos.nombre", equalTo("Proveedor Consulta"))
            .body("datos.razonSocial", equalTo("Proveedor Consulta S.A."));
    }

    @Test
    @DisplayName("CU 4.3.2 - Debería retornar error para proveedor inexistente")
    void deberiaRetornarErrorProveedorInexistente() {
        given()
        .when()
            .get("/proveedores/INEXISTENTE")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("30"));
    }

    @Test
    @DisplayName("CU 4.3.3 - Debería listar todos los proveedores")
    void deberiaListarTodosLosProveedores() {
        // Crear algunos proveedores para la lista
        String[] proveedores = {
            """
            {
                "cve": "LISTA001",
                "nombre": "Proveedor Lista 1",
                "razonSocial": "Proveedor Lista 1 S.A."
            }
            """,
            """
            {
                "cve": "LISTA002",
                "nombre": "Proveedor Lista 2",
                "razonSocial": "Proveedor Lista 2 S.A."
            }
            """
        };

        for (String proveedor : proveedores) {
            given()
                .contentType(ContentType.JSON)
                .body(proveedor)
            .when()
                .post("/proveedores")
            .then()
                .statusCode(200);
        }

        // Listar todos los proveedores
        given()
        .when()
            .get("/proveedores")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", hasSize(greaterThan(0)));
    }

    // ==================== CASO DE USO 4.4: ELIMINAR PROVEEDOR ====================

    @Test
    @DisplayName("CU 4.4.1 - Debería eliminar un proveedor correctamente")
    void deberiaEliminarProveedorCorrectamente() {
        // Crear un proveedor
        String requestCreacion = """
            {
                "cve": "ELIM001",
                "nombre": "Proveedor Para Eliminar",
                "razonSocial": "Proveedor Para Eliminar S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestCreacion)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200);

        // Eliminar el proveedor
        given()
        .when()
            .delete("/proveedores/ELIM001")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor eliminado exitosamente"));

        // Verificar que ya no existe
        given()
        .when()
            .get("/proveedores/ELIM001")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("30"));
    }

    @Test
    @DisplayName("CU 4.4.2 - Debería rechazar eliminación de proveedor inexistente")
    void deberiaRechazarEliminacionProveedorInexistente() {
        given()
        .when()
            .delete("/proveedores/NOEXISTE")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("30"));
    }

    // ==================== CASOS DE BÚSQUEDA ADICIONALES ====================

    @Test
    @DisplayName("CU 4.5.1 - Debería buscar proveedores por nombre")
    void deberiaBuscarProveedoresPorNombre() {
        // Crear proveedores con nombres similares
        String[] proveedores = {
            """
            {
                "cve": "SEARCH001",
                "nombre": "Tecnología Moderna",
                "razonSocial": "Tecnología Moderna S.A."
            }
            """,
            """
            {
                "cve": "SEARCH002",
                "nombre": "Tecnología Avanzada",
                "razonSocial": "Tecnología Avanzada S.A."
            }
            """
        };

        for (String proveedor : proveedores) {
            given()
                .contentType(ContentType.JSON)
                .body(proveedor)
            .when()
                .post("/proveedores")
            .then()
                .statusCode(200);
        }

        // Buscar por nombre
        given()
            .queryParam("nombre", "Tecnología")
        .when()
            .get("/proveedores/buscar/nombre")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("CU 4.5.2 - Debería buscar proveedores por razón social")
    void deberiaBuscarProveedoresPorRazonSocial() {
        // Crear un proveedor
        String requestCreacion = """
            {
                "cve": "RAZON001",
                "nombre": "Proveedor Razón",
                "razonSocial": "Distribuidora Nacional de Equipos S.A. de C.V."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestCreacion)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200);

        // Buscar por razón social
        given()
            .queryParam("razonSocial", "Distribuidora")
        .when()
            .get("/proveedores/buscar/razon-social")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("CU 4.5.3 - Debería rechazar búsqueda con parámetro vacío")
    void deberiaRechazarBusquedaParametroVacio() {
        given()
            .queryParam("nombre", "")
        .when()
            .get("/proveedores/buscar/nombre")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("8"));

        given()
            .queryParam("razonSocial", "")
        .when()
            .get("/proveedores/buscar/razon-social")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("8"));
    }

    // ==================== CASOS DE VALIDACIÓN ====================

    @Test
    @DisplayName("CU 4.6.1 - Debería validar clave requerida en creación")
    void deberiaValidarClaveRequeridaCreacion() {
        String requestBody = """
            {
                "nombre": "Proveedor Sin Clave",
                "razonSocial": "Proveedor Sin Clave S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CU 4.6.2 - Debería validar longitud máxima de campos")
    void deberiaValidarLongitudMaximaCampos() {
        String requestBody = """
            {
                "cve": "CLAVEMUYLARGANOVALIDA",
                "nombre": "Proveedor Con Nombre Muy Largo Que Excede Los 100 Caracteres Permitidos Por El Sistema De Validación",
                "razonSocial": "Proveedor Con Razón Social Muy Larga Que Excede Los 200 Caracteres Permitidos Por El Sistema De Validación Y Debería Fallar En La Validación"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CU 4.7.1 - Flujo completo: Crear, consultar, actualizar y eliminar")
    void flujoCompletoProveedorCRUD() {
        String clave = "FLUJO001";
        
        // 1. Crear proveedor
        String requestCreacion = """
            {
                "cve": "%s",
                "nombre": "Proveedor Flujo",
                "razonSocial": "Proveedor Flujo S.A."
            }
            """.formatted(clave);

        given()
            .contentType(ContentType.JSON)
            .body(requestCreacion)
        .when()
            .post("/proveedores")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));

        // 2. Consultar proveedor
        given()
        .when()
            .get("/proveedores/" + clave)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.cve", equalTo(clave));

        // 3. Actualizar proveedor
        String requestActualizacion = """
            {
                "nombre": "Proveedor Flujo Actualizado",
                "razonSocial": "Proveedor Flujo Actualizado S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestActualizacion)
        .when()
            .put("/proveedores/" + clave)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.nombre", equalTo("Proveedor Flujo Actualizado"));

        // 4. Eliminar proveedor
        given()
        .when()
            .delete("/proveedores/" + clave)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));

        // 5. Verificar eliminación
        given()
        .when()
            .get("/proveedores/" + clave)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("30"));
    }

    @Test
    @DisplayName("DEBUG: Diagnóstico de serialización JSON")
    void debugSerializacionJSON() {
        
        // Arrange
        ProveedorCreateRequest request = ProveedorCreateRequest.builder()
                .cve("DEBUG001") 
                .nombre("Debug Test")
                .razonSocial("Debug Test S.A. de C.V.")
                .build();
        
        // Act & Assert - Capturar la respuesta completa
        String responseBody = given()
                .auth().basic("test", "test123")
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/proveedores")
        .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
        
        // Log de la respuesta completa para análisis
        System.out.println("=== RESPUESTA JSON COMPLETA ===");
        System.out.println(responseBody);
        System.out.println("================================");
        
        // Verificar que al menos el JSON es válido
        assertThat(responseBody).isNotEmpty();
        assertThat(responseBody).contains("codigo");
        assertThat(responseBody).contains("mensaje");
        assertThat(responseBody).contains("datos");
    }
} 