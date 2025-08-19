package mx.com.qtx.cotizador.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import mx.com.qtx.cotizador.security.filter.JwtAuthenticationFilter;

/**
 * Configuración de seguridad del sistema Cotizador.
 * 
 * Implementa autenticación condicional por perfil:
 * - Perfil 'test': Solo Basic Auth
 * - Perfil 'default'/'docker': Solo JWT Auth
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    // Role constants for Componentes module
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_GERENTE = "GERENTE";
    public static final String ROLE_VENDEDOR = "VENDEDOR";
    public static final String ROLE_INVENTARIO = "INVENTARIO";
    public static final String ROLE_CONSULTOR = "CONSULTOR";
    
    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${security.basic.enabled:true}")
    private boolean basicAuthEnabled;
    
    @Value("${security.basic.username}")
    private String username;
    
    @Value("${security.basic.password}")
    private String password;
    
    @Value("${security.basic.realm}")
    private String realm;
    
    /**
     * Configuración de seguridad híbrida.
     * 
     * - Tests: Solo Basic Auth (JWT beans no disponibles)
     * - Producción: JWT + Basic Auth fallback
     * - Docker: Solo JWT (mediante configuración)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HttpSecurity httpSecurity = http
            // Deshabilitar CSRF para APIs REST
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar autorización de requests
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos - Solo para documentación y health checks
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Endpoints de API que requieren autenticación
                .requestMatchers("/componentes/**").authenticated()
                .requestMatchers("/pcs/**").authenticated()
                .requestMatchers("/promociones/**").authenticated()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ¡IMPORTANTE!
                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()                
            );
        
        // Agregar filtro JWT solo si está disponible (para producción)
        if (jwtAuthenticationFilter != null) {
            httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        // Configurar Basic Auth solo si está habilitado
        if (basicAuthEnabled) {
            httpSecurity.httpBasic(basic -> basic.realmName(realm));
        }
        
        return httpSecurity
            
            // Configurar sesiones como stateless (para APIs REST)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Encoder de contraseñas usando BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Servicio de usuarios en memoria.
     * 
     * Define el usuario único del sistema con credenciales desde configuration.
     * Solo se crea si Basic Auth está habilitado.
     */
    @Bean
    @ConditionalOnProperty(name = "security.basic.enabled", havingValue = "true", matchIfMissing = true)
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles(ROLE_ADMIN, ROLE_GERENTE, ROLE_VENDEDOR, ROLE_INVENTARIO, ROLE_CONSULTOR, "USER")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
}