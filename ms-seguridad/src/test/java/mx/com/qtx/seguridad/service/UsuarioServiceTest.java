package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.dto.UsuarioDto;
import mx.com.qtx.seguridad.dto.UsuarioMapper;
import mx.com.qtx.seguridad.entity.RolAsignado;
import mx.com.qtx.seguridad.entity.Usuario;
import mx.com.qtx.seguridad.repository.RolAsignadoRepository;
import mx.com.qtx.seguridad.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService
 * Verifica gestión de usuarios y operaciones DTO con dependencias mockeadas
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UsuarioService Tests")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolAsignadoRepository rolAsignadoRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    // Datos de prueba
    private Usuario usuarioTest;
    private Usuario usuarioInactivo;
    private UsuarioDto usuarioDto;
    private RolAsignado rolAsignadoAdmin;
    private RolAsignado rolAsignadoVendedor;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, rolAsignadoRepository, usuarioMapper, passwordEncoder);
        setupTestData();
    }

    private void setupTestData() {
        // Usuario activo de prueba
        usuarioTest = new Usuario();
        usuarioTest.setId(1);
        usuarioTest.setUsuario("testuser");
        usuarioTest.setPassword("hashedPassword123");
        usuarioTest.setActivo(true);
        usuarioTest.setFechaCreacion(LocalDateTime.now().minusMonths(1));

        // Usuario inactivo de prueba
        usuarioInactivo = new Usuario();
        usuarioInactivo.setId(2);
        usuarioInactivo.setUsuario("inactiveuser");
        usuarioInactivo.setPassword("hashedPassword456");
        usuarioInactivo.setActivo(false);
        usuarioInactivo.setFechaCreacion(LocalDateTime.now().minusMonths(2));

        // DTO de prueba
        usuarioDto = new UsuarioDto();
        usuarioDto.setUsuario("newuser");
        usuarioDto.setPassword("plainPassword123");
        usuarioDto.setActivo(true);

        // Roles asignados de prueba
        mx.com.qtx.seguridad.entity.Rol rolAdmin = mock(mx.com.qtx.seguridad.entity.Rol.class);
        when(rolAdmin.getNombre()).thenReturn("ADMIN");

        mx.com.qtx.seguridad.entity.Rol rolVendedor = mock(mx.com.qtx.seguridad.entity.Rol.class);
        when(rolVendedor.getNombre()).thenReturn("VENDEDOR");

        rolAsignadoAdmin = mock(RolAsignado.class);
        when(rolAsignadoAdmin.getRol()).thenReturn(rolAdmin);

        rolAsignadoVendedor = mock(RolAsignado.class);
        when(rolAsignadoVendedor.getRol()).thenReturn(rolVendedor);
    }

    // ===============================
    // TESTS PARA CONSULTAS PAGINADAS
    // ===============================

    @Test
    @DisplayName("obtenerUsuariosPaginados() - Debe retornar página con usuarios")
    void shouldReturnPagedUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Usuario> usuarios = Arrays.asList(usuarioTest, usuarioInactivo);
        Page<Usuario> expectedPage = new PageImpl<>(usuarios, pageable, usuarios.size());

        when(usuarioRepository.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<Usuario> result = usuarioService.obtenerUsuariosPaginados(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(usuarios, result.getContent());
        assertEquals(pageable, result.getPageable());
        verify(usuarioRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("obtenerUsuariosPaginados() - Debe retornar página vacía cuando no hay usuarios")
    void shouldReturnEmptyPageWhenNoUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(usuarioRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<Usuario> result = usuarioService.obtenerUsuariosPaginados(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(usuarioRepository, times(1)).findAll(pageable);
    }

    // ===============================
    // TESTS PARA OBTENCIÓN DE ROLES
    // ===============================

    @Test
    @DisplayName("obtenerRolesDeUsuario() - Debe retornar roles del usuario")
    void shouldReturnUserRoles() {
        // Given
        Integer usuarioId = 1;
        List<RolAsignado> rolesAsignados = Arrays.asList(rolAsignadoAdmin, rolAsignadoVendedor);

        when(rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true)).thenReturn(rolesAsignados);

        // When
        List<String> roles = usuarioService.obtenerRolesDeUsuario(usuarioId);

        // Then
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("VENDEDOR"));
        verify(rolAsignadoRepository, times(1)).findByUsuarioIdAndActivo(usuarioId, true);
    }

    @Test
    @DisplayName("obtenerRolesDeUsuario() - Debe retornar lista vacía para usuario sin roles")
    void shouldReturnEmptyListForUserWithoutRoles() {
        // Given
        Integer usuarioId = 2;
        when(rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true)).thenReturn(Collections.emptyList());

        // When
        List<String> roles = usuarioService.obtenerRolesDeUsuario(usuarioId);

        // Then
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
        verify(rolAsignadoRepository, times(1)).findByUsuarioIdAndActivo(usuarioId, true);
    }

    @Test
    @DisplayName("obtenerRolesDeUsuario() - Debe manejar roles duplicados correctamente")
    void shouldHandleDuplicateRolesCorrectly() {
        // Given
        Integer usuarioId = 1;
        mx.com.qtx.seguridad.entity.Rol rolAdminDuplicado = mock(mx.com.qtx.seguridad.entity.Rol.class);
        when(rolAdminDuplicado.getNombre()).thenReturn("ADMIN");

        RolAsignado rolAsignadoDuplicado = mock(RolAsignado.class);
        when(rolAsignadoDuplicado.getRol()).thenReturn(rolAdminDuplicado); // Mismo rol que rolAsignadoAdmin

        List<RolAsignado> rolesConDuplicados = Arrays.asList(rolAsignadoAdmin, rolAsignadoDuplicado);
        when(rolAsignadoRepository.findByUsuarioIdAndActivo(usuarioId, true)).thenReturn(rolesConDuplicados);

        // When
        List<String> roles = usuarioService.obtenerRolesDeUsuario(usuarioId);

        // Then
        assertNotNull(roles);
        assertEquals(1, roles.size()); // Debe eliminar duplicados
        assertTrue(roles.contains("ADMIN"));
        verify(rolAsignadoRepository, times(1)).findByUsuarioIdAndActivo(usuarioId, true);
    }

    // ===============================
    // TESTS PARA CONVERSIÓN A DTO
    // ===============================

    @Test
    @DisplayName("convertirADtoConRoles() - Debe convertir usuario con roles a DTO")
    void shouldConvertUserWithRolesToDto() {
        // Given
        List<String> roles = Arrays.asList("ADMIN", "VENDEDOR");
        UsuarioDto expectedDto = new UsuarioDto();
        expectedDto.setUsuario("testuser");

        when(usuarioMapper.toDto(usuarioTest, roles)).thenReturn(expectedDto);

        // When
        UsuarioDto result = usuarioService.convertirADtoConRoles(usuarioTest, roles);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(usuarioMapper, times(1)).toDto(usuarioTest, roles);
    }

    @Test
    @DisplayName("convertirADto() - Debe convertir usuario sin roles a DTO")
    void shouldConvertUserToDto() {
        // Given
        UsuarioDto expectedDto = new UsuarioDto();
        expectedDto.setUsuario("testuser");

        when(usuarioMapper.toDto(usuarioTest)).thenReturn(expectedDto);

        // When
        UsuarioDto result = usuarioService.convertirADto(usuarioTest);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(usuarioMapper, times(1)).toDto(usuarioTest);
    }

    // ===============================
    // TESTS PARA BÚSQUEDA DE USUARIOS
    // ===============================

    @Test
    @DisplayName("buscarPorId() - Debe retornar usuario existente")
    void shouldReturnExistingUserById() {
        // Given
        Integer usuarioId = 1;
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioTest));

        // When
        Optional<Usuario> result = usuarioService.buscarPorId(usuarioId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(usuarioTest, result.get());
        verify(usuarioRepository, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("buscarPorId() - Debe retornar Optional vacío para usuario inexistente")
    void shouldReturnEmptyOptionalForNonExistentUser() {
        // Given
        Integer usuarioId = 999;
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> result = usuarioService.buscarPorId(usuarioId);

        // Then
        assertFalse(result.isPresent());
        verify(usuarioRepository, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("buscarPorNombreUsuario() - Debe retornar usuario por nombre")
    void shouldReturnUserByUsername() {
        // Given
        String nombreUsuario = "testuser";
        when(usuarioRepository.findByUsuario(nombreUsuario)).thenReturn(Optional.of(usuarioTest));

        // When
        Optional<Usuario> result = usuarioService.buscarPorNombreUsuario(nombreUsuario);

        // Then
        assertTrue(result.isPresent());
        assertEquals(usuarioTest, result.get());
        verify(usuarioRepository, times(1)).findByUsuario(nombreUsuario);
    }

    @Test
    @DisplayName("buscarPorNombreUsuario() - Debe retornar Optional vacío para nombre inexistente")
    void shouldReturnEmptyOptionalForNonExistentUsername() {
        // Given
        String nombreUsuario = "nonexistent";
        when(usuarioRepository.findByUsuario(nombreUsuario)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> result = usuarioService.buscarPorNombreUsuario(nombreUsuario);

        // Then
        assertFalse(result.isPresent());
        verify(usuarioRepository, times(1)).findByUsuario(nombreUsuario);
    }

    // ===============================
    // TESTS PARA VALIDACIÓN DE CREACIÓN
    // ===============================

    @Test
    @DisplayName("esValidoParaCreacion() - Debe retornar true para DTO válido")
    void shouldReturnTrueForValidDto() {
        // Given
        when(usuarioMapper.isValidForCreation(usuarioDto)).thenReturn(true);

        // When
        boolean result = usuarioService.esValidoParaCreacion(usuarioDto);

        // Then
        assertTrue(result);
        verify(usuarioMapper, times(1)).isValidForCreation(usuarioDto);
    }

    @Test
    @DisplayName("esValidoParaCreacion() - Debe retornar false para DTO inválido")
    void shouldReturnFalseForInvalidDto() {
        // Given
        UsuarioDto dtoInvalido = new UsuarioDto();
        when(usuarioMapper.isValidForCreation(dtoInvalido)).thenReturn(false);

        // When
        boolean result = usuarioService.esValidoParaCreacion(dtoInvalido);

        // Then
        assertFalse(result);
        verify(usuarioMapper, times(1)).isValidForCreation(dtoInvalido);
    }

    // ===============================
    // TESTS PARA CREACIÓN DE USUARIOS
    // ===============================

    @Test
    @DisplayName("crearUsuario() - Debe crear nuevo usuario exitosamente")
    void shouldCreateUserSuccessfully() {
        // Given
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario("newuser");

        String hashedPassword = "hashedPlainPassword123";

        when(usuarioMapper.toEntity(usuarioDto)).thenReturn(nuevoUsuario);
        when(passwordEncoder.encode(usuarioDto.getPassword())).thenReturn(hashedPassword);
        when(usuarioRepository.save(nuevoUsuario)).thenReturn(nuevoUsuario);

        // When
        Usuario result = usuarioService.crearUsuario(usuarioDto);

        // Then
        assertNotNull(result);
        assertEquals(nuevoUsuario, result);
        assertEquals(hashedPassword, nuevoUsuario.getPassword());

        verify(usuarioMapper, times(1)).toEntity(usuarioDto);
        verify(passwordEncoder, times(1)).encode(usuarioDto.getPassword());
        verify(usuarioRepository, times(1)).save(nuevoUsuario);
    }

    // ===============================
    // TESTS PARA VERIFICACIÓN DE EXISTENCIA
    // ===============================

    @Test
    @DisplayName("existeOtroUsuarioConNombre() - Debe retornar true cuando existe otro usuario")
    void shouldReturnTrueWhenAnotherUserExists() {
        // Given
        String nombreUsuario = "testuser";
        Integer idExcluir = 2;

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(1); // ID diferente al excluido

        when(usuarioRepository.findByUsuario(nombreUsuario)).thenReturn(Optional.of(otroUsuario));

        // When
        boolean result = usuarioService.existeOtroUsuarioConNombre(nombreUsuario, idExcluir);

        // Then
        assertTrue(result);
        verify(usuarioRepository, times(1)).findByUsuario(nombreUsuario);
    }

    @Test
    @DisplayName("existeOtroUsuarioConNombre() - Debe retornar false cuando no existe otro usuario")
    void shouldReturnFalseWhenNoOtherUserExists() {
        // Given
        String nombreUsuario = "testuser";
        Integer idExcluir = 2;

        when(usuarioRepository.findByUsuario(nombreUsuario)).thenReturn(Optional.empty());

        // When
        boolean result = usuarioService.existeOtroUsuarioConNombre(nombreUsuario, idExcluir);

        // Then
        assertFalse(result);
        verify(usuarioRepository, times(1)).findByUsuario(nombreUsuario);
    }

    @Test
    @DisplayName("existeOtroUsuarioConNombre() - Debe retornar false cuando es el mismo usuario")
    void shouldReturnFalseWhenSameUser() {
        // Given
        String nombreUsuario = "testuser";
        Integer idExcluir = 1;

        Usuario mismoUsuario = new Usuario();
        mismoUsuario.setId(1); // Mismo ID que el excluido

        when(usuarioRepository.findByUsuario(nombreUsuario)).thenReturn(Optional.of(mismoUsuario));

        // When
        boolean result = usuarioService.existeOtroUsuarioConNombre(nombreUsuario, idExcluir);

        // Then
        assertFalse(result);
        verify(usuarioRepository, times(1)).findByUsuario(nombreUsuario);
    }

    // ===============================
    // TESTS PARA ACTUALIZACIÓN DE USUARIOS
    // ===============================

    @Test
    @DisplayName("actualizarUsuario() - Debe actualizar usuario sin cambiar contraseña")
    void shouldUpdateUserWithoutPasswordChange() {
        // Given
        UsuarioDto dtoSinPassword = mock(UsuarioDto.class);
        when(dtoSinPassword.getUsuario()).thenReturn("updateduser");

        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setUsuario("updateduser");

        when(usuarioMapper.updateEntity(usuarioTest, dtoSinPassword)).thenReturn(usuarioActualizado);
        when(dtoSinPassword.hasPassword()).thenReturn(false);
        when(usuarioRepository.save(usuarioActualizado)).thenReturn(usuarioActualizado);

        // When
        Usuario result = usuarioService.actualizarUsuario(usuarioTest, dtoSinPassword);

        // Then
        assertNotNull(result);
        assertEquals(usuarioActualizado, result);

        verify(usuarioMapper, times(1)).updateEntity(usuarioTest, dtoSinPassword);
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, times(1)).save(usuarioActualizado);
    }

    @Test
    @DisplayName("actualizarUsuario() - Debe actualizar usuario con nueva contraseña")
    void shouldUpdateUserWithPasswordChange() {
        // Given
        UsuarioDto dtoConPassword = mock(UsuarioDto.class);
        when(dtoConPassword.getUsuario()).thenReturn("updateduser");
        when(dtoConPassword.getPassword()).thenReturn("newPassword123");

        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setUsuario("updateduser");

        String hashedNewPassword = "hashedNewPassword123";

        when(usuarioMapper.updateEntity(usuarioTest, dtoConPassword)).thenReturn(usuarioActualizado);
        when(dtoConPassword.hasPassword()).thenReturn(true);
        when(passwordEncoder.encode(dtoConPassword.getPassword())).thenReturn(hashedNewPassword);
        when(usuarioRepository.save(usuarioActualizado)).thenReturn(usuarioActualizado);

        // When
        Usuario result = usuarioService.actualizarUsuario(usuarioTest, dtoConPassword);

        // Then
        assertNotNull(result);
        assertEquals(usuarioActualizado, result);
        assertEquals(hashedNewPassword, usuarioActualizado.getPassword());

        verify(usuarioMapper, times(1)).updateEntity(usuarioTest, dtoConPassword);
        verify(passwordEncoder, times(1)).encode(dtoConPassword.getPassword());
        verify(usuarioRepository, times(1)).save(usuarioActualizado);
    }

    // ===============================
    // TESTS PARA DESACTIVACIÓN DE USUARIOS
    // ===============================

    @Test
    @DisplayName("desactivarUsuario() - Debe desactivar usuario y sus roles")
    void shouldDeactivateUserAndRoles() {
        // Given
        List<RolAsignado> rolesAsignados = Arrays.asList(rolAsignadoAdmin, rolAsignadoVendedor);

        when(usuarioRepository.save(usuarioTest)).thenReturn(usuarioTest);
        when(rolAsignadoRepository.findByUsuarioId(usuarioTest.getId())).thenReturn(rolesAsignados);
        when(rolAsignadoRepository.saveAll(rolesAsignados)).thenReturn(rolesAsignados);

        // When
        Usuario result = usuarioService.desactivarUsuario(usuarioTest);

        // Then
        assertNotNull(result);
        assertFalse(result.isActivo()); // Usuario desactivado

        verify(usuarioRepository, times(1)).save(usuarioTest);
        verify(rolAsignadoRepository, times(1)).findByUsuarioId(usuarioTest.getId());
        verify(rolAsignadoRepository, times(1)).saveAll(rolesAsignados);

        // Verificar que los roles fueron desactivados
        verify(rolAsignadoAdmin, times(1)).setActivo(false);
        verify(rolAsignadoVendedor, times(1)).setActivo(false);
    }

    @Test
    @DisplayName("desactivarUsuario() - Debe manejar usuario sin roles asignados")
    void shouldDeactivateUserWithoutRoles() {
        // Given
        when(usuarioRepository.save(usuarioTest)).thenReturn(usuarioTest);
        when(rolAsignadoRepository.findByUsuarioId(usuarioTest.getId())).thenReturn(Collections.emptyList());
        when(rolAsignadoRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        Usuario result = usuarioService.desactivarUsuario(usuarioTest);

        // Then
        assertNotNull(result);
        assertFalse(result.isActivo());

        verify(usuarioRepository, times(1)).save(usuarioTest);
        verify(rolAsignadoRepository, times(1)).findByUsuarioId(usuarioTest.getId());
        verify(rolAsignadoRepository, times(1)).saveAll(Collections.emptyList());
    }

    // ===============================
    // TESTS PARA ESTADÍSTICAS
    // ===============================

    @Test
    @DisplayName("obtenerEstadisticasUsuarios() - Debe retornar estadísticas correctas")
    void shouldReturnCorrectUserStatistics() {
        // Given
        long totalUsers = 10L;
        long activeUsers = 7L;
        long expectedInactiveUsers = 3L;

        when(usuarioRepository.count()).thenReturn(totalUsers);
        when(usuarioRepository.countByActivoTrue()).thenReturn(activeUsers);

        // When
        long[] stats = usuarioService.obtenerEstadisticasUsuarios();

        // Then
        assertNotNull(stats);
        assertEquals(3, stats.length);
        assertEquals(totalUsers, stats[0]); // Total
        assertEquals(activeUsers, stats[1]); // Activos
        assertEquals(expectedInactiveUsers, stats[2]); // Inactivos

        verify(usuarioRepository, times(1)).count();
        verify(usuarioRepository, times(1)).countByActivoTrue();
    }

    @Test
    @DisplayName("obtenerEstadisticasUsuarios() - Debe manejar caso sin usuarios")
    void shouldHandleNoUsersCase() {
        // Given
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.countByActivoTrue()).thenReturn(0L);

        // When
        long[] stats = usuarioService.obtenerEstadisticasUsuarios();

        // Then
        assertNotNull(stats);
        assertEquals(3, stats.length);
        assertEquals(0L, stats[0]); // Total
        assertEquals(0L, stats[1]); // Activos
        assertEquals(0L, stats[2]); // Inactivos
    }

    // ===============================
    // TESTS DE CASOS EDGE Y VALIDACIONES
    // ===============================

    @Test
    @DisplayName("buscarPorId() - Debe manejar ID null sin lanzar excepción")
    void shouldHandleNullIdGracefully() {
        // Given
        when(usuarioRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> result = usuarioService.buscarPorId(null);

        // Then
        assertFalse(result.isPresent());
        verify(usuarioRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("buscarPorNombreUsuario() - Debe manejar nombre null sin lanzar excepción")
    void shouldHandleNullUsernameGracefully() {
        // Given
        when(usuarioRepository.findByUsuario(null)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> result = usuarioService.buscarPorNombreUsuario(null);

        // Then
        assertFalse(result.isPresent());
        verify(usuarioRepository, times(1)).findByUsuario(null);
    }

    @Test
    @DisplayName("convertirADtoConRoles() - Debe manejar lista de roles vacía")
    void shouldHandleEmptyRolesList() {
        // Given
        List<String> rolesVacios = Collections.emptyList();
        UsuarioDto expectedDto = new UsuarioDto();

        when(usuarioMapper.toDto(usuarioTest, rolesVacios)).thenReturn(expectedDto);

        // When
        UsuarioDto result = usuarioService.convertirADtoConRoles(usuarioTest, rolesVacios);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(usuarioMapper, times(1)).toDto(usuarioTest, rolesVacios);
    }

    @Test
    @DisplayName("convertirADtoConRoles() - Debe manejar lista de roles null")
    void shouldHandleNullRolesList() {
        // Given
        List<String> rolesNull = null;
        UsuarioDto expectedDto = new UsuarioDto();

        when(usuarioMapper.toDto(usuarioTest, rolesNull)).thenReturn(expectedDto);

        // When
        UsuarioDto result = usuarioService.convertirADtoConRoles(usuarioTest, rolesNull);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(usuarioMapper, times(1)).toDto(usuarioTest, rolesNull);
    }

    @Test
    @DisplayName("obtenerEstadisticasUsuarios() - Debe calcular inactivos cuando todos son activos")
    void shouldCalculateInactiveWhenAllActive() {
        // Given
        long totalUsers = 5L;
        long activeUsers = 5L; // Todos activos

        when(usuarioRepository.count()).thenReturn(totalUsers);
        when(usuarioRepository.countByActivoTrue()).thenReturn(activeUsers);

        // When
        long[] stats = usuarioService.obtenerEstadisticasUsuarios();

        // Then
        assertEquals(totalUsers, stats[0]);
        assertEquals(activeUsers, stats[1]);
        assertEquals(0L, stats[2]); // 0 inactivos
    }
}