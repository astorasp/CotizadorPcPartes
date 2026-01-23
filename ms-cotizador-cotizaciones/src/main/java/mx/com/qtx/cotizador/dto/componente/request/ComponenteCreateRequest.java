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
 * DTO (Data Transfer Object) para la solicitud de creación de componentes en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase representa la estructura de datos que el cliente debe enviar a la API REST
 * cuando desea crear un nuevo componente en el sistema. Incluye toda la información necesaria
 * para crear diferentes tipos de componentes (monitores, discos duros, tarjetas de video, etc.)
 * con validaciones exhaustivas para garantizar la integridad de los datos.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Crear componentes:</strong> Proporciona todos los campos necesarios para crear un componente</li>
 *   <li><strong>Validación de datos:</strong> Incluye anotaciones de validación de Bean Validation</li>
 *   <li><strong>Flexibilidad de tipos:</strong> Soporta diferentes tipos de componentes polimórficos</li>
 *   <li><strong>Campos opcionales:</strong> Algunos campos son específicos para ciertos tipos de componentes</li>
 *   <li><strong>Integración con API:</strong> Diseñado específicamente para endpoints REST</li>
 * </ul>
 *
 * <h3>Estructura JSON esperada:</h3>
 * <pre>{@code
 * {
 *   "id": "MON001",
 *   "descripcion": "Monitor LED 24 pulgadas Full HD",
 *   "marca": "Samsung",
 *   "modelo": "LU24R650",
 *   "costo": 2500.00,
 *   "precioBase": 3200.00,
 *   "tipoComponente": "MONITOR",
 *   "capacidadAlm": null,
 *   "memoria": null
 * }
 * }</pre>
 *
 * <h3>Tipos de componentes soportados:</h3>
 * <table border="1">
 *   <tr><th>Tipo</th><th>Valor tipoComponente</th><th>Campos específicos requeridos</th></tr>
 *   <tr><td>Monitor</td><td>"MONITOR"</td><td>Ninguno adicional</td></tr>
 *   <tr><td>Disco Duro</td><td>"DISCO_DURO"</td><td>capacidadAlm</td></tr>
 *   <tr><td>Tarjeta de Video</td><td>"TARJETA_VIDEO"</td><td>memoria</td></tr>
 *   <tr><td>PC Completa</td><td>"PC"</td><td>Ninguno adicional</td></tr>
 * </table>
 *
 * <h3>Validaciones implementadas:</h3>
 * <p>
 * La clase incluye múltiples niveles de validación:
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
 * @PostMapping("/componentes")
 * public ResponseEntity<ApiResponse<ComponenteResponse>> crearComponente(
 *         @Valid @RequestBody ComponenteCreateRequest request) {
 *
 *     // Validación automática por Bean Validation
 *     // Si la validación falla, se lanza MethodArgumentNotValidException
 *
 *     Componente componente = ComponenteMapper.toComponente(request);
 *     Componente guardado = servicio.guardar(componente);
 *     ComponenteResponse response = ComponenteMapper.toResponse(guardado);
 *
 *     return ResponseEntity.ok(new ApiResponse<>("0", "Componente creado", response));
 * }
 * }</pre>
 *
 * <h3>Consideraciones de diseño:</h3>
 * <ul>
 *   <li><strong>Lombok:</strong> Usa @Data para generar getters/setters automáticamente</li>
 *   <li><strong>Builder Pattern:</strong> Anotación @Builder permite construcción fluida</li>
 *   <li><strong>Constructores múltiples:</strong> Constructor vacío y completo disponibles</li>
 *   <li><strong>Campos opcionales:</strong> capacidadAlm y memoria son null para tipos que no los usan</li>
 *   <li><strong>Precisión decimal:</strong> Usa BigDecimal para precios y costos</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see ComponenteUpdateRequest
 * @see ComponenteResponse
 * @see ComponenteMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteCreateRequest {
    
    /**
     * Identificador único del componente dentro del sistema.
     * <p>
     * Este campo representa el código único que identifica al componente en todo el sistema.
     * Debe ser único y generalmente sigue un patrón específico según el tipo de componente
     * (ej: "MON001" para monitores, "HDD001" para discos duros, etc.).
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotBlank: No puede ser null, vacío o solo espacios</li>
     *   <li>@Size(max=10): Máximo 10 caracteres para mantener consistencia</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos de IDs válidos:</strong>
     * <ul>
     *   <li>"MON001", "MON002" - Para monitores</li>
     *   <li>"HDD001", "SSD002" - Para discos duros</li>
     *   <li>"GPU001", "RTX002" - Para tarjetas de video</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El ID del componente es requerido")
    @Size(max = 10, message = "El ID no puede exceder 10 caracteres")
    private String id;

    /**
     * Descripción detallada del componente.
     * <p>
     * Este campo contiene una descripción completa y descriptiva del componente,
     * incluyendo sus características principales, especificaciones técnicas y
     * cualquier información relevante que ayude a identificar el producto.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotBlank: No puede ser null, vacío o solo espacios</li>
     *   <li>@Size(max=200): Máximo 200 caracteres para descripciones completas</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos de descripciones:</strong>
     * <ul>
     *   <li>"Monitor LED 24 pulgadas Full HD con panel IPS"</li>
     *   <li>"Disco duro SSD 1TB NVMe PCIe 4.0 de alta velocidad"</li>
     *   <li>"Tarjeta de video NVIDIA RTX 4070 con 12GB GDDR6X"</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    /**
     * Marca o fabricante del componente.
     * <p>
     * Este campo especifica la marca comercial o fabricante del componente.
     * Es utilizado para categorización, búsqueda y para mantener consistencia
     * en el catálogo de productos.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotBlank: No puede ser null, vacío o solo espacios</li>
     *   <li>@Size(max=50): Máximo 50 caracteres para nombres de marca</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos de marcas válidas:</strong>
     * <ul>
     *   <li>"Samsung", "LG", "Dell" - Para monitores</li>
     *   <li>"Western Digital", "Seagate", "Samsung" - Para discos duros</li>
     *   <li>"NVIDIA", "AMD", "ASUS" - Para tarjetas de video</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "La marca es requerida")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;

    /**
     * Modelo específico del componente.
     * <p>
     * Este campo contiene el número de modelo o referencia específica del fabricante
     * para este componente. Es utilizado junto con la marca para identificar
     * unívocamente el producto específico.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotBlank: No puede ser null, vacío o solo espacios</li>
     *   <li>@Size(max=50): Máximo 50 caracteres para códigos de modelo</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Ejemplos de modelos:</strong>
     * <ul>
     *   <li>"LU24R650", "U2419HX" - Modelos de monitores</li>
     *   <li>"WD1002FAEX", "ST1000DM010" - Modelos de discos duros</li>
     *   <li>"RTX4070", "RX7800XT" - Modelos de tarjetas de video</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El modelo es requerido")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    /**
     * Costo de adquisición del componente.
     * <p>
     * Este campo representa el costo real de compra o adquisición del componente
     * para el negocio. Es utilizado para calcular márgenes de ganancia y para
     * análisis de rentabilidad, pero no se expone directamente al cliente final.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotNull: No puede ser null</li>
     *   <li>@DecimalMin: Debe ser mayor a 0.0 (valores positivos)</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Consideraciones:</strong>
     * <ul>
     *   <li>Usa BigDecimal para precisión decimal exacta</li>
     *   <li>Es diferente al precioBase que ve el cliente</li>
     *   <li>Se utiliza para cálculos internos de rentabilidad</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "El costo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
    private BigDecimal costo;

    /**
     * Precio base de venta del componente.
     * <p>
     * Este campo representa el precio base de venta al público del componente
     * antes de aplicar cualquier promoción o descuento. Es el precio que ve
     * el cliente y sobre el cual se calculan las cotizaciones.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotNull: No puede ser null</li>
     *   <li>@DecimalMin: Debe ser mayor a 0.0 (valores positivos)</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Relación con costo:</strong>
     * <ul>
     *   <li>precioBase > costo (para tener ganancia)</li>
     *   <li>precioBase es el precio sin promociones aplicadas</li>
     *   <li>Sirve como base para calcular descuentos y promociones</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "El precio base es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    /**
     * Tipo de componente que se está creando.
     * <p>
     * Este campo determina el tipo específico de componente y afecta qué campos
     * adicionales son requeridos. Define el comportamiento del componente en el
     * sistema de cotización y qué características específicas puede tener.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@NotBlank: No puede ser null, vacío o solo espacios</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Valores válidos:</strong>
     * <ul>
     *   <li>"MONITOR" - Para monitores y pantallas</li>
     *   <li>"DISCO_DURO" - Para discos duros y SSDs</li>
     *   <li>"TARJETA_VIDEO" - Para tarjetas de video/GPUs</li>
     *   <li>"PC" - Para computadoras completas</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Campos adicionales por tipo:</strong>
     * <ul>
     *   <li>MONITOR: No requiere campos adicionales</li>
     *   <li>DISCO_DURO: Requiere capacidadAlm</li>
     *   <li>TARJETA_VIDEO: Requiere memoria</li>
     *   <li>PC: No requiere campos adicionales</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El tipo de componente es requerido")
    private String tipoComponente; // MONITOR, DISCO_DURO, TARJETA_VIDEO, PC

    /**
     * Capacidad de almacenamiento (solo para discos duros).
     * <p>
     * Este campo es específico para componentes de tipo DISCO_DURO y especifica
     * la capacidad de almacenamiento del dispositivo. Para otros tipos de componente,
     * este campo debe ser null o vacío.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@Size(max=20): Máximo 20 caracteres para especificaciones</li>
     *   <li>No tiene @NotBlank porque es opcional según el tipo</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Formatos comunes:</strong>
     * <ul>
     *   <li>"1TB", "500GB", "2TB" - Para HDD tradicionales</li>
     *   <li>"1TB NVMe", "500GB SATA" - Especificando interfaz</li>
     *   <li>"2TB SSD PCIe 4.0" - Con detalles técnicos</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Nota:</strong> Este campo solo es relevante cuando tipoComponente es "DISCO_DURO".
     * Para otros tipos, debe dejarse como null.
     * </p>
     */
    // Campos específicos para disco duro
    @Size(max = 20, message = "La capacidad de almacenamiento no puede exceder 20 caracteres")
    private String capacidadAlm;

    /**
     * Memoria de video (solo para tarjetas de video).
     * <p>
     * Este campo es específico para componentes de tipo TARJETA_VIDEO y especifica
     * la cantidad de memoria VRAM (Video Random Access Memory) del dispositivo.
     * Para otros tipos de componente, este campo debe ser null o vacío.
     * </p>
     * <p>
     * <strong>Validaciones aplicadas:</strong>
     * <ul>
     *   <li>@Size(max=20): Máximo 20 caracteres para especificaciones</li>
     *   <li>No tiene @NotBlank porque es opcional según el tipo</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Formatos comunes:</strong>
     * <ul>
     *   <li>"8GB", "12GB", "16GB" - Cantidad de memoria</li>
     *   <li>"8GB GDDR6", "12GB GDDR6X" - Con tipo de memoria</li>
     *   <li>"16GB HBM2" - Tecnologías específicas</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Nota:</strong> Este campo solo es relevante cuando tipoComponente es "TARJETA_VIDEO".
     * Para otros tipos, debe dejarse como null.
     * </p>
     */
    // Campos específicos para tarjeta de video
    @Size(max = 20, message = "La memoria no puede exceder 20 caracteres")
    private String memoria;
} 