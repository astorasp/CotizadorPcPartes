package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.Pedido;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para la gestión de pedidos en el sistema CotizadorPcPartes.
 * <p>
 * Esta interfaz extiende JpaRepository para proporcionar operaciones CRUD básicas
 * sobre la entidad {@link Pedido}, además de consultas personalizadas específicas
 * para el dominio de gestión de pedidos.
 * </p>
 *
 * <h3>Métodos de Consulta Personalizados:</h3>
 * <ul>
 *   <li><strong>findByProveedorCve:</strong> Busca pedidos por clave de proveedor</li>
 *   <li><strong>findByFechaEmisionBetween:</strong> Filtra pedidos por rango de fechas de emisión</li>
 *   <li><strong>findByNivelSurtido:</strong> Busca pedidos por nivel de surtido específico</li>
 *   <li><strong>findByFechaEntregaBetween:</strong> Filtra pedidos por rango de fechas de entrega</li>
 *   <li><strong>findPedidosByComponente:</strong> Busca pedidos que contienen un componente específico</li>
 * </ul>
 *
 * <h3>Operaciones CRUD Heredadas:</h3>
 * <p>
 * Hereda todos los métodos estándar de JpaRepository:
 * </p>
 * <ul>
 *   <li><strong>save:</strong> Guardar o actualizar pedido</li>
 *   <li><strong>findById:</strong> Buscar pedido por ID</li>
 *   <li><strong>findAll:</strong> Obtener todos los pedidos</li>
 *   <li><strong>deleteById:</strong> Eliminar pedido por ID</li>
 *   <li><strong>existsById:</strong> Verificar existencia de pedido</li>
 *   <li><strong>count:</strong> Contar total de pedidos</li>
 * </ul>
 *
 * <h3>Consultas de Negocio Soportadas:</h3>
 * <ul>
 *   <li><strong>CU 5.2:</strong> Generar pedido desde cotización (validación de proveedor)</li>
 *   <li><strong>CU 5.3:</strong> Consultar pedidos (búsquedas por diversos criterios)</li>
 *   <li><strong>Reportes:</strong> Análisis de pedidos por fechas, proveedores y componentes</li>
 * </ul>
 *
 * @see Pedido Entidad JPA que gestiona
 * @see JpaRepository Interfaz padre con operaciones CRUD
 * @author Sistema CotizadorPcPartes - Repositorio JPA
 * @version 1.0
 */
@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Integer> {
    /**
     * Busca todos los pedidos asociados a un proveedor específico.
     * <p>
     * Este método permite consultar el historial completo de pedidos de un proveedor,
     * útil para análisis de rendimiento, reportes y gestión de relaciones comerciales.
     * </p>
     *
     * @param cveProveedor La clave única del proveedor
     * @return Lista de pedidos ordenados por fecha de emisión descendente
     */
    List<Pedido> findByProveedorCve(String cveProveedor);

    /**
     * Busca pedidos emitidos dentro de un rango de fechas específico.
     * <p>
     * Método utilizado para reportes de periodos específicos, análisis de tendencias
     * y consultas históricas. El rango es inclusivo (incluye ambas fechas).
     * </p>
     *
     * @param inicio Fecha inicial del rango (inclusive)
     * @param fin Fecha final del rango (inclusive)
     * @return Lista de pedidos emitidos en el rango especificado
     */
    List<Pedido> findByFechaEmisionBetween(LocalDate inicio, LocalDate fin);

    /**
     * Busca pedidos con un nivel de surtido específico.
     * <p>
     * Permite filtrar pedidos por su estado de cumplimiento:
     * 0 = No surtido, 100 = Completamente surtido, valores intermedios = Parcialmente surtido.
     * </p>
     *
     * @param nivelSurtido Nivel de surtido deseado (0-100)
     * @return Lista de pedidos con el nivel de surtido especificado
     */
    List<Pedido> findByNivelSurtido(Integer nivelSurtido);

    /**
     * Busca pedidos con fecha de entrega programada dentro de un rango específico.
     * <p>
     * Método útil para planificación logística, seguimiento de entregas programadas
     * y alertas de vencimientos próximos.
     * </p>
     *
     * @param inicio Fecha inicial del rango de entrega (inclusive)
     * @param fin Fecha final del rango de entrega (inclusive)
     * @return Lista de pedidos con entregas programadas en el rango
     */
    List<Pedido> findByFechaEntregaBetween(LocalDate inicio, LocalDate fin);

    /**
     * Busca pedidos que contienen un componente específico.
     * <p>
     * Consulta avanzada que atraviesa la relación pedido-detalles-componente
     * para encontrar todos los pedidos que incluyen un componente determinado.
     * Útil para trazabilidad de componentes y análisis de demanda.
     * </p>
     *
     * @param idComponente Identificador único del componente a buscar
     * @return Lista de pedidos que incluyen el componente especificado
     */
    @Query("SELECT p FROM Pedido p JOIN p.detalles d WHERE d.componente.id = :idComponente")
    List<Pedido> findPedidosByComponente(@Param("idComponente") String idComponente);
}
