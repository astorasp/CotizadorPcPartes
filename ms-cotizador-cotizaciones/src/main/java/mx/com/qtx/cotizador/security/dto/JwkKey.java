package mx.com.qtx.cotizador.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Representa una clave individual en el formato JWKS (JSON Web Key Set)
 * Cumple con el estándar RFC 7517 para distribución de claves públicas
 */
public class JwkKey {
    
    @JsonProperty("kty")
    private String keyType;
    
    @JsonProperty("use")
    private String publicKeyUse;
    
    @JsonProperty("kid")
    private String keyId;
    
    @JsonProperty("alg")
    private String algorithm;
    
    @JsonProperty("n")
    private String modulus;
    
    @JsonProperty("e")
    private String exponent;

    public JwkKey() {}

    public JwkKey(String keyType, String publicKeyUse, String keyId, String algorithm, String modulus, String exponent) {
        this.keyType = keyType;
        this.publicKeyUse = publicKeyUse;
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getPublicKeyUse() {
        return publicKeyUse;
    }

    public void setPublicKeyUse(String publicKeyUse) {
        this.publicKeyUse = publicKeyUse;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getModulus() {
        return modulus;
    }

    public void setModulus(String modulus) {
        this.modulus = modulus;
    }

    public String getExponent() {
        return exponent;
    }

    public void setExponent(String exponent) {
        this.exponent = exponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwkKey jwkKey = (JwkKey) o;
        return Objects.equals(keyId, jwkKey.keyId) &&
               Objects.equals(keyType, jwkKey.keyType) &&
               Objects.equals(publicKeyUse, jwkKey.publicKeyUse) &&
               Objects.equals(algorithm, jwkKey.algorithm) &&
               Objects.equals(modulus, jwkKey.modulus) &&
               Objects.equals(exponent, jwkKey.exponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyType, publicKeyUse, keyId, algorithm, modulus, exponent);
    }

    @Override
    public String toString() {
        return "JwkKey{" +
                "keyType='" + keyType + '\'' +
                ", publicKeyUse='" + publicKeyUse + '\'' +
                ", keyId='" + keyId + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", modulus='" + (modulus != null ? modulus.substring(0, Math.min(10, modulus.length())) + "..." : null) + '\'' +
                ", exponent='" + exponent + '\'' +
                '}';
    }
}