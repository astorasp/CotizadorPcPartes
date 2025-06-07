package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizadorv1ds.persistencia.entidades.DiscoDuro;

@Repository
public interface DiscoDuroRepositorio extends JpaRepository<DiscoDuro, String> {
    
}
