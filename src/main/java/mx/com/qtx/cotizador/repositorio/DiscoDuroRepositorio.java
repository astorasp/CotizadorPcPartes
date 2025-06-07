package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.DiscoDuro;

@Repository
public interface DiscoDuroRepositorio extends JpaRepository<DiscoDuro, String> {
    
}
