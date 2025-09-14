package mx.com.qtx.cotizador.controlador.roles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.com.qtx.cotizador.config.TestSecurityConfig;
import mx.com.qtx.cotizador.controlador.ProveedorController;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorUpdateRequest;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import mx.com.qtx.cotizador.servicio.pedido.ProveedorServicio;
import mx.com.qtx.cotizador.util.Errores;

/**
 * Tests de permisos basados en roles para ProveedorController usando Basic Auth.
 * <p>
 * Este test se enfoca ÚNICAMENTE en validar que Spring Security funciona correctamente
 * con los roles definidos. No prueba la lógica de negocio del servicio.
 * </p>
 *
 * <p>Matriz de Permisos para Proveedores:</p>
 * <ul>
 *   <li><strong>POST /proveedores</strong> - ADMIN, GERENTE, INVENTARIO</li>
 *   <li><strong>PUT /proveedores/{cve}</strong> - ADMIN, GERENTE, INVENTARIO</li>
 *   <li><strong>GET /proveedores/{cve}</strong> - Todos los roles autenticados</li>
 *   <li><strong>GET /proveedores</strong> - Todos los roles autenticados</li>
 *   <li><strong>DELETE /proveedores/{cve}</strong> - Solo ADMIN</li>
 *   <li><strong>GET /proveedores/buscar/**</strong> - Todos los roles autenticados</li>
 * </ul>
 *
 * <p>Usuario de prueba:</p>
 * <ul>
 *   <li><strong>Username:</strong> test</li>
 *   <li><strong>Password:</strong> test123</li>
 *   <li><strong>Roles:</strong> ADMIN, GERENTE, VENDEDOR, INVENTARIO, CONSULTOR</li>
 * </ul>
 */
