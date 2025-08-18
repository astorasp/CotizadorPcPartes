package mx.com.qtx.cotizador.integration.pc;

import io.restassured.http.ContentType;
import mx.com.qtx.cotizador.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import io.restassured.response.ValidatableResponse;

/**
 * Tests de integración para el manejo de PCs completas
 * 
 * Casos de uso cubiertos:
 * - CU 2.1: Armar PC completa con sub-componentes
 * - CU 2.2: Agregar componentes a PC existente
 * - CU 2.3: Quitar componentes de PC
 * - CU 2.4: Guardar sub-componentes
 * - CU 2.5: Consultar PC con todos sus componentes
 * 
 * Usa base de datos MySQL compartida via BaseIntegrationTest.
 * Configuración y datos de prueba heredados automáticamente.
 * 
 * Datos precargados disponibles:
 * - PCs: PC001-PC005 (con componentes asociados)
 * - Componentes: MON001-MON005, HDD001-HDD005, GPU001-GPU005
 * - Tipos: PC, DISCO_DURO, MONITOR, TARJETA_VIDEO
 * - Promociones: Regular, Monitores por Volumen, Tarjetas 3x2, etc.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PcIntegrationTest extends BaseIntegrationTest {

    // Base path para el API de PCs
    private static final String PCS_API_PATH = "/pcs/v1/api";
    
    // Base path para el API de Componentes (usado para crear componentes en tests de PC)
    private static final String COMPONENTES_API_PATH = "/componentes/v1/api";

    private static final String USER_ADMIN = "test";
    private static final String PASSWORD_ADMIN = "test123";

    // IDs de PCs para tests (usaremos timestamp para evitar conflictos con datos precargados)
    private static final String PC_TEST_ID = "PC" + System.currentTimeMillis() % 1000;
    private static final String PC_MODIFICAR_ID = "PM" + (System.currentTimeMillis() % 1000 + 1); 
    private static final String PC_ELIMINAR_ID = "PD" + (System.currentTimeMillis() % 1000 + 2);

    // ✅ Configuración heredada de BaseIntegrationTest:
    // - Base de datos MySQL compartida
    // - RestAssured configurado automáticamente  
    // - Autenticación (test/test123)
    // - Puerto aleatorio
    // - Scripts DDL + DML precargados

    // ========================================================================
    // CASO DE USO 2.5: CONSULTAR PC
    // ========================================================================

    @Test
    @DisplayName("CU 2.5.2: Debe fallar consulta sin autenticación")
    void deberiaFallarConsultaSinAutenticacion() {
        given()
            .auth().none()
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH)
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("CU 2.5.3: Debe consultar PC creada por ID exitosamente")
    void deberiaConsultarPcCreadaPorId() {
        // Primero crear una PC para poder consultarla (IDs cortos para evitar límite de 10 caracteres)
        String timeStamp = String.valueOf(System.currentTimeMillis() % 1000);
        String pcId = "PC" + timeStamp;
        String pcParaConsultar = """
            {
                "id": "%s",
                "nombre": "PC para Consultar",
                "precio": 15000.00,
                "descripcion": "PC para test de consulta",
                "modelo": "ConsultaTest",
                "marca": "TestPC",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MON%s",
                        "descripcion": "Monitor para consulta",
                        "marca": "Samsung",
                        "modelo": "ConsultaTest",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VID%s",
                        "descripcion": "Video para consulta",
                        "marca": "NVIDIA",
                        "modelo": "ConsultaTest",
                        "precioBase": 8000.00,
                        "costo": 6400.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDD%s",
                        "descripcion": "Disco para consulta",
                        "marca": "Kingston",
                        "modelo": "ConsultaTest",
                        "precioBase": 2500.00,
                        "costo": 2000.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(pcId, timeStamp, timeStamp, timeStamp);
            
        // PASO 1: Crear la PC (añadir debugging)
        System.out.println("DEBUG: Iniciando creación de " + pcId);
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcParaConsultar)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        System.out.println("DEBUG: PC creada exitosamente");
            
        // PASO 2: Ahora consultarla
        System.out.println("DEBUG: Iniciando consulta de " + pcId);
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH + "/{pcId}", pcId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos.id", equalTo(pcId))
            .body("datos.subComponentes", notNullValue())
            .body("datos.subComponentes.size()", equalTo(3));
            
        System.out.println("DEBUG: Consulta completada exitosamente");
    }

    @Test
    @DisplayName("CU 2.5.4: Debe retornar error al consultar PC inexistente")
    void deberiaRetornarErrorPcInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH + "/PC-INEXISTENTE")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // Recurso no encontrado
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 2.1: ARMAR PC (CREAR PC COMPLETA)
    // ========================================================================

    @Test
    @DisplayName("CU 2.1.1: Debe armar PC completa usando componentes nuevos")
    void deberiaArmarPcCompletaConComponentesNuevos() {
        String nuevaPc = """
            {
                "id": "%s",
                "nombre": "PC Gaming Ultimate",
                "precio": 18000.00,
                "descripcion": "PC Gaming de alto rendimiento personalizada",
                "modelo": "Gaming Ultimate 2024",
                "marca": "CustomPC",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "M%s",
                        "descripcion": "Monitor Gamer 32 pulgadas",
                        "marca": "ASUS",
                        "modelo": "ROG32",
                        "precioBase": 5200.00,
                        "costo": 4160.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "V%s",
                        "descripcion": "Tarjeta de Video RTX 4070",
                        "marca": "NVIDIA",
                        "modelo": "RTX4070",
                        "precioBase": 9500.00,
                        "costo": 7600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "H%s",
                        "descripcion": "SSD NVMe 2TB",
                        "marca": "Samsung",
                        "modelo": "980PRO",
                        "precioBase": 3300.00,
                        "costo": 2640.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(PC_TEST_ID, PC_TEST_ID, PC_TEST_ID, PC_TEST_ID);

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(nuevaPc)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", notNullValue())
            .body("datos", notNullValue());
    }

    @Test
    @DisplayName("CU 2.1.2: Debe fallar al armar PC con datos inválidos")
    void deberiaFallarArmarPcConDatosInvalidos() {
        String pcInvalida = """
            {
                "id": "",
                "nombre": "",
                "precio": -100.00,
                "descripcion": "",
                "modelo": "",
                "marca": "",
                "cantidad": 0,
                "subComponentes": []
            }
            """;

        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcInvalida)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("2")) // Error de validación
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 2.1.3: Debe fallar al armar PC con ID duplicado")
    void deberiaFallarArmarPcConIdDuplicado() {
        String pcOriginal = """
            {
                "id": "PC-DUPLICADO",
                "nombre": "PC Original",
                "precio": 15000.00,
                "descripcion": "PC original",
                "modelo": "Original",
                "marca": "TestPC",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MON-ORIG",
                        "descripcion": "Monitor original",
                        "marca": "Samsung",
                        "modelo": "Original",
                        "precioBase": 3000.00,
                        "costo": 2400.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VID-ORIG",
                        "descripcion": "Tarjeta de video original",
                        "marca": "NVIDIA",
                        "modelo": "GTX 1650",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDD-ORIG",
                        "descripcion": "Disco duro original",
                        "marca": "Seagate",
                        "modelo": "Barracuda 1TB",
                        "precioBase": 1200.00,
                        "costo": 960.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """;

        // Crear PC original
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcOriginal)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200);

        String pcDuplicada = """
            {
                "id": "PC-DUPLICADO",
                "nombre": "PC Duplicada",
                "precio": 10000.00,
                "descripcion": "PC que ya existe",
                "modelo": "Duplicada",
                "marca": "TestPC",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MON-DUP",
                        "descripcion": "Monitor duplicado",
                        "marca": "Test",
                        "modelo": "Test",
                        "precioBase": 3000.00,
                        "costo": 2400.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VID-DUP",
                        "descripcion": "Tarjeta de video duplicada",
                        "marca": "Test",
                        "modelo": "Test",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDD-DUP",
                        "descripcion": "Disco duro duplicado",
                        "marca": "Test",
                        "modelo": "Test",
                        "precioBase": 1200.00,
                        "costo": 960.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """;

        // Intentar crear la segunda PC con ID duplicado (debe fallar)
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcDuplicada)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("5")) // Recurso ya existe
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 2.2: AGREGAR COMPONENTE A PC
    // ========================================================================

    @Test
    @DisplayName("CU 2.2.1: Debe agregar componente nuevo a PC precargada exitosamente")
    void deberiaAgregarComponenteNuevoAPcPrecargada() {
        // 1. Primero crear una PC base válida (con monitor, tarjeta y disco)
        String pcBaseId = "PCADD" + System.currentTimeMillis() % 1000;
        String timeStamp = String.valueOf(System.currentTimeMillis() % 1000);
        String pcBase = """
            {
                "id": "%s",
                "nombre": "PC Base para agregar componentes",
                "precio": 15000.00,
                "descripcion": "PC base para pruebas de agregar componentes",
                "modelo": "TestAdd2024",
                "marca": "TestBuild",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MONBASE%s",
                        "descripcion": "Monitor base para PC",
                        "marca": "Samsung",
                        "modelo": "24inch",
                        "precioBase": 3000.00,
                        "costo": 2400.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VIDBASE%s",
                        "descripcion": "Tarjeta de video base",
                        "marca": "NVIDIA",
                        "modelo": "GTX 1650",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDDBASE%s",
                        "descripcion": "Disco duro base",
                        "marca": "Seagate",
                        "modelo": "Barracuda 1TB",
                        "precioBase": 1200.00,
                        "costo": 960.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(pcBaseId, timeStamp, timeStamp, timeStamp);
            
        // Crear la PC
        ValidatableResponse creacionResponse = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcBase)
        .when()
            .post(PCS_API_PATH)
        .then()
            .log().all(); // Debugging de creación
            
        System.out.println("=== DEBUGGING CREACIÓN PC ===");
        System.out.println("Status Code: " + creacionResponse.extract().statusCode());
        System.out.println("Response Body: " + creacionResponse.extract().asString());
        System.out.println("============================");
        
        creacionResponse
            .statusCode(200)
            .body("codigo", equalTo("0"));
        
        // 2. Ahora agregar un componente nuevo a la PC
        String nuevoComponente = """
            {
                "id": "MEMADD%s",
                "descripcion": "Memoria RAM adicional",
                "marca": "Corsair",
                "modelo": "Vengeance LPX",
                "precioBase": 1800.00,
                "costo": 1440.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(System.currentTimeMillis() % 10000);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(nuevoComponente)
        .when()
            .post(PCS_API_PATH + "/{pcId}/componentes", pcBaseId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 2.2.2: Debe agregar componente precargado existente a PC")
    void deberiaAgregarComponentePrecargadoAPc() {
        // 1. Crear un componente que actuará como "precargado"
        String componentePrecargadoId = "HDDPREC" + System.currentTimeMillis() % 1000;
        String componentePrecargado = """
            {
                "id": "%s",
                "descripcion": "Disco duro precargado para pruebas",
                "marca": "Western Digital",
                "modelo": "Blue 1TB",
                "precioBase": 1200.00,
                "costo": 960.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(componentePrecargadoId);
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componentePrecargado)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
        
        // 2. Crear una PC válida para agregar el componente
        String pcTargetId = "PCTAR" + System.currentTimeMillis() % 1000;
        String timeStamp2 = String.valueOf(System.currentTimeMillis() % 1000 + 10);
        String pcTarget = """
            {
                "id": "%s",
                "nombre": "PC Target para componente precargado",
                "precio": 18000.00,
                "descripcion": "PC para recibir componente precargado",
                "modelo": "TestTarget2024",
                "marca": "TestBuild",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MONTAR%s",
                        "descripcion": "Monitor para PC target",
                        "marca": "LG",
                        "modelo": "UltraWide",
                        "precioBase": 3500.00,
                        "costo": 2800.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VIDTAR%s",
                        "descripcion": "Tarjeta de video base",
                        "marca": "NVIDIA",
                        "modelo": "GTX 1650",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDDTAR%s",
                        "descripcion": "Disco duro para target",
                        "marca": "WD",
                        "modelo": "Black 1TB",
                        "precioBase": 1500.00,
                        "costo": 1200.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(pcTargetId, timeStamp2, timeStamp2, timeStamp2);
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcTarget)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
        
        // 3. Agregar el componente "precargado" a la PC
        String componenteExistente = """
            {
                "id": "%s",
                "descripcion": "Disco duro precargado para pruebas",
                "marca": "Western Digital", 
                "modelo": "Blue 1TB",
                "precioBase": 1200.00,
                "costo": 960.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(componentePrecargadoId);
            
        ValidatableResponse response = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteExistente)
        .when()
            .post(PCS_API_PATH + "/{pcId}/componentes", pcTargetId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 2.2.3: Debe fallar al agregar componente a PC inexistente")
    void deberiaFallarAgregarComponenteAPcInexistente() {
        String nuevoComponente = """
            {
                "id": "MEMFAIL%s",
                "descripcion": "Memoria para PC inexistente",
                "marca": "Corsair",
                "modelo": "Test",
                "precioBase": 1500.00,
                "costo": 1200.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(System.currentTimeMillis() % 10000);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(nuevoComponente)
        .when()
            .post(PCS_API_PATH + "/PC-INEXISTENTE/componentes")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // PC no encontrada
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // CASO DE USO 2.3: QUITAR COMPONENTE DE PC
    // ========================================================================

    @Test
    @DisplayName("CU 2.3.1: Debe quitar componente precargado de PC exitosamente")
    void deberiaQuitarComponentePrecargadoDePc() {
        // 1. Crear una PC con componentes para poder quitar uno después
        String pcId = "PCQUITAR" + System.currentTimeMillis() % 1000;
        String timeStamp = String.valueOf(System.currentTimeMillis() % 1000);
        String pcConComponentes = """
            {
                "id": "%s",
                "nombre": "PC para quitar componente",
                "precio": 15000.00,
                "descripcion": "PC para prueba de quitar componente",
                "modelo": "TestQuitar",
                "marca": "TestBuild",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MOQ%s",
                        "descripcion": "Monitor para quitar",
                        "marca": "Samsung",
                        "modelo": "24inch",
                        "precioBase": 3000.00,
                        "costo": 2400.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VIQ%s",
                        "descripcion": "Tarjeta de video para quitar",
                        "marca": "NVIDIA",
                        "modelo": "GTX 1650",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDQ%s",
                        "descripcion": "Disco duro para quitar",
                        "marca": "Seagate",
                        "modelo": "Barracuda 1TB",
                        "precioBase": 1200.00,
                        "costo": 960.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(pcId, timeStamp, timeStamp, timeStamp);
            
        // Crear la PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcConComponentes)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // 2. Quitar el disco duro de la PC
        String componenteIdQuitar = "HDQ" + timeStamp;
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(PCS_API_PATH + "/{pcId}/componentes/{componenteId}", pcId, componenteIdQuitar)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 2.3.2: Debe quitar componente agregado previamente")
    void deberiaQuitarComponenteAgregadoPreviamente() {
        // 1. Crear una PC base válida
        String pcId = "PCAGR" + System.currentTimeMillis() % 1000;
        String timeStamp = String.valueOf(System.currentTimeMillis() % 1000);
        String pcBase = """
            {
                "id": "%s",
                "nombre": "PC para agregar y quitar componente",
                "precio": 15000.00,
                "descripcion": "PC para prueba de agregar y quitar",
                "modelo": "TestAgregar",
                "marca": "TestBuild",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MONAGR%s",
                        "descripcion": "Monitor base",
                        "marca": "Samsung",
                        "modelo": "24inch",
                        "precioBase": 3000.00,
                        "costo": 2400.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VIDAGR%s",
                        "descripcion": "Tarjeta de video base",
                        "marca": "NVIDIA",
                        "modelo": "GTX 1650",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HDDAGR%s",
                        "descripcion": "Disco duro base",
                        "marca": "Seagate",
                        "modelo": "Barracuda 1TB",
                        "precioBase": 1200.00,
                        "costo": 960.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(pcId, timeStamp, timeStamp, timeStamp);
            
        // Crear la PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcBase)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // 2. Agregar un componente adicional
        String componenteIdAdicional = "HDDADD" + (System.currentTimeMillis() % 10000);
        String componenteAdicional = """
            {
                "id": "%s",
                "descripcion": "Disco adicional para luego quitar",
                "marca": "Western Digital",
                "modelo": "Blue 1TB",
                "precioBase": 1300.00,
                "costo": 1040.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(componenteIdAdicional);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteAdicional)
        .when()
            .post(PCS_API_PATH + "/{pcId}/componentes", pcId)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"));
            
        // 3. Quitar el componente que acabamos de agregar
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(PCS_API_PATH + "/{pcId}/componentes/{compId}", pcId, componenteIdAdicional)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 2.3.3: Debe fallar al quitar componente de PC inexistente")
    void deberiaFallarQuitarComponenteDePcInexistente() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(PCS_API_PATH + "/PC-INEXISTENTE/componentes/HDD001")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // PC no encontrada
            .body("mensaje", notNullValue());
    }

    @Test
    @DisplayName("CU 2.3.4: Debe fallar al quitar componente inexistente de PC")
    void deberiaFallarQuitarComponenteInexistenteDePc() {
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(PCS_API_PATH + "/PC002/componentes/COMP-INEXISTENTE")
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")) // Componente no encontrado
            .body("mensaje", notNullValue());
    }

    // ========================================================================
    // GESTIÓN COMPLETA DE PC
    // ========================================================================

    @Test
    @DisplayName("CU 2.1-2.3: Debe gestionar ciclo completo de PC con componentes mixtos")
    void deberiaGestionarCicloCompletoDePcConComponentesMixtos() {
        // PASO 0: Crear los componentes "precargados" que la prueba necesita (IDs únicos)
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000);
        String monId = "MON" + timestamp;
        String gpuId = "GPU" + timestamp;
        String hddId = "HDD" + timestamp;
        String hddCicloId = "HDDC" + timestamp;
        
        // IDs para componentes nuevos en la PC (diferentes a los precargados)
        String monPcId = "MONPC" + timestamp;
        String gpuPcId = "GPUPC" + timestamp;
        String hddPcId = "HDDPC" + timestamp;
        
        // Crear monitor
        String componenteMon = """
            {
                "id": "%s",
                "descripcion": "Monitor Samsung para ciclo mixto",
                "marca": "Samsung",
                "modelo": "Monitor24",
                "precioBase": 4000.00,
                "costo": 3200.00,
                "tipoComponente": "MONITOR"
            }
            """.formatted(monId);
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteMon)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200);
            
        // Crear GPU
        String componenteGpu = """
            {
                "id": "%s",
                "descripcion": "Tarjeta de Video NVIDIA para ciclo mixto",
                "marca": "NVIDIA",
                "modelo": "GTX1660",
                "precioBase": 8500.00,
                "costo": 6800.00,
                "tipoComponente": "TARJETA_VIDEO"
            }
            """.formatted(gpuId);
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteGpu)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200);
            
        // Crear HDD
        String componenteHdd = """
            {
                "id": "%s",
                "descripcion": "Disco Duro 2TB SATA",
                "marca": "Seagate",
                "modelo": "Barracuda2TB",
                "precioBase": 2200.00,
                "costo": 1760.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(hddId);
        
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componenteHdd)
        .when()
            .post(COMPONENTES_API_PATH)
        .then()
            .statusCode(200);
        
        // 1. Crear PC con mezcla de componentes nuevos y precargados
        String pcCompleta = """
            {
                "id": "%s",
                "nombre": "PC Ciclo Completo Mixta",
                "precio": 22000.00,
                "descripcion": "PC para prueba de ciclo completo usando componentes mixtos",
                "modelo": "CicloMixto2024",
                "marca": "TestBuild",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "%s",
                        "descripcion": "Monitor Samsung para ciclo mixto",
                        "marca": "Samsung",
                        "modelo": "Monitor24",
                        "precioBase": 4000.00,
                        "costo": 3200.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "%s",
                        "descripcion": "GPU NVIDIA para ciclo mixto",
                        "marca": "NVIDIA",
                        "modelo": "RTX3060",
                        "precioBase": 8000.00,
                        "costo": 6400.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "%s",
                        "descripcion": "Disco SSD para ciclo completo",
                        "marca": "Samsung",
                        "modelo": "980 EVO",
                        "precioBase": 2800.00,
                        "costo": 2240.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(PC_MODIFICAR_ID, monPcId, gpuPcId, hddPcId);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcCompleta)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("datos", notNullValue());

        // 2. Verificar que se creó correctamente
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH + "/{pcId}", PC_MODIFICAR_ID)
        .then()
            .statusCode(200)
            .body("datos.subComponentes.size()", equalTo(3))
            .body("datos.subComponentes.findAll { it.id in ['" + monPcId + "', '" + gpuPcId + "', '" + hddPcId + "'] }.size()", equalTo(3));

        // 3. Agregar componente precargado adicional
        String componentePrecargado = """
            {
                "id": "%s",
                "descripcion": "Disco Duro 2TB SATA",
                "marca": "Seagate",
                "modelo": "Barracuda",
                "precioBase": 2000.00,
                "costo": 1600.00,
                "tipoComponente": "DISCO_DURO"
            }
            """.formatted(hddId);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(componentePrecargado)
        .when()
            .post(PCS_API_PATH + "/{pcId}/componentes", PC_MODIFICAR_ID)
        .then()
            .statusCode(200);

        // 4. Verificar que la PC tiene 4 componentes
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH + "/{pcId}", PC_MODIFICAR_ID)
        .then()
            .statusCode(200)
            .body("datos.subComponentes.size()", equalTo(4))
            .body("datos.subComponentes.find { it.id == '" + hddId + "' }.descripcion", equalTo("Disco Duro 2TB SATA"));

        // 5. Quitar un componente precargado
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(PCS_API_PATH + "/{pcId}/componentes/{componenteId}", PC_MODIFICAR_ID, hddPcId)
        .then()
            .statusCode(200);

        // 6. Verificar que la PC tiene 3 componentes y ya no incluye el disco quitado
        ValidatableResponse response = given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH + "/{pcId}", PC_MODIFICAR_ID)
        .then()
            .log().all(); // Imprimir response completo para debugging
            
        System.out.println("=== DEBUGGING FINAL ===");
        System.out.println("Status Code: " + response.extract().statusCode());
        System.out.println("Response Body: " + response.extract().asString());
        System.out.println("========================");
        
        response
            .statusCode(200)
            .body("datos.subComponentes.size()", equalTo(3))
            .body("datos.subComponentes.findAll { it.id == '" + hddPcId + "' }.size()", equalTo(0))
            .body("datos.subComponentes.findAll { it.id in ['" + monPcId + "', '" + gpuPcId + "', '" + hddId + "'] }.size()", equalTo(3));
    }

    @Test
    @DisplayName("CU 2.4: Debe eliminar PC completa exitosamente")
    void deberiaEliminarPcCompleta() {
        // Primero crear la PC a eliminar
        String pcEliminar = """
            {
                "id": "%s",
                "nombre": "PC para Eliminar",
                "precio": 12000.00,
                "descripcion": "PC que será eliminada",
                "modelo": "Eliminar",
                "marca": "TestPC",
                "cantidad": 1,
                "subComponentes": [
                    {
                        "id": "MON-ELIM",
                        "descripcion": "Monitor para eliminar",
                        "marca": "Samsung",
                        "modelo": "ElimTest",
                        "precioBase": 4500.00,
                        "costo": 3600.00,
                        "tipoComponente": "MONITOR"
                    },
                    {
                        "id": "VID-ELIM",
                        "descripcion": "Video para eliminar",
                        "marca": "NVIDIA",
                        "modelo": "ElimTest",
                        "precioBase": 8000.00,
                        "costo": 6400.00,
                        "tipoComponente": "TARJETA_VIDEO"
                    },
                    {
                        "id": "HD-ELIM",
                        "descripcion": "Disco para eliminar",
                        "marca": "Kingston",
                        "modelo": "ElimTest",
                        "precioBase": 2500.00,
                        "costo": 2000.00,
                        "tipoComponente": "DISCO_DURO"
                    }
                ]
            }
            """.formatted(PC_ELIMINAR_ID);
            
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
            .body(pcEliminar)
        .when()
            .post(PCS_API_PATH)
        .then()
            .statusCode(200);
            
        // Ahora eliminar la PC
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .delete(PCS_API_PATH + "/{id}", PC_ELIMINAR_ID)
        .then()
            .statusCode(200)
            .body("codigo", equalTo("0"))
            .body("mensaje", equalTo("PC eliminada exitosamente"));
            
        // Verificar que ya no existe
        given()
            .auth().basic(USER_ADMIN, PASSWORD_ADMIN)
            .contentType(ContentType.JSON)
        .when()
            .get(PCS_API_PATH + "/{id}", PC_ELIMINAR_ID)
        .then()
            .statusCode(400)
            .body("codigo", equalTo("4")); // PC no encontrada
    }

    // ========================================================================
    // TESTS DE SEGURIDAD Y VALIDACIÓN
    // ========================================================================
    
    @Test
    @DisplayName("Seguridad: Todos los endpoints de PC requieren autenticación")
    void todosLosEndpointsDePcRequierenAutenticacion() {
        String pc = """
            {
                "id": "TEST-AUTH",
                "nombre": "Test Auth",
                "precio": 10000.00,
                "descripcion": "Test",
                "modelo": "Test",
                "marca": "Test",
                "cantidad": 1,
                "subComponentes": []
            }
            """;
        
        String componente = """
            {
                "id": "COMP-AUTH",
                "descripcion": "Test",
                "marca": "Test",
                "modelo": "Test",
                "precioBase": 100.00,
                "costo": 80.00,
                "tipoComponente": "MONITOR"
            }
            """;
        
        // GET sin auth - todas las PCs
        given().auth().none().contentType(ContentType.JSON)
        .when().get(PCS_API_PATH)
        .then().statusCode(401);
        
        // GET sin auth - PC por ID
        given().auth().none().contentType(ContentType.JSON)
        .when().get(PCS_API_PATH + "/PC001")
        .then().statusCode(401);
        
        // POST sin auth - crear PC
        given().auth().none().contentType(ContentType.JSON).body(pc)
        .when().post(PCS_API_PATH)
        .then().statusCode(401);
        
        // PUT sin auth - actualizar PC
        given().auth().none().contentType(ContentType.JSON).body(pc)
        .when().put(PCS_API_PATH + "/PC001")
        .then().statusCode(401);
        
        // DELETE sin auth - eliminar PC
        given().auth().none().contentType(ContentType.JSON)
        .when().delete(PCS_API_PATH + "/PC001")
        .then().statusCode(401);
        
        // POST sin auth - agregar componente
        given().auth().none().contentType(ContentType.JSON).body(componente)
        .when().post(PCS_API_PATH + "/PC001/componentes")
        .then().statusCode(401);
        
        // DELETE sin auth - quitar componente
        given().auth().none().contentType(ContentType.JSON)
        .when().delete(PCS_API_PATH + "/PC001/componentes/COMP001")
        .then().statusCode(401);
    }

    @Test
    @DisplayName("A. Infraestructura: Aplicación debe arrancar correctamente")
    void aplicacionDebeArrancar() {
        assertThat(port).isGreaterThan(0);
    }

    @AfterAll 
    static void cleanup() {
        System.out.println("🧹 Tests de PC completados - contenedor será destruido automáticamente");
    }
} 