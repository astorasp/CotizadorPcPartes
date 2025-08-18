package mx.com.qtx.cotizador.controlador;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.servicio.cache.ComponenteCacheService;
import mx.com.qtx.cotizador.servicio.cache.PromocionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para monitoreo y administración del cache local.
 * 
 * Proporciona endpoints para verificar el estado del cache,
 * obtener estadísticas y realizar operaciones de limpieza.
 * 
 * @author Subagente3F - [2025-01-17 20:10:00 MST] - Controlador de monitoreo de cache
 */
@RestController
@RequestMapping("/cache")
public class CacheMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(CacheMonitorController.class);

    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private ComponenteCacheService componenteCacheService;
    
    @Autowired
    private PromocionCacheService promocionCacheService;

    /**
     * Obtiene estadísticas del cache de componentes.
     * Solo accesible para administradores.
     */
    @GetMapping("/stats/componentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComponentesCacheStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Obtener cache de componentes
            var componentesCache = cacheManager.getCache("componentes");
            if (componentesCache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) componentesCache;
                CacheStats cacheStats = caffeineCache.getNativeCache().stats();
                
                stats.put("requestCount", cacheStats.requestCount());
                stats.put("hitCount", cacheStats.hitCount());
                stats.put("hitRate", cacheStats.hitRate());
                stats.put("missCount", cacheStats.missCount());
                stats.put("missRate", cacheStats.missRate());
                stats.put("loadCount", cacheStats.loadCount());
                stats.put("evictionCount", cacheStats.evictionCount());
            }
            
            // Obtener cache de existencia de componentes
            var existenciaCache = cacheManager.getCache("componentes-existencia");
            if (existenciaCache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) existenciaCache;
                CacheStats cacheStats = caffeineCache.getNativeCache().stats();
                
                Map<String, Object> existenciaStats = new HashMap<>();
                existenciaStats.put("requestCount", cacheStats.requestCount());
                existenciaStats.put("hitCount", cacheStats.hitCount());
                existenciaStats.put("hitRate", cacheStats.hitRate());
                existenciaStats.put("missCount", cacheStats.missCount());
                existenciaStats.put("missRate", cacheStats.missRate());
                
                stats.put("existenciaCache", existenciaStats);
            }
            
            logger.info("Estadísticas de cache de componentes solicitadas");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Estadísticas de cache obtenidas", stats));
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de cache de componentes: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error obteniendo estadísticas de cache"));
        }
    }

    /**
     * Obtiene estadísticas del cache de promociones.
     * Solo accesible para administradores.
     */
    @GetMapping("/stats/promociones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPromocionesCacheStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            var promocionesCache = cacheManager.getCache("promociones");
            if (promocionesCache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) promocionesCache;
                CacheStats cacheStats = caffeineCache.getNativeCache().stats();
                
                stats.put("requestCount", cacheStats.requestCount());
                stats.put("hitCount", cacheStats.hitCount());
                stats.put("hitRate", cacheStats.hitRate());
                stats.put("missCount", cacheStats.missCount());
                stats.put("missRate", cacheStats.missRate());
                stats.put("loadCount", cacheStats.loadCount());
                stats.put("evictionCount", cacheStats.evictionCount());
            }
            
            logger.info("Estadísticas de cache de promociones solicitadas");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Estadísticas de cache obtenidas", stats));
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de cache de promociones: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error obteniendo estadísticas de cache"));
        }
    }

    /**
     * Obtiene un resumen general de todos los caches.
     * Solo accesible para administradores.
     */
    @GetMapping("/stats/general")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGeneralCacheStats() {
        try {
            Map<String, Object> generalStats = new HashMap<>();
            
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache instanceof CaffeineCache) {
                    CaffeineCache caffeineCache = (CaffeineCache) cache;
                    CacheStats stats = caffeineCache.getNativeCache().stats();
                    
                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("requestCount", stats.requestCount());
                    cacheInfo.put("hitRate", stats.hitRate());
                    cacheInfo.put("missCount", stats.missCount());
                    cacheInfo.put("evictionCount", stats.evictionCount());
                    
                    generalStats.put(cacheName, cacheInfo);
                }
            }
            
            logger.info("Estadísticas generales de cache solicitadas");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Estadísticas generales obtenidas", generalStats));
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas generales de cache: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error obteniendo estadísticas generales"));
        }
    }

    /**
     * Invalida el cache de componentes.
     * Solo accesible para administradores.
     */
    @DeleteMapping("/componentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> invalidarCacheComponentes() {
        try {
            componenteCacheService.invalidarTodoElCache();
            
            logger.info("Cache de componentes invalidado por administrador");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Cache de componentes invalidado exitosamente"));
            
        } catch (Exception e) {
            logger.error("Error invalidando cache de componentes: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error invalidando cache de componentes"));
        }
    }

    /**
     * Invalida el cache de promociones.
     * Solo accesible para administradores.
     */
    @DeleteMapping("/promociones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> invalidarCachePromociones() {
        try {
            promocionCacheService.invalidarTodoElCache();
            
            logger.info("Cache de promociones invalidado por administrador");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Cache de promociones invalidado exitosamente"));
            
        } catch (Exception e) {
            logger.error("Error invalidando cache de promociones: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error invalidando cache de promociones"));
        }
    }

    /**
     * Invalida todos los caches.
     * Solo accesible para administradores.
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> invalidarTodosLosCaches() {
        try {
            componenteCacheService.invalidarTodoElCache();
            promocionCacheService.invalidarTodoElCache();
            
            logger.warn("Todos los caches invalidados por administrador");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Todos los caches invalidados exitosamente"));
            
        } catch (Exception e) {
            logger.error("Error invalidando todos los caches: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error invalidando todos los caches"));
        }
    }

    /**
     * Pre-carga componentes frecuentemente usados en cache.
     * Solo accesible para administradores.
     */
    @PostMapping("/componentes/precarga")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> precargarComponentesFrecuentes(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            var idsComponentes = (java.util.List<String>) request.get("idsComponentes");
            
            if (idsComponentes == null || idsComponentes.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("CAMPO_REQUERIDO", "La lista de IDs de componentes es requerida"));
            }
            
            for (String idComponente : idsComponentes) {
                componenteCacheService.precargarComponente(idComponente);
            }
            
            logger.info("Pre-carga de {} componentes completada por administrador", idsComponentes.size());
            return ResponseEntity.ok(new ApiResponse<>("OK", 
                String.format("Pre-carga de %d componentes completada", idsComponentes.size())));
            
        } catch (Exception e) {
            logger.error("Error en pre-carga de componentes: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error en pre-carga de componentes"));
        }
    }

    /**
     * Pre-carga promociones activas en cache.
     * Solo accesible para administradores.
     */
    @PostMapping("/promociones/precarga")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> precargarPromocionesActivas() {
        try {
            promocionCacheService.precargarPromocionesActivas();
            
            logger.info("Pre-carga de promociones activas completada por administrador");
            return ResponseEntity.ok(new ApiResponse<>("OK", "Pre-carga de promociones activas completada"));
            
        } catch (Exception e) {
            logger.error("Error en pre-carga de promociones: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ApiResponse<>("ERROR_INTERNO", "Error en pre-carga de promociones"));
        }
    }
}