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
 * Entidad JPA que representa un pedido en el sistema CotizadorPcPartes.
 * <p>
 * Esta entidad mapea la tabla <strong>copedido</strong> en la base de datos y representa
 * un pedido formal realizado a un proveedor basado en una cotización. Los pedidos
 * contienen información sobre fechas, proveedor, nivel de surtido y el detalle
 * de componentes solicitados.
 * </p>
 *
 * <h3>Estructura de la Tabla:</h3>
 * <ul>
 *   <li><strong>num_pedido:</strong> Clave primaria auto-generada (IDENTITY)</li>
 *   <li><strong>fecha_emision:</strong> Fecha en que se emitió el pedido</li>
 *   <li><strong>fecha_entrega:</strong> Fecha programada para la entrega</li>
 *   <li><strong>nivel_surtido:</strong> Porcentaje o nivel de cumplimiento del pedido (0-100)</li>
 *   <li><strong>total:</strong> Monto total del pedido (suma de todos los detalles)</li>
 *   <li><strong>cve_proveedor:</strong> Clave del proveedor (foreign key)</li>
 * </ul>
 *
 * <h3>Relaciones JPA:</h3>
 * <ul>
 *   <li><strong>@ManyToOne:</strong> Con {@link Proveedor} (cada pedido pertenece a un proveedor)</li>
 *   <li><strong>@OneToMany:</strong> Con {@link DetallePedido} (un pedido tiene múltiples detalles)</li>
 * </ul>
 *
 * <h3>Reglas de Negocio:</h3>
 * <ul>
 *   <li>El número de pedido es auto-generado por la base de datos</li>
 *   <li>La fecha de emisión no puede ser posterior a la fecha de entrega</li>
 *   <li>El nivel de surtido debe estar entre 0 y 100</li>
 *   <li>Los detalles del pedido se eliminan en cascada al eliminar el pedido</li>
 * </ul>
 *
 * <h3>Proceso de Generación:</h3>
 * <p>
 * Los pedidos se generan desde cotizaciones existentes utilizando el patrón Adapter:
 * </p>
 * <pre>
 * Cotización → CotizacionPresupuestoAdapter → IPresupuesto → GestorPedidos → Pedido
 * </pre>
 *
 * @see mx.com.qtx.cotizador.dominio.pedidos.Pedido Clase de dominio correspondiente
 * @see Proveedor Entidad del proveedor asociado
 * @see DetallePedido Entidades de detalle del pedido
 * @author Sistema CotizadorPcPartes - Entidad JPA
 * @version 1.0
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
