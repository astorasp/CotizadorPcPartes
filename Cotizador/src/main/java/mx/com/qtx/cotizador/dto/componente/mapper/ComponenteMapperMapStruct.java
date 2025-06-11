package mx.com.qtx.cotizador.dto.componente.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro;
import mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

/**
 * Mapper usando MapStruct para convertir entre DTOs y objetos del dominio
 * 
 * Ventajas:
 * - Generación automática en tiempo de compilación
 * - Mejor performance (sin reflexión)
 * - Type safety
 * - Menos código boilerplate
 */
@Mapper(componentModel = "spring")  // Para integración con Spring
public interface ComponenteMapperMapStruct {
    
    ComponenteMapperMapStruct INSTANCE = Mappers.getMapper(ComponenteMapperMapStruct.class);
    
    /**
     * Mapeo de ComponenteCreateRequest a Componente
     * Usa método custom para manejar la creación según el tipo
     */
    @Mapping(target = ".", source = ".", qualifiedByName = "createComponenteFromRequest")
    Componente toComponente(ComponenteCreateRequest request);
    
    /**
     * Mapeo de ComponenteUpdateRequest a Componente con ID externo
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = ".", source = "request", qualifiedByName = "createComponenteFromUpdateRequest")
    Componente toComponente(String id, ComponenteUpdateRequest request);
    
    /**
     * Mapeo de Componente a ComponenteResponse
     * MapStruct mapea automáticamente los campos comunes
     */
    @Mapping(target = "tipoComponente", source = ".", qualifiedByName = "getTipoComponente")
    @Mapping(target = "capacidadAlm", source = ".", qualifiedByName = "getCapacidadAlm")
    @Mapping(target = "memoria", source = ".", qualifiedByName = "getMemoria")
    ComponenteResponse toResponse(Componente componente);
    
    // ========================
    // MÉTODOS CUSTOM (Named)
    // ========================
    
    /**
     * Método custom para crear Componente desde CreateRequest
     */
    @Named("createComponenteFromRequest")
    default Componente createComponenteFromRequest(ComponenteCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return switch (request.getTipoComponente().toUpperCase()) {
            case "DISCO_DURO" -> Componente.crearDiscoDuro(
                request.getId(),
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getCapacidadAlm()
            );
            case "TARJETA_VIDEO" -> Componente.crearTarjetaVideo(
                request.getId(),
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getMemoria()
            );
            case "MONITOR" -> Componente.crearMonitor(
                request.getId(),
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase()
            );
            default -> throw new IllegalArgumentException("Tipo de componente no válido: " + request.getTipoComponente());
        };
    }
    
    /**
     * Método custom para crear Componente desde UpdateRequest
     */
    @Named("createComponenteFromUpdateRequest")
    default Componente createComponenteFromUpdateRequest(ComponenteUpdateRequest request, String id) {
        if (request == null) {
            return null;
        }
        
        return switch (request.getTipoComponente().toUpperCase()) {
            case "DISCO_DURO" -> Componente.crearDiscoDuro(
                id,
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getCapacidadAlm()
            );
            case "TARJETA_VIDEO" -> Componente.crearTarjetaVideo(
                id,
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase(),
                request.getMemoria()
            );
            case "MONITOR" -> Componente.crearMonitor(
                id,
                request.getDescripcion(),
                request.getMarca(),
                request.getModelo(),
                request.getCosto(),
                request.getPrecioBase()
            );
            default -> throw new IllegalArgumentException("Tipo de componente no válido: " + request.getTipoComponente());
        };
    }
    
    /**
     * Método custom para obtener el tipo de componente
     */
    @Named("getTipoComponente")
    default String getTipoComponente(Componente componente) {
        if (componente instanceof DiscoDuro) {
            return "DISCO_DURO";
        } else if (componente instanceof TarjetaVideo) {
            return "TARJETA_VIDEO";
        } else {
            return "MONITOR";
        }
    }
    
    /**
     * Método custom para obtener capacidad de almacenamiento
     */
    @Named("getCapacidadAlm")
    default String getCapacidadAlm(Componente componente) {
        return componente instanceof DiscoDuro disco ? disco.getCapacidadAlm() : null;
    }
    
    /**
     * Método custom para obtener memoria
     */
    @Named("getMemoria")
    default String getMemoria(Componente componente) {
        return componente instanceof TarjetaVideo tarjeta ? tarjeta.getMemoria() : null;
    }
} 