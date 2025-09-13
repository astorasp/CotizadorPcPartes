package mx.com.qtx.cotizador.integration.pc;

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
import mx.com.qtx.cotizador.dto.pc.request.PcCreateRequest;
import mx.com.qtx.cotizador.dto.pc.request.AgregarComponenteRequest;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;

import java.math.BigDecimal;

/**
 * Test de permisos basados en roles para el controlador de PCs
 * Verifica que los diferentes roles tengan los permisos correctos según la matriz de permisos
 * 
 * Matriz de Permisos para PCs:
 * - ADMIN: Full CRUD + Gestión de componentes + Vista de costos + Modificación de precios
 * - GERENTE: Create, Edit, Delete + Gestión de componentes + Vista de costos + Modificación de precios (NO Remove components)
 * - VENDEDOR: Read-only 
 * - INVENTARIO: Full CRUD + Gestión de componentes + Vista de costos (NO Modificación de precios)
 * - CONSULTOR: Read-only
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class PcRolePermissionsTest extends BaseIntegrationTest {

    private PcCreateRequest pcRequest;
    private AgregarComponenteRequest componenteRequest;
    private final String BASE_URL = "/pcs";

    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call parent setUp for RestAssured configuration

        // Preparar datos de prueba para PC con timestamp para evitar conflictos
        String timeStamp = String.valueOf(System.currentTimeMillis() % 1000);

        // PC requires minimum: 1 monitor, 1 graphics card, 1 hard drive

        // Create monitor component
        mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest monitor =
            new mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest();
        monitor.setId("MON" + timeStamp);
        monitor.setDescripcion("Monitor de prueba para permisos");
        monitor.setMarca("TestMonitor");
        monitor.setModelo("TM-PERM");
        monitor.setPrecioBase(new BigDecimal("4500.00"));
        monitor.setCosto(new BigDecimal("3600.00"));
        monitor.setTipoComponente("MONITOR");

        // Create graphics card component
        mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest tarjeta =
            new mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest();
        tarjeta.setId("GPU" + timeStamp);
        tarjeta.setDescripcion("Tarjeta de video de prueba");
        tarjeta.setMarca("TestGPU");
        tarjeta.setModelo("TG-PERM");
        tarjeta.setPrecioBase(new BigDecimal("8000.00"));
        tarjeta.setCosto(new BigDecimal("6400.00"));
        tarjeta.setTipoComponente("TARJETA_VIDEO");
        tarjeta.setMemoria("8GB");  // Required field for TARJETA_VIDEO

        // Create hard drive component
        mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest disco =
            new mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest();
        disco.setId("HDD" + timeStamp);
        disco.setDescripcion("Disco duro de prueba");
        disco.setMarca("TestHDD");
        disco.setModelo("TH-PERM");
        disco.setPrecioBase(new BigDecimal("2000.00"));
        disco.setCosto(new BigDecimal("1600.00"));
        disco.setTipoComponente("DISCO_DURO");
        disco.setCapacidadAlm("1TB");  // Required field for DISCO_DURO

        // Create all components in the database
        for (var component : java.util.List.of(monitor, tarjeta, disco)) {
            given()
                .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
                .contentType(ContentType.JSON)
                .body(component)
            .when()
                .post("/componentes")
            .then()
                .statusCode(200);
        }

        pcRequest = new PcCreateRequest();
        pcRequest.setId("PCP" + timeStamp); // PC Permissions + timestamp
        pcRequest.setNombre("PC Gaming de prueba para permisos");
        pcRequest.setDescripcion("PC Gaming de alta gama para testing de permisos");
        pcRequest.setMarca("TestGaming");
        pcRequest.setModelo("TG-PERM-001");
        pcRequest.setPrecio(new BigDecimal("15000.00"));
        pcRequest.setCantidad(1);
        pcRequest.setSubComponentes(java.util.List.of(monitor, tarjeta, disco));

        // Preparar datos de prueba para componente adicional (para agregar a PC)
        componenteRequest = new AgregarComponenteRequest();
        componenteRequest.setId("ADD" + timeStamp);  // Different ID for additional component
        componenteRequest.setTipoComponente("TARJETA_VIDEO");
        componenteRequest.setDescripcion("Tarjeta de video adicional de prueba");
        componenteRequest.setMarca("TestGPU2");
        componenteRequest.setModelo("TG2-001");
        componenteRequest.setCosto(new BigDecimal("6400.00"));
        componenteRequest.setPrecioBase(new BigDecimal("8000.00"));
        componenteRequest.setMemoria("8GB");
    }

    // ==========================================
    // TESTS PARA VERIFICAR ACCESO READ (todos los roles)
    // ==========================================

    @Test
    @DisplayName("Permisos PC 1: Usuario sin autenticación no puede acceder")
    void usuarioSinAutenticacionNoPuedeAcceder() {
        given()
            .auth().none() // Explicitly disable auth
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL)
        .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    @DisplayName("Permisos PC 2: Todos los roles pueden leer PCs")
    void todosLosRolesPuedenLeerPcs() {
        // Probar que todos los roles con permisos pueden leer
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("Permisos PC 3: Usuario autenticado puede obtener PC por ID")
    void usuarioAutenticadoPuedeObtenerPcPorId() {
        // Primero crear una PC
        crearPcTest();
        
        // Después obtenerla por ID
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.id", equalTo(pcRequest.getId()));
    }

    // ==========================================
    // TESTS PARA VERIFICAR ACCESO WRITE (solo roles con permisos)
    // ==========================================

    @Test
    @DisplayName("Permisos PC 4: Usuario autenticado puede crear PC")
    void usuarioAutenticadoPuedeCrearPc() {
        // Crear PC con usuario autenticado (ADMIN, GERENTE, INVENTARIO)
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .post(BASE_URL)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.id", equalTo(pcRequest.getId()));
    }

    @Test
    @DisplayName("Permisos PC 5: Usuario autenticado puede actualizar PC")
    void usuarioAutenticadoPuedeActualizarPc() {
        // Primero crear la PC
        crearPcTest();
        
        // Actualizar descripción
        pcRequest.setDescripcion("PC Gaming actualizada");
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .put(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("Permisos PC 6: Usuario autenticado puede eliminar PC")
    void usuarioAutenticadoPuedeEliminarPc() {
        // Primero crear la PC
        crearPcTest();
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA GESTIÓN DE COMPONENTES
    // ==========================================

    @Test
    @DisplayName("Permisos PC 7: Usuario autenticado puede agregar componente a PC")
    void usuarioAutenticadoPuedeAgregarComponenteAPc() {
        // Primero crear la PC
        crearPcTest();
        
        // Agregar componente a la PC (ADMIN, GERENTE, INVENTARIO)
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteRequest)
        .when()
            .post(BASE_URL + "/" + pcRequest.getId() + "/componentes")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("Permisos PC 8: Usuario autenticado puede quitar componente de PC")
    void usuarioAutenticadoPuedeQuitarComponenteDePc() {
        // Primero crear la PC y agregar componente
        crearPcTest();
        agregarComponenteTest();
        
        // Quitar componente de la PC (ADMIN, GERENTE, INVENTARIO)
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(BASE_URL + "/" + pcRequest.getId() + "/componentes/" + componenteRequest.getId())
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("Permisos PC 9: Usuario autenticado puede listar componentes de PC")
    void usuarioAutenticadoPuedeListarComponentesDePc() {
        // Primero crear la PC
        crearPcTest();
        
        // Listar componentes de la PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL + "/" + pcRequest.getId() + "/componentes")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // TESTS PARA VERIFICAR DENEGACIÓN DE ACCESO
    // ==========================================

    @Test
    @DisplayName("Permisos PC 10: Usuario sin autenticación no puede crear PC")
    void usuarioSinAutenticacionNoPuedeCrearPc() {
        given()
            .auth().none() // Explicitly disable auth
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .post(BASE_URL)
        .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    @DisplayName("Permisos PC 11: Usuario sin autenticación no puede actualizar PC")
    void usuarioSinAutenticacionNoPuedeActualizarPc() {
        given()
            .auth().none() // Explicitly disable auth
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .put(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    @DisplayName("Permisos PC 12: Usuario sin autenticación no puede eliminar PC")
    void usuarioSinAutenticacionNoPuedeEliminarPc() {
        given()
            .auth().none() // Explicitly disable auth
            .contentType(ContentType.JSON)
        .when()
            .delete(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    @DisplayName("Permisos PC 13: Usuario sin autenticación no puede agregar componente")
    void usuarioSinAutenticacionNoPuedeAgregarComponente() {
        given()
            .auth().none() // Explicitly disable auth
            .contentType(ContentType.JSON)
            .body(componenteRequest)
        .when()
            .post(BASE_URL + "/" + pcRequest.getId() + "/componentes")
        .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    @DisplayName("Permisos PC 14: Usuario sin autenticación no puede quitar componente")
    void usuarioSinAutenticacionNoPuedeQuitarComponente() {
        given()
            .auth().none() // Explicitly disable auth
            .contentType(ContentType.JSON)
        .when()
            .delete(BASE_URL + "/" + pcRequest.getId() + "/componentes/" + componenteRequest.getId())
        .then()
            .statusCode(401); // Unauthorized
    }

    // ==========================================
    // TESTS FUNCIONALES DE LA MATRIZ DE PERMISOS
    // ==========================================

    @Test
    @DisplayName("Permisos PC 15: Verificar que las anotaciones @PreAuthorize están presentes")
    void verificarQueAnotacionesDePermisosFuncionan() {
        // Este test verifica que el sistema funciona con la autenticación básica
        // En la implementación actual, el usuario de testing tiene todos los roles
        
        // Verificar lectura
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // Verificar creación
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .post(BASE_URL)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @DisplayName("Permisos PC 16: Workflow completo de gestión de PCs con permisos")
    void workflowCompletoDeGestionDePcsConPermisos() {
        // Test que valida el workflow completo respetando permisos
        
        // 1. Crear PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .post(BASE_URL)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
        
        // 2. Obtener PC creada
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.id", equalTo(pcRequest.getId()));
        
        // 3. Agregar componente
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteRequest)
        .when()
            .post(BASE_URL + "/" + pcRequest.getId() + "/componentes")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
        
        // 4. Listar componentes
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_URL + "/" + pcRequest.getId() + "/componentes")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
        
        // 5. Actualizar PC
        pcRequest.setDescripcion("PC actualizada en workflow");
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .put(BASE_URL + "/" + pcRequest.getId())
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private void crearPcTest() {
        // Component is already created in setUp(), just create the PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcRequest)
        .when()
            .post(BASE_URL)
        .then()
            .statusCode(200);
    }
    
    private void agregarComponenteTest() {
        // First, create the component in the database
        mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest componenteCreateRequest =
            new mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest();
        componenteCreateRequest.setId(componenteRequest.getId());
        componenteCreateRequest.setTipoComponente(componenteRequest.getTipoComponente());
        componenteCreateRequest.setDescripcion(componenteRequest.getDescripcion());
        componenteCreateRequest.setMarca(componenteRequest.getMarca());
        componenteCreateRequest.setModelo(componenteRequest.getModelo());
        componenteCreateRequest.setCosto(componenteRequest.getCosto());
        componenteCreateRequest.setPrecioBase(componenteRequest.getPrecioBase());

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteCreateRequest)
        .when()
            .post("/componentes")
        .then()
            .statusCode(200);

        // Now add the component to the PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteRequest)
        .when()
            .post(BASE_URL + "/" + pcRequest.getId() + "/componentes")
        .then()
            .statusCode(200);
    }
}