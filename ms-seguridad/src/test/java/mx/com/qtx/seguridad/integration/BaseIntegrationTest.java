package mx.com.qtx.seguridad.integration;

import mx.com.qtx.seguridad.config.TestContainerConfig;
import mx.com.qtx.seguridad.repository.AccesoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.AfterEach;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base para tests de integración con TestContainers
 * Proporciona configuración común y utilidades para testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Testcontainers
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected AccesoRepository accesoRepository;
    
    protected String baseUrl;
    
    // Lista para trackear tokens creados durante el test
    private final List<String> activeTokens = new ArrayList<>();

    @BeforeEach
    void setUpBaseTest() {
        baseUrl = "http://localhost:" + port + "/api/v1/seguridad";
        
        // Solo limpiar la lista de tokens para el nuevo test
        activeTokens.clear();
    }

    @AfterEach
    @Transactional
    void tearDownBaseTest() {
        try {
            // Hacer logout de todos los tokens activos de este test
            for (String token : new ArrayList<>(activeTokens)) {
                try {
                    Map<String, String> logoutRequest = Map.of(
                        "accessToken", token
                    );
                    restTemplate.exchange(
                        baseUrl + "/auth/logout",
                        org.springframework.http.HttpMethod.POST,
                        createAuthEntity(logoutRequest, token),
                        Map.class
                    );
                } catch (Exception e) {
                    // Ignorar errores de logout individual
                }
            }
            
            // Limpieza quirúrgica: solo eliminar sesiones para evitar conflictos
            // sin afectar otros datos del sistema
            accesoRepository.deleteAll();
            accesoRepository.flush();
            
            // Pausa breve para permitir que los cambios se propaguen
            Thread.sleep(100);
            
        } catch (Exception e) {
            System.err.println("Warning: Error during test cleanup: " + e.getMessage());
        } finally {
            // Limpiar la lista de tokens para el próximo test
            activeTokens.clear();
        }
    }

    /**
     * Crea headers HTTP con Content-Type JSON
     */
    protected HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Crea headers HTTP con Authorization Bearer token
     */
    protected HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /**
     * Crea HttpEntity con headers JSON
     */
    protected <T> HttpEntity<T> createJsonEntity(T body) {
        return new HttpEntity<>(body, createJsonHeaders());
    }

    /**
     * Crea HttpEntity con Authorization header
     */
    protected <T> HttpEntity<T> createAuthEntity(T body, String token) {
        return new HttpEntity<>(body, createAuthHeaders(token));
    }

    /**
     * Crea HttpEntity solo con Authorization header (sin body)
     */
    protected HttpEntity<Void> createAuthEntity(String token) {
        return new HttpEntity<>(createAuthHeaders(token));
    }

    /**
     * Realiza login de test y retorna el access token
     */
    protected String performTestLogin() {
        return performTestLoginForced("admin", "admin123");
    }

    /**
     * Realiza login con credenciales específicas
     */
    protected String performTestLogin(String username, String password) {
        Map<String, String> loginRequest = Map.of(
            "usuario", username,
            "password", password
        );

        try {
            var response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                createJsonEntity(loginRequest),
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                String token = (String) body.get("accessToken");
                if (token != null) {
                    // Registrar el token para limpieza posterior
                    activeTokens.add(token);
                    return token;
                }
            }
            
            throw new RuntimeException("Login failed for test user: " + username + ", status: " + response.getStatusCode());
            
        } catch (Exception e) {
            throw new RuntimeException("Login failed for test user: " + username, e);
        }
    }

    /**
     * Obtiene un refresh token de test
     */
    protected String performTestLoginAndGetRefreshToken() {
        return performTestLoginAndGetRefreshToken("admin", "admin123");
    }

    /**
     * Obtiene refresh token con credenciales específicas
     */
    protected String performTestLoginAndGetRefreshToken(String username, String password) {
        Map<String, String> loginRequest = Map.of(
            "usuario", username,
            "password", password
        );

        var response = restTemplate.postForEntity(
            baseUrl + "/auth/login",
            createJsonEntity(loginRequest),
            Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            if (body != null) {
                String accessToken = (String) body.get("accessToken");
                if (accessToken != null) {
                    // Registrar el token para limpieza posterior
                    activeTokens.add(accessToken);
                }
                return (String) body.get("refreshToken");
            }
        }

        throw new RuntimeException("Login failed for test user: " + username);
    }

    /**
     * Valida que una respuesta tenga estructura de error estándar
     */
    protected void assertErrorResponse(Map<String, Object> response, String expectedErrorType) {
        assert response != null;
        assert response.containsKey("timestamp");
        assert response.containsKey("status");
        assert response.containsKey("error");
        assert response.containsKey("message");
        
        if (expectedErrorType != null) {
            assert expectedErrorType.equals(response.get("errorType"));
        }
    }

    /**
     * Valida que una respuesta de token tenga la estructura correcta
     */
    protected void assertTokenResponse(Map<String, Object> response) {
        assert response != null;
        assert response.containsKey("accessToken");
        assert response.containsKey("refreshToken");
        assert response.containsKey("tokenType");
        assert response.containsKey("expiresIn");
        assert "Bearer".equals(response.get("tokenType"));
        assert response.get("accessToken") != null;
        assert response.get("refreshToken") != null;
        assert ((Number) response.get("expiresIn")).longValue() > 0;
    }

    /**
     * Valida que una respuesta de usuario tenga la estructura correcta
     */
    protected void assertUsuarioResponse(Map<String, Object> response) {
        assert response != null;
        assert response.containsKey("id");
        assert response.containsKey("usuario");
        assert response.containsKey("activo");
        assert response.containsKey("fechaCreacion");
        assert response.get("id") != null;
        assert response.get("usuario") != null;
    }

    /**
     * Espera un tiempo determinado (útil para tests de expiración)
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test interrupted", e);
        }
    }

    /**
     * Crea un usuario de test temporal
     */
    protected Map<String, Object> createTestUser(String username, String password) {
        return Map.of(
            "usuario", username,
            "password", password,
            "activo", true
        );
    }

    /**
     * Realiza múltiples logins con diferentes usuarios para evitar conflictos de sesión única
     * Usado para tests que requieren múltiples autenticaciones consecutivas
     */
    protected String performTestLoginWithUser(String username) {
        if ("admin".equals(username)) {
            return performTestLogin(username, "admin123");
        } else {
            return performTestLogin(username, "user123");
        }
    }

    /**
     * Genera un usuario único para cada test method para evitar conflictos de sesión
     */
    protected String getUniqueTestUser() {
        return "testuser_" + System.currentTimeMillis();
    }

    /**
     * Realiza login forzado limpiando cualquier sesión existente primero
     */
    protected String performTestLoginForced(String username, String password) {
        return performTestLogin(username, password);
    }

    /**
     * Obtiene el número de puerto del servidor de test
     */
    protected int getServerPort() {
        return port;
    }

    /**
     * Obtiene la URL base del servidor de test
     */
    protected String getBaseUrl() {
        return baseUrl;
    }

}