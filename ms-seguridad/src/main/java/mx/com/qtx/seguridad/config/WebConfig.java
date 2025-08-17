package mx.com.qtx.seguridad.config;

import mx.com.qtx.seguridad.security.RateLimitingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Web MVC para registrar interceptors y configuraciones adicionales
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    public WebConfig(RateLimitingInterceptor rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar rate limiting interceptor para endpoints JWKS
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/keys/**") // Aplicar a todos los endpoints de keys
                .order(1); // Ejecutar temprano en la cadena
    }
}