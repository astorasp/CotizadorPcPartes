package mx.com.qtx.cotizador.integration.proveedor;

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
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorUpdateRequest;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;

/**
 * Test de permisos basados en roles para el controlador de Proveedores
 * Verifica que los diferentes roles tengan los permisos correctos según la matriz de permisos
 * 
 * Matriz de Permisos para Proveedores:
 * - ADMIN: Full CRUD (Create, Read, Update, Delete) + Búsquedas
 * - GERENTE: Create, Read, Update, Búsquedas (no Delete)
 * - VENDEDOR: Read-only + Búsquedas
 * - INVENTARIO: Create, Read, Update, Búsquedas (no Delete)
 * - CONSULTOR: Read-only + Búsquedas
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("🔐 Tests de Permisos de Roles - Proveedores")
public class ProveedorRolePermissionsTest extends BaseIntegrationTest {

    private ProveedorCreateRequest validCreateRequest;
    private ProveedorUpdateRequest validUpdateRequest;
    private static final String TEST_PROVEEDOR_CVE = "PERM_TEST_001";
    private static final String PROVEEDORES_API_PATH = "/proveedores";

    @BeforeEach
    protected void setUp() {
        super.setUp();
        
        // Preparar request válido para crear proveedor
        validCreateRequest = new ProveedorCreateRequest();
        validCreateRequest.setCve(TEST_PROVEEDOR_CVE);
        validCreateRequest.setNombre("Proveedor Test Permisos");
        validCreateRequest.setRazonSocial("Proveedor Test Permisos S.A. de C.V.");
        
        // Preparar request válido para actualizar proveedor
        validUpdateRequest = new ProveedorUpdateRequest();
        validUpdateRequest.setNombre("Proveedor Test Actualizado");
        validUpdateRequest.setRazonSocial("Proveedor Test Actualizado S.A. de C.V.");
    }

    // ==========================================
    // TESTS PARA ROL ADMIN (Acceso Completo)
    // ==========================================

    @Test
    @DisplayName("01. ✅ ADMIN - Puede crear proveedores")
    void testAdminCanCreateProveedor() {
        given()
            .auth().basic("admin", "admin123")
            .contentType(ContentType.JSON)
            .body(validCreateRequest)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("02. ✅ ADMIN - Puede leer proveedores")
    void testAdminCanReadProveedores() {
        // Primero crear un proveedor para leer
        createTestProveedor();
        
        given()
            .auth().basic("admin", "admin123")
        .when()
            .get(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("03. ✅ ADMIN - Puede actualizar proveedores")
    void testAdminCanUpdateProveedor() {
        // Primero crear un proveedor para actualizar
        createTestProveedor();
        
        given()
            .auth().basic("admin", "admin123")
            .contentType(ContentType.JSON)
            .body(validUpdateRequest)
        .when()
            .put(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("04. ✅ ADMIN - Puede eliminar proveedores")
    void testAdminCanDeleteProveedor() {
        // Primero crear un proveedor para eliminar
        createTestProveedor();
        
        given()
            .auth().basic("admin", "admin123")
        .when()
            .delete(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("05. ✅ ADMIN - Puede realizar búsquedas")
    void testAdminCanSearchProveedores() {
        given()
            .auth().basic("admin", "admin123")
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/nombre?nombre=Test")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA ROL GERENTE
    // ==========================================

    @Test
    @DisplayName("06. ✅ GERENTE - Puede crear proveedores")
    void testGerenteCanCreateProveedor() {
        validCreateRequest.setCve("GERENTE_TEST_001");
        
        given()
            .auth().basic("gerente", "gerente123")
            .contentType(ContentType.JSON)
            .body(validCreateRequest)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("07. ✅ GERENTE - Puede actualizar proveedores")
    void testGerenteCanUpdateProveedor() {
        createTestProveedor();
        
        given()
            .auth().basic("gerente", "gerente123")
            .contentType(ContentType.JSON)
            .body(validUpdateRequest)
        .when()
            .put(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("08. ❌ GERENTE - NO puede eliminar proveedores")
    void testGerenteCannotDeleteProveedor() {
        createTestProveedor();
        
        given()
            .auth().basic("gerente", "gerente123")
        .when()
            .delete(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(403); // Forbidden
    }

    // ==========================================
    // TESTS PARA ROL VENDEDOR (Solo Lectura)
    // ==========================================

    @Test
    @DisplayName("09. ✅ VENDEDOR - Puede leer proveedores")
    void testVendedorCanReadProveedores() {
        createTestProveedor();
        
        given()
            .auth().basic("vendedor", "vendedor123")
        .when()
            .get(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("10. ❌ VENDEDOR - NO puede crear proveedores")
    void testVendedorCannotCreateProveedor() {
        validCreateRequest.setCve("VENDEDOR_TEST_001");
        
        given()
            .auth().basic("vendedor", "vendedor123")
            .contentType(ContentType.JSON)
            .body(validCreateRequest)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(403); // Forbidden
    }

    @Test
    @DisplayName("11. ❌ VENDEDOR - NO puede actualizar proveedores")
    void testVendedorCannotUpdateProveedor() {
        createTestProveedor();
        
        given()
            .auth().basic("vendedor", "vendedor123")
            .contentType(ContentType.JSON)
            .body(validUpdateRequest)
        .when()
            .put(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(403); // Forbidden
    }

    @Test
    @DisplayName("12. ❌ VENDEDOR - NO puede eliminar proveedores")
    void testVendedorCannotDeleteProveedor() {
        createTestProveedor();
        
        given()
            .auth().basic("vendedor", "vendedor123")
        .when()
            .delete(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(403); // Forbidden
    }

    // ==========================================
    // TESTS PARA ROL INVENTARIO
    // ==========================================

    @Test
    @DisplayName("13. ✅ INVENTARIO - Puede crear proveedores")
    void testInventarioCanCreateProveedor() {
        validCreateRequest.setCve("INVENTARIO_TEST_001");
        
        given()
            .auth().basic("inventario", "inventario123")
            .contentType(ContentType.JSON)
            .body(validCreateRequest)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("14. ✅ INVENTARIO - Puede actualizar proveedores")
    void testInventarioCanUpdateProveedor() {
        createTestProveedor();
        
        given()
            .auth().basic("inventario", "inventario123")
            .contentType(ContentType.JSON)
            .body(validUpdateRequest)
        .when()
            .put(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("15. ❌ INVENTARIO - NO puede eliminar proveedores")
    void testInventarioCannotDeleteProveedor() {
        createTestProveedor();
        
        given()
            .auth().basic("inventario", "inventario123")
        .when()
            .delete(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(403); // Forbidden
    }

    // ==========================================
    // TESTS PARA ROL CONSULTOR (Solo Lectura)
    // ==========================================

    @Test
    @DisplayName("16. ✅ CONSULTOR - Puede leer proveedores")
    void testConsultorCanReadProveedores() {
        createTestProveedor();
        
        given()
            .auth().basic("consultor", "consultor123")
        .when()
            .get(PROVEEDORES_API_PATH + "/" + TEST_PROVEEDOR_CVE)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("17. ❌ CONSULTOR - NO puede crear proveedores")
    void testConsultorCannotCreateProveedor() {
        validCreateRequest.setCve("CONSULTOR_TEST_001");
        
        given()
            .auth().basic("consultor", "consultor123")
            .contentType(ContentType.JSON)
            .body(validCreateRequest)
        .when()
            .post(PROVEEDORES_API_PATH)
        .then()
            .statusCode(403); // Forbidden
    }

    // ==========================================
    // TESTS DE BÚSQUEDAS (Todos los Roles)
    // ==========================================

    @Test
    @DisplayName("18. ✅ Todos los roles pueden buscar por nombre")
    void testAllRolesCanSearchByName() {
        // Test GERENTE
        given()
            .auth().basic("gerente", "gerente123")
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/nombre?nombre=Test")
        .then()
            .statusCode(200);

        // Test VENDEDOR
        given()
            .auth().basic("vendedor", "vendedor123")
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/nombre?nombre=Test")
        .then()
            .statusCode(200);

        // Test CONSULTOR
        given()
            .auth().basic("consultor", "consultor123")
        .when()
            .get(PROVEEDORES_API_PATH + "/buscar/nombre?nombre=Test")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("19. ✅ Todos los roles pueden buscar por razón social")
    void testAllRolesCanSearchByRazonSocial() {
        // Test múltiples roles
        String[] users = {"admin", "gerente", "vendedor", "inventario", "consultor"};
        String[] passwords = {"admin123", "gerente123", "vendedor123", "inventario123", "consultor123"};

        for (int i = 0; i < users.length; i++) {
            given()
                .auth().basic(users[i], passwords[i])
            .when()
                .get(PROVEEDORES_API_PATH + "/buscar/razon-social?razonSocial=Test")
            .then()
                .statusCode(200)
                .body("codigo", equalTo("0"));
        }
    }

    // ==========================================
    // TESTS SIN AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("20. ❌ Usuario sin autenticación NO puede acceder")
    void testUnauthenticatedUserCannotAccess() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(PROVEEDORES_API_PATH)
        .then()
            .statusCode(401); // Unauthorized
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private void createTestProveedor() {
        try {
            given()
                .auth().basic("admin", "admin123")
                .contentType(ContentType.JSON)
                .body(validCreateRequest)
            .when()
                .post(PROVEEDORES_API_PATH)
            .then()
                .statusCode(200);
        } catch (Exception e) {
            // Proveedor ya existe, continuar
        }
    }
}