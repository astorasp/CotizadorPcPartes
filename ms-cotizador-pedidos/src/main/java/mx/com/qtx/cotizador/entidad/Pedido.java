package mx.com.qtx.cotizador.entidad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa un pedido en el sistema CotizadorPcPartes - Microservicio de Pedidos.
 * <p>
 * Esta entidad mapea la tabla <strong>copedido</strong> en la base de datos y representa
 * un pedido formal realizado a un proveedor basado en una cotización. Los pedidos
 * contienen información completa sobre fechas, proveedor, nivel de surtido, componentes
 * solicitados y montos totales. Es la entidad central del microservicio de pedidos.
 * </p>
 *
 * <h3>Propósito Principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Gestión de pedidos:</strong> Almacena pedidos generados desde cotizaciones</li>
 *   <li><strong>Relación con proveedores:</strong> Vincula pedidos con proveedores específicos</li>
 *   <li><strong>Seguimiento de entregas:</strong> Controla fechas de emisión y entrega</li>
 *   <li><strong>Control de cumplimiento:</strong> Monitorea nivel de surtido de pedidos</li>
 *   <li><strong>Cálculo de totales:</strong> Mantiene consistencia entre detalles y totales</li>
 *   <li><strong>Integración con cotizaciones:</strong> Puente entre cotizaciones y ejecución</li>
 * </ul>
 *
 * <h3>Estructura Completa de la Tabla copedido:</h3>
 * <table border="1">
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th><th>Constraints</th></tr>
 *   <tr><td>num_pedido</td><td>INT IDENTITY</td><td>Clave primaria auto-generada</td><td>PRIMARY KEY, NOT NULL</td></tr>
 *   <tr><td>fecha_emision</td><td>DATE</td><td>Fecha de emisión del pedido</td><td>NOT NULL</td></tr>
 *   <tr><td>fecha_entrega</td><td>DATE</td><td>Fecha programada de entrega</td><td>NOT NULL</td></tr>
 *   <tr><td>nivel_surtido</td><td>INT</td><td>Porcentaje de cumplimiento (0-100)</td><td>NOT NULL, CHECK 0-100</td></tr>
 *   <tr><td>total</td><td>DECIMAL(10,2)</td><td>Monto total del pedido</td><td>NOT NULL</td></tr>
 *   <tr><td>cve_proveedor</td><td>VARCHAR(10)</td><td>Clave del proveedor</td><td>FOREIGN KEY, NOT NULL</td></tr>
 * </table>
 *
 * <h3>Relaciones JPA Complejas:</h3>
 * <table border="1">
 *   <tr><th>Relación</th><th>Tipo</th><th>Entidad Relacionada</th><th>Cascade</th><th>Fetch</th></tr>
 *   <tr><td>@ManyToOne</td><td>Unidireccional</td><td>{@link Proveedor}</td><td>Ninguno</td><td>EAGER</td></tr>
 *   <tr><td>@OneToMany</td><td>Bidireccional</td><td>{@link DetallePedido}</td><td>ALL</td><td>LAZY</td></tr>
 * </table>
 *
 * <h3>Reglas de Negocio Específicas:</h3>
 * <ul>
 *   <li><strong>Auto-generación:</strong> num_pedido es IDENTITY, no se puede establecer manualmente</li>
 *   <li><strong>Validación temporal:</strong> fecha_emision ≤ fecha_entrega</li>
 *   <li><strong>Rango de surtido:</strong> nivel_surtido debe estar entre 0 y 100</li>
 *   <li><strong>Integridad referencial:</strong> cve_proveedor debe existir en coproveedor</li>
 *   <li><strong>Consistencia de totales:</strong> total = suma de totalCotizado de todos los detalles</li>
 *   <li><strong>Cascade delete:</strong> Eliminar pedido elimina todos sus detalles</li>
 *   <li><strong>Estado inmutable:</strong> Una vez creado, solo se pueden actualizar ciertos campos</li>
 * </ul>
 *
 * <h3>Proceso de Generación desde Cotizaciones:</h3>
 * <p>
 * Los pedidos se generan desde cotizaciones existentes siguiendo este flujo:
 * </p>
 * <pre>{@code
 * 1. Cotización (ms-cotizador-cotizaciones)
 *    ↓ (REST API)
 * 2. GenerarPedidoRequest (DTO de entrada)
 *    ↓ (Validación + Servicio)
 * 3. CotizacionPresupuestoAdapter (Patrón Adapter)
 *    ↓ (Conversión)
 * 4. IPresupuesto (Interfaz de dominio)
 *    ↓ (Procesamiento de negocio)
 * 5. GestorPedidos (Servicio de dominio)
 *    ↓ (Persistencia)
 * 6. Pedido (Entidad JPA) ←─── POSICIÓN ACTUAL
 *    ↓ (Relaciones)
 * 7. DetallePedido + Proveedor (Entidades relacionadas)
 * }</pre>
 *
 * <h3>Casos de Uso Asociados:</h3>
 * <table border="1">
 *   <tr><th>CU</th><th>Descripción</th><th>Operación</th></tr>
 *   <tr><td>CU 5.1</td><td>Generar pedido desde cotización</td><td>INSERT con relaciones</td></tr>
 *   <tr><td>CU 5.2</td><td>Consultar pedidos por proveedor</td><td>SELECT con JOIN</td></tr>
 *   <tr><td>CU 5.3</td><td>Consultar pedido específico</td><td>SELECT con fetch</td></tr>
 *   <tr><td>CU 5.4</td><td>Actualizar nivel de surtido</td><td>UPDATE parcial</td></tr>
 *   <tr><td>CU 5.5</td><td>Cancelar pedido</td><td>DELETE con cascade</td></tr>
 * </table>
 *
 * <h3>Consideraciones de Performance:</h3>
 * <ul>
 *   <li><strong>Fetch strategy:</strong> Relación con proveedor EAGER, detalles LAZY</li>
 *   <li><strong>Indexing:</strong> Considerar índices en cve_proveedor y fechas</li>
 *   <li><strong>Batch operations:</strong> Para operaciones masivas con múltiples pedidos</li>
 *   <li><strong>Query optimization:</strong> Usar @EntityGraph para consultas complejas</li>
 *   <li><strong>Memory management:</strong> Controlar tamaño de listas de detalles</li>
 * </ul>
 *
 * <h3>Campos Calculados y Derivados:</h3>
 * <ul>
 *   <li><strong>total:</strong> Calculado como suma de totalCotizado de todos los detalles</li>
 *   <li><strong>num_pedido:</strong> Auto-generado por IDENTITY en INSERT</li>
 *   <li><strong>fecha_emision:</strong> Establecida al momento de creación</li>
 *   <li><strong>nivel_surtido:</strong> Inicialmente 0, actualizado durante el proceso</li>
 * </ul>
 *
 * <h3>Integración con Microservicios:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Recibe datos de cotizaciones vía API REST</li>
 *   <li><strong>ms-cotizador-componentes:</strong> Referencia componentes para detalles</li>
 *   <li><strong>Base de datos compartida:</strong> Acceso directo a tablas relacionadas</li>
 *   <li><strong>Eventos Kafka:</strong> Puede publicar eventos de cambios en pedidos</li>
 * </ul>
 *
 * @see mx.com.qtx.cotizador.dominio.pedidos.Pedido Clase de dominio correspondiente
 * @see Proveedor Entidad del proveedor asociado (@ManyToOne)
 * @see DetallePedido Entidades de detalle del pedido (@OneToMany)
 * @see mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest DTO para creación
 * @see mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse DTO de respuesta
 * @see mx.com.qtx.cotizador.repositorio.PedidoRepositorio Repositorio JPA
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "copedido")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_pedido")
    private Integer numPedido;
    
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
    
    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;
    
    @Column(name = "nivel_surtido")
    private Integer nivelSurtido;
    
    @Column(name = "total")
    private BigDecimal total;
    
    @ManyToOne
    @JoinColumn(name = "cve_proveedor")
    private Proveedor proveedor;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<DetallePedido> detalles = new ArrayList<>();
    
    // Constructores
    public Pedido() {
        // Constructor vacío necesario para JPA
    }
    
    // Getters y setters
    public Integer getNumPedido() {
        return numPedido;
    }
    
    public void setNumPedido(Integer numPedido) {
        this.numPedido = numPedido;
    }
    
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }
    
    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    
    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }
    
    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
    
    public Integer getNivelSurtido() {
        return nivelSurtido;
    }
    
    public void setNivelSurtido(Integer nivelSurtido) {
        this.nivelSurtido = nivelSurtido;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public Proveedor getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
    
    public List<DetallePedido> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
    
    // Método helper para agregar un detalle
    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }
    
    /**
     * Método para obtener el ID del pedido (alias para numPedido)
     * @return número de pedido como ID
     */
    public Integer getId() {
        return this.numPedido;
    }
}
