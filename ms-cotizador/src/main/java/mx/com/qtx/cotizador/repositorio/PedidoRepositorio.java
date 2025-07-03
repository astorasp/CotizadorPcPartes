package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.Pedido;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Integer> {
    // Encontrar pedidos por proveedor
    List<Pedido> findByProveedorCve(String cveProveedor);
    
    // Encontrar pedidos por rango de fechas de emisión
    List<Pedido> findByFechaEmisionBetween(LocalDate inicio, LocalDate fin);
    
    // Encontrar pedidos por nivel de surtido
    List<Pedido> findByNivelSurtido(Integer nivelSurtido);
    
    // Encontrar pedidos por rango de fechas de entrega
    List<Pedido> findByFechaEntregaBetween(LocalDate inicio, LocalDate fin);
    
    // Buscar pedidos que contengan un componente específico (usando JPQL)
    @Query("SELECT p FROM Pedido p JOIN p.detalles d WHERE d.componente.id = :idComponente")
    List<Pedido> findPedidosByComponente(@Param("idComponente") String idComponente);
}
