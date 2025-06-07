package mx.com.qtx.cotizadorv1ds.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizadorv1ds.core.Cotizacion;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ComponenteRepositorio;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.CotizacionRepositorio;
import mx.com.qtx.cotizadorv1ds.servicios.wrapper.CotizacionEntityConverter;

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
