package mx.com.qtx.cotizador.security.jwt.utils;

import mx.com.qtx.cotizador.security.client.JwksClient;
import mx.com.qtx.cotizador.security.dto.JwksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cliente JWKS mockeado para testing JWT sin dependencias de red.
 *
 * Esta clase simula completamente el comportamiento del JwksClient real,
 * permitiendo testing de validación JWT sin necesidad de un ms-seguridad
 * en ejecución. Es fundamental para el requerimiento de "simular que obtenemos
 * o contamos con el certificado publico para poder validar la firma".
 *
 * <h3>Capacidades de simulación:</h3>
 * <ul>
 *   <li><b>Respuestas JWKS válidas</b>: Con una o múltiples claves públicas</li>
 *   <li><b>Errores de red</b>: Connection refused, timeouts, etc.</li>
 *   <li><b>Respuestas vacías</b>: Para testing de casos extremos</li>
 *   <li><b>Delays de red</b>: Simula latencia real del servicio</li>
 *   <li><b>Múltiples escenarios</b>: Cambio dinámico de comportamiento</li>
 * </ul>
 *
 * <h3>Flujo de validación JWT simulado:</h3>
 * <pre>
 * 1. Sistema necesita validar token JWT
 * 2. MockJwksClient simula obtener certificados públicos
 * 3. Sistema valida firma del token con certificado simulado
 * 4. Validación exitosa sin dependencias externas
 * </pre>
 *
 * <h3>Ejemplo de uso básico:</h3>
 * <pre>{@code
 * // Configurar mock con clave válida
 * JwtTestKeyGenerator keyGen = JwtTestKeyGenerator.createDefault();
 * MockJwksClient mockClient = MockJwksClient.withSingleValidKey(keyGen);
 *
 * // El sistema ahora puede "obtener" certificados públicos
 * JwksResponse jwks = mockClient.fetchJwks();
 * RSAPublicKey publicKey = extractPublicKey(jwks);
 *
 * // Validar token con certificado "obtenido"
 * Claims claims = Jwts.parser()
 *     .verifyWith(publicKey)
 *     .build()
 *     .parseSignedClaims(token)
 *     .getPayload();
 * }</pre>
 *
 * <h3>Escenarios de error simulados:</h3>
 * <pre>{@code
 * // Simular error de conectividad
 * mockClient.simulateNetworkError(true);
 *
 * // Simular timeout del servicio
 * mockClient.simulateTimeout(true);
 *
 * // Simular respuesta JWKS vacía
 * mockClient.simulateEmptyResponse(true);
 * }</pre>
 *
 * @author Sistema Cotizador - Equipo JWT Testing
 * @version 1.0
 * @since 2.0.0
 * @see JwtTestKeyGenerator
 * @see mx.com.qtx.cotizador.security.client.JwksClient
 */
public class MockJwksClient extends JwksClient {

    private static final Logger logger = LoggerFactory.getLogger(MockJwksClient.class);

    // Cache de respuestas JWKS por escenario
    private final Map<String, JwksResponse> responseCache = new ConcurrentHashMap<>();

    // Configuración de comportamiento
    private boolean simulateNetworkError = false;
    private boolean simulateTimeout = false;
    private boolean simulateEmptyResponse = false;
    private String activeScenario = "default";
    private long responseDelay = 0;

    /**
     * Constructor que hereda del JwksClient real pero no hace llamadas HTTP
     */
    public MockJwksClient() {
        super("http://mock-seguridad:8080", "/api/v1/seguridad", 5000);
        logger.info("MockJwksClient inicializado para testing JWT");
    }

