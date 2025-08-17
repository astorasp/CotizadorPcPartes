package mx.com.qtx.seguridad.scheduler;

import mx.com.qtx.seguridad.config.TestContainerConfig;
import mx.com.qtx.seguridad.entity.Acceso;
import mx.com.qtx.seguridad.repository.AccesoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests para el job de limpieza de sesiones expiradas
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Testcontainers
@TestPropertySource(properties = {
    "jwt.access-token.expiration=5000", // 5 segundos para tests
    "session.cleanup.enabled=true",
    "session.cleanup.interval.seconds=1" // Cada segundo para tests
})
@DisplayName("Session Cleanup Job Tests")
class SessionCleanupJobTest {

    @Autowired
    private SessionCleanupJob sessionCleanupJob;
    
    @Autowired
    private AccesoRepository accesoRepository;
    
    @BeforeEach
    void setUp() {
        // Limpiar todas las sesiones antes de cada test
        accesoRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should cleanup expired sessions")
    @Transactional
    void shouldCleanupExpiredSessions() throws Exception {
        // Given - Crear sesiones con diferentes tiempos
        LocalDateTime now = LocalDateTime.now();
        
        // Sesión expirada (hace 10 segundos)
        Acceso expiredSession = new Acceso();
        expiredSession.setIdSesion("expired-session-1");
        expiredSession.setUsuarioId(1);
        expiredSession.setActivo(true);
        expiredSession.setFechaInicio(now.minusSeconds(10));
        accesoRepository.save(expiredSession);
        
        // Sesión activa reciente (hace 2 segundos)
        Acceso activeSession = new Acceso();
        activeSession.setIdSesion("active-session-1");
        activeSession.setUsuarioId(2);
        activeSession.setActivo(true);
        activeSession.setFechaInicio(now.minusSeconds(2));
        accesoRepository.save(activeSession);
        
        // When - Ejecutar el job
        JobExecutionContext mockContext = mock(JobExecutionContext.class);
        sessionCleanupJob.execute(mockContext);
        
        // Then - Verificar resultados
        Acceso expiredCheck = accesoRepository.findByIdSesion("expired-session-1").orElse(null);
        assertNotNull(expiredCheck);
        assertFalse(expiredCheck.isActivo(), "Expired session should be inactive");
        assertNotNull(expiredCheck.getFechaFin(), "Expired session should have end date");
        
        Acceso activeCheck = accesoRepository.findByIdSesion("active-session-1").orElse(null);
        assertNotNull(activeCheck);
        assertTrue(activeCheck.isActivo(), "Recent session should remain active");
        assertNull(activeCheck.getFechaFin(), "Recent session should not have end date");
    }
    
    @Test
    @DisplayName("Should handle empty sessions gracefully")
    void shouldHandleEmptySessionsGracefully() throws Exception {
        // Given - No hay sesiones
        assertEquals(0, accesoRepository.count());
        
        // When - Ejecutar el job
        JobExecutionContext mockContext = mock(JobExecutionContext.class);
        
        // Then - No debe lanzar excepción
        assertDoesNotThrow(() -> sessionCleanupJob.execute(mockContext));
    }
    
    @Test
    @DisplayName("Should cleanup multiple expired sessions")
    @Transactional
    void shouldCleanupMultipleExpiredSessions() throws Exception {
        // Given - Crear múltiples sesiones expiradas
        LocalDateTime now = LocalDateTime.now();
        
        // Solo usar IDs de usuarios que existen: 1 (admin), 2 (testuser), 3 (inactive)
        int[] existingUserIds = {1, 2, 3, 1, 2}; // Reutilizar IDs existentes
        for (int i = 0; i < 5; i++) {
            Acceso expiredSession = new Acceso();
            expiredSession.setIdSesion("expired-session-" + (i + 1));
            expiredSession.setUsuarioId(existingUserIds[i]);
            expiredSession.setActivo(true);
            expiredSession.setFechaInicio(now.minusSeconds(10 + i + 1)); // Todas expiradas
            accesoRepository.save(expiredSession);
        }
        
        // Verificar estado inicial
        List<Acceso> activeSessions = accesoRepository.findByActivoTrue();
        assertEquals(5, activeSessions.size());
        
        // When - Ejecutar el job
        JobExecutionContext mockContext = mock(JobExecutionContext.class);
        sessionCleanupJob.execute(mockContext);
        
        // Then - Todas deben estar inactivas
        List<Acceso> remainingActive = accesoRepository.findByActivoTrue();
        assertEquals(0, remainingActive.size(), "All expired sessions should be inactive");
        
        // Verificar que todas tienen fecha fin
        List<Acceso> allSessions = accesoRepository.findAll();
        for (Acceso session : allSessions) {
            assertFalse(session.isActivo());
            assertNotNull(session.getFechaFin());
        }
    }
    
    @Test
    @DisplayName("Should respect token expiration time configuration")
    @Transactional
    void shouldRespectTokenExpirationTimeConfiguration() throws Exception {
        // Given - Crear sesiones en el límite de expiración (5 segundos configurados)
        LocalDateTime now = LocalDateTime.now();
        
        // Sesión justo en el límite (4.5 segundos)
        Acceso borderlineSession = new Acceso();
        borderlineSession.setIdSesion("borderline-session");
        borderlineSession.setUsuarioId(1);
        borderlineSession.setActivo(true);
        borderlineSession.setFechaInicio(now.minusSeconds(4).minusNanos(500_000_000L));
        accesoRepository.save(borderlineSession);
        
        // Sesión claramente expirada (6 segundos)
        Acceso expiredSession = new Acceso();
        expiredSession.setIdSesion("clearly-expired");
        expiredSession.setUsuarioId(2);
        expiredSession.setActivo(true);
        expiredSession.setFechaInicio(now.minusSeconds(6));
        accesoRepository.save(expiredSession);
        
        // When - Ejecutar el job
        JobExecutionContext mockContext = mock(JobExecutionContext.class);
        sessionCleanupJob.execute(mockContext);
        
        // Then - Solo la claramente expirada debe estar inactiva
        Acceso borderlineCheck = accesoRepository.findByIdSesion("borderline-session").orElse(null);
        assertNotNull(borderlineCheck);
        assertTrue(borderlineCheck.isActivo(), "Borderline session should remain active");
        
        Acceso expiredCheck = accesoRepository.findByIdSesion("clearly-expired").orElse(null);
        assertNotNull(expiredCheck);
        assertFalse(expiredCheck.isActivo(), "Clearly expired session should be inactive");
    }
}