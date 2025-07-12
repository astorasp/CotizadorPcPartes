package mx.com.qtx.seguridad.integration;

import mx.com.qtx.seguridad.config.TestContainerConfig;
import mx.com.qtx.seguridad.dto.TokenResponse;
import mx.com.qtx.seguridad.entity.Acceso;
import mx.com.qtx.seguridad.repository.AccesoRepository;
import mx.com.qtx.seguridad.service.AuthService;
import mx.com.qtx.seguridad.service.JwtService;
import mx.com.qtx.seguridad.service.SessionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para el sistema de gestión de sesiones vinculadas a tokens
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Session Management Integration Tests")
class SessionManagementIntegrationTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AccesoRepository accesoRepository;
    
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "user123";
    
    private HttpServletRequest mockRequest;
    
    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Test-Agent/1.0");
        this.mockRequest = request;
        
        // Limpiar todas las sesiones activas antes de cada test
        accesoRepository.deleteAll();
    }
    
    @Test
    @Order(1)
    @DisplayName("Should create new session when refreshing token")
    void shouldCreateNewSessionWhenRefreshingToken() {
        // Given - Login inicial
        TokenResponse initialLogin = authService.authenticate(TEST_USER, TEST_PASSWORD, mockRequest);
        assertNotNull(initialLogin);
        
        String initialSessionId = jwtService.extractSessionId(initialLogin.getAccessToken());
        assertNotNull(initialSessionId);
        
        // Verificar que la sesión inicial existe y está activa
        assertTrue(sessionService.isSessionActive(initialSessionId));
        
        // When - Renovar token
        TokenResponse refreshedTokens = authService.refreshToken(initialLogin.getRefreshToken());
        assertNotNull(refreshedTokens);
        
        // Then - Verificar nueva sesión creada
        String newSessionId = jwtService.extractSessionId(refreshedTokens.getAccessToken());
        assertNotNull(newSessionId);
        assertNotEquals(initialSessionId, newSessionId, "Should have different session ID");
        
        // Verificar que la sesión anterior fue cerrada
        assertFalse(sessionService.isSessionActive(initialSessionId), 
            "Initial session should be inactive after refresh");
        
        // Verificar que la nueva sesión está activa
        assertTrue(sessionService.isSessionActive(newSessionId), 
            "New session should be active after refresh");
        
        // Verificar que el refresh token también tiene la nueva sesión
        String refreshTokenSessionId = jwtService.extractSessionId(refreshedTokens.getRefreshToken());
        assertEquals(newSessionId, refreshTokenSessionId, 
            "Both tokens should have the same session ID");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should close all user sessions on logout")
    void shouldCloseAllUserSessionsOnLogout() {
        // Given - Login inicial
        TokenResponse loginResponse = authService.authenticate(TEST_USER, TEST_PASSWORD, mockRequest);
        assertNotNull(loginResponse);
        
        String sessionId = jwtService.extractSessionId(loginResponse.getAccessToken());
        assertNotNull(sessionId);
        
        // Verificar sesión activa
        assertTrue(sessionService.isSessionActive(sessionId));
        
        // When - Logout
        Map<String, Object> logoutResult = authService.logout(
            loginResponse.getAccessToken(), 
            loginResponse.getRefreshToken()
        );
        
        // Then - Verificar logout exitoso
        assertTrue((Boolean) logoutResult.get("success"));
        assertTrue((Boolean) logoutResult.get("sessionClosed"));
        
        // Verificar que la sesión está inactiva
        assertFalse(sessionService.isSessionActive(sessionId), 
            "Session should be inactive after logout");
        
        // Verificar que no quedan sesiones activas para el usuario
        Integer userId = jwtService.extractUserId(loginResponse.getAccessToken());
        assertFalse(sessionService.hasActiveSession(userId), 
            "User should have no active sessions after logout");
    }
    
    @Test
    @Order(3)
    @DisplayName("Should handle multiple refresh tokens correctly")
    void shouldHandleMultipleRefreshTokensCorrectly() {
        // Given - Login inicial
        TokenResponse initialLogin = authService.authenticate(TEST_USER, TEST_PASSWORD, mockRequest);
        String session1 = jwtService.extractSessionId(initialLogin.getAccessToken());
        
        // When - Múltiples renovaciones
        TokenResponse refresh1 = authService.refreshToken(initialLogin.getRefreshToken());
        String session2 = jwtService.extractSessionId(refresh1.getAccessToken());
        
        TokenResponse refresh2 = authService.refreshToken(refresh1.getRefreshToken());
        String session3 = jwtService.extractSessionId(refresh2.getAccessToken());
        
        // Then - Verificar cadena de sesiones
        assertNotEquals(session1, session2);
        assertNotEquals(session2, session3);
        assertNotEquals(session1, session3);
        
        // Solo la última sesión debe estar activa
        assertFalse(sessionService.isSessionActive(session1));
        assertFalse(sessionService.isSessionActive(session2));
        assertTrue(sessionService.isSessionActive(session3));
        
        // Verificar historial de sesiones
        Integer userId = jwtService.extractUserId(refresh2.getAccessToken());
        List<Acceso> sessionHistory = sessionService.getUserSessionHistory(userId);
        assertTrue(sessionHistory.size() >= 3, "Should have at least 3 sessions in history");
    }
    
    @Test
    @Order(4)
    @DisplayName("Should fail refresh with inactive session")
    void shouldFailRefreshWithInactiveSession() {
        // Given - Login y cerrar sesión
        TokenResponse loginResponse = authService.authenticate(TEST_USER, TEST_PASSWORD, mockRequest);
        String sessionId = jwtService.extractSessionId(loginResponse.getAccessToken());
        
        // Cerrar sesión manualmente
        sessionService.closeSession(sessionId);
        
        // When/Then - Intentar renovar con sesión inactiva
        assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(loginResponse.getRefreshToken());
        }, "Should fail to refresh with inactive session");
    }
    
    @Test
    @Order(5)
    @DisplayName("Should track session lifecycle correctly")
    @Transactional
    void shouldTrackSessionLifecycleCorrectly() {
        // Given - Login
        TokenResponse loginResponse = authService.authenticate(TEST_USER, TEST_PASSWORD, mockRequest);
        String sessionId = jwtService.extractSessionId(loginResponse.getAccessToken());
        
        // Obtener información de la sesión
        Acceso session = sessionService.getSessionInfo(sessionId).orElse(null);
        assertNotNull(session);
        
        // Verificar estado inicial
        assertTrue(session.isActivo());
        assertNotNull(session.getFechaInicio());
        assertNull(session.getFechaFin());
        
        // When - Logout
        authService.logout(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        
        // Then - Verificar estado final
        Acceso closedSession = sessionService.getSessionInfo(sessionId).orElse(null);
        assertNotNull(closedSession);
        assertFalse(closedSession.isActivo());
        assertNotNull(closedSession.getFechaFin());
        assertTrue(closedSession.getFechaFin().isAfter(closedSession.getFechaInicio()));
    }
    
    @Test
    @Order(6)
    @DisplayName("Should maintain session count correctly")
    void shouldMaintainSessionCountCorrectly() {
        // Given - Obtener conteo inicial
        Integer userId = 2; // testuser ID
        long initialCount = sessionService.countActiveUserSessions(userId);
        
        // When - Login
        TokenResponse loginResponse = authService.authenticate(TEST_USER, TEST_PASSWORD, mockRequest);
        
        // Then - Verificar incremento
        assertEquals(initialCount + 1, sessionService.countActiveUserSessions(userId));
        
        // When - Refresh (debería mantener el conteo)
        TokenResponse refreshResponse = authService.refreshToken(loginResponse.getRefreshToken());
        
        // Then - El conteo debe mantenerse igual (se cierra una y se abre otra)
        assertEquals(initialCount + 1, sessionService.countActiveUserSessions(userId));
        
        // When - Logout
        authService.logout(refreshResponse.getAccessToken(), refreshResponse.getRefreshToken());
        
        // Then - Volver al conteo inicial
        assertEquals(initialCount, sessionService.countActiveUserSessions(userId));
    }
}