package mx.com.qtx.seguridad.service;

import mx.com.qtx.seguridad.config.RsaKeyProvider;
import mx.com.qtx.seguridad.dto.PublicKeyResponse;
import mx.com.qtx.seguridad.dto.KeyPairResponse;
import mx.com.qtx.seguridad.dto.JwksResponse;
import mx.com.qtx.seguridad.dto.JwkKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.Base64;
import java.math.BigInteger;

@Service
public class KeyManagementService {

    private static final Logger logger = LoggerFactory.getLogger(KeyManagementService.class);

    private final RsaKeyProvider rsaKeyProvider;

    public KeyManagementService(RsaKeyProvider rsaKeyProvider) {
        this.rsaKeyProvider = rsaKeyProvider;
    }

    /**
     * Inicialización automática al startup de la aplicación
     * Genera el par de llaves inicial si no existe
     */
    @PostConstruct
    public void initializeKeys() {
        logger.info("Iniciando KeyManagementService...");
        
        try {
            if (!rsaKeyProvider.areKeysAvailable()) {
                logger.warn("No hay llaves RSA disponibles, generando par inicial");
                rsaKeyProvider.generateKeyPair();
            }
            
            logger.info("KeyManagementService inicializado exitosamente");
            logger.info("Estado de llaves: {}", rsaKeyProvider.getKeyInfo());
            
        } catch (Exception e) {
            logger.error("Error crítico al inicializar KeyManagementService", e);
            throw new RuntimeException("Fallo fatal: No se pueden inicializar las llaves RSA", e);
        }
    }

