package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.security.RateLimitingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio para limpieza programada de contadores de rate limiting
 * Previene memory leaks eliminando contadores expirados
 */
@Service
public class RateLimitingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingCleanupService.class);

    private final RateLimitingInterceptor rateLimitingInterceptor;

    public RateLimitingCleanupService(RateLimitingInterceptor rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    /**
     * Limpia contadores expirados cada 30 minutos
     */
    @Scheduled(fixedRate = 1800000) // 30 minutos en milisegundos
    public void cleanupExpiredCounters() {
        try {
            logger.debug("Iniciando limpieza de contadores de rate limiting expirados");
            rateLimitingInterceptor.cleanupExpiredCounters();
            logger.debug("Limpieza de contadores completada");
        } catch (Exception e) {
            logger.error("Error durante limpieza de contadores de rate limiting", e);
        }
    }
}