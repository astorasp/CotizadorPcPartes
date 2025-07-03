package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de par de llaves (pública y privada) en formato PEM
 * ¡CUIDADO! Contiene información sensible (llave privada)
 * Solo debe ser utilizado por administradores autorizados
 */
public class KeyPairResponse {

    @JsonProperty("publicKey")
    private String publicKey; // Llave pública en formato PEM

    @JsonProperty("privateKey")
    private String privateKey; // Llave privada en formato PEM

    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("keySize")
    private String keySize;

    @JsonProperty("keyFormat")
    private String keyFormat;

    @JsonProperty("generatedAt")
    private LocalDateTime generatedAt;

    @JsonProperty("keyId")
    private String keyId; // Identificador único del par de llaves

    @JsonProperty("usage")
    private String usage;

    @JsonProperty("retrievedAt")
    private LocalDateTime retrievedAt;

    @JsonProperty("warning")
    private String warning;

    // Constructor por defecto
    public KeyPairResponse() {
        this.algorithm = "RSA";
        this.keySize = "2048";
        this.keyFormat = "PEM";
        this.usage = "JWT signing and verification";
        this.retrievedAt = LocalDateTime.now();
        this.warning = "¡ALERTA! Esta respuesta contiene información sensible (llave privada). Mantenga segura esta información.";
    }

    // Constructor completo
    public KeyPairResponse(String publicKey, String privateKey, 
                          LocalDateTime generatedAt, String keyId) {
        this();
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.generatedAt = generatedAt;
        this.keyId = keyId;
    }

    // Getters y Setters
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getKeySize() {
        return keySize;
    }

    public void setKeySize(String keySize) {
        this.keySize = keySize;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public LocalDateTime getRetrievedAt() {
        return retrievedAt;
    }

    public void setRetrievedAt(LocalDateTime retrievedAt) {
        this.retrievedAt = retrievedAt;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    // Métodos de utilidad
    @JsonIgnore
    public boolean hasPublicKey() {
        return publicKey != null && !publicKey.trim().isEmpty();
    }

    @JsonIgnore
    public boolean hasPrivateKey() {
        return privateKey != null && !privateKey.trim().isEmpty();
    }

    @JsonIgnore
    public boolean isCompleteKeyPair() {
        return hasPublicKey() && hasPrivateKey();
    }

    @JsonIgnore
    public boolean isValidFormat() {
        boolean validPublic = publicKey != null && 
                              publicKey.contains("-----BEGIN PUBLIC KEY-----") && 
                              publicKey.contains("-----END PUBLIC KEY-----");
        
        boolean validPrivate = privateKey != null && 
                               privateKey.contains("-----BEGIN PRIVATE KEY-----") && 
                               privateKey.contains("-----END PRIVATE KEY-----");
        
        return validPublic && validPrivate;
    }

    // Método para obtener solo la llave pública (sin la privada)
    @JsonIgnore
    public PublicKeyResponse getPublicKeyOnly() {
        return new PublicKeyResponse(publicKey, generatedAt, keyId);
    }

    // Método estático para crear respuesta de error
    public static KeyPairResponse error(String message) {
        KeyPairResponse response = new KeyPairResponse();
        response.setPublicKey(null);
        response.setPrivateKey(null);
        response.setUsage("Error: " + message);
        response.setWarning("Error al obtener par de llaves: " + message);
        return response;
    }

    // toString sin mostrar las llaves completas (información sensible)
    @Override
    public String toString() {
        String publicPreview = publicKey != null ? 
            publicKey.substring(0, Math.min(30, publicKey.length())) + "..." : 
            "null";
        
        String privatePreview = privateKey != null ? "[PRIVATE_KEY_PRESENT]" : "null";
            
        return "KeyPairResponse{" +
                "publicKey='" + publicPreview + '\'' +
                ", privateKey='" + privatePreview + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", keySize='" + keySize + '\'' +
                ", keyFormat='" + keyFormat + '\'' +
                ", generatedAt=" + generatedAt +
                ", keyId='" + keyId + '\'' +
                ", usage='" + usage + '\'' +
                ", retrievedAt=" + retrievedAt +
                ", warning='" + warning + '\'' +
                '}';
    }
}