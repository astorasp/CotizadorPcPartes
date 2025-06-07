package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.PcParte.PcPartesId;

import java.util.List;

@Repository
public interface PcPartesRepositorio extends JpaRepository<PcParte, PcPartesId> {
    
    // Encontrar componentes por tipo para un PC específico
    @Query("SELECT p FROM PcPartes p WHERE p.idPc = :idPc AND p.componente.tipoComponente.nombre = :tipoComponente")
    List<PcParte> findByPcAndTipoComponente(
        @Param("idPc") String idPc, 
        @Param("tipoComponente") String tipoComponente
    );
    
    // Contar el número de componentes en un PC
    @Query("SELECT COUNT(p) FROM PcPartes p WHERE p.idPc = :idPc")
    long countComponentesByPc(@Param("idPc") String idPc);
    
    // Eliminar todas las partes de un PC específico
    void deleteByIdPc(String idPc);
}
