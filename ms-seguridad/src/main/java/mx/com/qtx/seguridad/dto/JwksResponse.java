package mx.com.qtx.seguridad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * Respuesta JWKS (JSON Web Key Set) estándar RFC 7517
 * Contiene un array de claves públicas para validación JWT
 */
public class JwksResponse {
    
    @JsonProperty("keys")
    private List<JwkKey> keys;

    public JwksResponse() {}

    public JwksResponse(List<JwkKey> keys) {
        this.keys = keys;
    }

    public List<JwkKey> getKeys() {
        return keys;
    }

    public void setKeys(List<JwkKey> keys) {
        this.keys = keys;
    }

    /**
     * Verifica si la respuesta contiene al menos una clave válida
     */
    public boolean hasValidKeys() {
        return keys != null && !keys.isEmpty() && keys.stream().allMatch(this::isValidKey);
    }

    /**
     * Verifica si una clave JWK es válida
     */
    private boolean isValidKey(JwkKey key) {
        return key != null &&
               key.getKeyType() != null &&
               key.getKeyId() != null &&
               key.getModulus() != null &&
               key.getExponent() != null;
    }

    /**
     * Obtiene el número total de claves en el set
     */
    public int getKeyCount() {
        return keys != null ? keys.size() : 0;
    }

    /**
     * Busca una clave específica por su ID
     */
    public JwkKey findKeyById(String keyId) {
        if (keys == null || keyId == null) {
            return null;
        }
        return keys.stream()
                   .filter(key -> keyId.equals(key.getKeyId()))
                   .findFirst()
                   .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwksResponse that = (JwksResponse) o;
        return Objects.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }

    @Override
    public String toString() {
        return "JwksResponse{" +
                "keys=" + (keys != null ? keys.size() : 0) + " keys" +
                '}';
    }
}