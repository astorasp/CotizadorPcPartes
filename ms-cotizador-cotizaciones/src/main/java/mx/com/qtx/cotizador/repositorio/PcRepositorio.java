package mx.com.qtx.cotizador.repositorio;

import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.PcParteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repositorio para gestión de PCs y sus componentes.
 * 
 * Utiliza la tabla copc_parte para gestionar las relaciones PC-Componente
 * recibidas desde ms-cotizador-componentes vía eventos Kafka.
 * 
 * @author Claude - [2025-08-20] - Repositorio de PCs para ms-cotizador-cotizaciones
 */
@Repository
public interface PcRepositorio extends JpaRepository<PcParte, PcParteId> {
    
    /**
     * Encuentra todos los componentes de una PC específica.
     */
    @Query("SELECT p FROM PcParte p WHERE p.id.idPc = :pcId")
    List<PcParte> findByPcId(@Param("pcId") String pcId);
    
    /**
     * Encuentra todas las PCs que contienen un componente específico.
     */
    @Query("SELECT p FROM PcParte p WHERE p.id.idComponente = :componenteId")
    List<PcParte> findByComponenteId(@Param("componenteId") String componenteId);
    
    /**
     * Verifica si una PC existe (tiene al menos un componente).
     */
    @Query("SELECT COUNT(p) > 0 FROM PcParte p WHERE p.id.idPc = :pcId")
    boolean existsByPcId(@Param("pcId") String pcId);
    
    /**
     * Elimina todas las partes de una PC específica.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PcParte p WHERE p.id.idPc = :pcId")
    void deleteByIdPc(@Param("pcId") String pcId);
}