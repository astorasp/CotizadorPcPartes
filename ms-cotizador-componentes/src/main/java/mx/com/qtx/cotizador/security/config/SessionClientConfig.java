package mx.com.qtx.cotizador.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Collections;

/**
 * Configuración para el cliente de validación de sesiones
 * Incluye configuración de retry, circuit breaker y scheduling
 */
@Configuration
@EnableRetry
@EnableScheduling
public class SessionClientConfig {

    /**
     * Configuración del RetryTemplate para operaciones de sesión
     */
    @Bean
    public RetryTemplate sessionRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Política de reintentos
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, 
                Collections.singletonMap(WebClientException.class, true));
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Política de backoff
        FixedBackOffPolicy backoffPolicy = new FixedBackOffPolicy();
        backoffPolicy.setBackOffPeriod(1000); // 1 segundo
        retryTemplate.setBackOffPolicy(backoffPolicy);
        
        return retryTemplate;
    }
}