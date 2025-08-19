package mx.com.qtx.cotizador.integration.pedido;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;

/**
 * Test de permisos basados en roles para el controlador de Pedidos
 * Verifica que los diferentes roles tengan los permisos correctos según la matriz de permisos
 * 
 * Matriz de Permisos para Pedidos:
 * - ADMIN: Acceso completo (Create, Read, Update, Delete, Approve, Change Status)
 * - GERENTE: Gestión y aprobación (Create, Read, Update, Approve, Change Status - no Delete)
 * - VENDEDOR: Generación y consulta (Create, Read - no Update, Delete, Approve)
 * - INVENTARIO: Gestión de cumplimiento (Create, Read, Update, Change Status - no Delete, Approve)
 * - CONSULTOR: Solo lectura (Read - no Create, Update, Delete, Approve, Change Status)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("🔐 Tests de Permisos de Roles - Pedidos")
public class PedidoRolePermissionsTest extends BaseIntegrationTest {

    private GenerarPedidoRequest validGenerarPedidoRequest;
    private static final Integer TEST_COTIZACION_ID = 1;
    private static final String TEST_PROVEEDOR_CVE = "PROV-001";
    private static final String URL_PEDIDOS = "/pedidos";

    @BeforeEach
    protected void setUp() {
        super.setUp();
        
        // Preparar request válido para generar pedido
        validGenerarPedidoRequest = new GenerarPedidoRequest();
        validGenerarPedidoRequest.setCotizacionId(TEST_COTIZACION_ID);
        validGenerarPedidoRequest.setCveProveedor(TEST_PROVEEDOR_CVE);
    }

    // ==========================================
    // TESTS PARA ROL ADMIN (Acceso Completo)
    // ==========================================

    @Test
    @DisplayName("01. ✅ ADMIN - Puede generar pedidos")
    void testAdminCanCreatePedido() {
        given()
            .auth().basic("admin", "admin123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("02. ✅ ADMIN - Puede leer pedidos específicos")
    void testAdminCanReadPedidoById() {
        given()
            .auth().basic("admin", "admin123")
        .when()
            .get(URL_PEDIDOS+"/1")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("03. ✅ ADMIN - Puede listar todos los pedidos")
    void testAdminCanReadAllPedidos() {
        given()
            .auth().basic("admin", "admin123")
        .when()
            .get(URL_PEDIDOS+"/pedidos")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA ROL GERENTE
    // ==========================================

    @Test
    @DisplayName("04. ✅ GERENTE - Puede generar pedidos")
    void testGerenteCanCreatePedido() {
        given()
            .auth().basic("gerente", "gerente123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("05. ✅ GERENTE - Puede leer pedidos")
    void testGerenteCanReadPedidos() {
        given()
            .auth().basic("gerente", "gerente123")
        .when()
            .get(URL_PEDIDOS+"/pedidos")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA ROL VENDEDOR
    // ==========================================

    @Test
    @DisplayName("06. ✅ VENDEDOR - Puede generar pedidos")
    void testVendedorCanCreatePedido() {
        given()
            .auth().basic("vendedor", "vendedor123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("07. ✅ VENDEDOR - Puede leer pedidos")
    void testVendedorCanReadPedidos() {
        given()
            .auth().basic("vendedor", "vendedor123")
        .when()
            .get(URL_PEDIDOS+"/pedidos")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("08. ✅ VENDEDOR - Puede consultar pedido específico")
    void testVendedorCanReadPedidoById() {
        given()
            .auth().basic("vendedor", "vendedor123")
        .when()
            .get(URL_PEDIDOS+"/1")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA ROL INVENTARIO
    // ==========================================

    @Test
    @DisplayName("09. ✅ INVENTARIO - Puede generar pedidos")
    void testInventarioCanCreatePedido() {
        given()
            .auth().basic("inventario", "inventario123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("10. ✅ INVENTARIO - Puede leer pedidos")
    void testInventarioCanReadPedidos() {
        given()
            .auth().basic("inventario", "inventario123")
        .when()
            .get(URL_PEDIDOS+"/pedidos")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("11. ✅ INVENTARIO - Puede consultar pedido específico")
    void testInventarioCanReadPedidoById() {
        given()
            .auth().basic("inventario", "inventario123")
        .when()
            .get(URL_PEDIDOS+"/1")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA ROL CONSULTOR (Solo Lectura)
    // ==========================================

    @Test
    @DisplayName("12. ✅ CONSULTOR - Puede leer pedidos")
    void testConsultorCanReadPedidos() {
        given()
            .auth().basic("consultor", "consultor123")
        .when()
            .get(URL_PEDIDOS+"/pedidos")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("13. ✅ CONSULTOR - Puede consultar pedido específico")
    void testConsultorCanReadPedidoById() {
        given()
            .auth().basic("consultor", "consultor123")
        .when()
            .get(URL_PEDIDOS+"/1")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("14. ❌ CONSULTOR - NO puede generar pedidos")
    void testConsultorCannotCreatePedido() {
        given()
            .auth().basic("consultor", "consultor123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(403); // Forbidden
    }

    // ==========================================
    // TESTS DE ACCESO MÚLTIPLE POR ROLES
    // ==========================================

    @Test
    @DisplayName("15. ✅ Roles con permisos CREATE pueden generar pedidos")
    void testCreateRolesCanGeneratePedidos() {
        // Test ADMIN
        given()
            .auth().basic("admin", "admin123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200);

        // Test GERENTE
        given()
            .auth().basic("gerente", "gerente123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200);

        // Test INVENTARIO
        given()
            .auth().basic("inventario", "inventario123")
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("16. ✅ Todos los roles pueden leer pedidos")
    void testAllRolesCanReadPedidos() {
        String[] users = {"admin", "gerente", "vendedor", "inventario", "consultor"};
        String[] passwords = {"admin123", "gerente123", "vendedor123", "inventario123", "consultor123"};

        for (int i = 0; i < users.length; i++) {
            given()
                .auth().basic(users[i], passwords[i])
            .when()
                .get(URL_PEDIDOS+"/pedidos")
            .then()
                .statusCode(200)
                .body("codigo", equalTo("0"));
        }
    }

    // ==========================================
    // TESTS SIN AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("17. ❌ Usuario sin autenticación NO puede acceder")
    void testUnauthenticatedUserCannotAccess() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(URL_PEDIDOS+"/pedidos")
        .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    @DisplayName("18. ❌ Usuario sin autenticación NO puede generar pedidos")
    void testUnauthenticatedUserCannotCreatePedido() {
        given()
            .contentType(ContentType.JSON)
            .body(validGenerarPedidoRequest)
        .when()
            .post(URL_PEDIDOS+"/generar")
        .then()
            .statusCode(401); // Unauthorized
    }

    // ==========================================
    // TESTS DE CASOS LÍMITE
    // ==========================================

    @Test
    @DisplayName("19. ✅ Request válido con diferentes roles")
    void testValidRequestWithDifferentRoles() {
        // Crear request válido para cada rol permitido
        String[] allowedUsers = {"admin", "gerente", "vendedor", "inventario"};
        String[] allowedPasswords = {"admin123", "gerente123", "vendedor123", "inventario123"};

        for (int i = 0; i < allowedUsers.length; i++) {
            GenerarPedidoRequest request = new GenerarPedidoRequest();
            request.setCotizacionId(TEST_COTIZACION_ID);
            request.setCveProveedor(TEST_PROVEEDOR_CVE);

            given()
                .auth().basic(allowedUsers[i], allowedPasswords[i])
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post(URL_PEDIDOS+"/generar")
            .then()
                .statusCode(200);
        }
    }

    @Test
    @DisplayName("20. ✅ Consulta de pedido específico por todos los roles")
    void testSpecificPedidoReadByAllRoles() {
        String[] users = {"admin", "gerente", "vendedor", "inventario", "consultor"};
        String[] passwords = {"admin123", "gerente123", "vendedor123", "inventario123", "consultor123"};

        for (int i = 0; i < users.length; i++) {
            given()
                .auth().basic(users[i], passwords[i])
            .when()
                .get(URL_PEDIDOS+"/1")
            .then()
                .statusCode(200);
        }
    }
}