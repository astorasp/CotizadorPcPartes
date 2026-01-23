package mx.com.qtx.cotizador.util;

import org.springframework.http.HttpStatus;

/**
 * Clase utilitaria para mapear códigos de error del aplicativo a códigos HTTP
 * 
 * Reglas de mapeo:
 * - Código "0" → HTTP 200 (OK)
 * - Código "3" → HTTP 500 (Internal Server Error)
 * - Todo lo demás → HTTP 400 (Bad Request)
 */
public final class HttpStatusMapper {
    
    /**
     * Mapea un código de error del aplicativo a un HttpStatus
     * 
     * @param codigoError Código de error del enum Errores
     * @return HttpStatus correspondiente
     */
    public static HttpStatus mapearCodigoAHttpStatus(String codigoError) {
        if (codigoError == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return switch (codigoError) {
            case "0" -> HttpStatus.OK;              // Éxito
            case "3" -> HttpStatus.INTERNAL_SERVER_ERROR;  // Error interno del servicio
            default -> HttpStatus.BAD_REQUEST;      // Todo lo demás (errores de cliente)
        };
    }
    
    /**
     * Mapea usando el enum Errores directamente
     * 
     * @param error Enum Errores
     * @return HttpStatus correspondiente
     */
    public static HttpStatus mapearCodigoAHttpStatus(Errores error) {
        return mapearCodigoAHttpStatus(error.getCodigo());
    }
    
    // Constructor privado para evitar instanciación
    private HttpStatusMapper() {
        throw new UnsupportedOperationException("Esta clase no debe ser instanciada");
    }
} 