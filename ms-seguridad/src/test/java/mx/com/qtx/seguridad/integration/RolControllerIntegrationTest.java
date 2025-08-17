package mx.com.qtx.seguridad.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para RolController
 * Valida endpoints REST de gestión de roles y asignaciones (requiere ADMIN)
 */
@DisplayName("RolController Integration Tests")
public class RolControllerIntegrationTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE LISTAR ROLES (/roles)
    // ===============================

    @Test
    @DisplayName("GET /roles - Debe listar roles activos con autenticación ADMIN")
    void shouldListActiveRolesWithAdminAuth() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("roles"));
        assertNotNull(responseBody.get("total"));
        assertNotNull(responseBody.get("retrievedAt"));
        
        List<Map<String, Object>> roles = (List<Map<String, Object>>) responseBody.get("roles");
        assertTrue(roles.size() >= 0);
        
        // Validar estructura de cada rol
        if (!roles.isEmpty()) {
            Map<String, Object> firstRole = roles.get(0);
            assertNotNull(firstRole.get("id"));
            assertNotNull(firstRole.get("nombre"));
            assertNotNull(firstRole.get("activo"));
            assertNotNull(firstRole.get("fechaCreacion"));
            assertEquals(true, firstRole.get("activo"));
        }
    }

    @Test
    @DisplayName("GET /roles - Debe rechazar acceso sin autenticación")
    void shouldRejectAccessWithoutAuthentication() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/roles",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("missing_token", errorBody.get("errorType"));
    }

    @Test
    @DisplayName("GET /roles - Debe rechazar acceso con token inválido")
    void shouldRejectAccessWithInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(invalidToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("authentication_failed", errorBody.get("errorType"));
    }

    // ===============================
    // PRUEBAS DE ROLES POR USUARIO (/roles/usuario/{usuarioId})
    // ===============================

    @Test
    @DisplayName("GET /roles/usuario/{usuarioId} - Debe obtener roles de usuario existente")
    void shouldGetRolesForExistingUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 1; // Asumiendo que existe usuario con ID 1

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId,
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals(usuarioId, responseBody.get("usuarioId"));
        assertNotNull(responseBody.get("usuario"));
        assertNotNull(responseBody.get("roles"));
        assertNotNull(responseBody.get("totalRoles"));
        
        List<Map<String, Object>> roles = (List<Map<String, Object>>) responseBody.get("roles");
        assertTrue(roles.size() >= 0);
        
        // Validar estructura de roles asignados
        if (!roles.isEmpty()) {
            Map<String, Object> firstRole = roles.get(0);
            assertNotNull(firstRole.get("rolId"));
            assertNotNull(firstRole.get("nombre"));
            assertNotNull(firstRole.get("fechaAsignacion"));
        }
    }

    @Test
    @DisplayName("GET /roles/usuario/{usuarioId} - Debe retornar 404 para usuario inexistente")
    void shouldReturn404ForNonExistentUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 99999; // ID que no existe

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId,
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("user_not_found", errorBody.get("error"));
        assertEquals("Usuario no encontrado", errorBody.get("message"));
    }

    // ===============================
    // PRUEBAS DE ASIGNAR ROL (/roles/usuario/{usuarioId}/rol/{rolId})
    // ===============================

    @Test
    @DisplayName("POST /roles/usuario/{usuarioId}/rol/{rolId} - Debe asignar rol a usuario")
    void shouldAssignRoleToUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 2; // Usuario testuser (no admin para evitar conflictos)
        Integer rolId = 3; // Rol SUPERVISOR (no ADMIN para evitar conflictos)
        
        // Primero revocar el rol para asegurarnos que no esté asignado
        restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.DELETE,
                createAuthEntity(accessToken),
                Map.class
        );

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("Rol asignado exitosamente", responseBody.get("message"));
        assertEquals("success", responseBody.get("status"));
        assertEquals(usuarioId, responseBody.get("usuarioId"));
        assertEquals(rolId, responseBody.get("rolId"));
        assertNotNull(responseBody.get("usuario"));
        assertNotNull(responseBody.get("rol"));
        assertNotNull(responseBody.get("assignedAt"));
    }

    @Test
    @DisplayName("POST /roles/usuario/{usuarioId}/rol/{rolId} - Debe retornar 404 para usuario inexistente")
    void shouldReturn404WhenAssigningRoleToNonExistentUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 99999; // Usuario que no existe
        Integer rolId = 1; // Rol existente

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("user_not_found", errorBody.get("error"));
        assertEquals("Usuario no encontrado", errorBody.get("message"));
    }

    @Test
    @DisplayName("POST /roles/usuario/{usuarioId}/rol/{rolId} - Debe retornar 404 para rol inexistente")
    void shouldReturn404WhenAssigningNonExistentRole() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 1; // Usuario existente
        Integer rolId = 99999; // Rol que no existe

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("role_not_found", errorBody.get("error"));
        assertEquals("Rol no encontrado o inactivo", errorBody.get("message"));
    }

    @Test
    @DisplayName("POST /roles/usuario/{usuarioId}/rol/{rolId} - Debe retornar 409 si rol ya está asignado")
    void shouldReturn409WhenRoleAlreadyAssigned() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 2; // Usuario testuser (ya tiene rol USER asignado)
        Integer rolId = 2; // Rol USER (ya está asignado al testuser)
        
        // When - Intentar asignar el mismo rol otra vez (ya está asignado en DDL)
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("role_already_assigned", errorBody.get("error"));
        assertEquals("El rol ya está asignado al usuario", errorBody.get("message"));
    }

    // ===============================
    // PRUEBAS DE REVOCAR ROL (DELETE /roles/usuario/{usuarioId}/rol/{rolId})
    // ===============================

    @Test
    @DisplayName("DELETE /roles/usuario/{usuarioId}/rol/{rolId} - Debe revocar rol asignado")
    void shouldRevokeAssignedRole() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 2; // Usuario testuser (no admin para evitar conflictos)
        Integer rolId = 2; // Rol USER (ya está asignado al testuser en DDL)

        // When - Revocar rol USER del testuser (que ya lo tiene asignado)
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.DELETE,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("Rol revocado exitosamente", responseBody.get("message"));
        assertEquals("success", responseBody.get("status"));
        assertEquals(usuarioId, responseBody.get("usuarioId"));
        assertEquals(rolId, responseBody.get("rolId"));
        assertNotNull(responseBody.get("usuario"));
        assertNotNull(responseBody.get("rol"));
        assertNotNull(responseBody.get("revokedAt"));
    }

    @Test
    @DisplayName("DELETE /roles/usuario/{usuarioId}/rol/{rolId} - Debe retornar 404 si rol no está asignado")
    void shouldReturn404WhenRevokingUnassignedRole() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer usuarioId = 4; // Usuario 'noroles' (sin roles asignados)
        Integer rolId = 3; // Rol SUPERVISOR (no asignado a ese usuario)

        // When - Intentar revocar rol que no está asignado
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + usuarioId + "/rol/" + rolId,
                org.springframework.http.HttpMethod.DELETE,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("role_not_assigned", errorBody.get("error"));
        assertEquals("El rol no está asignado al usuario", errorBody.get("message"));
    }

    // ===============================
    // PRUEBAS DE ESTADÍSTICAS (/roles/stats)
    // ===============================

    @Test
    @DisplayName("GET /roles/stats - Debe retornar estadísticas de roles")
    void shouldReturnRoleStatistics() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/stats",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("totalRoles"));
        assertNotNull(responseBody.get("roles"));
        assertNotNull(responseBody.get("generatedAt"));
        
        List<Map<String, Object>> roleDetails = (List<Map<String, Object>>) responseBody.get("roles");
        assertTrue(roleDetails.size() >= 0);
        
        // Validar estructura de estadísticas de roles
        if (!roleDetails.isEmpty()) {
            Map<String, Object> firstRoleStat = roleDetails.get(0);
            assertNotNull(firstRoleStat.get("rolId"));
            assertNotNull(firstRoleStat.get("nombre"));
            assertNotNull(firstRoleStat.get("usuariosAsignados"));
            assertTrue(((Number) firstRoleStat.get("usuariosAsignados")).longValue() >= 0);
        }
    }

    // ===============================
    // PRUEBAS DE SEGURIDAD Y AUTORIZACIÓN
    // ===============================

    @Test
    @DisplayName("Debe rechazar métodos HTTP no permitidos")
    void shouldRejectUnsupportedHttpMethods() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // Test PATCH en endpoint GET
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/roles",
                org.springframework.http.HttpMethod.PATCH,
                createAuthEntity(accessToken),
                Map.class
        );
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response1.getStatusCode());

        // Test PUT en endpoint POST
        ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/roles/usuario/1/rol/1",
                org.springframework.http.HttpMethod.PUT,
                createAuthEntity(accessToken),
                Map.class
        );
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response2.getStatusCode());
    }

    @Test
    @DisplayName("Debe validar parámetros de path inválidos")
    void shouldValidateInvalidPathParameters() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // Test con usuarioId no numérico
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/roles/usuario/invalid/rol/1",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        // Test con rolId no numérico  
        ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/roles/usuario/1/rol/invalid",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @DisplayName("Debe manejar errores internos del servidor gracefully")
    void shouldHandleInternalServerErrorsGracefully() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // Test con IDs extremadamente grandes que podrían causar overflow
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/roles/usuario/" + Long.MAX_VALUE + "/rol/" + Long.MAX_VALUE,
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then - Debe manejar gracefully, no 500
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is2xxSuccessful());
    }
}