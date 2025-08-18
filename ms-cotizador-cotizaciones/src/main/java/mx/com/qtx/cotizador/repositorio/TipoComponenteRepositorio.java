package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.TipoComponente;

import java.util.Optional;

@Repository
public interface TipoComponenteRepositorio extends JpaRepository<TipoComponente, Short> {
    // Encontrar tipo de componente por nombre
    Optional<TipoComponente> findByNombreIgnoreCase(String nombre);
    
    // Método para compatibilidad
    default TipoComponente findByNombre(String nombre) {
        return findByNombreIgnoreCase(nombre).orElse(null);
    }
}