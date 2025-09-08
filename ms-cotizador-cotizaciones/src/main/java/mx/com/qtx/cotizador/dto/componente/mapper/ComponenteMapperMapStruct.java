package mx.com.qtx.cotizador.dto.componente.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro;
import mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

/**
 * Mapper usando MapStruct para convertir entre DTOs y objetos del dominio de componentes.
 * <p>
 * Esta interfaz implementa el patrón Mapper utilizando la librería MapStruct, que genera
 * automáticamente el código de mapeo en tiempo de compilación. MapStruct ofrece ventajas
 * significativas sobre los mappers manuales para conversiones simples y predecibles.
 * </p>
 *
 * <h3>Ventajas principales de MapStruct:</h3>
 * <ul>
 *   <li><strong>Generación automática:</strong> Código generado en tiempo de compilación, sin reflexión en runtime</li>
 *   <li><strong>Mejor performance:</strong> Código optimizado sin overhead de reflexión</li>
 *   <li><strong>Type safety:</strong> Detección de errores en tiempo de compilación</li>
 *   <li><strong>Menos boilerplate:</strong> Reducción significativa de código manual</li>
 *   <li><strong>Mantenibilidad:</strong> Cambios en los DTOs se reflejan automáticamente</li>
 *   <li><strong>Debugging:</strong> Código fuente generado facilita el debugging</li>
 * </ul>
 *
 * <h3>Configuración MapStruct:</h3>
 * <p>
 * La anotación {@code @Mapper(componentModel = "spring")} configura la integración con Spring:
 * </p>
 * <ul>
 *   <li><strong>componentModel = "spring":</strong> Genera un componente Spring (@Component)</li>
 *   <li><strong>Inyección de dependencias:</strong> Puede ser inyectado en servicios Spring</li>
 *   <li><strong>Gestión del ciclo de vida:</strong> Spring administra la instancia del mapper</li>
 * </ul>
 *
 * <h3>Estrategia de mapeo:</h3>
 * <p>
 * Esta implementación combina mapeo automático con lógica personalizada:
 * </p>
 * <ul>
 *   <li><strong>Mapeo automático:</strong> Campos con nombres idénticos se mapean automáticamente</li>
 *   <li><strong>Métodos custom:</strong> Lógica compleja mediante métodos anotados con {@code @Named}</li>
 *   <li><strong>Mapeo condicional:</strong> Campos específicos según el tipo de componente</li>
 *   <li><strong>Validación de tipos:</strong> Verificación de tipos en tiempo de compilación</li>
 * </ul>
 *
 * <h3>Métodos de mapeo implementados:</h3>
 * <table border="1">
 *   <tr><th>Método</th><th>Entrada</th><th>Salida</th><th>Tipo de mapeo</th></tr>
 *   <tr><td>{@code toComponente(ComponenteCreateRequest)}</td><td>CreateRequest</td><td>Componente</td><td>Custom + Automático</td></tr>
 *   <tr><td>{@code toComponente(String, ComponenteUpdateRequest)}</td><td>ID + UpdateRequest</td><td>Componente</td><td>Custom + Automático</td></tr>
 *   <tr><td>{@code toResponse(Componente)}</td><td>Componente</td><td>ComponenteResponse</td><td>Custom + Automático</td></tr>
 * </table>
 *
 * <h3>Ejemplo de uso con Spring:</h3>
 * <pre>{@code
 * @Service
 * public class ComponenteService {
 *
 *     private final ComponenteMapperMapStruct mapper;
 *
 *     // Inyección por constructor
 *     public ComponenteService(ComponenteMapperMapStruct mapper) {
 *         this.mapper = mapper;
 *     }
 *
 *     public ComponenteResponse crearComponente(ComponenteCreateRequest request) {
 *         // Convertir usando MapStruct
 *         Componente componente = mapper.toComponente(request);
 *
 *         // Persistir
 *         Componente guardado = repositorio.save(componente);
 *
 *         // Convertir respuesta
 *         return mapper.toResponse(guardado);
 *     }
 * }
 * }</pre>
 *
 * <h3>Métodos custom implementados:</h3>
 * <p>
 * Los métodos anotados con {@code @Named} proporcionan lógica personalizada:
 * </p>
 * <ul>
 *   <li><strong>createComponenteFromRequest:</strong> Factory method para crear componentes</li>
 *   <li><strong>createComponenteFromUpdateRequest:</strong> Factory method para updates</li>
 *   <li><strong>getTipoComponente:</strong> Determina el tipo de componente como String</li>
 *   <li><strong>getCapacidadAlm:</strong> Extrae capacidad de almacenamiento de DiscoDuro</li>
 *   <li><strong>getMemoria:</strong> Extrae memoria de TarjetaVideo</li>
 * </ul>
 *
 * <h3>Comparación con ComponenteMapper manual:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>MapStruct</th><th>Manual</th></tr>
 *   <tr><td>Performance</td><td>⭐⭐⭐⭐⭐</td><td>⭐⭐⭐⭐</td></tr>
 *   <tr><td>Mantenibilidad</td><td>⭐⭐⭐⭐⭐</td><td>⭐⭐⭐</td></tr>
 *   <tr><td>Lógica compleja</td><td>⭐⭐⭐</td><td>⭐⭐⭐⭐⭐</td></tr>
 *   <tr><td>Curva de aprendizaje</td><td>⭐⭐⭐</td><td>⭐⭐⭐⭐⭐</td></tr>
 *   <tr><td>Debugging</td><td>⭐⭐⭐⭐</td><td>⭐⭐⭐⭐⭐</td></tr>
 *   <tr><td>Cantidad de código</td><td>⭐⭐⭐⭐⭐</td><td>⭐⭐⭐</td></tr>
 * </table>
 *
 * <h3>Consideraciones importantes:</h3>
 * <ul>
 *   <li><strong>Compilación:</strong> Requiere procesamiento de anotaciones para generar código</li>
 *   <li><strong>Configuración:</strong> Necesita configuración específica en el build tool (Maven/Gradle)</li>
 *   <li><strong>Limitaciones:</strong> Lógica muy compleja puede requerir métodos custom</li>
 *   <li><strong>Integración Spring:</strong> Compatible con inyección de dependencias de Spring</li>
 *   <li><strong>Testing:</strong> El código generado puede ser probado unitariamente</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see org.mapstruct.Mapper
 * @see org.mapstruct.Mapping
 * @see org.mapstruct.Named
 * @see ComponenteMapper
 * @see ComponenteCreateRequest
 * @see ComponenteResponse
 */
