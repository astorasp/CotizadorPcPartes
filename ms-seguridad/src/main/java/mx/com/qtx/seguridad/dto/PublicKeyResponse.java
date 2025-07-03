package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de llave pública en formato PEM
 * Utilizado en el endpoint público /keys/public
 */
public class PublicKeyResponse {

    @JsonProperty("publicKey")
    private String publicKey; // Llave pública en formato PEM

    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("keySize")
    private String keySize;

    @JsonProperty("keyFormat")
    private String keyFormat;

    @JsonProperty("generatedAt")
    private LocalDateTime generatedAt;

    @JsonProperty("keyId")
    private String keyId; // Identificador único de la llave

    @JsonProperty("usage")
    private String usage;

    @JsonProperty("retrievedAt")
    private LocalDateTime retrievedAt;

    // Constructor por defecto
    public PublicKeyResponse() {
        this.algorithm = "RSA";
        this.keySize = "2048";
        this.keyFormat = "PEM";
        this.usage = "JWT signature verification";
        this.retrievedAt = LocalDateTime.now();
    }

    // Constructor completo
    public PublicKeyResponse(String publicKey, LocalDateTime generatedAt, String keyId) {
        this();
        this.publicKey = publicKey;
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

    // Métodos de utilidad
    @JsonIgnore
    public boolean hasPublicKey() {
        return publicKey != null && !publicKey.trim().isEmpty();
    }

    @JsonIgnore
    public boolean isValidFormat() {
        return publicKey != null && 
               publicKey.contains("-----BEGIN PUBLIC KEY-----") && 
               publicKey.contains("-----END PUBLIC KEY-----");
    }

    // Método estático para crear respuesta de error
    public static PublicKeyResponse error(String message) {
        PublicKeyResponse response = new PublicKeyResponse();
        response.setPublicKey(null);
        response.setUsage("Error: " + message);
        return response;
    }

    // toString sin mostrar la llave completa (muy larga)
    @Override
    public String toString() {
        String keyPreview = publicKey != null ? 
            publicKey.substring(0, Math.min(50, publicKey.length())) + "..." : 
            "null";
            
        return "PublicKeyResponse{" +
                "publicKey='" + keyPreview + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", keySize='" + keySize + '\'' +
                ", keyFormat='" + keyFormat + '\'' +
                ", generatedAt=" + generatedAt +
                ", keyId='" + keyId + '\'' +
                ", usage='" + usage + '\'' +
                ", retrievedAt=" + retrievedAt +
                '}';
    }
}