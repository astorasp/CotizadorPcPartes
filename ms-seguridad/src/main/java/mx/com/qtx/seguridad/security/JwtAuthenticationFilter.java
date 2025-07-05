package mx.com.qtx.seguridad.security;

import mx.com.qtx.seguridad.service.JwtService;
import mx.com.qtx.seguridad.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro de autenticación JWT que intercepta requests HTTP
 * Extrae y valida tokens JWT del header Authorization
 * Configura el SecurityContext con información del usuario autenticado
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    /**
     * Filtro principal que procesa cada request HTTP
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extraer token del header Authorization
            String token = extractTokenFromRequest(request);
            
            if (token == null) {
                // No hay token, continuar con el filtro chain
                filterChain.doFilter(request, response);
                return;
            }

            // Validar token
            if (!isTokenValid(token)) {
                logger.debug("Token inválido o expirado para request: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Si ya hay autenticación en el contexto, no procesar de nuevo
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Procesar token válido y configurar autenticación
            processValidToken(token, request);

        } catch (Exception e) {
            logger.error("Error en filtro de autenticación JWT: {}", e.getMessage(), e);
            // No lanzar excepción, dejar que continúe el filtro chain
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }

    /**
     * Valida el token usando AuthService (incluye blacklist)
     */
    private boolean isTokenValid(String token) {
        try {
            return authService.isTokenValid(token);
        } catch (Exception e) {
            logger.debug("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Procesa un token válido y configura el SecurityContext
     */
    private void processValidToken(String token, HttpServletRequest request) {
        try {
            // Extraer información del token
            String username = jwtService.extractUsername(token);
            Integer userId = jwtService.extractUserId(token);
            List<String> roles = jwtService.extractRoles(token);

            if (username == null || username.trim().isEmpty()) {
                logger.warn("Token válido pero sin username");
                return;
            }

            // Verificar que sea un access token
            if (!jwtService.isAccessToken(token)) {
                logger.warn("Token no es de tipo access para usuario: {}", username);
                return;
            }

            // Convertir roles a authorities de Spring Security
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // Crear objeto de autenticación
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                            username, 
                            null, // No password needed for JWT
                            authorities
                    );

            // Agregar detalles adicionales a la autenticación
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Agregar información adicional como atributos
            authentication.setDetails(new JwtAuthenticationDetails(
                    request, username, userId, roles, token
            ));

            // Configurar SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Usuario autenticado: {} con roles: {}", username, roles);

        } catch (Exception e) {
            logger.error("Error al procesar token válido: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Determina si el filtro debe ejecutarse para esta request
     * Excluye URLs que no necesitan procesamiento JWT
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // URLs públicas que no necesitan filtro JWT
        if ("POST".equals(method) && "/auth/login".equals(path)) {
            return true;
        }
        
        if ("POST".equals(method) && "/auth/refresh".equals(path)) {
            return true;
        }
        
        if ("GET".equals(method) && path.startsWith("/actuator/health")) {
            return true;
        }
        
        if ("GET".equals(method) && "/keys/jwks".equals(path)) {
            return true;
        }
        
        if ("GET".equals(method) && path.startsWith("/swagger-ui")) {
            return true;
        }
        
        if ("GET".equals(method) && path.startsWith("/v3/api-docs")) {
            return true;
        }

        // Para todas las demás URLs, ejecutar el filtro
        return false;
    }

    /**
     * Clase interna para almacenar detalles adicionales de autenticación JWT
     */
    public static class JwtAuthenticationDetails extends WebAuthenticationDetailsSource {
        
        private final String username;
        private final Integer userId;
        private final List<String> roles;
        private final String token;
        private final String remoteAddress;
        private final String sessionId;

        public JwtAuthenticationDetails(HttpServletRequest request, String username, 
                                      Integer userId, List<String> roles, String token) {
            this.username = username;
            this.userId = userId;
            this.roles = roles;
            this.token = token;
            this.remoteAddress = request.getRemoteAddr();
            this.sessionId = request.getSession(false) != null ? 
                           request.getSession().getId() : null;
        }

        // Getters
        public String getUsername() { return username; }
        public Integer getUserId() { return userId; }
        public List<String> getRoles() { return roles; }
        public String getToken() { return token; }
        public String getRemoteAddress() { return remoteAddress; }
        public String getSessionId() { return sessionId; }

        @Override
        public String toString() {
            return "JwtAuthenticationDetails{" +
                    "username='" + username + '\'' +
                    ", userId=" + userId +
                    ", roles=" + roles +
                    ", remoteAddress='" + remoteAddress + '\'' +
                    '}';
        }
    }
}