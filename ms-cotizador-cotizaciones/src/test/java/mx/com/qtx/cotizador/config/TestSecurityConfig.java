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
 * Configuración de seguridad específica para pruebas de integración.
 * 
 * Esta configuración reemplaza la configuración de seguridad principal durante las pruebas,
 * proporcionando una autenticación simplificada usando HTTP Basic Authentication sin JWT.
 * 
 * Características principales:
 * - Autenticación básica HTTP (usuario/contraseña)
 * - Usuario único en memoria (configurable via properties)
 * - CORS completamente abierto para facilitar pruebas
 * - Deshabilitación de CSRF
 * - Gestión de sesiones sin estado
 * - Sin filtros JWT para simplificar las pruebas
 * 
 * Está diseñada para permitir pruebas de integración eficientes y rápidas
 * sin la complejidad adicional de tokens JWT.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@TestConfiguration
@Profile("test")
public class TestSecurityConfig {
    
    /** Usuario de prueba configurable vía properties */
    @Value("${security.basic.username:test}")
    private String username;
    
    /** Contraseña de prueba configurable vía properties */
    @Value("${security.basic.password:test123}")
    private String password;
    
    /** Nombre del realm HTTP Basic configurable */
    @Value("${security.basic.realm:Test Realm}")
    private String realm;

    /**
     * Configura la cadena de filtros de seguridad para pruebas de integración.
     * 
     * Establece una configuración simplificada que incluye:
     * - Configuración CORS abierta
     * - Deshabilitación de CSRF
     * - Políticas de autorización para endpoints REST
     * - Autenticación básica HTTP
     * - Gestión de sesiones sin estado
     * 
     * @param http Configuración HTTP de Spring Security
     * @return Cadena de filtros de seguridad configurada para pruebas
     * @throws Exception Si ocurre un error durante la configuración
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

    /**
     * Configura la fuente de configuración CORS para pruebas.
     * 
     * Permite todas las solicitudes desde cualquier origen con todos los métodos
     * y encabezados HTTP, facilitando las pruebas de integración desde cualquier cliente.
     * 
     * @return Configuración CORS completamente abierta para pruebas
     */
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
     * Proporciona un codificador de contraseñas para pruebas.
     * 
     * Utiliza BCrypt para codificar contraseñas de forma segura
     * durante las pruebas de integración.
     * 
     * @return Codificador BCrypt para contraseñas de prueba
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configura el servicio de detalles de usuario para pruebas.
     * 
     * Crea un usuario único en memoria con las credenciales configuradas.
     * Las credenciales se pueden configurar vía properties del sistema:
     * - security.basic.username (default: test)
     * - security.basic.password (default: test123)
     * - security.basic.realm (default: Test Realm)
     * 
     * @return Servicio de detalles de usuario en memoria con usuario de prueba
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