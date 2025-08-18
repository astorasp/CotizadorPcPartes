package mx.com.qtx.cotizador.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.entidad.Componente;

import java.util.List;

/**
 * Cliente REST para comunicación con el microservicio de componentes
 */
@Component
public class ComponenteClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ComponenteClient.class);
    
    private final RestTemplate restTemplate;
    private final String componenteBaseUrl;
    
    public ComponenteClient(RestTemplate restTemplate, 
                           @Value("${microservices.componente.base-url:http://localhost:8082}") String componenteBaseUrl) {
        this.restTemplate = restTemplate;
        this.componenteBaseUrl = componenteBaseUrl;
    }
    
    /**
     * Busca un componente por ID en el microservicio de componentes
     * 
     * @param componenteId ID del componente
     * @return ApiResponse con el componente encontrado
     */
    public ApiResponse<Componente> buscarComponentePorId(Integer componenteId) {
        try {
            logger.info("Buscando componente con ID: {} en {}", componenteId, componenteBaseUrl);
            
            String url = componenteBaseUrl + "/componentes/v1/api/componentes/" + componenteId;
            
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
                logger.info("Componente encontrado exitosamente: {}", componenteId);
                return response.getBody();
            } else {
                logger.warn("Componente no encontrado: {}", componenteId);
                return new ApiResponse<>("404", "Componente no encontrado");
            }
            
        } catch (Exception e) {
            logger.error("Error al buscar componente {}: {}", componenteId, e.getMessage(), e);
            return new ApiResponse<>("500", "Error al comunicarse con el servicio de componentes: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los componentes desde el microservicio de componentes
     * 
     * @return ApiResponse con la lista de componentes
     */
    public ApiResponse<List<Componente>> obtenerTodosLosComponentes() {
        try {
            logger.info("Obteniendo todos los componentes desde {}", componenteBaseUrl);
            
            String url = componenteBaseUrl + "/componentes/v1/api/componentes";
            
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
                logger.info("Componentes obtenidos exitosamente");
                return response.getBody();
            } else {
                logger.warn("No se pudieron obtener los componentes");
                return new ApiResponse<>("404", "No se pudieron obtener los componentes");
            }
            
        } catch (Exception e) {
            logger.error("Error al obtener componentes: {}", e.getMessage(), e);
            return new ApiResponse<>("500", "Error al comunicarse con el servicio de componentes: " + e.getMessage());
        }
    }
}