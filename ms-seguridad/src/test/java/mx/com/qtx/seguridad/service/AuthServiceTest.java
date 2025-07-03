package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.domain.Usuario;
import mx.com.qtx.seguridad.domain.RolAsignado;
import mx.com.qtx.seguridad.domain.Rol;
import mx.com.qtx.seguridad.dto.TokenResponse;
import mx.com.qtx.seguridad.repository.UsuarioRepository;
import mx.com.qtx.seguridad.repository.RolAsignadoRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService
 * Verifica autenticación, renovación de tokens y gestión de blacklist
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolAsignadoRepository rolAsignadoRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private BCryptPasswordEncoder passwordEncoder;

    private Usuario testUser;
    private RolAsignado rolAsignado;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
        authService = new AuthService(usuarioRepository, rolAsignadoRepository, jwtService);

        // Configurar datos de test
        setupTestData();
    }

    private void setupTestData() {
        // Usuario de test
        testUser = new Usuario();
        testUser.setId(1);
        testUser.setUsuario("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setActivo(true);
        testUser.setFechaCreacion(LocalDateTime.now());
        testUser.setFechaModificacion(LocalDateTime.now());

        // Rol de test
        rolAdmin = new Rol();
        rolAdmin.setId(1);
        rolAdmin.setNombre("ADMIN");
        rolAdmin.setActivo(true);

        // Asignación de rol
        rolAsignado = new RolAsignado();
        rolAsignado.setUsuario(testUser);
        rolAsignado.setRol(rolAdmin);
        rolAsignado.setActivo(true);
    }

    @Test
    @DisplayName("Debe autenticar usuario con credenciales válidas")
    void shouldAuthenticateUserWithValidCredentials() {
        // Given
        String username = "testuser";
        String password = "password123";
        List<String> roles = Arrays.asList("ADMIN");

        when(usuarioRepository.findByUsuarioAndActivo(username, true))
                .thenReturn(Optional.of(testUser));
        when(rolAsignadoRepository.findActiveAssignmentsByUsuarioWithRoleDetails(testUser.getId()))
                .thenReturn(Arrays.asList(rolAsignado));
        when(jwtService.generateAccessToken(username, testUser.getId(), roles))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(username, testUser.getId()))
                .thenReturn("refresh-token");

        // When
        TokenResponse response = authService.authenticate(username, password);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(300L, response.getExpiresIn());

        verify(usuarioRepository).findByUsuarioAndActivo(username, true);
        verify(jwtService).generateAccessToken(username, testUser.getId(), roles);
        verify(jwtService).generateRefreshToken(username, testUser.getId());
    }

    @Test
    @DisplayName("Debe rechazar usuario inexistente")
    void shouldRejectNonExistentUser() {
        // Given
        String username = "nonexistent";
        String password = "password123";

        when(usuarioRepository.findByUsuarioAndActivo(username, true))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(username, password));
        
        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepository).findByUsuarioAndActivo(username, true);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Debe rechazar contraseña incorrecta")
    void shouldRejectIncorrectPassword() {
        // Given
        String username = "testuser";
        String wrongPassword = "wrongpassword";

        when(usuarioRepository.findByUsuarioAndActivo(username, true))
                .thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(username, wrongPassword));
        
        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepository).findByUsuarioAndActivo(username, true);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Debe rechazar usuario sin roles")
    void shouldRejectUserWithoutRoles() {
        // Given
        String username = "testuser";
        String password = "password123";

        when(usuarioRepository.findByUsuarioAndActivo(username, true))
                .thenReturn(Optional.of(testUser));
        when(rolAsignadoRepository.findActiveAssignmentsByUsuarioWithRoleDetails(testUser.getId()))
                .thenReturn(Arrays.asList()); // Sin roles

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.authenticate(username, password));
        
        assertEquals("Usuario sin roles asignados", exception.getMessage());
        verify(usuarioRepository).findByUsuarioAndActivo(username, true);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Debe obtener roles asignados correctamente")
    void shouldGetAssignedRolesCorrectly() {
        // Given
        Integer userId = 1;
        
        when(rolAsignadoRepository.findActiveAssignmentsByUsuarioWithRoleDetails(userId))
                .thenReturn(Arrays.asList(rolAsignado));

        // When
        List<String> roles = authService.getRolesAsignados(userId);

        // Then
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ADMIN", roles.get(0));
        verify(rolAsignadoRepository).findActiveAssignmentsByUsuarioWithRoleDetails(userId);
    }

    @Test
    @DisplayName("Debe manejar error al obtener roles")
    void shouldHandleErrorWhenGettingRoles() {
        // Given
        Integer userId = 1;
        
        when(rolAsignadoRepository.findActiveAssignmentsByUsuarioWithRoleDetails(userId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        List<String> roles = authService.getRolesAsignados(userId);

        // Then
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
        verify(rolAsignadoRepository).findActiveAssignmentsByUsuarioWithRoleDetails(userId);
    }

    @Test
    @DisplayName("Debe renovar token correctamente")
    void shouldRefreshTokenCorrectly() {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("ADMIN");

        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(usuarioRepository.findByUsuarioAndActivo(username, true))
                .thenReturn(Optional.of(testUser));
        when(rolAsignadoRepository.findActiveAssignmentsByUsuarioWithRoleDetails(userId))
                .thenReturn(Arrays.asList(rolAsignado));
        when(jwtService.generateAccessToken(username, userId, roles))
                .thenReturn("new-access-token");

        // When
        TokenResponse response = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(300L, response.getExpiresIn());

        verify(jwtService).extractUsername(refreshToken);
        verify(jwtService).extractUserId(refreshToken);
        verify(jwtService).isRefreshToken(refreshToken);
        verify(jwtService).generateAccessToken(username, userId, roles);
    }

    @Test
    @DisplayName("Debe rechazar token no refresh")
    void shouldRejectNonRefreshToken() {
        // Given
        String accessToken = "access-token";
        String username = "testuser";
        Integer userId = 1;

        when(jwtService.extractUsername(accessToken)).thenReturn(username);
        when(jwtService.extractUserId(accessToken)).thenReturn(userId);
        when(jwtService.isRefreshToken(accessToken)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.refreshToken(accessToken));
        
        assertTrue(exception.getMessage().contains("Token no es de tipo refresh"));
    }

    @Test
    @DisplayName("Debe realizar logout correctamente")
    void shouldPerformLogoutCorrectly() {
        // Given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String username = "testuser";

        when(jwtService.extractUsername(accessToken)).thenReturn(username);

        // When
        var result = authService.logout(accessToken, refreshToken);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Logout exitoso", result.get("message"));
        verify(jwtService).extractUsername(accessToken);
    }

    @Test
    @DisplayName("Debe validar token correctamente")
    void shouldValidateTokenCorrectly() {
        // Given
        String validToken = "valid-token";
        String invalidToken = "invalid-token";
        
        when(jwtService.isTokenExpired(validToken)).thenReturn(false);
        when(jwtService.validateToken(validToken)).thenReturn(mock(Claims.class));
        
        when(jwtService.isTokenExpired(invalidToken)).thenReturn(true);
        when(jwtService.validateToken(invalidToken)).thenThrow(new JwtException("Invalid token"));

        // When
        boolean isValidTokenValid = authService.isTokenValid(validToken);
        boolean isInvalidTokenValid = authService.isTokenValid(invalidToken);

        // Then
        assertTrue(isValidTokenValid);
        assertFalse(isInvalidTokenValid);
    }


    @Test
    @DisplayName("Debe obtener información de usuario desde token")
    void shouldGetUserInfoFromToken() {
        // Given
        String token = "valid-token";
        String username = "testuser";
        Integer userId = 1;
        List<String> roles = Arrays.asList("ADMIN");

        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.validateToken(token)).thenReturn(mock(Claims.class));
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.extractRoles(token)).thenReturn(roles);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        var userInfo = authService.getUserInfoFromToken(token);

        // Then
        assertNotNull(userInfo);
        assertEquals(userId, userInfo.get("id"));
        assertEquals(username, userInfo.get("username"));
        assertEquals(true, userInfo.get("active"));
        assertEquals(roles, userInfo.get("roles"));
    }

    @Test
    @DisplayName("Debe manejar token inválido al obtener información de usuario")
    void shouldHandleInvalidTokenWhenGettingUserInfo() {
        // Given
        String invalidToken = "invalid-token";
        
        when(jwtService.isTokenExpired(invalidToken)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.getUserInfoFromToken(invalidToken));
        
        assertEquals("Error al obtener información de usuario", exception.getMessage());
    }


    @Test
    @DisplayName("Debe obtener estadísticas de autenticación")
    void shouldGetAuthenticationStats() {
        // Given
        String token1 = "token1";
        String token2 = "token2";
        
        authService.logout(token1, null);
        authService.logout(token2, null);

        // When
        var stats = authService.getAuthStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("serviceStatus"));
        assertEquals("active", stats.get("serviceStatus"));
    }
}