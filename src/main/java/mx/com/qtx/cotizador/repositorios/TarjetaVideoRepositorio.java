package mx.com.qtx.cotizador.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.persistencia.entidades.TarjetaVideo;

@Repository
public interface TarjetaVideoRepositorio extends JpaRepository<TarjetaVideo, String> {
    
}
