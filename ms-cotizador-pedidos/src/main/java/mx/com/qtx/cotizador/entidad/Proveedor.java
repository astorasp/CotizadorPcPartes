package mx.com.qtx.cotizador.entidad;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

/**
 * Entidad JPA que representa un proveedor en el sistema CotizadorPcPartes.
 * <p>
 * Esta entidad mapea la tabla <strong>coproveedor</strong> en la base de datos y representa
 * a las empresas o proveedores que suministran componentes para las PCs. Cada proveedor
 * puede tener múltiples pedidos asociados y es identificado por una clave única.
 * </p>
 *
 * <h3>Estructura de la Tabla:</h3>
 * <ul>
 *   <li><strong>cve:</strong> Clave primaria alfanumérica única del proveedor</li>
 *   <li><strong>nombre:</strong> Nombre comercial o corto del proveedor</li>
 *   <li><strong>razon_social:</strong> Razón social completa y legal del proveedor</li>
 * </ul>
 *
 * <h3>Relaciones JPA:</h3>
 * <ul>
 *   <li><strong>@OneToMany:</strong> Con {@link Pedido} (un proveedor puede tener múltiples pedidos)</li>
 * </ul>
 *
 * <h3>Reglas de Negocio:</h3>
 * <ul>
 *   <li>La clave (CVE) debe ser única en todo el sistema</li>
 *   <li>La clave no puede ser modificada después de creada</li>
 *   <li>Los pedidos asociados se eliminan en cascada al eliminar un proveedor</li>
 *   <li>La razón social debe corresponder a la denominación legal del proveedor</li>
 * </ul>
 *
 * <h3>Casos de Uso Asociados:</h3>
 * <ul>
 *   <li><strong>CU 4.1:</strong> Agregar proveedor</li>
 *   <li><strong>CU 4.2:</strong> Modificar proveedor</li>
 *   <li><strong>CU 4.3:</strong> Consultar proveedores</li>
 *   <li><strong>CU 4.4:</strong> Eliminar proveedor</li>
 *   <li><strong>CU 5.2:</strong> Generar pedido desde cotización (selección de proveedor)</li>
 * </ul>
 *
 * <h3>Integración con Microservicios:</h3>
 * <p>
 * Los proveedores pueden ser sincronizados desde otros microservicios vía eventos Kafka
 * o pueden ser gestionados directamente en este microservicio para pedidos específicos.
 * </p>
 *
 * @see mx.com.qtx.cotizador.dominio.pedidos.Proveedor Clase de dominio correspondiente
 * @see Pedido Entidades de pedidos asociados al proveedor
 * @author Sistema CotizadorPcPartes - Entidad JPA
 * @version 1.0
 */
@Entity
@Table(name = "coproveedor")
public class Proveedor {
    
    @Id
    @Column(name = "cve")
    private String cve;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "razon_social")
    private String razonSocial;
    
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();
    
    // Constructores
    public Proveedor() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public String getCve() {
        return cve;
    }
    
    public void setCve(String cve) {
        this.cve = cve;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRazonSocial() {
        return razonSocial;
    }
    
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
    
    public List<Pedido> getPedidos() {
        return pedidos;
    }
    
    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
    
    // Método helper para agregar un pedido
    public void addPedido(Pedido pedido) {
        pedidos.add(pedido);
        pedido.setProveedor(this);
    }
}
