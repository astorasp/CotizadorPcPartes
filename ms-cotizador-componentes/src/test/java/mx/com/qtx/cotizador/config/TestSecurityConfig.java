package mx.com.qtx.cotizador.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad específica para tests.
 * 
 * Solo Basic Auth - Sin JWT para simplicidad en tests.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@TestConfiguration
@Profile("test")
public class TestSecurityConfig {
    
    @Value("${security.basic.username:test}")
    private String username;
    
    @Value("${security.basic.password:test123}")
    private String password;
    
    @Value("${security.basic.realm:Test Realm}")
    private String realm;

    /**
     * Configuración de seguridad para tests.
     * Solo Basic Auth - Sin filtros JWT.
     */
    @Bean("securityFilterChain")
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Deshabilitar CSRF para APIs REST
            .cors(cors -> cors.configurationSource(testCorsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar autorización de requests
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos - Solo para documentación y health checks
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Endpoints de API que requieren autenticación Basic
                .requestMatchers("/componentes/**").authenticated()
                .requestMatchers("/pcs/**").authenticated()
                .requestMatchers("/cotizaciones/**").authenticated()
                .requestMatchers("/pedidos/**").authenticated()
                .requestMatchers("/promociones/**").authenticated()
                .requestMatchers("/proveedores/**").authenticated()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ¡IMPORTANTE!
                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()                
            )
            
            // Solo autenticación básica HTTP - SIN JWT
            .httpBasic(basic -> basic
                .realmName(realm)
            )
            
            // Configurar sesiones como stateless (para APIs REST)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .build();
    }

    @Bean
    @Primary
    public CorsConfigurationSource testCorsConfigurationSource() {
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
     * Encoder de contraseñas usando BCrypt para tests.
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Servicio de usuarios en memoria para tests.
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails user = User.builder()
            .username(username)
            .password(testPasswordEncoder().encode(password))
            .roles("ADMIN", "USER")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
}