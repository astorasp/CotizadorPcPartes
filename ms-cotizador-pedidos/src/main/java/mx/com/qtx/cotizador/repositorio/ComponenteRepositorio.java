package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.Componente;

/**
 * Repositorio para la entidad Componente
 * Nota: En el contexto de microservicios, este repositorio es principalmente
 * para referencias locales. Los datos reales se obtienen del microservicio de componentes.
 */
@Repository
public interface ComponenteRepositorio extends JpaRepository<Componente, Integer> {
    
    // Métodos de consulta básicos heredados de JpaRepository
}