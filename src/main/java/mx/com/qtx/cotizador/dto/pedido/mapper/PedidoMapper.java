package mx.com.qtx.cotizador.dto.pedido.mapper;

import mx.com.qtx.cotizador.dominio.pedidos.DetallePedido;
import mx.com.qtx.cotizador.dominio.pedidos.Pedido;
import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.dto.pedido.request.DetallePedidoRequest;
import mx.com.qtx.cotizador.dto.pedido.request.PedidoCreateRequest;
import mx.com.qtx.cotizador.dto.pedido.response.DetallePedidoResponse;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.dto.proveedor.mapper.ProveedorMapper;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mapper para conversiones entre DTOs y objetos de dominio de Pedido
 * 
 * Maneja las transformaciones:
 * - PedidoCreateRequest → Pedido (dominio)
 * - Pedido (dominio) → PedidoResponse
 * - DetallePedidoRequest → DetallePedido (dominio)
 * - DetallePedido (dominio) → DetallePedidoResponse
 */
public class PedidoMapper {
    
    /**
     * Convierte un PedidoCreateRequest a objeto de dominio Pedido
     * 
     * @param request DTO con datos para crear pedido
     * @param proveedor Objeto proveedor del dominio
     * @param numPedido Número generado para el pedido (usar 0L para auto-increment)
     * @return Objeto de dominio Pedido
     */
    public static Pedido toPedido(PedidoCreateRequest request, Proveedor proveedor, long numPedido) {
        if (request == null || proveedor == null) {
            return null;
        }
        
        // Crear pedido básico
        Pedido pedido = new Pedido(
            numPedido,
            request.getFechaEmision(),
            request.getFechaEntrega(),
            request.getNivelSurtido(),
            proveedor
        );
        
        // Agregar detalles
        if (request.getDetalles() != null) {
            for (DetallePedidoRequest detalleRequest : request.getDetalles()) {
                DetallePedido detalle = toDetallePedido(detalleRequest);
                if (detalle != null) {
                    pedido.agregarDetallePedido(
                        detalle.getIdArticulo(),
                        detalle.getDescripcion(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getTotalCotizado()
                    );
                }
            }
        }
        
        return pedido;
    }
    
    /**
     * Convierte un DetallePedidoRequest a objeto de dominio DetallePedido
     * 
     * @param request DTO con datos del detalle
     * @return Objeto de dominio DetallePedido
     */
    public static DetallePedido toDetallePedido(DetallePedidoRequest request) {
        if (request == null) {
            return null;
        }
        
        return new DetallePedido(
            request.getIdArticulo(),
            request.getDescripcion(),
            request.getCantidad(),
            request.getPrecioUnitario(),
            request.getTotalCotizado()
        );
    }
    
    /**
     * Convierte un objeto de dominio Pedido a PedidoResponse
     * 
     * @param pedido Objeto de dominio
     * @param proveedorResponse DTO del proveedor (ya convertido)
     * @return DTO de respuesta
     */
    public static PedidoResponse toResponse(Pedido pedido, ProveedorResponse proveedorResponse) {
        if (pedido == null) {
            return null;
        }
        
        // Convertir detalles
        List<DetallePedidoResponse> detallesResponse = toDetallePedidoResponseList(pedido.getDetallesPedido());
        
        return PedidoResponse.builder()
            .numPedido((int) pedido.getNumPedido())
            .fechaEmision(pedido.getFechaEmision())
            .fechaEntrega(pedido.getFechaEntrega())
            .nivelSurtido(pedido.getNivelSurtido())
            .total(pedido.getTotalPedido())
            .proveedor(proveedorResponse)
            .detalles(detallesResponse)
            .totalDetalles(detallesResponse != null ? detallesResponse.size() : 0)
            .estado(calcularEstado(pedido))
            .build();
    }
    
    /**
     * Convierte un objeto de dominio Pedido a PedidoResponse
     * (Versión que maneja la conversión del proveedor internamente)
     * 
     * @param pedido Objeto de dominio
     * @return DTO de respuesta
     */
    public static PedidoResponse toResponse(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        
        // Convertir proveedor a DTO
        ProveedorResponse proveedorResponse = ProveedorMapper.toResponse(pedido.getProveedor());
        
        return toResponse(pedido, proveedorResponse);
    }
    
    /**
     * Convierte un objeto de dominio DetallePedido a DetallePedidoResponse
     * 
     * @param detalle Objeto de dominio
     * @param numeroDetalle Número del detalle para ordenamiento
     * @return DTO de respuesta
     */
    public static DetallePedidoResponse toDetallePedidoResponse(DetallePedido detalle, int numeroDetalle) {
        if (detalle == null) {
            return null;
        }
        
        return DetallePedidoResponse.builder()
            .idArticulo(detalle.getIdArticulo())
            .descripcion(detalle.getDescripcion())
            .cantidad(detalle.getCantidad())
            .precioUnitario(detalle.getPrecioUnitario())
            .totalCotizado(detalle.getTotalCotizado())
            .numeroDetalle(numeroDetalle)
            .build();
    }
    
    /**
     * Convierte una lista de objetos de dominio DetallePedido a lista de DetallePedidoResponse
     * 
     * @param detalles Lista de objetos de dominio
     * @return Lista de DTOs de respuesta
     */
    public static List<DetallePedidoResponse> toDetallePedidoResponseList(List<DetallePedido> detalles) {
        if (detalles == null) {
            return null;
        }
        
        return IntStream.range(0, detalles.size())
            .mapToObj(i -> toDetallePedidoResponse(detalles.get(i), i + 1))
            .collect(Collectors.toList());
    }
    
    /**
     * Convierte una lista de objetos de dominio Pedido a lista de PedidoResponse
     * 
     * @param pedidos Lista de objetos de dominio
     * @return Lista de DTOs de respuesta
     */
    public static List<PedidoResponse> toResponseList(List<Pedido> pedidos) {
        if (pedidos == null) {
            return null;
        }
        
        return pedidos.stream()
            .map(PedidoMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Calcula el estado del pedido basado en las fechas
     * 
     * @param pedido Objeto de dominio
     * @return Estado como string
     */
    private static String calcularEstado(Pedido pedido) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaEntrega = pedido.getFechaEntrega();
        
        if (fechaEntrega == null) {
            return "PENDIENTE";
        }
        
        if (fechaEntrega.isBefore(hoy)) {
            return "VENCIDO";
        } else if (fechaEntrega.isEqual(hoy)) {
            return "EN_PROCESO";
        } else {
            return "PENDIENTE";
        }
    }
} 