    /**
     * Genera un nuevo par de llaves RSA
     * Este método reemplaza completamente las llaves existentes
     * 
     * @return Map con información sobre la operación
     */
    public Map<String, Object> generateNewKeyPair() {
        logger.info("Solicitada generación de nuevo par de llaves RSA");
        
        try {
            long oldTimestamp = rsaKeyProvider.getKeyGenerationTimestamp();
            rsaKeyProvider.rotateKeys();
            long newTimestamp = rsaKeyProvider.getKeyGenerationTimestamp();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Nuevo par de llaves generado exitosamente");
            result.put("previousKeyTimestamp", oldTimestamp);
            result.put("newKeyTimestamp", newTimestamp);
            result.put("keyInfo", rsaKeyProvider.getKeyInfo());
            
            logger.info("Nuevo par de llaves generado exitosamente");
            return result;
            
        } catch (Exception e) {
            logger.error("Error al generar nuevo par de llaves", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error al generar nuevo par de llaves: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return result;
        }
    }

    /**
     * Obtiene la llave pública actual en formato PEM
     * 
     * @return String con la llave pública en formato PEM
     */
    public String getPublicKeyAsPem() {
        try {
            return rsaKeyProvider.getPublicKeyAsPem();
        } catch (Exception e) {
            logger.error("Error al obtener llave pública en formato PEM", e);
            throw new RuntimeException("Error al obtener llave pública", e);
        }
    }

    /**
     * Obtiene la llave privada actual en formato PEM
     * ¡CUIDADO! Este método expone la llave privada
     * Debe ser usado solo por administradores autorizados
     * 
     * @return String con la llave privada en formato PEM
     */
    public String getPrivateKeyAsPem() {
        logger.warn("ALERTA DE SEGURIDAD: Llave privada solicitada en formato PEM");
        
        try {
            return rsaKeyProvider.getPrivateKeyAsPem();
        } catch (Exception e) {
            logger.error("Error al obtener llave privada en formato PEM", e);
            throw new RuntimeException("Error al obtener llave privada", e);
        }
    }

    /**
     * Obtiene ambas llaves (pública y privada) en formato PEM
     * ¡CUIDADO! Este método expone la llave privada
     * Debe ser usado solo por administradores autorizados
     * 
     * @return Map con ambas llaves en formato PEM
     */
    public Map<String, String> getKeyPairAsPem() {
        logger.warn("ALERTA DE SEGURIDAD: Par de llaves completo solicitado en formato PEM");
        
        try {
            Map<String, String> keyPair = new HashMap<>();
            keyPair.put("publicKey", rsaKeyProvider.getPublicKeyAsPem());
            keyPair.put("privateKey", rsaKeyProvider.getPrivateKeyAsPem());
            keyPair.put("algorithm", "RSA");
            keyPair.put("keySize", "2048");
            keyPair.put("generatedAt", new java.util.Date(rsaKeyProvider.getKeyGenerationTimestamp()).toString());
            
            return keyPair;
            
        } catch (Exception e) {
            logger.error("Error al obtener par de llaves en formato PEM", e);
            throw new RuntimeException("Error al obtener par de llaves", e);
        }
    }

    /**
     * Obtiene la llave pública RSA para uso interno
     * Método thread-safe para uso por JwtService
     * 
     * @return RSAPublicKey para verificación de tokens
     */
    public RSAPublicKey getPublicKey() {
        return rsaKeyProvider.getPublicKey();
    }

    /**
     * Obtiene la llave privada RSA para uso interno
     * Método thread-safe para uso por JwtService
     * 
     * @return RSAPrivateKey para firma de tokens
     */
    public RSAPrivateKey getPrivateKey() {
        return rsaKeyProvider.getPrivateKey();
    }

    /**
     * Obtiene información sobre las llaves actuales
     * No incluye datos sensibles, solo metadatos
     * 
     * @return Map con información de las llaves
     */
    public Map<String, Object> getKeyInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("available", rsaKeyProvider.areKeysAvailable());
            info.put("generatedAt", new java.util.Date(rsaKeyProvider.getKeyGenerationTimestamp()));
            info.put("timestamp", rsaKeyProvider.getKeyGenerationTimestamp());
            info.put("details", rsaKeyProvider.getKeyInfo());
            
            return info;
            
        } catch (Exception e) {
            logger.error("Error al obtener información de llaves", e);
            throw new RuntimeException("Error al obtener información de llaves", e);
        }
    }

    /**
     * Rota las llaves existentes por un nuevo par
     * Invalida todas las llaves anteriores
     * 
     * @return Map con información sobre la rotación
     */
    public Map<String, Object> rotateKeys() {
        logger.warn("Iniciando rotación manual de llaves RSA");
        
        try {
            long oldTimestamp = rsaKeyProvider.getKeyGenerationTimestamp();
            rsaKeyProvider.rotateKeys();
            long newTimestamp = rsaKeyProvider.getKeyGenerationTimestamp();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Llaves rotadas exitosamente");
            result.put("oldKeyTimestamp", oldTimestamp);
            result.put("newKeyTimestamp", newTimestamp);
            result.put("rotatedAt", new java.util.Date());
            
            logger.warn("Rotación de llaves completada exitosamente");
            return result;
            
        } catch (Exception e) {
            logger.error("Error durante la rotación de llaves", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error durante la rotación: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            throw new RuntimeException("Error crítico durante la rotación de llaves", e);
        }
    }

    /**
     * Verifica el estado de salud del sistema de llaves
     * 
     * @return Map con el estado de salud
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean keysAvailable = rsaKeyProvider.areKeysAvailable();
            long keyAge = System.currentTimeMillis() - rsaKeyProvider.getKeyGenerationTimestamp();
            
            health.put("status", keysAvailable ? "UP" : "DOWN");
            health.put("keysAvailable", keysAvailable);
            health.put("keyAgeMs", keyAge);
            health.put("keyAgeDays", keyAge / (1000 * 60 * 60 * 24));
            health.put("lastCheck", new java.util.Date());
            
            if (keysAvailable) {
                health.put("keyInfo", rsaKeyProvider.getKeyInfo());
            }
            
        } catch (Exception e) {
            logger.error("Error durante health check de llaves", e);
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    /**
     * Obtiene la llave pública en formato DTO
     * 
     * @return PublicKeyResponse con llave pública
     */
    public PublicKeyResponse getPublicKeyPem() {
        try {
            String publicKeyPem = rsaKeyProvider.getPublicKeyAsPem();
            LocalDateTime generatedAt = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(rsaKeyProvider.getKeyGenerationTimestamp()),
                    ZoneId.systemDefault());
            String keyId = UUID.nameUUIDFromBytes(publicKeyPem.getBytes()).toString();
            
            return new PublicKeyResponse(publicKeyPem, generatedAt, keyId);
            
        } catch (Exception e) {
            logger.error("Error al obtener llave pública como DTO", e);
            return PublicKeyResponse.error("Error al obtener llave pública");
        }
    }

    /**
     * Obtiene el par completo de llaves en formato DTO
     * ¡CUIDADO! Incluye llave privada
     * 
     * @return KeyPairResponse con ambas llaves
     */
    public KeyPairResponse getKeyPairPem() {
        try {
            String publicKeyPem = rsaKeyProvider.getPublicKeyAsPem();
            String privateKeyPem = rsaKeyProvider.getPrivateKeyAsPem();
            LocalDateTime generatedAt = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(rsaKeyProvider.getKeyGenerationTimestamp()),
                    ZoneId.systemDefault());
            String keyId = UUID.nameUUIDFromBytes(publicKeyPem.getBytes()).toString();
            
            return new KeyPairResponse(publicKeyPem, privateKeyPem, generatedAt, keyId);
            
        } catch (Exception e) {
            logger.error("Error al obtener par de llaves como DTO", e);
            return KeyPairResponse.error("Error al obtener par de llaves");
        }
    }

    /**
     * Genera nuevas llaves RSA
     * 
     * @return boolean true si se generaron exitosamente
     */
    public boolean generateNewKeys() {
        try {
            logger.warn("Generando nuevas llaves RSA por solicitud");
            rsaKeyProvider.rotateKeys();
            return true;
        } catch (Exception e) {
            logger.error("Error al generar nuevas llaves", e);
            return false;
        }
    }

    /**
     * Obtiene la llave pública en formato JWKS (JSON Web Key Set)
     * Cumple con el estándar RFC 7517 para distribución de claves públicas
     * 
     * @return JwksResponse con la llave pública en formato JWKS
     */
    public JwksResponse getPublicKeyAsJwks() {
        try {
            RSAPublicKey publicKey = rsaKeyProvider.getPublicKey();
            String publicKeyPem = rsaKeyProvider.getPublicKeyAsPem();
            
            // Generar keyId consistente basado en la llave pública
            String keyId = UUID.nameUUIDFromBytes(publicKeyPem.getBytes()).toString();
            
            // Extraer componentes RSA para JWKS
            BigInteger modulus = publicKey.getModulus();
            BigInteger exponent = publicKey.getPublicExponent();
            
            // Convertir a Base64URL sin padding
            String modulusBase64 = encodeBase64Url(modulus.toByteArray());
            String exponentBase64 = encodeBase64Url(exponent.toByteArray());
            
            // Crear JWK key
            JwkKey jwkKey = new JwkKey(
                "RSA",           // kty: Key Type
                "sig",           // use: Public Key Use (signature)
                keyId,           // kid: Key ID
                "RS256",         // alg: Algorithm
                modulusBase64,   // n: Modulus
                exponentBase64   // e: Exponent
            );
            
            // Crear JWKS response con un solo key
            JwksResponse jwksResponse = new JwksResponse(Arrays.asList(jwkKey));
            
            logger.debug("Llave pública generada en formato JWKS con keyId: {}", keyId);
            return jwksResponse;
            
        } catch (Exception e) {
            logger.error("Error al generar JWKS", e);
            throw new RuntimeException("Error al generar JWKS", e);
        }
    }

    /**
     * Codifica bytes en Base64URL sin padding según RFC 7515
     * 
     * @param bytes bytes a codificar
     * @return String codificado en Base64URL
     */
    private String encodeBase64Url(byte[] bytes) {
        // Remover bytes de signo para números positivos
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        
        return Base64.getUrlEncoder()
                     .withoutPadding()
                     .encodeToString(bytes);
    }
}