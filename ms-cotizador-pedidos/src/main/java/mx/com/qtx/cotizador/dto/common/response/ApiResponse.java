package mx.com.qtx.cotizador.dto.common.response;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase genérica para respuestas estandarizadas de la API del microservicio ms-cotizador-pedidos.
 * <p>
 * Esta clase implementa el patrón de respuesta consistente utilizado en todo el microservicio
 * de pedidos, proporcionando una estructura uniforme para comunicar resultados de operaciones
 * relacionadas con pedidos, proveedores y gestión de componentes. La clase es genérica ({@code <T>})
 * permitiendo encapsular cualquier tipo de datos en la respuesta.
 * </p>
 *
 * <h3>Propósito principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Gestión de Pedidos:</strong> Respuestas para creación, consulta y modificación de pedidos</li>
 *   <li><strong>Gestión de Proveedores:</strong> Respuestas para operaciones CRUD de proveedores</li>
 *   <li><strong>Gestión de PCs:</strong> Respuestas para consultas de configuraciones de PC</li>
 *   <li><strong>Integración con Cotizaciones:</strong> Respuestas para generación de pedidos desde cotizaciones</li>
 *   <li><strong>Consistencia de API:</strong> Formato uniforme para todas las operaciones del microservicio</li>
 * </ul>
 *
 * <h3>Estructura de respuesta específica para pedidos:</h3>
 * <p>
 * Toda respuesta del microservicio de pedidos sigue esta estructura JSON:
 * </p>
 * <pre>{@code
 * {
 *   "codigo": "0",
 *   "mensaje": "Pedido creado exitosamente",
 *   "datos": {
 *     "numPedido": 12345,
 *     "fechaEmision": "2025-01-17",
 *     "proveedor": "PROV001",
 *     "total": 25000.00
 *   }
 * }
 * }</pre>
 *
 * <h3>Códigos de respuesta específicos para pedidos:</h3>
 * <table border="1">
 *   <tr><th>Código</th><th>Significado</th><th>HTTP Status</th><th>Ejemplo de Uso</th></tr>
 *   <tr><td>"0"</td><td>Operación exitosa</td><td>200 OK</td><td>Pedido creado correctamente</td></tr>
 *   <tr><td>"1"</td><td>Error de validación</td><td>400 Bad Request</td><td>Datos de pedido inválidos</td></tr>
 *   <tr><td>"3"</td><td>Error interno</td><td>500 Internal Server Error</td><td>Error en base de datos</td></tr>
 *   <tr><td>"4"</td><td>Acceso denegado</td><td>403 Forbidden</td><td>Sin permisos para operación</td></tr>
 *   <tr><td>"PED001"</td><td>Pedido no encontrado</td><td>404 Not Found</td><td>ID de pedido inexistente</td></tr>
 *   <tr><td>"PROV001"</td><td>Proveedor no encontrado</td><td>404 Not Found</td><td>Clave de proveedor inválida</td></tr>
 *   <tr><td>"COT001"</td><td>Cotización no encontrada</td><td>404 Not Found</td><td>ID de cotización inexistente</td></tr>
 * </table>
 *
 * <h3>Ejemplos de uso específicos para pedidos:</h3>
 * <pre>{@code
 * // Respuesta exitosa de creación de pedido
 * ApiResponse<PedidoResponse> exito = new ApiResponse<>("0",
 *     "Pedido generado exitosamente desde cotización", pedidoCreado);
 *
 * // Respuesta de error cuando proveedor no existe
 * ApiResponse<Void> error = new ApiResponse<>("PROV001",
 *     "El proveedor especificado no existe en el sistema");
 *
 * // Respuesta con lista de pedidos de un proveedor
 * List<PedidoResponse> pedidos = obtenerPedidosPorProveedor("PROV001");
 * ApiResponse<List<PedidoResponse>> respuesta = new ApiResponse<>("0",
 *     "Pedidos del proveedor obtenidos correctamente", pedidos);
 * }</pre>
 *
 * <h3>Características técnicas específicas para pedidos:</h3>
 * <ul>
 *   <li><strong>Generic Type {@code <T>}:</strong> Soporta respuestas con datos específicos del dominio de pedidos</li>
 *   <li><strong>Lombok @Data:</strong> Genera getters, setters, toString, equals, hashCode automáticamente</li>
 *   <li><strong>Jackson @JsonProperty:</strong> Serializa el campo de datos como "datos" en JSON</li>
 *   <li><strong>Constructores múltiples:</strong> Soporta respuestas con y sin datos según el caso de uso</li>
 *   <li><strong>Null Safety:</strong> Maneja correctamente valores null en datos de pedidos</li>
 *   <li><strong>Type Safety:</strong> Mantiene la integridad de tipos en respuestas de pedidos</li>
 * </ul>
 *
 * <h3>Integración con controladores de pedidos:</h3>
 * <p>
 * Esta clase se utiliza en todos los controladores REST del microservicio de pedidos:
 * </p>
 * <pre>{@code
 * @PostMapping("/pedidos")
 * public ResponseEntity<ApiResponse<PedidoResponse>> crearPedido(
 *         @Valid @RequestBody GenerarPedidoRequest request) {
 *     try {
 *         Pedido pedido = servicioPedido.generarPedidoDesdeCotizacion(request);
 *         PedidoResponse response = PedidoMapper.toResponse(pedido);
 *
 *         ApiResponse<PedidoResponse> apiResponse = new ApiResponse<>(
 *             "0", "Pedido generado exitosamente", response);
 *
 *         return ResponseEntity.ok(apiResponse);
 *
 *     } catch (ProveedorNoEncontradoException e) {
 *         ApiResponse<Void> error = new ApiResponse<>(
 *             "PROV001", "Proveedor no encontrado: " + request.getCveProveedor());
 *         return ResponseEntity.badRequest().body(error);
 *
 *     } catch (Exception e) {
 *         ApiResponse<Void> error = new ApiResponse<>(
 *             "3", "Error interno al generar pedido");
 *         return ResponseEntity.internalServerError().body(error);
 *     }
 * }
 * }</pre>
 *
 * <h3>Manejo de errores específicos de pedidos:</h3>
 * <p>
 * El microservicio maneja diversos tipos de errores específicos del dominio de pedidos:
 * </p>
 * <ul>
 *   <li><strong>Errores de Validación:</strong> Datos de pedido incompletos o inválidos</li>
 *   <li><strong>Errores de Negocio:</strong> Reglas de negocio violadas (proveedor inactivo, etc.)</li>
 *   <li><strong>Errores de Integración:</strong> Problemas con microservicios relacionados</li>
 *   <li><strong>Errores de Datos:</strong> Inconsistencias en base de datos de pedidos</li>
 *   <li><strong>Errores de Seguridad:</strong> Acceso no autorizado a operaciones de pedidos</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> Tipo genérico de los datos contenidos en la respuesta (PedidoResponse, ProveedorResponse, etc.)
 * @see lombok.Data
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse
 * @see mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse
 */