    /**
     * Simula la obtención del JWKS según el escenario configurado
     */
    @Override
    public JwksResponse fetchJwks() {
        logger.debug("MockJwksClient.fetchJwks() - Escenario: {}", activeScenario);

        // Simular delay de red si está configurado
        if (responseDelay > 0) {
            try {
                Thread.sleep(responseDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during mock delay", e);
            }
        }

        // Simular errores de red
        if (simulateNetworkError) {
            logger.debug("Simulando error de red");
            throw new JwksClientException("Mock network error: Connection refused");
        }

        // Simular timeout
        if (simulateTimeout) {
            logger.debug("Simulando timeout");
            throw new JwksClientException("Mock timeout error: Request timeout");
        }

        // Simular respuesta vacía
        if (simulateEmptyResponse) {
            logger.debug("Simulando respuesta vacía");
            return new JwksResponse();
        }

        // Obtener respuesta del cache según el escenario
        JwksResponse response = responseCache.get(activeScenario);
        if (response == null) {
            logger.warn("No se encontró respuesta para escenario: {}", activeScenario);
            throw new JwksClientException("Mock error: No response configured for scenario: " + activeScenario);
        }

        logger.debug("Retornando respuesta mock con {} claves", response.getKeyCount());
        return response;
    }

    /**
     * Simula verificación de disponibilidad del servicio
     */
    @Override
    public boolean isServiceAvailable() {
        try {
            fetchJwks();
            return true;
        } catch (Exception e) {
            logger.debug("Service mock availability check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simula información de salud
     */
    @Override
    public HealthInfo getHealthInfo() {
        long startTime = System.currentTimeMillis();
        boolean available = false;
        String error = null;

        try {
            fetchJwks();
            available = true;
        } catch (Exception e) {
            error = "Mock error: " + e.getMessage();
        }

        long responseTime = System.currentTimeMillis() - startTime;
        return new HealthInfo(available, responseTime, "http://mock-jwks-url", error);
    }

    // === MÉTODOS DE CONFIGURACIÓN PARA TESTING ===

    /**
     * Configura una respuesta JWKS para un escenario específico
     */
    public MockJwksClient withJwksResponse(String scenario, JwksResponse response) {
        responseCache.put(scenario, response);
        logger.debug("Configurada respuesta JWKS para escenario: {}", scenario);
        return this;
    }

    /**
     * Configura la respuesta por defecto
     */
    public MockJwksClient withDefaultJwksResponse(JwksResponse response) {
        return withJwksResponse("default", response);
    }

    /**
     * Configura el escenario activo
     */
    public MockJwksClient useScenario(String scenario) {
        this.activeScenario = scenario;
        logger.debug("Cambiado a escenario: {}", scenario);
        return this;
    }

    /**
     * Simula error de red en la próxima llamada
     */
    public MockJwksClient simulateNetworkError(boolean enable) {
        this.simulateNetworkError = enable;
        logger.debug("Simulación de error de red: {}", enable);
        return this;
    }

    /**
     * Simula timeout en la próxima llamada
     */
    public MockJwksClient simulateTimeout(boolean enable) {
        this.simulateTimeout = enable;
        logger.debug("Simulación de timeout: {}", enable);
        return this;
    }

    /**
     * Simula respuesta vacía
     */
    public MockJwksClient simulateEmptyResponse(boolean enable) {
        this.simulateEmptyResponse = enable;
        logger.debug("Simulación de respuesta vacía: {}", enable);
        return this;
    }

    /**
     * Configura delay de respuesta (en ms)
     */
    public MockJwksClient withResponseDelay(long delayMs) {
        this.responseDelay = delayMs;
        logger.debug("Configurado delay de respuesta: {}ms", delayMs);
        return this;
    }

    /**
     * Reset de todos los estados de simulación
     */
    public MockJwksClient reset() {
        simulateNetworkError = false;
        simulateTimeout = false;
        simulateEmptyResponse = false;
        responseDelay = 0;
        activeScenario = "default";
        logger.debug("MockJwksClient reseteado a estado inicial");
        return this;
    }

    /**
     * Limpia el cache de respuestas
     */
    public MockJwksClient clearCache() {
        responseCache.clear();
        logger.debug("Cache de respuestas limpiado");
        return this;
    }

    // === MÉTODOS DE UTILIDAD PARA ESCENARIOS COMUNES ===

    /**
     * Configura un escenario con clave única válida
     */
    public static MockJwksClient withSingleValidKey(JwtTestKeyGenerator keyGenerator) {
        MockJwksClient mock = new MockJwksClient();
        mock.withDefaultJwksResponse(keyGenerator.toJwksResponse());
        return mock;
    }

    /**
     * Configura un escenario con múltiples claves (rotación)
     */
    public static MockJwksClient withMultipleKeys(JwtTestKeyGenerator... keyGenerators) {
        MockJwksClient mock = new MockJwksClient();
        mock.withDefaultJwksResponse(JwtTestKeyGenerator.createMultiKeyJwksResponse(keyGenerators));
        return mock;
    }

    /**
     * Configura un escenario de error de red
     */
    public static MockJwksClient withNetworkError() {
        return new MockJwksClient().simulateNetworkError(true);
    }

    /**
     * Configura un escenario de respuesta vacía
     */
    public static MockJwksClient withEmptyResponse() {
        return new MockJwksClient().simulateEmptyResponse(true);
    }
}