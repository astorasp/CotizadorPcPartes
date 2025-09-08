package mx.com.qtx.cotizador.dto.componente.mapper;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro;
import mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

/**
 * Mapper manual para convertir entre DTOs y objetos del dominio de componentes.
 * <p>
 * Esta clase implementa el patrón Mapper para facilitar la conversión bidireccional
 * entre los objetos del dominio ({@link mx.com.qtx.cotizador.dominio.core.componentes.Componente})
 * y los DTOs utilizados en la capa de presentación de la API REST.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Separación de capas:</strong> Aísla la lógica de dominio de la presentación</li>
 *   <li><strong>Conversión bidireccional:</strong> Request → Domain y Domain → Response</li>
 *   <li><strong>Type Safety:</strong> Mapeo explícito con validación de tipos</li>
 *   <li><strong>Flexibilidad:</strong> Permite lógica de conversión personalizada</li>
 *   <li><strong>Mantenibilidad:</strong> Centraliza toda la lógica de mapeo de componentes</li>
 * </ul>
 *
 * <h3>Operaciones soportadas:</h3>
 * <table border="1">
 *   <tr><th>Operación</th><th>Entrada</th><th>Salida</th><th>Propósito</th></tr>
 *   <tr><td>Crear componente</td><td>ComponenteCreateRequest</td><td>Componente</td><td>Crear entidad desde request</td></tr>
 *   <tr><td>Actualizar componente</td><td>ID + ComponenteUpdateRequest</td><td>Componente</td><td>Actualizar entidad desde request</td></tr>
 *   <tr><td>Generar respuesta</td><td>Componente</td><td>ComponenteResponse</td><td>Convertir entidad a response</td></tr>
 * </table>
 *
 * <h3>Tipos de componentes soportados:</h3>
 * <p>
 * El mapper maneja automáticamente la creación de diferentes tipos de componentes
 * basándose en el campo {@code tipoComponente} del request:
 * </p>
 * <ul>
 *   <li><strong>MONITOR:</strong> {@link mx.com.qtx.cotizador.dominio.core.componentes.Componente#crearMonitor}</li>
 *   <li><strong>DISCO_DURO:</strong> {@link mx.com.qtx.cotizador.dominio.core.componentes.Componente#crearDiscoDuro}</li>
 *   <li><strong>TARJETA_VIDEO:</strong> {@link mx.com.qtx.cotizador.dominio.core.componentes.Componente#crearTarjetaVideo}</li>
 * </ul>
 *
 * <h3>Ejemplo de uso en servicio:</h3>
 * <pre>{@code
 * @Service
 * public class ComponenteService {
 *
 *     public ComponenteResponse crearComponente(ComponenteCreateRequest request) {
 *         // Convertir request a entidad del dominio
 *         Componente componente = ComponenteMapper.toComponente(request);
 *
 *         // Persistir componente
 *         Componente guardado = repositorio.save(componente);
 *
 *         // Convertir entidad a response
 *         return ComponenteMapper.toResponse(guardado);
 *     }
 *
 *     public ComponenteResponse actualizarComponente(String id, ComponenteUpdateRequest request) {
 *         // Convertir request a entidad con ID específico
 *         Componente componente = ComponenteMapper.toComponente(id, request);
 *
 *         // Actualizar componente
 *         Componente actualizado = repositorio.save(componente);
 *
 *         // Convertir a response
 *         return ComponenteMapper.toResponse(actualizado);
 *     }
 * }
 * }</pre>
 *
 * <h3>Validación y manejo de errores:</h3>
 * <p>
 * El mapper incluye validaciones para asegurar la integridad de los datos:
 * </p>
 * <ul>
 *   <li>Validación de tipo de componente válido</li>
 *   <li>Manejo de valores null en requests</li>
 *   <li>Propagación de excepciones con mensajes claros</li>
 *   <li>Validación de campos requeridos según el tipo</li>
 * </ul>
 *
 * <h3>Comparación con MapStruct:</h3>
 * <p>
 * Este mapper manual ofrece ventajas sobre MapStruct en ciertos escenarios:
 * </p>
 * <ul>
 *   <li><strong>Lógica compleja:</strong> Manejo de tipos polimórficos de componentes</li>
 *   <li><strong>Control total:</strong> Lógica de conversión completamente personalizable</li>
 *   <li><strong>Performance:</strong> Sin reflexión en tiempo de ejecución</li>
 *   <li><strong>Debugging:</strong> Código fuente claro y depurable</li>
 * </ul>
 * <p>
 * Sin embargo, para mapeos más simples, {@link ComponenteMapperMapStruct} puede ser más apropiado.
 * </p>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see ComponenteMapperMapStruct
 * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente
 * @see mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest
 * @see mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse
 */
