package mx.com.qtx.cotizador.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.persistencia.entidades.TipoComponente;

import java.util.Optional;

@Repository
public interface TipoComponenteRepositorio extends JpaRepository<TipoComponente, Short> {
    // Encontrar tipo de componente por nombre
    Optional<TipoComponente> findByNombreIgnoreCase(String nombre);
}
