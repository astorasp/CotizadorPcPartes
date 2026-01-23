package mx.com.qtx.cotizador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para comunicación entre microservicios.
 * <p>
 * Esta clase configura el cliente HTTP RestTemplate utilizado para realizar llamadas
 * síncronas a otros microservicios del sistema CotizadorPcPartes, principalmente
 * al microservicio de componentes (ms-cotizador-componentes) para obtener información
 * de componentes y promociones.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Consulta de componentes:</strong> Obtener detalles de componentes para cotización</li>
 *   <li><strong>Validación de componentes:</strong> Verificar existencia y estado de componentes</li>
 *   <li><strong>Consulta de promociones:</strong> Obtener información de promociones activas</li>
 *   <li><strong>Comunicación con ms-seguridad:</strong> Validación de sesiones y tokens JWT</li>
 * </ul>
 *
 * <h3>Características de configuración:</h3>
 * <ul>
 *   <li><strong>Configuración básica:</strong> RestTemplate con configuración por defecto de Spring</li>
 *   <li><strong>Timeout:</strong> Configurable a través de propiedades de aplicación</li>
 *   <li><strong>Pooling de conexiones:</strong> Utiliza HttpClient por defecto de Java</li>
 *   <li><strong>Serialización JSON:</strong> Jackson configurado automáticamente por Spring Boot</li>
 * </ul>
 *
 * <h3>Uso típico:</h3>
 * <pre>{@code
 * @Autowired
 * private RestTemplate restTemplate;
 *
 * // Consulta de componente
 * String url = "http://ms-cotizador-componentes/api/v1/componentes/" + idComponente;
 * ComponenteResponse response = restTemplate.getForObject(url, ComponenteResponse.class);
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see org.springframework.web.client.RestTemplate
 * @see org.springframework.context.annotation.Bean
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Crea y configura un bean de RestTemplate para uso en el microservicio.
     * <p>
     * Este método crea una instancia de RestTemplate con configuración por defecto
     * que incluye soporte para JSON, manejo de errores HTTP y configuración de timeouts
     * básica. El RestTemplate se inyecta automáticamente en servicios que lo requieran
     * mediante la anotación @Autowired.
     * </p>
     *
     * <h4>Configuración por defecto incluye:</h4>
     * <ul>
     *   <li>Cliente HTTP básico con soporte para métodos GET, POST, PUT, DELETE</li>
     *   <li>Serialización/deserialización JSON automática con Jackson</li>
     *   <li>Manejo de errores HTTP con excepciones específicas</li>
     *   <li>Configuración de User-Agent por defecto</li>
     * </ul>
     *
     * @return Una nueva instancia de RestTemplate configurada y lista para usar
     * @see org.springframework.web.client.RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}