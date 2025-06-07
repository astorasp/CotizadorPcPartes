package mx.com.qtx.cotizador.servicio.componente;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.Pc;
import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.DiscoDuroRepositorio;
import mx.com.qtx.cotizador.repositorio.PcPartesRepositorio;
import mx.com.qtx.cotizador.repositorio.TarjetaVideoRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.ComponenteEntityConverter;

@Service
public class ComponenteServicio {
    
    private ComponenteRepositorio compRepo;
    private TipoComponenteRepositorio tipoRepo;
    private PcPartesRepositorio pcPartesRepo;  
    private DiscoDuroRepositorio discoDuroRepo;
    private TarjetaVideoRepositorio tarjetaVideoRepo; 
    private List<TipoComponente> tipos;
    
    public ComponenteServicio(ComponenteRepositorio compRepo, 
        TipoComponenteRepositorio tipoRepo, PcPartesRepositorio pcPartesRepo,
        DiscoDuroRepositorio discoDuroRepo, TarjetaVideoRepositorio tarjetaVideoRepo) {
        this.compRepo = compRepo;
        this.tipoRepo = tipoRepo;
        this.pcPartesRepo = pcPartesRepo;
        this.discoDuroRepo = discoDuroRepo;
        this.tarjetaVideoRepo = tarjetaVideoRepo;
        tipos = tipoRepo.findAll();
    }

    @Transactional
    public void borrarComponente(String id) {
        discoDuroRepo.deleteById(id);
        tarjetaVideoRepo.deleteById(id);
        compRepo.deleteById(id);
    }

    @Transactional
    public void guardarComponente(Componente comp) {
        // Convertir y guardar/actualizar componente si es necesario
        // Usamos el método con nombre descriptivo para evitar ambigüedades
        var compEntity = ComponenteEntityConverter.convertToEntity(comp);            
        compRepo.save(compEntity);
    }

    @Transactional
    public void guardarPcCompleto(Componente pcComponente) {
        // 1. Convertir y guardar PC
        // Usamos el método con nombre descriptivo para evitar ambigüedades
        if(pcComponente instanceof Pc) {
            Pc pc = (Pc) pcComponente;
            var pcEntity = ComponenteEntityConverter.convertToEntity(pc);
            pcEntity = compRepo.save(pcEntity);        
            // 2. Procesar componentes y crear asociaciones
            for (Componente comp : pc.getSubComponentes()) {
                // Convertir y guardar/actualizar componente si es necesario
                // Usamos el método con nombre descriptivo para evitar ambigüedades
                var compEntity = ComponenteEntityConverter.convertToEntity(comp);
                compRepo.save(compEntity);        

                PcParte pcParte = new PcParte(pcEntity.getId(), compEntity.getId());
                pcPartesRepo.save(pcParte);
            }
        }
    }


    public Componente buscarComponente(String id) {
        return ComponenteEntityConverter.convertToComponente(compRepo.findById(id).orElse(null));
    }   
}
