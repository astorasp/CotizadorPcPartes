package mx.com.qtx.seguridad.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Component
public class RsaKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(RsaKeyProvider.class);
    
    // Tamaño de la llave RSA (2048 bits es recomendado para seguridad)
    private static final int KEY_SIZE = 2048;
    
    // Algoritmo para generación de llaves
    private static final String ALGORITHM = "RSA";
    
    // Par de llaves almacenado en memoria únicamente
    private volatile KeyPair currentKeyPair;
    
    // Timestamp de cuando se generó el par de llaves actual
    private volatile long keyGenerationTimestamp;

    /**
     * Constructor que genera automáticamente el primer par de llaves
     */
    public RsaKeyProvider() {
        generateKeyPair();
    }

    /**
     * Genera un nuevo par de llaves RSA y lo almacena en memoria
     * Este método es thread-safe
     */
    public synchronized void generateKeyPair() {
        try {
            logger.info("Generando nuevo par de llaves RSA de {} bits", KEY_SIZE);
            
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
            
            this.currentKeyPair = keyPairGenerator.generateKeyPair();
            this.keyGenerationTimestamp = System.currentTimeMillis();
            
            logger.info("Par de llaves RSA generado exitosamente en {}", 
                       new java.util.Date(keyGenerationTimestamp));
                       
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error al generar par de llaves RSA: Algoritmo {} no disponible", ALGORITHM, e);
            throw new RuntimeException("Error fatal: No se puede generar llaves RSA", e);
        } catch (Exception e) {
            logger.error("Error inesperado al generar par de llaves RSA", e);
            throw new RuntimeException("Error fatal: No se puede generar llaves RSA", e);
        }
    }

    /**
     * Obtiene la llave pública RSA actual
     * @return RSAPublicKey para verificación de tokens
     */
    public RSAPublicKey getPublicKey() {
        ensureKeyPairExists();
        return (RSAPublicKey) currentKeyPair.getPublic();
    }

    /**
     * Obtiene la llave privada RSA actual
     * @return RSAPrivateKey para firma de tokens
     */
    public RSAPrivateKey getPrivateKey() {
        ensureKeyPairExists();
        return (RSAPrivateKey) currentKeyPair.getPrivate();
    }

    /**
     * Obtiene la llave pública en formato PEM Base64
     * @return String con la llave pública en formato PEM
     */
    public String getPublicKeyAsPem() {
        RSAPublicKey publicKey = getPublicKey();
        byte[] encoded = publicKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        
        return "-----BEGIN PUBLIC KEY-----\n" +
               formatBase64ForPem(base64) +
               "\n-----END PUBLIC KEY-----";
    }

    /**
     * Obtiene la llave privada en formato PEM Base64
     * @return String con la llave privada en formato PEM
     */
    public String getPrivateKeyAsPem() {
        RSAPrivateKey privateKey = getPrivateKey();
        byte[] encoded = privateKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        
        return "-----BEGIN PRIVATE KEY-----\n" +
               formatBase64ForPem(base64) +
               "\n-----END PRIVATE KEY-----";
    }

    /**
     * Rota las llaves generando un nuevo par
     * Método thread-safe que invalida las llaves anteriores
     */
    public synchronized void rotateKeys() {
        logger.warn("Iniciando rotación de llaves RSA");
        long oldTimestamp = keyGenerationTimestamp;
        
        generateKeyPair();
        
        logger.warn("Rotación de llaves completada. Llaves anteriores generadas en {} han sido invalidadas", 
                   new java.util.Date(oldTimestamp));
    }

    /**
     * Obtiene el timestamp de cuando se generó el par de llaves actual
     * @return long timestamp en milisegundos
     */
    public long getKeyGenerationTimestamp() {
        return keyGenerationTimestamp;
    }

    /**
     * Obtiene información sobre las llaves actuales
     * @return String con información de depuración (sin datos sensibles)
     */
    public String getKeyInfo() {
        ensureKeyPairExists();
        
        RSAPublicKey publicKey = getPublicKey();
        return String.format(
            "RSA Key Info - Algorithm: %s, KeySize: %d bits, Generated: %s, Modulus Length: %d",
            publicKey.getAlgorithm(),
            publicKey.getModulus().bitLength(),
            new java.util.Date(keyGenerationTimestamp),
            publicKey.getModulus().bitLength()
        );
    }

    /**
     * Verifica si las llaves están disponibles
     * @return true si hay un par de llaves válido
     */
    public boolean areKeysAvailable() {
        return currentKeyPair != null && 
               currentKeyPair.getPublic() != null && 
               currentKeyPair.getPrivate() != null;
    }

    /**
     * Verifica que exista un par de llaves válido
     * Genera uno nuevo si no existe
     */
    private void ensureKeyPairExists() {
        if (!areKeysAvailable()) {
            logger.warn("Par de llaves no disponible, generando nuevo par");
            generateKeyPair();
        }
    }

    /**
     * Formatea una cadena Base64 para el formato PEM (líneas de 64 caracteres)
     * @param base64 String Base64 sin formato
     * @return String Base64 formateado para PEM
     */
    private String formatBase64ForPem(String base64) {
        StringBuilder formatted = new StringBuilder();
        int index = 0;
        while (index < base64.length()) {
            int endIndex = Math.min(index + 64, base64.length());
            formatted.append(base64, index, endIndex);
            if (endIndex < base64.length()) {
                formatted.append("\n");
            }
            index = endIndex;
        }
        return formatted.toString();
    }
}