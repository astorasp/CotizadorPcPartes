package mx.com.qtx.cotizador.servicio.pedido;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.proveedor.mapper.ProveedorMapper;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorUpdateRequest;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.ProveedorEntityConverter;
import mx.com.qtx.cotizador.util.Errores;

/**
 * Servicio que gestiona las operaciones relacionadas con los proveedores.
 * 
 * Implementa la arquitectura de manejo de errores donde:
 * - Los servicios retornan ApiResponse<T>
 * - Manejo interno de errores con try-catch
 * - Códigos de error específicos del enum Errores
 * - Trabajo con DTOs en la interfaz pública
 */
@Service
public class ProveedorServicio {
    private final ProveedorRepositorio proveedorRepositorio;
    
    public ProveedorServicio(ProveedorRepositorio proveedorRepositorio) {
        this.proveedorRepositorio = proveedorRepositorio;
    }
    
    /**
     * Busca un proveedor por su clave
     * 
     * @param cve La clave del proveedor a buscar
     * @return ApiResponse<ProveedorResponse> con el proveedor encontrado
     */
    public ApiResponse<ProveedorResponse> buscarPorClave(String cve) {
        try {
            if (cve == null || cve.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "La clave del proveedor es requerida");
            }
            
            var proveedorEntity = proveedorRepositorio.findByCve(cve);
            if (proveedorEntity == null) {
                return new ApiResponse<>(Errores.PROVEEDOR_NO_ENCONTRADO.getCodigo(), 
                                       Errores.PROVEEDOR_NO_ENCONTRADO.getMensaje());
            }
            
            // Convertir a dominio y luego a DTO
            Proveedor proveedor = ProveedorEntityConverter.convertToDomain(proveedorEntity);
            ProveedorResponse response = ProveedorMapper.toResponse(proveedor);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Proveedor encontrado", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Crea un nuevo proveedor
     * 
     * @param request DTO con los datos del proveedor a crear
     * @return ApiResponse<ProveedorResponse> con el proveedor creado
     */
    @Transactional
    public ApiResponse<ProveedorResponse> crearProveedor(ProveedorCreateRequest request) {
        try {
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos del proveedor son requeridos");
            }
            
            // Verificar si el proveedor ya existe
            if (proveedorRepositorio.findByCve(request.getCve()) != null) {
                return new ApiResponse<>(Errores.PROVEEDOR_YA_EXISTE.getCodigo(), 
                                       Errores.PROVEEDOR_YA_EXISTE.getMensaje());
            }
            
            // Convertir DTO a dominio
            Proveedor proveedor = ProveedorMapper.toProveedor(request);
            
            // Convertir a entidad y guardar
            var proveedorEntity = ProveedorEntityConverter.convertToNewEntity(proveedor);
            proveedorEntity = proveedorRepositorio.save(proveedorEntity);
            
            // Convertir resultado a DTO
            Proveedor proveedorGuardado = ProveedorEntityConverter.convertToDomain(proveedorEntity);
            ProveedorResponse response = ProveedorMapper.toResponse(proveedorGuardado);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Proveedor creado exitosamente", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Actualiza un proveedor existente
     * 
     * @param cve La clave del proveedor a actualizar
     * @param request DTO con los datos actualizados
     * @return ApiResponse<ProveedorResponse> con el proveedor actualizado
     */
    @Transactional
    public ApiResponse<ProveedorResponse> actualizarProveedor(String cve, ProveedorUpdateRequest request) {
        try {
            if (cve == null || cve.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "La clave del proveedor es requerida");
            }
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos del proveedor son requeridos");
            }
            
            // Verificar que el proveedor exista
            var proveedorEntity = proveedorRepositorio.findByCve(cve);
            if (proveedorEntity == null) {
                return new ApiResponse<>(Errores.PROVEEDOR_NO_ENCONTRADO.getCodigo(), 
                                       Errores.PROVEEDOR_NO_ENCONTRADO.getMensaje());
            }
            
            // Convertir DTO a dominio
            Proveedor proveedor = ProveedorMapper.toProveedor(cve, request);
            
            // Actualizar la entidad con los datos del dominio
            proveedorEntity = ProveedorEntityConverter.convertToEntity(proveedor, proveedorEntity);
            proveedorEntity = proveedorRepositorio.save(proveedorEntity);
            
            // Convertir resultado a DTO
            Proveedor proveedorActualizado = ProveedorEntityConverter.convertToDomain(proveedorEntity);
            ProveedorResponse response = ProveedorMapper.toResponse(proveedorActualizado);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Proveedor actualizado exitosamente", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Elimina un proveedor
     * 
     * @param cve La clave del proveedor a eliminar
     * @return ApiResponse<Void> con el resultado de la operación
     */
    @Transactional
    public ApiResponse<Void> eliminarProveedor(String cve) {
        try {
            if (cve == null || cve.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "La clave del proveedor es requerida");
            }
            
            if (!proveedorRepositorio.existsById(cve)) {
                return new ApiResponse<>(Errores.PROVEEDOR_NO_ENCONTRADO.getCodigo(), 
                                       Errores.PROVEEDOR_NO_ENCONTRADO.getMensaje());
            }
            
            // Verificar si tiene pedidos activos (opcional: implementar lógica de negocio)
            // Por ahora permitimos eliminar sin validaciones adicionales
            
            proveedorRepositorio.deleteById(cve);
            return new ApiResponse<>(Errores.OK.getCodigo(), "Proveedor eliminado exitosamente");
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Obtiene todos los proveedores
     * 
     * @return ApiResponse<List<ProveedorResponse>> con la lista de proveedores
     */
    public ApiResponse<List<ProveedorResponse>> obtenerTodosLosProveedores() {
        try {
            var proveedorEntities = proveedorRepositorio.findAll();
            
            List<ProveedorResponse> proveedores = proveedorEntities.stream()
                .map(entity -> {
                    Proveedor proveedor = ProveedorEntityConverter.convertToDomain(entity);
                    return ProveedorMapper.toResponse(proveedor);
                })
                .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Proveedores obtenidos exitosamente (" + proveedores.size() + " encontrados)", 
                                   proveedores);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Busca proveedores por nombre (búsqueda parcial)
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return ApiResponse<List<ProveedorResponse>> con la lista de proveedores encontrados
     */
    public ApiResponse<List<ProveedorResponse>> buscarPorNombre(String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El nombre a buscar es requerido");
            }
            
            var proveedorEntities = proveedorRepositorio.findByNombreContainingIgnoreCase(nombre.trim());
            
            List<ProveedorResponse> proveedores = proveedorEntities.stream()
                .map(entity -> {
                    Proveedor proveedor = ProveedorEntityConverter.convertToDomain(entity);
                    return ProveedorMapper.toResponse(proveedor);
                })
                .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Búsqueda completada (" + proveedores.size() + " proveedores encontrados)", 
                                   proveedores);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Busca proveedores por razón social (búsqueda parcial)
     * 
     * @param razonSocial Razón social o parte de la razón social a buscar
     * @return ApiResponse<List<ProveedorResponse>> con la lista de proveedores encontrados
     */
    public ApiResponse<List<ProveedorResponse>> buscarPorRazonSocial(String razonSocial) {
        try {
            if (razonSocial == null || razonSocial.trim().isEmpty()) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "La razón social a buscar es requerida");
            }
            
            var proveedorEntities = proveedorRepositorio.findByRazonSocialContainingIgnoreCase(razonSocial.trim());
            
            List<ProveedorResponse> proveedores = proveedorEntities.stream()
                .map(entity -> {
                    Proveedor proveedor = ProveedorEntityConverter.convertToDomain(entity);
                    return ProveedorMapper.toResponse(proveedor);
                })
                .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Búsqueda completada (" + proveedores.size() + " proveedores encontrados)", 
                                   proveedores);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
}