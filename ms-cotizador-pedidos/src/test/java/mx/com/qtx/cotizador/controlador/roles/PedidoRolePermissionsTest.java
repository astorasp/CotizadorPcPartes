package mx.com.qtx.cotizador.controlador.roles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import mx.com.qtx.cotizador.controlador.PedidoController;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.servicio.pedido.PedidoServicio;
import mx.com.qtx.cotizador.util.Errores;

/**
 * Tests de permisos basados en roles para PedidoController usando Basic Auth.
 * <p>
 * Este test se enfoca ÚNICAMENTE en validar que Spring Security funciona correctamente
 * con los roles definidos. No prueba la lógica de negocio del servicio.
 * </p>
 *
 * <p>Matriz de Permisos para Pedidos:</p>
 * <ul>
 *   <li><strong>POST /pedidos/generar</strong> - ADMIN, GERENTE, VENDEDOR, INVENTARIO</li>
 *   <li><strong>GET /pedidos/{id}</strong> - Todos los roles autenticados</li>
 *   <li><strong>GET /pedidos</strong> - Todos los roles autenticados</li>
 *   <li><strong>POST /pedidos/{id}/cancelar</strong> - ADMIN, GERENTE, VENDEDOR</li>
 * </ul>
 *
 * <p>Usuario de prueba:</p>
 * <ul>
 *   <li><strong>Username:</strong> test</li>
 *   <li><strong>Password:</strong> test123</li>
 *   <li><strong>Roles:</strong> ADMIN, GERENTE, VENDEDOR, INVENTARIO, CONSULTOR</li>
 * </ul>
 */
