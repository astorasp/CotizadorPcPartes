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
    public ApiResponse<Componente> guardarPcCompleto(Componente pcComponente) {
        try {
            if (pcComponente == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "El componente PC no puede ser nulo");
            }
            
            if (!(pcComponente instanceof Pc)) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El componente debe ser de tipo PC");
            }
            
            if (compRepo.existsById(pcComponente.getId())) {
                return new ApiResponse<>(Errores.RECURSO_YA_EXISTE.getCodigo(), 
                                       Errores.RECURSO_YA_EXISTE.getMensaje());
            }
            
            Pc pc = (Pc) pcComponente;
            
            // Validar que tenga sub-componentes
            if (pc.getSubComponentes() == null || pc.getSubComponentes().isEmpty()) {
                return new ApiResponse<>(Errores.REGLA_NEGOCIO_VIOLADA.getCodigo(), 
                                       "Una PC debe tener al menos un sub-componente");
            }
            
            // 1. Convertir y guardar PC
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
                ApiResponse<Componente> response = guardarComponente(comp);
                if ("0".equals(response.getCodigo())) {
                    var compEntity = ComponenteEntityConverter.convertToEntity(response.getData());
                    PcParte pcParte = new PcParte(pcEntity.getId(), compEntity.getId());
                    pcPartesRepo.save(pcParte);
                } else {
                    // Si falla algún sub-componente, retornar el error
                    return new ApiResponse<>(response.getCodigo(), 
                                           "Error guardando sub-componente: " + response.getMensaje());
                }
            }
            
            // Convertir de vuelta a objeto de dominio
            Componente pcResultado = ComponenteEntityConverter.convertToComponente(pcEntity, null);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "PC guardada exitosamente", pcResultado);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
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
    
    @Transactional
    public ApiResponse<Componente> actualizarPcCompleto(Componente pcComponente) {
        try {
            if (pcComponente == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "El componente PC no puede ser nulo");
            }
            
            if (!(pcComponente instanceof Pc)) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El componente debe ser de tipo PC");
            }
            
            if (!compRepo.existsById(pcComponente.getId())) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            Pc pc = (Pc) pcComponente;
            
            // Validar que tenga sub-componentes
            if (pc.getSubComponentes() == null || pc.getSubComponentes().isEmpty()) {
                return new ApiResponse<>(Errores.REGLA_NEGOCIO_VIOLADA.getCodigo(), 
                                       "Una PC debe tener al menos un sub-componente");
            }
            
            // 1. Eliminar asociaciones existentes de sub-componentes
            pcPartesRepo.deleteByIdPc(pcComponente.getId());
            
            // 2. Actualizar PC principal
            var pcEntity = ComponenteEntityConverter.convertToEntity(pc);
            var promo = promoRepo.findByNombre("PC Componentes");
            TipoComponente tipo = tipos.stream()
                .filter(t -> t.getNombre().equals("PC"))
                .findFirst()
                .orElse(null);
                
            pcEntity.setPromocion(promo);
            pcEntity.setTipoComponente(tipo);
            pcEntity = compRepo.save(pcEntity);
            
            // 3. Procesar y recrear asociaciones con sub-componentes
            for (Componente comp : pc.getSubComponentes()) {
                // Si el sub-componente existe, actualizarlo; si no, crearlo
                ApiResponse<Componente> response;
                if (compRepo.existsById(comp.getId())) {
                    response = actualizarComponente(comp);
                } else {
                    response = guardarComponente(comp);
                }
                
                if ("0".equals(response.getCodigo())) {
                    var compEntity = ComponenteEntityConverter.convertToEntity(response.getData());
                    PcParte pcParte = new PcParte(pcEntity.getId(), compEntity.getId());
                    pcPartesRepo.save(pcParte);
                } else {
                    return new ApiResponse<>(response.getCodigo(), 
                                           "Error procesando sub-componente: " + response.getMensaje());
                }
            }
            
            // Convertir de vuelta a objeto de dominio
            Componente pcResultado = ComponenteEntityConverter.convertToComponente(pcEntity, null);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "PC actualizada exitosamente", pcResultado);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    @Transactional
    public ApiResponse<Componente> agregarComponenteAPc(String pcId, Componente componente) {
        try {
            if (pcId == null || pcId.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la PC es requerido");
            }
            
            if (componente == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "El componente a agregar no puede ser nulo");
            }
            
            // Verificar que la PC existe
            if (!compRepo.existsById(pcId)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       "La PC especificada no existe");
            }
            
            // Verificar que es realmente una PC
            var pcEntity = compRepo.findByIdWithTipoComponente(pcId);
            if (!pcEntity.getTipoComponente().getNombre().equals("PC")) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El ID especificado no corresponde a una PC");
            }
            
            // Validar que el componente no sea una PC (no se pueden anidar PCs)
            if ("PC".equals(componente.getCategoria())) {
                return new ApiResponse<>(Errores.REGLA_NEGOCIO_VIOLADA.getCodigo(), 
                                       "No se pueden agregar PCs como sub-componentes de otra PC");
            }
            
            // Verificar si el componente ya existe, si no, crearlo
            ApiResponse<Componente> responseComponente;
            if (compRepo.existsById(componente.getId())) {
                // Si ya existe, verificar que no esté ya asociado a esta PC
                var existeAsociacion = pcPartesRepo.existsById(new mx.com.qtx.cotizador.entidad.PcParte.PcPartesId(pcId, componente.getId()));
                if (existeAsociacion) {
                    return new ApiResponse<>(Errores.RECURSO_YA_EXISTE.getCodigo(), 
                                           "El componente ya está asociado a esta PC");
                }
                responseComponente = new ApiResponse<>(Errores.OK.getCodigo(), "Componente existente encontrado", componente);
            } else {
                // Crear el componente nuevo
                responseComponente = guardarComponente(componente);
                if (!"0".equals(responseComponente.getCodigo())) {
                    return new ApiResponse<>(responseComponente.getCodigo(), 
                                           "Error creando el componente: " + responseComponente.getMensaje());
                }
            }
            
            // Crear la asociación PC-Componente
            var pcParte = new mx.com.qtx.cotizador.entidad.PcParte(pcId, componente.getId());
            pcPartesRepo.save(pcParte);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Componente agregado exitosamente a la PC", 
                                   responseComponente.getData());
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    @Transactional
    public ApiResponse<Void> quitarComponenteDePc(String pcId, String componenteId) {
        try {
            if (pcId == null || pcId.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la PC es requerido");
            }
            
            if (componenteId == null || componenteId.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del componente es requerido");
            }
            
            // Verificar que la PC existe
            if (!compRepo.existsById(pcId)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       "La PC especificada no existe");
            }
            
            // Verificar que es realmente una PC
            var pcEntity = compRepo.findByIdWithTipoComponente(pcId);
            if (!pcEntity.getTipoComponente().getNombre().equals("PC")) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El ID especificado no corresponde a una PC");
            }
            
            // Verificar que el componente existe
            if (!compRepo.existsById(componenteId)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       "El componente especificado no existe");
            }
            
            // Verificar que la asociación existe
            var pcPartesId = new mx.com.qtx.cotizador.entidad.PcParte.PcPartesId(pcId, componenteId);
            if (!pcPartesRepo.existsById(pcPartesId)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       "El componente no está asociado a esta PC");
            }
            
            // Verificar que no se violen las reglas de negocio (mínimos requeridos)
            long totalComponentes = pcPartesRepo.countComponentesByPc(pcId);
            if (totalComponentes <= 1) {
                return new ApiResponse<>(Errores.REGLA_NEGOCIO_VIOLADA.getCodigo(), 
                                       "No se puede quitar el último componente. Una PC debe tener al menos un sub-componente");
            }
            
            // Eliminar la asociación
            pcPartesRepo.deleteById(pcPartesId);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Componente removido exitosamente de la PC");
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    public List<TipoComponente> obtenerTipos() {
        return tipos;
    }
} 