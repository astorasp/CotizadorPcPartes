package mx.com.qtx.cotizador.entidad;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Embeddable;

/**
 * Entidad JPA que representa el detalle individual de una cotización en el sistema CotizadorPcPartes - Microservicio de Pedidos.
 * <p>
 * Esta entidad mapea la tabla <strong>codetalle_cotizacion</strong> y representa cada línea específica
 * de una cotización, conectando una cotización con un componente específico incluyendo cantidades,
 * precios base y descripciones. Sirve como puente entre cotizaciones y la generación de pedidos.
 * </p>
 *
 * <h3>Propósito Principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Referencia de cotizaciones:</strong> Almacena líneas de cotización para conversión a pedidos</li>
 *   <li><strong>Fuente de datos para pedidos:</strong> Proporciona información para generar DetallePedido</li>
 *   <li><strong>Histórico de precios:</strong> Mantiene precios base históricos de componentes</li>
 *   <li><strong>Validación de componentes:</strong> Verifica existencia de componentes en cotizaciones</li>
 *   <li><strong>Conversión automática:</strong> Base para transformación automática a pedidos</li>
 *   <li><strong>Integridad de datos:</strong> Garantiza consistencia entre cotizaciones y componentes</li>
 * </ul>
 *
 * <h3>Estructura Completa de la Tabla codetalle_cotizacion:</h3>
 * <table border="1">
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th><th>Constraints</th></tr>
 *   <tr><td>folio</td><td>INT</td><td>Parte de clave primaria (FK)</td><td>PRIMARY KEY, FOREIGN KEY</td></tr>
 *   <tr><td>num_detalle</td><td>INT</td><td>Parte de clave primaria</td><td>PRIMARY KEY, NOT NULL</td></tr>
 *   <tr><td>cantidad</td><td>INT</td><td>Cantidad solicitada del componente</td><td>NOT NULL</td></tr>
 *   <tr><td>descripcion</td><td>VARCHAR(255)</td><td>Descripción específica del componente</td><td>NOT NULL</td></tr>
 *   <tr><td>precio_base</td><td>DECIMAL(10,2)</td><td>Precio base del componente</td><td>NOT NULL</td></tr>
 *   <tr><td>id_componente</td><td>VARCHAR(50)</td><td>ID del componente en la cotización</td><td>FOREIGN KEY, NOT NULL</td></tr>
 * </table>
 *
 * <h3>Relaciones JPA Específicas:</h3>
 * <table border="1">
 *   <tr><th>Relación</th><th>Tipo</th><th>Entidad Relacionada</th><th>Cascade</th><th>Fetch</th></tr>
 *   <tr><td>@ManyToOne</td><td>Bidireccional</td><td>{@link Cotizacion}</td><td>Ninguno</td><td>EAGER</td></tr>
 *   <tr><td>@ManyToOne</td><td>Unidireccional</td><td>{@link Componente}</td><td>Ninguno</td><td>EAGER</td></tr>
 * </table>
 *
 * <h3>Llave Primaria Compuesta - DetalleCotizacionId:</h3>
 * <p>
 * La entidad utiliza una llave primaria compuesta que combina:
 * </p>
 * <ul>
 *   <li><strong>folio:</strong> Número de folio de la cotización (relación con Cotizacion)</li>
 *   <li><strong>numDetalle:</strong> Número secuencial dentro de la cotización</li>
 * </ul>
 * <p>
 * Esta estructura permite múltiples líneas por cotización y garantiza unicidad.
 * </p>
 *
 * <h3>Reglas de Negocio Específicas:</h3>
 * <ul>
 *   <li><strong>Cantidad positiva:</strong> cantidad debe ser mayor a cero</li>
 *   <li><strong>Precio válido:</strong> precio_base debe ser mayor a cero</li>
 *   <li><strong>Referencias válidas:</strong> folio y id_componente deben existir</li>
 *   <li><strong>Numeración secuencial:</strong> num_detalle debe ser único dentro de la cotización</li>
 *   <li><strong>Descripción requerida:</strong> descripcion no puede ser null o vacía</li>
 *   <li><strong>Consistencia con componentes:</strong> id_componente debe existir en el catálogo</li>
 * </ul>
 *
 * <h3>Proceso de Conversión a Pedidos:</h3>
 * <p>
 * Los detalles de cotización sirven como fuente para generar detalles de pedido:
 * </p>
 * <pre>{@code
 * 1. Cotizacion (con DetalleCotizacion)
 *    ↓ (Selección para pedido)
 * 2. GenerarPedidoRequest → Validación
 *    ↓ (Procesamiento)
 * 3. DetalleCotizacion → DetallePedido (conversión)
 *    ↓ (Transformación)
 * 4. precio_base → precio_unitario
 *    descripcion → descripcion (mantiene)
 *    cantidad → cantidad (mantiene)
 *    ↓ (Cálculo)
 * 5. total_cotizado = cantidad × precio_unitario
 * }</pre>
 *
 * <h3>Diferencias con DetallePedido:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>DetalleCotizacion</th><th>DetallePedido</th></tr>
 *   <tr><td>Precio</td><td>precio_base</td><td>precio_unitario</td></tr>
 *   <tr><td>Relación padre</td><td>Cotizacion (folio)</td><td>Pedido (num_pedido)</td></tr>
 *   <tr><td>Estado</td><td>Histórico/Referencial</td><td>Activo/Transaccional</td></tr>
 *   <tr><td>Cascade</td><td>Ninguno</td><td>ALL (DELETE)</td></tr>
 *   <tr><td>Propósito</td><td>Fuente de datos</td><td>Ejecución del pedido</td></tr>
 * </table>
 *
 * <h3>Casos de Uso Asociados:</h3>
 * <table border="1">
 *   <tr><th>CU</th><th>Descripción</th><th>Operación en DetalleCotizacion</th></tr>
 *   <tr><td>CU 5.1</td><td>Generar pedido desde cotización</td><td>READ para conversión</td></tr>
 *   <tr><td>CU 5.2</td><td>Consultar pedidos por proveedor</td><td>READ para trazabilidad</td></tr>
 *   <tr><td>CU 5.3</td><td>Consultar pedido específico</td><td>READ para comparación</td></tr>
 * </table>
 *
 * <h3>Consideraciones de Performance:</h3>
 * <ul>
 *   <li><strong>Index compuesto:</strong> En (folio, num_detalle) para consultas rápidas</li>
 *   <li><strong>Foreign keys indexadas:</strong> folio e id_componente con índices</li>
 *   <li><strong>Batch reads:</strong> Para conversiones masivas de cotización a pedido</li>
 *   <li><strong>Query optimization:</strong> Usar JOIN FETCH para consultas con componentes</li>
 *   <li><strong>Data archival:</strong> Considerar estrategias de archivado para cotizaciones antiguas</li>
 * </ul>
 *
 * <h3>Campos y su Utilidad Específica:</h3>
 * <ul>
 *   <li><strong>id (DetalleCotizacionId):</strong> Clave primaria compuesta para unicidad</li>
 *   <li><strong>cantidad:</strong> Cantidad solicitada en la cotización original</li>
 *   <li><strong>descripcion:</strong> Descripción específica del componente cotizado</li>
 *   <li><strong>precio_base:</strong> Precio base utilizado en la cotización</li>
 *   <li><strong>cotizacion:</strong> Referencia a la cotización padre (relación bidireccional)</li>
 *   <li><strong>componente:</strong> Referencia al componente cotizado</li>
 * </ul>
 *
 * <h3>Integración con Microservicios:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Fuente principal de datos para esta entidad</li>
 *   <li><strong>ms-cotizador-componentes:</strong> Valida existencia de componentes referenciados</li>
 *   <li><strong>Base de datos compartida:</strong> Acceso directo a datos de cotizaciones</li>
 *   <li><strong>Sincronización:</strong> Datos se actualizan vía eventos Kafka desde cotizaciones</li>
 * </ul>
 *
 * <h3>Ejemplo de Conversión a Pedido:</h3>
 * <pre>{@code
 * @Service
 * public class PedidoService {
 *
 *     public DetallePedido convertirDetalleCotizacion(DetalleCotizacion detalleCot,
 *                                                    Pedido pedidoPadre) {
 *
 *         DetallePedido detallePed = new DetallePedido();
 *         detallePed.setId(new DetallePedidoId(pedidoPadre.getNumPedido(),
 *                                             obtenerSiguienteNumDetalle(pedidoPadre)));
 *         detallePed.setPedido(pedidoPadre);
 *         detallePed.setComponente(detalleCot.getComponente());
 *         detallePed.setCantidad(detalleCot.getCantidad());
 *         detallePed.setPrecioUnitario(detalleCot.getPrecioBase()); // Conversión de precio
 *
 *         // Cálculo del total
 *         BigDecimal totalLinea = detalleCot.getPrecioBase()
 *                                 .multiply(BigDecimal.valueOf(detalleCot.getCantidad()));
 *         detallePed.setTotalCotizado(totalLinea);
 *
 *         return detallePed;
 *     }
 * }
 * }</pre>
 *
 * @see Cotizacion Entidad padre que contiene la colección de detalles
 * @see Componente Entidad del componente cotizado
 * @see mx.com.qtx.cotizador.entidad.DetalleCotizacion.DetalleCotizacionId Clase interna para llave primaria compuesta
 * @see DetallePedido Entidad resultante de la conversión de pedidos
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "codetalle_cotizacion")
public class DetalleCotizacion implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Llave primaria compuesta
    @EmbeddedId
    private DetalleCotizacionId id = new DetalleCotizacionId();
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "precio_base")
    private BigDecimal precioBase;
    
    @ManyToOne
    @MapsId("folio") // Este campo se mapea a partir de la clave embebida
    @JoinColumn(name = "folio")
    private Cotizacion cotizacion;
    
    @ManyToOne
    @JoinColumn(name = "id_componente")
    private Componente componente;
    
    // Constructores
    public DetalleCotizacion() {
        // Constructor vacío necesario para JPA
    }
    
    // Getters y setters
    public DetalleCotizacionId getId() {
        return id;
    }
    
    public void setId(DetalleCotizacionId id) {
        this.id = id;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getPrecioBase() {
        return precioBase;
    }
    
    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }
    
    public Cotizacion getCotizacion() {
        return cotizacion;
    }
    
    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }
    
    public Componente getComponente() {
        return componente;
    }
    
    public void setComponente(Componente componente) {
        this.componente = componente;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleCotizacion that = (DetalleCotizacion) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Clase embebida que representa la llave primaria compuesta de DetalleCotizacion.
     * <p>
     * Esta clase interna define la estructura de la llave primaria compuesta que identifica
     * de manera única cada línea de detalle dentro de una cotización específica. Combina el
     * folio de la cotización con un número secuencial de detalle dentro de esa cotización.
     * </p>
     *
     * <h3>Estructura de la Llave Compuesta:</h3>
     * <table border="1">
     *   <tr><th>Campo</th><th>Tipo</th><th>Descripción</th><th>Ejemplo</th></tr>
     *   <tr><td>folio</td><td>Integer</td><td>Folio de la cotización padre</td><td>12345</td></tr>
     *   <tr><td>numDetalle</td><td>Integer</td><td>Número secuencial en la cotización</td><td>1, 2, 3...</td></tr>
     * </table>
     *
     * <h3>Reglas de Generación:</h3>
     * <ul>
     *   <li><strong>folio:</strong> Debe coincidir con una cotización existente</li>
     *   <li><strong>numDetalle:</strong> Debe ser único dentro de la misma cotización</li>
     *   <li><strong>Secuencialidad:</strong> Generalmente 1, 2, 3... sin saltos</li>
     *   <li><strong>Consistencia:</strong> No puede haber números duplicados en la misma cotización</li>
     * </ul>
     *
     * <h3>Ejemplo de Uso:</h3>
     * <pre>{@code
     * // Para la cotización folio 12345, línea 1:
     * DetalleCotizacionId id1 = new DetalleCotizacionId(12345, 1);
     *
     * // Para la cotización folio 12345, línea 2:
     * DetalleCotizacionId id2 = new DetalleCotizacionId(12345, 2);
     *
     * // Para la cotización folio 67890, línea 1:
     * DetalleCotizacionId id3 = new DetalleCotizacionId(67890, 1);
     * }</pre>
     *
     * <h3>Mapeo a Columnas de Base de Datos:</h3>
     * <ul>
     *   <li><strong>folio → folio:</strong> Foreign key a la cotización padre</li>
     *   <li><strong>numDetalle → num_detalle:</strong> Número secuencial en la cotización</li>
     * </ul>
     *
     * @see DetalleCotizacion Entidad padre que contiene esta llave
     * @see Cotizacion Entidad relacionada por folio
     * @author Subagente3F - [2025-01-17 19:30:00 MST]
     * @version 1.0.0
     * @since 1.0.0
     */
    @Embeddable
    public static class DetalleCotizacionId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "folio")
        private Integer folio;
        
        @Column(name = "num_detalle")
        private Integer numDetalle;
        
        // Constructores
        public DetalleCotizacionId() {}
        
        public DetalleCotizacionId(Integer folio, Integer numDetalle) {
            this.folio = folio;
            this.numDetalle = numDetalle;
        }
        
        // Getters y setters
        public Integer getFolio() {
            return folio;
        }
        
        public void setFolio(Integer folio) {
            this.folio = folio;
        }
        
        public Integer getNumDetalle() {
            return numDetalle;
        }
        
        public void setNumDetalle(Integer numDetalle) {
            this.numDetalle = numDetalle;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetalleCotizacionId that = (DetalleCotizacionId) o;
            return Objects.equals(folio, that.folio) &&
                   Objects.equals(numDetalle, that.numDetalle);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(folio, numDetalle);
        }
    }
}