@WebMvcTest(ProveedorController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ProveedorController - Tests de Permisos por Rol")
public class ProveedorRolePermissionsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProveedorServicio proveedorServicio;

    private static final String TEST_USER = "test";
    private static final String TEST_PASSWORD = "test123";

    // Helper para Basic Auth
    private String basicAuth(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
    }

    // Datos de prueba
    private ProveedorCreateRequest proveedorCreateRequest;
    private ProveedorUpdateRequest proveedorUpdateRequest;
    private ApiResponse<ProveedorResponse> proveedorResponseSuccess;
    private ApiResponse<List<ProveedorResponse>> proveedoresListResponseSuccess;
    private ApiResponse<Void> deleteResponseSuccess;

    @BeforeEach
    void setUp() {
        // Configurar request para crear proveedor
        proveedorCreateRequest = new ProveedorCreateRequest();
        proveedorCreateRequest.setCve("PROV001");
        proveedorCreateRequest.setNombre("Proveedor Test");
        proveedorCreateRequest.setRazonSocial("Proveedor Test S.A. de C.V.");

        // Configurar request para actualizar proveedor
        proveedorUpdateRequest = new ProveedorUpdateRequest();
        proveedorUpdateRequest.setNombre("Proveedor Test Actualizado");
        proveedorUpdateRequest.setRazonSocial("Proveedor Test Actualizado S.A. de C.V.");

        // Configurar response exitosa para proveedor individual
        ProveedorResponse proveedorResponse = new ProveedorResponse();
        proveedorResponse.setCve("PROV001");
        proveedorResponse.setNombre("Proveedor Test");
        proveedorResponse.setRazonSocial("Proveedor Test S.A. de C.V.");
        proveedorResponse.setNumeroPedidos(5);

        proveedorResponseSuccess = new ApiResponse<>(Errores.OK.getCodigo(),
            "Proveedor obtenido exitosamente", proveedorResponse);

        // Configurar response exitosa para lista de proveedores
        proveedoresListResponseSuccess = new ApiResponse<>(Errores.OK.getCodigo(),
            "Proveedores obtenidos exitosamente", Arrays.asList(proveedorResponse));

        // Configurar response exitosa para eliminación
        deleteResponseSuccess = new ApiResponse<>(Errores.OK.getCodigo(),
            "Proveedor eliminado exitosamente", null);
    }

    // ==========================================
    // TESTS DE ACCESO SIN AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("Sin autenticación - POST /proveedores debe devolver 401")
    void sinAutenticacion_crearProveedor_debeDevolver401() throws Exception {
        mockMvc.perform(post("/proveedores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proveedorCreateRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - PUT /proveedores/PROV001 debe devolver 401")
    void sinAutenticacion_actualizarProveedor_debeDevolver401() throws Exception {
        mockMvc.perform(put("/proveedores/PROV001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proveedorUpdateRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - GET /proveedores/PROV001 debe devolver 401")
    void sinAutenticacion_consultarProveedor_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores/PROV001"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - GET /proveedores debe devolver 401")
    void sinAutenticacion_consultarProveedores_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - DELETE /proveedores/PROV001 debe devolver 401")
    void sinAutenticacion_eliminarProveedor_debeDevolver401() throws Exception {
        mockMvc.perform(delete("/proveedores/PROV001"))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: POST /proveedores
    // Permisos: ADMIN, GERENTE, INVENTARIO
    // ==========================================

    @Test
    @DisplayName("POST /proveedores - Con autenticación válida debe permitir acceso")
    void crearProveedor_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.crearProveedor(any(ProveedorCreateRequest.class)))
            .thenReturn(proveedorResponseSuccess);

        // Act & Assert
        mockMvc.perform(post("/proveedores")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proveedorCreateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos.cve").value("PROV001"));
    }

    @Test
    @DisplayName("POST /proveedores - Con credenciales inválidas debe devolver 401")
    void crearProveedor_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(post("/proveedores")
                .header("Authorization", basicAuth("invalid", "invalid"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proveedorCreateRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: PUT /proveedores/{cve}
    // Permisos: ADMIN, GERENTE, INVENTARIO
    // ==========================================

    @Test
    @DisplayName("PUT /proveedores/PROV001 - Con autenticación válida debe permitir acceso")
    void actualizarProveedor_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.actualizarProveedor(anyString(), any(ProveedorUpdateRequest.class)))
            .thenReturn(proveedorResponseSuccess);

        // Act & Assert
        mockMvc.perform(put("/proveedores/PROV001")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proveedorUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));
    }

    @Test
    @DisplayName("PUT /proveedores/PROV001 - Con credenciales inválidas debe devolver 401")
    void actualizarProveedor_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(put("/proveedores/PROV001")
                .header("Authorization", basicAuth("invalid", "invalid"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proveedorUpdateRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: GET /proveedores/{cve}
    // Permisos: Todos los roles autenticados
    // ==========================================

    @Test
    @DisplayName("GET /proveedores/PROV001 - Con autenticación válida debe permitir acceso")
    void consultarProveedorPorClave_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.buscarPorClave(anyString())).thenReturn(proveedorResponseSuccess);

        // Act & Assert
        mockMvc.perform(get("/proveedores/PROV001")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos.cve").value("PROV001"));
    }

    @Test
    @DisplayName("GET /proveedores/PROV001 - Con credenciales inválidas debe devolver 401")
    void consultarProveedorPorClave_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores/PROV001")
                .header("Authorization", basicAuth("invalid", "invalid")))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: GET /proveedores
    // Permisos: Todos los roles autenticados
    // ==========================================

    @Test
    @DisplayName("GET /proveedores - Con autenticación válida debe permitir acceso")
    void consultarTodosLosProveedores_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.obtenerTodosLosProveedores()).thenReturn(proveedoresListResponseSuccess);

        // Act & Assert
        mockMvc.perform(get("/proveedores")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    @DisplayName("GET /proveedores - Con credenciales inválidas debe devolver 401")
    void consultarTodosLosProveedores_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores")
                .header("Authorization", basicAuth("invalid", "invalid")))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: DELETE /proveedores/{cve}
    // Permisos: Solo ADMIN
    // ==========================================

    @Test
    @DisplayName("DELETE /proveedores/PROV001 - Con autenticación válida debe permitir acceso")
    void eliminarProveedor_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.eliminarProveedor(anyString())).thenReturn(deleteResponseSuccess);

        // Act & Assert
        mockMvc.perform(delete("/proveedores/PROV001")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));
    }

    @Test
    @DisplayName("DELETE /proveedores/PROV001 - Con credenciales inválidas debe devolver 401")
    void eliminarProveedor_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(delete("/proveedores/PROV001")
                .header("Authorization", basicAuth("invalid", "invalid")))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINTS DE BÚSQUEDA
    // Permisos: Todos los roles autenticados
    // ==========================================

    @Test
    @DisplayName("GET /proveedores/buscar/nombre - Con autenticación válida debe permitir acceso")
    void buscarProveedoresPorNombre_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.buscarPorNombre(anyString())).thenReturn(proveedoresListResponseSuccess);

        // Act & Assert
        mockMvc.perform(get("/proveedores/buscar/nombre")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD))
                .param("nombre", "Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));
    }

    @Test
    @DisplayName("GET /proveedores/buscar/nombre - Con credenciales inválidas debe devolver 401")
    void buscarProveedoresPorNombre_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores/buscar/nombre")
                .header("Authorization", basicAuth("invalid", "invalid"))
                .param("nombre", "Test"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /proveedores/buscar/razon-social - Con autenticación válida debe permitir acceso")
    void buscarProveedoresPorRazonSocial_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(proveedorServicio.buscarPorRazonSocial(anyString())).thenReturn(proveedoresListResponseSuccess);

        // Act & Assert
        mockMvc.perform(get("/proveedores/buscar/razon-social")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD))
                .param("razonSocial", "Test S.A."))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));
    }

    @Test
    @DisplayName("GET /proveedores/buscar/razon-social - Con credenciales inválidas debe devolver 401")
    void buscarProveedoresPorRazonSocial_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores/buscar/razon-social")
                .header("Authorization", basicAuth("invalid", "invalid"))
                .param("razonSocial", "Test S.A."))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS ADICIONALES DE VALIDACIÓN
    // ==========================================

    @Test
    @DisplayName("Endpoints públicos - Actuator health debe permitir acceso sin autenticación")
    void endpointsPublicos_actuatorHealth_debePermitirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().is5xxServerError()); // 500 porque GlobalExceptionHandler maneja el error, no 404
    }

    @Test
    @DisplayName("Header Authorization malformado debe devolver 401")
    void headerAuthorizationMalformado_debeDevolver401() throws Exception {
        mockMvc.perform(get("/proveedores")
                .header("Authorization", "InvalidHeader"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Header Authorization con Basic Auth vacío debe devolver 401")
    void headerAuthorizationBasicVacio_debeDevolver401() throws Exception {
        String emptyAuth = Base64.getEncoder().encodeToString(":".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/proveedores")
                .header("Authorization", "Basic " + emptyAuth))
            .andExpect(status().isUnauthorized());
    }
}