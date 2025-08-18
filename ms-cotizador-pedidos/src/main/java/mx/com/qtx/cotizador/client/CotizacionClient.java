package mx.com.qtx.cotizador.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.entidad.Cotizacion;

/**
 * Cliente REST para comunicación con el microservicio de cotizaciones
 */
@Component
public class CotizacionClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CotizacionClient.class);
    
    private final RestTemplate restTemplate;
    private final String cotizacionBaseUrl;
    
    public CotizacionClient(RestTemplate restTemplate, 
                           @Value("${microservices.cotizacion.base-url:http://localhost:8083}") String cotizacionBaseUrl) {
        this.restTemplate = restTemplate;
        this.cotizacionBaseUrl = cotizacionBaseUrl;
    }
    
    /**
     * Busca una cotización por ID en el microservicio de cotizaciones
     * 
     * @param cotizacionId ID de la cotización
     * @return ApiResponse con la cotización encontrada
     */
    public ApiResponse<Cotizacion> buscarCotizacionPorId(Integer cotizacionId) {
        try {
            logger.info("Buscando cotización con ID: {} en {}", cotizacionId, cotizacionBaseUrl);
            
            String url = cotizacionBaseUrl + "/cotizaciones/v1/api/cotizaciones/" + cotizacionId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                ApiResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Cotización encontrada exitosamente: {}", cotizacionId);
                return response.getBody();
            } else {
                logger.warn("Cotización no encontrada: {}", cotizacionId);
                return new ApiResponse<>("404", "Cotización no encontrada");
            }
            
        } catch (Exception e) {
            logger.error("Error al buscar cotización {}: {}", cotizacionId, e.getMessage(), e);
            return new ApiResponse<>("500", "Error al comunicarse con el servicio de cotizaciones: " + e.getMessage());
        }
    }
}