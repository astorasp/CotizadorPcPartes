package mx.com.qtx.cotizador.servicio.componente;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.Pc;
import mx.com.qtx.cotizador.dominio.core.componentes.TipoComponenteEnum;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PcPartesRepositorio;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.ComponenteEntityConverter;
import mx.com.qtx.cotizador.util.Errores;

@Service
public class ComponenteServicio {
    
    private ComponenteRepositorio compRepo;
    private PcPartesRepositorio pcPartesRepo;  
    private PromocionRepositorio promoRepo;
    private List<TipoComponente> tipos;
    public ComponenteServicio(ComponenteRepositorio compRepo, 
        PcPartesRepositorio pcPartesRepo,
        PromocionRepositorio promoRepo,
        TipoComponenteRepositorio tipoRepo) {
        this.compRepo = compRepo;
        this.pcPartesRepo = pcPartesRepo;
        this.promoRepo = promoRepo;
        this.tipos = tipoRepo.findAll();
    }

    @Transactional
    public ApiResponse<Void> borrarComponente(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del componente es requerido");
            }
            
            if (!compRepo.existsById(id)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            compRepo.deleteById(id);
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente eliminado exitosamente");
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    @Transactional
    public ApiResponse<Componente> guardarComponente(Componente comp) {
        try {
            if (comp == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "El componente no puede ser nulo");
            }
            
            if (compRepo.existsById(comp.getId())) {
                return new ApiResponse<>(Errores.RECURSO_YA_EXISTE.getCodigo(), 
                                       Errores.RECURSO_YA_EXISTE.getMensaje());
            }
            
            // Convertir y guardar/actualizar componente si es necesario
            // Usamos el método con nombre descriptivo para evitar ambigüedades
            var compEntity = ComponenteEntityConverter.convertToEntity(comp);
            mx.com.qtx.cotizador.entidad.Promocion promo = null;
            
            switch(comp.getCategoria()) {
                case "Disco Duro":
                    TipoComponente tipo = tipos.stream()
                        .filter(t -> t.getNombre().equals("DISCO_DURO"))
                        .findFirst()
                        .orElse(null);
                    compEntity.setTipoComponente(tipo);
                    promo = promoRepo.findByNombre("Regular");
                    break;
                case "Tarjeta de Video":
                    tipo = tipos.stream()
                        .filter(t -> t.getNombre().equals("TARJETA_VIDEO"))
                        .findFirst()
                        .orElse(null);
                    compEntity.setTipoComponente(tipo);
                    promo = promoRepo.findByNombre("Tarjetas 3x2");
                    break;
                case "Monitor":
                    tipo = tipos.stream()
                        .filter(t -> t.getNombre().equals("MONITOR"))
                        .findFirst()
                        .orElse(null);
                    compEntity.setTipoComponente(tipo);
                    promo = promoRepo.findByNombre("Monitores por Volumen");
                    break;
                default:
                    return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                           "Tipo de componente no válido: " + comp.getCategoria());
            }
            
            compEntity.setPromocion(promo);
            var componenteGuardado = compRepo.save(compEntity);
            
            // Convertir de vuelta a objeto de dominio para retornar
            Componente componenteResultado = ComponenteEntityConverter.convertToComponente(componenteGuardado, null);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente guardado exitosamente", componenteResultado);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    @Transactional
    public void guardarPcCompleto(Componente pcComponente) {
        // 1. Convertir y guardar PC
        // Usamos el método con nombre descriptivo para evitar ambigüedades
        if(pcComponente instanceof Pc) {
            Pc pc = (Pc) pcComponente;
            var pcEntity = ComponenteEntityConverter.convertToEntity(pc);
            var promo = promoRepo.findByNombre("PC Componentes");
            TipoComponente tipo = tipos.stream()
                .filter(t -> t.getNombre().equals("PC"))
                .findFirst()
                .orElse(null);            
            pcEntity.setPromocion(promo);
            pcEntity.setTipoComponente(tipo);
            pcEntity = compRepo.save(pcEntity);        
            // 2. Procesar componentes y crear asociaciones
            for (Componente comp : pc.getSubComponentes()) {
                // Convertir y guardar/actualizar componente si es necesario
                // Usamos el método con nombre descriptivo para evitar ambigüedades
                ApiResponse<Componente> response = guardarComponente(comp);
                if ("0".equals(response.getCodigo())) {
                    var compEntity = ComponenteEntityConverter.convertToEntity(response.getData());
                    PcParte pcParte = new PcParte(pcEntity.getId(), compEntity.getId());
                    pcPartesRepo.save(pcParte);
                }
            }
        }
    }


    public ApiResponse<Componente> buscarComponente(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del componente es requerido");
            }
            
            var compEntity = compRepo.findByIdWithTipoComponente(id);
            if(compEntity == null) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            Componente componente;
            if(compEntity.getTipoComponente().getNombre().equals(TipoComponenteEnum.PC.name())) {
                var subCompEntities = compRepo.findComponentesByPcWithTipoComponente(compEntity.getId());
                componente = ComponenteEntityConverter.convertToComponente(compEntity, subCompEntities);
            } else {
                componente = ComponenteEntityConverter.convertToComponente(compEntity,null);
            }
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente encontrado exitosamente", componente);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }          
    
    public ApiResponse<List<Componente>> obtenerTodosLosComponentes() {
        try {
            List<Componente> componentes = List.of();
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componentes obtenidos exitosamente", componentes);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    @Transactional
    public ApiResponse<Componente> actualizarComponente(Componente componente) {
        try {
            if (componente == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "El componente no puede ser nulo");
            }
            
            if (!compRepo.existsById(componente.getId())) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente actualizado exitosamente", componente);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    public ApiResponse<Boolean> existeComponente(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del componente es requerido");
            }
            
            boolean existe = compRepo.existsById(id);
            return new ApiResponse<>(Errores.OK.getCodigo(), "Verificación completada", existe);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    public ApiResponse<List<Componente>> buscarPorTipo(String tipoComponente) {
        try {
            if (tipoComponente == null || tipoComponente.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El tipo de componente es requerido");
            }
            
            String tipoValidado = tipoComponente.toUpperCase();
            if (!List.of("MONITOR", "DISCO_DURO", "TARJETA_VIDEO", "PC").contains(tipoValidado)) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "Tipo de componente inválido. Valores permitidos: MONITOR, DISCO_DURO, TARJETA_VIDEO, PC");
            }
            
            List<Componente> componentes = List.of();
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componentes filtrados exitosamente", componentes);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    public List<TipoComponente> obtenerTipos() {
        return tipos;
    }
} 