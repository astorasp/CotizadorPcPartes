package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import mx.com.qtx.cotizador.entidad.TipoComponente;

/**
 * Repositorio para la entidad TipoComponente.
 * Facilita la gestión de tipos de componentes en el microservicio de pedidos.
 */
public interface TipoComponenteRepositorio extends JpaRepository<TipoComponente, Integer> {

    /**
     * Busca un tipo de componente por su nombre.
     * 
     * @param nombre El nombre del tipo de componente a buscar.
     * @return El tipo de componente encontrado o null si no existe.
     */
    TipoComponente findByNombre(String nombre);
}