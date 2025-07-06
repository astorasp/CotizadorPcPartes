package mx.com.qtx.cotizador.security.client;

import mx.com.qtx.cotizador.security.dto.JwksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;

/**
 * Cliente para comunicación con ms-seguridad y obtención de JWKS
 * Implementa retry logic y manejo de errores para alta disponibilidad
 */
@Component
@Profile({"default", "docker"})
public class JwksClient {

    private static final Logger logger = LoggerFactory.getLogger(JwksClient.class);

    private final WebClient webClient;
    private final String jwksUrl;
    private final Duration timeout;

    public JwksClient(
            @Value("${jwt.ms-seguridad.base-url}") String baseUrl,
            @Value("${jwt.ms-seguridad.context-path}") String contextPath,
            @Value("${jwt.ms-seguridad.timeout:15000}") int timeoutMs) {
        this.jwksUrl = baseUrl + contextPath + "/keys/jwks";
        this.timeout = Duration.ofMillis(timeoutMs);
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB max
                .build();
        
        logger.info("JwksClient inicializado con URL: {} y timeout: {}ms", this.jwksUrl, timeoutMs);
    }

    /**
     * Obtiene el JWKS desde ms-seguridad con retry automático
     * 
     * @return JwksResponse con las claves públicas
     * @throws JwksClientException si no se puede obtener el JWKS después de reintentos
     */
    @Retryable(
        retryFor = {WebClientException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public JwksResponse fetchJwks() {
        logger.debug("Obteniendo JWKS desde: {}", jwksUrl);
        
        try {
            JwksResponse response = webClient
                    .get()
                    .uri(jwksUrl)
                    .retrieve()
                    .bodyToMono(JwksResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null || !response.hasValidKeys()) {
                throw new JwksClientException("JWKS response inválido o vacío desde: " + jwksUrl);
            }

            logger.debug("JWKS obtenido exitosamente. Claves disponibles: {}", response.getKeyCount());
            return response;

        } catch (WebClientException e) {
            logger.error("Error de comunicación al obtener JWKS desde {}: {}", jwksUrl, e.getMessage());
            throw new JwksClientException("Error de comunicación con ms-seguridad", e);
        } catch (Exception e) {
            logger.error("Error inesperado al obtener JWKS desde {}: {}", jwksUrl, e.getMessage(), e);
            throw new JwksClientException("Error inesperado al obtener JWKS", e);
        }
    }

    /**
     * Verifica la conectividad con ms-seguridad
     * 
     * @return true si ms-seguridad está disponible
     */
    public boolean isServiceAvailable() {
        try {
            fetchJwks();
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
            fetchJwks();
            available = true;
        } catch (Exception e) {
            error = e.getMessage();
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        return new HealthInfo(available, responseTime, jwksUrl, error);
    }

    /**
     * Excepción específica para errores del cliente JWKS
     */
    public static class JwksClientException extends RuntimeException {
        public JwksClientException(String message) {
            super(message);
        }

        public JwksClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Información de salud del cliente JWKS
     */
    public static class HealthInfo {
        private final boolean available;
        private final long responseTimeMs;
        private final String url;
        private final String error;

        public HealthInfo(boolean available, long responseTimeMs, String url, String error) {
            this.available = available;
            this.responseTimeMs = responseTimeMs;
            this.url = url;
            this.error = error;
        }

        public boolean isAvailable() {
            return available;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public String getUrl() {
            return url;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "HealthInfo{" +
                    "available=" + available +
                    ", responseTimeMs=" + responseTimeMs +
                    ", url='" + url + '\'' +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}