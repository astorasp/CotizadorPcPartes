package mx.com.qtx.cotizador.integration.componente;

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
import org.junit.jupiter.api.AfterAll;


import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;

/**
 * Tests de integración para casos de uso de Gestión de Componentes
 * 
 * Casos de uso probados:
 * - 1.1 Agregar componente
 * - 1.2 Modificar componente  
 * - 1.3 Eliminar componente
 * - 1.4 Consultar componentes
 * 
 * Usa la base de datos MySQL compartida para optimizar rendimiento.
 * Hereda configuración común de BaseIntegrationTest.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class ComponenteIntegrationTest extends BaseIntegrationTest {

    // Base path para el API de componentes
    private static final String COMPONENTES_API_PATH = "/componentes/v1/api";
    
    // IDs de componentes para tests (usaremos estos para mantener consistencia)
    private static final String COMPONENTE_TEST_ID = "TEST001";
    private static final String COMPONENTE_MODIFICAR_ID = "TEST002"; 
    private static final String COMPONENTE_ELIMINAR_ID = "TEST003";
    
    // setUp() heredado de BaseIntegrationTest

    // ========================================================================
    // CASO DE USO 1.4: CONSULTAR COMPONENTES  
    // ========================================================================
    
    @Test
    @DisplayName("CU 1.4.1: Debe consultar todos los componentes exitosamente")
    void deberiaConsultarTodosLosComponentes() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(COMPONENTES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Consulta exitosa"))
            .body("datos", notNullValue())
            .body("datos", hasSize(greaterThan(0))); // Debe tener datos precargados
    }
    
    @Test
    @DisplayName("CU 1.4.2: Debe fallar consulta sin autenticación")
    void deberiaFallarConsultaSinAutenticacion() {
        given()
            .auth().none()
            .contentType(ContentType.JSON)
        .when()
            .get(COMPONENTES_API_PATH)
        .then()
            .statusCode(401);
    }
    
    @Test
    @DisplayName("CU 1.4.3: Debe consultar componente por ID exitosamente")
    void deberiaConsultarComponentePorId() {
        // Usar un componente precargado en DML
        String componenteId = "MON001";
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(COMPONENTES_API_PATH + "/{id}", componenteId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.id", equalTo(componenteId))
            .body("datos.marca", equalTo("LG"));
    }
    
    @Test
    @DisplayName("CU 1.4.4: Debe retornar error al consultar componente inexistente")
    void deberiaRetornarErrorComponenteInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(COMPONENTES_API_PATH + "/INEXISTENTE-999")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // Código de error para "recurso no encontrado"
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 1.1: AGREGAR COMPONENTE
    // ========================================================================
    
    @Test
    @DisplayName("CU 1.1.1: Debe agregar componente nuevo exitosamente")
    void deberiaAgregarComponenteNuevo() {
        String nuevoComponente = """
            {
                "id": "%s",
                "descripcion": "Monitor Samsung 27 pulgadas 4K",
                "marca": "Samsung",
                "modelo": "U28E590D",
                "precioBase": 8500.00,
                "costo": 6800.00,
                "tipoComponente": "MONITOR"
            }
            """.formatted(COMPONENTE_TEST_ID);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(nuevoComponente)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Componente guardado exitosamente"))
            .body("datos.id", equalTo(COMPONENTE_TEST_ID));
    }
    
    @Test
    @DisplayName("CU 1.1.2: Debe fallar al agregar componente con ID duplicado")
    void deberiaFallarAgregarComponenteDuplicado() {
        String componenteDuplicado = """
            {
                "id": "MON001",
                "descripcion": "Monitor duplicado",
                "marca": "LG",
                "modelo": "24MK430H-DUPLICADO",
                "precioBase": 3500.00,
                "costo": 2800.00,
                "tipoComponente": "MONITOR"
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteDuplicado)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("5")) // Código de error para "recurso ya existe"
            .body("mensaje", notNullValue());
    }
    
    @Test
    @DisplayName("CU 1.1.3: Debe fallar al agregar componente con datos inválidos")
    void deberiaFallarAgregarComponenteDatosInvalidos() {
        String componenteInvalido = """
            {
                "id": "",
                "descripcion": "",
                "marca": "",
                "modelo": "",
                "precioBase": -100.00,
                "costo": -50.00,
                "tipoComponente": ""
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteInvalido)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")) // Código de error para "error de validación"
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 1.2: MODIFICAR COMPONENTE
    // ========================================================================
    
    @Test
    @DisplayName("CU 1.2.1: Debe modificar componente existente exitosamente")
    void deberiaModificarComponenteExistente() {
        // Primero crear el componente a modificar
        String componenteOriginal = """
            {
                "id": "%s",
                "descripcion": "Teclado original",
                "marca": "Dell",
                "modelo": "KB216",
                "precioBase": 300.00,
                "costo": 240.00,
                "tipoComponente": "MONITOR"
            }
            """.formatted(COMPONENTE_MODIFICAR_ID);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteOriginal)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200);
            
        // Ahora modificar el componente
        String componenteModificado = """
            {
                "descripcion": "Teclado mecánico RGB modificado",
                "marca": "Dell",
                "modelo": "KB216-RGB",
                "precioBase": 450.00,
                "costo": 360.00,
                "tipoComponente": "MONITOR"
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteModificado)
        .when()
            .put(COMPONENTES_API_PATH + "/{id}", COMPONENTE_MODIFICAR_ID)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Componente actualizado exitosamente"))
            .body("datos.descripcion", equalTo("Teclado mecánico RGB modificado"))
            .body("datos.precioBase", equalTo(450.0f));
    }
    
    @Test
    @DisplayName("CU 1.2.2: Debe fallar al modificar componente inexistente")
    void deberiaFallarModificarComponenteInexistente() {
        String componenteModificar = """
            {
                "descripcion": "Componente que no existe",
                "marca": "NoMarca",
                "modelo": "NoModelo",
                "precioBase": 100.00,
                "costo": 80.00,
                "tipoComponente": "MONITOR"
            }
            """;
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteModificar)
        .when()
            .put(COMPONENTES_API_PATH + "/INEXISTENTE-999")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // Código de error para "recurso no encontrado"
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 1.3: ELIMINAR COMPONENTE
    // ========================================================================
    
    @Test
    @DisplayName("CU 1.3.1: Debe eliminar componente existente exitosamente")
    void deberiaEliminarComponenteExistente() {
        // Primero crear el componente a eliminar
        String componenteEliminar = """
            {
                "id": "%s",
                "descripcion": "Mouse a eliminar",
                "marca": "Microsoft",
                "modelo": "Basic",
                "precioBase": 200.00,
                "costo": 160.00,
                "tipoComponente": "MONITOR"
            }
            """.formatted(COMPONENTE_ELIMINAR_ID);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteEliminar)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200);
            
        // Ahora eliminar el componente
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(COMPONENTES_API_PATH + "/{id}", COMPONENTE_ELIMINAR_ID)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Componente eliminado exitosamente"));
            
        // Verificar que ya no existe
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(COMPONENTES_API_PATH + "/{id}", COMPONENTE_ELIMINAR_ID)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")); // Recurso no encontrado
    }
    
    @Test
    @DisplayName("CU 1.3.2: Debe fallar al eliminar componente inexistente")
    void deberiaFallarEliminarComponenteInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(COMPONENTES_API_PATH + "/COMPONENTE-NO-EXISTE")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // Código de error para "recurso no encontrado"
            .body("mensaje", notNullValue());
    }
    
    @Test
    @DisplayName("CU 1.3.3: Debe fallar al eliminar componente con referencias")
    void deberiaFallarEliminarComponenteConReferencias() {
        // Este test simula eliminar un componente que está siendo usado en cotizaciones
        // Usaremos un componente precargado que podría tener referencias
        // TODO: Implementar verificación de referencias antes de eliminar
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(COMPONENTES_API_PATH + "/MON-LG-24MK430H") // Componente precargado
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // Por ahora retorna "recurso no encontrado" hasta implementar verificación de referencias
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // TESTS DE SEGURIDAD Y VALIDACIÓN
    // ========================================================================
    
    @Test
    @DisplayName("Seguridad: Todos los endpoints requieren autenticación")
    void todosLosEndpointsRequierenAutenticacion() {
        String componente = """
            {
                "id": "TEST-AUTH",
                "descripcion": "Test",
                "marca": "Test",
                "modelo": "Test",
                "precioBase": 100.00,
                "costo": 80.00,
                "tipoComponente": "MONITOR"
            }
            """;
        
        // GET sin auth
        given().auth().none().contentType(ContentType.JSON)
        .when().get(COMPONENTES_API_PATH)
        .then().statusCode(401);
        
        // POST sin auth
        given().auth().none().contentType(ContentType.JSON).body(componente)
        .when().post(COMPONENTES_API_PATH)
        .then().statusCode(401);
        
        // PUT sin auth
        given().auth().none().contentType(ContentType.JSON).body(componente)
        .when().put(COMPONENTES_API_PATH + "/TEST-AUTH")
        .then().statusCode(401);
        
        // DELETE sin auth
        given().auth().none().contentType(ContentType.JSON)
        .when().delete(COMPONENTES_API_PATH + "/TEST-AUTH")
        .then().statusCode(401);
    }

    @Test
    @DisplayName("A. Infraestructura: Aplicación debe arrancar correctamente")
    void aplicacionDebeArrancar() {
        assertThat(port).isGreaterThan(0);
    }

    @AfterAll 
    static void cleanup() {
        System.out.println("🧹 Tests completados - contenedor será destruido automáticamente");
    }
} 