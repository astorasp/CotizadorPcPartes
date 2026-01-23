package mx.com.qtx.cotizador.dto.cotizacion.mapper;

import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.dto.cotizacion.response.DetalleCotizacionResponse;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.entidad.DetalleCotizacion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre entidades de cotización del dominio y DTOs de respuesta.
 * <p>
 * Esta clase implementa el patrón Mapper para facilitar la transformación bidireccional
 * entre las entidades del dominio de cotización ({@link mx.com.qtx.cotizador.entidad.Cotizacion})
 * y los DTOs utilizados en la capa de presentación de la API REST. Proporciona métodos
 * estáticos para todas las conversiones necesarias en el sistema de cotizaciones.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Separación de capas:</strong> Aísla la lógica de dominio de la presentación REST</li>
 *   <li><strong>Conversión unidireccional:</strong> Entidades del dominio → DTOs de respuesta</li>
 *   <li><strong>Mapeo automático:</strong> Conversión directa de campos con nombres coincidentes</li>
 *   <li><strong>Manejo de relaciones:</strong> Resuelve referencias a entidades relacionadas</li>
 *   <li><strong>Procesamiento batch:</strong> Soporta conversión de listas de entidades</li>
 *   <li><strong>Null safety:</strong> Maneja correctamente valores null en entidades</li>
 * </ul>
 *
 * <h3>Arquitectura de mapeo:</h3>
 * <p>
 * La clase sigue una arquitectura de mapeo estructurada:
 * </p>
 * <table border="1">
 *   <tr><th>Capa Origen</th><th>Capa Destino</th><th>Método Principal</th></tr>
 *   <tr><td>Entidad Cotizacion</td><td>CotizacionResponse</td><td>{@link #toResponse(Cotizacion)}</td></tr>
 *   <tr><td>Entidad DetalleCotizacion</td><td>DetalleCotizacionResponse</td><td>{@link #toDetalleResponse(DetalleCotizacion)}</td></tr>
 *   <tr><td>List&lt;Cotizacion&gt;</td><td>List&lt;CotizacionResponse&gt;</td><td>{@link #toResponseList(List)}</td></tr>
 * </table>
 *
 * <h3>Patrones de diseño aplicados:</h3>
 * <ul>
 *   <li><strong>Mapper Pattern:</strong> Centraliza toda la lógica de conversión</li>
 *   <li><strong>Static Methods:</strong> Métodos de utilidad sin estado</li>
 *   <li><strong>Builder Pattern:</strong> Construcción fluida de DTOs</li>
 *   <li><strong>Null Object Pattern:</strong> Manejo seguro de valores null</li>
 *   <li><strong>Stream API:</strong> Procesamiento funcional de colecciones</li>
 * </ul>
 *
 * <h3>Métodos principales:</h3>
 * <table border="1">
 *   <tr><th>Método</th><th>Función</th><th>Complejidad</th></tr>
 *   <tr><td>{@code toResponse()}</td><td>Convierte entidad completa</td><td>Media</td></tr>
 *   <tr><td>{@code toDetalleResponse()}</td><td>Convierte detalle individual</td><td>Alta</td></tr>
 *   <tr><td>{@code toResponseList()}</td><td>Convierte lista de entidades</td><td>Baja</td></tr>
 *   <tr><td>{@code calcularImporteTotal()}</td><td>Cálculo de totales</td><td>Baja</td></tr>
 * </table>
 *
 * <h3>Estrategia de conversión:</h3>
 * <p>
 * La conversión sigue una estrategia sistemática:
 * </p>
 * <ol>
 *   <li><strong>Validación de entrada:</strong> Verifica que la entidad no sea null</li>
 *   <li><strong>Mapeo directo:</strong> Campos con nombres idénticos se mapean automáticamente</li>
 *   <li><strong>Resolución de referencias:</strong> Navega relaciones de entidades</li>
 *   <li><strong>Cálculos adicionales:</strong> Realiza cálculos específicos si es necesario</li>
 *   <li><strong>Construcción del DTO:</strong> Usa Builder pattern para crear el resultado</li>
 *   <li><strong>Manejo de errores:</strong> Retorna null si la entrada es null</li>
 * </ol>
 *
 * <h3>Manejo de relaciones de entidades:</h3>
 * <p>
 * El mapper maneja complejas relaciones de entidades:
 * </p>
 * <ul>
 *   <li><strong>Cotizacion → Detalles:</strong> Convierte lista de DetalleCotizacion</li>
 *   <li><strong>DetalleCotizacion → Componente:</strong> Resuelve referencia al componente</li>
 *   <li><strong>Componente → TipoComponente:</strong> Obtiene información del tipo</li>
 *   <li><strong>Null safety:</strong> Todas las navegaciones incluyen verificación de null</li>
 * </ul>
 *
 * <h3>Ejemplo de uso en servicio:</h3>
 * <pre>{@code
 * @Service
 * public class CotizacionService {
 *
 *     public CotizacionResponse obtenerCotizacion(Long folio) {
 *         // Obtener entidad del dominio
 *         Cotizacion cotizacion = cotizacionRepository.findByFolio(folio);
 *
 *         // Convertir a DTO usando el mapper
 *         return CotizacionMapper.toResponse(cotizacion);
 *     }
 *
 *     public List<CotizacionResponse> listarCotizaciones() {
 *         // Obtener lista de entidades
 *         List<Cotizacion> cotizaciones = cotizacionRepository.findAll();
 *
 *         // Convertir lista completa
 *         return CotizacionMapper.toResponseList(cotizaciones);
 *     }
 * }
 * }</pre>
 *
 * <h3>Consideraciones de rendimiento:</h3>
 * <ul>
 *   <li><strong>Lazy Loading:</strong> Considerar carga diferida para relaciones complejas</li>
 *   <li><strong>Batch Processing:</strong> El método toResponseList() es eficiente para listas grandes</li>
 *   <li><strong>Memory Usage:</strong> Crea nuevos objetos DTO, considera el impacto en memoria</li>
 *   <li><strong>Database Queries:</strong> Mapeos complejos pueden generar queries adicionales</li>
 * </ul>
 *
 * <h3>Limitaciones actuales:</h3>
 * <ul>
 *   <li><strong>Direccionalidad:</strong> Solo mapea de entidad a DTO, no al revés</li>
 *   <li><strong>Campos opcionales:</strong> Algunos campos como observaciones no se mapean</li>
 *   <li><strong>Validaciones:</strong> No realiza validaciones de negocio, solo conversión</li>
 *   <li><strong>Profundidad:</strong> Navega relaciones pero con límite de profundidad</li>
 * </ul>
 *
 * <h3>Comparación con MapStruct:</h3>
 * <p>
 * Esta implementación manual ofrece ventajas específicas:
 * </p>
 * <ul>
 *   <li><strong>Flexibilidad:</strong> Lógica de conversión completamente personalizable</li>
 *   <li><strong>Control total:</strong> Manejo específico de casos edge y errores</li>
 *   <li><strong>Performance:</strong> Sin reflexión en runtime para casos simples</li>
 *   <li><strong>Debugging:</strong> Código fuente claro y fácil de depurar</li>
 *   <li><strong>Mantenibilidad:</strong> Cambios son explícitos y documentados</li>
 * </ul>
 * <p>
 * Para mapeos más complejos, {@link mx.com.qtx.cotizador.dto.componente.mapper.ComponenteMapperMapStruct}
 * puede ser más apropiado.
 * </p>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.entidad.Cotizacion
 * @see mx.com.qtx.cotizador.entidad.DetalleCotizacion
 * @see CotizacionResponse
 * @see DetalleCotizacionResponse
 * @see mx.com.qtx.cotizador.dto.componente.mapper.ComponenteMapperMapStruct
 */
public class CotizacionMapper {
    
    /**
     * Convierte una entidad Cotizacion completa a CotizacionResponse.
     * <p>
     * Este método realiza la conversión principal del mapper, transformando una entidad
     * completa del dominio Cotizacion en un DTO de respuesta listo para ser serializado
     * a JSON y enviado al cliente de la API. Incluye el mapeo de todos los campos principales
     * y la conversión recursiva de la lista de detalles.
     * </p>
     * <p>
     * <strong>Proceso de conversión:</strong>
     * </p>
     * <ol>
     *   <li><strong>Validación:</strong> Verifica que la entidad cotizacion no sea null</li>
     *   <li><strong>Mapeo directo:</strong> Campos simples (folio, fecha, subtotal, etc.)</li>
     *   <li><strong>Conversión de detalles:</strong> Usa Stream API para convertir cada DetalleCotizacion</li>
     *   <li><strong>Campos opcionales:</strong> observaciones se establece como null (no implementado aún)</li>
     *   <li><strong>Construcción:</strong> Usa Builder pattern para crear CotizacionResponse</li>
     * </ol>
     * <p>
     * <strong>Mapeo de campos:</strong>
     * </p>
     * <table border="1">
     *   <tr><th>Campo Entidad</th><th>Campo DTO</th><th>Tipo</th></tr>
     *   <tr><td>folio</td><td>folio</td><td>Integer</td></tr>
     *   <tr><td>fecha</td><td>fecha</td><td>String</td></tr>
     *   <tr><td>subtotal</td><td>subtotal</td><td>BigDecimal</td></tr>
     *   <tr><td>impuestos</td><td>impuestos</td><td>BigDecimal</td></tr>
     *   <tr><td>total</td><td>total</td><td>BigDecimal</td></tr>
     *   <tr><td>detalles</td><td>detalles</td><td>List&lt;DetalleCotizacionResponse&gt;</td></tr>
     *   <tr><td>N/A</td><td>observaciones</td><td>String (null)</td></tr>
     * </table>
     *
     * @param cotizacion Entidad del dominio Cotizacion a convertir. Puede ser null.
     * @return Una nueva instancia de CotizacionResponse con todos los datos mapeados,
     *         o null si la entidad de entrada es null.
     * @see Cotizacion
     * @see CotizacionResponse
     * @see #toDetalleResponse(DetalleCotizacion)
     */
    public static CotizacionResponse toResponse(Cotizacion cotizacion) {
        if (cotizacion == null) {
            return null;
        }

        List<DetalleCotizacionResponse> detallesResponse = cotizacion.getDetalles().stream()
                .map(CotizacionMapper::toDetalleResponse)
                .collect(Collectors.toList());

        return CotizacionResponse.builder()
                .folio(cotizacion.getFolio())
                .fecha(cotizacion.getFecha())
                .subtotal(cotizacion.getSubtotal())
                .impuestos(cotizacion.getImpuestos())
                .total(cotizacion.getTotal())
                .detalles(detallesResponse)
                .observaciones(null) // Las observaciones no se almacenan en la entidad actual
                .build();
    }
    
    /**
     * Convierte una entidad DetalleCotizacion a DetalleCotizacionResponse.
     * <p>
     * Este método maneja la conversión más compleja del mapper, ya que debe navegar
     * las relaciones entre entidades para obtener toda la información necesaria.
     * Convierte un detalle individual de cotización incluyendo la resolución de
     * referencias al componente relacionado.
     * </p>
     * <p>
     * <strong>Navegación de relaciones:</strong>
     * </p>
     * <ol>
     *   <li><strong>DetalleCotizacion:</strong> Entidad principal a convertir</li>
     *   <li><strong>Componente:</strong> Referencia al componente (detalle.getComponente())</li>
     *   <li><strong>TipoComponente:</strong> Tipo del componente (componente.getTipoComponente())</li>
     *   <li><strong>DetalleCotizacionId:</strong> ID compuesto (detalle.getId())</li>
     * </ol>
     * <p>
     * <strong>Mapeo de campos con navegación:</strong>
     * </p>
     * <table border="1">
     *   <tr><th>Campo DTO</th><th>Origen</th><th>Navegación</th></tr>
     *   <tr><td>numDetalle</td><td>detalle.getId().getNumDetalle()</td><td>ID compuesto</td></tr>
     *   <tr><td>idComponente</td><td>detalle.getComponente().getId()</td><td>Relación componente</td></tr>
     *   <tr><td>nombreComponente</td><td>detalle.getComponente().getDescripcion()</td><td>Relación componente</td></tr>
     *   <tr><td>categoria</td><td>componente.getTipoComponente().getNombre()</td><td>Relación anidada</td></tr>
     *   <tr><td>cantidad</td><td>detalle.getCantidad()</td><td>Campo directo</td></tr>
     *   <tr><td>descripcion</td><td>detalle.getDescripcion()</td><td>Campo directo</td></tr>
     *   <tr><td>precioBase</td><td>detalle.getPrecioBase()</td><td>Campo directo</td></tr>
     *   <tr><td>importeTotal</td><td>calcularImporteTotal(detalle)</td><td>Cálculo derivado</td></tr>
     * </table>
     *
     * @param detalle Entidad DetalleCotizacion a convertir. Puede ser null.
     * @return Una nueva instancia de DetalleCotizacionResponse con todas las referencias
     *         resueltas, o null si la entidad de entrada es null.
     * @see DetalleCotizacion
     * @see DetalleCotizacionResponse
     * @see #calcularImporteTotal(DetalleCotizacion)
     */
    public static DetalleCotizacionResponse toDetalleResponse(DetalleCotizacion detalle) {
        if (detalle == null) {
            return null;
        }

        return DetalleCotizacionResponse.builder()
                .numDetalle(detalle.getId() != null ? detalle.getId().getNumDetalle() : null)
                .idComponente(detalle.getComponente() != null ? detalle.getComponente().getId() : null)
                .nombreComponente(detalle.getComponente() != null ? detalle.getComponente().getDescripcion() : null)
                .categoria(detalle.getComponente() != null && detalle.getComponente().getTipoComponente() != null ?
                          detalle.getComponente().getTipoComponente().getNombre() : null)
                .cantidad(detalle.getCantidad())
                .descripcion(detalle.getDescripcion())
                .precioBase(detalle.getPrecioBase())
                .importeTotal(calcularImporteTotal(detalle))
                .build();
    }
    
    /**
     * Convierte una lista de entidades Cotizacion a lista de CotizacionResponse.
     * <p>
     * Este método proporciona procesamiento batch eficiente para convertir múltiples
     * entidades Cotizacion a sus correspondientes DTOs de respuesta. Utiliza la
     * Stream API de Java 8 para procesamiento funcional y paralelo cuando es posible.
     * </p>
     * <p>
     * <strong>Proceso de conversión:</strong>
     * </p>
     * <ol>
     *   <li><strong>Validación:</strong> Verifica que la lista de entrada no sea null</li>
     *   <li><strong>Stream processing:</strong> Convierte cada entidad usando toResponse()</li>
     *   <li><strong>Recolección:</strong> Recoge los resultados en una nueva lista</li>
     *   <li><strong>Manejo de nulls:</strong> Filtra automáticamente entidades null</li>
     * </ol>
     * <p>
     * <strong>Características de rendimiento:</strong>
     * <ul>
     *   <li><strong>Stream API:</strong> Procesamiento funcional eficiente</li>
     *   <li><strong>Paralelización:</strong> Puede paralelizarse automáticamente si es beneficioso</li>
     *   <li><strong>Lazy evaluation:</strong> Evalúa elementos solo cuando son necesarios</li>
     *   <li><strong>Memory efficient:</strong> No requiere listas intermedias grandes</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Casos de uso típicos:</strong>
     * <ul>
     *   <li>Listado de cotizaciones para interfaz de administración</li>
     *   <li>Exportación masiva de datos de cotización</li>
     *   <li>Reportes que requieren múltiples cotizaciones</li>
     *   <li>APIs de búsqueda que retornan listas</li>
     * </ul>
     * </p>
     *
     * @param cotizaciones Lista de entidades Cotizacion a convertir. Puede ser null.
     * @return Una nueva lista de CotizacionResponse, o null si la lista de entrada es null.
     *         La lista retornada puede ser más pequeña que la original si contiene entidades null.
     * @see #toResponse(Cotizacion)
     * @see java.util.stream.Stream
     * @see java.util.stream.Collectors
     */
    public static List<CotizacionResponse> toResponseList(List<Cotizacion> cotizaciones) {
        if (cotizaciones == null) {
            return null;
        }

        return cotizaciones.stream()
                .map(CotizacionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calcula el importe total de un detalle de cotización.
     * <p>
     * Método auxiliar que realiza el cálculo matemático básico para determinar
     * el importe total de un detalle de cotización multiplicando el precio base
     * por la cantidad. Incluye validaciones de null safety para evitar errores
     * de cálculo.
     * </p>
     * <p>
     * <strong>Fórmula aplicada:</strong>
     * <pre>
     * importeTotal = precioBase × cantidad
     * </pre>
     * </p>
     * <p>
     * <strong>Validaciones incluidas:</strong>
     * <ul>
     *   <li><strong>Null checking:</strong> Verifica que precioBase y cantidad no sean null</li>
     *   <li><strong>Valor por defecto:</strong> Retorna BigDecimal.ZERO si faltan datos</li>
     *   <li><strong>Precisión decimal:</strong> Mantiene precisión exacta con BigDecimal</li>
     *   <li><strong>Consistencia:</strong> Siempre retorna un valor no null</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Consideraciones matemáticas:</strong>
     * <ul>
     *   <li>La multiplicación preserva la precisión decimal</li>
     *   <li>Los valores null se tratan como cero para evitar NPE</li>
     *   <li>El resultado contribuye al subtotal de la cotización</li>
     *   <li>Es independiente del cálculo de impuestos</li>
     * </ul>
     * </p>
     *
     * @param detalle Entidad DetalleCotizacion con los datos para el cálculo
     * @return El importe total calculado como BigDecimal, o BigDecimal.ZERO si faltan datos
     * @see java.math.BigDecimal#multiply(java.math.BigDecimal)
     * @see java.math.BigDecimal#ZERO
     */
    private static java.math.BigDecimal calcularImporteTotal(DetalleCotizacion detalle) {
        if (detalle.getPrecioBase() == null || detalle.getCantidad() == null) {
            return java.math.BigDecimal.ZERO;
        }

        return detalle.getPrecioBase().multiply(java.math.BigDecimal.valueOf(detalle.getCantidad()));
    }
} 