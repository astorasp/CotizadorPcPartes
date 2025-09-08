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
 * Entidad JPA que representa el detalle individual de un pedido en el sistema CotizadorPcPartes - Microservicio de Pedidos.
 * <p>
 * Esta entidad mapea la tabla <strong>codetalle_pedido</strong> y representa cada línea específica
 * de un pedido, conectando un pedido con un componente específico incluyendo cantidades,
 * precios y cálculos totales. Es una entidad crítica para el procesamiento de pedidos.
 * </p>
 *
 * <h3>Propósito Principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Líneas de pedido:</strong> Representa cada componente individual solicitado en un pedido</li>
 *   <li><strong>Cálculo de totales:</strong> Contiene la lógica de cálculo por línea de pedido</li>
 *   <li><strong>Relación pedido-componente:</strong> Puente entre pedidos y componentes del catálogo</li>
 *   <li><strong>Seguimiento de cantidades:</strong> Controla las cantidades específicas por componente</li>
 *   <li><strong>Cálculo financiero:</strong> Mantiene precios unitarios y totales por línea</li>
 *   <li><strong>Integridad referencial:</strong> Garantiza consistencia entre pedidos y componentes</li>
 * </ul>
 *
 * <h3>Estructura Completa de la Tabla codetalle_pedido:</h3>
 * <table border="1">
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th><th>Constraints</th></tr>
 *   <tr><td>num_pedido</td><td>INT</td><td>Parte de clave primaria (FK)</td><td>PRIMARY KEY, FOREIGN KEY</td></tr>
 *   <tr><td>num_detalle</td><td>INT</td><td>Parte de clave primaria</td><td>PRIMARY KEY, NOT NULL</td></tr>
 *   <tr><td>cantidad</td><td>INT</td><td>Cantidad solicitada del componente</td><td>NOT NULL</td></tr>
 *   <tr><td>precio_unitario</td><td>DECIMAL(10,2)</td><td>Precio unitario del componente</td><td>NOT NULL</td></tr>
 *   <tr><td>total_cotizado</td><td>DECIMAL(10,2)</td><td>Total de esta línea (cantidad × precio)</td><td>NOT NULL</td></tr>
 *   <tr><td>id_componente</td><td>VARCHAR(50)</td><td>ID del componente solicitado</td><td>FOREIGN KEY, NOT NULL</td></tr>
 * </table>
 *
 * <h3>Relaciones JPA Específicas:</h3>
 * <table border="1">
 *   <tr><th>Relación</th><th>Tipo</th><th>Entidad Relacionada</th><th>Cascade</th><th>Fetch</th></tr>
 *   <tr><td>@ManyToOne</td><td>Bidireccional</td><td>{@link Pedido}</td><td>Ninguno</td><td>EAGER</td></tr>
 *   <tr><td>@ManyToOne</td><td>Unidireccional</td><td>{@link Componente}</td><td>Ninguno</td><td>EAGER</td></tr>
 * </table>
 *
 * <h3>Llave Primaria Compuesta - DetallePedidoId:</h3>
 * <p>
 * La entidad utiliza una llave primaria compuesta que combina:
 * </p>
 * <ul>
 *   <li><strong>num_pedido:</strong> Número del pedido (relación con Pedido)</li>
 *   <li><strong>num_detalle:</strong> Número secuencial dentro del pedido</li>
 * </ul>
 * <p>
 * Esta estructura permite múltiples líneas por pedido y garantiza unicidad.
 * </p>
 *
 * <h3>Reglas de Negocio Específicas:</h3>
 * <ul>
 *   <li><strong>Consistencia de totales:</strong> total_cotizado = cantidad × precio_unitario</li>
 *   <li><strong>Cantidad positiva:</strong> cantidad debe ser mayor a cero</li>
 *   <li><strong>Precio válido:</strong> precio_unitario debe ser mayor a cero</li>
 *   <li><strong>Referencias válidas:</strong> num_pedido y id_componente deben existir</li>
 *   <li><strong>Numeración secuencial:</strong> num_detalle debe ser único dentro del pedido</li>
 *   <li><strong>Integridad financiera:</strong> Los totales deben ser consistentes con el pedido padre</li>
 * </ul>
 *
 * <h3>Proceso de Creación de Detalles:</h3>
 * <p>
 * Los detalles de pedido se crean durante la generación de pedidos desde cotizaciones:
 * </p>
 * <pre>{@code
 * 1. Cotizacion → DetalleCotizacion (líneas de cotización)
 *    ↓ (Conversión)
 * 2. GenerarPedidoRequest → Validación de componentes
 *    ↓ (Procesamiento)
 * 3. DetallePedido ←─── POSICIÓN ACTUAL
 *    ↓ (Persistencia)
 * 4. INSERT en codetalle_pedido con FKs
 *    ↓ (Cálculo)
 * 5. Actualización de total en Pedido padre
 * }</pre>
 *
 * <h3>Cálculos Financieros:</h3>
 * <table border="1">
 *   <tr><th>Cálculo</th><th>Fórmula</th><th>Cuándo se Ejecuta</th></tr>
 *   <tr><td>total_cotizado</td><td>cantidad × precio_unitario</td><td>Al crear/modificar línea</td></tr>
 *   <tr><td>Total pedido</td><td>∑ total_cotizado de todas las líneas</td><td>Después de modificar líneas</td></tr>
 *   <tr><td>Validación</td><td>total_cotizado > 0</td><td>Antes de persistir</td></tr>
 * </table>
 *
 * <h3>Casos de Uso Asociados:</h3>
 * <table border="1">
 *   <tr><th>CU</th><th>Descripción</th><th>Operación en Detalle</th></tr>
 *   <tr><td>CU 5.1</td><td>Generar pedido desde cotización</td><td>CREATE múltiples líneas</td></tr>
 *   <tr><td>CU 5.3</td><td>Consultar pedido específico</td><td>READ con JOIN</td></tr>
 *   <tr><td>CU 5.4</td><td>Actualizar nivel de surtido</td><td>READ para reportes</td></tr>
 *   <tr><td>CU 5.5</td><td>Cancelar pedido</td><td>DELETE cascade</td></tr>
 * </table>
 *
 * <h3>Consideraciones de Performance:</h3>
 * <ul>
 *   <li><strong>Index compuesto:</strong> En (num_pedido, num_detalle) para consultas rápidas</li>
 *   <li><strong>Foreign keys indexadas:</strong> num_pedido y id_componente con índices</li>
 *   <li><strong>Batch inserts:</strong> Para pedidos con múltiples líneas de detalle</li>
 *   <li><strong>Query optimization:</strong> Usar JOIN FETCH para consultas con componentes</li>
 *   <li><strong>Memory usage:</strong> Controlar tamaño de listas en consultas masivas</li>
 * </ul>
 *
 * <h3>Campos y su Utilidad Específica:</h3>
 * <ul>
 *   <li><strong>id (DetallePedidoId):</strong> Clave primaria compuesta para unicidad</li>
 *   <li><strong>cantidad:</strong> Cantidad específica solicitada del componente</li>
 *   <li><strong>precio_unitario:</strong> Precio vigente al momento del pedido</li>
 *   <li><strong>total_cotizado:</strong> Total calculado para esta línea específica</li>
 *   <li><strong>pedido:</strong> Referencia al pedido padre (relación bidireccional)</li>
 *   <li><strong>componente:</strong> Referencia al componente solicitado</li>
 * </ul>
 *
 * <h3>Integración con Microservicios:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Recibe datos de DetalleCotizacion</li>
 *   <li><strong>ms-cotizador-componentes:</strong> Valida existencia de componentes</li>
 *   <li><strong>Base de datos compartida:</strong> Acceso directo a componentes</li>
 *   <li><strong>Eventos Kafka:</strong> Puede publicar cambios en líneas de pedido</li>
 * </ul>
 *
 * <h3>Ejemplo de Uso en Servicio de Pedidos:</h3>
 * <pre>{@code
 * @Service
 * public class PedidoService {
 *
 *     public void crearDetallePedido(Pedido pedido, Componente componente,
 *                                   Integer cantidad, BigDecimal precioUnitario) {
 *
 *         DetallePedido detalle = new DetallePedido();
 *         detalle.setId(new DetallePedidoId(pedido.getNumPedido(), obtenerSiguienteNumDetalle(pedido)));
 *         detalle.setPedido(pedido);
 *         detalle.setComponente(componente);
 *         detalle.setCantidad(cantidad);
 *         detalle.setPrecioUnitario(precioUnitario);
 *
 *         // Cálculo del total de la línea
 *         BigDecimal totalLinea = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
 *         detalle.setTotalCotizado(totalLinea);
 *
 *         // Agregar a la colección del pedido
 *         pedido.getDetalles().add(detalle);
 *     }
 * }
 * }</pre>
 *
 * @see Pedido Entidad padre que contiene la colección de detalles
 * @see Componente Entidad del componente solicitado en esta línea
 * @see mx.com.qtx.cotizador.entidad.DetallePedido.DetallePedidoId Clase interna para llave primaria compuesta
 * @see mx.com.qtx.cotizador.dto.pedido.response.DetallePedidoResponse DTO de respuesta
 * @see mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest DTO para creación
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "codetalle_pedido")
public class DetallePedido implements Serializable {    
    
