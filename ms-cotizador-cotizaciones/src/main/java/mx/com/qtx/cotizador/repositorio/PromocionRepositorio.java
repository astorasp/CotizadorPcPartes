package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.com.qtx.cotizador.entidad.Promocion;


public interface PromocionRepositorio extends JpaRepository<Promocion, Integer> {
 
    /**
     * Busca una promoción por su nombre.
     * 
     * @param nombre El nombre de la promoción a buscar.
     * @return La promoción encontrada o null si no existe.
     */
    Promocion findByNombre(String nombre);
}
