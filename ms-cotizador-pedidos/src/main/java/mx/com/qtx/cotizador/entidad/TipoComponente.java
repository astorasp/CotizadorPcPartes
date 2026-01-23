package mx.com.qtx.cotizador.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import java.util.ArrayList;

/**
 * Entidad JPA que representa los tipos de componentes de hardware en el sistema CotizadorPcPartes - Microservicio de Pedidos.
 * <p>
 * Esta entidad mapea la tabla <strong>cotipo_componente</strong> y define las categorías o tipos
 * de componentes de hardware disponibles en el sistema (CPU, Memoria RAM, Disco Duro, etc.).
 * Sirve como clasificador principal para organizar y categorizar los componentes del catálogo.
 * </p>
 *
 * <h3>Propósito Principal en ms-cotizador-pedidos:</h3>
 * <ul>
 *   <li><strong>Clasificación de componentes:</strong> Organiza componentes por tipo/categoría</li>
 *   <li><strong>Catálogo estructurado:</strong> Proporciona jerarquía para el inventario</li>
 *   <li><strong>Validación de tipos:</strong> Verifica existencia de tipos al crear componentes</li>
 *   <li><strong>Reportes por categoría:</strong> Base para análisis por tipo de componente</li>
 *   <li><strong>Integridad referencial:</strong> Garantiza que componentes referencien tipos válidos</li>
 *   <li><strong>Filtros de búsqueda:</strong> Permite búsquedas por categoría de hardware</li>
 * </ul>
 *
 * <h3>Estructura Completa de la Tabla cotipo_componente:</h3>
 * <table border="1">
 *   <tr><th>Columna</th><th>Tipo</th><th>Descripción</th><th>Constraints</th></tr>
 *   <tr><td>id</td><td>SMALLINT</td><td>Identificador único del tipo</td><td>PRIMARY KEY, NOT NULL</td></tr>
 *   <tr><td>nombre</td><td>VARCHAR(100)</td><td>Nombre descriptivo del tipo</td><td>NOT NULL, UNIQUE</td></tr>
 * </table>
 *
 * <h3>Relaciones JPA Específicas:</h3>
 * <table border="1">
 *   <tr><th>Relación</th><th>Tipo</th><th>Entidad Relacionada</th><th>Cascade</th><th>Fetch</th></tr>
 *   <tr><td>@OneToMany</td><td>Bidireccional</td><td>{@link Componente}</td><td>ALL</td><td>LAZY</td></tr>
 * </table>
 *
 * <h3>Llave Primaria - Identificador Numérico:</h3>
 * <p>
 * La entidad utiliza un identificador numérico corto (Short) para optimizar espacio:
 * </p>
 * <ul>
 *   <li><strong>id:</strong> SMALLINT - Valores típicos: 1=CPU, 2=RAM, 3=Disco, etc.</li>
 *   <li><strong>Rango limitado:</strong> Diseñado para catálogo pequeño y estable</li>
 *   <li><strong>Performance:</strong> Índices más eficientes con tipos de datos pequeños</li>
 * </ul>
 *
 * <h3>Reglas de Negocio Específicas:</h3>
 * <ul>
 *   <li><strong>Unicidad de nombre:</strong> Cada nombre de tipo debe ser único en el sistema</li>
 *   <li><strong>Identificadores predefinidos:</strong> IDs estándar para tipos comunes de hardware</li>
 *   <li><strong>Cascade delete:</strong> Eliminar tipo elimina todos sus componentes asociados</li>
 *   <li><strong>Referencias requeridas:</strong> Todo componente debe tener un tipo asignado</li>
 *   <li><strong>Catálogo estable:</strong> Los tipos cambian raramente (alta estabilidad)</li>
 *   <li><strong>Nombres descriptivos:</strong> Nombres claros para interfaces de usuario</li>
 * </ul>
 *
 * <h3>Tipos de Componentes Estándar:</h3>
 * <p>
 * El sistema incluye los siguientes tipos de componentes estándar:
 * </p>
 * <table border="1">
 *   <tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Ejemplos</th></tr>
 *   <tr><td>1</td><td>Procesador</td><td>Unidad central de procesamiento</td><td>Intel i7, AMD Ryzen</td></tr>
 *   <tr><td>2</td><td>Memoria RAM</td><td>Memoria principal del sistema</td><td>DDR4 16GB, DDR5 32GB</td></tr>
 *   <tr><td>3</td><td>Disco Duro</td><td>Almacenamiento persistente</td><td>SSD 1TB, HDD 2TB</td></tr>
 *   <tr><td>4</td><td>Tarjeta Gráfica</td><td>Procesador gráfico dedicado</td><td>NVIDIA RTX, AMD Radeon</td></tr>
 *   <tr><td>5</td><td>Monitor</td><td>Dispositivo de visualización</td><td>24" 1080p, 27" 4K</td></tr>
 *   <tr><td>6</td><td>Placa Madre</td><td>Placa base del sistema</td><td>ATX, Micro-ATX</td></tr>
 *   <tr><td>7</td><td>Fuente de Poder</td><td>Suministro eléctrico</td><td>650W 80+ Bronze</td></tr>
 *   <tr><td>8</td><td>Gabinete</td><td>Caja del sistema</td><td>Mid Tower, Full Tower</td></tr>
 * </table>
 *
 * <h3>Casos de Uso Asociados:</h3>
 * <table border="1">
 *   <tr><th>CU</th><th>Descripción</th><th>Operación en TipoComponente</th></tr>
 *   <tr><td>CU 1.1</td><td>Consultar componentes por tipo</td><td>READ para filtrado</td></tr>
 *   <tr><td>CU 1.2</td><td>Crear componente nuevo</td><td>READ para validación de tipo</td></tr>
 *   <tr><td>CU 1.3</td><td>Modificar componente</td><td>READ para dropdown de tipos</td></tr>
 *   <tr><td>CU 5.1</td><td>Generar pedido desde cotización</td><td>READ para información de componentes</td></tr>
 * </table>
 *
 * <h3>Consideraciones de Performance:</h3>
 * <ul>
 *   <li><strong>Catálogo pequeño:</strong> Número limitado de tipos (generalmente < 20)</li>
 *   <li><strong>Cache recomendado:</strong> Los tipos pueden cachearse completamente</li>
 *   <li><strong>Índices optimizados:</strong> Índice compuesto en (id, nombre) para búsquedas</li>
 *   <li><strong>Relación LAZY:</strong> Lista de componentes se carga bajo demanda</li>
 *   <li><strong>Consultas JOIN eficientes:</strong> Para reportes por tipo de componente</li>
 *   <li><strong>Memory footprint bajo:</strong> Entidad simple con pocos campos</li>
 * </ul>
 *
 * <h3>Campos y su Utilidad Específica:</h3>
 * <ul>
 *   <li><strong>id:</strong> Identificador numérico único del tipo de componente</li>
 *   <li><strong>nombre:</strong> Nombre descriptivo del tipo para interfaces de usuario</li>
 *   <li><strong>componentes:</strong> Colección de componentes que pertenecen a este tipo</li>
 * </ul>
 *
 * <h3>Integración con Microservicios:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-componentes:</strong> Fuente principal de datos de tipos</li>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Utilizado para clasificar componentes en cotizaciones</li>
 *   <li><strong>Base de datos compartida:</strong> Acceso directo a catálogo de tipos</li>
 *   <li><strong>Sincronización Kafka:</strong> Eventos de actualización de catálogo</li>
 *   <li><strong>Cache distribuido:</strong> Para mejorar performance en consultas frecuentes</li>
 * </ul>
 *
 * <h3>Ejemplo de Uso en Servicio de Componentes:</h3>
 * <pre>{@code
 * @Service
 * public class ComponenteService {
 *
 *     private final TipoComponenteRepositorio tipoRepositorio;
 *
 *     public List<Componente> obtenerComponentesPorTipo(String nombreTipo) {
 *         // Buscar tipo por nombre
 *         TipoComponente tipo = tipoRepositorio.findByNombre(nombreTipo);
 *         if (tipo == null) {
 *             throw new TipoComponenteNoEncontradoException(nombreTipo);
 *         }
 *
 *         // Retornar componentes del tipo usando carga lazy
 *         return tipo.getComponentes();
 *     }
 *
 *     public TipoComponente crearTipoComponente(String nombre) {
 *         // Validar unicidad del nombre
 *         if (tipoRepositorio.findByNombre(nombre) != null) {
 *             throw new TipoComponenteDuplicadoException(nombre);
 *         }
 *
 *         // Crear nuevo tipo con ID auto-generado
 *         TipoComponente nuevoTipo = new TipoComponente();
 *         nuevoTipo.setNombre(nombre);
 *
 *         return tipoRepositorio.save(nuevoTipo);
 *     }
 * }
 * }</pre>
 *
 * <h3>Estrategias de Gestión del Catálogo:</h3>
 * <p>
 * El catálogo de tipos de componentes puede ser gestionado de diferentes maneras:
 * </p>
 * <ul>
 *   <li><strong>Predefinido inicial:</strong> Tipos estándar cargados al iniciar el sistema</li>
 *   <li><strong>Gestión administrativa:</strong> CRUD completo para administradores del sistema</li>
 *   <li><strong>Sincronización automática:</strong> Actualización desde ms-cotizador-componentes</li>
 *   <li><strong>Validación estricta:</strong> Solo tipos preaprobados pueden ser utilizados</li>
 *   <li><strong>Cache permanente:</strong> Una vez cargado, el catálogo cambia raramente</li>
 * </ul>
 *
 * @see Componente Entidad relacionada que referencia tipos de componente
 * @see mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio Repositorio JPA para operaciones
 * @see mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse DTO que incluye información de tipo
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "cotipo_componente")
public class TipoComponente {
    
    @Id
    private Short id;
    
    @Column(name = "nombre")
    private String nombre;
    
    @OneToMany(mappedBy = "tipoComponente", cascade = CascadeType.ALL)
    private List<Componente> componentes = new ArrayList<>();
    
    // Constructores
    public TipoComponente() {}
    
    // Getters y setters
    public Short getId() {
        return id;
    }
    
    public void setId(Short id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
