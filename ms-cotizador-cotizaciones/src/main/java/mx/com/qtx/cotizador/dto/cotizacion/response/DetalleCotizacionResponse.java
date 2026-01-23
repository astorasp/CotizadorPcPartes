package mx.com.qtx.cotizador.dto.cotizacion.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) de respuesta para detalles individuales de cotización.
 * <p>
 * Esta clase representa la información detallada de un componente específico dentro
 * de una cotización generada. Forma parte integral de {@link CotizacionResponse}
 * y proporciona toda la información necesaria sobre cada producto cotizado,
 * incluyendo cantidades, precios, descripciones y cálculos específicos.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Detalle granular:</strong> Proporciona información específica de cada componente cotizado</li>
 *   <li><strong>Desglose de cotización:</strong> Permite al cliente ver el detalle de cada línea</li>
 *   <li><strong>Cálculos por línea:</strong> Incluye importes calculados para cada componente</li>
 *   <li><strong>Información completa:</strong> Combina datos del catálogo con cálculos específicos</li>
 *   <li><strong>Serialización JSON:</strong> Optimizado para transmisión y consumo por clientes</li>
 *   <li><strong>Integración con frontend:</strong> Ideal para mostrar tablas detalladas de cotización</li>
 * </ul>
 *
 * <h3>Estructura JSON de un detalle:</h3>
 * <pre>{@code
 * {
 *   "numDetalle": 1,
 *   "idComponente": "MON001",
 *   "nombreComponente": "Monitor LED 24\"",
 *   "categoria": "MONITOR",
 *   "cantidad": 2,
 *   "descripcion": "Monitor LED 24\" Full HD con panel IPS",
 *   "precioBase": 2500.00,
 *   "importeTotal": 5000.00
 * }
 * }</pre>
 *
 * <h3>Campos principales y su significado:</h3>
 * <table border="1">
 *   <tr><th>Categoría</th><th>Campos</th><th>Descripción</th></tr>
 *   <tr><td>Orden</td><td>numDetalle</td><td>Número secuencial dentro de la cotización</td></tr>
 *   <tr><td>Identificación</td><td>idComponente, nombreComponente</td><td>ID único y nombre legible del componente</td></tr>
 *   <tr><td>Clasificación</td><td>categoria</td><td>Tipo de componente (MONITOR, DISCO_DURO, etc.)</td></tr>
 *   <tr><td>Cantidad</td><td>cantidad</td><td>Número de unidades del componente</td></tr>
 *   <tr><td>Descripción</td><td>descripcion</td><td>Descripción detallada del producto</td></tr>
 *   <tr><td>Precios</td><td>precioBase, importeTotal</td><td>Precio unitario y total del detalle</td></tr>
 * </table>
 *
 * <h3>Cálculos y fórmulas aplicadas:</h3>
 * <p>
 * Los campos numéricos siguen fórmulas específicas de cálculo:
 * </p>
 * <ul>
 *   <li><strong>Precio base:</strong> Precio unitario del componente (del catálogo o sobrescrito)</li>
 *   <li><strong>Importe total:</strong> cantidad × precioBase (sin impuestos)</li>
 *   <li><strong>Relación con subtotal:</strong> Todos los importeTotal suman el subtotal de la cotización</li>
 *   <li><strong>Promociones aplicadas:</strong> precioBase puede incluir descuentos por promociones</li>
 *   <li><strong>Precisión decimal:</strong> BigDecimal asegura cálculos exactos</li>
 * </ul>
 *
 * <h3>Proceso de generación del detalle:</h3>
 * <p>
 * Cada instancia de DetalleCotizacionResponse se crea siguiendo estos pasos:
 * </p>
 * <ol>
 *   <li><strong>Obtener componente:</strong> Buscar el componente en el catálogo usando idComponente</li>
 *   <li><strong>Fusionar datos:</strong> Combinar información del catálogo con datos de la solicitud</li>
 *   <li><strong>Calcular precios:</strong> Aplicar precioBase (del catálogo o sobrescrito)</li>
 *   <li><strong>Calcular importes:</strong> importeTotal = cantidad × precioBase</li>
 *   <li><strong>Asignar metadatos:</strong> numDetalle, categoria, nombreComponente</li>
 *   <li><strong>Formatear respuesta:</strong> Preparar para serialización JSON</li>
 * </ol>
 *
 * <h3>Campos requeridos vs opcionales:</h3>
 * <table border="1">
 *   <tr><th>Campo</th><th>Requerido</th><th>Tipo</th><th>Fuente</th></tr>
 *   <tr><td>numDetalle</td><td>✅ Sí</td><td>Integer</td><td>Generado automáticamente</td></tr>
 *   <tr><td>idComponente</td><td>✅ Sí</td><td>String</td><td>Del catálogo de componentes</td></tr>
 *   <tr><td>nombreComponente</td><td>✅ Sí</td><td>String</td><td>Del catálogo de componentes</td></tr>
 *   <tr><td>categoria</td><td>✅ Sí</td><td>String</td><td>Del catálogo de componentes</td></tr>
 *   <tr><td>cantidad</td><td>✅ Sí</td><td>Integer</td><td>De la solicitud del cliente</td></tr>
 *   <tr><td>descripcion</td><td>❌ No</td><td>String</td><td>Catálogo o solicitud del cliente</td></tr>
 *   <tr><td>precioBase</td><td>✅ Sí</td><td>BigDecimal</td><td>Catálogo o solicitud del cliente</td></tr>
 *   <tr><td>importeTotal</td><td>✅ Sí</td><td>BigDecimal</td><td>Calculado: cantidad × precioBase</td></tr>
 * </table>
 *
 * <h3>Ejemplo de procesamiento en servicio:</h3>
 * <pre>{@code
 * public DetalleCotizacionResponse procesarDetalle(DetalleCotizacionRequest request) {
 *     // 1. Obtener componente del catálogo
 *     Componente componenteCatalogo = componenteService.obtenerPorId(request.getIdComponente());
 *
 *     // 2. Determinar precio a usar (catálogo o sobrescrito)
 *     BigDecimal precioFinal = request.getPrecioBase() != null ?
 *         request.getPrecioBase() : componenteCatalogo.getPrecioBase();
 *
 *     // 3. Calcular importe total
 *     BigDecimal importeTotal = precioFinal.multiply(
 *         BigDecimal.valueOf(request.getCantidad())
 *     );
 *
 *     // 4. Crear respuesta
 *     return DetalleCotizacionResponse.builder()
 *         .numDetalle(generarNumeroDetalle())
 *         .idComponente(componenteCatalogo.getId())
 *         .nombreComponente(componenteCatalogo.getDescripcion())
 *         .categoria(componenteCatalogo.getTipoComponente().getNombre())
 *         .cantidad(request.getCantidad())
 *         .descripcion(request.getDescripcion() != null ?
 *             request.getDescripcion() : componenteCatalogo.getDescripcion())
 *         .precioBase(precioFinal)
 *         .importeTotal(importeTotal)
 *         .build();
 * }
 * }</pre>
 *
 * <h3>Consideraciones de diseño:</h3>
 * <ul>
 *   <li><strong>Lombok:</strong> Usa @Data para generar getters/setters automáticamente</li>
 *   <li><strong>Builder Pattern:</strong> Anotación @Builder permite construcción fluida</li>
 *   <li><strong>Constructores múltiples:</strong> Constructor vacío y completo disponibles</li>
 *   <li><strong>Precisión decimal:</strong> BigDecimal para cálculos exactos de precios</li>
 *   <li><strong>Inmutabilidad opcional:</strong> Los campos pueden modificarse después de la construcción</li>
 *   <li><strong>Null safety:</strong> Algunos campos opcionales pueden ser null</li>
 * </ul>
 *
 * <h3>Relación con otros componentes:</h3>
 * <ul>
 *   <li><strong>CotizacionResponse:</strong> Esta clase forma parte de la lista detalles</li>
 *   <li><strong>DetalleCotizacionRequest:</strong> DTO de entrada correspondiente</li>
 *   <li><strong>CotizacionMapper:</strong> Convierte entidades a este DTO</li>
 *   <li><strong>Componente:</strong> Entidad del dominio que proporciona los datos base</li>
 *   <li><strong>ICotizador:</strong> Algoritmos que calculan los precios finales</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see CotizacionResponse
 * @see DetalleCotizacionRequest
 * @see CotizacionMapper
 * @see mx.com.qtx.cotizador.dominio.core.Componente
 * @see mx.com.qtx.cotizador.dominio.core.ICotizador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCotizacionResponse {

    /**
     * Número secuencial del detalle dentro de la cotización.
     * <p>
     * Identificador numérico único que indica la posición ordinal de este detalle
     * dentro de la lista de componentes de la cotización. Se utiliza para ordenar
     * y referenciar los detalles en la interfaz de usuario.
     * </p>
     * <p>
     * <strong>Características:</strong>
     * <ul>
     *   <li>Secuencial comenzando desde 1</li>
     *   <li>Único dentro de la cotización</li>
     *   <li>Determina el orden de presentación</li>
     *   <li>Generado automáticamente por el sistema</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Rango típico:</strong> 1, 2, 3, ... (hasta el número total de detalles)
     * </p>
     */
    private Integer numDetalle;

    /**
     * Identificador único del componente en el catálogo del sistema.
     * <p>
     * Código único que identifica al componente específico dentro del sistema.
     * Este ID es la clave principal para relacionar el detalle con el componente
     * correspondiente en el catálogo de productos.
     * </p>
     * <p>
     * <strong>Propósito:</strong>
     * <ul>
     *   <li>Vinculación con el catálogo de componentes</li>
     *   <li>Referencia para operaciones CRUD posteriores</li>
     *   <li>Búsqueda y filtrado de componentes</li>
     *   <li>Integridad referencial con el inventario</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Formato:</strong> Generalmente alfanumérico con patrón específico
     * <br><strong>Ejemplos:</strong> "MON001", "HDD001", "GPU001", "CPU001"
     * </p>
     */
    private String idComponente;

    /**
     * Nombre comercial legible del componente.
     * <p>
     * Nombre descriptivo del componente que es fácil de entender para el usuario final.
     * Este nombre se obtiene del catálogo de componentes y puede ser diferente al ID técnico.
     * </p>
     * <p>
     * <strong>Uso principal:</strong>
     * <ul>
     *   <li>Presentación en interfaces de usuario</li>
     *   <li>Descripciones en reportes de cotización</li>
     *   <li>Comunicación con clientes</li>
     *   <li>Identificación visual del producto</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos:</strong>
     * <ul>
     *   <li>"Monitor LED 24\" Full HD"</li>
     *   <li>"Disco duro SSD 1TB NVMe"</li>
     *   <li>"Tarjeta gráfica NVIDIA RTX 4070"</li>
     * </ul>
     * </p>
     */
    private String nombreComponente;

    /**
     * Clasificación por tipo del componente.
     * <p>
     * Categoría o tipo al que pertenece el componente, que determina sus
     * características específicas y comportamiento en el sistema.
     * </p>
     * <p>
     * <strong>Valores posibles:</strong>
     * <ul>
     *   <li>"MONITOR" - Monitores y pantallas</li>
     *   <li>"DISCO_DURO" - Discos duros y unidades de almacenamiento</li>
     *   <li>"TARJETA_VIDEO" - Tarjetas de video/GPUs</li>
     *   <li>"PC" - Computadoras completas</li>
     *   <li>Otros tipos según la configuración del sistema</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Utilidad:</strong>
     * <ul>
     *   <li>Agrupamiento y filtrado de componentes</li>
     *   <li>Aplicación de reglas específicas por tipo</li>
     *   <li>Presentación organizada en catálogos</li>
     *   <li>Análisis y reportes por categorías</li>
     * </ul>
     * </p>
     */
    private String categoria;

    /**
     * Número de unidades del componente solicitadas.
     * <p>
     * Cantidad específica de unidades del componente que el cliente desea adquirir.
     * Este valor se utiliza directamente en todos los cálculos de precios y promociones.
     * </p>
     * <p>
     * <strong>Validación:</strong>
     * <ul>
     *   <li>Siempre mayor que cero</li>
     *   <li>Entero positivo</li>
     *   <li>Limitado por disponibilidad de inventario</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Impacto en cálculos:</strong>
     * <ul>
     *   <li>importeTotal = cantidad × precioBase</li>
     *   <li>Afecta aplicación de promociones (ej: descuentos por cantidad)</li>
     *   <li>Determina el subtotal de la cotización</li>
     *   <li>Importante para logística y entrega</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Rango típico:</strong> 1-100 unidades (depende del componente y política comercial)
     * </p>
     */
    private Integer cantidad;

    /**
     * Descripción detallada del componente.
     * <p>
     * Texto descriptivo completo que incluye las especificaciones técnicas,
     * características principales y cualquier información relevante del componente.
     * Esta descripción puede provenir del catálogo o ser personalizada para la cotización.
     * </p>
     * <p>
     * <strong>Contenido típico:</strong>
     * <ul>
     *   <li>Especificaciones técnicas (resolución, capacidad, velocidad)</li>
     *   <li>Características físicas (tamaño, peso, conectores)</li>
     *   <li>Compatibilidad y requisitos del sistema</li>
     *   <li>Beneficios y ventajas del producto</li>
     *   <li>Notas especiales o consideraciones de instalación</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Uso en la cotización:</strong>
     * <ul>
     *   <li>Presentación detallada al cliente</li>
     *   <li>Información para toma de decisiones</li>
     *   <li>Especificaciones técnicas para comparación</li>
     *   <li>Base para personalización de pedidos</li>
     * </ul>
     * </p>
     */
    private String descripcion;

    /**
     * Precio unitario base del componente.
     * <p>
     * Precio por unidad del componente después de aplicar cualquier promoción
     * o descuento. Este es el precio que efectivamente se cobra por cada unidad
     * del componente en esta cotización específica.
     * </p>
     * <p>
     * <strong>Características del precio:</strong>
     * <ul>
     *   <li>Puede ser diferente al precio estándar del catálogo</li>
     *   <li>Incluye descuentos por promociones aplicadas</li>
     *   <li>Sirve como base para calcular importeTotal</li>
     *   <li>Precisión decimal garantizada con BigDecimal</li>
     *   <li>Siempre positivo y mayor que cero</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Relación con promociones:</strong>
     * <ul>
     *   <li>precioBase = precioCatalogo - descuentosAplicados</li>
     *   <li>Puede incluir descuentos por cantidad</li>
     *   <li>Puede reflejar ofertas especiales</li>
     *   <li>Es el precio final por unidad para esta cotización</li>
     * </ul>
     * </p>
     */
    private BigDecimal precioBase;

    /**
     * Importe total del detalle de cotización.
     * <p>
     * Monto total correspondiente a este detalle específico, calculado como
     * el producto de la cantidad por el precio base. Este importe contribuye
     * directamente al subtotal de la cotización completa.
     * </p>
     * <p>
     * <strong>Fórmula de cálculo:</strong>
     * <pre>
     * importeTotal = cantidad × precioBase
     * </pre>
     * </p>
     * <p>
     * <strong>Características:</strong>
     * <ul>
     *   <li>Calculado automáticamente por el sistema</li>
     *   <li>Precisión decimal con BigDecimal</li>
     *   <li>Siempre positivo (cantidad > 0, precioBase > 0)</li>
     *   <li>Parte del subtotal de la cotización</li>
     *   <li>No incluye impuestos (se calculan a nivel de cotización)</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Relación con la cotización:</strong>
     * <ul>
     *   <li>Suma de todos los importeTotal = subtotal de la cotización</li>
     *   <li>Contribuye al cálculo de impuestos</li>
     *   <li>Determina el total final de la cotización</li>
     *   <li>Importante para análisis de rentabilidad por producto</li>
     * </ul>
     * </p>
     */
    private BigDecimal importeTotal;
} 