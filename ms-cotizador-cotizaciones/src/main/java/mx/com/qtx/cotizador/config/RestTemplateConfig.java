package mx.com.qtx.cotizador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para RestTemplate.
 * Proporciona la instancia de RestTemplate para comunicación entre microservicios.
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Bean para RestTemplate con configuración por defecto
     * 
     * @return RestTemplate configurado
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}