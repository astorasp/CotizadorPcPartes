package mx.com.qtx.cotizador.servicio.cache;

import mx.com.qtx.cotizador.client.ComponenteClient;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Servicio de cache para componentes con fallback a base de datos.
 * 
 * Proporciona acceso en cache a información de componentes obtenida
 * desde ms-cotizador-componentes para mejorar el rendimiento de
 * los algoritmos de cotización.
 * 
 * @author Subagente3F - [2025-01-17 19:35:00 MST] - Servicio de cache para componentes
 */
@Service
public class ComponenteCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ComponenteCacheService.class);
    
    @Autowired
    private ComponenteClient componenteClient;

    /**
     * Busca un componente por ID utilizando cache.
     * Si no está en cache, consulta el microservicio y almacena el resultado.
     * 
     * @param idComponente ID del componente a buscar
     * @return ApiResponse con el componente encontrado o error
     */
    @Cacheable(value = "componentes", key = "#idComponente", unless = "#result.codigo != 'OK'")
    public ApiResponse<ComponenteResponse> buscarComponente(String idComponente) {
        logger.info("Consultando componente desde microservicio (cache miss): {}", idComponente);
        
        try {
            ApiResponse<ComponenteResponse> response = componenteClient.buscarComponente(idComponente);
            
            if ("OK".equals(response.getCodigo())) {
                logger.debug("Componente cacheado exitosamente: id={}, nombre={}", 
                           idComponente, response.getDatos() != null ? response.getDatos().getNombre() : "N/A");
            } else {
                logger.warn("Componente no encontrado o error: id={}, código={}, mensaje={}", 
                          idComponente, response.getCodigo(), response.getMensaje());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error consultando componente {}: {}", idComponente, e.getMessage(), e);
            return new ApiResponse<>("ERROR_CACHE", 
                                   "Error consultando componente desde cache: " + e.getMessage());
        }
    }

    /**
     * Verifica si un componente existe utilizando cache optimizado.
     * Utiliza un cache separado más liviano para validaciones de existencia.
     * 
     * @param idComponente ID del componente a verificar
     * @return true si el componente existe, false en caso contrario
     */
    @Cacheable(value = "componentes-existencia", key = "#idComponente")
    public boolean existeComponente(String idComponente) {
        logger.debug("Verificando existencia de componente desde cache: {}", idComponente);
        
        try {
            // Primero intentar obtener desde cache de componentes completos
            ApiResponse<ComponenteResponse> cachedResponse = buscarComponente(idComponente);
            boolean existe = "OK".equals(cachedResponse.getCodigo()) && cachedResponse.getDatos() != null;
            
            logger.debug("Existencia de componente {}: {}", idComponente, existe);
            return existe;
            
        } catch (Exception e) {
            logger.error("Error verificando existencia del componente {}: {}", idComponente, e.getMessage());
            return false;
        }
    }

    /**
     * Invalida un componente específico del cache.
     * Usado cuando se recibe un evento Kafka de cambio de componente.
     * 
     * @param idComponente ID del componente a invalidar
     */
    @CacheEvict(value = {"componentes", "componentes-existencia"}, key = "#idComponente")
    public void invalidarComponente(String idComponente) {
        logger.info("Invalidando cache de componente: {}", idComponente);
    }

    /**
     * Invalida todo el cache de componentes.
     * Usado en casos de sincronización masiva o errores.
     */
    @CacheEvict(value = {"componentes", "componentes-existencia"}, allEntries = true)
    public void invalidarTodoElCache() {
        logger.warn("Invalidando todo el cache de componentes");
    }

    /**
     * Pre-carga un componente en el cache.
     * Útil para pre-cargar componentes frecuentemente usados.
     * 
     * @param idComponente ID del componente a pre-cargar
     */
    public void precargarComponente(String idComponente) {
        logger.debug("Pre-cargando componente en cache: {}", idComponente);
        try {
            buscarComponente(idComponente);
        } catch (Exception e) {
            logger.error("Error pre-cargando componente {}: {}", idComponente, e.getMessage());
        }
    }
}