@Mapper(componentModel = "spring")  // Para integración con Spring
public interface ComponenteMapperMapStruct {
    
    /**
     * Instancia singleton del mapper para uso sin inyección de dependencias.
     * <p>
     * Esta constante proporciona una instancia del mapper que puede ser utilizada
     * directamente sin necesidad de inyección de dependencias de Spring. Es útil
     * en contextos donde no se dispone de un contenedor IoC o para testing.
     * </p>
     * <p>
     * <strong>Nota:</strong> En aplicaciones Spring, se recomienda usar la inyección
     * de dependencias en lugar de esta instancia singleton para mejor testabilidad
     * y gestión del ciclo de vida.
     * </p>
     *
     * @see Mappers#getMapper(Class)
     */
    ComponenteMapperMapStruct INSTANCE = Mappers.getMapper(ComponenteMapperMapStruct.class);
    
    /**
     * Mapeo de ComponenteCreateRequest a Componente usando método custom.
     * <p>
     * Este método mapea un DTO de creación a una entidad del dominio Componente.
     * Utiliza un método custom ({@code createComponenteFromRequest}) para manejar
     * la lógica compleja de creación de diferentes tipos de componentes basada
     * en el campo {@code tipoComponente}.
     * </p>
     * <p>
     * La anotación {@code @Mapping} configura:
     * <ul>
     *   <li><strong>target = ".":</strong> Mapea al objeto resultante completo</li>
     *   <li><strong>source = ".":</strong> Usa el objeto de entrada completo</li>
     *   <li><strong>qualifiedByName:</strong> Especifica el método custom a utilizar</li>
     * </ul>
     * </p>
     *
     * @param request DTO de creación que contiene toda la información necesaria
     * @return Nueva instancia de Componente del tipo apropiado
     * @see #createComponenteFromRequest(ComponenteCreateRequest)
     */
    @Mapping(target = ".", source = ".", qualifiedByName = "createComponenteFromRequest")
    Componente toComponente(ComponenteCreateRequest request);
    