@Data
public class ApiResponse<T> {

    /**
     * Código de respuesta que indica el resultado de la operación en el microservicio de pedidos.
     * <p>
     * Este campo contiene un código alfanumérico que identifica el tipo de resultado
     * de la operación realizada en el contexto de gestión de pedidos. Los códigos están
     * estandarizados en todo el microservicio y permiten al cliente identificar rápidamente
     * el tipo de respuesta, especialmente útil para manejo de errores específicos de pedidos.
     * </p>
     * <p>
     * Los códigos más comunes en el contexto de pedidos son:
     * <ul>
     *   <li>"0": Operación exitosa (pedido creado, proveedor actualizado, etc.)</li>
     *   <li>"PED001": Pedido no encontrado o inexistente</li>
     *   <li>"PROV001": Proveedor no encontrado o clave inválida</li>
     *   <li>"COT001": Cotización no encontrada (para generación de pedidos)</li>
     *   <li>"3": Error interno del servidor en operaciones de pedidos</li>
     *   <li>"1": Error de validación en datos de pedidos</li>
     *   <li>"4": Acceso denegado a operaciones de pedidos</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Importancia en pedidos:</strong> Los códigos específicos permiten al cliente
     * implementar lógica de manejo de errores granular, especialmente útil para:
     * <ul>
     *   <li>Reintentar operaciones fallidas</li>
     *   <li>Mostrar mensajes específicos al usuario</li>
     *   <li>Implementar lógica de recuperación automática</li>
     *   <li>Reportar errores específicos a sistemas de monitoreo</li>
     * </ul>
     * </p>
     */
    private String codigo;

    /**
     * Mensaje descriptivo que explica el resultado de la operación de pedidos.
     * <p>
     * Este campo proporciona una descripción legible por humanos del resultado
     * de la operación realizada en el microservicio de pedidos. El mensaje debe
     * ser claro, conciso y útil para que el cliente pueda entender qué sucedió
     * durante la operación, especialmente en contextos de error o éxito.
     * </p>
     * <p>
     * Ejemplos de mensajes específicos para pedidos:
     * <ul>
     *   <li>"Pedido generado exitosamente desde cotización"</li>
     *   <li>"Proveedor PROV001 no encontrado en el sistema"</li>
     *   <li>"Error al validar datos del pedido"</li>
     *   <li>"Nivel de surtido debe estar entre 0 y 100"</li>
     *   <li>"Fecha de entrega no puede ser anterior a fecha de emisión"</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Características de los mensajes:</strong>
     * <ul>
     *   <li><strong>Específicos del dominio:</strong> Usan terminología de pedidos y proveedores</li>
     *   <li><strong>Accionables:</strong> Proporcionan información útil para resolver problemas</li>
     *   <li><strong>Consistentes:</strong> Siguen un patrón uniforme en todo el microservicio</li>
     *   <li><strong>Localizables:</strong> Preparados para internacionalización si es necesario</li>
     * </ul>
     * </p>
     */
    private String mensaje;

