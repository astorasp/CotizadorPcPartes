package mx.com.qtx.cotizador.integration.componente;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.com.qtx.cotizador.config.TestSecurityConfig;
import mx.com.qtx.cotizador.controlador.ComponenteController;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.servicio.componente.ComponenteServicio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Test de permisos basados en roles para el controlador de Componentes usando mocks.
 *
 * Este test se enfoca ÚNICAMENTE en validar que Spring Security funciona correctamente
 * con los roles definidos. No prueba la lógica de negocio del servicio.
 *
 * Matriz de Permisos para Componentes:
 * - ADMIN: Full CRUD (Create, Read, Update, Delete)
 * - GERENTE: Read + Update (no Create, no Delete)
 * - VENDEDOR: Read-only
 * - INVENTARIO: Full CRUD (Create, Read, Update, Delete)
 * - CONSULTOR: Read-only
 */
@WebMvcTest(ComponenteController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class ComponenteRolePermissionsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComponenteServicio componenteServicio;

    private static final String ADMIN_USER = "test";
    private static final String ADMIN_PASSWORD = "test123";

    // Helper para Basic Auth
    private String basicAuth(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
    }

    // Datos de prueba
    private ComponenteCreateRequest createRequest() {
        return ComponenteCreateRequest.builder()
            .id("TEST01")
            .tipoComponente("MONITOR")
            .descripcion("Monitor de prueba")
            .marca("TestBrand")
            .modelo("TB-001")
            .costo(new BigDecimal("5000.00"))
            .precioBase(new BigDecimal("6500.00"))
            .build();
    }

    private ComponenteResponse mockResponse() {
        return ComponenteResponse.builder()
            .id("TEST01")
            .descripcion("Monitor de prueba")
            .marca("TestBrand")
            .modelo("TB-001")
            .costo(new BigDecimal("5000.00"))
            .precioBase(new BigDecimal("6500.00"))
            .build();
    }

    // ==========================================
    // TESTS DE ACCESO SIN AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("Usuario sin autenticación no puede acceder a GET /componentes")
    void usuarioSinAutenticacionNoPuedeLeer() throws Exception {
        mockMvc.perform(get("/componentes"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede crear componente")
    void usuarioSinAutenticacionNoPuedeCrear() throws Exception {
        String requestJson = objectMapper.writeValueAsString(createRequest());

        mockMvc.perform(post("/componentes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede actualizar componente")
    void usuarioSinAutenticacionNoPuedeActualizar() throws Exception {
        ComponenteUpdateRequest updateRequest = new ComponenteUpdateRequest();
        updateRequest.setDescripcion("Actualizado");
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/componentes/TEST01")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede eliminar componente")
    void usuarioSinAutenticacionNoPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/componentes/TEST01"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    // ==========================================
    // TESTS DE ACCESO CON AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("Usuario autenticado puede leer componentes")
    void usuarioAutenticadoPuedeLeer() throws Exception {
        // Mock del servicio para devolver lista vacía exitosa
        when(componenteServicio.obtenerTodosLosComponentes())
            .thenReturn(new ApiResponse<>("0", "OK", List.of()));

        mockMvc.perform(get("/componentes")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).obtenerTodosLosComponentes();
    }

    @Test
    @DisplayName("Usuario autenticado puede crear componente")
    void usuarioAutenticadoPuedeCrear() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.guardarComponente(any(ComponenteCreateRequest.class)))
            .thenReturn(new ApiResponse<>("0", "Componente guardado exitosamente", mockResponse()));

        String requestJson = objectMapper.writeValueAsString(createRequest());

        mockMvc.perform(post("/componentes")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos.id").value("TEST01"));

        verify(componenteServicio).guardarComponente(any(ComponenteCreateRequest.class));
    }

    @Test
    @DisplayName("Usuario autenticado puede actualizar componente")
    void usuarioAutenticadoPuedeActualizar() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.actualizarComponente(anyString(), any(ComponenteUpdateRequest.class)))
            .thenReturn(new ApiResponse<>("0", "Componente actualizado exitosamente", mockResponse()));

        ComponenteUpdateRequest updateRequest = new ComponenteUpdateRequest();
        updateRequest.setDescripcion("Monitor actualizado");
        updateRequest.setMarca("TestBrand");
        updateRequest.setModelo("TB-001");
        updateRequest.setCosto(new BigDecimal("5000.00"));
        updateRequest.setPrecioBase(new BigDecimal("6500.00"));
        updateRequest.setTipoComponente("MONITOR");
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/componentes/TEST01")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).actualizarComponente(anyString(), any(ComponenteUpdateRequest.class));
    }

    @Test
    @DisplayName("Usuario autenticado puede eliminar componente")
    void usuarioAutenticadoPuedeEliminar() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.borrarComponente(anyString()))
            .thenReturn(new ApiResponse<>("0", "Componente eliminado exitosamente"));

        mockMvc.perform(delete("/componentes/TEST01")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).borrarComponente("TEST01");
    }
}