package mx.com.qtx.cotizador.dto.pedido.mapper;

import mx.com.qtx.cotizador.dominio.pedidos.Pedido;
import mx.com.qtx.cotizador.dominio.pedidos.DetallePedido;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.dto.pedido.response.DetallePedidoResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversiones entre objetos de dominio Pedido y DTOs
 * 
 * Proporciona métodos estáticos para convertir:
 * - Pedido de dominio → PedidoResponse
 * - DetallePedido de dominio → DetallePedidoResponse
 */
public class PedidoMapper {
    
    /**
     * Convierte un objeto de dominio Pedido a PedidoResponse
     * 
     * @param pedido Objeto de dominio
     * @return DTO de respuesta
     */
    public static PedidoResponse toResponse(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        
        // Convertir detalles
        List<DetallePedidoResponse> detallesResponse = pedido.getDetallesPedido()
                .stream()
                .map(PedidoMapper::toDetallePedidoResponse)
                .collect(Collectors.toList());
        
        return PedidoResponse.builder()
                .numPedido(pedido.getNumPedido())
                .fechaEmision(pedido.getFechaEmision())
                .fechaEntrega(pedido.getFechaEntrega())
                .nivelSurtido(pedido.getNivelSurtido())
                .cveProveedor(pedido.getProveedor() != null ? pedido.getProveedor().getCve() : null)
                .nombreProveedor(pedido.getProveedor() != null ? pedido.getProveedor().getNombre() : null)
                .total(pedido.getTotalPedido())
                .detalles(detallesResponse)
                .totalDetalles(detallesResponse.size())
                .build();
    }
    
    /**
     * Convierte un objeto de dominio DetallePedido a DetallePedidoResponse
     * 
     * @param detalle Objeto de dominio del detalle
     * @return DTO de respuesta del detalle
     */
    public static DetallePedidoResponse toDetallePedidoResponse(DetallePedido detalle) {
        if (detalle == null) {
            return null;
        }
        
        return DetallePedidoResponse.builder()
                .idArticulo(detalle.getIdArticulo())
                .descripcion(detalle.getDescripcion())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .totalCotizado(detalle.getTotalCotizado())
                .build();
    }
} 