    /**
     * Datos específicos de la respuesta de operaciones de pedidos, pueden ser de cualquier tipo.
     * <p>
     * Este campo genérico contiene los datos específicos de la operación realizada
     * en el microservicio de pedidos. Puede contener un objeto individual, una lista
     * de objetos, o ser null en caso de errores. La anotación {@code @JsonProperty("datos")}
     * asegura que se serialice correctamente en JSON con el nombre "datos".
     * </p>
     * <p>
     * Tipos de datos comunes en el contexto de pedidos:
     * <ul>
     *   <li>{@code PedidoResponse} - Para operaciones con un pedido específico</li>
     *   <li>{@code List<PedidoResponse>} - Para listas de pedidos de un proveedor</li>
     *   <li>{@code ProveedorResponse} - Para operaciones con proveedores</li>
     *   <li>{@code List<ProveedorResponse>} - Para catálogos de proveedores</li>
     *   <li>{@code Void} - Para respuestas sin datos (errores o confirmaciones)</li>
     *   <li>{@code Map<String, Object>} - Para respuestas con metadatos adicionales</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos de uso con datos específicos:</strong>
     * </p>
     * <pre>{@code
     * // Respuesta con datos de pedido
     * ApiResponse<PedidoResponse> pedidoResponse = new ApiResponse<>(
     *     "0", "Pedido consultado exitosamente", pedidoData);
     *
     * // Respuesta con lista de proveedores
     * ApiResponse<List<ProveedorResponse>> proveedoresResponse = new ApiResponse<>(
     *     "0", "Proveedores obtenidos", listaProveedores);
     *
     * // Respuesta de error sin datos
     * ApiResponse<Void> errorResponse = new ApiResponse<>(
     *     "PED001", "Pedido no encontrado");
     * }</pre>
     * <p>
     * <strong>Consideraciones de serialización:</strong>
     * <ul>
     *   <li>Los tipos genéricos se preservan en la serialización JSON</li>
     *   <li>Los objetos complejos se serializan recursivamente</li>
     *   <li>Los valores null se representan correctamente como null en JSON</li>
     *   <li>Las fechas se formatean según la configuración de Jackson</li>
     * </ul>
     * </p>
     */
    @JsonProperty("datos")
    private T datos;

    /**
     * Constructor completo para respuestas con datos específicos de pedidos.
     * <p>
     * Crea una instancia completa de ApiResponse con código, mensaje y datos.
     * Este constructor se utiliza cuando la operación fue exitosa y se necesitan
     * enviar datos específicos del dominio de pedidos al cliente.
     * </p>
     * <p>
     * <strong>Casos de uso típicos:</strong>
     * <ul>
     *   <li>Respuestas exitosas con datos de pedidos</li>
     *   <li>Consultas que retornan información de proveedores</li>
     *   <li>Operaciones CRUD que devuelven el objeto modificado</li>
     *   <li>Reportes y listados con datos específicos</li>
     * </ul>
     * </p>
     *
     * @param codigo Código de respuesta específico del microservicio de pedidos
     * @param mensaje Mensaje descriptivo del resultado de la operación
     * @param datos Objeto de datos específico del dominio de pedidos (puede ser null)
     */
    public ApiResponse(String codigo, String mensaje, T datos) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    /**
     * Constructor para respuestas sin datos específicos de pedidos (típicamente errores).
     * <p>
     * Crea una instancia de ApiResponse con código y mensaje, pero sin datos.
     * Este constructor se utiliza principalmente para respuestas de error donde
     * no hay datos útiles del dominio de pedidos que enviar al cliente, o para
     * confirmaciones de operaciones que no requieren datos adicionales.
     * </p>
     * <p>
     * <strong>Casos de uso típicos:</strong>
     * <ul>
     *   <li>Errores de validación en datos de pedidos</li>
     *   <li>Proveedores no encontrados</li>
     *   <li>Pedidos inexistentes</li>
     *   <li>Confirmaciones de operaciones sin retorno de datos</li>
     *   <li>Errores internos del servidor</li>
     * </ul>
     * </p>
     *
     * @param codigo Código de respuesta que indica el tipo de resultado/error
     * @param mensaje Mensaje descriptivo del error o resultado obtenido
     */
    public ApiResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

}
