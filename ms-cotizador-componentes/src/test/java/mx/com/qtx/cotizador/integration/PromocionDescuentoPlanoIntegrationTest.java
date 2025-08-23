package mx.com.qtx.cotizador.integration;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.DetallePromocionRequest;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionAcumulable;

/**
 * Tests de integración para API de promociones tipo Descuento Plano
 * 
 * Valida los endpoints REST que gestionan promociones con descuentos 
 * porcentuales fijos aplicados al total de la compra.
 * 
 * @author Claude Code
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PromocionDescuentoPlanoIntegrationTest extends BaseIntegrationTest {

    private static final String PROMOCIONES_ENDPOINT = "/promociones";
    private static Long promocionDescuentoPlanoId;
    
    @BeforeAll
    static void setUpDescuentoPlanoTests() {
        System.out.println("🧪 Iniciando tests de integración para promociones de Descuento Plano");
    }

    @Test
    @Order(1)
    @DisplayName("Crear promoción Descuento Plano 15%")
    void testCrearPromocionDescuentoPlano15() {
        // Arrange - Base detail (required) + Acumulable detail with descuento plano
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base Sin Descuento");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleDescuento = new DetallePromocionRequest();
        detalleDescuento.setNombre("Detalle Descuento Plano 15%");
        detalleDescuento.setEsBase(false);
        detalleDescuento.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
        detalleDescuento.setPorcentajeDescuentoPlano(15.0);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Descuento Plano 15%");
        request.setDescripcion("Descuento del 15% en toda la compra - Test automatizado");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(3));
        request.setDetalles(new java.util.ArrayList<>(List.of(detalleBase, detalleDescuento)));

        // Act & Assert - Capture response to debug 500 error
        var response = given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
            .log().body()
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .log().all()
            .extract();

        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            Integer id = response.path("datos.idPromocion");
            promocionDescuentoPlanoId = id.longValue();
            assertNotNull(promocionDescuentoPlanoId, "ID de promoción debe ser generado");
            
            // Validate response structure
            assertEquals("Promoción Descuento Plano 15%", response.path("datos.nombre"));
            assertEquals(2, ((List<?>) response.path("datos.detalles")).size());
            assertEquals(true, response.path("datos.detalles[0].esBase"));
            assertEquals(false, response.path("datos.detalles[1].esBase"));
            assertEquals("DESCUENTO_PLANO", response.path("datos.detalles[1].tipoAcumulable"));
            
            System.out.println("✅ Promoción Descuento Plano 15% creada con ID: " + promocionDescuentoPlanoId);
        } else {
            throw new AssertionError("Expected 200 but got " + response.statusCode() + ". Response: " + response.asString());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Crear promoción Descuento Plano 25%")
    void testCrearPromocionDescuentoPlano25() {
        // Arrange - Base detail (required) + Acumulable detail
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base Sin Descuento");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleDescuento = new DetallePromocionRequest();
        detalleDescuento.setNombre("Detalle Descuento 25%");
        detalleDescuento.setEsBase(false);
        detalleDescuento.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
        detalleDescuento.setPorcentajeDescuentoPlano(25.0);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Descuento Plano 25%");
        request.setDescripcion("Descuento del 25% en toda la compra");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(2));
        request.setDetalles(new java.util.ArrayList<>(List.of(detalleBase, detalleDescuento)));

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.detalles[1].tipoAcumulable", equalTo("DESCUENTO_PLANO"));
    }

    @Test
    @Order(3)
    @DisplayName("Crear promoción Descuento Plano 5% (mínimo)")
    void testCrearPromocionDescuentoPlanoMinimo() {
        // Arrange - Base detail (required) + Acumulable detail
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base Sin Descuento");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleDescuento = new DetallePromocionRequest();
        detalleDescuento.setNombre("Detalle Descuento Mínimo 5%");
        detalleDescuento.setEsBase(false);
        detalleDescuento.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
        detalleDescuento.setPorcentajeDescuentoPlano(5.0);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Descuento Plano 5%");
        request.setDescripcion("Descuento mínimo del 5%");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(new java.util.ArrayList<>(List.of(detalleBase, detalleDescuento)));

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
    }

    @Test
    @Order(4)
    @DisplayName("Error: Descuento plano mayor a 100%")
    void testErrorDescuentoMayorA100() {
        // Arrange - descuento inválido > 100%
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleInvalido = new DetallePromocionRequest();
        detalleInvalido.setNombre("Detalle Descuento Inválido");
        detalleInvalido.setEsBase(false);
        detalleInvalido.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
        detalleInvalido.setPorcentajeDescuentoPlano(120.0); // Inválido
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Descuento Inválido 120%");
        request.setDescripcion("Promoción con descuento inválido");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(new java.util.ArrayList<>(List.of(detalleInvalido)));

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")));
    }

    @Test
    @Order(5)
    @DisplayName("Error: Descuento plano negativo")
    void testErrorDescuentoNegativo() {
        // Arrange - descuento negativo
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleInvalido = new DetallePromocionRequest();
        detalleInvalido.setNombre("Detalle Descuento Negativo");
        detalleInvalido.setEsBase(false);
        detalleInvalido.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
        detalleInvalido.setPorcentajeDescuentoPlano(-10.0); // Inválido
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Descuento Negativo");
        request.setDescripcion("Promoción con descuento negativo");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(new java.util.ArrayList<>(List.of(detalleInvalido)));

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")));
    }

    @Test
    @Order(6)
    @DisplayName("Error: Sin detalle base")
    void testErrorSinDetalleBase() {
        // Arrange - solo detalle acumulable, sin base
        DetallePromocionRequest detalleDescuento = new DetallePromocionRequest();
        detalleDescuento.setNombre("Detalle Solo Descuento");
        detalleDescuento.setEsBase(false);
        detalleDescuento.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
        detalleDescuento.setPorcentajeDescuentoPlano(20.0);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Sin Base");
        request.setDescripcion("Promoción sin detalle base");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(new java.util.ArrayList<>(List.of(detalleDescuento))); // Sin detalle base

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(400)
            .body("codigo", not(equalTo("0")));
    }

    @Test
    @Order(7)
    @DisplayName("Consultar promoción Descuento Plano por ID")
    void testConsultarPromocionDescuentoPlanoPorId() {
        if (promocionDescuentoPlanoId == null) {
            System.out.println("⚠️ Skip consulta - no hay promoción creada");
            return;
        }

        // Act & Assert
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .pathParam("id", promocionDescuentoPlanoId)
        .when()
            .get(PROMOCIONES_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.idPromocion", equalTo(promocionDescuentoPlanoId.intValue()))
            .body("datos.detalles", hasSize(2))
            .body("datos.detalles[0].esBase", equalTo(true))
            .body("datos.detalles[1].tipoAcumulable", equalTo("DESCUENTO_PLANO"));
    }

    @Test
    @Order(8)
    @DisplayName("Múltiples descuentos planos válidos")
    void testMultiplesDescuentosPlanos() {
        // Descuentos típicos de negocio
        Double[] descuentos = {10.0, 20.0, 30.0, 50.0};

        for (Double descuento : descuentos) {
            // Arrange - Base detail (required) + Acumulable detail
            DetallePromocionRequest detalleBase = new DetallePromocionRequest();
            detalleBase.setNombre("Detalle Base Sin Descuento");
            detalleBase.setEsBase(true);
            detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
            
            DetallePromocionRequest detalleDescuento = new DetallePromocionRequest();
            detalleDescuento.setNombre(String.format("Detalle Descuento %.0f%%", descuento));
            detalleDescuento.setEsBase(false);
            detalleDescuento.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
            detalleDescuento.setPorcentajeDescuentoPlano(descuento);
            
            PromocionCreateRequest request = new PromocionCreateRequest();
            request.setNombre(String.format("Test Descuento %.0f%%", descuento));
            request.setDescripcion(String.format("Test descuento plano %.0f%%", descuento));
            request.setVigenciaDesde(LocalDate.now());
            request.setVigenciaHasta(LocalDate.now().plusMonths(1));
            request.setDetalles(new java.util.ArrayList<>(List.of(detalleBase, detalleDescuento)));

            // Act & Assert
            given()
                .contentType(ContentType.JSON)
                .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
                .body(request)
            .when()
                .post(PROMOCIONES_ENDPOINT)
            .then()
                .statusCode(200)
                .body("codigo", equalTo("0"))
                .body("datos.detalles[1].tipoAcumulable", equalTo("DESCUENTO_PLANO"));

            System.out.println("✅ Promoción Descuento Plano " + descuento + "% creada exitosamente");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Error: Consultar promoción inexistente")
    void testConsultarPromocionInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .pathParam("id", 888888)
        .when()
            .get(PROMOCIONES_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400)))
            .body("codigo", not(equalTo("0")));
    }

    @Test
    @Order(10)
    @DisplayName("Listar promociones incluyendo Descuento Plano")
    void testListarPromocionesConDescuentoPlano() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @AfterAll
    static void cleanUpDescuentoPlanoTests() {
        System.out.println("✅ Finalizados tests de integración para promociones de Descuento Plano");
    }
}