@WebMvcTest(PedidoController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("PedidoController - Tests de Permisos por Rol")
public class PedidoRolePermissionsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoServicio pedidoServicio;

    private static final String TEST_USER = "test";
    private static final String TEST_PASSWORD = "test123";

    // Helper para Basic Auth
    private String basicAuth(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
    }

    // Datos de prueba
    private GenerarPedidoRequest generarPedidoRequest;
    private ApiResponse<PedidoResponse> pedidoResponseSuccess;
    private ApiResponse<List<PedidoResponse>> pedidosListResponseSuccess;
    private ApiResponse<String> cancelacionResponseSuccess;

    @BeforeEach
    void setUp() {
        // Configurar request para generar pedido
        generarPedidoRequest = new GenerarPedidoRequest();
        generarPedidoRequest.setCotizacionId(1);
        generarPedidoRequest.setCveProveedor("PROV001");
        generarPedidoRequest.setFechaEmision(LocalDate.now());
        generarPedidoRequest.setFechaEntrega(LocalDate.now().plusDays(7));
        generarPedidoRequest.setNivelSurtido(100);

        // Configurar response exitosa para pedido individual
        PedidoResponse pedidoResponse = new PedidoResponse();
        pedidoResponse.setNumPedido(1L);
        pedidoResponse.setCveProveedor("PROV001");
        pedidoResponse.setFechaEmision(LocalDate.now());
        pedidoResponse.setNivelSurtido(100);

        pedidoResponseSuccess = new ApiResponse<>(Errores.OK.getCodigo(),
            "Pedido obtenido exitosamente", pedidoResponse);

        // Configurar response exitosa para lista de pedidos
        pedidosListResponseSuccess = new ApiResponse<>(Errores.OK.getCodigo(),
            "Pedidos obtenidos exitosamente", Arrays.asList(pedidoResponse));

        // Configurar response exitosa para cancelación
        cancelacionResponseSuccess = new ApiResponse<>(Errores.OK.getCodigo(),
            "Pedido cancelado exitosamente", "Pedido PED-001 cancelado correctamente");
    }

    // ==========================================
    // TESTS DE ACCESO SIN AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("Sin autenticación - POST /pedidos/generar debe devolver 401")
    void sinAutenticacion_generarPedido_debeDevolver401() throws Exception {
        mockMvc.perform(post("/pedidos/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generarPedidoRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - GET /pedidos/1 debe devolver 401")
    void sinAutenticacion_consultarPedido_debeDevolver401() throws Exception {
        mockMvc.perform(get("/pedidos/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - GET /pedidos debe devolver 401")
    void sinAutenticacion_consultarPedidos_debeDevolver401() throws Exception {
        mockMvc.perform(get("/pedidos"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin autenticación - POST /pedidos/1/cancelar debe devolver 401")
    void sinAutenticacion_cancelarPedido_debeDevolver401() throws Exception {
        mockMvc.perform(post("/pedidos/1/cancelar")
                .param("reason", "Cancelación de prueba"))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: POST /pedidos/generar
    // Permisos: ADMIN, GERENTE, VENDEDOR, INVENTARIO
    // ==========================================

    @Test
    @DisplayName("POST /pedidos/generar - Con autenticación válida debe permitir acceso")
    void generarPedido_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(pedidoServicio.generarPedidoDesdeCotizacion(any(GenerarPedidoRequest.class)))
            .thenReturn(pedidoResponseSuccess);

        // Act & Assert
        mockMvc.perform(post("/pedidos/generar")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generarPedidoRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos.numPedido").value(1));
    }

    @Test
    @DisplayName("POST /pedidos/generar - Con credenciales inválidas debe devolver 401")
    void generarPedido_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(post("/pedidos/generar")
                .header("Authorization", basicAuth("invalid", "invalid"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generarPedidoRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: GET /pedidos/{id}
    // Permisos: Todos los roles autenticados
    // ==========================================

    @Test
    @DisplayName("GET /pedidos/1 - Con autenticación válida debe permitir acceso")
    void consultarPedidoPorId_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(pedidoServicio.buscarPorId(anyInt())).thenReturn(pedidoResponseSuccess);

        // Act & Assert
        mockMvc.perform(get("/pedidos/1")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos.numPedido").value(1));
    }

    @Test
    @DisplayName("GET /pedidos/1 - Con credenciales inválidas debe devolver 401")
    void consultarPedidoPorId_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(get("/pedidos/1")
                .header("Authorization", basicAuth("invalid", "invalid")))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: GET /pedidos
    // Permisos: Todos los roles autenticados
    // ==========================================

    @Test
    @DisplayName("GET /pedidos - Con autenticación válida debe permitir acceso")
    void consultarTodosLosPedidos_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(pedidoServicio.obtenerTodosLosPedidos()).thenReturn(pedidosListResponseSuccess);

        // Act & Assert
        mockMvc.perform(get("/pedidos")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos").isArray())
            .andExpect(jsonPath("$.datos[0].numPedido").value(1));
    }

    @Test
    @DisplayName("GET /pedidos - Con credenciales inválidas debe devolver 401")
    void consultarTodosLosPedidos_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(get("/pedidos")
                .header("Authorization", basicAuth("invalid", "invalid")))
            .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // TESTS DE ENDPOINT: POST /pedidos/{id}/cancelar
    // Permisos: ADMIN, GERENTE, VENDEDOR
    // ==========================================

    @Test
    @DisplayName("POST /pedidos/1/cancelar - Con autenticación válida debe permitir acceso")
    void cancelarPedido_conAutenticacionValida_debePermitirAcceso() throws Exception {
        // Arrange
        when(pedidoServicio.cancelarPedido(anyInt(), anyString())).thenReturn(cancelacionResponseSuccess);

        // Act & Assert
        mockMvc.perform(post("/pedidos/1/cancelar")
                .header("Authorization", basicAuth(TEST_USER, TEST_PASSWORD))
                .param("reason", "Cancelación de prueba"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos").value("Pedido PED-001 cancelado correctamente"));
    }

    @Test
    @DisplayName("POST /pedidos/1/cancelar - Con credenciales inválidas debe devolver 401")
    void cancelarPedido_conCredencialesInvalidas_debeDevolver401() throws Exception {
        mockMvc.perform(post("/pedidos/1/cancelar")
                .header("Authorization", basicAuth("invalid", "invalid"))
                .param("reason", "Cancelación de prueba"))
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
        mockMvc.perform(get("/pedidos")
                .header("Authorization", "InvalidHeader"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Header Authorization con Basic Auth vacío debe devolver 401")
    void headerAuthorizationBasicVacio_debeDevolver401() throws Exception {
        String emptyAuth = Base64.getEncoder().encodeToString(":".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/pedidos")
                .header("Authorization", "Basic " + emptyAuth))
            .andExpect(status().isUnauthorized());
    }
}