package mx.com.qtx.cotizador.security.service;

import mx.com.qtx.cotizador.security.client.JwksClient;
import mx.com.qtx.cotizador.security.dto.JwksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Servicio de cache para JWKS con auto-refresh
 * Mantiene una copia local de las claves públicas y las actualiza automáticamente
 */
@Service
@Profile({"default", "docker"})
public class JwksCacheService {

    private static final Logger logger = LoggerFactory.getLogger(JwksCacheService.class);

    private final JwksClient jwksClient;
    private final AtomicReference<JwksResponse> cachedJwks = new AtomicReference<>();
    private final AtomicReference<LocalDateTime> lastRefresh = new AtomicReference<>();
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final long refreshIntervalMs;
    private final int maxRetries;

    public JwksCacheService(
            JwksClient jwksClient,
            @Value("${jwt.jwks.refresh-interval-ms:300000}") long refreshIntervalMs,
            @Value("${jwt.jwks.max-retries:3}") int maxRetries) {
        this.jwksClient = jwksClient;
        this.refreshIntervalMs = refreshIntervalMs;
        this.maxRetries = maxRetries;
        
        logger.info("JwksCacheService inicializado con refresh interval: {}ms, max retries: {}", 
                   refreshIntervalMs, maxRetries);
    }

    /**
     * Inicializa el cache al arrancar la aplicación
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initializeCache() {
        if (!isInitialized.get()) {
            logger.info("Inicializando cache JWKS al arrancar la aplicación");
            refreshCacheAsync();
        }
    }

    /**
     * Refresh automático del cache cada 5 minutos por defecto
     */
    @Scheduled(fixedRateString = "${jwt.jwks.refresh-interval-ms:300000}")
    public void scheduledRefresh() {
        logger.debug("Ejecutando refresh programado del cache JWKS");
        refreshCacheAsync();
    }

    /**
     * Obtiene el JWKS desde el cache
     * Si no está disponible, intenta obtenerlo sincronamente
     */
    public JwksResponse getCachedJwks() {
        JwksResponse cached = cachedJwks.get();
        
        if (cached != null && cached.hasValidKeys()) {
            logger.debug("Retornando JWKS desde cache. Claves disponibles: {}", cached.getKeyCount());
            return cached;
        }
        
        logger.warn("Cache JWKS vacío o inválido, obteniendo sincronamente");
        return refreshCacheSync();
    }

    /**
     * Fuerza un refresh del cache
     */
    public JwksResponse forceRefresh() {
        logger.info("Forzando refresh del cache JWKS");
        return refreshCacheSync();
    }

    /**
     * Verifica si el cache está disponible y válido
     */
    public boolean isCacheValid() {
        JwksResponse cached = cachedJwks.get();
        LocalDateTime lastRefreshTime = lastRefresh.get();
        
        if (cached == null || !cached.hasValidKeys()) {
            return false;
        }
        
        if (lastRefreshTime == null) {
            return false;
        }
        
        // Verificar si el cache no ha expirado
        LocalDateTime expiration = lastRefreshTime.plusSeconds(refreshIntervalMs / 1000);
        return LocalDateTime.now().isBefore(expiration);
    }

    /**
     * Obtiene información del estado del cache
     */
    public CacheInfo getCacheInfo() {
        JwksResponse cached = cachedJwks.get();
        LocalDateTime lastRefreshTime = lastRefresh.get();
        
        return new CacheInfo(
            cached != null,
            cached != null ? cached.getKeyCount() : 0,
            cached != null && cached.hasValidKeys(),
            lastRefreshTime != null ? lastRefreshTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            isCacheValid(),
            isInitialized.get()
        );
    }

    /**
     * Refresh asincrono del cache
     */
    private void refreshCacheAsync() {
        new Thread(() -> {
            try {
                refreshCacheSync();
            } catch (Exception e) {
                logger.error("Error en refresh asíncrono del cache JWKS: {}", e.getMessage(), e);
            }
        }, "jwks-cache-refresh").start();
    }

    /**
     * Refresh sincrono del cache con reintentos
     */
    private JwksResponse refreshCacheSync() {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("Intento {} de {} para obtener JWKS", attempt, maxRetries);
                
                JwksResponse response = jwksClient.fetchJwks();
                
                if (response != null && response.hasValidKeys()) {
                    cachedJwks.set(response);
                    lastRefresh.set(LocalDateTime.now());
                    isInitialized.set(true);
                    
                    logger.info("Cache JWKS actualizado exitosamente. Claves disponibles: {}", 
                               response.getKeyCount());
                    return response;
                } else {
                    logger.warn("JWKS response inválido en intento {}", attempt);
                }
                
            } catch (Exception e) {
                logger.error("Error en intento {} para obtener JWKS: {}", attempt, e.getMessage());
                
                if (attempt == maxRetries) {
                    logger.error("Todos los intentos fallaron para obtener JWKS", e);
                    throw new RuntimeException("No se pudo obtener JWKS después de " + maxRetries + " intentos", e);
                }
                
                // Esperar antes del siguiente intento
                try {
                    Thread.sleep(1000 * attempt); // Backoff exponencial
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // Si llegamos aquí, usar cache existente si está disponible
        JwksResponse cached = cachedJwks.get();
        if (cached != null && cached.hasValidKeys()) {
            logger.warn("Usando cache JWKS existente después de fallar el refresh");
            return cached;
        }
        
        throw new RuntimeException("No se pudo obtener JWKS y no hay cache disponible");
    }

    /**
     * Limpia el cache
     */
    public void clearCache() {
        cachedJwks.set(null);
        lastRefresh.set(null);
        isInitialized.set(false);
        logger.info("Cache JWKS limpiado");
    }

    /**
     * Información del estado del cache
     */
    public static class CacheInfo {
        private final boolean hasCachedData;
        private final int keyCount;
        private final boolean isValid;
        private final String lastRefreshTime;
        private final boolean isExpired;
        private final boolean isInitialized;

        public CacheInfo(boolean hasCachedData, int keyCount, boolean isValid, 
                        String lastRefreshTime, boolean isExpired, boolean isInitialized) {
            this.hasCachedData = hasCachedData;
            this.keyCount = keyCount;
            this.isValid = isValid;
            this.lastRefreshTime = lastRefreshTime;
            this.isExpired = isExpired;
            this.isInitialized = isInitialized;
        }

        public boolean hasCachedData() {
            return hasCachedData;
        }

        public int getKeyCount() {
            return keyCount;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getLastRefreshTime() {
            return lastRefreshTime;
        }

        public boolean isExpired() {
            return isExpired;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        @Override
        public String toString() {
            return "CacheInfo{" +
                    "hasCachedData=" + hasCachedData +
                    ", keyCount=" + keyCount +
                    ", isValid=" + isValid +
                    ", lastRefreshTime='" + lastRefreshTime + '\'' +
                    ", isExpired=" + isExpired +
                    ", isInitialized=" + isInitialized +
                    '}';
        }
    }
}