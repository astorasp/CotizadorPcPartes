package mx.com.qtx.cotizador.entidad;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

/**
 * Entidad JPA que representa una cotización en el sistema CotizadorPcPartes - Microservicio de Pedidos.
 * <p>
 * Esta entidad mapea la tabla <strong>cocotizacion</strong> y representa una cotización completa
 * generada por el sistema de cotizaciones. En el contexto del microservicio de pedidos,
 * las cotizaciones sirven como fuente de datos para generar pedidos formales. Esta entidad
 * mantiene la información histórica y financiera de las cotizaciones para su posterior conversión.
 * </p>
 *
 * <h3>Propósito Principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Fuente de pedidos:</strong> Base de datos para generar pedidos desde cotizaciones existentes</li>
 *   <li><strong>Histórico financiero:</strong> Mantiene subtotales, impuestos y totales históricos</li>
 *   <li><strong>Referencia de componentes:</strong> Contiene detalles de componentes cotizados</li>
 *   <li><strong>Validación de pedidos:</strong> Verifica existencia y vigencia de cotizaciones</li>
 *   <li><strong>Trazabilidad:</strong> Permite seguimiento desde cotización hasta pedido final</li>
 *   <li><strong>Integridad de datos:</strong> Garantiza consistencia entre cotizaciones y pedidos</li>
 * </ul>
 *
 * <h3>Estructura Completa de la Tabla cocotizacion:</h3>
 * <table border="1">
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th><th>Constraints</th></tr>
 *   <tr><td>folio</td><td>INT IDENTITY</td><td>Clave primaria auto-generada</td><td>PRIMARY KEY, NOT NULL</td></tr>
 *   <tr><td>fecha</td><td>VARCHAR(50)</td><td>Fecha de emisión de la cotización</td><td>NOT NULL</td></tr>
 *   <tr><td>impuestos</td><td>DECIMAL(10,2)</td><td>Monto total de impuestos calculados</td><td>NOT NULL</td></tr>
 *   <tr><td>subtotal</td><td>DECIMAL(10,2)</td><td>Subtotal antes de impuestos</td><td>NOT NULL</td></tr>
 *   <tr><td>total</td><td>DECIMAL(10,2)</td><td>Monto total de la cotización</td><td>NOT NULL</td></tr>
 * </table>
 *
 * <h3>Relaciones JPA Específicas:</h3>
 * <table border="1">
 *   <tr><th>Relación</th><th>Tipo</th><th>Entidad Relacionada</th><th>Cascade</th><th>Fetch</th></tr>
 *   <tr><td>@OneToMany</td><td>Bidireccional</td><td>{@link DetalleCotizacion}</td><td>Ninguno</td><td>LAZY</td></tr>
 * </table>
 *
 * <h3>Llave Primaria - Generación Automática:</h3>
 * <p>
 * La entidad utiliza una llave primaria auto-generada:
 * </p>
 * <ul>
 *   <li><strong>folio:</strong> IDENTITY - Generado automáticamente por la base de datos</li>
 *   <li><strong>Secuencialidad:</strong> Cada nueva cotización recibe un folio único incremental</li>
 *   <li><strong>No modificable:</strong> El folio no puede ser establecido manualmente</li>
 * </ul>
 *
 * <h3>Reglas de Negocio Específicas:</h3>
 * <ul>
 *   <li><strong>Consistencia financiera:</strong> total = subtotal + impuestos</li>
 *   <li><strong>Subtotal válido:</strong> subtotal debe ser mayor a cero</li>
 *   <li><strong>Impuestos calculados:</strong> impuestos calculados según reglas fiscales</li>
 *   <li><strong>Total consistente:</strong> total debe coincidir con suma de detalles</li>
 *   <li><strong>Fecha requerida:</strong> fecha no puede ser null o vacía</li>
 *   <li><strong>Detalles requeridos:</strong> Una cotización debe tener al menos un detalle</li>
 * </ul>
 *
 * <h3>Proceso de Conversión a Pedidos:</h3>
 * <p>
 * Las cotizaciones sirven como base para generar pedidos formales:
 * </p>
 * <pre>{@code
 * 1. Cotizacion (con DetalleCotizacion)
 *    ↓ (Selección por usuario)
 * 2. GenerarPedidoRequest (folio + datos adicionales)
 *    ↓ (Validación de existencia)
 * 3. Verificar Cotizacion.folio existe
 *    ↓ (Procesamiento)
 * 4. Crear Pedido desde Cotizacion
 *    ↓ (Conversión)
 * 5. DetalleCotizacion → DetallePedido (por cada línea)
 *    ↓ (Persistencia)
 * 6. Guardar Pedido con DetallePedido
 *    ↓ (Resultado)
 * 7. Pedido generado exitosamente
 * }</pre>
 *
 * <h3>Cálculos Financieros:</h3>
 * <table border="1">
 *   <tr><th>Cálculo</th><th>Fórmula</th><th>Cuándo se Ejecuta</th></tr>
 *   <tr><td>subtotal</td><td>∑ precio_base × cantidad de detalles</td><td>Al calcular cotización</td></tr>
 *   <tr><td>impuestos</td><td>subtotal × tasa_impuesto</td><td>Según reglas fiscales</td></tr>
 *   <tr><td>total</td><td>subtotal + impuestos</td><td>Al finalizar cotización</td></tr>
 *   <tr><td>Validación</td><td>total > 0</td><td>Antes de guardar</td></tr>
 * </table>
 *
 * <h3>Casos de Uso Asociados:</h3>
 * <table border="1">
 *   <tr><th>CU</th><th>Descripción</th><th>Operación en Cotizacion</th></tr>
 *   <tr><td>CU 5.1</td><td>Generar pedido desde cotización</td><td>READ con detalles</td></tr>
 *   <tr><td>CU 5.2</td><td>Consultar pedidos por proveedor</td><td>READ para trazabilidad</td></tr>
 *   <tr><td>CU 5.3</td><td>Consultar pedido específico</td><td>READ para comparación</td></tr>
 *   <tr><td>CU 3.1-3.3</td><td>Generar cotización (ms-cotizaciones)</td><td>CREATE con detalles</td></tr>
 * </table>
 *
 * <h3>Consideraciones de Performance:</h3>
 * <ul>
 *   <li><strong>Index en folio:</strong> Clave primaria, consultas frecuentes por folio</li>
 *   <li><strong>Index en fecha:</strong> Para consultas por rango de fechas</li>
 *   <li><strong>Relación LAZY:</strong> Detalles se cargan bajo demanda</li>
 *   <li><strong>Query optimization:</strong> Usar JOIN FETCH para consultas con detalles</li>
 *   <li><strong>Archival strategy:</strong> Considerar archivado de cotizaciones antiguas</li>
 * </ul>
 *
 * <h3>Campos y su Utilidad Específica:</h3>
 * <ul>
 *   <li><strong>folio:</strong> Identificador único auto-generado de la cotización</li>
 *   <li><strong>fecha:</strong> Fecha de emisión de la cotización (formato string)</li>
 *   <li><strong>impuestos:</strong> Monto total de impuestos calculados en la cotización</li>
 *   <li><strong>subtotal:</strong> Suma de los precios base antes de impuestos</li>
 *   <li><strong>total:</strong> Monto total final de la cotización</li>
 *   <li><strong>detalles:</strong> Colección de líneas de detalle de la cotización</li>
 * </ul>
 *
 * <h3>Integración con Microservicios:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Microservicio que crea y gestiona las cotizaciones</li>
 *   <li><strong>ms-cotizador-componentes:</strong> Proporciona datos de componentes para detalles</li>
 *   <li><strong>Base de datos compartida:</strong> Acceso directo a datos de cotizaciones</li>
 *   <li><strong>Sincronización Kafka:</strong> Eventos de actualización desde ms-cotizaciones</li>
 * </ul>
 *
 * <h3>Ejemplo de Conversión a Pedido:</h3>
 * <pre>{@code
 * @Service
 * public class PedidoService {
 *
 *     public Pedido convertirCotizacionAPedido(Cotizacion cotizacion,
 *                                              String cveProveedor,
 *                                              LocalDate fechaEntrega) {
 *
 *         Pedido pedido = new Pedido();
 *         // El num_pedido se genera automáticamente en INSERT
 *
 *         pedido.setFechaEmision(LocalDate.now());
 *         pedido.setFechaEntrega(fechaEntrega);
 *         pedido.setNivelSurtido(0); // Inicialmente no surtido
 *         pedido.setTotal(cotizacion.getTotal());
 *
 *         // Buscar proveedor por CVE
 *         Proveedor proveedor = proveedorRepositorio.findByCve(cveProveedor);
 *         pedido.setProveedor(proveedor);
 *
 *         // Convertir detalles de cotización a detalles de pedido
 *         for (DetalleCotizacion detalleCot : cotizacion.getDetalles()) {
 *             DetallePedido detallePed = convertirDetalleCotizacion(detalleCot, pedido);
 *             pedido.getDetalles().add(detallePed);
 *         }
 *
 *         return pedido;
 *     }
 * }
 * }</pre>
 *
 * <h3>Diferencias con Pedido:</h3>
 * <table border="1">
 *   <tr><th>Aspecto</th><th>Cotizacion</th><th>Pedido</th></tr>
 *   <tr><td>Estado</td><td>Estimación/Propuesta</td><td>Compromiso Formal</td></tr>
 *   <tr><td>Proveedor</td><td>No asignado</td><td>Asignado obligatoriamente</td></tr>
 *   <tr><td>Fecha entrega</td><td>No obligatoria</td><td>Obligatoria</td></tr>
 *   <tr><td>Nivel surtido</td><td>No aplica</td><td>0-100%</td></tr>
 *   <tr><td>Cascade</td><td>Ninguno</td><td>ALL en detalles</td></tr>
 *   <tr><td>Propósito</td><td>Estimación de costos</td><td>Ejecución de compra</td></tr>
 * </table>
 *
 * @see DetalleCotizacion Líneas de detalle de la cotización
 * @see Pedido Entidad resultante de la conversión de pedidos
 * @see mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest DTO para conversión
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "cocotizacion")
public class Cotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folio;
    
    @Column(name = "fecha")
    private String fecha;
    
    @Column(name = "impuestos")
    private BigDecimal impuestos;
    
    @Column(name = "subtotal")
    private BigDecimal subtotal;
    
    @Column(name = "total")
    private BigDecimal total;
    
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL)
    private List<DetalleCotizacion> detalles = new ArrayList<>();
    
    // Constructores
    public Cotizacion() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public String getFecha() {
        return fecha;
    }
    
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    
    public Integer getFolio() {
        return folio;
    }
    
    public void setFolio(Integer folio) {
        this.folio = folio;
    }
    
    public BigDecimal getImpuestos() {
        return impuestos;
    }
    
    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public List<DetalleCotizacion> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetalleCotizacion> detalles) {
        this.detalles = detalles;
    }
    
    // Método helper para agregar un detalle
    public void addDetalle(DetalleCotizacion detalle) {
        detalles.add(detalle);
        detalle.setCotizacion(this);
    }
}
