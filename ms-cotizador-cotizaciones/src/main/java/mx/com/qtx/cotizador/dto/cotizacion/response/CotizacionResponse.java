package mx.com.qtx.cotizador.dto.cotizacion.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) de respuesta completa para cotizaciones en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase representa la estructura de datos que se envía al cliente de la API REST
 * cuando se solicita información completa de una cotización generada. Incluye todos
 * los detalles financieros, componentes cotizados, y metadatos relevantes de manera
 * estructurada y serializable a JSON.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Respuesta de cotización completa:</strong> Proporciona toda la información de una cotización generada</li>
 *   <li><strong>Información financiera detallada:</strong> Subtotales, impuestos y totales desglosados</li>
 *   <li><strong>Detalles de componentes:</strong> Lista completa de productos cotizados con cantidades y precios</li>
 *   <li><strong>Metadatos de cotización:</strong> Folio, fecha, observaciones y otros datos relevantes</li>
 *   <li><strong>Serialización JSON:</strong> Optimizado para respuesta HTTP y consumo por clientes</li>
 *   <li><strong>Integración con frontend:</strong> Formato ideal para mostrar cotizaciones al usuario</li>
 * </ul>
 *
 * <h3>Estructura JSON de respuesta completa:</h3>
 * <pre>{@code
 * {
 *   "folio": 12345,
 *   "fecha": "2025-01-17",
 *   "subtotal": 5500.00,
 *   "impuestos": 880.00,
 *   "total": 6380.00,
 *   "detalles": [
 *     {
 *       "numDetalle": 1,
 *       "idComponente": "MON001",
 *       "nombreComponente": "Monitor LED 24\"",
 *       "categoria": "MONITOR",
 *       "cantidad": 2,
 *       "descripcion": "Monitor LED 24\" Full HD",
 *       "precioBase": 2500.00,
 *       "importeTotal": 5000.00
 *     },
 *     {
 *       "numDetalle": 2,
 *       "idComponente": "HDD001",
 *       "nombreComponente": "Disco SSD 1TB",
 *       "categoria": "DISCO_DURO",
 *       "cantidad": 1,
 *       "descripcion": "SSD NVMe 1TB de alta velocidad",
 *       "precioBase": 2200.00,
 *       "importeTotal": 2200.00
 *     }
 *   ],
 *   "observaciones": "Cotización válida por 30 días"
 * }
 * }</pre>
 *
 * <h3>Campos principales y su significado:</h3>
 * <table border="1">
 *   <tr><th>Categoría</th><th>Campos</th><th>Descripción</th></tr>
 *   <tr><td>Identificación</td><td>folio, fecha</td><td>Identificación única y temporal de la cotización</td></tr>
 *   <tr><td>Financiero</td><td>subtotal, impuestos, total</td><td>Desglose completo de montos</td></tr>
 *   <tr><td>Contenido</td><td>detalles</td><td>Lista detallada de componentes cotizados</td></tr>
 *   <tr><td>Metadatos</td><td>observaciones</td><td>Información adicional opcional</td></tr>
 * </table>
 *
 * <h3>Cálculos y relaciones entre campos:</h3>
 * <p>
 * Los campos financieros mantienen relaciones matemáticas precisas:
 * </p>
 * <ul>
 *   <li><strong>Subtotal:</strong> Suma de (cantidad × precioBase) para todos los detalles</li>
 *   <li><strong>Impuestos:</strong> Cálculo basado en tipos de impuesto aplicados (IVA, FEDERAL, LOCAL)</li>
 *   <li><strong>Total:</strong> subtotal + impuestos</li>
 *   <li><strong>Importe por detalle:</strong> cantidad × precioBase (sin impuestos)</li>
 *   <li><strong>Relación detalle-total:</strong> Cada detalle contribuye al subtotal total</li>
 * </ul>
 *
 * <h3>Proceso de generación de respuesta:</h3>
 * <p>
 * Una instancia de CotizacionResponse se genera siguiendo estos pasos:
 * </p>
 * <ol>
 *   <li><strong>Crear cotización base:</strong> Usar algoritmo de cotización (A o B)</li>
 *   <li><strong>Agregar componentes:</strong> Procesar cada DetalleCotizacionRequest</li>
 *   <li><strong>Calcular promociones:</strong> Aplicar descuentos y ofertas activas</li>
 *   <li><strong>Calcular impuestos:</strong> Aplicar tipos de impuesto especificados</li>
 *   <li><strong>Generar totales:</strong> Calcular subtotal, impuestos y total final</li>
 *   <li><strong>Crear detalles:</strong> Generar DetalleCotizacionResponse para cada componente</li>
 *   <li><strong>Asignar metadatos:</strong> Folio, fecha, observaciones</li>
 *   <li><strong>Mapear respuesta:</strong> Usar CotizacionMapper para conversión final</li>
 * </ol>
 *
 * <h3>Ejemplo de uso en controlador:</h3>
 * <pre>{@code
 * @PostMapping("/cotizaciones")
 * public ResponseEntity<ApiResponse<CotizacionResponse>> crearCotizacion(
 *         @Valid @RequestBody CotizacionCreateRequest request) {
 *
 *     try {
 *         // 1. Crear cotizador según tipo solicitado
 *         ICotizador cotizador = crearCotizador(request.getTipoCotizador());
 *
 *         // 2. Agregar componentes al cotizador
 *         for (DetalleCotizacionRequest detalle : request.getDetalles()) {
 *             Componente componente = componenteService.obtenerPorId(detalle.getIdComponente());
 *             cotizador.agregarComponente(detalle.getCantidad(), componente);
 *         }
 *
 *         // 3. Generar cotización con impuestos
 *         Cotizacion cotizacion = cotizador.generarCotizacion(
 *             crearCalculadoresImpuesto(request.getImpuestos())
 *         );
 *
 *         // 4. Convertir a DTO de respuesta
 *         CotizacionResponse response = CotizacionMapper.toResponse(cotizacion);
 *
 *         // 5. Agregar metadatos adicionales si es necesario
 *         response.setFolio(generarFolioUnico());
 *         response.setFecha(request.getFecha() != null ? request.getFecha() :
 *                          LocalDate.now().toString());
 *         response.setObservaciones(request.getObservaciones());
 *
 *         return ResponseEntity.ok(new ApiResponse<>("0", "Cotización creada", response));
 *
 *     } catch (Exception e) {
 *         return ResponseEntity.badRequest()
 *             .body(new ApiResponse<>("1", "Error al crear cotización: " + e.getMessage()));
 *     }
 * }
 * }</pre>
 *
 * <h3>Consideraciones de rendimiento:</h3>
 * <ul>
 *   <li><strong>Cálculos precisos:</strong> Usa BigDecimal para evitar errores de redondeo</li>
 *   <li><strong>Eficiencia de memoria:</strong> Lista de detalles puede ser grande para cotizaciones complejas</li>
 *   <li><strong>Serialización JSON:</strong> Optimizado para transmisión eficiente</li>
 *   <li><strong>Caché opcional:</strong> Cotizaciones grandes pueden beneficiarse de caché</li>
 * </ul>
 *
 * <h3>Campos opcionales vs requeridos:</h3>
 * <table border="1">
 *   <tr><th>Campo</th><th>Requerido</th><th>Tipo</th><th>Notas</th></tr>
 *   <tr><td>folio</td><td>✅ Sí</td><td>Integer</td><td>Identificador único generado</td></tr>
 *   <tr><td>fecha</td><td>✅ Sí</td><td>String</td><td>Fecha de generación (yyyy-MM-dd)</td></tr>
 *   <tr><td>subtotal</td><td>✅ Sí</td><td>BigDecimal</td><td>Siempre calculado</td></tr>
 *   <tr><td>impuestos</td><td>✅ Sí</td><td>BigDecimal</td><td>Puede ser cero</td></tr>
 *   <tr><td>total</td><td>✅ Sí</td><td>BigDecimal</td><td>subtotal + impuestos</td></tr>
 *   <tr><td>detalles</td><td>✅ Sí</td><td>List</td><td>Mínimo un detalle</td></tr>
 *   <tr><td>observaciones</td><td>❌ No</td><td>String</td><td>Texto libre opcional</td></tr>
 * </table>
 *
 * <h3>Integración con otros componentes:</h3>
 * <ul>
 *   <li><strong>CotizacionMapper:</strong> Convierte entidades Cotizacion a este DTO</li>
 *   <li><strong>ICotizador:</strong> Algoritmos A y B generan la cotización base</li>
 *   <li><strong>CalculadorImpuesto:</strong> Calcula los impuestos aplicados</li>
 *   <li><strong>ApiResponse:</strong> Envuelve esta respuesta para la API REST</li>
 *   <li><strong>Frontend:</strong> Consume este formato para mostrar cotizaciones</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see CotizacionCreateRequest
 * @see DetalleCotizacionResponse
 * @see CotizacionMapper
 * @see mx.com.qtx.cotizador.dominio.core.Cotizacion
 * @see mx.com.qtx.cotizador.dominio.core.ICotizador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionResponse {

    /**
     * Folio único identificador de la cotización.
     * <p>
     * Número secuencial único que identifica a esta cotización dentro del sistema.
     * Se genera automáticamente y sirve como referencia principal para seguimiento,
     * búsqueda y relación con otras entidades del sistema.
     * </p>
     * <p>
     * <strong>Características:</strong>
     * <ul>
     *   <li>Único en todo el sistema</li>
     *   <li>Secuencial o con patrón específico</li>
     *   <li>Inmutable después de la creación</li>
     *   <li>Utilizado como referencia en órdenes de compra</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos:</strong> 12345, 2025001, COT-001
     * </p>
     */
    private Integer folio;

    /**
     * Fecha de emisión de la cotización.
     * <p>
     * Fecha en la que se generó la cotización, expresada en formato de cadena.
     * Esta fecha es importante para determinar la vigencia de precios, promociones
     * aplicadas y para seguimiento temporal de las cotizaciones.
     * </p>
     * <p>
     * <strong>Formato esperado:</strong> yyyy-MM-dd (ej: "2025-01-17")
     * </p>
     * <p>
     * <strong>Consideraciones:</strong>
     * <ul>
     *   <li>Generalmente la fecha actual del sistema</li>
     *   <li>Puede ser especificada por el usuario en casos especiales</li>
     *   <li>Importante para cálculos de vigencia de precios</li>
     *   <li>Se utiliza en reportes y análisis históricos</li>
     * </ul>
     * </p>
     */
    private String fecha;

    /**
     * Subtotal de la cotización antes de aplicar impuestos.
     * <p>
     * Monto total de todos los componentes cotizados sin incluir ningún tipo
     * de impuesto. Representa la suma de (cantidad × precioBase) para todos
     * los detalles de la cotización, después de aplicar promociones pero antes
     * de impuestos.
     * </p>
     * <p>
     * <strong>Cálculo:</strong>
     * <ul>
     *   <li>Suma de importeTotal de todos los detalles</li>
     *   <li>Incluye descuentos por promociones aplicadas</li>
     *   <li>Excluye cualquier tipo de impuesto</li>
     *   <li>Precisión decimal con BigDecimal</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Relación con otros campos:</strong>
     * <ul>
     *   <li>subtotal + impuestos = total</li>
     *   <li>Siempre ≥ 0</li>
     *   <li>Base para cálculo de impuestos</li>
     * </ul>
     * </p>
     */
    private BigDecimal subtotal;

    /**
     * Monto total de impuestos aplicados a la cotización.
     * <p>
     * Suma de todos los impuestos calculados sobre el subtotal de la cotización.
     * Los impuestos se calculan según los tipos especificados en la solicitud
     * (IVA, FEDERAL, LOCAL) y se aplican secuencialmente sobre el subtotal.
     * </p>
     * <p>
     * <strong>Tipos de impuesto incluidos:</strong>
     * <ul>
     *   <li>IVA (Impuesto al Valor Agregado)</li>
     *   <li>Impuestos federales adicionales</li>
     *   <li>Impuestos locales/municipales</li>
     *   <li>Cualquier otro tipo configurado en el sistema</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Características:</strong>
     * <ul>
     *   <li>Puede ser cero si no se aplican impuestos</li>
     *   <li>Se calcula sobre el subtotal (no sobre precios individuales)</li>
     *   <li>Afecta directamente al total final</li>
     *   <li>Precisión decimal garantizada</li>
     * </ul>
     * </p>
     */
    private BigDecimal impuestos;

    /**
     * Monto total final de la cotización incluyendo todos los conceptos.
     * <p>
     * Importe final que debe pagar el cliente, incluyendo subtotal más todos
     * los impuestos aplicados. Este es el monto más importante de la cotización
     * y el que se utiliza para decisiones de compra.
     * </p>
     * <p>
     * <strong>Cálculo:</strong>
     * <ul>
     *   <li>total = subtotal + impuestos</li>
     *   <li>Incluye todos los componentes y promociones</li>
     *   <li>Representa el costo total para el cliente</li>
     *   <li>Precisión decimal con BigDecimal</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Importancia:</strong>
     * <ul>
     *   <li>Monto principal mostrado al cliente</li>
     *   <li>Base para decisiones de compra</li>
     *   <li>Utilizado en reportes y análisis</li>
     *   <li>Referencia para órdenes de compra</li>
     * </ul>
     * </p>
     */
    private BigDecimal total;

    /**
     * Lista detallada de todos los componentes incluidos en la cotización.
     * <p>
     * Colección completa de DetalleCotizacionResponse que representa cada
     * componente individual cotizado, con sus cantidades, precios, descripciones
     * y otros detalles específicos. Esta lista proporciona el desglose completo
     * de lo que incluye la cotización.
     * </p>
     * <p>
     * <strong>Contenido de cada detalle:</strong>
     * <ul>
     *   <li>ID y nombre del componente</li>
     *   <li>Cantidad solicitada</li>
     *   <li>Precio base unitario</li>
     *   <li>Descripción del producto</li>
     *   <li>Categoría del componente</li>
     *   <li>Importe total del detalle</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Características de la lista:</strong>
     * <ul>
     *   <li>Mínimo un elemento (cotización no vacía)</li>
     *   <li>Ordenada por numDetalle</li>
     *   <li>Contribuye al cálculo del subtotal</li>
     *   <li>Esencial para el detalle de la cotización</li>
     * </ul>
     * </p>
     */
    private List<DetalleCotizacionResponse> detalles;

    /**
     * Observaciones o notas adicionales sobre la cotización.
     * <p>
     * Campo de texto libre que permite incluir información adicional relevante
     * para la cotización, como condiciones especiales, plazos de entrega,
     * términos y condiciones particulares, o cualquier nota que ayude al
     * cliente a entender mejor la cotización.
     * </p>
     * <p>
     * <strong>Campo opcional:</strong> Puede ser null o vacío
     * </p>
     * <p>
     * <strong>Casos de uso comunes:</strong>
     * <ul>
     *   <li>Condiciones de entrega especiales</li>
     *   <li>Plazos de vigencia de la cotización</li>
     *   <li>Descuentos adicionales no calculados</li>
     *   <li>Notas sobre disponibilidad de productos</li>
     *   <li>Información de contacto adicional</li>
     *   <li>Términos y condiciones particulares</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Recomendaciones:</strong>
     * <ul>
     *   <li>Mantener un formato consistente</li>
     *   <li>Incluir información clara y concisa</li>
     *   <li>Evitar información duplicada con otros campos</li>
     *   <li>Considerar límite de caracteres apropiado</li>
     * </ul>
     * </p>
     */
    private String observaciones;
} 