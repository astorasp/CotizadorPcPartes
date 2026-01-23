package mx.com.qtx.cotizador.security.client;

import mx.com.qtx.cotizador.security.dto.SessionInfo;
import mx.com.qtx.cotizador.security.dto.SessionInfoResponse;
import mx.com.qtx.cotizador.security.dto.SessionValidationResponse;
import mx.com.qtx.cotizador.security.dto.SessionCloseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Optional;

/**
 * Cliente para comunicación con ms-seguridad para validación de sesiones
 * Implementa retry logic, circuit breaker y manejo de errores para alta disponibilidad
 */
@Component
@Profile({"default", "docker"})
public class SessionValidationClient {

    private static final Logger logger = LoggerFactory.getLogger(SessionValidationClient.class);

    private final WebClient webClient;
    private final String baseUrl;
    private final String contextPath;
    private final Duration timeout;
    private final Duration connectTimeout;

    // URLs de endpoints específicos
    private final String validateSessionUrl;
    private final String getSessionInfoUrl;
    private final String closeSessionUrl;

    public SessionValidationClient(
            @Value("${jwt.ms-seguridad.base-url}") String baseUrl,
            @Value("${jwt.ms-seguridad.context-path}") String contextPath,
            @Value("${jwt.ms-seguridad.timeout:15000}") int timeoutMs,
            @Value("${jwt.ms-seguridad.connect-timeout:5000}") int connectTimeoutMs) {
        
        this.baseUrl = baseUrl;
        this.contextPath = contextPath;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.connectTimeout = Duration.ofMillis(connectTimeoutMs);
        
        // Construir URLs de endpoints
        this.validateSessionUrl = baseUrl + contextPath + "/session/validate";
        this.getSessionInfoUrl = baseUrl + contextPath + "/session/info";
        this.closeSessionUrl = baseUrl + contextPath + "/session/close";
        
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB max
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "ms-cotizador/1.0")
                .defaultHeader("X-Service", "ms-cotizador")
                .build();
        
