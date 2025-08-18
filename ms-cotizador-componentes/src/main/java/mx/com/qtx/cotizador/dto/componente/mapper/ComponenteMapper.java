package mx.com.qtx.cotizador.dto.componente.mapper;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro;
import mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

/**
 * Mapper para convertir entre DTOs y objetos del dominio de componentes
 */
public class ComponenteMapper {
    
    /**
     * Convierte un ComponenteCreateRequest a un objeto del dominio Componente
     */
    public static Componente toComponente(ComponenteCreateRequest request) {
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
     * Convierte un ComponenteUpdateRequest a un objeto del dominio Componente
     * (necesita el ID desde el parámetro de la URL)
     */
    public static Componente toComponente(String id, ComponenteUpdateRequest request) {
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
     * Convierte un objeto del dominio Componente a ComponenteResponse
     */
    public static ComponenteResponse toResponse(Componente componente) {
        if (componente == null) {
            return null;
        }
        
        ComponenteResponse.ComponenteResponseBuilder builder = ComponenteResponse.builder()
                .id(componente.getId())
                .descripcion(componente.getDescripcion())
                .marca(componente.getMarca())
                .modelo(componente.getModelo())
                .costo(componente.getCosto())
                .precioBase(componente.getPrecioBase());
        
        // Determinar el tipo y campos específicos
        if (componente instanceof DiscoDuro disco) {
            builder.tipoComponente("DISCO_DURO")
                   .capacidadAlm(disco.getCapacidadAlm());
        } else if (componente instanceof TarjetaVideo tarjeta) {
            builder.tipoComponente("TARJETA_VIDEO")
                   .memoria(tarjeta.getMemoria());
        } else {
            builder.tipoComponente("MONITOR");
        }
        
        return builder.build();
    }
} 