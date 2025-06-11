package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.PcParte.PcPartesId;

@Repository
public interface PcPartesRepositorio extends JpaRepository<PcParte, PcPartesId> {
    
    /**
     * Contar el número de componentes en un PC
     */
    @Query("SELECT COUNT(p) FROM PcParte p WHERE p.idPc = :idPc")
    long countComponentesByPc(@Param("idPc") String idPc);
    
    /**
     * Eliminar todas las partes de un PC específico
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PcParte p WHERE p.idPc = :idPc")
    void deleteByPcId(@Param("idPc") String idPc);
    
    /**
     * Eliminar todas las partes de un PC específico (método alternativo para compatibilidad)
     */
    void deleteByIdPc(String idPc);
    
    /**
     * Verificar si existe una asociación entre un PC y un componente
     */
    @Query("SELECT COUNT(p) > 0 FROM PcParte p WHERE p.idPc = :pcId AND p.idComponente = :componenteId")
    boolean existsByPcIdAndComponenteId(@Param("pcId") String pcId, @Param("componenteId") String componenteId);
    
    /**
     * Eliminar una asociación específica entre un PC y un componente
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PcParte p WHERE p.idPc = :pcId AND p.idComponente = :componenteId")
    void deleteByPcIdAndComponenteId(@Param("pcId") String pcId, @Param("componenteId") String componenteId);
}
