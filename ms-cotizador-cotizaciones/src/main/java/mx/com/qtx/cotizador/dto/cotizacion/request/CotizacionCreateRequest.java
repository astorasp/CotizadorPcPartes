package mx.com.qtx.cotizador.dto.cotizacion.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import java.util.List;

/**
 * DTO (Data Transfer Object) para la solicitud de creación de cotizaciones en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase representa la estructura de datos que el cliente debe enviar a la API REST
 * cuando desea crear una nueva cotización. Incluye toda la información necesaria para
 * generar una cotización completa, incluyendo el tipo de algoritmo de cotización a utilizar,
 * los impuestos a aplicar, los componentes solicitados y metadatos adicionales.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Crear cotizaciones:</strong> Proporciona todos los datos necesarios para generar una cotización</li>
 *   <li><strong>Flexibilidad de algoritmos:</strong> Permite elegir entre diferentes estrategias de cotización</li>
 *   <li><strong>Configuración fiscal:</strong> Especifica qué tipos de impuestos aplicar</li>
 *   <li><strong>Validación completa:</strong> Incluye validaciones exhaustivas de Bean Validation</li>
 *   <li><strong>Integración con API:</strong> Diseñado específicamente para endpoints POST de cotizaciones</li>
 * </ul>
 *
 * <h3>Estructura JSON esperada:</h3>
 * <pre>{@code
 * {
 *   "tipoCotizador": "A",
 *   "impuestos": ["IVA", "FEDERAL"],
 *   "detalles": [
 *     {
 *       "idComponente": "MON001",
 *       "cantidad": 2,
 *       "descripcion": "Monitor LED 24\" Full HD",
 *       "precioBase": 3200.00
 *     },
 *     {
 *       "idComponente": "HDD001",
 *       "cantidad": 1,
 *       "descripcion": "Disco duro SSD 1TB",
 *       "precioBase": 2200.00
 *     }
 *   ],
 *   "observaciones": "Cotización especial para cliente corporativo",
 *   "fecha": "2025-01-17"
 * }
 * }</pre>
 *
 * <h3>Tipos de cotizador soportados:</h3>
 * <table border="1">
 *   <tr><th>Tipo</th><th>Descripción</th><th>Características</th></tr>
 *   <tr><td>"A"</td><td>Cotizador con listas paralelas</td><td>Simple, memoria eficiente, O(n) búsqueda</td></tr>
 *   <tr><td>"B"</td><td>Cotizador con Map</td><td>Rápido, O(1) acceso, más memoria</td></tr>
 * </table>
 *
 * <h3>Impuestos soportados:</h3>
 * <p>
 * La lista de impuestos especifica qué tipos de cálculo fiscal aplicar:
 * </p>
 * <ul>
 *   <li><strong>"IVA":</strong> Impuesto al Valor Agregado (16% en México)</li>
 *   <li><strong>"FEDERAL":</strong> Impuestos federales adicionales (5%)</li>
 *   <li><strong>"LOCAL":</strong> Impuestos locales/municipales (3%)</li>
 *   <li><strong>Lista vacía/null:</strong> Sin impuestos aplicados</li>
 * </ul>
 *
 * <h3>Validaciones implementadas:</h3>
 * <p>
 * La clase incluye múltiples niveles de validación:
 * </p>
 * <ul>
 *   <li><strong>@NotBlank:</strong> Campos de texto requeridos no pueden estar vacíos</li>
 *   <li><strong>@NotNull:</strong> Campos obligatorios no pueden ser null</li>
 *   <li><strong>@NotEmpty:</strong> La lista de detalles debe tener al menos un elemento</li>
 *   <li><strong>@Valid:</strong> Valida recursivamente cada DetalleCotizacionRequest</li>
 *   <li><strong>Mensajes personalizados:</strong> Mensajes de error descriptivos en español</li>
 * </ul>
 *
 * <h3>Ejemplo de uso en controlador:</h3>
 * <pre>{@code
 * @PostMapping("/cotizaciones")
 * public ResponseEntity<ApiResponse<CotizacionResponse>> crearCotizacion(
 *         @Valid @RequestBody CotizacionCreateRequest request) {
 *
 *     try {
 *         // Determinar el tipo de cotizador
 *         ICotizador cotizador = switch (request.getTipoCotizador()) {
 *             case "A" -> new Cotizador(crearCalculadorImpuestos(request.getImpuestos()));
 *             case "B" -> new CotizadorConMap();
 *             default -> throw new IllegalArgumentException("Tipo de cotizador no válido");
 *         };
 *
 *         // Agregar componentes al cotizador
 *         for (DetalleCotizacionRequest detalle : request.getDetalles()) {
 *             Componente componente = componenteService.obtenerPorId(detalle.getIdComponente());
 *             cotizador.agregarComponente(detalle.getCantidad(), componente);
 *         }
 *
 *         // Generar cotización
 *         Cotizacion cotizacion = cotizador.generarCotizacion(
 *             crearListaCalculadores(request.getImpuestos())
 *         );
 *
 *         // Convertir a response
 *         CotizacionResponse response = CotizacionMapper.toResponse(cotizacion);
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
 * <h3>Consideraciones importantes:</h3>
 * <ul>
 *   <li><strong>Fecha opcional:</strong> Si no se proporciona fecha, se usa la fecha actual del sistema</li>
 *   <li><strong>Observaciones opcionales:</strong> Campo de texto libre para notas adicionales</li>
 *   <li><strong>Validación de componentes:</strong> Los IDs de componentes deben existir en el sistema</li>
 *   <li><strong>Integridad referencial:</strong> Los componentes referenciados deben estar disponibles</li>
 *   <li><strong>Transaccionalidad:</strong> La creación de cotización debe ser atómica</li>
 * </ul>
 *
 * <h3>Campos opcionales vs requeridos:</h3>
 * <table border="1">
 *   <tr><th>Campo</th><th>Requerido</th><th>Validación</th><th>Notas</th></tr>
 *   <tr><td>tipoCotizador</td><td>✅ Sí</td><td>@NotBlank</td><td>"A" o "B"</td></tr>
 *   <tr><td>impuestos</td><td>❌ No</td><td>Ninguna</td><td>Lista puede estar vacía</td></tr>
 *   <tr><td>detalles</td><td>✅ Sí</td><td>@NotNull, @NotEmpty, @Valid</td><td>Mínimo 1 elemento</td></tr>
 *   <tr><td>observaciones</td><td>❌ No</td><td>Ninguna</td><td>Texto libre opcional</td></tr>
 *   <tr><td>fecha</td><td>❌ No</td><td>Ninguna</td><td>Formato yyyy-MM-dd</td></tr>
 * </table>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see CotizacionResponse
 * @see DetalleCotizacionRequest
 * @see CotizacionMapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionCreateRequest {

    /**
     * Tipo de algoritmo de cotización a utilizar.
     * <p>
     * Especifica qué implementación del patrón Strategy utilizar para generar la cotización.
     * Cada tipo tiene diferentes características de rendimiento y uso de memoria.
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotBlank - Campo requerido
     * </p>
     * <p>
     * <strong>Valores válidos:</strong>
     * <ul>
     *   <li>"A" - Cotizador con listas paralelas (más simple, menos memoria)</li>
     *   <li>"B" - Cotizador con Map (más rápido, más memoria)</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El tipo de cotizador es requerido")
    private String tipoCotizador;

    /**
     * Lista de tipos de impuestos a aplicar sobre la cotización.
     * <p>
     * Especifica qué calculadores de impuestos aplicar secuencialmente sobre el total
     * de la cotización. Los impuestos se aplican en el orden especificado.
     * </p>
     * <p>
     * <strong>Campo opcional:</strong> Si es null o vacío, no se aplican impuestos
     * </p>
     * <p>
     * <strong>Valores válidos:</strong>
     * <ul>
     *   <li>"IVA" - Impuesto al Valor Agregado</li>
     *   <li>"FEDERAL" - Impuestos federales adicionales</li>
     *   <li>"LOCAL" - Impuestos locales/municipales</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplo:</strong> ["IVA", "FEDERAL"] aplicaría primero IVA luego impuestos federales
     * </p>
     */
    private List<String> impuestos;

    /**
     * Lista de componentes y cantidades que conforman la cotización.
     * <p>
     * Colección de detalles que especifican qué componentes incluir en la cotización,
     * con sus respectivas cantidades. Cada detalle representa un componente específico
     * que el cliente desea adquirir.
     * </p>
     * <p>
     * <strong>Validaciones:</strong>
     * <ul>
     *   <li>@NotNull - La lista no puede ser null</li>
     *   <li>@NotEmpty - Debe contener al menos un elemento</li>
     *   <li>@Valid - Valida cada DetalleCotizacionRequest individualmente</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Estructura de cada detalle:</strong>
     * <ul>
     *   <li>idComponente - ID del componente en el catálogo</li>
     *   <li>cantidad - Número de unidades del componente</li>
     *   <li>descripcion - Descripción opcional (puede obtenerse del catálogo)</li>
     *   <li>precioBase - Precio unitario opcional (puede obtenerse del catálogo)</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La cotización debe tener al menos un detalle")
    @Valid
    private List<DetalleCotizacionRequest> detalles;

    /**
     * Observaciones o notas adicionales sobre la cotización.
     * <p>
     * Campo de texto libre que permite agregar información adicional relevante
     * para la cotización, como condiciones especiales, requisitos del cliente,
     * o cualquier nota que ayude al procesamiento o entrega.
     * </p>
     * <p>
     * <strong>Campo opcional:</strong> Puede ser null o vacío
     * </p>
     * <p>
     * <strong>Ejemplos de uso:</strong>
     * <ul>
     *   <li>"Cliente requiere entrega urgente"</li>
     *   <li>"Cotización especial para proyecto corporativo"</li>
     *   <li>"Incluir instalación y configuración"</li>
     * </ul>
     * </p>
     */
    private String observaciones;

    /**
     * Fecha de emisión de la cotización.
     * <p>
     * Fecha en la que se genera la cotización. Si no se proporciona,
     * el sistema utilizará automáticamente la fecha actual.
     * </p>
     * <p>
     * <strong>Campo opcional:</strong> Si es null o vacío, se usa fecha actual
     * </p>
     * <p>
     * <strong>Formato esperado:</strong> yyyy-MM-dd (ej: "2025-01-17")
     * </p>
     * <p>
     * <strong>Consideraciones:</strong>
     * <ul>
     *   <li>Debe ser una fecha válida</li>
     *   <li>Generalmente no se permite fechas futuras</li>
     *   <li>Se utiliza para cálculos de vigencia de precios</li>
     * </ul>
     * </p>
     */
    private String fecha;
} 