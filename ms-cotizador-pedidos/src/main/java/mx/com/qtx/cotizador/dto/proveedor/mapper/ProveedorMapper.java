package mx.com.qtx.cotizador.dto.proveedor.mapper;

import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorUpdateRequest;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversiones entre DTOs y objetos de dominio de Proveedor
 * 
 * Maneja las transformaciones:
 * - ProveedorCreateRequest → Proveedor (dominio)
 * - ProveedorUpdateRequest + cve → Proveedor (dominio)
 * - Proveedor (dominio) → ProveedorResponse
 */
public class ProveedorMapper {
    
    /**
     * Convierte un ProveedorCreateRequest a objeto de dominio Proveedor
     * 
     * @param request DTO con datos para crear proveedor
     * @return Objeto de dominio Proveedor
     */
    public static Proveedor toProveedor(ProveedorCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return new Proveedor(
            request.getCve(),
            request.getNombre(),
            request.getRazonSocial()
        );
    }
    
    /**
     * Convierte un ProveedorUpdateRequest + cve a objeto de dominio Proveedor
     * 
     * @param cve Clave del proveedor a actualizar
     * @param request DTO con datos actualizados
     * @return Objeto de dominio Proveedor
     */
    public static Proveedor toProveedor(String cve, ProveedorUpdateRequest request) {
        if (request == null || cve == null) {
            return null;
        }
        
        return new Proveedor(
            cve,
            request.getNombre(),
            request.getRazonSocial()
        );
    }
    
    /**
     * Convierte un objeto de dominio Proveedor a ProveedorResponse
     * 
     * @param proveedor Objeto de dominio
     * @return DTO de respuesta
     */
    public static ProveedorResponse toResponse(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }
        
        return ProveedorResponse.builder()
            .cve(proveedor.getCve())
            .nombre(proveedor.getNombre())
            .razonSocial(proveedor.getRazonSocial())
            .numeroPedidos(0) // Por ahora 0, se puede calcular más adelante
            .build();
    }
    
    /**
     * Convierte una lista de objetos de dominio Proveedor a lista de ProveedorResponse
     * 
     * @param proveedores Lista de objetos de dominio
     * @return Lista de DTOs de respuesta
     */
    public static List<ProveedorResponse> toResponseList(List<Proveedor> proveedores) {
        if (proveedores == null) {
            return null;
        }
        
        return proveedores.stream()
            .map(ProveedorMapper::toResponse)
            .collect(Collectors.toList());
    }
} 