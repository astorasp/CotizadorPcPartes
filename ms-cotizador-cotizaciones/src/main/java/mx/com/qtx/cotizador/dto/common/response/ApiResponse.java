package mx.com.qtx.cotizador.dto.common.response;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase genérica para respuestas estandarizadas de la API del sistema CotizadorPcPartes.
 * <p>
 * Esta clase implementa el patrón de respuesta consistente utilizado en toda la API REST,
 * proporcionando una estructura uniforme para comunicar resultados de operaciones al cliente.
 * La clase es genérica ({@code <T>}) permitiendo encapsular cualquier tipo de datos en la respuesta.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Consistencia:</strong> Todas las respuestas de la API siguen el mismo formato</li>
 *   <li><strong>Flexibilidad:</strong> Soporta cualquier tipo de datos a través de generics</li>
 *   <li><strong>Información clara:</strong> Código, mensaje y datos estructurados</li>
 *   <li><strong>Compatibilidad:</strong> Anotaciones Jackson para serialización JSON correcta</li>
 * </ul>
 *
 * <h3>Estructura de respuesta:</h3>
 * <p>
 * Toda respuesta de la API sigue esta estructura JSON:
 * </p>
 * <pre>{@code
 * {
 *   "codigo": "0",
 *   "mensaje": "Operación exitosa",
 *   "datos": { ... } // Objeto específico según la operación
 * }
 * }</pre>
 *
 * <h3>Códigos de respuesta estándar:</h3>
 * <table border="1">
 *   <tr><th>Código</th><th>Significado</th><th>HTTP Status</th></tr>
 *   <tr><td>"0"</td><td>Operación exitosa</td><td>200 OK</td></tr>
 *   <tr><td>"3"</td><td>Error interno del servidor</td><td>500 Internal Server Error</td></tr>
 *   <tr><td>"1", "2", "4+", etc.</td><td>Errores específicos del cliente</td><td>400 Bad Request</td></tr>
 * </table>
 *
 * <h3>Ejemplos de uso:</h3>
 * <pre>{@code
 * // Respuesta exitosa con datos
 * ApiResponse<ComponenteResponse> exito = new ApiResponse<>("0", "Componente creado exitosamente", componente);
 *
 * // Respuesta de error sin datos
 * ApiResponse<Void> error = new ApiResponse<>("3", "Error interno del servidor");
 *
 * // Respuesta con lista de componentes
 * List<ComponenteResponse> componentes = obtenerComponentes();
 * ApiResponse<List<ComponenteResponse>> respuesta = new ApiResponse<>("0", "Componentes obtenidos", componentes);
 * }</pre>
 *
 * <h3>Características técnicas:</h3>
 * <ul>
 *   <li><strong>Generic Type:</strong> {@code <T>} permite tipado fuerte de los datos</li>
 *   <li><strong>Lombok:</strong> Anotación {@code @Data} genera getters, setters, toString, equals, hashCode</li>
 *   <li><strong>Jackson:</strong> Anotación {@code @JsonProperty} mapea "datos" en JSON</li>
 *   <li><strong>Constructores múltiples:</strong> Soporta respuestas con y sin datos</li>
 *   <li><strong>Null Safety:</strong> Maneja correctamente valores null en datos</li>
 * </ul>
 *
 * <h3>Integración con controladores:</h3>
 * <p>
 * Esta clase se utiliza en todos los controladores REST para mantener consistencia:
 * </p>
 * <pre>{@code
 * @PostMapping("/componentes")
 * public ResponseEntity<ApiResponse<ComponenteResponse>> crearComponente(@Valid @RequestBody request) {
 *     try {
 *         ComponenteResponse componente = servicio.crearComponente(request);
 *         ApiResponse<ComponenteResponse> respuesta = new ApiResponse<>("0", "Componente creado", componente);
 *         return ResponseEntity.ok(respuesta);
 *     } catch (Exception e) {
 *         ApiResponse<Void> error = new ApiResponse<>("3", "Error al crear componente");
 *         return ResponseEntity.internalServerError().body(error);
 *     }
 * }
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> Tipo genérico de los datos contenidos en la respuesta
 * @see lombok.Data
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Data
public class ApiResponse<T> {

    /**
     * Código de respuesta que indica el resultado de la operación.
     * <p>
     * Este campo contiene un código alfanumérico que identifica el tipo de resultado
     * de la operación realizada. Los códigos están estandarizados en todo el sistema
     * y permiten al cliente identificar rápidamente el tipo de respuesta.
     * </p>
     * <p>
     * Los códigos más comunes son:
     * <ul>
     *   <li>"0": Operación exitosa</li>
     *   <li>"3": Error interno del servidor</li>
     *   <li>Otros códigos específicos de error</li>
     * </ul>
     * </p>
     */
    private String codigo;

    /**
     * Mensaje descriptivo que explica el resultado de la operación.
     * <p>
     * Este campo proporciona una descripción legible por humanos del resultado
     * de la operación. El mensaje debe ser claro, conciso y útil para que el
     * cliente pueda entender qué sucedió durante la operación.
     * </p>
     * <p>
     * Ejemplos de mensajes:
     * <ul>
     *   <li>"Componente creado exitosamente"</li>
     *   <li>"El componente solicitado no existe"</li>
     *   <li>"Error interno del servidor"</li>
     * </ul>
     * </p>
     */
    private String mensaje;

    /**
     * Datos específicos de la respuesta, pueden ser de cualquier tipo.
     * <p>
     * Este campo genérico contiene los datos específicos de la operación realizada.
     * Puede contener un objeto individual, una lista de objetos, o ser null en caso
     * de errores. La anotación {@code @JsonProperty("datos")} asegura que se serialice
     * correctamente en JSON con el nombre "datos".
     * </p>
     * <p>
     * Tipos de datos comunes:
     * <ul>
     *   <li>{@code ComponenteResponse} - Para operaciones con un componente</li>
     *   <li>{@code List<ComponenteResponse>} - Para listas de componentes</li>
     *   <li>{@code CotizacionResponse} - Para respuestas de cotizaciones</li>
     *   <li>{@code Void} - Para respuestas sin datos (errores)</li>
     * </ul>
     * </p>
     */
    @JsonProperty("datos")
    private T datos;

    /**
     * Constructor completo para respuestas con datos.
     * <p>
     * Crea una instancia completa de ApiResponse con código, mensaje y datos.
     * Este constructor se utiliza cuando la operación fue exitosa y se necesitan
     * enviar datos al cliente.
     * </p>
     *
     * @param codigo Código de respuesta que indica el resultado
     * @param mensaje Mensaje descriptivo del resultado
     * @param datos Objeto de datos específico de la operación (puede ser null)
     */
    public ApiResponse(String codigo, String mensaje, T datos) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    /**
     * Constructor para respuestas sin datos (típicamente errores).
     * <p>
     * Crea una instancia de ApiResponse con código y mensaje, pero sin datos.
     * Este constructor se utiliza principalmente para respuestas de error donde
     * no hay datos útiles que enviar al cliente.
     * </p>
     *
     * @param codigo Código de respuesta que indica el error
     * @param mensaje Mensaje descriptivo del error ocurrido
     */
    public ApiResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

}
