package mx.com.qtx.cotizador.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

/**
 * Cliente para comunicación con el microservicio de componentes.
 * Maneja las peticiones HTTP para obtener información de componentes.
 */
@Component
public class ComponenteClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ComponenteClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${microservices.cotizador-componentes.base-url:http://localhost:8082}")
    private String componentesBaseUrl;
    
    public ComponenteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Busca un componente por su ID
     * 
     * @param idComponente ID del componente a buscar
     * @return ApiResponse con el componente encontrado o error
     */
    @SuppressWarnings("unchecked")
    public ApiResponse<ComponenteResponse> buscarComponente(String idComponente) {
        try {
            String url = componentesBaseUrl + "/componentes/" + idComponente;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.info("Consultando componente en: {}", url);
            
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, ApiResponse.class);
            
            if (response.getBody() != null) {
                ApiResponse<ComponenteResponse> apiResponse = response.getBody();
                logger.info("Respuesta del microservicio de componentes: código={}, mensaje={}", 
                           apiResponse.getCodigo(), apiResponse.getMensaje());
                return apiResponse;
            }
            
            return new ApiResponse<>("ERROR_INTERNO", "No se pudo obtener respuesta del servicio de componentes");
            
        } catch (Exception e) {
            logger.error("Error al consultar componente {}: {}", idComponente, e.getMessage(), e);
            return new ApiResponse<>("ERROR_CONEXION", 
                                   "Error de conexión con el servicio de componentes: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si un componente existe
     * 
     * @param idComponente ID del componente a verificar
     * @return true si el componente existe, false en caso contrario
     */
    public boolean existeComponente(String idComponente) {
        try {
            ApiResponse<ComponenteResponse> response = buscarComponente(idComponente);
            return "OK".equals(response.getCodigo()) && response.getDatos() != null;
        } catch (Exception e) {
            logger.error("Error al verificar existencia del componente {}: {}", idComponente, e.getMessage());
            return false;
        }
    }
}