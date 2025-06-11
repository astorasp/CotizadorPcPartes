package mx.com.qtx.cotizador.integration.promocion;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionUpdateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.DetallePromocionRequest;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests de integración para endpoints RESTful de gestión de promociones
 * 
 * Implementa casos de uso:
 * - 6.1: Agregar promoción
 * - 6.2: Modificar promoción 
 * - 6.3: Consultar promociones (por ID y listar todas)
 * - 6.4: Eliminar promoción
 * 
 * Usa TestContainers con MySQL y RestAssured para tests end-to-end
 * Datos de prueba cargados desde archivos SQL en test/resources
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Integration Tests - Gestión de Promociones")
class PromocionIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.4")
            .withDatabaseName("cotizador_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("sql/ddl.sql");

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.data-locations", () -> "classpath:sql/dml.sql");
    }

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.authentication = RestAssured.basic("test", "test123");
        RestAssured.basePath = "/cotizador/v1/api";
    }

    // ==================== CASO DE USO 6.1: AGREGAR PROMOCIÓN ====================

    @Test
    @DisplayName("6.1 - Debería crear promoción básica exitosamente")
    void deberiaCrearPromocionBasicaExitosamente() {
        
        // Arrange - Promoción simple sin descuento (promoción base válida)
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Descuento Base Test");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Test Básica");
        request.setDescripcion("Promoción de prueba para testing automatizado");
        request.setVigenciaDesde(LocalDate.of(2025, 6, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 6, 30));
        request.setDetalles(List.of(detalleBase));
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/promociones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", containsString("creada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.nombre", equalTo("Promoción Test Básica"))
            .body("datos.descripcion", equalTo("Promoción de prueba para testing automatizado"))
            .body("datos.vigenciaDesde", equalTo("2025-06-01"))
            .body("datos.vigenciaHasta", equalTo("2025-06-30"))
            .body("datos.detalles", hasSize(1))
            .body("datos.detalles[0].nombre", equalTo("Descuento Base Test"))
            .body("datos.detalles[0].esBase", equalTo(true));
    }

    @Test
    @DisplayName("6.1 - Debería fallar con nombre duplicado")
    void deberiaFallarConNombreDuplicado() {
        
        // Arrange - Usar nombre de promoción existente del DML
        DetallePromocionRequest detalle = new DetallePromocionRequest();
        detalle.setNombre("Detalle Regular");
        detalle.setEsBase(true);
        detalle.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Regular"); // Nombre que ya existe en DML
        request.setDescripcion("Promoción duplicada");
        request.setVigenciaDesde(LocalDate.of(2025, 7, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 7, 31));
        request.setDetalles(List.of(detalle));
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/promociones")
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")))
            .body("mensaje", containsString("Ya existe una promoción"))
            .body("datos", nullValue());
    }

    @Test
    @DisplayName("6.1 - Debería fallar con datos inválidos")
    void deberiaFallarConDatosInvalidos() {
        
        // Arrange - Request con datos faltantes
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre(""); // Nombre vacío (inválido)
        request.setDescripcion("Descripción válida");
        request.setVigenciaDesde(LocalDate.of(2025, 8, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 7, 31)); // Fecha fin antes que inicio (inválido)
        request.setDetalles(List.of()); // Sin detalles (inválido)
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/promociones")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")) // Error de validación
            .body("datos", notNullValue()); // Detalles de validación
    }

    // ==================== CASO DE USO 6.2: MODIFICAR PROMOCIÓN ====================

    @Test
    @DisplayName("6.2 - Debería actualizar promoción existente exitosamente")
    void deberiaActualizarPromocionExitosamente() {
        
        // Arrange - Actualizar promoción existente del DML (ID 2: Monitores por Volumen)
        DetallePromocionRequest detalleActualizado = new DetallePromocionRequest();
        detalleActualizado.setNombre("Descuento Monitores Actualizado");
        detalleActualizado.setEsBase(true);
        detalleActualizado.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionUpdateRequest request = new PromocionUpdateRequest();
        request.setNombre("Monitores por Volumen Actualizada");
        request.setDescripcion("Promoción actualizada para monitores con mejores descuentos");
        request.setVigenciaDesde(LocalDate.of(2025, 3, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 6, 30)); // Extender vigencia
        request.setDetalles(List.of(detalleActualizado));
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/promociones/2")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", containsString("actualizada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.idPromocion", equalTo(2))
            .body("datos.nombre", equalTo("Monitores por Volumen Actualizada"))
            .body("datos.descripcion", equalTo("Promoción actualizada para monitores con mejores descuentos"))
            .body("datos.vigenciaHasta", equalTo("2025-06-30"));
    }

    @Test
    @DisplayName("6.2 - Debería fallar con ID inexistente")
    void deberiaFallarConIdInexistente() {
        
        // Arrange
        DetallePromocionRequest detalle = new DetallePromocionRequest();
        detalle.setNombre("Detalle cualquiera");
        detalle.setEsBase(true);
        detalle.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionUpdateRequest request = new PromocionUpdateRequest();
        request.setNombre("Promoción No Existe");
        request.setDescripcion("Esta promoción no debería actualizarse");
        request.setVigenciaDesde(LocalDate.of(2025, 9, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 9, 30));
        request.setDetalles(List.of(detalle));
        
        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/promociones/99999")
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")))
            .body("mensaje", containsString("no encontrada"))
            .body("datos", nullValue());
    }

    // ==================== CASO DE USO 6.3: CONSULTAR PROMOCIONES ====================

    @Test
    @DisplayName("6.3 - Debería obtener promoción por ID exitosamente")
    void deberiaObtenerPromocionPorIdExitosamente() {
        
        // Act & Assert - Consultar promoción existente del DML (ID 1: Regular)
        given()
        .when()
            .get("/promociones/1")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Promoción encontrada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.idPromocion", equalTo(1))
            .body("datos.nombre", equalTo("Regular"))
            .body("datos.descripcion", equalTo("Sin promoción"))
            .body("datos.vigenciaDesde", equalTo("2025-01-01"))
            .body("datos.vigenciaHasta", equalTo("2030-12-31"))
            .body("datos.detalles", notNullValue());
    }

    @Test
    @DisplayName("6.3 - Debería fallar con ID de promoción inexistente")
    void deberiaFallarConIdPromocionInexistente() {
        
        // Act & Assert
        given()
        .when()
            .get("/promociones/99999")
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")))
            .body("mensaje", containsString("no encontrada"))
            .body("datos", nullValue());
    }

    @Test
    @DisplayName("6.3 - Debería obtener todas las promociones exitosamente")
    void deberiaObtenerTodasLasPromocionesExitosamente() {
        
        // Act & Assert
        given()
        .when()
            .get("/promociones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("Promociones obtenidas exitosamente"))
            .body("datos", notNullValue())
            .body("datos", hasSize(greaterThan(0))) // Debería haber promociones del DML
            .body("datos[0].idPromocion", notNullValue())
            .body("datos[0].nombre", notNullValue())
            .body("datos[0].vigenciaDesde", notNullValue())
            .body("datos[0].vigenciaHasta", notNullValue());
    }

    // ==================== CASO DE USO 6.4: ELIMINAR PROMOCIÓN ====================

    @Test
    @DisplayName("6.4 - Debería eliminar promoción sin componentes asociados")
    void deberiaEliminarPromocionSinComponentes() {
        
        // Arrange - Crear promoción temporal para eliminar
        DetallePromocionRequest detalle = new DetallePromocionRequest();
        detalle.setNombre("Detalle Temporal");
        detalle.setEsBase(true);
        detalle.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionCreateRequest createRequest = new PromocionCreateRequest();
        createRequest.setNombre("Promoción Temporal Para Eliminar");
        createRequest.setDescripcion("Esta promoción será eliminada en el test");
        createRequest.setVigenciaDesde(LocalDate.of(2025, 10, 1));
        createRequest.setVigenciaHasta(LocalDate.of(2025, 10, 31));
        createRequest.setDetalles(List.of(detalle));
        
        // Crear promoción primero
        Integer promocionId = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/promociones")
        .then()
            .statusCode(200)
            .extract()
            .path("datos.idPromocion");
        
        // Act & Assert - Eliminar la promoción creada
        given()
        .when()
            .delete("/promociones/" + promocionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", containsString("eliminada exitosamente"))
            .body("datos", nullValue());
        
        // Verificar que realmente se eliminó
        given()
        .when()
            .get("/promociones/" + promocionId)
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")));
    }

    @Test
    @DisplayName("6.4 - Debería fallar al eliminar promoción con componentes asociados")
    void deberiaFallarEliminarPromocionConComponentes() {
        
        // Act & Assert - Intentar eliminar promoción con componentes asociados (ID 2: Monitores)
        // El sistema devuelve HTTP 500 por foreign key constraint (comportamiento actual)
        given()
        .when()
            .delete("/promociones/2")
        .then()
            .statusCode(500)
            .body("codigo", equalTo("3")) // Error interno del servidor
            .body("mensaje", equalTo("Servicio no disponible"))
            .body("datos", nullValue());
    }

    @Test
    @DisplayName("6.4 - Debería fallar al eliminar promoción inexistente")
    void deberiaFallarEliminarPromocionInexistente() {
        
        // Act & Assert
        given()
        .when()
            .delete("/promociones/99999")
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")))
            .body("mensaje", containsString("no encontrada"))
            .body("datos", nullValue());
    }

    // ==================== CASOS DE USO DE SEGURIDAD ====================

    @Test
    @DisplayName("Seguridad - Debería requerir autenticación para crear promoción")
    void deberiaRequerirAutenticacionParaCrearPromocion() {
        
        // Arrange
        DetallePromocionRequest detalle = new DetallePromocionRequest();
        detalle.setNombre("Detalle Test");
        detalle.setEsBase(true);
        detalle.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Sin Auth");
        request.setDescripcion("Esta promoción no debería crearse");
        request.setVigenciaDesde(LocalDate.of(2025, 11, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 11, 30));
        request.setDetalles(List.of(detalle));
        
        // Act & Assert - Sin autenticación
        given()
            .auth().none()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/promociones")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Seguridad - Debería requerir autenticación para consultar promoción")
    void deberiaRequerirAutenticacionParaConsultarPromocion() {
        
        // Act & Assert - Sin autenticación
        given()
            .auth().none()
        .when()
            .get("/promociones/1")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Seguridad - Debería requerir autenticación para actualizar promoción")
    void deberiaRequerirAutenticacionParaActualizarPromocion() {
        
        // Arrange
        DetallePromocionRequest detalle = new DetallePromocionRequest();
        detalle.setNombre("Detalle Sin Auth");
        detalle.setEsBase(true);
        detalle.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionUpdateRequest request = new PromocionUpdateRequest();
        request.setNombre("Actualización Sin Auth");
        request.setDescripcion("Esta actualización no debería funcionar");
        request.setVigenciaDesde(LocalDate.of(2025, 12, 1));
        request.setVigenciaHasta(LocalDate.of(2025, 12, 31));
        request.setDetalles(List.of(detalle));
        
        // Act & Assert - Sin autenticación
        given()
            .auth().none()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/promociones/1")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Seguridad - Debería requerir autenticación para eliminar promoción")
    void deberiaRequerirAutenticacionParaEliminarPromocion() {
        
        // Act & Assert - Sin autenticación
        given()
            .auth().none()
        .when()
            .delete("/promociones/1")
        .then()
            .statusCode(401);
    }

    // ==================== CASOS DE USO ADICIONALES ====================

    @Test
    @DisplayName("Flujo completo - Crear, consultar, actualizar y eliminar promoción")
    void flujoCompleto_CrearConsultarActualizarEliminarPromocion() {
        
        // Paso 1: Crear promoción
        DetallePromocionRequest detalleInicial = new DetallePromocionRequest();
        detalleInicial.setNombre("Detalle Inicial");
        detalleInicial.setEsBase(true);
        detalleInicial.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionCreateRequest createRequest = new PromocionCreateRequest();
        createRequest.setNombre("Promoción Flujo Completo");
        createRequest.setDescripcion("Promoción para test de flujo completo");
        createRequest.setVigenciaDesde(LocalDate.of(2025, 12, 1));
        createRequest.setVigenciaHasta(LocalDate.of(2025, 12, 31));
        createRequest.setDetalles(List.of(detalleInicial));
        
        Integer promocionId = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/promociones")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .extract()
            .path("datos.idPromocion");
        
        // Paso 2: Consultar la promoción creada
        given()
        .when()
            .get("/promociones/" + promocionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.idPromocion", equalTo(promocionId))
            .body("datos.nombre", equalTo("Promoción Flujo Completo"));
        
        // Paso 3: Actualizar la promoción
        DetallePromocionRequest detalleActualizado = new DetallePromocionRequest();
        detalleActualizado.setNombre("Detalle Actualizado");
        detalleActualizado.setEsBase(true);
        detalleActualizado.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        PromocionUpdateRequest updateRequest = new PromocionUpdateRequest();
        updateRequest.setNombre("Promoción Flujo Completo Actualizada");
        updateRequest.setDescripcion("Promoción actualizada en flujo completo");
        updateRequest.setVigenciaDesde(LocalDate.of(2025, 12, 1));
        updateRequest.setVigenciaHasta(LocalDate.of(2026, 1, 31));
        updateRequest.setDetalles(List.of(detalleActualizado));
        
        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/promociones/" + promocionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.nombre", equalTo("Promoción Flujo Completo Actualizada"));
        
        // Paso 4: Eliminar la promoción
        given()
        .when()
            .delete("/promociones/" + promocionId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }
} 