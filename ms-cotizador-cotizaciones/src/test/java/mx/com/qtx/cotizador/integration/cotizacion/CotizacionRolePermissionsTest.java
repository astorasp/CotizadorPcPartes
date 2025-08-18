package mx.com.qtx.cotizador.integration.cotizacion;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;

/**
 * Tests de integración para validar permisos basados en roles para cotizaciones
 * 
 * Roles del sistema:
 * - ADMIN: Acceso completo (crear, ver, buscar)
 * - GERENTE: Acceso de gestión (crear, ver, buscar)
 * - VENDEDOR: Acceso de ventas (crear, ver cotizaciones propias)
 * - INVENTARIO: Solo lectura para planificación
 * - CONSULTOR: Solo lectura para consultoría
 * 
 * Configuración:
 * - Usa TestContainers con MySQL 8.4.4
 * - Perfil 'test' con configuración específica
 * - Datos de prueba precargados via DDL/DML de /sql
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class CotizacionRolePermissionsTest extends BaseIntegrationTest {
    
    // ========================================================================
    // TESTS DE PERMISOS PARA CREAR COTIZACIONES
    // ========================================================================
    
    @Test
    @DisplayName("Permisos: ADMIN debe poder crear cotizaciones")
    void adminDebePodeCrearCotizaciones() {
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
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }
    
    @Test
    @DisplayName("Permisos: GERENTE debe poder crear cotizaciones")
    void gerenteDebePodeCrearCotizaciones() {
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
            
        given()
            .auth().basic(USER_GERENTE, PASSWORD_GERENTE)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }
    
    @Test
    @DisplayName("Permisos: VENDEDOR debe poder crear cotizaciones")
    void vendedorDebePodeCrearCotizaciones() {
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
            
        given()
            .auth().basic(USER_VENDEDOR, PASSWORD_VENDEDOR)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }
    
    @Test
    @DisplayName("Permisos: INVENTARIO NO debe poder crear cotizaciones")
    void inventarioNoDebePodeCrearCotizaciones() {
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
            
        given()
            .auth().basic(USER_INVENTARIO, PASSWORD_INVENTARIO)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(403); // Forbidden
    }
    
    @Test
    @DisplayName("Permisos: CONSULTOR NO debe poder crear cotizaciones")
    void consultorNoDebePodeCrearCotizaciones() {
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
            
        given()
            .auth().basic(USER_CONSULTOR, PASSWORD_CONSULTOR)
            .contentType(ContentType.JSON)
            .body(cotizacionRequest)
        .when()
            .post("/cotizaciones")
        .then()
            .statusCode(403); // Forbidden
    }

    // ========================================================================
    // TESTS DE PERMISOS PARA LISTAR COTIZACIONES
    // ========================================================================
    
    @Test
    @DisplayName("Permisos: Todos los roles deben poder listar cotizaciones")
    void todosLosRolesDebenPoderListarCotizaciones() {
        // ADMIN
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // GERENTE
        given()
            .auth().basic(USER_GERENTE, PASSWORD_GERENTE)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // VENDEDOR
        given()
            .auth().basic(USER_VENDEDOR, PASSWORD_VENDEDOR)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // INVENTARIO
        given()
            .auth().basic(USER_INVENTARIO, PASSWORD_INVENTARIO)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // CONSULTOR
        given()
            .auth().basic(USER_CONSULTOR, PASSWORD_CONSULTOR)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ========================================================================
    // TESTS DE PERMISOS PARA CONSULTAR COTIZACIÓN POR ID
    // ========================================================================
    
    @Test
    @DisplayName("Permisos: Todos los roles deben poder consultar cotización por ID")
    void todosLosRolesDebenPoderConsultarPorId() {
        // Primero crear una cotización como ADMIN
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
            
        // Ahora todos los roles deben poder consultarla
        // ADMIN
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // GERENTE
        given()
            .auth().basic(USER_GERENTE, PASSWORD_GERENTE)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // VENDEDOR
        given()
            .auth().basic(USER_VENDEDOR, PASSWORD_VENDEDOR)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // INVENTARIO
        given()
            .auth().basic(USER_INVENTARIO, PASSWORD_INVENTARIO)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // CONSULTOR
        given()
            .auth().basic(USER_CONSULTOR, PASSWORD_CONSULTOR)
            .contentType(ContentType.JSON)
        .when()
            .get("/cotizaciones/{id}", cotizacionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ========================================================================
    // TESTS DE PERMISOS PARA BÚSQUEDA POR FECHA
    // ========================================================================
    
    @Test
    @DisplayName("Permisos: Todos los roles deben poder buscar cotizaciones por fecha")
    void todosLosRolesDebenPoderBuscarPorFecha() {
        String fechaHoy = java.time.LocalDate.now().toString();
        
        // ADMIN
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .queryParam("fecha", fechaHoy)
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // GERENTE
        given()
            .auth().basic(USER_GERENTE, PASSWORD_GERENTE)
            .contentType(ContentType.JSON)
            .queryParam("fecha", fechaHoy)
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // VENDEDOR
        given()
            .auth().basic(USER_VENDEDOR, PASSWORD_VENDEDOR)
            .contentType(ContentType.JSON)
            .queryParam("fecha", fechaHoy)
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // INVENTARIO
        given()
            .auth().basic(USER_INVENTARIO, PASSWORD_INVENTARIO)
            .contentType(ContentType.JSON)
            .queryParam("fecha", fechaHoy)
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // CONSULTOR
        given()
            .auth().basic(USER_CONSULTOR, PASSWORD_CONSULTOR)
            .contentType(ContentType.JSON)
            .queryParam("fecha", fechaHoy)
        .when()
            .get("/cotizaciones/buscar/fecha")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }
}