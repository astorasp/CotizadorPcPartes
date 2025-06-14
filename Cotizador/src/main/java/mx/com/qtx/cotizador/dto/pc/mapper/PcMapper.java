package mx.com.qtx.cotizador.dto.pc.mapper;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.Pc;
import mx.com.qtx.cotizador.dominio.core.componentes.PcBuilder;
import mx.com.qtx.cotizador.dto.componente.mapper.ComponenteMapper;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.dto.pc.request.PcCreateRequest;
import mx.com.qtx.cotizador.dto.pc.request.PcUpdateRequest;
import mx.com.qtx.cotizador.dto.pc.request.AgregarComponenteRequest;
import mx.com.qtx.cotizador.dto.pc.response.PcResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre DTOs de PC y objetos del dominio
 */
public class PcMapper {
    
    /**
     * Convierte un PcCreateRequest a un objeto Pc del dominio
     */
    public static Pc toPc(PcCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        // Usar PcBuilder para crear la PC
        PcBuilder builder = Componente.getPcBuilder()
                .definirId(request.getId())
                .definirDescripcion(request.getDescripcion() != null ? request.getDescripcion() : request.getNombre())
                .definirMarcaYmodelo(request.getMarca(), request.getModelo());
        
        // Agregar sub-componentes según su tipo
        for (var subRequest : request.getSubComponentes()) {
            Componente comp = ComponenteMapper.toComponente(subRequest);
            
            switch (subRequest.getTipoComponente().toUpperCase()) {
                case "DISCO_DURO" -> builder.agregarDisco(
                        comp.getId(), comp.getDescripcion(), comp.getMarca(), 
                        comp.getModelo(), comp.getCosto(), comp.getPrecioBase(),
                        subRequest.getCapacidadAlm()
                );
                case "MONITOR" -> builder.agregarMonitor(
                        comp.getId(), comp.getDescripcion(), comp.getMarca(),
                        comp.getModelo(), comp.getCosto(), comp.getPrecioBase()
                );
                case "TARJETA_VIDEO" -> builder.agregarTarjetaVideo(
                        comp.getId(), comp.getDescripcion(), comp.getMarca(),
                        comp.getModelo(), comp.getCosto(), comp.getPrecioBase(),
                        subRequest.getMemoria()
                );
            }
        }
        
        return builder.build();
    }
    
    /**
     * Convierte un PcUpdateRequest a un objeto Pc del dominio
     */
    public static Pc toPc(String id, PcUpdateRequest request) {
        if (request == null) {
            return null;
        }
        
        // Usar PcBuilder para crear la PC
        PcBuilder builder = Componente.getPcBuilder()
                .definirId(id)
                .definirDescripcion(request.getDescripcion() != null ? request.getDescripcion() : request.getNombre())
                .definirMarcaYmodelo(request.getMarca(), request.getModelo());
        
        // Agregar sub-componentes según su tipo
        for (var subRequest : request.getSubComponentes()) {
            Componente comp = ComponenteMapper.toComponente(subRequest);
            
            switch (subRequest.getTipoComponente().toUpperCase()) {
                case "DISCO_DURO" -> builder.agregarDisco(
                        comp.getId(), comp.getDescripcion(), comp.getMarca(), 
                        comp.getModelo(), comp.getCosto(), comp.getPrecioBase(),
                        subRequest.getCapacidadAlm()
                );
                case "MONITOR" -> builder.agregarMonitor(
                        comp.getId(), comp.getDescripcion(), comp.getMarca(),
                        comp.getModelo(), comp.getCosto(), comp.getPrecioBase()
                );
                case "TARJETA_VIDEO" -> builder.agregarTarjetaVideo(
                        comp.getId(), comp.getDescripcion(), comp.getMarca(),
                        comp.getModelo(), comp.getCosto(), comp.getPrecioBase(),
                        subRequest.getMemoria()
                );
            }
        }
        
        return builder.build();
    }
    
    /**
     * Convierte un objeto Pc del dominio a PcResponse
     */
    public static PcResponse toResponse(Pc pc) {
        if (pc == null) {
            return null;
        }
        
        PcResponse response = new PcResponse();
        response.setId(pc.getId());
        response.setNombre(pc.getDescripcion());
        response.setPrecio(pc.getPrecioBase());
        response.setDescripcion(pc.getDescripcion());
        response.setMarca(pc.getMarca());
        response.setModelo(pc.getModelo());
        response.setCategoria("PC");
        response.setCantidad(1); // Default para PC
        
        // Convertir sub-componentes a DTOs
        if (pc.getSubComponentes() != null) {
            List<ComponenteResponse> subComponentesResponse = pc.getSubComponentes().stream()
                    .map(ComponenteMapper::toResponse)
                    .collect(Collectors.toList());
            
            response.setSubComponentes(subComponentesResponse);
            response.setTotalSubComponentes(subComponentesResponse.size());
            
            // Calcular precio total (incluyendo descuento de PC)
            response.setPrecioTotal(pc.getPrecioBase());
        } else {
            response.setSubComponentes(new ArrayList<>());
            response.setTotalSubComponentes(0);
            response.setPrecioTotal(BigDecimal.ZERO);
        }
        
        return response;
    }
    
    /**
     * Convierte un Componente genérico a PcResponse si es una PC
     */
    public static PcResponse toResponse(Componente componente) {
        if (componente instanceof Pc pc) {
            return toResponse(pc);
        }
        return null;
    }
    
    /**
     * Convierte un AgregarComponenteRequest a un objeto Componente del dominio
     */
    public static Componente toComponente(AgregarComponenteRequest request) {
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
} 