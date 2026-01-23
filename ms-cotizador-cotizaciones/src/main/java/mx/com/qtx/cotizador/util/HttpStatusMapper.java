package mx.com.qtx.cotizador.util;

import org.springframework.http.HttpStatus;

/**
 * Utilidad para mapear códigos de error del dominio a códigos HTTP estándar.
 * <p>
 * Esta clase utilitaria centraliza la lógica de mapeo entre los códigos de error
 * definidos en el enum {@link Errores} y los códigos de estado HTTP estándar.
 * Garantiza consistencia en las respuestas HTTP del microservicio y sigue
 * las mejores prácticas de APIs REST.
 * </p>
 *
 * <h3>Arquitectura de manejo de errores:</h3>
 * <p>
 * Esta clase es parte fundamental de la arquitectura de manejo de errores:
 * </p>
 * <pre>
 * Errores del Dominio → HttpStatusMapper → Respuesta HTTP
 *     (Errores)       →    (mapeo)     →  (JSON + Status)
 * </pre>
 *
 * <h3>Reglas de mapeo definidas:</h3>
 * <table border="1">
 *   <tr><th>Código de Error</th><th>HTTP Status</th><th>Significado</th></tr>
 *   <tr><td>"0"</td><td>200 OK</td><td>Operación exitosa</td></tr>
 *   <tr><td>"3"</td><td>500 Internal Server Error</td><td>Error interno del servicio</td></tr>
 *   <tr><td>Otros</td><td>400 Bad Request</td><td>Errores de validación o cliente</td></tr>
 * </table>
 *
 * <h3>Ejemplos de uso:</h3>
 * <pre>{@code
 * // En un controlador REST
 * @PostMapping("/cotizaciones")
 * public ResponseEntity<ApiResponse<CotizacionResponse>> crearCotizacion(...) {
 *     ApiResponse<CotizacionResponse> response = cotizacionServicio.guardarCotizacion(request);
 *
 *     // Mapeo automático del código de error a HTTP status
 *     return ResponseEntity
 *         .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
 *         .body(response);
 * }
 *
 * // Usando directamente el enum
 * return ResponseEntity
 *     .status(HttpStatusMapper.mapearCodigoAHttpStatus(Errores.COTIZACION_NO_ENCONTRADA))
 *     .body(errorResponse);
 * }</pre>
 *
 * <h3>Beneficios:</h3>
 * <ul>
 *   <li><strong>Consistencia:</strong> Mapeo uniforme en toda la aplicación</li>
 *   <li><strong>Mantenibilidad:</strong> Centralización de reglas de mapeo</li>
 *   <li><strong>Legibilidad:</strong> Código más claro en controladores</li>
 *   <li><strong>Flexibilidad:</strong> Fácil modificar reglas de mapeo</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.util.Errores
 * @see mx.com.qtx.cotizador.controlador.CotizacionController
 * @see org.springframework.http.HttpStatus
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