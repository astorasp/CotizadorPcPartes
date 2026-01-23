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
 * Entidad JPA que representa un proveedor en el sistema CotizadorPcPartes - Microservicio de Pedidos.
 * <p>
 * Esta entidad mapea la tabla <strong>coproveedor</strong> en la base de datos y representa
 * a las empresas o proveedores que suministran componentes para las PCs. Los proveedores
 * son entidades críticas en el flujo de pedidos, ya que cada pedido debe estar asociado
 * a un proveedor específico que se encargará de surtir los componentes solicitados.
 * </p>
 *
 * <h3>Propósito Principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Catálogo de proveedores:</strong> Mantiene registro de proveedores disponibles</li>
 *   <li><strong>Asociación con pedidos:</strong> Cada pedido debe tener un proveedor asignado</li>
 *   <li><strong>Información legal:</strong> Almacena razón social y datos formales del proveedor</li>
 *   <li><strong>Histórico de pedidos:</strong> Mantiene relación con todos los pedidos del proveedor</li>
 *   <li><strong>Integridad referencial:</strong> Garantiza que pedidos referencien proveedores válidos</li>
 *   <li><strong>Reportes y análisis:</strong> Base para análisis de performance por proveedor</li>
 * </ul>
 *
 * <h3>Estructura Completa de la Tabla coproveedor:</h3>
 * <table border="1">
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th><th>Constraints</th></tr>
 *   <tr><td>cve</td><td>VARCHAR(10)</td><td>Clave primaria alfanumérica</td><td>PRIMARY KEY, NOT NULL, UNIQUE</td></tr>
 *   <tr><td>nombre</td><td>VARCHAR(100)</td><td>Nombre comercial del proveedor</td><td>NOT NULL</td></tr>
 *   <tr><td>razon_social</td><td>VARCHAR(200)</td><td>Razón social legal completa</td><td>NOT NULL</td></tr>
 * </table>
 *
 * <h3>Relaciones JPA Específicas:</h3>
 * <table border="1">
 *   <tr><th>Relación</th><th>Tipo</th><th>Entidad Relacionada</th><th>Cascade</th><th>Fetch</th></tr>
 *   <tr><td>@OneToMany</td><td>Bidireccional</td><td>{@link Pedido}</td><td>ALL</td><td>LAZY</td></tr>
 * </table>
 *
 * <h3>Reglas de Negocio Específicas:</h3>
 * <ul>
 *   <li><strong>Unicidad de clave:</strong> CVE debe ser única en todo el sistema</li>
 *   <li><strong>Inmutabilidad de clave:</strong> Una vez creada, la CVE no puede modificarse</li>
 *   <li><strong>Cascade delete:</strong> Eliminar proveedor elimina todos sus pedidos asociados</li>
 *   <li><strong>Integridad legal:</strong> La razón social debe corresponder a la denominación legal</li>
 *   <li><strong>Referencial integrity:</strong> No se puede eliminar proveedor con pedidos activos</li>
 *   <li><strong>Formato de clave:</strong> Generalmente alfanumérica con patrón específico</li>
 * </ul>
 *
 * <h3>Casos de Uso Asociados en ms-cotizador-pedidos:</h3>
 * <table border="1">
 *   <tr><th>CU</th><th>Descripción</th><th>Operación en Proveedor</th></tr>
 *   <tr><td>CU 4.1</td><td>Agregar proveedor</td><td>INSERT en coproveedor</td></tr>
 *   <tr><td>CU 4.2</td><td>Modificar proveedor</td><td>UPDATE en coproveedor</td></tr>
 *   <tr><td>CU 4.3</td><td>Consultar proveedores</td><td>SELECT desde coproveedor</td></tr>
 *   <tr><td>CU 4.4</td><td>Eliminar proveedor</td><td>DELETE con cascade</td></tr>
 *   <tr><td>CU 5.1</td><td>Generar pedido desde cotización</td><td>Validación de existencia</td></tr>
 *   <tr><td>CU 5.2</td><td>Consultar pedidos por proveedor</td><td>JOIN con pedidos</td></tr>
 * </table>
 *
 * <h3>Proceso de Integración con Pedidos:</h3>
 * <p>
 * Los proveedores son críticos en el flujo de generación de pedidos:
 * </p>
 * <pre>{@code
 * 1. GenerarPedidoRequest (contiene cve_proveedor)
 *    ↓ (Validación)
 * 2. Buscar Proveedor por CVE en coproveedor
 *    ↓ (Si no existe → Error PROV001)
 * 3. Validar que proveedor esté activo
 *    ↓ (Crear asociación)
 * 4. Crear Pedido con referencia a Proveedor ←─── POSICIÓN ACTUAL
 *    ↓ (Persistencia)
 * 5. Guardar Pedido con foreign key a proveedor
 * }</pre>
 *
 * <h3>Consideraciones de Performance:</h3>
 * <ul>
 *   <li><strong>Index en CVE:</strong> Clave primaria, búsquedas frecuentes por este campo</li>
 *   <li><strong>Relación LAZY:</strong> Lista de pedidos se carga bajo demanda</li>
 *   <li><strong>Cache de proveedores:</strong> Considerar cache para proveedores activos</li>
 *   <li><strong>Query optimization:</strong> Usar JOIN FETCH para consultas con pedidos</li>
 *   <li><strong>Batch operations:</strong> Para operaciones masivas con múltiples proveedores</li>
 * </ul>
 *
 * <h3>Campos y su Utilidad:</h3>
 * <ul>
 *   <li><strong>cve:</strong> Identificador único, usado en todas las operaciones</li>
 *   <li><strong>nombre:</strong> Nombre comercial para interfaces de usuario</li>
 *   <li><strong>razon_social:</strong> Nombre legal para documentos formales</li>
 *   <li><strong>pedidos:</strong> Colección de pedidos asociados (relación bidireccional)</li>
 * </ul>
 *
 * <h3>Estrategias de Sincronización:</h3>
 * <p>
 * Los proveedores pueden ser gestionados de diferentes maneras:
 * </p>
 * <ul>
 *   <li><strong>Gestión local:</strong> CRUD completo en este microservicio</li>
 *   <li><strong>Sincronización Kafka:</strong> Eventos desde ms-proveedores central</li>
 *   <li><strong>Referencias read-only:</strong> Solo consultas desde otros microservicios</li>
 *   <li><strong>Cache distribuido:</strong> Para mejorar performance en consultas frecuentes</li>
 * </ul>
 *
 * @see mx.com.qtx.cotizador.dominio.pedidos.Proveedor Clase de dominio correspondiente
 * @see Pedido Entidades de pedidos asociados (@OneToMany bidireccional)
 * @see mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest DTO para creación
 * @see mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse DTO de respuesta
 * @see mx.com.qtx.cotizador.repositorio.ProveedorRepositorio Repositorio JPA
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
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
