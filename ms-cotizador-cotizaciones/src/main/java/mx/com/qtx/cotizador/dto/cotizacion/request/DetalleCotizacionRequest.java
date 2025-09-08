package mx.com.qtx.cotizador.dto.cotizacion.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para detalles individuales de una solicitud de cotización.
 * <p>
 * Esta clase representa un componente específico dentro de una cotización, especificando
 * qué componente se desea, en qué cantidad, y opcionalmente información adicional como
 * descripción y precio. Es parte integral de {@link CotizacionCreateRequest} y se valida
 * como parte del proceso de creación de cotizaciones.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Especificar componentes:</strong> Define qué componente incluir en la cotización</li>
 *   <li><strong>Controlar cantidades:</strong> Especifica cuántas unidades del componente</li>
 *   <li><strong>Información adicional:</strong> Permite sobrescribir descripción y precio del catálogo</li>
 *   <li><strong>Validación granular:</strong> Validaciones específicas para cada detalle</li>
 *   <li><strong>Flexibilidad:</strong> Campos opcionales permiten usar datos del catálogo</li>
 * </ul>
 *
 * <h3>Estructura JSON esperada:</h3>
 * <pre>{@code
 * {
 *   "idComponente": "MON001",
 *   "cantidad": 2,
 *   "descripcion": "Monitor LED 27 pulgadas 4K UHD (opcional)",
 *   "precioBase": 4500.00
 * }
 *
 * // Ejemplo mínimo (usando datos del catálogo):
 * {
 *   "idComponente": "HDD001",
 *   "cantidad": 1
 * }
 * }</pre>
 *
 * <h3>Campos requeridos vs opcionales:</h3>
 * <table border="1">
 *   <tr><th>Campo</th><th>Requerido</th><th>Propósito</th><th>Fuente alternativa</th></tr>
 *   <tr><td>idComponente</td><td>✅ Sí</td><td>Identificar componente</td><td>N/A</td></tr>
 *   <tr><td>cantidad</td><td>✅ Sí</td><td>Especificar unidades</td><td>N/A</td></tr>
 *   <tr><td>descripcion</td><td>❌ No</td><td>Sobrescribir descripción</td><td>Catálogo de componentes</td></tr>
 *   <tr><td>precioBase</td><td>❌ No</td><td>Sobrescribir precio</td><td>Catálogo de componentes</td></tr>
 * </table>
 *
 * <h3>Validaciones implementadas:</h3>
 * <p>
 * La clase incluye validaciones específicas para asegurar la integridad de cada detalle:
 * </p>
 * <ul>
 *   <li><strong>@NotBlank:</strong> ID del componente es obligatorio y no vacío</li>
 *   <li><strong>@Size:</strong> Límites de caracteres para prevenir datos excesivos</li>
 *   <li><strong>@NotNull:</strong> Cantidad debe estar presente</li>
 *   <li><strong>@Positive:</strong> Cantidad y precio deben ser valores positivos</li>
 *   <li><strong>Mensajes personalizados:</strong> Errores descriptivos en español</li>
 * </ul>
 *
 * <h3>Uso de campos opcionales:</h3>
 * <p>
 * Los campos opcionales permiten flexibilidad en cómo se especifica la información:
 * </p>
 * <h4>Escenario 1: Usar datos del catálogo (recomendado)</h4>
 * <pre>{@code
 * {
 *   "idComponente": "GPU001",
 *   "cantidad": 1
 *   // descripcion y precioBase se obtienen automáticamente del catálogo
 * }
 * }</pre>
 *
 * <h4>Escenario 2: Sobrescribir información</h4>
 * <pre>{@code
 * {
 *   "idComponente": "GPU001",
 *   "cantidad": 1,
 *   "descripcion": "Tarjeta gráfica especial con overclock",
 *   "precioBase": 5500.00
 *   // Se usa la información proporcionada en lugar de la del catálogo
 * }
 * }</pre>
 *
 * <h3>Integración con el sistema de cotización:</h3>
 * <p>
 * Este DTO se utiliza dentro de {@link CotizacionCreateRequest} y es procesado por:
 * </p>
 * <ol>
 *   <li><strong>Validación:</strong> Bean Validation verifica restricciones</li>
 *   <li><strong>Resolución:</strong> Se obtiene el componente del catálogo usando idComponente</li>
 *   <li><strong>Fusión de datos:</strong> Campos opcionales sobrescriben datos del catálogo</li>
 *   <li><strong>Cálculo:</strong> Se utiliza en el algoritmo de cotización seleccionado</li>
 *   <li><strong>Resultado:</strong> Contribuye al total de la cotización</li>
 * </ol>
 *
 * <h3>Consideraciones de rendimiento:</h3>
 * <ul>
 *   <li><strong>Búsqueda en catálogo:</strong> El idComponente debe existir para evitar errores</li>
 *   <li><strong>Validación de stock:</strong> Considerar validar disponibilidad antes de cotizar</li>
 *   <li><strong>Precisión decimal:</strong> BigDecimal asegura cálculos exactos de precios</li>
 *   <li><strong>Cantidad razonable:</strong> Validar límites superiores de cantidad</li>
 * </ul>
 *
 * <h3>Ejemplo de procesamiento en servicio:</h3>
 * <pre>{@code
 * public Cotizacion procesarDetalle(DetalleCotizacionRequest detalle) {
 *     // 1. Obtener componente del catálogo
 *     Componente componenteCatalogo = componenteService.obtenerPorId(detalle.getIdComponente());
 *
 *     // 2. Fusionar datos (opcional sobrescrive catálogo)
 *     String descripcionFinal = detalle.getDescripcion() != null ?
 *         detalle.getDescripcion() : componenteCatalogo.getDescripcion();
 *
 *     BigDecimal precioFinal = detalle.getPrecioBase() != null ?
 *         detalle.getPrecioBase() : componenteCatalogo.getPrecioBase();
 *
 *     // 3. Crear instancia para cotización
 *     ComponenteParaCotizar componenteCotizado = new ComponenteParaCotizar(
 *         componenteCatalogo.getId(),
 *         descripcionFinal,
 *         precioFinal
 *     );
 *
 *     // 4. Agregar al cotizador con cantidad especificada
 *     cotizador.agregarComponente(detalle.getCantidad(), componenteCotizado);
 *
 *     return cotizacion;
 * }
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see CotizacionCreateRequest
 * @see CotizacionResponse
 * @see DetalleCotizacionResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCotizacionRequest {

    /**
     * Identificador único del componente en el catálogo del sistema.
     * <p>
     * Este campo hace referencia al ID único del componente que se encuentra
     * registrado en el catálogo de productos del sistema. Es el vínculo principal
     * entre la solicitud de cotización y el componente específico.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotBlank: No puede ser null, vacío o solo espacios</li>
     *   <li>@Size(max=50): Máximo 50 caracteres para compatibilidad</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Requisitos:</strong>
     * <ul>
     *   <li>Debe existir en el catálogo de componentes</li>
     *   <li>Debe ser único dentro del sistema</li>
     *   <li>Generalmente sigue un patrón específico por tipo</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos válidos:</strong> "MON001", "HDD001", "GPU001", "CPU001"
     * </p>
     */
    @NotBlank(message = "El ID del componente es requerido")
    @Size(max = 50, message = "El ID del componente no puede exceder 50 caracteres")
    private String idComponente;

    /**
     * Número de unidades del componente que se desea adquirir.
     * <p>
     * Especifica cuántas unidades del componente identificado se incluyen
     * en este detalle de la cotización. Este valor se utiliza directamente
     * en los cálculos de precios y promociones.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotNull: La cantidad debe estar presente</li>
     *   <li>@Positive: Debe ser un número entero positivo mayor que cero</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Consideraciones:</strong>
     * <ul>
     *   <li>El valor mínimo es 1 (no se permiten cantidades negativas o cero)</li>
     *   <li>Se utiliza en multiplicaciones de precio: precioUnitario × cantidad</li>
     *   <li>Afecta el cálculo de promociones (ej: "3x2" requiere cantidad ≥ 3)</li>
     *   <li>Debe ser razonable según el tipo de componente</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Rango típico:</strong> 1-100 unidades (depende del componente y negocio)
     * </p>
     */
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer cantidad;

    /**
     * Descripción alternativa del componente (opcional).
     * <p>
     * Permite sobrescribir la descripción estándar del componente que se
     * encuentra en el catálogo. Útil cuando se necesita especificar características
     * particulares, personalizaciones o condiciones especiales.
     * </p>
     * <p>
     * <strong>Campo opcional:</strong> Si no se proporciona, se usa la descripción del catálogo
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@Size(max=200): Máximo 200 caracteres si se proporciona</li>
     *   <li>No tiene @NotBlank porque es opcional</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Casos de uso:</strong>
     * <ul>
     *   <li>Especificar color, tamaño o características especiales</li>
     *   <li>Indicar personalizaciones o modificaciones</li>
     *   <li>Agregar notas específicas para este pedido</li>
     *   <li>Corregir o actualizar información del catálogo</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplo:</strong> "Monitor LED 27\" con calibración de color profesional"
     * </p>
     */
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    /**
     * Precio unitario alternativo para el componente (opcional).
     * <p>
     * Permite especificar un precio unitario diferente al que está registrado
     * en el catálogo. Útil para ofertas especiales, descuentos personalizados
     * o precios negociados específicamente para este cliente.
     * </p>
     * <p>
     * <strong>Campo opcional:</strong> Si no se proporciona, se usa el precio del catálogo
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@Positive: Si se proporciona, debe ser mayor que cero</li>
     *   <li>No tiene @NotNull porque es opcional</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Casos de uso:</strong>
     * <ul>
     *   <li>Ofertas especiales para clientes corporativos</li>
     *   <li>Descuentos por volumen o lealtad</li>
     *   <li>Precios promocionales temporales</li>
     *   <li>Ajustes por condiciones de mercado</li>
     *   <li>Precios negociados para proyectos grandes</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Consideraciones:</strong>
     * <ul>
     *   <li>Usa BigDecimal para precisión decimal</li>
     *   <li>Debe incluir todos los impuestos aplicables</li>
     *   <li>Se utiliza para calcular: precioUnitario × cantidad</li>
     *   <li>Puede afectar el cálculo de promociones</li>
     * </ul>
     * </p>
     */
    @Positive(message = "El precio base debe ser positivo")
    private BigDecimal precioBase;
} 