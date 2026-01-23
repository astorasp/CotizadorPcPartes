package mx.com.qtx.cotizador.integration.pc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
import mx.com.qtx.cotizador.controlador.PcController;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pc.request.PcCreateRequest;
import mx.com.qtx.cotizador.dto.pc.request.PcUpdateRequest;
import mx.com.qtx.cotizador.dto.pc.request.AgregarComponenteRequest;
import mx.com.qtx.cotizador.dto.pc.response.PcResponse;
import mx.com.qtx.cotizador.servicio.componente.ComponenteServicio;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Test de permisos basados en roles para el controlador de PCs usando mocks.
 *
 * Este test se enfoca ÚNICAMENTE en validar que Spring Security funciona correctamente
 * con los roles definidos. No prueba la lógica de negocio del servicio.
 *
 * Matriz de Permisos para PCs:
 * - ADMIN: Full CRUD + Gestión de componentes + Vista de costos + Modificación de precios
 * - GERENTE: Create, Edit, Delete + Gestión de componentes + Vista de costos + Modificación de precios
 * - VENDEDOR: Read-only
 * - INVENTARIO: Full CRUD + Gestión de componentes + Vista de costos (NO Modificación de precios)
 * - CONSULTOR: Read-only
 */
@WebMvcTest(PcController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class PcRolePermissionsTest {

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
    private PcCreateRequest createRequest() {
        PcCreateRequest request = new PcCreateRequest();
        request.setId("TEST_PC01");
        request.setNombre("PC Gaming de prueba");
        request.setDescripcion("PC Gaming para tests de permisos");
        request.setPrecio(new BigDecimal("15000.00"));  // usar 'precio' no 'precioBase'
        request.setMarca("TestBrand");
        request.setModelo("Gaming-001");
        return request;
    }

    private PcResponse mockResponse() {
        PcResponse response = new PcResponse();
        response.setId("TEST_PC01");
        response.setNombre("PC Gaming de prueba");
        response.setDescripcion("PC Gaming para tests de permisos");
        return response;
    }

    private AgregarComponenteRequest agregarComponenteRequest() {
        AgregarComponenteRequest request = new AgregarComponenteRequest();
        request.setId("TEST_COMP01");  // usar 'id' no 'idComponente'
        request.setTipoComponente("MONITOR");
        request.setDescripcion("Monitor de prueba");
        request.setMarca("TestBrand");
        request.setModelo("Test-001");
        request.setCosto(new BigDecimal("1000.00"));
        request.setPrecioBase(new BigDecimal("1500.00"));
        return request;
    }

    // ==========================================
    // TESTS DE ACCESO SIN AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("Usuario sin autenticación no puede acceder a GET /pcs")
    void usuarioSinAutenticacionNoPuedeLeer() throws Exception {
        mockMvc.perform(get("/pcs"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede crear PC")
    void usuarioSinAutenticacionNoPuedeCrear() throws Exception {
        String requestJson = objectMapper.writeValueAsString(createRequest());

        mockMvc.perform(post("/pcs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede actualizar PC")
    void usuarioSinAutenticacionNoPuedeActualizar() throws Exception {
        PcUpdateRequest updateRequest = new PcUpdateRequest();
        updateRequest.setNombre("PC actualizado");
        updateRequest.setDescripcion("PC actualizado para permisos");
        updateRequest.setPrecio(new BigDecimal("16000.00"));  // usar 'precio' no 'precioBase'
        updateRequest.setMarca("TestBrand");
        updateRequest.setModelo("Gaming-002");
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/pcs/TEST_PC01")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede eliminar PC")
    void usuarioSinAutenticacionNoPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/pcs/TEST_PC01"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    @Test
    @DisplayName("Usuario sin autenticación no puede agregar componente a PC")
    void usuarioSinAutenticacionNoPuedeAgregarComponente() throws Exception {
        String requestJson = objectMapper.writeValueAsString(agregarComponenteRequest());

        mockMvc.perform(post("/pcs/TEST_PC01/componentes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(componenteServicio);
    }

    // ==========================================
    // TESTS DE ACCESO CON AUTENTICACIÓN
    // ==========================================

    @Test
    @DisplayName("Usuario autenticado puede leer PCs")
    void usuarioAutenticadoPuedeLeer() throws Exception {
        // Mock del servicio para devolver lista vacía exitosa
        when(componenteServicio.buscarPorTipo("PC"))
            .thenReturn(new ApiResponse<>("0", "OK", List.of()));

        mockMvc.perform(get("/pcs")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).buscarPorTipo("PC");
    }

    @Test
    @DisplayName("Usuario autenticado puede crear PC")
    void usuarioAutenticadoPuedeCrear() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.guardarPcCompleto(any(PcCreateRequest.class)))
            .thenReturn(new ApiResponse<>("0", "PC guardado exitosamente", mockResponse()));

        String requestJson = objectMapper.writeValueAsString(createRequest());

        mockMvc.perform(post("/pcs")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"))
            .andExpect(jsonPath("$.datos.id").value("TEST_PC01"));

        verify(componenteServicio).guardarPcCompleto(any(PcCreateRequest.class));
    }

    @Test
    @DisplayName("Usuario autenticado puede actualizar PC")
    void usuarioAutenticadoPuedeActualizar() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.actualizarPcCompleto(anyString(), any(PcUpdateRequest.class)))
            .thenReturn(new ApiResponse<>("0", "PC actualizado exitosamente", mockResponse()));

        PcUpdateRequest updateRequest = new PcUpdateRequest();
        updateRequest.setNombre("PC Gaming actualizado");
        updateRequest.setDescripcion("PC Gaming actualizado para permisos");
        updateRequest.setPrecio(new BigDecimal("16000.00"));  // usar 'precio' no 'precioBase'
        updateRequest.setMarca("TestBrand");
        updateRequest.setModelo("Gaming-002");
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/pcs/TEST_PC01")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).actualizarPcCompleto(anyString(), any(PcUpdateRequest.class));
    }

    @Test
    @DisplayName("Usuario autenticado puede eliminar PC")
    void usuarioAutenticadoPuedeEliminar() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.eliminarPcCompleta(anyString()))
            .thenReturn(new ApiResponse<>("0", "PC eliminado exitosamente"));

        mockMvc.perform(delete("/pcs/TEST_PC01")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).eliminarPcCompleta("TEST_PC01");
    }

    @Test
    @DisplayName("Usuario autenticado puede agregar componente a PC")
    void usuarioAutenticadoPuedeAgregarComponente() throws Exception {
        // Mock del servicio para devolver respuesta exitosa
        when(componenteServicio.agregarComponenteAPc(anyString(), any(AgregarComponenteRequest.class)))
            .thenReturn(new ApiResponse<>("0", "Componente agregado exitosamente", null));

        String requestJson = objectMapper.writeValueAsString(agregarComponenteRequest());

        mockMvc.perform(post("/pcs/TEST_PC01/componentes")
                .header("Authorization", basicAuth(ADMIN_USER, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("0"));

        verify(componenteServicio).agregarComponenteAPc(anyString(), any(AgregarComponenteRequest.class));
    }
}