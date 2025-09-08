package mx.com.qtx.cotizador.dto.componente.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la solicitud de actualización de componentes en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase representa la estructura de datos que el cliente debe enviar a la API REST
 * cuando desea actualizar un componente existente en el sistema. A diferencia de la creación,
 * esta clase no incluye el campo ID ya que se envía como parámetro en la URL del endpoint.
 * Incluye todas las propiedades modificables del componente con validaciones exhaustivas.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Actualizar componentes:</strong> Permite modificar las propiedades de componentes existentes</li>
 *   <li><strong>ID separado:</strong> El identificador viene en la URL, no en el body del request</li>
 *   <li><strong>Validación de datos:</strong> Incluye anotaciones de validación para garantizar integridad</li>
 *   <li><strong>Campos opcionales:</strong> Todos los campos pueden actualizarse individualmente</li>
 *   <li><strong>Integración con API:</strong> Diseñado específicamente para endpoints PUT/PATCH</li>
 * </ul>
 *
 * <h3>Diferencias con ComponenteCreateRequest:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>ComponenteCreateRequest</th><th>ComponenteUpdateRequest</th></tr>
 *   <tr><td>ID del componente</td><td>Incluido en el body</td><td>No incluido (viene en URL)</td></tr>
 *   <tr><td>Uso típico</td><td>POST /componentes</td><td>PUT /componentes/{id}</td></tr>
 *   <tr><td>Todos los campos</td><td>Siempre requeridos</td><td>Pueden ser opcionales</td></tr>
 *   <tr><td>Validaciones</td><td>Estrictas para creación</td><td>Estrictas para actualización</td></tr>
 * </table>
 *
 * <h3>Estructura JSON esperada:</h3>
 * <pre>{@code
 * PUT /componentes/MON001
 * {
 *   "descripcion": "Monitor LED 27 pulgadas 4K UHD actualizado",
 *   "marca": "Samsung",
 *   "modelo": "LU27R650",
 *   "costo": 2800.00,
 *   "precioBase": 3600.00,
 *   "tipoComponente": "MONITOR",
 *   "capacidadAlm": null,
 *   "memoria": null
 * }
 * }</pre>
 *
 * <h3>Campos modificables:</h3>
 * <p>
 * Todos los campos del componente pueden ser actualizados excepto el ID, que es inmutable:
 * </p>
 * <ul>
 *   <li>✅ descripcion - Nueva descripción del producto</li>
 *   <li>✅ marca - Cambio de marca/fabricante</li>
 *   <li>✅ modelo - Nuevo número de modelo</li>
 *   <li>✅ costo - Actualización del costo de adquisición</li>
 *   <li>✅ precioBase - Modificación del precio de venta</li>
 *   <li>✅ tipoComponente - Cambio del tipo de componente</li>
 *   <li>✅ capacidadAlm - Actualización de capacidad (para discos duros)</li>
 *   <li>✅ memoria - Actualización de memoria (para tarjetas de video)</li>
 *   <li>❌ id - No se puede modificar (es parte de la URL)</li>
 * </ul>
 *
 * <h3>Validaciones implementadas:</h3>
 * <p>
 * La clase mantiene las mismas validaciones estrictas que la creación:
 * </p>
 * <ul>
 *   <li><strong>Validación de presencia:</strong> Campos requeridos marcados con @NotBlank/@NotNull</li>
 *   <li><strong>Validación de longitud:</strong> Límites de caracteres con @Size</li>
 *   <li><strong>Validación numérica:</strong> Valores positivos con @DecimalMin</li>
 *   <li><strong>Mensajes personalizados:</strong> Mensajes de error descriptivos en español</li>
 * </ul>
 *
 * <h3>Ejemplo de uso en controlador:</h3>
 * <pre>{@code
 * @PutMapping("/componentes/{id}")
 * public ResponseEntity<ApiResponse<ComponenteResponse>> actualizarComponente(
 *         @PathVariable String id,
 *         @Valid @RequestBody ComponenteUpdateRequest request) {
 *
 *     try {
 *         // El ID viene de la URL, los datos del body
 *         Componente componente = ComponenteMapper.toComponente(id, request);
 *
 *         // Actualizar en el repositorio
 *         Componente actualizado = servicio.actualizar(id, componente);
 *
 *         // Convertir a response
 *         ComponenteResponse response = ComponenteMapper.toResponse(actualizado);
 *
 *         return ResponseEntity.ok(new ApiResponse<>("0", "Componente actualizado", response));
 *
 *     } catch (ComponenteNoEncontradoException e) {
 *         return ResponseEntity.notFound().build();
 *     }
 * }
 * }</pre>
 *
 * <h3>Consideraciones de actualización:</h3>
 * <ul>
 *   <li><strong>Campos opcionales:</strong> Aunque todos tienen validaciones @NotBlank/@NotNull,
 *       en una implementación PATCH podrían ser opcionales</li>
 *   <li><strong>ID inmutable:</strong> El identificador nunca cambia durante la actualización</li>
 *   <li><strong>Validación completa:</strong> Se validan todos los campos como si fueran nuevos</li>
 *   <li><strong>Transaccionalidad:</strong> La actualización debe ser atómica</li>
 *   <li><strong>Auditoría:</strong> Considerar registrar cambios para trazabilidad</li>
 * </ul>
 *
 * <h3>Manejo de campos específicos por tipo:</h3>
 * <p>
 * Al actualizar el tipoComponente, los campos específicos deben manejarse correctamente:
 * </p>
 * <ul>
 *   <li><strong>Cambio a MONITOR:</strong> capacidadAlm y memoria deben ser null</li>
 *   <li><strong>Cambio a DISCO_DURO:</strong> memoria debe ser null, capacidadAlm requerida</li>
 *   <li><strong>Cambio a TARJETA_VIDEO:</strong> capacidadAlm debe ser null, memoria requerida</li>
 *   <li><strong>Cambio a PC:</strong> Ambos campos específicos deben ser null</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see ComponenteCreateRequest
 * @see ComponenteResponse
 * @see ComponenteMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteUpdateRequest {
    
    /**
     * Nueva descripción detallada del componente.
     * <p>
     * Campo utilizado para actualizar la descripción completa del componente.
     * Debe incluir todas las características técnicas y comerciales relevantes.
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotBlank, @Size(max=200)
     * </p>
     */
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    /**
     * Nueva marca o fabricante del componente.
     * <p>
     * Permite cambiar la marca comercial del componente. Todos los valores
     * permitidos en creación también son válidos en actualización.
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotBlank, @Size(max=50)
     * </p>
     */
    @NotBlank(message = "La marca es requerida")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;

    /**
     * Nuevo modelo específico del componente.
     * <p>
     * Permite actualizar el número de modelo o referencia del fabricante.
     * Es utilizado junto con la marca para identificar el producto específico.
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotBlank, @Size(max=50)
     * </p>
     */
    @NotBlank(message = "El modelo es requerido")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    /**
     * Nuevo costo de adquisición del componente.
     * <p>
     * Actualización del costo real de compra del componente. Este campo
     * afecta los cálculos de rentabilidad internos del sistema.
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotNull, @DecimalMin > 0
     * </p>
     */
    @NotNull(message = "El costo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
    private BigDecimal costo;

    /**
     * Nuevo precio base de venta del componente.
     * <p>
     * Actualización del precio de venta al público antes de aplicar promociones.
     * Este es el precio que ven los clientes en las cotizaciones.
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotNull, @DecimalMin > 0
     * </p>
     */
    @NotNull(message = "El precio base es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    /**
     * Nuevo tipo de componente.
     * <p>
     * Permite cambiar el tipo de componente. Al cambiar el tipo, se deben
     * actualizar también los campos específicos correspondientes.
     * </p>
     * <p>
     * <strong>Valores válidos:</strong> "MONITOR", "DISCO_DURO", "TARJETA_VIDEO", "PC"
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @NotBlank
     * </p>
     */
    @NotBlank(message = "El tipo de componente es requerido")
    private String tipoComponente; // MONITOR, DISCO_DURO, TARJETA_VIDEO, PC

    /**
     * Nueva capacidad de almacenamiento (solo para discos duros).
     * <p>
     * Campo específico para actualizar la capacidad de almacenamiento
     * cuando el tipoComponente es "DISCO_DURO".
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @Size(max=20)
     * <br><strong>Nota:</strong> Debe ser null para otros tipos de componente
     * </p>
     */
    // Campos específicos para disco duro
    @Size(max = 20, message = "La capacidad de almacenamiento no puede exceder 20 caracteres")
    private String capacidadAlm;

    /**
     * Nueva memoria de video (solo para tarjetas de video).
     * <p>
     * Campo específico para actualizar la memoria VRAM cuando el
     * tipoComponente es "TARJETA_VIDEO".
     * </p>
     * <p>
     * <strong>Validaciones:</strong> @Size(max=20)
     * <br><strong>Nota:</strong> Debe ser null para otros tipos de componente
     * </p>
     */
    // Campos específicos para tarjeta de video
    @Size(max = 20, message = "La memoria no puede exceder 20 caracteres")
    private String memoria;
} 