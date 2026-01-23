package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.entity.Acceso;
import mx.com.qtx.seguridad.repository.AccesoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para SessionService
 * Verifica gestión de sesiones de usuario con dependencias mockeadas
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SessionService Tests")
class SessionServiceTest {

    @Mock
    private AccesoRepository accesoRepository;

    private SessionService sessionService;

    // Datos de prueba
    private Acceso sesionActiva;
    private Acceso sesionInactiva;
    private Acceso sesionExpirada;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(accesoRepository);
        setupTestData();
    }

    private void setupTestData() {
        // Sesión activa de prueba
        sesionActiva = new Acceso();
        sesionActiva.setIdSesion("session-123");
        sesionActiva.setUsuarioId(1);
        sesionActiva.setActivo(true);
        sesionActiva.setFechaInicio(LocalDateTime.now().minusHours(1));
        sesionActiva.setFechaFin(null);

        // Sesión inactiva de prueba
        sesionInactiva = new Acceso();
        sesionInactiva.setIdSesion("session-456");
        sesionInactiva.setUsuarioId(2);
        sesionInactiva.setActivo(false);
        sesionInactiva.setFechaInicio(LocalDateTime.now().minusHours(2));
        sesionInactiva.setFechaFin(LocalDateTime.now().minusMinutes(30));

        // Sesión expirada de prueba
        sesionExpirada = new Acceso();
        sesionExpirada.setIdSesion("session-789");
        sesionExpirada.setUsuarioId(3);
        sesionExpirada.setActivo(true); // Marcada como activa pero expirada
        sesionExpirada.setFechaInicio(LocalDateTime.now().minusDays(1));
        sesionExpirada.setFechaFin(null);
    }

    // ===============================
    // TESTS PARA CREACIÓN DE SESIONES
    // ===============================

    @Test
    @DisplayName("createSession() - Debe crear nueva sesión exitosamente con todos los parámetros")
    void shouldCreateSessionSuccessfully() {
        // Given
        Integer usuarioId = 1;
        String ipAddress = "192.168.1.100";
        String userAgent = "Mozilla/5.0";

        when(accesoRepository.save(any(Acceso.class))).thenAnswer(invocation -> {
            Acceso session = invocation.getArgument(0);
            session.setIdSesion("new-session-id");
            return session;
        });

        // When
        String sessionId = sessionService.createSession(usuarioId, ipAddress, userAgent);

        // Then
        assertNotNull(sessionId);
        assertEquals("new-session-id", sessionId);
        verify(accesoRepository, times(1)).save(argThat(session ->
            session.getUsuarioId().equals(usuarioId) &&
            session.isActivo() &&
            session.getFechaInicio() != null &&
            session.getFechaFin() == null &&
            session.getIdSesion() != null
        ));
    }

    @Test
    @DisplayName("createSession() - Debe crear sesión con parámetros opcionales null")
    void shouldCreateSessionWithNullOptionalParams() {
        // Given
        Integer usuarioId = 1;
        String ipAddress = null;
        String userAgent = null;

        when(accesoRepository.save(any(Acceso.class))).thenAnswer(invocation -> {
            Acceso session = invocation.getArgument(0);
            session.setIdSesion("session-with-nulls");
            return session;
        });

        // When
        String sessionId = sessionService.createSession(usuarioId, ipAddress, userAgent);

        // Then
        assertNotNull(sessionId);
        assertEquals("session-with-nulls", sessionId);
        verify(accesoRepository, times(1)).save(any(Acceso.class));
    }

    @Test
    @DisplayName("createSession() - Debe lanzar excepción con usuarioId null")
    void shouldThrowExceptionWithNullUserId() {
        // Given
        Integer usuarioId = null;
        String ipAddress = "192.168.1.100";
        String userAgent = "Mozilla/5.0";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.createSession(usuarioId, ipAddress, userAgent));

        assertEquals("Usuario ID es requerido y debe ser mayor a 0", exception.getMessage());
        verify(accesoRepository, never()).save(any(Acceso.class));
    }

    @Test
    @DisplayName("createSession() - Debe lanzar excepción con usuarioId inválido")
    void shouldThrowExceptionWithInvalidUserId() {
        // Given
        Integer usuarioId = 0;
        String ipAddress = "192.168.1.100";
        String userAgent = "Mozilla/5.0";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.createSession(usuarioId, ipAddress, userAgent));

        assertEquals("Usuario ID es requerido y debe ser mayor a 0", exception.getMessage());
        verify(accesoRepository, never()).save(any(Acceso.class));
    }

    @Test
    @DisplayName("createSession() - Debe manejar error de base de datos")
    void shouldHandleDatabaseErrorOnCreate() {
        // Given
        Integer usuarioId = 1;
        when(accesoRepository.save(any(Acceso.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> sessionService.createSession(usuarioId, "192.168.1.100", "Mozilla/5.0"));

        assertTrue(exception.getMessage().contains("Error al crear sesión"));
        assertTrue(exception.getCause().getMessage().contains("Database error"));
    }

    // ===============================
    // TESTS PARA CIERRE DE SESIONES
    // ===============================

    @Test
    @DisplayName("closeSession() - Debe cerrar sesión activa exitosamente")
    void shouldCloseActiveSessionSuccessfully() {
        // Given
        String sessionId = "session-123";
        Acceso activeMockSession = spy(sesionActiva);

        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.of(activeMockSession));
        when(accesoRepository.save(activeMockSession)).thenReturn(activeMockSession);

        // When
        boolean result = sessionService.closeSession(sessionId);

        // Then
        assertTrue(result);
        verify(activeMockSession, times(1)).cerrarSesion();
        verify(accesoRepository, times(1)).save(activeMockSession);
    }

    @Test
    @DisplayName("closeSession() - Debe retornar false para sesión inexistente")
    void shouldReturnFalseForNonExistentSession() {
        // Given
        String sessionId = "non-existent-session";
        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.empty());

        // When
        boolean result = sessionService.closeSession(sessionId);

        // Then
        assertFalse(result);
        verify(accesoRepository, never()).save(any(Acceso.class));
    }

    @Test
    @DisplayName("closeSession() - Debe retornar true para sesión ya cerrada")
    void shouldReturnTrueForAlreadyClosedSession() {
        // Given
        String sessionId = "session-456";
        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.of(sesionInactiva));

        // When
        boolean result = sessionService.closeSession(sessionId);

        // Then
        assertTrue(result);
        verify(accesoRepository, never()).save(any(Acceso.class));
    }

    @Test
    @DisplayName("closeSession() - Debe lanzar excepción con sessionId null")
    void shouldThrowExceptionWithNullSessionId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.closeSession(null));

        assertEquals("ID de sesión es requerido", exception.getMessage());
        verify(accesoRepository, never()).findByIdSesion(any());
    }

    @Test
    @DisplayName("closeSession() - Debe lanzar excepción con sessionId vacío")
    void shouldThrowExceptionWithEmptySessionId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.closeSession("   "));

        assertEquals("ID de sesión es requerido", exception.getMessage());
        verify(accesoRepository, never()).findByIdSesion(any());
    }

    @Test
    @DisplayName("closeAllUserSessions() - Debe cerrar múltiples sesiones del usuario")
    void shouldCloseAllUserSessions() {
        // Given
        Integer usuarioId = 1;
        List<Acceso> sessionesActivas = Arrays.asList(sesionActiva, sesionExpirada);

        when(accesoRepository.findByUsuarioIdAndActivoTrue(usuarioId)).thenReturn(sessionesActivas);

        // When
        int sessionsClosed = sessionService.closeAllUserSessions(usuarioId);

        // Then
        assertEquals(2, sessionsClosed);
        verify(accesoRepository, times(1)).closeAllActiveUserSessions(usuarioId);
    }

    @Test
    @DisplayName("closeAllUserSessions() - Debe retornar 0 cuando no hay sesiones activas")
    void shouldReturnZeroWhenNoActiveSessions() {
        // Given
        Integer usuarioId = 1;
        when(accesoRepository.findByUsuarioIdAndActivoTrue(usuarioId)).thenReturn(Collections.emptyList());

        // When
        int sessionsClosed = sessionService.closeAllUserSessions(usuarioId);

        // Then
        assertEquals(0, sessionsClosed);
        verify(accesoRepository, never()).closeAllActiveUserSessions(any());
    }

    @Test
    @DisplayName("closeAllUserSessions() - Debe lanzar excepción con usuarioId inválido")
    void shouldThrowExceptionWithInvalidUserIdOnCloseAll() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.closeAllUserSessions(-1));

        assertEquals("Usuario ID es requerido y debe ser mayor a 0", exception.getMessage());
        verify(accesoRepository, never()).findByUsuarioIdAndActivoTrue(any());
    }

    // ===============================
    // TESTS PARA VALIDACIÓN DE SESIONES
    // ===============================

    @Test
    @DisplayName("isSessionActive() - Debe retornar true para sesión activa")
    void shouldReturnTrueForActiveSession() {
        // Given
        String sessionId = "session-123";
        Acceso mockSession = mock(Acceso.class);
        when(mockSession.isSesionVigente()).thenReturn(true);
        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.of(mockSession));

        // When
        boolean isActive = sessionService.isSessionActive(sessionId);

        // Then
        assertTrue(isActive);
        verify(mockSession, times(1)).isSesionVigente();
    }

    @Test
    @DisplayName("isSessionActive() - Debe retornar false para sesión inexistente")
    void shouldReturnFalseForNonExistentSessionOnValidation() {
        // Given
        String sessionId = "non-existent";
        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.empty());

        // When
        boolean isActive = sessionService.isSessionActive(sessionId);

        // Then
        assertFalse(isActive);
    }

    @Test
    @DisplayName("isSessionActive() - Debe retornar false para sessionId null")
    void shouldReturnFalseForNullSessionIdOnValidation() {
        // When
        boolean isActive = sessionService.isSessionActive(null);

        // Then
        assertFalse(isActive);
        verify(accesoRepository, never()).findByIdSesion(any());
    }

    @Test
    @DisplayName("hasActiveSession() - Debe retornar true cuando usuario tiene sesiones activas")
    void shouldReturnTrueWhenUserHasActiveSessions() {
        // Given
        Integer usuarioId = 1;
        when(accesoRepository.existsByUsuarioIdAndActivoTrue(usuarioId)).thenReturn(true);

        // When
        boolean hasActive = sessionService.hasActiveSession(usuarioId);

        // Then
        assertTrue(hasActive);
        verify(accesoRepository, times(1)).existsByUsuarioIdAndActivoTrue(usuarioId);
    }

    @Test
    @DisplayName("hasActiveSession() - Debe retornar false cuando usuario no tiene sesiones activas")
    void shouldReturnFalseWhenUserHasNoActiveSessions() {
        // Given
        Integer usuarioId = 1;
        when(accesoRepository.existsByUsuarioIdAndActivoTrue(usuarioId)).thenReturn(false);

        // When
        boolean hasActive = sessionService.hasActiveSession(usuarioId);

        // Then
        assertFalse(hasActive);
        verify(accesoRepository, times(1)).existsByUsuarioIdAndActivoTrue(usuarioId);
    }

    @Test
    @DisplayName("hasActiveSession() - Debe retornar false para usuarioId inválido")
    void shouldReturnFalseForInvalidUserIdOnHasActive() {
        // When
        boolean hasActive = sessionService.hasActiveSession(-1);

        // Then
        assertFalse(hasActive);
        verify(accesoRepository, never()).existsByUsuarioIdAndActivoTrue(any());
    }

    // ===============================
    // TESTS PARA CONSULTA DE INFORMACIÓN
    // ===============================

    @Test
    @DisplayName("getSessionInfo() - Debe retornar información de sesión existente")
    void shouldReturnSessionInfoForExistingSession() {
        // Given
        String sessionId = "session-123";
        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.of(sesionActiva));

        // When
        Optional<Acceso> sessionInfo = sessionService.getSessionInfo(sessionId);

        // Then
        assertTrue(sessionInfo.isPresent());
        assertEquals(sesionActiva, sessionInfo.get());
        verify(accesoRepository, times(1)).findByIdSesion(sessionId);
    }

    @Test
    @DisplayName("getSessionInfo() - Debe retornar Optional vacío para sesión inexistente")
    void shouldReturnEmptyOptionalForNonExistentSessionInfo() {
        // Given
        String sessionId = "non-existent";
        when(accesoRepository.findByIdSesion(sessionId)).thenReturn(Optional.empty());

        // When
        Optional<Acceso> sessionInfo = sessionService.getSessionInfo(sessionId);

        // Then
        assertFalse(sessionInfo.isPresent());
    }

    @Test
    @DisplayName("getSessionInfo() - Debe retornar Optional vacío para sessionId inválido")
    void shouldReturnEmptyOptionalForInvalidSessionId() {
        // When
        Optional<Acceso> sessionInfo = sessionService.getSessionInfo("   ");

        // Then
        assertFalse(sessionInfo.isPresent());
        verify(accesoRepository, never()).findByIdSesion(any());
    }

    @Test
    @DisplayName("getActiveUserSessions() - Debe retornar sesiones activas del usuario")
    void shouldReturnActiveUserSessions() {
        // Given
        Integer usuarioId = 1;
        List<Acceso> sessionesActivas = Arrays.asList(sesionActiva, sesionExpirada);

        when(accesoRepository.findByUsuarioIdAndActivoTrueOrderByFechaInicioDesc(usuarioId))
                .thenReturn(sessionesActivas);

        // When
        List<Acceso> result = sessionService.getActiveUserSessions(usuarioId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(sessionesActivas, result);
        verify(accesoRepository, times(1)).findByUsuarioIdAndActivoTrueOrderByFechaInicioDesc(usuarioId);
    }

    @Test
    @DisplayName("getActiveUserSessions() - Debe lanzar excepción con usuarioId inválido")
    void shouldThrowExceptionForInvalidUserIdOnGetActiveSessions() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.getActiveUserSessions(0));

        assertEquals("Usuario ID es requerido y debe ser mayor a 0", exception.getMessage());
        verify(accesoRepository, never()).findByUsuarioIdAndActivoTrueOrderByFechaInicioDesc(any());
    }

    @Test
    @DisplayName("getUserSessionHistory() - Debe retornar historial completo del usuario")
    void shouldReturnUserSessionHistory() {
        // Given
        Integer usuarioId = 1;
        List<Acceso> historial = Arrays.asList(sesionActiva, sesionInactiva, sesionExpirada);

        when(accesoRepository.findByUsuarioIdOrderByFechaInicioDesc(usuarioId))
                .thenReturn(historial);

        // When
        List<Acceso> result = sessionService.getUserSessionHistory(usuarioId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(historial, result);
        verify(accesoRepository, times(1)).findByUsuarioIdOrderByFechaInicioDesc(usuarioId);
    }

    @Test
    @DisplayName("countActiveUserSessions() - Debe retornar número correcto de sesiones activas")
    void shouldReturnCorrectActiveSessionCount() {
        // Given
        Integer usuarioId = 1;
        long expectedCount = 3L;

        when(accesoRepository.countByUsuarioIdAndActivoTrue(usuarioId)).thenReturn(expectedCount);

        // When
        long count = sessionService.countActiveUserSessions(usuarioId);

        // Then
        assertEquals(expectedCount, count);
        verify(accesoRepository, times(1)).countByUsuarioIdAndActivoTrue(usuarioId);
    }

    @Test
    @DisplayName("countActiveUserSessions() - Debe lanzar excepción con usuarioId null")
    void shouldThrowExceptionForNullUserIdOnCount() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionService.countActiveUserSessions(null));

        assertEquals("Usuario ID es requerido y debe ser mayor a 0", exception.getMessage());
        verify(accesoRepository, never()).countByUsuarioIdAndActivoTrue(any());
    }

    // ===============================
    // TESTS PARA LIMPIEZA Y MANTENIMIENTO
    // ===============================

    @Test
    @DisplayName("cleanupExpiredSessions() - Debe limpiar sesiones expiradas")
    void shouldCleanupExpiredSessions() {
        // Given
        List<Acceso> sessionesExpiradas = Arrays.asList(sesionExpirada);
        when(accesoRepository.findExpiredActiveSessions()).thenReturn(sessionesExpiradas);
        when(accesoRepository.saveAll(sessionesExpiradas)).thenReturn(sessionesExpiradas);

        // When
        int cleaned = sessionService.cleanupExpiredSessions();

        // Then
        assertEquals(1, cleaned);
        verify(accesoRepository, times(1)).findExpiredActiveSessions();
        verify(accesoRepository, times(1)).saveAll(sessionesExpiradas);

        // Verificar que se marcó como inactiva
        assertFalse(sesionExpirada.isActivo());
        assertNotNull(sesionExpirada.getFechaFin());
    }

    @Test
    @DisplayName("cleanupExpiredSessions() - Debe retornar 0 cuando no hay sesiones expiradas")
    void shouldReturnZeroWhenNoExpiredSessions() {
        // Given
        when(accesoRepository.findExpiredActiveSessions()).thenReturn(Collections.emptyList());

        // When
        int cleaned = sessionService.cleanupExpiredSessions();

        // Then
        assertEquals(0, cleaned);
        verify(accesoRepository, times(1)).findExpiredActiveSessions();
        verify(accesoRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("getSessionStats() - Debe retornar estadísticas del servicio")
    void shouldReturnSessionStats() {
        // Given
        long totalActive = 5L;
        long totalSessions = 10L;
        List<Acceso> expiredSessions = Arrays.asList(sesionExpirada);

        when(accesoRepository.countByActivoTrue()).thenReturn(totalActive);
        when(accesoRepository.count()).thenReturn(totalSessions);
        when(accesoRepository.findExpiredActiveSessions()).thenReturn(expiredSessions);

        // When
        Map<String, Object> stats = sessionService.getSessionStats();

        // Then
        assertNotNull(stats);
        assertEquals(totalActive, stats.get("totalActiveSessions"));
        assertEquals(totalSessions, stats.get("totalSessions"));
        assertEquals(1, stats.get("expiredActiveSessions"));
        assertEquals("active", stats.get("serviceStatus"));
        assertNotNull(stats.get("timestamp"));

        verify(accesoRepository, times(1)).countByActivoTrue();
        verify(accesoRepository, times(1)).count();
        verify(accesoRepository, times(1)).findExpiredActiveSessions();
    }

    // ===============================
    // TESTS DE MANEJO DE ERRORES
    // ===============================

    @Test
    @DisplayName("isSessionActive() - Debe manejar excepciones y retornar false")
    void shouldHandleExceptionsAndReturnFalseOnValidation() {
        // Given
        String sessionId = "session-123";
        when(accesoRepository.findByIdSesion(sessionId)).thenThrow(new RuntimeException("Database error"));

        // When
        boolean isActive = sessionService.isSessionActive(sessionId);

        // Then
        assertFalse(isActive);
    }

    @Test
    @DisplayName("hasActiveSession() - Debe manejar excepciones y retornar false")
    void shouldHandleExceptionsAndReturnFalseOnHasActive() {
        // Given
        Integer usuarioId = 1;
        when(accesoRepository.existsByUsuarioIdAndActivoTrue(usuarioId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        boolean hasActive = sessionService.hasActiveSession(usuarioId);

        // Then
        assertFalse(hasActive);
    }

    @Test
    @DisplayName("getSessionInfo() - Debe manejar excepciones y retornar Optional vacío")
    void shouldHandleExceptionsAndReturnEmptyOptionalOnGetInfo() {
        // Given
        String sessionId = "session-123";
        when(accesoRepository.findByIdSesion(sessionId)).thenThrow(new RuntimeException("Database error"));

        // When
        Optional<Acceso> sessionInfo = sessionService.getSessionInfo(sessionId);

        // Then
        assertFalse(sessionInfo.isPresent());
    }

    @Test
    @DisplayName("cleanupExpiredSessions() - Debe manejar errores de base de datos")
    void shouldHandleDatabaseErrorOnCleanup() {
        // Given
        when(accesoRepository.findExpiredActiveSessions()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> sessionService.cleanupExpiredSessions());

        assertTrue(exception.getMessage().contains("Error al limpiar sesiones expiradas"));
    }

    @Test
    @DisplayName("getSessionStats() - Debe manejar errores de base de datos")
    void shouldHandleDatabaseErrorOnStats() {
        // Given
        when(accesoRepository.countByActivoTrue()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> sessionService.getSessionStats());

        assertTrue(exception.getMessage().contains("Error al obtener estadísticas"));
    }
}