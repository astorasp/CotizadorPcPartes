package mx.com.qtx.seguridad.controller;

import mx.com.qtx.seguridad.dto.PublicKeyResponse;
import mx.com.qtx.seguridad.dto.KeyPairResponse;
import mx.com.qtx.seguridad.dto.JwksResponse;
import mx.com.qtx.seguridad.service.KeyManagementService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestión de llaves RSA
 * Endpoints públicos y privados según configuración de seguridad
 */
@RestController
@RequestMapping("/keys")
public class KeyController {

    private final KeyManagementService keyManagementService;

    public KeyController(KeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }


    /**
     * Obtener llave pública en formato JWKS estándar (acceso público)
     * Endpoint JWKS compatible con estándar RFC 7517
     * Usado por otros servicios para validar tokens JWT de forma estándar
     * 
     * NOTA: Este endpoint tiene rate limiting aplicado para prevenir abuso.
     * Límites: 60 requests/minuto y 1000 requests/hora por IP.
     * 
     * @return JwksResponse con llave pública en formato JWKS
     */
    @GetMapping("/jwks")
    public ResponseEntity<JwksResponse> getPublicKeyAsJwks() {
        try {
            JwksResponse jwksResponse = keyManagementService.getPublicKeyAsJwks();
            
            if (jwksResponse != null && jwksResponse.hasValidKeys()) {
                return ResponseEntity.ok()
                    .header("Cache-Control", "public, max-age=300") // Cache por 5 minutos
                    .header("X-Content-Type-Options", "nosniff")
                    .body(jwksResponse);
            } else {
                // Crear respuesta de error vacía JWKS
                JwksResponse errorResponse = new JwksResponse(java.util.Collections.emptyList());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorResponse);
            }
            
        } catch (Exception e) {
            // Crear respuesta de error vacía JWKS
            JwksResponse errorResponse = new JwksResponse(java.util.Collections.emptyList());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Obtener llave privada (requiere ADMIN)
     * ¡CUIDADO! Endpoint sensible que expone llave privada
     * 
     * @return KeyPairResponse solo con llave privada
     */
    @GetMapping("/private")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPrivateKey() {
        try {
            KeyPairResponse keyPairResponse = keyManagementService.getKeyPairPem();
            
            if (keyPairResponse != null && keyPairResponse.hasPrivateKey()) {
                // Crear respuesta solo con llave privada
                Map<String, Object> response = new HashMap<>();
                response.put("privateKey", keyPairResponse.getPrivateKey());
                response.put("algorithm", keyPairResponse.getAlgorithm());
                response.put("keySize", keyPairResponse.getKeySize());
                response.put("keyFormat", keyPairResponse.getKeyFormat());
                response.put("generatedAt", keyPairResponse.getGeneratedAt());
                response.put("keyId", keyPairResponse.getKeyId());
                response.put("usage", "JWT signing (private key)");
                response.put("retrievedAt", java.time.LocalDateTime.now());
                response.put("warning", "¡INFORMACIÓN SENSIBLE! Esta es la llave privada para firmar JWT. Mantenga segura.");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "private_key_unavailable");
                error.put("message", "No se pudo obtener la llave privada");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "private_key_error");
            error.put("message", "Error interno al obtener la llave privada");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Generar nuevas llaves RSA (requiere ADMIN)
     * Rota el par de llaves actual por uno nuevo
     * 
     * @return Confirmación de generación
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateNewKeys() {
        try {
            // Obtener información del par de llaves anterior (si existe)
            String previousKeyId = null;
            try {
                PublicKeyResponse previousKey = keyManagementService.getPublicKeyPem();
                if (previousKey != null) {
                    previousKeyId = previousKey.getKeyId();
                }
            } catch (Exception e) {
                // No hay llaves previas o error al obtenerlas
            }
            
            // Generar nuevas llaves
            keyManagementService.generateNewKeys();
            
            // Obtener información de las nuevas llaves
            PublicKeyResponse newPublicKey = keyManagementService.getPublicKeyPem();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Nuevas llaves RSA generadas exitosamente");
            response.put("status", "success");
            response.put("previousKeyId", previousKeyId);
            response.put("newKeyId", newPublicKey != null ? newPublicKey.getKeyId() : null);
            response.put("algorithm", "RSA");
            response.put("keySize", "2048");
            response.put("generatedAt", java.time.LocalDateTime.now());
            response.put("warning", "Llaves rotadas. Los tokens firmados con la llave anterior ya no serán válidos.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "key_generation_error");
            error.put("message", "Error al generar nuevas llaves RSA");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener par completo de llaves (requiere ADMIN)
     * ¡MÁXIMO CUIDADO! Endpoint que expone tanto llave pública como privada
     * 
     * @return KeyPairResponse completo
     */
    @GetMapping("/keypair")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KeyPairResponse> getKeyPair() {
        try {
            KeyPairResponse keyPairResponse = keyManagementService.getKeyPairPem();
            
            if (keyPairResponse != null && keyPairResponse.isCompleteKeyPair()) {
                return ResponseEntity.ok(keyPairResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(KeyPairResponse.error("No se pudo obtener el par de llaves completo"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(KeyPairResponse.error("Error interno al obtener el par de llaves"));
        }
    }

    /**
     * Obtener información básica de las llaves actuales (requiere ADMIN)
     * No expone las llaves, solo metadatos
     * 
     * @return Información de las llaves sin contenido sensible
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getKeyInfo() {
        try {
            PublicKeyResponse publicKeyInfo = keyManagementService.getPublicKeyPem();
            
            if (publicKeyInfo != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("keyId", publicKeyInfo.getKeyId());
                info.put("algorithm", publicKeyInfo.getAlgorithm());
                info.put("keySize", publicKeyInfo.getKeySize());
                info.put("keyFormat", publicKeyInfo.getKeyFormat());
                info.put("generatedAt", publicKeyInfo.getGeneratedAt());
                info.put("usage", publicKeyInfo.getUsage());
                info.put("hasPublicKey", publicKeyInfo.hasPublicKey());
                info.put("isValidFormat", publicKeyInfo.isValidFormat());
                info.put("retrievedAt", java.time.LocalDateTime.now());
                
                return ResponseEntity.ok(info);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "key_info_unavailable");
                error.put("message", "No se pudo obtener información de las llaves");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "key_info_error");
            error.put("message", "Error al obtener información de las llaves");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint de salud para verificar el servicio de llaves
     * 
     * @return Estado del servicio de gestión de llaves
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean keysAvailable = false;
            String keyId = null;
            
            try {
                PublicKeyResponse publicKey = keyManagementService.getPublicKeyPem();
                keysAvailable = publicKey != null && publicKey.hasPublicKey();
                keyId = publicKey != null ? publicKey.getKeyId() : null;
            } catch (Exception e) {
                // Keys not available
            }
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", keysAvailable ? "UP" : "DOWN");
            health.put("service", "key-management");
            health.put("keysAvailable", keysAvailable);
            health.put("currentKeyId", keyId);
            health.put("timestamp", java.time.LocalDateTime.now());
            
            HttpStatus status = keysAvailable ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(health);
            
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "ERROR");
            health.put("service", "key-management");
            health.put("error", "Health check failed");
            health.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
        }
    }
}