    /**
     * Mapeo de ComponenteUpdateRequest a Componente con ID externo.
     * <p>
     * Este método maneja la conversión de un DTO de actualización a una entidad del dominio,
     * recibiendo el ID como parámetro separado (típicamente del path de la URL).
     * Utiliza un método custom para la lógica de creación basada en el tipo de componente.
     * </p>
     * <p>
     * Las anotaciones {@code @Mapping} configuran:
     * <ul>
     *   <li><strong>target = "id", source = "id":</strong> Mapea el ID del parámetro al campo id</li>
     *   <li><strong>target = ".", source = "request":</strong> Usa el request para el resto del mapeo</li>
     *   <li><strong>qualifiedByName:</strong> Método custom para la creación</li>
     * </ul>
     * </p>
     *
     * @param id Identificador único del componente
     * @param request DTO de actualización con los nuevos valores
     * @return Nueva instancia de Componente con el ID especificado
     * @see #createComponenteFromUpdateRequest(ComponenteUpdateRequest, String)
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = ".", source = "request", qualifiedByName = "createComponenteFromUpdateRequest")
    Componente toComponente(String id, ComponenteUpdateRequest request);
    
    /**
     * Mapeo de Componente a ComponenteResponse con campos específicos.
     * <p>
     * Este método convierte una entidad del dominio Componente a un DTO de respuesta
     * para la API. Combina mapeo automático de campos comunes con métodos custom
     * para manejar campos específicos según el tipo de componente.
     * </p>
     * <p>
     * Las anotaciones {@code @Mapping} configuran métodos custom para:
     * <ul>
     *   <li><strong>getTipoComponente:</strong> Determina el tipo como String</li>
     *   <li><strong>getCapacidadAlm:</strong> Extrae capacidad de DiscoDuro</li>
     *   <li><strong>getMemoria:</strong> Extrae memoria de TarjetaVideo</li>
     * </ul>
     * </p>
     * <p>
     * Los campos comunes (id, descripcion, marca, modelo, costo, precioBase)
     * se mapean automáticamente por coincidencia de nombres.
     * </p>
     *
     * @param componente Entidad del dominio a convertir
     * @return DTO de respuesta con toda la información del componente
     * @see #getTipoComponente(Componente)
     * @see #getCapacidadAlm(Componente)
     * @see #getMemoria(Componente)
     */
    @Mapping(target = "tipoComponente", source = ".", qualifiedByName = "getTipoComponente")
    @Mapping(target = "capacidadAlm", source = ".", qualifiedByName = "getCapacidadAlm")
    @Mapping(target = "memoria", source = ".", qualifiedByName = "getMemoria")
    ComponenteResponse toResponse(Componente componente);
    
    // ========================
    // MÉTODOS CUSTOM (Named)
    // ========================
    
    /**
     * Método custom para crear Componente desde CreateRequest
     */
    @Named("createComponenteFromRequest")
    default Componente createComponenteFromRequest(ComponenteCreateRequest request) {
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
     * Método custom para crear Componente desde UpdateRequest
     */
    @Named("createComponenteFromUpdateRequest")
    default Componente createComponenteFromUpdateRequest(ComponenteUpdateRequest request, String id) {
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
     * Método custom para obtener el tipo de componente
     */
    @Named("getTipoComponente")
    default String getTipoComponente(Componente componente) {
        if (componente instanceof DiscoDuro) {
            return "DISCO_DURO";
        } else if (componente instanceof TarjetaVideo) {
            return "TARJETA_VIDEO";
        } else {
            return "MONITOR";
        }
    }
    
    /**
     * Método custom para obtener capacidad de almacenamiento
     */
    @Named("getCapacidadAlm")
    default String getCapacidadAlm(Componente componente) {
        return componente instanceof DiscoDuro disco ? disco.getCapacidadAlm() : null;
    }
    
    /**
     * Método custom para obtener memoria
     */
    @Named("getMemoria")
    default String getMemoria(Componente componente) {
        return componente instanceof TarjetaVideo tarjeta ? tarjeta.getMemoria() : null;
    }
} 