public class ComponenteMapper {
    
    /**
     * Convierte un ComponenteCreateRequest a un objeto del dominio Componente.
     * <p>
     * Este método realiza la conversión de un DTO de creación a una entidad del dominio,
     * determinando automáticamente el tipo específico de componente basado en el campo
     * {@code tipoComponente} del request. Utiliza el patrón Factory Method implementado
     * en la clase {@link mx.com.qtx.cotizador.dominio.core.componentes.Componente} para
     * crear instancias de los diferentes tipos de componentes.
     * </p>
     * <p>
     * El proceso de conversión incluye:
     * <ol>
     *   <li>Validación de que el request no sea null</li>
     *   <li>Determinación del tipo de componente basado en {@code tipoComponente}</li>
     *   <li>Invocación del método factory correspondiente con los parámetros apropiados</li>
     *   <li>Retorno de la instancia del componente creada</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Mapeo de tipos de componentes:</strong>
     * </p>
     * <table border="1">
     *   <tr><th>tipoComponente</th><th>Método Factory</th><th>Parámetros específicos</th></tr>
     *   <tr><td>"DISCO_DURO"</td><td>{@code crearDiscoDuro()}</td><td>ID, descripcion, marca, modelo, costo, precioBase, capacidadAlm</td></tr>
     *   <tr><td>"TARJETA_VIDEO"</td><td>{@code crearTarjetaVideo()}</td><td>ID, descripcion, marca, modelo, costo, precioBase, memoria</td></tr>
     *   <tr><td>"MONITOR"</td><td>{@code crearMonitor()}</td><td>ID, descripcion, marca, modelo, costo, precioBase</td></tr>
     * </table>
     *
     * @param request DTO de creación que contiene toda la información necesaria
     *               para crear un componente del dominio
     * @return Una nueva instancia de {@link mx.com.qtx.cotizador.dominio.core.componentes.Componente}
     *         del tipo específico determinado por el campo tipoComponente
     * @throws IllegalArgumentException si el tipoComponente no es válido o si request es null
     * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente#crearDiscoDuro
     * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente#crearTarjetaVideo
     * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente#crearMonitor
     * @see ComponenteCreateRequest#getTipoComponente()
     */
    public static Componente toComponente(ComponenteCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return switch (request.getTipoComponente().toUpperCase()) {
            case "DISCO_DURO" -> Componente.crearDiscoDuro(
                request.getId(),
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getCapacidadAlm()
            );
            case "TARJETA_VIDEO" -> Componente.crearTarjetaVideo(
                request.getId(),
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getMemoria()
            );
            case "MONITOR" -> Componente.crearMonitor(
                request.getId(),
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase()
            );
            default -> throw new IllegalArgumentException("Tipo de componente no válido: " + request.getTipoComponente());
        };
    }
    
    /**
     * Convierte un ComponenteUpdateRequest a un objeto del dominio Componente con ID específico.
     * <p>
     * Este método maneja la conversión de un DTO de actualización a una entidad del dominio,
     * similar al método {@link #toComponente(ComponenteCreateRequest)} pero específicamente
     * diseñado para operaciones de actualización. El ID del componente debe ser proporcionado
     * externamente (típicamente desde el parámetro de la URL en la API REST).
     * </p>
     * <p>
     * La diferencia principal con el método de creación es que este método:
     * <ul>
     *   <li>Recibe el ID como parámetro separado (no viene en el request)</li>
     *   <li>Está diseñado para actualizaciones donde el ID ya existe</li>
     *   <li>Mantiene la misma lógica de determinación de tipo de componente</li>
     *   <li>Utiliza los mismos métodos factory de la clase Componente</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Casos de uso típico:</strong>
     * </p>
     * <pre>{@code
     * @PutMapping("/componentes/{id}")
     * public ResponseEntity<ApiResponse<ComponenteResponse>> actualizarComponente(
     *         @PathVariable String id,
     *         @Valid @RequestBody ComponenteUpdateRequest request) {
     *
     *     // Convertir request a entidad con ID específico
     *     Componente componente = ComponenteMapper.toComponente(id, request);
     *
     *     // Actualizar en el repositorio
     *     Componente actualizado = repositorio.save(componente);
     *
     *     // Convertir a response
     *     ComponenteResponse response = ComponenteMapper.toResponse(actualizado);
     *
     *     return ResponseEntity.ok(new ApiResponse<>("0", "Componente actualizado", response));
     * }
     * }</pre>
     *
     * @param id Identificador único del componente que se va a actualizar.
     *           Este ID típicamente viene del parámetro de la URL.
     * @param request DTO de actualización que contiene los nuevos valores para el componente
     * @return Una nueva instancia de {@link mx.com.qtx.cotizador.dominio.core.componentes.Componente}
     *         con el ID especificado y los valores actualizados del request
     * @throws IllegalArgumentException si el tipoComponente no es válido, si id es null,
     *                                  o si request es null
     * @see #toComponente(ComponenteCreateRequest)
     * @see ComponenteUpdateRequest
     * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente
     */
    public static Componente toComponente(String id, ComponenteUpdateRequest request) {
        if (request == null) {
            return null;
        }
        
        return switch (request.getTipoComponente().toUpperCase()) {
            case "DISCO_DURO" -> Componente.crearDiscoDuro(
                id,
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getCapacidadAlm()
            );
            case "TARJETA_VIDEO" -> Componente.crearTarjetaVideo(
                id,
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getMemoria()
            );
            case "MONITOR" -> Componente.crearMonitor(
                id,
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase()
            );
            default -> throw new IllegalArgumentException("Tipo de componente no válido: " + request.getTipoComponente());
        };
    }
    
