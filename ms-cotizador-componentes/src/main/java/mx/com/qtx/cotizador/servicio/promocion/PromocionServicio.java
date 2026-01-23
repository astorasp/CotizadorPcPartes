package mx.com.qtx.cotizador.servicio.promocion;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.promocion.mapper.PromocionMapper;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionUpdateRequest;
import mx.com.qtx.cotizador.dto.promocion.response.PromocionResponse;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.servicio.wrapper.PromocionEntityConverter;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.util.Errores;
/**
 * Servicio que gestiona las operaciones relacionadas con las promociones.
 * Implementa la arquitectura ApiResponse<T> con manejo de errores completo.
 */
@Service
public class PromocionServicio {
    
    private static final Logger logger = LoggerFactory.getLogger(PromocionServicio.class);
    private final PromocionRepositorio promocionRepositorio;
    
    public PromocionServicio(PromocionRepositorio promocionRepositorio) {
        this.promocionRepositorio = promocionRepositorio;
    }
    
    /**
     * Crea una nueva promoción (Caso 6.1: Agregar promoción)
     */
    @Transactional
    public ApiResponse<PromocionResponse> crearPromocion(PromocionCreateRequest request) {
        try {
            logger.info("Iniciando creación de promoción: {}", request);
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos de la promoción son requeridos");
            }
            
            if (!request.esValida()) {
                return new ApiResponse<>(Errores.PROMOCION_SIN_DETALLES.getCodigo(), 
                                       "Los datos de la promoción no son válidos");
            }
            
            if (promocionRepositorio.findByNombre(request.getNombre()) != null) {
                return new ApiResponse<>(Errores.PROMOCION_YA_EXISTE.getCodigo(), 
                                       "Ya existe una promoción con el nombre: " + request.getNombre());
            }
            
            // 1) Convertir DTO → Dominio
            mx.com.qtx.cotizador.dominio.promos.Promocion promDominio = PromocionMapper.toDominio(request);

            // 2) Convertir Dominio → Entidad y setear vigencias desde el DTO
            Promocion entidad = PromocionEntityConverter.convertToEntity(promDominio);
            entidad.setVigenciaDesde(request.getVigenciaDesde());
            entidad.setVigenciaHasta(request.getVigenciaHasta());

            // 3) Persistir
            Promocion promocionGuardada = promocionRepositorio.save(entidad);
            mx.com.qtx.cotizador.dominio.promos.Promocion domGuardada = PromocionEntityConverter.convertToPromocion(promocionGuardada);
            // Pasar nombres de detalles del request para respetar nombre del detalle base
            java.util.List<String> nombres = request.getDetalles() != null ?
                request.getDetalles().stream().map(d -> d.getNombre()).collect(java.util.stream.Collectors.toList()) : null;
            PromocionResponse response = PromocionMapper.toResponse(domGuardada,
                promocionGuardada.getIdPromocion(), promocionGuardada.getVigenciaDesde(), promocionGuardada.getVigenciaHasta(), nombres);
            
            logger.info("Promoción creada exitosamente: ID={}, Nombre={}", 
                       promocionGuardada.getIdPromocion(), promocionGuardada.getNombre());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Promoción creada exitosamente", response);
                                   
        } catch (Exception e) {
            logger.error("Error al crear promoción: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error interno al crear promoción: " + e.getMessage());
        }
    }
    
    /**
     * Busca una promoción por su ID (Caso 6.3: Consultar promoción)
     */
    public ApiResponse<PromocionResponse> buscarPorId(Integer id) {
        try {
            if (id == null) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la promoción es requerido");
            }
            
            Promocion promocion = promocionRepositorio.findById(id).orElse(null);
            if (promocion == null) {
                return new ApiResponse<>(Errores.PROMOCION_NO_ENCONTRADA.getCodigo(), 
                                       "Promoción no encontrada con ID: " + id);
            }
            
            // Entidad → Dominio → Response
            mx.com.qtx.cotizador.dominio.promos.Promocion dom = PromocionEntityConverter.convertToPromocion(promocion);
            PromocionResponse response = PromocionMapper.toResponse(dom,
                promocion.getIdPromocion(), promocion.getVigenciaDesde(), promocion.getVigenciaHasta());
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Promoción encontrada exitosamente", response);
                                   
        } catch (Exception e) {
            logger.error("Error al buscar promoción ID={}: {}", id, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error interno al buscar promoción: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todas las promociones (Caso 6.3: Consultar promociones)
     */
    public ApiResponse<List<PromocionResponse>> obtenerTodasLasPromociones() {
        try {
            List<Promocion> promociones = promocionRepositorio.findAll();
            List<PromocionResponse> responses = promociones.stream()
                .map(p -> {
                    var dom = PromocionEntityConverter.convertToPromocion(p);
                    return PromocionMapper.toResponse(dom, p.getIdPromocion(), p.getVigenciaDesde(), p.getVigenciaHasta());
                })
                .collect(java.util.stream.Collectors.toList());
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Promociones obtenidas exitosamente", responses);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error interno al obtener promociones: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza una promoción existente (Caso 6.2: Modificar promoción)
     */
    @Transactional
    public ApiResponse<PromocionResponse> actualizarPromocion(Integer id, PromocionUpdateRequest request) {
        try {
            if (id == null) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la promoción es requerido");
            }
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos de actualización son requeridos");
            }
            
            Promocion promocionExistente = promocionRepositorio.findById(id).orElse(null);
            if (promocionExistente == null) {
                return new ApiResponse<>(Errores.PROMOCION_NO_ENCONTRADA.getCodigo(), 
                                       "Promoción no encontrada con ID: " + id);
            }
            
            if (!request.esValida()) {
                return new ApiResponse<>(Errores.PROMOCION_SIN_DETALLES.getCodigo(), 
                                       "Los datos de actualización no son válidos");
            }
            
            // Validar que el nombre no exista en otra promoción
            Promocion promocionConMismoNombre = promocionRepositorio.findByNombre(request.getNombre());
            if (promocionConMismoNombre != null && !promocionConMismoNombre.getIdPromocion().equals(id)) {
                return new ApiResponse<>(Errores.PROMOCION_YA_EXISTE.getCodigo(), 
                                       "Ya existe otra promoción con el nombre: " + request.getNombre());
            }
            
            // 1) DTO → Dominio
            mx.com.qtx.cotizador.dominio.promos.Promocion promDominio = PromocionMapper.toDominio(request);

            // 2) Dominio → Entidad (merge en existente) y setear vigencias
            Promocion entidadActualizada = PromocionEntityConverter.mergeIntoEntity(promDominio, promocionExistente);
            entidadActualizada.setVigenciaDesde(request.getVigenciaDesde());
            entidadActualizada.setVigenciaHasta(request.getVigenciaHasta());

            // 3) Persistir
            Promocion promocionGuardada = promocionRepositorio.save(entidadActualizada);
            mx.com.qtx.cotizador.dominio.promos.Promocion domGuardada = PromocionEntityConverter.convertToPromocion(promocionGuardada);
            java.util.List<String> nombres = request.getDetalles() != null ?
                request.getDetalles().stream().map(d -> d.getNombre()).collect(java.util.stream.Collectors.toList()) : null;
            PromocionResponse response = PromocionMapper.toResponse(domGuardada,
                promocionGuardada.getIdPromocion(), promocionGuardada.getVigenciaDesde(), promocionGuardada.getVigenciaHasta(), nombres);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Promoción actualizada exitosamente", response);
                                   
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error interno al actualizar promoción: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una promoción (Caso 6.4: Eliminar promoción)
     */
    @Transactional
    public ApiResponse<Void> eliminarPromocion(Integer id) {
        try {
            if (id == null) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID de la promoción es requerido");
            }
            
            Promocion promocion = promocionRepositorio.findById(id).orElse(null);
            if (promocion == null) {
                return new ApiResponse<>(Errores.PROMOCION_NO_ENCONTRADA.getCodigo(), 
                                       "Promoción no encontrada con ID: " + id);
            }
            
            promocionRepositorio.delete(promocion);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Promoción eliminada exitosamente");
                                   
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error interno al eliminar promoción: " + e.getMessage());
        }
    }
} 