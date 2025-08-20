package mx.com.qtx.cotizador.servicio.componente;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.Pc;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.mapper.ComponenteMapper;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.dto.pc.response.PcResponse;
import mx.com.qtx.cotizador.dto.pc.mapper.PcMapper;
import mx.com.qtx.cotizador.dto.pc.request.PcCreateRequest;
import mx.com.qtx.cotizador.dto.pc.request.PcUpdateRequest;
import mx.com.qtx.cotizador.dto.pc.request.AgregarComponenteRequest;
import mx.com.qtx.cotizador.entidad.PcParte;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PcPartesRepositorio;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.ComponenteEntityConverter;
import mx.com.qtx.cotizador.util.Errores;
import mx.com.qtx.cotizador.util.TipoComponenteEnum;
import mx.com.qtx.cotizador.kafka.service.EventPublishingService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ComponenteServicio {
    
    private ComponenteRepositorio compRepo;
    private PcPartesRepositorio pcPartesRepo;  
    private PromocionRepositorio promoRepo;
    private List<TipoComponente> tipos;
    private EventPublishingService eventPublishingService;
    
    public ComponenteServicio(ComponenteRepositorio compRepo, 
        PcPartesRepositorio pcPartesRepo,
        PromocionRepositorio promoRepo,
        TipoComponenteRepositorio tipoRepo,
        EventPublishingService eventPublishingService) {
        this.compRepo = compRepo;
        this.pcPartesRepo = pcPartesRepo;
        this.promoRepo = promoRepo;
        this.tipos = tipoRepo.findAll();
        this.eventPublishingService = eventPublishingService;
    }

    /**
     * Elimina un componente por ID
     * @param id ID del componente a eliminar
     * @return ApiResponse<Void> con el resultado de la operación
     */
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
            
            // Publicar evento de eliminación de componente
            eventPublishingService.publishComponenteDeleted(id);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente eliminado exitosamente");
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Guarda un nuevo componente
     * @param request DTO con los datos del componente a crear
     * @return ApiResponse<ComponenteResponse> con el componente creado
     */
    @Transactional
    public ApiResponse<ComponenteResponse> guardarComponente(ComponenteCreateRequest request) {
        try {
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos del componente son requeridos");
            }
            
            if (compRepo.existsById(request.getId())) {
                return new ApiResponse<>(Errores.RECURSO_YA_EXISTE.getCodigo(), 
                                       Errores.RECURSO_YA_EXISTE.getMensaje());
            }
            
            // Mapear DTO a objeto de dominio
            Componente componente = ComponenteMapper.toComponente(request);
            
            // Convertir y guardar componente
            var compEntity = ComponenteEntityConverter.convertToEntity(componente);
            mx.com.qtx.cotizador.entidad.Promocion promo = null;
            
            switch(componente.getCategoria()) {
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
                                           "Tipo de componente no válido: " + componente.getCategoria());
            }
            
            compEntity.setPromocion(promo);
            var componenteGuardado = compRepo.save(compEntity);
            
            // Convertir de vuelta a objeto de dominio y luego a DTO de respuesta
            Componente componenteResultado = ComponenteEntityConverter.convertToComponente(componenteGuardado, null);
            ComponenteResponse response = ComponenteMapper.toResponse(componenteResultado);
            
            // Publicar evento de creación de componente
            eventPublishingService.publishComponenteCreated(componenteGuardado);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente guardado exitosamente", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Actualiza un componente existente
     * @param id ID del componente a actualizar
     * @param request DTO con los nuevos datos del componente
     * @return ApiResponse<ComponenteResponse> con el componente actualizado
     */
    @Transactional
    public ApiResponse<ComponenteResponse> actualizarComponente(String id, ComponenteUpdateRequest request) {
        try {
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos del componente son requeridos");
            }
            
            if (id == null || id.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del componente es requerido");
            }
            
            if (!compRepo.existsById(id)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            // Mapear DTO a objeto de dominio
            Componente componente = ComponenteMapper.toComponente(id, request);
            
            // Convertir y actualizar componente
            var compEntity = ComponenteEntityConverter.convertToEntity(componente);
            mx.com.qtx.cotizador.entidad.Promocion promo = null;
            
            switch(componente.getCategoria()) {
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
                                           "Tipo de componente no válido: " + componente.getCategoria());
            }
            
            compEntity.setPromocion(promo);
            var componenteActualizado = compRepo.save(compEntity);
            
            // Convertir de vuelta a objeto de dominio y luego a DTO de respuesta
            Componente componenteResultado = ComponenteEntityConverter.convertToComponente(componenteActualizado, null);
            ComponenteResponse response = ComponenteMapper.toResponse(componenteResultado);
            
            // Publicar evento de actualización de componente
            eventPublishingService.publishComponenteUpdated(componenteActualizado);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente actualizado exitosamente", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca un componente por ID
     * @param id ID del componente a buscar
     * @return ApiResponse<ComponenteResponse> con el componente encontrado
     */
    public ApiResponse<ComponenteResponse> buscarComponente(String id) {
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
                componente = ComponenteEntityConverter.convertToComponente(compEntity, null);
            }
            
            // Convertir a DTO de respuesta
            ComponenteResponse response = ComponenteMapper.toResponse(componente);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente encontrado", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca una PC completa por ID con sus sub-componentes cargados
     * Método especializado para el controlador de PCs
     * @param pcId ID de la PC a buscar
     * @return ApiResponse<PcResponse> con la PC encontrada y sus sub-componentes
     */
    public ApiResponse<PcResponse> buscarPcCompleto(String pcId) {
        try {
            if (pcId == null || pcId.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la PC es requerido");
            }
            
            var compEntity = compRepo.findByIdWithTipoComponente(pcId);
            if(compEntity == null) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            // Verificar que el componente sea de tipo PC
            if(!compEntity.getTipoComponente().getNombre().equals(TipoComponenteEnum.PC.name())) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El componente especificado no es una PC");
            }
            
            // Cargar los sub-componentes de la PC
            var subCompEntities = compRepo.findComponentesByPcWithTipoComponente(compEntity.getId());
            Componente componente = ComponenteEntityConverter.convertToComponente(compEntity, subCompEntities);
            
            // Verificar que se convirtió correctamente a PC
            if(!(componente instanceof Pc)) {
                return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                       "Error interno: No se pudo convertir el componente a PC");
            }
            
            Pc pc = (Pc) componente;
            PcResponse pcResponse = PcMapper.toResponse(pc);
            return new ApiResponse<>(Errores.OK.getCodigo(), "PC encontrada con " + pc.getSubComponentes().size() + " sub-componentes", pcResponse);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Obtiene todos los componentes
     * @return ApiResponse<List<ComponenteResponse>> con la lista de componentes
     */
    public ApiResponse<List<ComponenteResponse>> obtenerTodosLosComponentes() {
        try {
            var compEntities = compRepo.findAllWithTipoComponente()
                .stream()
                .filter(entity -> !TipoComponenteEnum.PC.name().equals(entity.getTipoComponente().getNombre()))
                .collect(Collectors.toList());
            List<ComponenteResponse> componentes = compEntities.stream()
                .map(entity -> {
                    Componente componente = ComponenteEntityConverter.convertToComponente(entity, null);
                    return ComponenteMapper.toResponse(componente);
                })
                .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Consulta exitosa", componentes);
        } catch (Exception e) {
            log.error("Error al obtener todos los componentes: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca componentes por tipo
     * @param tipoComponente Tipo de componente a buscar
     * @return ApiResponse<List<ComponenteResponse>> con la lista de componentes del tipo especificado
     */
    public ApiResponse<List<ComponenteResponse>> buscarPorTipo(String tipoComponente) {
        try {
            if (tipoComponente == null || tipoComponente.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El tipo de componente es requerido");
            }

            var compEntities = compRepo.findByTipoComponenteNombre(tipoComponente.toUpperCase());
            List<ComponenteResponse> componentes = compEntities.stream()
                .map(entity -> {
                    Componente componente;
                    // Si es una PC, cargar sus sub-componentes
                    if (entity.getTipoComponente().getNombre().equals(TipoComponenteEnum.PC.name())) {
                        var subCompEntities = compRepo.findComponentesByPcWithTipoComponente(entity.getId());
                        componente = ComponenteEntityConverter.convertToComponente(entity, subCompEntities);
                    } else {
                        componente = ComponenteEntityConverter.convertToComponente(entity, null);
                    }
                    return ComponenteMapper.toResponse(componente);
                })
                .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Componentes del tipo " + tipoComponente + " obtenidos exitosamente", 
                                   componentes);
        } catch (Exception e) {
            log.error("Error en buscarPorTipo para tipo {}: {}", tipoComponente, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Verifica si existe un componente por ID
     * @param id ID del componente a verificar
     * @return ApiResponse<Boolean> indicando si el componente existe
     */
    public ApiResponse<Boolean> existeComponente(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del componente es requerido");
            }
            
            boolean existe = compRepo.existsById(id);
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   existe ? "El componente existe" : "El componente no existe",
                                   existe);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    // ==================== MÉTODOS PARA MANEJO DE PCs ====================
    // Los métodos relacionados con PC se mantienen temporalmente usando objetos de dominio
    // hasta que se definan los DTOs específicos para PC

    @Transactional
    public ApiResponse<PcResponse> guardarPcCompleto(PcCreateRequest request) {
        try {
            log.debug("guardarPcCompleto: Iniciando con request: {}", request);
            
            if (request == null) {
                log.debug("guardarPcCompleto: Request nulo");
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos de la PC son requeridos");
            }
            
            log.debug("guardarPcCompleto: Verificando si ID {} ya existe", request.getId());
            if (compRepo.existsById(request.getId())) {
                log.debug("guardarPcCompleto: ID {} ya existe", request.getId());
                return new ApiResponse<>(Errores.RECURSO_YA_EXISTE.getCodigo(), 
                                       Errores.RECURSO_YA_EXISTE.getMensaje());
            }
            
            // Convertir DTO a objeto de dominio
            Pc pc = PcMapper.toPc(request);
            
            // Validar que tenga sub-componentes
            if (pc.getSubComponentes() == null || pc.getSubComponentes().isEmpty()) {
                return new ApiResponse<>(Errores.REGLA_NEGOCIO_VIOLADA.getCodigo(), 
                                       "Una PC debe tener al menos un sub-componente");
            }
            
            // 1. Convertir y guardar PC
            var pcEntity = ComponenteEntityConverter.convertToEntity(pc);
            
            // Buscar promoción PC Componentes
            var promo = promoRepo.findByNombre("PC Componentes");
            if (promo == null) {
                // Fallback a promoción Regular si PC Componentes no existe
                promo = promoRepo.findByNombre("Regular");
            }
            
            // Buscar tipo PC - validar que existe
            TipoComponente tipo = tipos.stream()
                .filter(t -> t.getNombre().equals("PC"))
                .findFirst()
                .orElse(null);
                
            if (tipo == null) {
                return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                       "Tipo de componente PC no encontrado en el sistema");
            }
                
            pcEntity.setPromocion(promo);
            pcEntity.setTipoComponente(tipo);
            pcEntity = compRepo.save(pcEntity);  
            
            // 2. Procesar componentes y crear asociaciones
            for (Componente comp : pc.getSubComponentes()) {
                if (!compRepo.existsById(comp.getId())) {
                    return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(),
                                            "El componente " + comp.getId() + " no existe en el inventario");
                }

                // Solo crear la asociación PC-Componente
                PcParte pcParte = new PcParte(pcEntity.getId(), comp.getId());
                pcPartesRepo.save(pcParte);
            }                                

            // Obtener la PC completa con sus componentes asociados y convertir a DTO
            ApiResponse<PcResponse> pcCompleta = buscarPcCompleto(pcEntity.getId());
            if (!"0".equals(pcCompleta.getCodigo())) {
                return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                       "Error al recuperar la PC guardada");
            }
            PcResponse pcResponse = pcCompleta.getDatos();
            
            // Publicar evento de creación de PC
            eventPublishingService.publishPcCreated(pcEntity.getId(), pc.getId(), "PC completa", 
                                                   1000.0, true);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "PC guardada exitosamente", pcResponse);
        } catch (Exception e) {
            log.error("Error al guardar PC completa: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Mapea las categorías de componentes a los tipos válidos del sistema
     * @param categoria Categoría del componente (ej: "Monitor", "Disco Duro", etc.)
     * @return Tipo de componente mapeado para el enum
     */
    private String mapearCategoriaATipo(String categoria) {
        if (categoria == null) {
            return "MONITOR"; // Valor por defecto
        }
        
        switch (categoria.toUpperCase().trim()) {
            case "DISCO DURO":
            case "DISCO_DURO":
            case "HDD":
            case "SSD":
                return "DISCO_DURO";
            case "TARJETA DE VIDEO":
            case "TARJETA_VIDEO": 
            case "GPU":
                return "TARJETA_VIDEO";
            case "MONITOR":
                return "MONITOR";
            case "PC":
                return "PC";
            default:
                return "MONITOR"; // Valor por defecto para tipos no reconocidos
        }
    }

    @Transactional
    public ApiResponse<PcResponse> actualizarPcCompleto(String id, PcUpdateRequest request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la PC es requerido");
            }
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos de la PC son requeridos");
            }
            
            if (!compRepo.existsById(id)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            // Convertir DTO a objeto de dominio
            Pc pc = PcMapper.toPc(id, request);
            
            // 1. Actualizar datos básicos de la PC
            var pcEntity = ComponenteEntityConverter.convertToEntity(pc);
            var promo = promoRepo.findByNombre("PC Componentes");
            TipoComponente tipo = tipos.stream()
                .filter(t -> t.getNombre().equals("PC"))
                .findFirst()
                .orElse(null);
                
            pcEntity.setPromocion(promo);
            pcEntity.setTipoComponente(tipo);
            pcEntity = compRepo.save(pcEntity);
            
            // 2. Eliminar asociaciones existentes
            pcPartesRepo.deleteByPcId(pc.getId());
            
            // 3. Agregar nuevas asociaciones si hay sub-componentes
            if (pc.getSubComponentes() != null && !pc.getSubComponentes().isEmpty()) {
                for (Componente comp : pc.getSubComponentes()) {
                    if (compRepo.existsById(comp.getId())) {
                        PcParte pcParte = new PcParte(pcEntity.getId(), comp.getId());
                        pcPartesRepo.save(pcParte);
                    }
                }
            }
            
            // Convertir de vuelta a objeto de dominio y luego a DTO
            Componente pcResultado = ComponenteEntityConverter.convertToComponente(pcEntity, null);
            PcResponse pcResponse = PcMapper.toResponse((Pc) pcResultado);
            
            // Publicar evento de actualización de PC
            eventPublishingService.publishPcUpdated(id, pc.getId(), "PC completa", 
                                                   1000.0, true);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "PC actualizada exitosamente", pcResponse);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    @Transactional
    public ApiResponse<ComponenteResponse> agregarComponenteAPc(String pcId, AgregarComponenteRequest request) {
        try {
            if (pcId == null || pcId.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la PC es requerido");
            }
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos del componente son requeridos");
            }
            
            // Verificar que la PC existe
            var pcEntity = compRepo.findByIdWithTipoComponente(pcId);
            if (pcEntity == null || !pcEntity.getTipoComponente().getNombre().equals("PC")) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       "PC no encontrada");
            }
            
            // Verificar si el componente ya existe
            boolean componenteExiste = compRepo.existsById(request.getId());
            ComponenteResponse componenteResponse;
            
            if (componenteExiste) {
                // Caso 1: El componente ya existe, solo crear la asociación
                
                // Verificar que no esté ya asociado
                if (pcPartesRepo.existsByPcIdAndComponenteId(pcId, request.getId())) {
                    return new ApiResponse<>(Errores.RECURSO_YA_EXISTE.getCodigo(), 
                                           "El componente ya está asociado a esta PC");
                }
                
                // Crear la asociación
                PcParte pcParte = new PcParte(pcId, request.getId());
                pcPartesRepo.save(pcParte);
                
                // Obtener el componente existente y convertir a DTO
                var componenteEntity = compRepo.findByIdWithTipoComponente(request.getId());
                Componente componenteResultado = ComponenteEntityConverter.convertToComponente(componenteEntity, null);
                componenteResponse = ComponenteMapper.toResponse(componenteResultado);
                
            } else {
                // Caso 2: El componente no existe, crearlo y luego asociarlo
                
                // Convertir AgregarComponenteRequest a ComponenteCreateRequest
                ComponenteCreateRequest crearRequest = ComponenteCreateRequest.builder()
                    .id(request.getId())
                    .descripcion(request.getDescripcion())
                    .marca(request.getMarca())
                    .modelo(request.getModelo())
                    .costo(request.getCosto())
                    .precioBase(request.getPrecioBase())
                    .tipoComponente(request.getTipoComponente())
                    .capacidadAlm(request.getCapacidadAlm())
                    .memoria(request.getMemoria())
                    .build();
                
                // Crear el componente usando el método existente
                ApiResponse<ComponenteResponse> crearResponse = guardarComponente(crearRequest);
                if (!"0".equals(crearResponse.getCodigo())) {
                    return new ApiResponse<>(crearResponse.getCodigo(), 
                                           "Error creando componente: " + crearResponse.getMensaje());
                }
                
                // Crear la asociación
                PcParte pcParte = new PcParte(pcId, request.getId());
                pcPartesRepo.save(pcParte);
                
                componenteResponse = crearResponse.getDatos();
            }
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente agregado a la PC exitosamente", componenteResponse);
        } catch (Exception e) {
            log.error("Error al agregar componente a PC: {}", e.getMessage(), e);
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
            
            // Verificar que la asociación existe
            if (!pcPartesRepo.existsByPcIdAndComponenteId(pcId, componenteId)) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       "La asociación entre la PC y el componente no existe");
            }
            
            // Eliminar la asociación
            pcPartesRepo.deleteByPcIdAndComponenteId(pcId, componenteId);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Componente removido de la PC exitosamente");
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    // Método utilitario que se mantiene para compatibilidad
    public List<TipoComponente> obtenerTipos() {
        return tipos;
    }

    /**
     * Elimina una PC completa incluyendo todas sus asociaciones
     * Maneja las foreign keys eliminando primero las asociaciones y luego la PC
     * @param pcId ID de la PC a eliminar
     * @return ApiResponse<Void> con el resultado de la operación
     */
    @Transactional
    public ApiResponse<Void> eliminarPcCompleta(String pcId) {
        try {
            if (pcId == null || pcId.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la PC es requerido");
            }
            
            // Verificar que la PC existe y es de tipo PC
            var pcEntity = compRepo.findByIdWithTipoComponente(pcId);
            if (pcEntity == null) {
                return new ApiResponse<>(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), 
                                       Errores.RECURSO_NO_ENCONTRADO.getMensaje());
            }
            
            if (!pcEntity.getTipoComponente().getNombre().equals(TipoComponenteEnum.PC.name())) {
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El componente especificado no es una PC");
            }
            
            // 1. Eliminar todas las asociaciones de la PC con sus componentes
            pcPartesRepo.deleteByPcId(pcId);
            
            // 2. Eliminar la PC
            compRepo.deleteById(pcId);
            
            // Publicar evento de eliminación de PC
            eventPublishingService.publishPcDeleted(pcId);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "PC eliminada exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar PC completa: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
} 