package mx.com.qtx.seguridad.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Interceptor para rate limiting usando algoritmo Token Bucket simplificado
 * Específicamente diseñado para proteger endpoints JWKS
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptor.class);

    // Configuración de rate limiting
    private final int maxRequestsPerMinute;
    private final int maxRequestsPerHour;
    private final boolean rateLimitingEnabled;
    
    // Cache de contadores por IP
    private final ConcurrentMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    public RateLimitingInterceptor(
            @Value("${jwt.rate-limiting.max-requests-per-minute:60}") int maxRequestsPerMinute,
            @Value("${jwt.rate-limiting.max-requests-per-hour:1000}") int maxRequestsPerHour,
            @Value("${jwt.rate-limiting.enabled:true}") boolean rateLimitingEnabled) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.rateLimitingEnabled = rateLimitingEnabled;
        
        logger.info("RateLimitingInterceptor inicializado - Habilitado: {}, Max/min: {}, Max/hora: {}", 
                   rateLimitingEnabled, maxRequestsPerMinute, maxRequestsPerHour);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Si rate limiting está deshabilitado, permitir request
        if (!rateLimitingEnabled) {
            return true;
        }
        
        // Solo aplicar rate limiting a endpoints JWKS
        String requestURI = request.getRequestURI();
        if (!requestURI.contains("/keys/jwks")) {
            return true;
        }
        
        String clientIp = getClientIpAddress(request);
        RequestCounter counter = getOrCreateCounter(clientIp);
        
        boolean allowed = counter.allowRequest();
        
        if (!allowed) {
            // Rate limit excedido
            logger.warn("Rate limit excedido para IP: {} en endpoint: {}. Requests/min: {}, Requests/hora: {}", 
                       clientIp, requestURI, counter.getRequestsInCurrentMinute(), counter.getRequestsInCurrentHour());
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            // Headers informativos de rate limiting
            response.setHeader("X-RateLimit-Limit-Minute", String.valueOf(maxRequestsPerMinute));
            response.setHeader("X-RateLimit-Limit-Hour", String.valueOf(maxRequestsPerHour));
            response.setHeader("X-RateLimit-Remaining-Minute", "0");
            response.setHeader("X-RateLimit-Remaining-Hour", "0");
            response.setHeader("X-RateLimit-Reset-Minute", String.valueOf(counter.getNextMinuteReset()));
            response.setHeader("X-RateLimit-Reset-Hour", String.valueOf(counter.getNextHourReset()));
            
            String errorJson = "{"
                + "\"error\": \"rate_limit_exceeded\","
                + "\"message\": \"Too many requests to JWKS endpoint\","
                + "\"maxRequestsPerMinute\": " + maxRequestsPerMinute + ","
                + "\"maxRequestsPerHour\": " + maxRequestsPerHour + ","
                + "\"retryAfterSeconds\": 60"
                + "}";
            
            response.getWriter().write(errorJson);
            return false;
        }
        
        // Request permitido, agregar headers informativos
        response.setHeader("X-RateLimit-Limit-Minute", String.valueOf(maxRequestsPerMinute));
        response.setHeader("X-RateLimit-Limit-Hour", String.valueOf(maxRequestsPerHour));
        response.setHeader("X-RateLimit-Remaining-Minute", String.valueOf(maxRequestsPerMinute - counter.getRequestsInCurrentMinute()));
        response.setHeader("X-RateLimit-Remaining-Hour", String.valueOf(maxRequestsPerHour - counter.getRequestsInCurrentHour()));
        
        return true;
    }

    /**
     * Obtiene o crea un contador de requests para una IP
     */
    private RequestCounter getOrCreateCounter(String clientIp) {
        return requestCounters.computeIfAbsent(clientIp, ip -> new RequestCounter());
    }

    /**
     * Extrae la IP real del cliente considerando proxies y load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Limpia contadores expirados para evitar memory leaks
     */
    public void cleanupExpiredCounters() {
        long currentTime = Instant.now().getEpochSecond();
        requestCounters.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(currentTime));
    }

    /**
     * Contador de requests por IP con ventanas deslizantes
     */
    private class RequestCounter {
        private volatile int requestsInCurrentMinute = 0;
        private volatile int requestsInCurrentHour = 0;
        private volatile long currentMinuteStart;
        private volatile long currentHourStart;
        
        public RequestCounter() {
            long now = Instant.now().getEpochSecond();
            this.currentMinuteStart = (now / 60) * 60; // Redondear al minuto
            this.currentHourStart = (now / 3600) * 3600; // Redondear a la hora
        }
        
        public synchronized boolean allowRequest() {
            long now = Instant.now().getEpochSecond();
            
            // Reset contador de minuto si es necesario
            long minuteWindow = (now / 60) * 60;
            if (minuteWindow > currentMinuteStart) {
                currentMinuteStart = minuteWindow;
                requestsInCurrentMinute = 0;
            }
            
            // Reset contador de hora si es necesario
            long hourWindow = (now / 3600) * 3600;
            if (hourWindow > currentHourStart) {
                currentHourStart = hourWindow;
                requestsInCurrentHour = 0;
            }
            
            // Verificar límites
            if (requestsInCurrentMinute >= maxRequestsPerMinute) {
                return false;
            }
            
            if (requestsInCurrentHour >= maxRequestsPerHour) {
                return false;
            }
            
            // Incrementar contadores
            requestsInCurrentMinute++;
            requestsInCurrentHour++;
            
            return true;
        }
        
        public int getRequestsInCurrentMinute() {
            return requestsInCurrentMinute;
        }
        
        public int getRequestsInCurrentHour() {
            return requestsInCurrentHour;
        }
        
        public long getNextMinuteReset() {
            return currentMinuteStart + 60;
        }
        
        public long getNextHourReset() {
            return currentHourStart + 3600;
        }
        
        public boolean isExpired(long currentTime) {
            // Considerar expirado si no hay actividad en las últimas 2 horas
            return (currentTime - currentHourStart) > 7200;
        }
    }
}