        logger.info("SessionValidationClient inicializado con base URL: {}, context path: {}, timeout: {}ms, connect timeout: {}ms", 
                   baseUrl, contextPath, timeoutMs, connectTimeoutMs);
    }

    /**
     * Valida si una sesión está activa
     * 
     * @param sessionId ID de la sesión a validar
     * @return true si la sesión está activa, false en caso contrario
     */
    @Retryable(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @CircuitBreaker(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        openTimeout = 5000,
        resetTimeout = 30000
    )
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warn("Session ID es null o vacío");
            return false;
        }

        logger.debug("Validando sesión: {}", sessionId);
        
        try {
            SessionValidationResponse response = webClient
                    .get()
                    .uri(validateSessionUrl + "/{sessionId}", sessionId)
                    .retrieve()
                    .bodyToMono(SessionValidationResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null) {
                logger.warn("Respuesta null al validar sesión: {}", sessionId);
                return false;
            }

            boolean isValid = response.isValid();
            logger.debug("Sesión {} validada: {}", sessionId, isValid);
            return isValid;

        } catch (WebClientResponseException e) {
            logger.error("Error HTTP al validar sesión {}: {} - {}", sessionId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (WebClientException e) {
            logger.error("Error de comunicación al validar sesión {}: {}", sessionId, e.getMessage());
            throw new SessionValidationException("Error de comunicación con ms-seguridad", e);
        } catch (Exception e) {
            logger.error("Error inesperado al validar sesión {}: {}", sessionId, e.getMessage(), e);
            throw new SessionValidationException("Error inesperado al validar sesión", e);
        }
    }

    /**
     * Obtiene información detallada de una sesión
     * 
     * @param sessionId ID de la sesión
     * @return Optional con información de la sesión si existe
     */
    @Retryable(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @CircuitBreaker(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        openTimeout = 5000,
        resetTimeout = 30000
    )
    public Optional<SessionInfo> getSessionInfo(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warn("Session ID es null o vacío");
            return Optional.empty();
        }

        logger.debug("Obteniendo información de sesión: {}", sessionId);
        
        try {
            SessionInfoResponse response = webClient
                    .get()
                    .uri(getSessionInfoUrl + "/{sessionId}", sessionId)
                    .retrieve()
                    .bodyToMono(SessionInfoResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null) {
                logger.warn("Respuesta null al obtener información de sesión: {}", sessionId);
                return Optional.empty();
            }

            if (response.isSuccessful()) {
                SessionInfo sessionInfo = response.toSessionInfo();
                logger.debug("Información de sesión {} obtenida exitosamente", sessionId);
                return Optional.of(sessionInfo);
            } else {
                logger.warn("Error al obtener información de sesión {}: {}", sessionId, response.getMessage());
                return Optional.empty();
            }

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.debug("Sesión no encontrada: {}", sessionId);
                return Optional.empty();
            }
            logger.error("Error HTTP al obtener información de sesión {}: {} - {}", sessionId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new SessionValidationException("Error HTTP al obtener información de sesión", e);
        } catch (WebClientException e) {
            logger.error("Error de comunicación al obtener información de sesión {}: {}", sessionId, e.getMessage());
            throw new SessionValidationException("Error de comunicación con ms-seguridad", e);
        } catch (Exception e) {
            logger.error("Error inesperado al obtener información de sesión {}: {}", sessionId, e.getMessage(), e);
            throw new SessionValidationException("Error inesperado al obtener información de sesión", e);
        }
    }

    /**
     * Cierra una sesión
     * 
     * @param sessionId ID de la sesión a cerrar
     * @return true si la sesión fue cerrada exitosamente
     */
    @Retryable(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @CircuitBreaker(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        openTimeout = 5000,
        resetTimeout = 30000
    )
    public boolean closeSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warn("Session ID es null o vacío");
            return false;
        }

        logger.debug("Cerrando sesión: {}", sessionId);
        
        try {
            SessionCloseResponse response = webClient
                    .post()
                    .uri(closeSessionUrl + "/{sessionId}", sessionId)
                    .retrieve()
                    .bodyToMono(SessionCloseResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null) {
                logger.warn("Respuesta null al cerrar sesión: {}", sessionId);
                return false;
            }

            boolean closed = response.isSuccessful();
            logger.debug("Sesión {} cerrada: {}", sessionId, closed);
            return closed;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.debug("Sesión no encontrada al cerrar: {}", sessionId);
                return false;
            }
            logger.error("Error HTTP al cerrar sesión {}: {} - {}", sessionId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (WebClientException e) {
            logger.error("Error de comunicación al cerrar sesión {}: {}", sessionId, e.getMessage());
            throw new SessionValidationException("Error de comunicación con ms-seguridad", e);
        } catch (Exception e) {
            logger.error("Error inesperado al cerrar sesión {}: {}", sessionId, e.getMessage(), e);
            throw new SessionValidationException("Error inesperado al cerrar sesión", e);
        }
    }

    /**
     * Verifica la conectividad con ms-seguridad
     * 
     * @return true si ms-seguridad está disponible
     */
    public boolean isServiceAvailable() {
        try {
            // Usar un sessionId ficticio para probar conectividad
            validateSession("health-check");
            return true;
        } catch (Exception e) {
            logger.warn("ms-seguridad no disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información de conectividad para health checks
     */
    public HealthInfo getHealthInfo() {
        long startTime = System.currentTimeMillis();
        boolean available = false;
        String error = null;
        
        try {
            // Intentar validar una sesión ficticia para verificar conectividad
            validateSession("health-check");
            available = true;
        } catch (Exception e) {
            error = e.getMessage();
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        return new HealthInfo(available, responseTime, baseUrl + contextPath, error);
    }

    /**
     * Excepción específica para errores del cliente de validación de sesiones
     */
    public static class SessionValidationException extends RuntimeException {
        public SessionValidationException(String message) {
            super(message);
        }

        public SessionValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Información de salud del cliente de validación de sesiones
     */
    public static class HealthInfo {
        private final boolean available;
        private final long responseTimeMs;
        private final String baseUrl;
        private final String error;

        public HealthInfo(boolean available, long responseTimeMs, String baseUrl, String error) {
            this.available = available;
            this.responseTimeMs = responseTimeMs;
            this.baseUrl = baseUrl;
            this.error = error;
        }

        public boolean isAvailable() {
            return available;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "HealthInfo{" +
                    "available=" + available +
                    ", responseTimeMs=" + responseTimeMs +
                    ", baseUrl='" + baseUrl + '\'' +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}