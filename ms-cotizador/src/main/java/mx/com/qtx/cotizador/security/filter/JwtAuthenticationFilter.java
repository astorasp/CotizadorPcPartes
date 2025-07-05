package mx.com.qtx.cotizador.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.com.qtx.cotizador.security.service.JwtValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro para autenticación JWT
 * Intercepta requests y valida tokens JWT en el header Authorization
 */
@Component
@Profile({"default", "docker"})
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidationService jwtValidationService;

    public JwtAuthenticationFilter(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extraer token del header Authorization
            String token = extractTokenFromRequest(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Validar token JWT
                if (jwtValidationService.isTokenValid(token)) {
                    // Extraer información del usuario
                    JwtValidationService.UserInfo userInfo = jwtValidationService.extractUserInfo(token);
                    
                    // Crear authentication
                    Authentication authentication = createAuthentication(userInfo, request);
                    
                    // Establecer en security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("Usuario autenticado vía JWT: {}", userInfo.getUsername());
                } else {
                    logger.debug("Token JWT inválido en request: {}", request.getRequestURI());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error procesando JWT token: {}", e.getMessage(), e);
            // No fallar la cadena de filtros - permitir que Basic Auth maneje la autenticación
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Crea un objeto Authentication basado en la información del usuario JWT
     */
    private Authentication createAuthentication(JwtValidationService.UserInfo userInfo, HttpServletRequest request) {
        // Extraer roles del usuario
        List<SimpleGrantedAuthority> authorities = extractAuthorities(userInfo.getRoles());
        
        // Crear authentication token
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userInfo.getUsername(),
                null, // No credentials needed for JWT
                authorities
            );
        
        // Establecer detalles de la web request
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        return authentication;
    }

    /**
     * Extrae las autoridades (roles) del string de roles
     */
    private List<SimpleGrantedAuthority> extractAuthorities(String roles) {
        if (roles == null || roles.trim().isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Determina si el filtro debe ejecutarse para este request
     * Skip para endpoints que no requieren autenticación
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // No filtrar endpoints públicos
        return path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/error");
    }
}