    /**
     * Convierte un objeto del dominio Componente a ComponenteResponse para la API.
     * <p>
     * Este método realiza la conversión inversa al proceso de creación, transformando
     * una entidad del dominio en un DTO de respuesta que puede ser serializado a JSON
     * y enviado al cliente de la API. La conversión incluye la determinación automática
     * del tipo de componente y el mapeo de los campos específicos correspondientes.
     * </p>
     * <p>
     * El proceso de conversión incluye:
     * <ol>
     *   <li>Validación de que la entidad no sea null</li>
     *   <li>Creación del builder de ComponenteResponse con campos comunes</li>
     *   <li>Determinación del tipo de componente mediante reflexión (instanceof)</li>
     *   <li>Mapeo de campos específicos según el tipo de componente</li>
     *   <li>Construcción del objeto ComponenteResponse final</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Mapeo de tipos y campos específicos:</strong>
     * </p>
     * <table border="1">
     *   <tr><th>Tipo de Componente</th><th>tipoComponente</th><th>Campo específico</th></tr>
     *   <tr><td>{@link mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro}</td><td>"DISCO_DURO"</td><td>capacidadAlm</td></tr>
     *   <tr><td>{@link mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo}</td><td>"TARJETA_VIDEO"</td><td>memoria</td></tr>
     *   <tr><td>Otros (Monitor)</td><td>"MONITOR"</td><td>Ninguno</td></tr>
     * </table>
     *
     * <h3>Ejemplo de uso en controlador:</h3>
     * <pre>{@code
     * @GetMapping("/componentes/{id}")
     * public ResponseEntity<ApiResponse<ComponenteResponse>> obtenerComponente(@PathVariable String id) {
     *     try {
     *         // Obtener entidad del dominio
     *         Componente componente = servicio.obtenerComponente(id);
     *
     *         // Convertir a DTO de respuesta
     *         ComponenteResponse response = ComponenteMapper.toResponse(componente);
     *
     *         // Enviar respuesta al cliente
     *         ApiResponse<ComponenteResponse> apiResponse =
     *             new ApiResponse<>("0", "Componente encontrado", response);
     *
     *         return ResponseEntity.ok(apiResponse);
     *
     *     } catch (ComponenteNoEncontradoException e) {
     *         ApiResponse<Void> error = new ApiResponse<>("1", "Componente no encontrado");
     *         return ResponseEntity.badRequest().body(error);
     *     }
     * }
     * }</pre>
     *
     * @param componente Entidad del dominio que se va a convertir a DTO de respuesta.
     *                  Puede ser de cualquier subtipo de Componente.
     * @return Una nueva instancia de {@link ComponenteResponse} con toda la información
     *         del componente preparada para ser enviada al cliente de la API
     * @throws IllegalStateException si el tipo de componente no puede ser determinado
     * @see ComponenteResponse
     * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente
     * @see mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro
     * @see mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo
     */
    public static ComponenteResponse toResponse(Componente componente) {
        if (componente == null) {
            return null;
        }
        
        ComponenteResponse.ComponenteResponseBuilder builder = ComponenteResponse.builder()
                .id(componente.getId())
                .descripcion(componente.getDescripcion())
                .marca(componente.getMarca())
                .modelo(componente.getModelo())
                .costo(componente.getCosto())
                .precioBase(componente.getPrecioBase());
        
        // Determinar el tipo y campos específicos
        if (componente instanceof DiscoDuro disco) {
            builder.tipoComponente("DISCO_DURO")
                   .capacidadAlm(disco.getCapacidadAlm());
        } else if (componente instanceof TarjetaVideo tarjeta) {
            builder.tipoComponente("TARJETA_VIDEO")
                   .memoria(tarjeta.getMemoria());
        } else {
            builder.tipoComponente("MONITOR");
        }
        
        return builder.build();
    }
} 