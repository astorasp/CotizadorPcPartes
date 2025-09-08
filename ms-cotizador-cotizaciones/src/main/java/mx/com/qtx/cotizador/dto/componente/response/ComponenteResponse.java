package mx.com.qtx.cotizador.dto.componente.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) de respuesta para componentes en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase representa la estructura de datos que se envía al cliente de la API REST
 * cuando se solicita información de componentes. Incluye todos los datos relevantes
 * del componente de manera estructurada y serializable a JSON, incluyendo información
 * adicional sobre promociones activas cuando corresponde.
 * </p>
 *
 * <h3>Propósito principal:</h3>
 * <ul>
 *   <li><strong>Respuesta de API:</strong> Formato estándar para respuestas de componentes</li>
 *   <li><strong>Información completa:</strong> Incluye todos los datos del componente</li>
 *   <li><strong>Campos específicos:</strong> Maneja atributos únicos por tipo de componente</li>
 *   <li><strong>Información promocional:</strong> Incluye datos de promociones activas</li>
 *   <li><strong>Serialización JSON:</strong> Optimizado para respuesta HTTP</li>
 * </ul>
 *
 * <h3>Estructura JSON de respuesta:</h3>
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
 *   "memoria": null,
 *   "promocionId": null,
 *   "promocionDescripcion": null
 * }
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see ComponenteCreateRequest
 * @see ComponenteUpdateRequest
 * @see ComponenteMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteResponse {

    /**
     * Identificador único del componente.
     * <p>
     * Código único que identifica al componente en todo el sistema.
     * Este ID es inmutable y se utiliza para todas las operaciones CRUD.
     * </p>
     */
    private String id;

    /**
     * Descripción completa del componente.
     * <p>
     * Descripción detallada que incluye especificaciones técnicas,
     * características y cualquier información relevante del producto.
     * </p>
     */
    private String descripcion;

    /**
     * Marca o fabricante del componente.
     * <p>
     * Nombre de la marca comercial o fabricante del componente.
     * Se utiliza para categorización y búsqueda de productos.
     * </p>
     */
    private String marca;

    /**
     * Modelo específico del componente.
     * <p>
     * Número de modelo o referencia específica del fabricante.
     * Se utiliza junto con la marca para identificar el producto exacto.
     * </p>
     */
    private String modelo;

    /**
     * Costo de adquisición del componente.
     * <p>
     * <strong>Nota de seguridad:</strong> Este campo contiene información
     * sensible sobre el costo interno. En producción, considerar ocultarlo
     * para usuarios no autorizados.
     * </p>
     */
    private BigDecimal costo;

    /**
     * Precio base de venta del componente.
     * <p>
     * Precio de venta al público antes de aplicar cualquier promoción
     * o descuento. Este es el precio que ven los clientes.
     * </p>
     */
    private BigDecimal precioBase;

    /**
     * Tipo de componente.
     * <p>
     * Clasificación del tipo de componente. Valores posibles:
     * "MONITOR", "DISCO_DURO", "TARJETA_VIDEO", "PC"
     * </p>
     */
    private String tipoComponente;

    /**
     * Capacidad de almacenamiento (solo para discos duros).
     * <p>
     * Especificación de capacidad para componentes tipo DISCO_DURO.
     * Para otros tipos de componente, este campo es null.
     * </p>
     * <p>
     * <strong>Ejemplos:</strong> "1TB", "500GB", "2TB SSD"
     * </p>
     */
    // Campos específicos para disco duro
    private String capacidadAlm;

    /**
     * Memoria de video (solo para tarjetas de video).
     * <p>
     * Especificación de memoria VRAM para componentes tipo TARJETA_VIDEO.
     * Para otros tipos de componente, este campo es null.
     * </p>
     * <p>
     * <strong>Ejemplos:</strong> "8GB", "12GB GDDR6", "16GB HBM2"
     * </p>
     */
    // Campos específicos para tarjeta de video
    private String memoria;

    /**
     * Identificador de la promoción activa (opcional).
     * <p>
     * ID de la promoción que se está aplicando actualmente a este componente.
     * Es null cuando no hay promoción activa.
     * </p>
     */
    // Información adicional de la promoción si existe
    private String promocionId;

    /**
     * Descripción de la promoción activa (opcional).
     * <p>
     * Descripción legible de la promoción que se está aplicando.
     * Es null cuando no hay promoción activa.
     * </p>
     * <p>
     * <strong>Ejemplo:</strong> "3x2 en monitores", "10% descuento en discos duros"
     * </p>
     */
    private String promocionDescripcion;
} 