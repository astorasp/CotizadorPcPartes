package mx.com.qtx.cotizador.servicio.cotizacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.CotizacionEntityConverter;

@Service
public class CotizacionServicio {
    
    private CotizacionRepositorio cotizacionRepo;
    private ComponenteRepositorio componenteRepo;
    
    public CotizacionServicio(CotizacionRepositorio cotizacionRepo, ComponenteRepositorio componenteRepo) {
        this.cotizacionRepo = cotizacionRepo;
        this.componenteRepo = componenteRepo;
    }   

    @Transactional
    public void guardarCotizacion(Cotizacion cotizacion) {
        var cotizacionEntity = CotizacionEntityConverter.convertToEntity(cotizacion, null);
        CotizacionEntityConverter.addDetallesTo(cotizacion, cotizacionEntity, componenteRepo);
        cotizacionRepo.save(cotizacionEntity);
    }
}
