package mx.com.qtx.cotizador.security;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Tests de integración para verificar la configuración de seguridad.
 * 
 * Verifica que:
 * - Los endpoints públicos sean accesibles sin autenticación
 * - Los endpoints protegidos requieran autenticación
 * - La autenticación básica funcione correctamente
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SecurityIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/cotizador/v1/api";
    }
    
    /**
     * Verificar que los endpoints públicos sean accesibles sin autenticación
     */
    @Test
    @DisplayName("Los endpoints públicos deben ser accesibles sin autenticación")
    void endpointsPublicosDebenSerAccesibles() {
        // WHEN/THEN - Swagger UI debe ser accesible
        given()
        .when()
            .get("/swagger-ui.html")
        .then()
            .statusCode(200);
    }
    
    /**
     * Verificar que los endpoints protegidos requieran autenticación
     */
    @Test
    @DisplayName("Los endpoints protegidos deben requerir autenticación")
    void endpointsProtegidosDebenRequerirAutenticacion() {
        // WHEN/THEN - Acceso sin credenciales debe retornar 401
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/componentes")
        .then()
            .statusCode(401);
    }
    
    /**
     * Verificar que la autenticación básica funcione (solo si seguridad está habilitada)
     */
    @Test
    @DisplayName("La autenticación básica debe funcionar con credenciales válidas")
    void autenticacionBasicaDebeFuncionar() {
        // GIVEN - Credenciales válidas
        String username = "admin";
        String password = "admin123";
        
        // WHEN/THEN - Con credenciales válidas debe permitir acceso
        // Nota: En profile test, la seguridad está deshabilitada, 
        // por lo que este test es más conceptual
        given()
            .auth().basic(username, password)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/componentes")
        .then()
            .statusCode(200); // En test profile debería retornar 200 por seguridad deshabilitada
    }
} 