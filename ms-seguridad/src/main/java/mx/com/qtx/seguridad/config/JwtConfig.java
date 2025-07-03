package mx.com.qtx.seguridad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class JwtConfig {

    // Configuración de duración de tokens desde application.yml
    @Value("${jwt.access-token.expiration:600000}") // 10 minutos por defecto
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:7200000}") // 2 horas por defecto
    private long refreshTokenExpiration;

    @Value("${jwt.algorithm:RS256}") // RS256 por defecto
    private String algorithm;

    /**
     * Obtiene la duración configurada para access tokens
     * 
     * @return long duración en milisegundos
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Obtiene la duración configurada para refresh tokens
     * 
     * @return long duración en milisegundos
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Obtiene el algoritmo configurado para JWT
     * 
     * @return String algoritmo (RS256)
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Obtiene la duración del access token en segundos
     * Útil para respuestas de API
     * 
     * @return long duración en segundos
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Obtiene la duración del refresh token en segundos
     * Útil para respuestas de API
     * 
     * @return long duración en segundos
     */
    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    /**
     * Valida que la configuración JWT sea correcta
     * 
     * @return boolean true si la configuración es válida
     */
    public boolean isConfigurationValid() {
        return accessTokenExpiration > 0 && 
               refreshTokenExpiration > 0 && 
               refreshTokenExpiration > accessTokenExpiration &&
               "RS256".equals(algorithm);
    }

    /**
     * Obtiene información de la configuración JWT
     * 
     * @return String información de configuración
     */
    public String getConfigurationInfo() {
        return String.format(
            "JWT Configuration - Access: %d sec, Refresh: %d sec, Algorithm: %s",
            getAccessTokenExpirationInSeconds(),
            getRefreshTokenExpirationInSeconds(),
            algorithm
        );
    }
}