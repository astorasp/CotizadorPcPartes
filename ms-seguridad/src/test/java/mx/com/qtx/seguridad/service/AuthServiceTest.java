package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.config.TestContainerConfig;
import mx.com.qtx.seguridad.dto.TokenResponse;
import mx.com.qtx.seguridad.entity.Acceso;
import mx.com.qtx.seguridad.repository.AccesoRepository;
import mx.com.qtx.seguridad.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para AuthService usando TestContainers
 * Prueba el comportamiento real del servicio con base de datos MySQL
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("AuthService Integration Tests")
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private AccesoRepository accesoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Crear un mock request para simular IP y User-Agent
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Test-Agent/1.0");
        this.mockRequest = request;
        
        // Limpiar sesiones existentes antes de cada test
        accesoRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe autenticar usuario válido y crear sesión exitosamente")
    @Transactional
    void shouldAuthenticateValidUserAndCreateSession() {
        // Given
        String username = "admin";
        String password = "admin123";

        // When
        TokenResponse response = authService.authenticate(username, password, mockRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getExpiresIn() > 0);

        // Verificar que se creó la sesión en la base de datos
        List<Acceso> sesiones = accesoRepository.findByUsuarioIdAndActivoTrue(1);
        assertEquals(1, sesiones.size());
        
        Acceso sesion = sesiones.get(0);
        assertTrue(sesion.isActivo());
        assertNotNull(sesion.getIdSesion());
        assertEquals(1, sesion.getUsuarioId());
        assertNotNull(sesion.getFechaInicio());
        assertNull(sesion.getFechaFin());
    }

    @Test
    @DisplayName("Debe rechazar usuario con credenciales inválidas")
    void shouldRejectInvalidCredentials() {
        // Given
        String username = "admin";
        String wrongPassword = "wrongpassword";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(username, wrongPassword, mockRequest));
        
        assertEquals("Credenciales inválidas", exception.getMessage());
        
        // Verificar que no se creó ninguna sesión
        List<Acceso> sesiones = accesoRepository.findAll();
        assertTrue(sesiones.isEmpty());
    }

    @Test
    @DisplayName("Debe rechazar usuario inexistente")
    void shouldRejectNonExistentUser() {
        // Given
        String nonExistentUser = "noexiste";
        String password = "password123";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(nonExistentUser, password, mockRequest));
        
        assertEquals("Credenciales inválidas", exception.getMessage());
        
        // Verificar que no se creó ninguna sesión
        List<Acceso> sesiones = accesoRepository.findAll();
        assertTrue(sesiones.isEmpty());
    }

    @Test
    @DisplayName("Debe rechazar usuario inactivo")
    void shouldRejectInactiveUser() {
        // Given
        String inactiveUser = "inactive";
        String password = "user123";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(inactiveUser, password, mockRequest));
        
        assertEquals("Credenciales inválidas", exception.getMessage());
        
        // Verificar que no se creó ninguna sesión
        List<Acceso> sesiones = accesoRepository.findAll();
        assertTrue(sesiones.isEmpty());
    }

    @Test
    @DisplayName("Debe rechazar usuario sin roles asignados")
    void shouldRejectUserWithoutRoles() {
        // Given
        String userWithoutRoles = "noroles";
        String password = "user123";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(userWithoutRoles, password, mockRequest));
        
        assertEquals("Usuario sin roles asignados", exception.getMessage());
        
        // Verificar que no se creó ninguna sesión
        List<Acceso> sesiones = accesoRepository.findAll();
        assertTrue(sesiones.isEmpty());
    }

    @Test
    @DisplayName("Debe rechazar autenticación si usuario ya tiene sesión activa")
    @Transactional
    void shouldRejectAuthenticationWhenUserHasActiveSession() {
        // Given
        String username = "admin";
        String password = "admin123";
        
        // Primero crear una sesión
        authService.authenticate(username, password, mockRequest);
        
        // Verificar que se creó la sesión
        List<Acceso> sesionesAntes = accesoRepository.findByUsuarioIdAndActivoTrue(1);
        assertEquals(1, sesionesAntes.size());

        // When & Then - Intentar autenticarse de nuevo
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(username, password, mockRequest));
        
        assertEquals("Ya existe una sesión activa para este usuario", exception.getMessage());
        
        // Verificar que sigue habiendo solo una sesión activa
        List<Acceso> sesionesDespues = accesoRepository.findByUsuarioIdAndActivoTrue(1);
        assertEquals(1, sesionesDespues.size());
    }

    @Test
    @DisplayName("Debe renovar token con refresh token válido")
    @Transactional
    void shouldRefreshTokenWithValidRefreshToken() {
        // Given
        String username = "admin";
        String password = "admin123";
        
        // Autenticarse primero
        TokenResponse initialResponse = authService.authenticate(username, password, mockRequest);
        String refreshToken = initialResponse.getRefreshToken();

        // When
        TokenResponse refreshedResponse = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(refreshedResponse);
        assertNotNull(refreshedResponse.getAccessToken());
        assertEquals(refreshToken, refreshedResponse.getRefreshToken()); // El refresh token se reutiliza
        assertEquals("Bearer", refreshedResponse.getTokenType());
        assertTrue(refreshedResponse.getExpiresIn() > 0);
        
        // El nuevo access token debe ser diferente
        assertNotEquals(initialResponse.getAccessToken(), refreshedResponse.getAccessToken());
    }

    @Test
    @DisplayName("Debe realizar logout correctamente y cerrar sesión")
    @Transactional
    void shouldPerformLogoutAndCloseSession() {
        // Given
        String username = "admin";
        String password = "admin123";
        
        // Autenticarse primero
        TokenResponse response = authService.authenticate(username, password, mockRequest);
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();
        
        // Verificar que hay una sesión activa
        List<Acceso> sesionesAntes = accesoRepository.findByUsuarioIdAndActivoTrue(1);
        assertEquals(1, sesionesAntes.size());

        // When
        Map<String, Object> logoutResult = authService.logout(accessToken, refreshToken);

        // Then
        assertNotNull(logoutResult);
        assertTrue((Boolean) logoutResult.get("success"));
        assertEquals("Logout exitoso", logoutResult.get("message"));
        
        // Verificar que la sesión se cerró en la base de datos
        List<Acceso> sesionesDespues = accesoRepository.findByUsuarioIdAndActivoTrue(1);
        assertTrue(sesionesDespues.isEmpty());
        
        // Verificar que la sesión existe pero está inactiva
        List<Acceso> todasLasSesiones = accesoRepository.findByUsuarioId(1);
        assertEquals(1, todasLasSesiones.size());
        assertFalse(todasLasSesiones.get(0).isActivo());
        assertNotNull(todasLasSesiones.get(0).getFechaFin());
    }

    @Test
    @DisplayName("Debe validar token correctamente")
    @Transactional
    void shouldValidateTokenCorrectly() {
        // Given
        String username = "admin";
        String password = "admin123";
        
        // Autenticarse
        TokenResponse response = authService.authenticate(username, password, mockRequest);
        String validToken = response.getAccessToken();

        // When
        boolean isValid = authService.isTokenValid(validToken);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Debe obtener información de usuario desde token")
    @Transactional
    void shouldGetUserInfoFromToken() {
        // Given
        String username = "admin";
        String password = "admin123";
        
        // Autenticarse
        TokenResponse response = authService.authenticate(username, password, mockRequest);
        String token = response.getAccessToken();

        // When
        Map<String, Object> userInfo = authService.getUserInfoFromToken(token);

        // Then
        assertNotNull(userInfo);
        assertEquals(1, userInfo.get("id"));
        assertEquals("admin", userInfo.get("username"));
        assertEquals(true, userInfo.get("active"));
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) userInfo.get("roles");
        assertNotNull(roles);
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    @DisplayName("Debe obtener estadísticas de autenticación")
    void shouldGetAuthenticationStats() {
        // When
        Map<String, Object> stats = authService.getAuthStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("serviceStatus"));
        assertEquals("active", stats.get("serviceStatus"));
    }
}