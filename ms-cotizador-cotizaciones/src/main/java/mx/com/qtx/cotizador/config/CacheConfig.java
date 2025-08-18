package mx.com.qtx.cotizador.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de cache local para ms-cotizador-cotizaciones.
 * 
 * Utiliza Caffeine como proveedor de cache con TTL configurables
 * para mejorar el rendimiento de cotizaciones evitando llamadas
 * frecuentes a ms-cotizador-componentes.
 * 
 * @author Subagente3F - [2025-01-17 19:30:00 MST] - Configuración de cache local
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.componentes.ttl-minutes:15}")
    private int componentesTtlMinutes;
    
    @Value("${cache.promociones.ttl-minutes:10}")
    private int promocionesTtlMinutes;
    
    @Value("${cache.componentes.max-size:1000}")
    private int componentesMaxSize;
    
    @Value("${cache.promociones.max-size:500}")
    private int promocionesMaxSize;

    /**
     * Configuración del cache manager con diferentes TTL por tipo de dato.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Cache para componentes - TTL más largo ya que cambian menos frecuentemente
        cacheManager.registerCustomCache("componentes", 
            Caffeine.newBuilder()
                .maximumSize(componentesMaxSize)
                .expireAfterWrite(Duration.ofMinutes(componentesTtlMinutes))
                .recordStats() // Habilitar estadísticas para monitoreo
                .build());
        
        // Cache para promociones - TTL más corto ya que pueden cambiar más frecuentemente
        cacheManager.registerCustomCache("promociones", 
            Caffeine.newBuilder()
                .maximumSize(promocionesMaxSize)
                .expireAfterWrite(Duration.ofMinutes(promocionesTtlMinutes))
                .recordStats() // Habilitar estadísticas para monitoreo
                .build());
        
        // Cache para existencia de componentes - TTL muy corto para validaciones rápidas
        cacheManager.registerCustomCache("componentes-existencia", 
            Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats() // Habilitar estadísticas para monitoreo
                .build());

        return cacheManager;
    }
}