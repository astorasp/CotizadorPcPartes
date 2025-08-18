package mx.com.qtx.cotizador.security.service;

import mx.com.qtx.cotizador.security.client.SessionValidationClient;
import mx.com.qtx.cotizador.security.dto.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de caché para validación de sesiones
 * Implementa un caché con TTL corto para mejorar el rendimiento
 * y reducir la carga en ms-seguridad
 */
@Service
@Profile({"default", "docker"})
public class SessionCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SessionCacheService.class);

    private final SessionValidationClient sessionValidationClient;
    private final Map<String, CacheEntry> sessionCache = new ConcurrentHashMap<>();
    private final long cacheTtlMs;
    private final int maxCacheSize;
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public SessionCacheService(
            SessionValidationClient sessionValidationClient,
            @Value("${session.cache.ttl-ms:30000}") long cacheTtlMs,
            @Value("${session.cache.max-size:1000}") int maxCacheSize) {
        this.sessionValidationClient = sessionValidationClient;
        this.cacheTtlMs = cacheTtlMs;
        this.maxCacheSize = maxCacheSize;
        
        logger.info("SessionCacheService inicializado con TTL: {}ms, max size: {}", cacheTtlMs, maxCacheSize);
    }

    /**
     * Valida una sesión con caché
     * 
     * @param sessionId ID de la sesión a validar
     * @return true si la sesión está activa
     */
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return false;
        }

        // Intentar obtener desde caché
        CacheEntry cached = sessionCache.get(sessionId);
        if (cached != null && !cached.isExpired()) {
            hitCount.incrementAndGet();
            logger.debug("Cache hit para sesión: {}", sessionId);
            return cached.isValid();
        }

        // Cache miss - consultar ms-seguridad
        missCount.incrementAndGet();
        logger.debug("Cache miss para sesión: {}", sessionId);
        
        try {
            boolean isValid = sessionValidationClient.validateSession(sessionId);
            
            // Actualizar caché
            updateCache(sessionId, isValid, null);
            
            return isValid;
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("Error al validar sesión {}: {}", sessionId, e.getMessage());
            
            // En caso de error, usar caché expirado si existe
            if (cached != null) {
                logger.warn("Usando caché expirado para sesión {} debido a error", sessionId);
                return cached.isValid();
            }
            
            return false;
        }
    }

    /**
     * Obtiene información de una sesión con caché
     * 
     * @param sessionId ID de la sesión
     * @return Optional con información de la sesión
     */
    public Optional<SessionInfo> getSessionInfo(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return Optional.empty();
        }

        // Intentar obtener desde caché
        CacheEntry cached = sessionCache.get(sessionId);
        if (cached != null && !cached.isExpired() && cached.getSessionInfo() != null) {
            hitCount.incrementAndGet();
            logger.debug("Cache hit para información de sesión: {}", sessionId);
            return Optional.of(cached.getSessionInfo());
        }

        // Cache miss - consultar ms-seguridad
        missCount.incrementAndGet();
        logger.debug("Cache miss para información de sesión: {}", sessionId);
        
        try {
            Optional<SessionInfo> sessionInfo = sessionValidationClient.getSessionInfo(sessionId);
            
            // Actualizar caché
            updateCache(sessionId, sessionInfo.map(SessionInfo::isValid).orElse(false), sessionInfo.orElse(null));
            
            return sessionInfo;
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("Error al obtener información de sesión {}: {}", sessionId, e.getMessage());
            
            // En caso de error, usar caché expirado si existe
            if (cached != null && cached.getSessionInfo() != null) {
                logger.warn("Usando caché expirado para información de sesión {} debido a error", sessionId);
                return Optional.of(cached.getSessionInfo());
            }
            
            return Optional.empty();
        }
    }

    /**
     * Cierra una sesión e invalida el caché
     * 
     * @param sessionId ID de la sesión a cerrar
     * @return true si la sesión fue cerrada exitosamente
     */
    public boolean closeSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.debug("Session ID es null o vacío");
            return false;
        }

        try {
            boolean closed = sessionValidationClient.closeSession(sessionId);
            
            // Invalidar caché independientemente del resultado
            sessionCache.remove(sessionId);
            logger.debug("Caché invalidado para sesión cerrada: {}", sessionId);
            
            return closed;
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("Error al cerrar sesión {}: {}", sessionId, e.getMessage());
            
            // Invalidar caché aunque haya error
            sessionCache.remove(sessionId);
            
            return false;
        }
    }

    /**
     * Invalida una entrada específica del caché
     * 
     * @param sessionId ID de la sesión a invalidar
     */
    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            sessionCache.remove(sessionId);
            logger.debug("Caché invalidado manualmente para sesión: {}", sessionId);
        }
    }

    /**
     * Limpia todo el caché
     */
    public void clearCache() {
        sessionCache.clear();
        logger.info("Caché de sesiones limpiado completamente");
    }

    /**
     * Limpieza automática de entradas expiradas cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void cleanupExpiredEntries() {
        long startTime = System.currentTimeMillis();
        AtomicInteger removedCount = new AtomicInteger(0);
        
        sessionCache.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });
        
        long duration = System.currentTimeMillis() - startTime;
        
        if (removedCount.get() > 0) {
            logger.debug("Limpieza de caché completada: {} entradas removidas en {}ms", 
                        removedCount.get(), duration);
        }
    }

    /**
     * Limpieza por tamaño cuando se excede el límite
     */
    private void enforceMaxSize() {
        if (sessionCache.size() > maxCacheSize) {
            // Remover las entradas más antiguas
            sessionCache.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(sessionCache.size() - maxCacheSize)
                    .forEach(entry -> sessionCache.remove(entry.getKey()));
            
            logger.debug("Caché reducido a {} entradas por límite de tamaño", sessionCache.size());
        }
    }

    /**
     * Actualiza el caché con nueva información
     */
    private void updateCache(String sessionId, boolean isValid, SessionInfo sessionInfo) {
        CacheEntry entry = new CacheEntry(isValid, sessionInfo, System.currentTimeMillis());
        sessionCache.put(sessionId, entry);
        
        // Verificar límite de tamaño
        enforceMaxSize();
        
        logger.debug("Caché actualizado para sesión: {} (válida: {})", sessionId, isValid);
    }

    /**
     * Obtiene estadísticas del caché
     */
    public CacheStats getCacheStats() {
        long totalRequests = hitCount.get() + missCount.get();
        double hitRate = totalRequests > 0 ? (double) hitCount.get() / totalRequests : 0.0;
        
        return new CacheStats(
                sessionCache.size(),
                hitCount.get(),
                missCount.get(),
                errorCount.get(),
                hitRate,
                cacheTtlMs,
                maxCacheSize
        );
    }

    /**
     * Obtiene información de salud del servicio
     */
    public HealthInfo getHealthInfo() {
        return new HealthInfo(
                sessionValidationClient.isServiceAvailable(),
                sessionCache.size(),
                getCacheStats(),
                sessionValidationClient.getHealthInfo()
        );
    }

    /**
     * Entrada del caché con timestamp
     */
    private class CacheEntry implements Comparable<CacheEntry> {
        private final boolean isValid;
        private final SessionInfo sessionInfo;
        private final long timestamp;

        public CacheEntry(boolean isValid, SessionInfo sessionInfo, long timestamp) {
            this.isValid = isValid;
            this.sessionInfo = sessionInfo;
            this.timestamp = timestamp;
        }

        public boolean isValid() {
            return isValid;
        }

        public SessionInfo getSessionInfo() {
            return sessionInfo;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > cacheTtlMs;
        }

        @Override
        public int compareTo(CacheEntry other) {
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

    /**
     * Estadísticas del caché
     */
    public static class CacheStats {
        private final int size;
        private final long hitCount;
        private final long missCount;
        private final long errorCount;
        private final double hitRate;
        private final long ttlMs;
        private final int maxSize;

        public CacheStats(int size, long hitCount, long missCount, long errorCount, 
                         double hitRate, long ttlMs, int maxSize) {
            this.size = size;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.errorCount = errorCount;
            this.hitRate = hitRate;
            this.ttlMs = ttlMs;
            this.maxSize = maxSize;
        }

        public int getSize() {
            return size;
        }

        public long getHitCount() {
            return hitCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public double getHitRate() {
            return hitRate;
        }

        public long getTtlMs() {
            return ttlMs;
        }

        public int getMaxSize() {
            return maxSize;
        }

        @Override
        public String toString() {
            return "CacheStats{" +
                    "size=" + size +
                    ", hitCount=" + hitCount +
                    ", missCount=" + missCount +
                    ", errorCount=" + errorCount +
                    ", hitRate=" + String.format("%.2f%%", hitRate * 100) +
                    ", ttlMs=" + ttlMs +
                    ", maxSize=" + maxSize +
                    '}';
        }
    }

    /**
     * Información de salud del servicio
     */
    public static class HealthInfo {
        private final boolean serviceAvailable;
        private final int cacheSize;
        private final CacheStats cacheStats;
        private final SessionValidationClient.HealthInfo clientHealthInfo;

        public HealthInfo(boolean serviceAvailable, int cacheSize, CacheStats cacheStats, 
                         SessionValidationClient.HealthInfo clientHealthInfo) {
            this.serviceAvailable = serviceAvailable;
            this.cacheSize = cacheSize;
            this.cacheStats = cacheStats;
            this.clientHealthInfo = clientHealthInfo;
        }

        public boolean isServiceAvailable() {
            return serviceAvailable;
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public CacheStats getCacheStats() {
            return cacheStats;
        }

        public SessionValidationClient.HealthInfo getClientHealthInfo() {
            return clientHealthInfo;
        }

        @Override
        public String toString() {
            return "HealthInfo{" +
                    "serviceAvailable=" + serviceAvailable +
                    ", cacheSize=" + cacheSize +
                    ", cacheStats=" + cacheStats +
                    ", clientHealthInfo=" + clientHealthInfo +
                    '}';
        }
    }
}