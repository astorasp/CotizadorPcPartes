package mx.com.qtx.cotizador.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración de RestTemplate para comunicación síncrona entre microservicios en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase configura el cliente HTTP RestTemplate utilizado para realizar llamadas síncronas
 * a otros microservicios del sistema, específicamente desde el microservicio de pedidos hacia:
 * </p>
 *
 * <h3>Microservicios Consumidos:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Para obtener detalles de cotizaciones al generar pedidos</li>
 *   <li><strong>ms-cotizador-componentes:</strong> Para validar existencia y precios de componentes</li>
 *   <li><strong>ms-seguridad:</strong> Para validación de sesiones y obtención de claves JWT</li>
 * </ul>
 *
 * <h3>Configuraciones de Timeout:</h3>
 * <ul>
 *   <li><strong>Connect Timeout:</strong> 10 segundos - Tiempo máximo para establecer conexión</li>
 *   <li><strong>Read Timeout:</strong> 30 segundos - Tiempo máximo para recibir respuesta completa</li>
 * </ul>
 *
 * <h3>Headers por Defecto:</h3>
 * <ul>
 *   <li><strong>Content-Type:</strong> application/json</li>
 *   <li><strong>Accept:</strong> application/json</li>
 * </ul>
 *
 * <h3>Uso Principal:</h3>
 * <p>
 * Este RestTemplate se utiliza principalmente en:
 * </p>
 * <ul>
 *   <li>{@link mx.com.qtx.cotizador.servicio.pedido.PedidoServicio#generarPedidoDesdeCotizacion} - Para validar cotizaciones</li>
 *   <li>{@link mx.com.qtx.cotizador.security.client.JwksClient} - Para obtener claves JWKS</li>
 *   <li>{@link mx.com.qtx.cotizador.security.client.SessionValidationClient} - Para validar sesiones</li>
 * </ul>
 *
 * @see RestTemplateBuilder Para construcción fluida del RestTemplate
 * @see mx.com.qtx.cotizador.servicio.pedido.PedidoServicio
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    @Bean
    public HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}