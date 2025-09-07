package mx.com.qtx.cotizador.config;

import java.util.Arrays;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
 * Configuración de seguridad simplificada para pruebas de integración
 * 
 * Esta configuración reemplaza completamente la configuración de seguridad principal
 * durante las pruebas, proporcionando una configuración simplificada con:
 * - Autenticación básica HTTP
 * - Usuario de prueba único (test/test123)
 * - CORS completamente abierto para facilitar las pruebas
 * - Deshabilitación de CSRF
 * - Gestión de sesiones sin estado
 * 
 * Está diseñada para permitir pruebas de integración rápidas y eficientes
 * sin la complejidad de JWT o autenticación externa.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@TestConfiguration
public class SimpleTestSecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para pruebas.
     * 
     * Establece:
     * - Configuración CORS completa
     * - Deshabilitación de CSRF
     * - Autenticación básica HTTP
     * - Políticas de autorización para endpoints REST
     * - Gestión de sesiones sin estado
     * 
     * @param http Configuración HTTP de Spring Security
     * @return Cadena de filtros de seguridad configurada
     * @throws Exception Si ocurre un error durante la configuración
     */
    @Bean("filterChain")
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(testCorsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/componentes/**").authenticated()
                .requestMatchers("/pcs/**").authenticated()
                .requestMatchers("/cotizaciones/**").authenticated()
                .requestMatchers("/pedidos/**").authenticated()
                .requestMatchers("/promociones/**").authenticated()
                .requestMatchers("/proveedores/**").authenticated()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()                
            )
            .httpBasic(basic -> basic.realmName("Test Realm"))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
    }

    /**
     * Configura la fuente de configuración CORS para pruebas.
     * 
     * Permite todas las solicitudes desde cualquier origen con todos los métodos
     * y encabezados, facilitando las pruebas de integración.
     * 
     * @return Configuración CORS completamente abierta
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
     * @return Codificador BCrypt para contraseñas
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configura el servicio de detalles de usuario para pruebas.
     * 
     * Crea un usuario único en memoria con credenciales de prueba:
     * - Usuario: test
     * - Contraseña: test123
     * - Roles: ADMIN, USER
     * 
     * @return Servicio de detalles de usuario en memoria
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails user = User.builder()
            .username("test")
            .password(testPasswordEncoder().encode("test123"))
            .roles("ADMIN", "USER")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
}