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
 * Configuración de seguridad simple para tests de integración.
 * Reemplaza completamente la configuración de seguridad principal durante los tests,
 * proporcionando una configuración simplificada con autenticación básica HTTP.
 *
 * Esta configuración permite:
 * - Acceso público a documentación Swagger y endpoints de health
 * - Autenticación requerida para todos los endpoints de negocio
 * - Usuario de test con roles ADMIN y USER
 * - Configuración CORS permisiva para tests
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
@TestConfiguration
public class SimpleTestSecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para tests.
     * Define reglas de autorización, deshabilita CSRF y configura autenticación básica.
     *
     * @param http El objeto HttpSecurity para configurar
     * @return La cadena de filtros de seguridad configurada
     * @throws Exception Si ocurre un error en la configuración
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
     * Configura la fuente de configuración CORS para tests.
     * Permite todas las solicitudes desde cualquier origen para facilitar los tests.
     *
     * @return La fuente de configuración CORS configurada
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
     * Proporciona un codificador de contraseñas BCrypt para tests.
     *
     * @return El codificador de contraseñas configurado
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Configura un servicio de detalles de usuario en memoria para tests.
     * Crea un usuario de test con credenciales simples y roles ADMIN y USER.
     *
     * @return El servicio de detalles de usuario configurado
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