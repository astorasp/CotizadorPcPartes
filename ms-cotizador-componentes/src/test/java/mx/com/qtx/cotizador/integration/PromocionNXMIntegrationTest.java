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
import mx.com.qtx.cotizador.dto.promocion.request.ParametrosNxMRequest;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;

/**
 * Tests de integración para API de promociones tipo NXM
 * 
 * Valida los endpoints REST que gestionan promociones de tipo "Lleve N, Pague M"
 * usando la estructura DTO correcta y formato de respuesta real.
 * 
 * @author Claude Code
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PromocionNXMIntegrationTest extends BaseIntegrationTest {

    private static final String PROMOCIONES_ENDPOINT = "/promociones";
    private static Long promocionNXMId;
    
    @BeforeAll
    static void setUpNXMTests() {
        System.out.println("🧪 Iniciando tests de integración para promociones NXM");
    }

    @Test
    @Order(1)
    @DisplayName("Crear promoción NXM básica (3x2)")
    void testCrearPromocionNXM3x2() {
        // Arrange
        ParametrosNxMRequest parametros = new ParametrosNxMRequest(3, 2);
        
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle 3x2 Base");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.NXM);
        detalleBase.setParametrosNxM(parametros);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción NXM 3x2 Test");
        request.setDescripcion("Lleve 3 componentes y pague solo 2 - Test automatizado");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(3));
        request.setDetalles(List.of(detalleBase));

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
            .body("datos.nombre", equalTo("Promoción NXM 3x2 Test"))
            .body("datos.detalles", hasSize(1))
            .body("datos.detalles[0].esBase", equalTo(true))
            .body("datos.detalles[0].tipoBase", equalTo("NXM"))
            .body("datos.detalles[0].parametrosNxM", notNullValue())
            .body("datos.detalles[0].parametrosNxM.llevent", equalTo(3))
            .body("datos.detalles[0].parametrosNxM.paguen", equalTo(2))
        .extract()
            .path("datos.idPromocion");
        
        promocionNXMId = idResult.longValue();

        assertNotNull(promocionNXMId, "ID de promoción debe ser generado");
        System.out.println("✅ Promoción NXM 3x2 creada con ID: " + promocionNXMId);
    }

    @Test
    @Order(2)
    @DisplayName("Crear promoción NXM 2x1 (50% descuento)")
    void testCrearPromocionNXM2x1() {
        // Arrange
        ParametrosNxMRequest parametros = new ParametrosNxMRequest(2, 1);
        
        DetallePromocionRequest detalleBase = new DetallePromocionRequest();
        detalleBase.setNombre("Detalle 2x1 Flash");
        detalleBase.setEsBase(true);
        detalleBase.setTipoBase(TipoPromocionBase.NXM);
        detalleBase.setParametrosNxM(parametros);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción NXM 2x1 Flash");
        request.setDescripcion("Lleve 2 componentes y pague solo 1 - Promoción flash");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusWeeks(1));
        request.setDetalles(List.of(detalleBase));

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
            .body("datos.detalles[0].parametrosNxM.llevent", equalTo(2))
            .body("datos.detalles[0].parametrosNxM.paguen", equalTo(1));
    }

    @Test
    @Order(3)
    @DisplayName("Error: llevent menor que paguen")
    void testErrorLleventMenorQuePaguen() {
        // Arrange - configuración inválida
        ParametrosNxMRequest parametrosInvalidos = new ParametrosNxMRequest(2, 3); // Inválido
        
        DetallePromocionRequest detalleInvalido = new DetallePromocionRequest();
        detalleInvalido.setNombre("Detalle Inválido");
        detalleInvalido.setEsBase(true);
        detalleInvalido.setTipoBase(TipoPromocionBase.NXM);
        detalleInvalido.setParametrosNxM(parametrosInvalidos);
        
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Inválida NXM");
        request.setDescripcion("Esta promoción tiene configuración inválida");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusMonths(1));
        request.setDetalles(List.of(detalleInvalido));

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
    @DisplayName("Consultar promoción NXM por ID")
    void testConsultarPromocionNXMPorId() {
        if (promocionNXMId == null) {
            System.out.println("⚠️ Skip consulta - no hay promoción creada");
            return;
        }

        // Act & Assert
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .pathParam("id", promocionNXMId)
        .when()
            .get(PROMOCIONES_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.idPromocion", equalTo(promocionNXMId.intValue()))
            .body("datos.detalles[0].tipoBase", equalTo("NXM"))
            .body("datos.detalles[0].parametrosNxM.llevent", equalTo(3))
            .body("datos.detalles[0].parametrosNxM.paguen", equalTo(2));
    }

    @Test
    @Order(5)
    @DisplayName("Error: Consultar promoción inexistente")
    void testConsultarPromocionInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .pathParam("id", 999999)
        .when()
            .get(PROMOCIONES_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400)))
            .body("codigo", not(equalTo("0")));
    }

    @Test
    @Order(6)
    @DisplayName("Listar todas las promociones")
    void testListarTodasLasPromociones() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
        .when()
            .get(PROMOCIONES_ENDPOINT)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());
    }

    @Test
    @Order(7)
    @DisplayName("Múltiples configuraciones NXM válidas")
    void testMultiplesConfiguracionesNXMValidas() {
        // Configuraciones NXM realistas para test
        int[][] configuracionesNXM = {
            {4, 3}, {5, 3}, {6, 4}  // Configuraciones menos propensas a conflicto
        };

        for (int[] config : configuracionesNXM) {
            int lleve = config[0];
            int pague = config[1];

            // Arrange
            ParametrosNxMRequest parametros = new ParametrosNxMRequest(lleve, pague);
            
            DetallePromocionRequest detalle = new DetallePromocionRequest();
            detalle.setNombre(String.format("Detalle %dx%d", lleve, pague));
            detalle.setEsBase(true);
            detalle.setTipoBase(TipoPromocionBase.NXM);
            detalle.setParametrosNxM(parametros);
            
            PromocionCreateRequest request = new PromocionCreateRequest();
            request.setNombre(String.format("Test NXM %dx%d", lleve, pague));
            request.setDescripcion(String.format("Test promoción lleve %d pague %d", lleve, pague));
            request.setVigenciaDesde(LocalDate.now());
            request.setVigenciaHasta(LocalDate.now().plusMonths(1));
            request.setDetalles(List.of(detalle));

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
                .body("datos.detalles[0].parametrosNxM.llevent", equalTo(lleve))
                .body("datos.detalles[0].parametrosNxM.paguen", equalTo(pague));

            System.out.println("✅ Promoción NXM " + lleve + "x" + pague + " creada exitosamente");
        }
    }

    @AfterAll
    static void cleanUpNXMTests() {
        System.out.println("✅ Finalizados tests de integración para promociones NXM");
    }
}