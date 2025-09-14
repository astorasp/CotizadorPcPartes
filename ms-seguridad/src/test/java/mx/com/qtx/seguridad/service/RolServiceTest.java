package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.entity.Rol;
import mx.com.qtx.seguridad.entity.RolAsignado;
import mx.com.qtx.seguridad.entity.RolAsignadoId;
import mx.com.qtx.seguridad.entity.Usuario;
import mx.com.qtx.seguridad.repository.RolAsignadoRepository;
import mx.com.qtx.seguridad.repository.RolRepository;
import mx.com.qtx.seguridad.repository.UsuarioRepository;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RolService
 * Verifica gestión de roles y asignaciones con dependencias mockeadas
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RolService Tests")
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private RolAsignadoRepository rolAsignadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private RolService rolService;

    // Datos de prueba
    private Rol rolAdmin;
    private Rol rolVendedor;
    private Rol rolInactivo;
    private Usuario usuarioTest;
    private RolAsignado rolAsignado;

    @BeforeEach
    void setUp() {
        rolService = new RolService(rolRepository, rolAsignadoRepository, usuarioRepository);

        // Crear datos de prueba
        setupTestData();
    }

    private void setupTestData() {
        // Roles de prueba
        rolAdmin = new Rol();
        rolAdmin.setId(1);
        rolAdmin.setNombre("ADMIN");
        rolAdmin.setActivo(true);
        rolAdmin.setFechaCreacion(LocalDateTime.now());

        rolVendedor = new Rol();
        rolVendedor.setId(2);
        rolVendedor.setNombre("VENDEDOR");
        rolVendedor.setActivo(true);
        rolVendedor.setFechaCreacion(LocalDateTime.now());

        rolInactivo = new Rol();
        rolInactivo.setId(3);
        rolInactivo.setNombre("INACTIVO");
        rolInactivo.setActivo(false);
        rolInactivo.setFechaCreacion(LocalDateTime.now());

        // Usuario de prueba
        usuarioTest = new Usuario();
        usuarioTest.setId(1);
        usuarioTest.setUsuario("testuser");
        usuarioTest.setActivo(true);
        usuarioTest.setFechaCreacion(LocalDateTime.now());

        // Rol asignado de prueba
        rolAsignado = new RolAsignado();
        rolAsignado.setId(new RolAsignadoId(1, 1));
        rolAsignado.setUsuario(usuarioTest);
        rolAsignado.setRol(rolAdmin);
        rolAsignado.setActivo(true);
    }

    // ===============================
    // TESTS PARA MÉTODOS DE CONSULTA
    // ===============================

    @Test
    @DisplayName("obtenerRolesActivos() - Debe retornar lista de roles activos")
    void shouldReturnActiveRoles() {
        // Given
        List<Rol> rolesActivos = Arrays.asList(rolAdmin, rolVendedor);
        when(rolRepository.findByActivoTrue()).thenReturn(rolesActivos);

        // When
        List<Rol> resultado = rolService.obtenerRolesActivos();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(rolAdmin));
        assertTrue(resultado.contains(rolVendedor));
        verify(rolRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerRolesActivos() - Debe retornar lista vacía cuando no hay roles activos")
    void shouldReturnEmptyListWhenNoActiveRoles() {
        // Given
        when(rolRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<Rol> resultado = rolService.obtenerRolesActivos();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(rolRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerRolesPorUsuario() - Debe retornar roles asignados al usuario")
    void shouldReturnUserRoles() {
        // Given
        Integer usuarioId = 1;
        List<RolAsignado> rolesAsignados = Arrays.asList(rolAsignado);
        when(rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true))
                .thenReturn(rolesAsignados);

        // When
        List<RolAsignado> resultado = rolService.obtenerRolesPorUsuario(usuarioId);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(rolAsignado, resultado.get(0));
        verify(rolAsignadoRepository, times(1)).findByUsuarioIdAndActivo(usuarioId, true);
    }

    @Test
    @DisplayName("obtenerRolesPorUsuario() - Debe retornar lista vacía para usuario sin roles")
    void shouldReturnEmptyListForUserWithoutRoles() {
        // Given
        Integer usuarioId = 2;
        when(rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true))
                .thenReturn(Collections.emptyList());

        // When
        List<RolAsignado> resultado = rolService.obtenerRolesPorUsuario(usuarioId);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(rolAsignadoRepository, times(1)).findByUsuarioIdAndActivo(usuarioId, true);
    }

    @Test
    @DisplayName("buscarUsuarioPorId() - Debe retornar usuario existente")
    void shouldReturnExistingUser() {
        // Given
        Integer usuarioId = 1;
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioTest));

        // When
        Optional<Usuario> resultado = rolService.buscarUsuarioPorId(usuarioId);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(usuarioTest, resultado.get());
        verify(usuarioRepository, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("buscarUsuarioPorId() - Debe retornar Optional vacío para usuario inexistente")
    void shouldReturnEmptyOptionalForNonExistentUser() {
        // Given
        Integer usuarioId = 999;
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> resultado = rolService.buscarUsuarioPorId(usuarioId);

        // Then
        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("buscarRolActivoPorId() - Debe retornar rol activo existente")
    void shouldReturnActiveRole() {
        // Given
        Integer rolId = 1;
        when(rolRepository.findById(rolId)).thenReturn(Optional.of(rolAdmin));

        // When
        Optional<Rol> resultado = rolService.buscarRolActivoPorId(rolId);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(rolAdmin, resultado.get());
        verify(rolRepository, times(1)).findById(rolId);
    }

    @Test
    @DisplayName("buscarRolActivoPorId() - Debe retornar Optional vacío para rol inactivo")
    void shouldReturnEmptyOptionalForInactiveRole() {
        // Given
        Integer rolId = 3;
        when(rolRepository.findById(rolId)).thenReturn(Optional.of(rolInactivo));

        // When
        Optional<Rol> resultado = rolService.buscarRolActivoPorId(rolId);

        // Then
        assertFalse(resultado.isPresent());
        verify(rolRepository, times(1)).findById(rolId);
    }

    @Test
    @DisplayName("buscarRolPorId() - Debe retornar rol existente independientemente de su estado")
    void shouldReturnRoleRegardlessOfStatus() {
        // Given
        Integer rolId = 3;
        when(rolRepository.findById(rolId)).thenReturn(Optional.of(rolInactivo));

        // When
        Optional<Rol> resultado = rolService.buscarRolPorId(rolId);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(rolInactivo, resultado.get());
        verify(rolRepository, times(1)).findById(rolId);
    }

    @Test
    @DisplayName("obtenerRolesConEstadisticas() - Debe retornar roles activos")
    void shouldReturnRolesWithStats() {
        // Given
        List<Rol> rolesActivos = Arrays.asList(rolAdmin, rolVendedor);
        when(rolRepository.findByActivoTrue()).thenReturn(rolesActivos);

        // When
        List<Rol> resultado = rolService.obtenerRolesConEstadisticas();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(rolRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("contarUsuariosAsignados() - Debe retornar número de usuarios asignados al rol")
    void shouldCountUsersAssignedToRole() {
        // Given
        Integer rolId = 1;
        long expectedCount = 5L;
        when(rolAsignadoRepository.countByRolIdAndActivo(rolId, true)).thenReturn(expectedCount);

        // When
        long resultado = rolService.contarUsuariosAsignados(rolId);

        // Then
        assertEquals(expectedCount, resultado);
        verify(rolAsignadoRepository, times(1)).countByRolIdAndActivo(rolId, true);
    }

    // ===============================
    // TESTS PARA ASIGNACIÓN DE ROLES
    // ===============================

    @Test
    @DisplayName("asignarRol() - Debe crear nueva asignación exitosamente")
    void shouldCreateNewRoleAssignment() {
        // Given
        Integer usuarioId = 1;
        Integer rolId = 1;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.empty());
        when(rolAsignadoRepository.save(any(RolAsignado.class))).thenReturn(rolAsignado);

        // When
        boolean resultado = rolService.asignarRol(usuarioId, rolId, usuarioTest, rolAdmin);

        // Then
        assertTrue(resultado);
        verify(rolAsignadoRepository, times(1)).findById(rolAsignadoId);
        verify(rolAsignadoRepository, times(1)).save(any(RolAsignado.class));
    }

    @Test
    @DisplayName("asignarRol() - Debe retornar false si el rol ya está asignado")
    void shouldReturnFalseWhenRoleAlreadyAssigned() {
        // Given
        Integer usuarioId = 1;
        Integer rolId = 1;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        RolAsignado existingAssignment = new RolAsignado();
        existingAssignment.setActivo(true);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.of(existingAssignment));

        // When
        boolean resultado = rolService.asignarRol(usuarioId, rolId, usuarioTest, rolAdmin);

        // Then
        assertFalse(resultado);
        verify(rolAsignadoRepository, times(1)).findById(rolAsignadoId);
        verify(rolAsignadoRepository, never()).save(any(RolAsignado.class));
    }

    @Test
    @DisplayName("asignarRol() - Debe reactivar asignación inactiva")
    void shouldReactivateInactiveAssignment() {
        // Given
        Integer usuarioId = 1;
        Integer rolId = 1;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        RolAsignado inactiveAssignment = new RolAsignado();
        inactiveAssignment.setActivo(false);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.of(inactiveAssignment));
        when(rolAsignadoRepository.save(inactiveAssignment)).thenReturn(inactiveAssignment);

        // When
        boolean resultado = rolService.asignarRol(usuarioId, rolId, usuarioTest, rolAdmin);

        // Then
        assertTrue(resultado);
        assertTrue(inactiveAssignment.isActivo());
        verify(rolAsignadoRepository, times(1)).findById(rolAsignadoId);
        verify(rolAsignadoRepository, times(1)).save(inactiveAssignment);
    }

    // ===============================
    // TESTS PARA REVOCACIÓN DE ROLES
    // ===============================

    @Test
    @DisplayName("revocarRol() - Debe revocar rol asignado exitosamente")
    void shouldRevokeAssignedRole() {
        // Given
        Integer usuarioId = 1;
        Integer rolId = 1;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        RolAsignado activeAssignment = new RolAsignado();
        activeAssignment.setActivo(true);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.of(activeAssignment));
        when(rolAsignadoRepository.save(activeAssignment)).thenReturn(activeAssignment);

        // When
        boolean resultado = rolService.revocarRol(usuarioId, rolId);

        // Then
        assertTrue(resultado);
        assertFalse(activeAssignment.isActivo());
        verify(rolAsignadoRepository, times(1)).findById(rolAsignadoId);
        verify(rolAsignadoRepository, times(1)).save(activeAssignment);
    }

    @Test
    @DisplayName("revocarRol() - Debe retornar false si el rol no estaba asignado")
    void shouldReturnFalseWhenRoleNotAssigned() {
        // Given
        Integer usuarioId = 1;
        Integer rolId = 1;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.empty());

        // When
        boolean resultado = rolService.revocarRol(usuarioId, rolId);

        // Then
        assertFalse(resultado);
        verify(rolAsignadoRepository, times(1)).findById(rolAsignadoId);
        verify(rolAsignadoRepository, never()).save(any(RolAsignado.class));
    }

    @Test
    @DisplayName("revocarRol() - Debe retornar false si la asignación ya estaba inactiva")
    void shouldReturnFalseWhenAssignmentAlreadyInactive() {
        // Given
        Integer usuarioId = 1;
        Integer rolId = 1;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        RolAsignado inactiveAssignment = new RolAsignado();
        inactiveAssignment.setActivo(false);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.of(inactiveAssignment));

        // When
        boolean resultado = rolService.revocarRol(usuarioId, rolId);

        // Then
        assertFalse(resultado);
        verify(rolAsignadoRepository, times(1)).findById(rolAsignadoId);
        verify(rolAsignadoRepository, never()).save(any(RolAsignado.class));
    }

    // ===============================
    // TESTS DE CASOS EDGE Y VALIDACIONES
    // ===============================

    @Test
    @DisplayName("buscarUsuarioPorId() - Debe manejar ID null sin lanzar excepción")
    void shouldHandleNullUserIdGracefully() {
        // Given
        when(usuarioRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> resultado = rolService.buscarUsuarioPorId(null);

        // Then
        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("buscarRolPorId() - Debe manejar ID null sin lanzar excepción")
    void shouldHandleNullRoleIdGracefully() {
        // Given
        when(rolRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Rol> resultado = rolService.buscarRolPorId(null);

        // Then
        assertFalse(resultado.isPresent());
        verify(rolRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("contarUsuariosAsignados() - Debe retornar 0 para rol sin usuarios asignados")
    void shouldReturnZeroForRoleWithoutAssignedUsers() {
        // Given
        Integer rolId = 999;
        when(rolAsignadoRepository.countByRolIdAndActivo(rolId, true)).thenReturn(0L);

        // When
        long resultado = rolService.contarUsuariosAsignados(rolId);

        // Then
        assertEquals(0L, resultado);
        verify(rolAsignadoRepository, times(1)).countByRolIdAndActivo(rolId, true);
    }

    @Test
    @DisplayName("asignarRol() - Debe crear asignación con entidades proporcionadas")
    void shouldCreateAssignmentWithProvidedEntities() {
        // Given
        Integer usuarioId = 1; // Usar el mismo ID que el usuarioTest
        Integer rolId = 2;
        RolAsignadoId rolAsignadoId = new RolAsignadoId(usuarioId, rolId);

        when(rolAsignadoRepository.findById(rolAsignadoId)).thenReturn(Optional.empty());
        when(rolAsignadoRepository.save(any(RolAsignado.class))).thenAnswer(invocation -> {
            RolAsignado assignment = invocation.getArgument(0);
            // Verificar que se usaron las entidades proporcionadas
            assertEquals(usuarioTest, assignment.getUsuario());
            assertEquals(rolVendedor, assignment.getRol());
            assertEquals(rolAsignadoId, assignment.getId());
            assertTrue(assignment.isActivo());
            return assignment;
        });

        // When
        boolean resultado = rolService.asignarRol(usuarioId, rolId, usuarioTest, rolVendedor);

        // Then
        assertTrue(resultado);
        verify(rolAsignadoRepository, times(1)).save(any(RolAsignado.class));
    }
}