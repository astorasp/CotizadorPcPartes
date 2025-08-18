package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.Promocion;

/**
 * Repositorio para la entidad Promocion en ms-cotizador-pedidos.
 * 
 * Mantiene promociones replicadas desde ms-cotizador-componentes
 * para uso en el contexto de gestión de pedidos.
 */
@Repository
public interface PromocionRepositorio extends JpaRepository<Promocion, Integer> {
    
    /**
     * Busca una promoción por su nombre.
     * 
     * @param nombre El nombre de la promoción a buscar.
     * @return La promoción encontrada o null si no existe.
     */
    Promocion findByNombre(String nombre);
}