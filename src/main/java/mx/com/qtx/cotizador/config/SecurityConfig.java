package mx.com.qtx.cotizador.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import lombok.RequiredArgsConstructor;

/**
 * Configuración de seguridad del sistema Cotizador.
 * 
 * Implementa autenticación básica HTTP con usuario y contraseña
 * configurables desde application.yml.
 * 
 * @author Sistema Cotizador
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Value("${security.basic.username}")
    private String username;
    
    @Value("${security.basic.password}")
    private String password;
    
    @Value("${security.basic.realm}")
    private String realm;
    
    /**
     * Configuración principal de seguridad HTTP.
     * 
     * Define qué endpoints requieren autenticación y cuáles son públicos.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Deshabilitar CSRF para APIs REST
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar autorización de requests
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos - Solo para documentación y health checks
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Endpoints de API que requieren autenticación (sin context path adicional)
                .requestMatchers("/componentes/**").authenticated()
                .requestMatchers("/cotizaciones/**").authenticated()
                .requestMatchers("/pedidos/**").authenticated()
                .requestMatchers("/promociones/**").authenticated()
                .requestMatchers("/proveedores/**").authenticated()
                .requestMatchers("/promociones/**").authenticated()
                
                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar autenticación básica HTTP
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
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles("ADMIN", "USER")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
}