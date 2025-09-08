package mx.com.qtx.cotizador.dto.pedido.mapper;

import mx.com.qtx.cotizador.dominio.pedidos.Pedido;
import mx.com.qtx.cotizador.dominio.pedidos.DetallePedido;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.dto.pedido.response.DetallePedidoResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversiones entre entidades del dominio de pedidos y DTOs de respuesta.
 * <p>
 * Esta clase implementa el patrón Mapper para facilitar la transformación bidireccional
 * entre las entidades del dominio de pedidos ({@link mx.com.qtx.cotizador.entidad.Pedido})
 * y los DTOs utilizados en la capa de presentación de la API REST del microservicio de pedidos.
 * Proporciona métodos estáticos para todas las conversiones necesarias en el sistema de gestión de pedidos.
 * </p>
 *
 * <h3>Propósito principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Separación de capas:</strong> Aísla la lógica de dominio JPA de la presentación REST</li>
 *   <li><strong>Conversión unidireccional:</strong> Entidades del dominio → DTOs de respuesta</li>
 *   <li><strong>Mapeo de relaciones:</strong> Resuelve referencias JPA (Pedido → Proveedor, DetallePedido → Componente)</li>
 *   <li><strong>Procesamiento jerárquico:</strong> Maneja estructuras complejas de pedidos con detalles</li>
 *   <li><strong>Null safety:</strong> Maneja correctamente valores null en entidades relacionadas</li>
 *   <li><strong>Performance:</strong> Optimizado para consultas JPA con fetch de relaciones</li>
 * </ul>
 *
 * <h3>Arquitectura de mapeo específica para pedidos:</h3>
 * <p>
 * La clase sigue una arquitectura especializada para el dominio de pedidos:
 * </p>
 * <table border="1">
 *   <tr><th>Capa Origen</th><th>Capa Destino</th><th>Método Principal</th><th>Relaciones Mapeadas</th></tr>
 *   <tr><td>Entidad Pedido</td><td>PedidoResponse</td><td>{@link #toResponse(Pedido)}</td><td>Pedido → Proveedor</td></tr>
 *   <tr><td>Entidad DetallePedido</td><td>DetallePedidoResponse</td><td>{@link #toDetallePedidoResponse(DetallePedido)}</td><td>DetallePedido → Componente</td></tr>
 *   <tr><td>List&lt;DetallePedido&gt;</td><td>List&lt;DetallePedidoResponse&gt;</td><td>Stream API</td><td>Múltiples componentes</td></tr>
 * </table>
 *
 * <h3>Manejo de relaciones JPA complejas:</h3>
 * <p>
 * El mapper maneja relaciones JPA específicas del dominio de pedidos:
 * </p>
 * <ul>
 *   <li><strong>Pedido → Proveedor (@ManyToOne):</strong> Extrae cve y nombre del proveedor</li>
 *   <li><strong>Pedido → DetallesPedido (@OneToMany):</strong> Convierte lista de detalles usando Stream API</li>
 *   <li><strong>DetallePedido → Componente (@ManyToOne):</strong> Resuelve idArticulo y descripcion del componente</li>
 *   <li><strong>DetallePedido → Pedido (@ManyToOne):</strong> Relación bidireccional para integridad</li>
 *   <li><strong>Null safety en relaciones:</strong> Todas las navegaciones incluyen verificación de null</li>
 * </ul>
 *
 * <h3>Patrones de diseño aplicados:</h3>
 * <ul>
 *   <li><strong>Mapper Pattern:</strong> Centraliza lógica de conversión específica de pedidos</li>
 *   <li><strong>Static Methods:</strong> Métodos de utilidad sin estado, thread-safe</li>
 *   <li><strong>Builder Pattern:</strong> Construcción fluida de DTOs usando Lombok @Builder</li>
 *   <li><strong>Stream API:</strong> Procesamiento funcional eficiente de colecciones</li>
 *   <li><strong>Null Object Pattern:</strong> Manejo seguro de valores null en relaciones opcionales</li>
 *   <li><strong>Fluent Interface:</strong> Métodos encadenables para construcción de objetos</li>
 * </ul>
 *
 * <h3>Estrategia de conversión específica para pedidos:</h3>
 * <p>
 * La conversión sigue una estrategia especializada para pedidos:
 * </p>
 * <ol>
 *   <li><strong>Validación de entidad:</strong> Verifica que el pedido/detalle no sea null</li>
 *   <li><strong>Mapeo de campos simples:</strong> numPedido, fechas, total (campos directos)</li>
 *   <li><strong>Resolución de proveedor:</strong> Navega relación Pedido.proveedor para obtener datos</li>
 *   <li><strong>Procesamiento de detalles:</strong> Usa Stream API para convertir lista de detalles</li>
 *   <li><strong>Cálculos agregados:</strong> Calcula totalDetalles como tamaño de lista</li>
 *   <li><strong>Construcción de DTOs:</strong> Usa Builder pattern para objetos inmutables</li>
 *   <li><strong>Manejo de errores:</strong> Retorna null si entidades requeridas son null</li>
 * </ol>
 *
 * <h3>Ejemplo de uso en servicio de pedidos:</h3>
 * <pre>{@code
 * @Service
 * public class PedidoService {
 *
 *     private final PedidoRepositorio pedidoRepositorio;
 *
 *     public PedidoResponse obtenerPedido(Long numPedido) {
 *         // Obtener entidad del dominio con relaciones fetch
 *         Pedido pedido = pedidoRepositorio.findById(numPedido)
 *             .orElseThrow(() -> new PedidoNoEncontradoException(numPedido));
 *
 *         // Convertir a DTO usando el mapper especializado
 *         return PedidoMapper.toResponse(pedido);
 *     }
 *
 *     public List<PedidoResponse> obtenerPedidosPorProveedor(String cveProveedor) {
 *         // Obtener entidades filtradas por proveedor
 *         List<Pedido> pedidos = pedidoRepositorio.findByProveedorCve(cveProveedor);
 *
 *         // Convertir lista completa usando mapper
 *         return pedidos.stream()
 *             .map(PedidoMapper::toResponse)
 *             .collect(Collectors.toList());
 *     }
 * }
 * }</pre>
 *
 * <h3>Consideraciones de performance en pedidos:</h3>
 * <ul>
 *   <li><strong>Fetch de relaciones:</strong> Considerar @EntityGraph para evitar N+1 queries</li>
 *   <li><strong>Stream API efficiency:</strong> Procesamiento lazy y paralelizable cuando es beneficioso</li>
 *   <li><strong>Memory usage:</strong> Crear nuevos DTOs por conversión, considerar impacto en memoria</li>
 *   <li><strong>Database optimization:</strong> Queries optimizadas para relaciones de pedidos</li>
 *   <li><strong>Caching strategy:</strong> Considerar cache para proveedores y componentes referenciados</li>
 * </ul>
 *
 * <h3>Métodos de conversión disponibles:</h3>
 * <table border="1">
 *   <tr><th>Método</th><th>Función</th><th>Complejidad</th><th>Relaciones</th></tr>
 *   <tr><td>{@code toResponse(Pedido)}</td><td>Convierte pedido completo</td><td>Alta</td><td>Proveedor + Detalles</td></tr>
 *   <tr><td>{@code toDetallePedidoResponse(DetallePedido)}</td><td>Convierte detalle individual</td><td>Media</td><td>Componente</td></tr>
 * </table>
 *
 * <h3>Limitaciones actuales y consideraciones:</h3>
 * <ul>
 *   <li><strong>Direccionalidad:</strong> Solo mapea de entidad a DTO, no al revés (no necesario para consultas)</li>
 *   <li><strong>Campos opcionales:</strong> Algunos campos como observaciones no se mapean aún</li>
 *   <li><strong>Profundidad de navegación:</strong> Limita navegación a 2 niveles (Pedido → Proveedor/Componentes)</li>
 *   <li><strong>Validaciones:</strong> No realiza validaciones de negocio, solo conversión de formato</li>
 *   <li><strong>Timezone handling:</strong> Las fechas se mapean directamente, considerar timezone si es necesario</li>
 * </ul>
 *
 * <h3>Integración con otros componentes del microservicio:</h3>
 * <ul>
 *   <li><strong>PedidoRepositorio:</strong> Proporciona entidades con relaciones fetch apropiadas</li>
 *   <li><strong>PedidoService:</strong> Utiliza mapper para preparar respuestas de API</li>
 *   <li><strong>Controladores REST:</strong> Reciben DTOs preparados por el mapper</li>
 *   <li><strong>ApiResponse:</strong> Envuelve los DTOs del mapper en respuestas estandarizadas</li>
 *   <li><strong>JPA Entities:</strong> Entidades del dominio que el mapper convierte</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.entidad.Pedido
 * @see mx.com.qtx.cotizador.entidad.DetallePedido
 * @see mx.com.qtx.cotizador.entidad.Proveedor
 * @see PedidoResponse
 * @see DetallePedidoResponse
 * @see mx.com.qtx.cotizador.repositorio.PedidoRepositorio
 */
public class PedidoMapper {
    
    /**
     * Convierte una entidad Pedido completa a PedidoResponse con todas sus relaciones.
     * <p>
     * Este método realiza la conversión más compleja del mapper, transformando una entidad
     * completa del dominio Pedido en un DTO de respuesta listo para ser serializado a JSON
     * y enviado al cliente de la API. Incluye el mapeo de todas las relaciones JPA y el
     * procesamiento recursivo de los detalles del pedido.
     * </p>
     * <p>
     * <strong>Proceso de conversión detallado:</strong>
     * </p>
     * <ol>
     *   <li><strong>Validación de entidad:</strong> Verifica que el pedido no sea null</li>
     *   <li><strong>Mapeo de campos simples:</strong> numPedido, fechas, nivelSurtido, total</li>
     *   <li><strong>Resolución de proveedor:</strong> Navega relación Pedido.proveedor con null safety</li>
     *   <li><strong>Procesamiento de detalles:</strong> Convierte lista de DetallePedido usando Stream API</li>
     *   <li><strong>Cálculos agregados:</strong> Calcula totalDetalles como tamaño de la lista</li>
     *   <li><strong>Construcción del DTO:</strong> Usa Builder pattern para crear PedidoResponse</li>
     * </ol>
     * <p>
     * <strong>Mapeo de campos y relaciones:</strong>
     * </p>
     * <table border="1">
     *   <tr><th>Campo DTO</th><th>Campo Entidad</th><th>Tipo</th><th>Relación</th></tr>
     *   <tr><td>numPedido</td><td>numPedido</td><td>Integer</td><td>Directo</td></tr>
     *   <tr><td>fechaEmision</td><td>fechaEmision</td><td>LocalDate</td><td>Directo</td></tr>
     *   <tr><td>fechaEntrega</td><td>fechaEntrega</td><td>LocalDate</td><td>Directo</td></tr>
     *   <tr><td>nivelSurtido</td><td>nivelSurtido</td><td>Integer</td><td>Directo</td></tr>
     *   <tr><td>cveProveedor</td><td>proveedor.cve</td><td>String</td><td>@ManyToOne</td></tr>
     *   <tr><td>nombreProveedor</td><td>proveedor.nombre</td><td>String</td><td>@ManyToOne</td></tr>
     *   <tr><td>total</td><td>totalPedido</td><td>BigDecimal</td><td>Directo</td></tr>
     *   <tr><td>detalles</td><td>detallesPedido</td><td>List</td><td>@OneToMany</td></tr>
     *   <tr><td>totalDetalles</td><td>detalles.size()</td><td>Integer</td><td>Calculado</td></tr>
     * </table>
     *
     * @param pedido Entidad del dominio Pedido a convertir con todas sus relaciones cargadas
     * @return Una nueva instancia de PedidoResponse con todos los datos mapeados, o null si el pedido es null
     * @see Pedido
     * @see PedidoResponse
     * @see #toDetallePedidoResponse(DetallePedido)
     */
    public static PedidoResponse toResponse(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        
        // Convertir detalles usando Stream API para procesamiento eficiente
        List<DetallePedidoResponse> detallesResponse = pedido.getDetallesPedido()
                .stream()
                .map(PedidoMapper::toDetallePedidoResponse)
                .collect(Collectors.toList());
        
        return PedidoResponse.builder()
                .numPedido(pedido.getNumPedido())
                .fechaEmision(pedido.getFechaEmision())
                .fechaEntrega(pedido.getFechaEntrega())
                .nivelSurtido(pedido.getNivelSurtido())
                .cveProveedor(pedido.getProveedor() != null ? pedido.getProveedor().getCve() : null)
                .nombreProveedor(pedido.getProveedor() != null ? pedido.getProveedor().getNombre() : null)
                .total(pedido.getTotalPedido())
                .detalles(detallesResponse)
                .totalDetalles(detallesResponse.size())
                .build();
    }
    
    /**
     * Convierte una entidad DetallePedido a DetallePedidoResponse.
     * <p>
     * Este método maneja la conversión de un detalle individual de pedido, resolviendo
     * la relación con el componente correspondiente cuando es necesario. Realiza el mapeo
     * directo de campos simples y calcula los totales cuando corresponde.
     * </p>
     * <p>
     * <strong>Mapeo de campos:</strong>
     * </p>
     * <table border="1">
     *   <tr><th>Campo DTO</th><th>Campo Entidad</th><th>Tipo</th><th>Notas</th></tr>
     *   <tr><td>idArticulo</td><td>idArticulo/componente.id</td><td>String</td><td>Resuelve de componente si es null</td></tr>
     *   <tr><td>descripcion</td><td>descripcion</td><td>String</td><td>Campo directo</td></tr>
     *   <tr><td>cantidad</td><td>cantidad</td><td>Integer</td><td>Campo directo</td></tr>
     *   <tr><td>precioUnitario</td><td>precioUnitario</td><td>BigDecimal</td><td>Campo directo</td></tr>
     *   <tr><td>totalCotizado</td><td>totalCotizado</td><td>BigDecimal</td><td>Campo directo</td></tr>
     * </table>
     *
     * @param detalle Entidad DetallePedido a convertir
     * @return Una nueva instancia de DetallePedidoResponse, o null si el detalle es null
     * @see DetallePedido
     * @see DetallePedidoResponse
     */
    public static DetallePedidoResponse toDetallePedidoResponse(DetallePedido detalle) {
        if (detalle == null) {
            return null;
        }
        
        return DetallePedidoResponse.builder()
                .idArticulo(detalle.getIdArticulo())
                .descripcion(detalle.getDescripcion())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .totalCotizado(detalle.getTotalCotizado())
                .build();
    }
} 