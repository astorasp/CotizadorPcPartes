package mx.com.qtx.cotizador.integration;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.DetallePromocionRequest;
import mx.com.qtx.cotizador.dto.promocion.request.EscalaDescuentoRequest;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionAcumulable;

/**
 * Tests de integración para API de promociones con descuento por cantidad
 * 
 * Valida los endpoints REST que gestionan promociones de tipo "Descuento por Cantidad"
 * con múltiples escalas de descuento basadas en cantidades mínimas.
 * 
 * @author Claude Code
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PromocionDescuentoPorCantidadIntegrationTest extends BaseIntegrationTest {

    private static final String PROMOCIONES_ENDPOINT = "/promociones";
    private static Integer promocionPorCantidadId;
    
    @BeforeAll
    static void setUpPorCantidadTests() {
        System.out.println("🧪 Iniciando tests de integración para promociones por cantidad");
    }

    @Test
    @Order(1)
    @DisplayName("Crear promoción por cantidad - escala múltiple")
    void testCrearPromocionPorCantidadEscalaMultiple() {
        // Arrange - configuración de 3 escalas
        EscalaDescuentoRequest escala1 = new EscalaDescuentoRequest(3, 5.0);
        EscalaDescuentoRequest escala2 = new EscalaDescuentoRequest(5, 10.0);
        EscalaDescuentoRequest escala3 = new EscalaDescuentoRequest(10, 15.0);
        
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleAcumulable = new DetallePromocionRequest();
        detalleAcumulable.setNombre("Detalle Descuento por Cantidad");
        detalleAcumulable.setEsBase(false);
        detalleAcumulable.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD);
        detalleAcumulable.setEscalasDescuento(List.of(escala1, escala2, escala3));
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Cantidad Múltiple Test");
        request.setDescripcion("Descuento escalonado: 3+→5%, 5+→10%, 10+→15%");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(2));
        request.setDetalles(List.of(detalleBase, detalleAcumulable));

        // Act & Assert
        Integer idResult = given()
            .contentType(ContentType.JSON)
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .body(request)
        .when()
            .post(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", containsString("creada exitosamente"))
            .body("datos", notNullValue())
            .body("datos.nombre", equalTo("Promoción Cantidad Múltiple Test"))
            .body("datos.detalles", hasSize(2))
            // Verificar detalle base
            .body("datos.detalles[0].esBase", equalTo(true))
            .body("datos.detalles[0].tipoBase", equalTo("SIN_DESCUENTO"))
            // Verificar detalle acumulable
            .body("datos.detalles[1].esBase", equalTo(false))
            .body("datos.detalles[1].tipoAcumulable", equalTo("DESCUENTO_POR_CANTIDAD"))
            .body("datos.detalles[1].escalasDescuento", hasSize(3))
            .body("datos.detalles[1].escalasDescuento[0].cantidad", equalTo(3))
            .body("datos.detalles[1].escalasDescuento[0].descuento", equalTo(5.0f))
            .body("datos.detalles[1].escalasDescuento[1].cantidad", equalTo(5))
            .body("datos.detalles[1].escalasDescuento[1].descuento", equalTo(10.0f))
            .body("datos.detalles[1].escalasDescuento[2].cantidad", equalTo(10))
            .body("datos.detalles[1].escalasDescuento[2].descuento", equalTo(15.0f))
        .extract()
            .path("datos.idPromocion");
        
        promocionPorCantidadId = idResult;
        assertNotNull(promocionPorCantidadId, "ID de promoción debe ser generado");
        System.out.println("✅ Promoción por cantidad creada con ID: " + promocionPorCantidadId);
    }

    @Test
    @Order(2)
    @DisplayName("Crear promoción por cantidad - escala simple")
    void testCrearPromocionPorCantidadEscalaSimple() {
        // Arrange - una sola escala
        EscalaDescuentoRequest escalaUnica = new EscalaDescuentoRequest(2, 12.5);
        
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle Base Simple");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleAcumulable = new DetallePromocionRequest();
        detalleAcumulable.setNombre("Descuento Cantidad Simple");
        detalleAcumulable.setEsBase(false);
        detalleAcumulable.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD);
        detalleAcumulable.setEscalasDescuento(List.of(escalaUnica));
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Cantidad Simple");
        request.setDescripcion("12.5% descuento desde 2 unidades");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusWeeks(3));
        request.setDetalles(List.of(detalleBase, detalleAcumulable));

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
            .body("datos.detalles[1].escalasDescuento", hasSize(1))
            .body("datos.detalles[1].escalasDescuento[0].cantidad", equalTo(2))
            .body("datos.detalles[1].escalasDescuento[0].descuento", equalTo(12.5f));
        
        System.out.println("✅ Promoción por cantidad simple creada exitosamente");
    }

    @Test
    @Order(3)
    @DisplayName("Error: descuento negativo")
    void testErrorDescuentoNegativo() {
        // Arrange - escala con descuento inválido
        EscalaDescuentoRequest escalaNegativa = new EscalaDescuentoRequest(5, -10.0); // Descuento negativo
        
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Base Inválida");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleInvalido = new DetallePromocionRequest();
        detalleInvalido.setNombre("Descuento Negativo");
        detalleInvalido.setEsBase(false);
        detalleInvalido.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD);
        detalleInvalido.setEscalasDescuento(List.of(escalaNegativa));
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Inválida Negativa");
        request.setDescripcion("Esta promoción tiene descuento negativo");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(List.of(detalleBase, detalleInvalido));

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
    @Order(4)
    @DisplayName("Error: escalas sin configuración")
    void testErrorEscalasSinConfiguracion() {
        // Arrange - sin escalas
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Base Sin Escalas");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleVacio = new DetallePromocionRequest();
        detalleVacio.setNombre("Sin Escalas");
        detalleVacio.setEsBase(false);
        detalleVacio.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD);
        detalleVacio.setEscalasDescuento(List.of()); // Lista vacía
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Sin Escalas");
        request.setDescripcion("Esta promoción no tiene escalas configuradas");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(List.of(detalleBase, detalleVacio));

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
    @DisplayName("Consultar promoción por cantidad por ID")
    void testConsultarPromocionPorCantidadPorId() {
        if (promocionPorCantidadId == null) {
            System.out.println("⚠️ Skip consulta - no hay promoción creada");
            return;
        }

        // Act & Assert
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .pathParam("id", promocionPorCantidadId)
        .when()
            .get(PROMOCIONES_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.idPromocion", equalTo(promocionPorCantidadId))
            .body("datos.detalles[1].tipoAcumulable", equalTo("DESCUENTO_POR_CANTIDAD"))
            .body("datos.detalles[1].escalasDescuento", hasSize(3))
            .body("datos.detalles[1].escalasDescuento[0].cantidad", equalTo(3))
            .body("datos.detalles[1].escalasDescuento[1].cantidad", equalTo(5))
            .body("datos.detalles[1].escalasDescuento[2].cantidad", equalTo(10));
    }

    @Test
    @Order(6)
    @DisplayName("Promoción con escalas complejas - 5 niveles")
    void testPromocionEscalasComplejas5Niveles() {
        // Arrange - configuración de 5 escalas
        EscalaDescuentoRequest[] escalas = {
            new EscalaDescuentoRequest(2, 2.0),
            new EscalaDescuentoRequest(4, 4.0),
            new EscalaDescuentoRequest(6, 7.0),
            new EscalaDescuentoRequest(10, 12.0),
            new EscalaDescuentoRequest(20, 20.0)
        };
        
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Base Escalas Complejas");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        
        DetallePromocionRequest detalleComplejo = new DetallePromocionRequest();
        detalleComplejo.setNombre("Escalas Complejas 5 Niveles");
        detalleComplejo.setEsBase(false);
        detalleComplejo.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD);
        detalleComplejo.setEscalasDescuento(List.of(escalas));
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Escalas Complejas");
        request.setDescripcion("5 niveles: 2+→2%, 4+→4%, 6+→7%, 10+→12%, 20+→20%");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(6));
        request.setDetalles(List.of(detalleBase, detalleComplejo));

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
            .body("datos.detalles[1].escalasDescuento", hasSize(5))
            .body("datos.detalles[1].escalasDescuento[0].descuento", equalTo(2.0f))
            .body("datos.detalles[1].escalasDescuento[4].cantidad", equalTo(20))
            .body("datos.detalles[1].escalasDescuento[4].descuento", equalTo(20.0f));
        
        System.out.println("✅ Promoción escalas complejas (5 niveles) creada exitosamente");
    }

    @Test
    @Order(7)
    @DisplayName("Listar todas las promociones")
    void testListarTodasLasPromocionesPorCantidad() {
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
    static void cleanUpPorCantidadTests() {
        System.out.println("✅ Finalizados tests de integración para promociones por cantidad");
    }
}