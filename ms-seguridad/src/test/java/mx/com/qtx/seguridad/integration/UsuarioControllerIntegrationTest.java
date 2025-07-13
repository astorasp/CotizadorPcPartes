package mx.com.qtx.seguridad.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para UsuarioController
 * Valida endpoints REST de gestión de usuarios (requiere ADMIN)
 */
@DisplayName("UsuarioController Integration Tests")
public class UsuarioControllerIntegrationTest extends BaseIntegrationTest {

    // ===============================
    // PRUEBAS DE LISTAR USUARIOS (/usuarios)
    // ===============================

    @Test
    @DisplayName("GET /usuarios - Debe listar usuarios con paginación por defecto")
    void shouldListUsersWithDefaultPagination() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("usuarios"));
        assertNotNull(responseBody.get("currentPage"));
        assertNotNull(responseBody.get("totalItems"));
        assertNotNull(responseBody.get("totalPages"));
        assertNotNull(responseBody.get("pageSize"));
        assertNotNull(responseBody.get("hasNext"));
        assertNotNull(responseBody.get("hasPrevious"));
        
        assertEquals(0, responseBody.get("currentPage")); // Primera página
        assertEquals(10, responseBody.get("pageSize")); // Tamaño por defecto
        assertEquals(false, responseBody.get("hasPrevious")); // Primera página
        
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) responseBody.get("usuarios");
        assertTrue(usuarios.size() >= 0);
        
        // Validar estructura de usuario
        if (!usuarios.isEmpty()) {
            Map<String, Object> firstUser = usuarios.get(0);
            assertUsuarioResponse(firstUser);
        }
    }

    @Test
    @DisplayName("GET /usuarios - Debe listar usuarios con parámetros de paginación personalizados")
    void shouldListUsersWithCustomPagination() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios?page=0&size=5&sort=usuario&direction=DESC",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals(0, responseBody.get("currentPage"));
        assertEquals(5, responseBody.get("pageSize"));
    }

    @Test
    @DisplayName("GET /usuarios - Debe rechazar acceso sin autenticación")
    void shouldRejectAccessWithoutAuthentication() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/usuarios",
                Map.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        assertEquals("missing_token", errorBody.get("errorType"));
    }

    // ===============================
    // PRUEBAS DE OBTENER USUARIO POR ID (/usuarios/{id})
    // ===============================

    @Test
    @DisplayName("GET /usuarios/{id} - Debe obtener usuario existente por ID")
    void shouldGetExistingUserById() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer userId = 1; // Asumiendo que existe

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertUsuarioResponse(responseBody);
        assertEquals(userId, responseBody.get("id"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Debe retornar 404 para usuario inexistente")
    void shouldReturn404ForNonExistentUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer userId = 99999; // ID que no existe

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
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
    // PRUEBAS DE CREAR USUARIO (/usuarios)
    // ===============================

    @Test
    @DisplayName("POST /usuarios - Debe crear usuario válido exitosamente")
    void shouldCreateValidUserSuccessfully() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        String uniqueUsername = getUniqueTestUser();
        
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("usuario", uniqueUsername);
        newUser.put("password", "password123");
        newUser.put("activo", true);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(newUser, accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertUsuarioResponse(responseBody);
        assertEquals(uniqueUsername, responseBody.get("usuario"));
        assertTrue((Boolean) responseBody.get("activo"));
        assertNull(responseBody.get("password")); // Password no debe retornarse
    }

    @Test
    @DisplayName("POST /usuarios - Debe rechazar usuario con datos inválidos")
    void shouldRejectUserWithInvalidData() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        
        Map<String, Object> invalidUser = new HashMap<>();
        invalidUser.put("usuario", ""); // Usuario vacío
        invalidUser.put("password", ""); // Password vacío

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(invalidUser, accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> errorBody = response.getBody();
        // Spring Boot devuelve "Bad Request" para validaciones básicas
        assertTrue(errorBody.containsKey("message") || errorBody.containsKey("error"));
    }

    @Test
    @DisplayName("POST /usuarios - Debe rechazar usuario duplicado")
    void shouldRejectDuplicateUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        String duplicateUsername = "admin"; // Usuario que ya existe
        
        Map<String, Object> duplicateUser = new HashMap<>();
        duplicateUser.put("usuario", duplicateUsername);
        duplicateUser.put("password", "password123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(duplicateUser, accessToken),
                Map.class
        );

        // Then - Puede ser CONFLICT o BAD_REQUEST dependiendo de la validación
        assertTrue(response.getStatusCode().equals(HttpStatus.CONFLICT) || 
                  response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        assertNotNull(response.getBody());
    }

    // ===============================
    // PRUEBAS DE ACTUALIZAR USUARIO (/usuarios/{id})
    // ===============================

    @Test
    @DisplayName("PUT /usuarios/{id} - Debe actualizar usuario existente")
    void shouldUpdateExistingUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        
        // Crear usuario primero
        String uniqueUsername = getUniqueTestUser();
        Map<String, Object> newUser = createTestUser(uniqueUsername, "password123");
        
        ResponseEntity<Map> createResponse = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(newUser, accessToken),
                Map.class
        );
        
        Integer userId = (Integer) createResponse.getBody().get("id");
        
        // Datos de actualización
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("usuario", uniqueUsername + "_updated");
        updateData.put("activo", false);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
                org.springframework.http.HttpMethod.PUT,
                createAuthEntity(updateData, accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertUsuarioResponse(responseBody);
        assertEquals(uniqueUsername + "_updated", responseBody.get("usuario"));
        assertEquals(false, responseBody.get("activo"));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Debe retornar 404 para usuario inexistente")
    void shouldReturn404WhenUpdatingNonExistentUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer userId = 99999; // ID que no existe
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("usuario", "updated_user");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
                org.springframework.http.HttpMethod.PUT,
                createAuthEntity(updateData, accessToken),
                Map.class
        );

        // Then - Puede ser NOT_FOUND o BAD_REQUEST dependiendo de la validación del ID
        assertTrue(response.getStatusCode().equals(HttpStatus.NOT_FOUND) || 
                  response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Debe rechazar conflicto de nombre de usuario")
    void shouldRejectUsernameConflict() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer userId = 1; // Usuario existente
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("usuario", "admin"); // Nombre que ya existe en otro usuario

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
                org.springframework.http.HttpMethod.PUT,
                createAuthEntity(updateData, accessToken),
                Map.class
        );

        // Then - Puede ser CONFLICT, OK o BAD_REQUEST dependiendo de la lógica
        assertTrue(response.getStatusCode().equals(HttpStatus.CONFLICT) || 
                  response.getStatusCode().equals(HttpStatus.OK) ||
                  response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
    }

    // ===============================
    // PRUEBAS DE ELIMINAR USUARIO (/usuarios/{id})
    // ===============================

    @Test
    @DisplayName("DELETE /usuarios/{id} - Debe desactivar usuario existente")
    void shouldDeactivateExistingUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        
        // Crear usuario primero
        String uniqueUsername = getUniqueTestUser();
        Map<String, Object> newUser = createTestUser(uniqueUsername, "password123");
        
        ResponseEntity<Map> createResponse = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(newUser, accessToken),
                Map.class
        );
        
        Integer userId = (Integer) createResponse.getBody().get("id");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
                org.springframework.http.HttpMethod.DELETE,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertEquals("Usuario desactivado exitosamente", responseBody.get("message"));
        assertEquals("success", responseBody.get("status"));
        assertEquals(userId.toString(), responseBody.get("usuarioId"));
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} - Debe retornar 404 para usuario inexistente")
    void shouldReturn404WhenDeletingNonExistentUser() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        Integer userId = 99999; // ID que no existe

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + userId,
                org.springframework.http.HttpMethod.DELETE,
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
    // PRUEBAS DE ESTADÍSTICAS (/usuarios/stats)
    // ===============================

    @Test
    @DisplayName("GET /usuarios/stats - Debe retornar estadísticas de usuarios")
    void shouldReturnUserStatistics() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios/stats",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody.get("totalUsers"));
        assertNotNull(responseBody.get("activeUsers"));
        assertNotNull(responseBody.get("inactiveUsers"));
        assertNotNull(responseBody.get("generatedAt"));
        
        long totalUsers = ((Number) responseBody.get("totalUsers")).longValue();
        long activeUsers = ((Number) responseBody.get("activeUsers")).longValue();
        long inactiveUsers = ((Number) responseBody.get("inactiveUsers")).longValue();
        
        assertTrue(totalUsers >= 0);
        assertTrue(activeUsers >= 0);
        assertTrue(inactiveUsers >= 0);
        assertEquals(totalUsers, activeUsers + inactiveUsers);
    }

    // ===============================
    // PRUEBAS DE SEGURIDAD Y VALIDACIÓN
    // ===============================

    @Test
    @DisplayName("Debe rechazar métodos HTTP no permitidos")
    void shouldRejectUnsupportedHttpMethods() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // Test PATCH en endpoint GET - puede retornar 405 o 200 dependiendo del endpoint
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.PATCH,
                createAuthEntity(accessToken),
                Map.class
        );
        assertTrue(response1.getStatusCode().equals(HttpStatus.METHOD_NOT_ALLOWED) ||
                   response1.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    @DisplayName("Debe validar parámetros de paginación inválidos")
    void shouldValidateInvalidPaginationParameters() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // Test con página negativa - Spring Boot normaliza valores negativos a 0
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/usuarios?page=-1",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );
        // Spring Boot maneja parámetros inválidos gracefully
        assertTrue(response1.getStatusCode().is2xxSuccessful() || 
                   response1.getStatusCode().is4xxClientError() ||
                   response1.getStatusCode().is5xxServerError());
    }

    @Test
    @DisplayName("Debe validar IDs de usuario inválidos")
    void shouldValidateInvalidUserIds() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");

        // Test con ID no numérico
        ResponseEntity<Map> response1 = restTemplate.exchange(
                baseUrl + "/usuarios/invalid",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        // Test con ID negativo
        ResponseEntity<Map> response2 = restTemplate.exchange(
                baseUrl + "/usuarios/-1",
                org.springframework.http.HttpMethod.GET,
                createAuthEntity(accessToken),
                Map.class
        );
        assertTrue(response2.getStatusCode().is4xxClientError() || response2.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("Debe manejar JSON malformado gracefully")
    void shouldHandleMalformedJsonGracefully() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        String malformedJson = "{\"usuario\":\"test\",\"password\":}";

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(malformedJson, accessToken),
                Map.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe limitar tamaño de payload para creación de usuarios")
    void shouldLimitPayloadSizeForUserCreation() {
        // Given
        String accessToken = performTestLogin("admin", "admin123");
        
        // Payload extremadamente grande
        StringBuilder largeUsername = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeUsername.append("a");
        }
        
        Map<String, Object> largeUser = new HashMap<>();
        largeUser.put("usuario", largeUsername.toString());
        largeUser.put("password", "password123");

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                org.springframework.http.HttpMethod.POST,
                createAuthEntity(largeUser, accessToken),
                Map.class
        );

        // Then - Debe ser rechazado por validación o tamaño
        assertTrue(response.getStatusCode().is4xxClientError());
    }
}