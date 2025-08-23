package mx.com.qtx.cotizador.integration.proveedor;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;

/**
 * Tests de integración para ProveedorController
 * 
 * Casos de uso cubiertos:
 * - 4.1 Crear proveedor
 * - 4.2 Actualizar proveedor
 * - 4.3 Consultar proveedores
 * - 4.4 Eliminar proveedor
 * - Búsquedas adicionales
 * - Validaciones y manejo de errores
 * 
 * Usa base de datos MySQL compartida via BaseIntegrationTest.
 * Configuración y datos de prueba heredados automáticamente.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ProveedorIntegrationTest extends BaseIntegrationTest {

    // Base path para el API de proveedores
    private static final String PROVEEDORES_API_PATH = "/api/v1/proveedores";

    // ✅ Configuración heredada de BaseIntegrationTest:
    // - Base de datos MySQL compartida
    // - RestAssured configurado automáticamente  
    // - Autenticación (test/test123)
    // - Puerto aleatorio
    // - Scripts DDL + DML precargados

    // ==================== CASO DE USO 4.1: CREAR PROVEEDOR ====================

    @Test
    @DisplayName("CU 4.1.1 - Debería crear un proveedor correctamente")
    void deberiaCrearProveedorCorrectamente() {
        String requestBody = """
            {
                "cve": "PROV999",
                "nombre": "Proveedor Test",
                "razonSocial": "Proveedor Test S.A. de C.V."
            }
            """;

        given()
            .contentType(ContentType.JSON) 
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestBody)
        .when()
            .post(PROVEEDORES_API_PATH)
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestBody)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CU 4.1.3 - Debería rechazar clave duplicada")
    void deberiaRechazarClaveDuplicada() {
        // Primero crear un proveedor con clave nueva (no existe en DML)
        String requestBody = """
            {
                "cve": "PROV099",
                "nombre": "Proveedor Original",
                "razonSocial": "Proveedor Original S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestBody)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200);

        // Intentar crear otro con la misma clave
        String requestBodyDuplicado = """
            {
                "cve": "PROV099",
                "nombre": "Proveedor Duplicado",
                "razonSocial": "Proveedor Duplicado S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestBodyDuplicado)
        .when()
            .post(PROVEEDORES_API_PATH)
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
                "cve": "PROV006",
                "nombre": "Proveedor Original",
                "razonSocial": "Proveedor Original S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestCreacion)
        .when()
            .post(PROVEEDORES_API_PATH)
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestActualizacion)
        .when()
            .put(PROVEEDORES_API_PATH + "/PROV006")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor actualizado exitosamente"))
            .body("datos.cve", equalTo("PROV006"))
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestActualizacion)
        .when()
            .put(PROVEEDORES_API_PATH + "/NOEXISTE")
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
                "cve": "PROV007",
                "nombre": "Proveedor Consulta",
                "razonSocial": "Proveedor Consulta S.A."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestCreacion)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200);

        // Consultar el proveedor
        given()
        .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/PROV007")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor encontrado"))
            .body("datos.cve", equalTo("PROV007"))
            .body("datos.nombre", equalTo("Proveedor Consulta"))
            .body("datos.razonSocial", equalTo("Proveedor Consulta S.A."));
    }

    @Test
    @DisplayName("CU 4.3.2 - Debería retornar error para proveedor inexistente")
    void deberiaRetornarErrorProveedorInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/INEXISTENTE")
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
                .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
                .body(proveedor)
            .when()
                .post(PROVEEDORES_API_PATH)
            .then()
                .statusCode(200);
        }

        // Listar todos los proveedores
        given()
        .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH)
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestCreacion)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200);

        // Eliminar el proveedor
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .delete(PROVEEDORES_API_PATH + "/ELIM001")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Proveedor eliminado exitosamente"));

        // Verificar que ya no existe
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/ELIM001")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("30"));
    }

    @Test
    @DisplayName("CU 4.4.2 - Debería rechazar eliminación de proveedor inexistente")
    void deberiaRechazarEliminacionProveedorInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .delete(PROVEEDORES_API_PATH + "/NOEXISTE")
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
                .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
                .body(proveedor)
            .when()
                .post(PROVEEDORES_API_PATH)
            .then()
                .statusCode(200);
        }

        // Buscar por nombre
        given()
            .queryParam("nombre", "Tecnología")
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/nombre")
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestCreacion)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200);

        // Buscar por razón social
        given()
            .queryParam("razonSocial", "Distribuidora")
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/razon-social")
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/nombre")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("8"));

        given()
            .queryParam("razonSocial", "")
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/razon-social")
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestBody)
        .when()
            .post(PROVEEDORES_API_PATH)
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestBody)
        .when()
            .post(PROVEEDORES_API_PATH)
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestCreacion)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));

        // 2. Consultar proveedor
        given()
        .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/" + clave)
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
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(requestActualizacion)
        .when()
            .put(PROVEEDORES_API_PATH + "/" + clave)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.nombre", equalTo("Proveedor Flujo Actualizado"));

        // 4. Eliminar proveedor
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .delete(PROVEEDORES_API_PATH + "/" + clave)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));

        // 5. Verificar eliminación
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROVEEDORES_API_PATH + "/" + clave)
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
                .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(PROVEEDORES_API_PATH)
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