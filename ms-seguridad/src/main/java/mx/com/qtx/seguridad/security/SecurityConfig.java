package mx.com.qtx.seguridad.security;

import mx.com.qtx.seguridad.service.JwtService;
import mx.com.qtx.seguridad.service.AuthService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de Spring Security para el microservicio de seguridad
 * Define URLs públicas, protegidas y configuración de filtros JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtService jwtService;
    private final AuthService authService;

    public SecurityConfig(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    /**
     * Configuración principal de seguridad con filtros JWT
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF ya que usamos JWT
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar manejo de sesiones como STATELESS
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configurar autorización de requests
            .authorizeHttpRequests(authz -> authz
                // URLs públicas - acceso sin autenticación
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/keys/jwks").permitAll()
                .requestMatchers(HttpMethod.GET, "/keys/health").permitAll()
                
                // URLs públicas de gestión de sesiones
                .requestMatchers(HttpMethod.GET, "/session/validate/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/session/close/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/session/info/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/session/health").permitAll()
                
                .requestMatchers("/error").permitAll()
                
                // URLs de actuator para monitoreo
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // URLs de monitoreo interno - acceso sin autenticación
                .requestMatchers(HttpMethod.GET, "/monitoring/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/seguridad/v1/api/monitoring/**").permitAll()
                
                // URLs de documentación (Swagger)
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // URLs de autenticación que requieren token válido
                .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                .requestMatchers(HttpMethod.GET, "/auth/validate").authenticated()
                .requestMatchers(HttpMethod.GET, "/auth/token-ttl").authenticated()
                
                // URLs de gestión de usuarios - requieren rol ADMIN
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                
                // URLs de gestión de roles - requieren rol ADMIN
                .requestMatchers("/roles/**").hasRole("ADMIN")
                
                // URLs de gestión de llaves privadas - requieren rol ADMIN
                .requestMatchers(HttpMethod.GET, "/keys/private").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/keys/generate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/keys/keypair").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/keys/info").hasRole("ADMIN")
                
                // Cualquier otra request requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar manejo de excepciones de autenticación
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtExceptionHandler())
                .accessDeniedHandler(jwtExceptionHandler())
            )
            
            // Configurar headers de seguridad
            .headers(headers -> headers
                .cacheControl(Customizer.withDefaults())
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny()))
            
            // Agregar filtros JWT
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean para el filtro de autenticación JWT
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, authService);
    }

    /**
     * Bean para el manejador de excepciones JWT
     */
    @Bean
    public JwtExceptionHandler jwtExceptionHandler() {
        return new JwtExceptionHandler();
    }

    /**
     * Bean para el encoder de contraseñas con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 para mayor seguridad
    }

    /**
     * Configuración de CORS para permitir requests desde diferentes orígenes
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (ajustar según ambiente)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", 
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}