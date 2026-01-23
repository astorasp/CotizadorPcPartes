package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.Cotizacion;

/**
 * Repositorio para la entidad Cotizacion
 * Nota: En el contexto de microservicios, este repositorio es principalmente
 * para referencias locales. Los datos reales se obtienen del microservicio de cotizaciones.
 */
@Repository
public interface CotizacionRepositorio extends JpaRepository<Cotizacion, Integer> {
    
    // Métodos de consulta básicos heredados de JpaRepository
}