    private static final long serialVersionUID = 1L;
    
    // Llave primaria compuesta
    @EmbeddedId
    private DetallePedidoId id = new DetallePedidoId();
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    
    @Column(name = "total_cotizado")
    private BigDecimal totalCotizado;
    
    @ManyToOne
    @MapsId("idPedido") // Este campo se mapea a partir de la clave embebida
    @JoinColumn(name = "num_pedido")
    private Pedido pedido;
    
    @ManyToOne
    @JoinColumn(name = "id_componente")
    private Componente componente;
    
    // Constructores
    public DetallePedido() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public DetallePedidoId getId() {
        return id;
    }
    
    public void setId(DetallePedidoId id) {
        this.id = id;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public BigDecimal getTotalCotizado() {
        return totalCotizado;
    }
    
    public void setTotalCotizado(BigDecimal totalCotizado) {
        this.totalCotizado = totalCotizado;
    }
    
    public Pedido getPedido() {
        return pedido;
    }
    
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
        DetallePedido that = (DetallePedido) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Clase embebida que representa la llave primaria compuesta de DetallePedido.
     * <p>
     * Esta clase interna define la estructura de la llave primaria compuesta que identifica
     * de manera única cada línea de detalle dentro de un pedido específico. Combina el
     * número del pedido con un número secuencial de detalle dentro de ese pedido.
     * </p>
     *
     * <h3>Estructura de la Llave Compuesta:</h3>
     * <table border="1">
     *   <tr><th>Campo</th><th>Tipo</th><th>Descripción</th><th>Ejemplo</th></tr>
     *   <tr><td>idPedido</td><td>Integer</td><td>Número del pedido padre</td><td>12345</td></tr>
     *   <tr><td>numDetalle</td><td>Integer</td><td>Número secuencial en el pedido</td><td>1, 2, 3...</td></tr>
     * </table>
     *
     * <h3>Reglas de Generación:</h3>
     * <ul>
     *   <li><strong>idPedido:</strong> Debe coincidir con un pedido existente</li>
     *   <li><strong>numDetalle:</strong> Debe ser único dentro del mismo pedido</li>
     *   <li><strong>Secuencialidad:</strong> Generalmente 1, 2, 3... sin saltos</li>
     *   <li><strong>Consistencia:</strong> No puede haber números duplicados en el mismo pedido</li>
     * </ul>
     *
     * <h3>Ejemplo de Uso:</h3>
     * <pre>{@code
     * // Para el pedido número 12345, línea 1:
     * DetallePedidoId id1 = new DetallePedidoId(12345, 1);
     *
     * // Para el pedido número 12345, línea 2:
     * DetallePedidoId id2 = new DetallePedidoId(12345, 2);
     *
     * // Para el pedido número 67890, línea 1:
     * DetallePedidoId id3 = new DetallePedidoId(67890, 1);
     * }</pre>
     *
     * <h3>Mapeo a Columnas de Base de Datos:</h3>
     * <ul>
     *   <li><strong>idPedido → num_pedido:</strong> Foreign key al pedido padre</li>
     *   <li><strong>numDetalle → num_detalle:</strong> Número secuencial en el pedido</li>
     * </ul>
     *
     * @see DetallePedido Entidad padre que contiene esta llave
     * @see Pedido Entidad relacionada por idPedido
     * @author Subagente3F - [2025-01-17 19:30:00 MST]
     * @version 1.0.0
     * @since 1.0.0
     */
    @Embeddable
    public static class DetallePedidoId implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "num_pedido")
        private Integer idPedido;
        
        @Column(name = "num_detalle")
        private Integer numDetalle;
        
        // Constructores
        public DetallePedidoId() {}
        
        public DetallePedidoId(Integer idPedido, Integer numDetalle) {
            this.idPedido = idPedido;
            this.numDetalle = numDetalle;
        }
        
        // Getters y setters
        public Integer getIdPedido() {
            return idPedido;
        }
        
        public void setIdPedido(Integer idPedido) {
            this.idPedido = idPedido;
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
            DetallePedidoId that = (DetallePedidoId) o;
            return Objects.equals(idPedido, that.idPedido) &&
                   Objects.equals(numDetalle, that.numDetalle);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(idPedido, numDetalle);
        }
    }
}
