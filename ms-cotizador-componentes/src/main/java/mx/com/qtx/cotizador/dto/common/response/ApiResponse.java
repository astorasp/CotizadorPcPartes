package mx.com.qtx.cotizador.dto.common.response;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase genérica que envuelve las respuestas de la API.
 * Proporciona una estructura estándar para todas las respuestas HTTP,
 * incluyendo código de estado, mensaje descriptivo y datos opcionales.
 *
 * Esta clase es utilizada por todos los controladores para mantener
 * consistencia en las respuestas de la API.
 *
 * @param <T> Tipo de los datos contenidos en la respuesta
 * @author [Nombre del autor]
 * @version 1.0
 */
@Data
public class ApiResponse<T> {
    /** Código de respuesta que indica el estado de la operación */
    private String codigo;
    /** Mensaje descriptivo del resultado de la operación */
    private String mensaje;
    
    /** Datos opcionales retornados por la operación */
    @JsonProperty("datos")
    private T datos;

    /**
     * Constructor completo para respuestas con datos.
     *
     * @param codigo Código de estado de la respuesta
     * @param mensaje Mensaje descriptivo del resultado
     * @param datos Datos asociados a la respuesta
     */
    public ApiResponse(String codigo, String mensaje, T datos) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    /**
     * Constructor para respuestas sin datos (solo código y mensaje).
     *
     * @param codigo Código de estado de la respuesta
     * @param mensaje Mensaje descriptivo del resultado
     */
    public ApiResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }
    
}
