package mx.com.qtx.cotizador.dto.pedido.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para detalles de pedido
 * 
 * Contiene toda la información de un detalle de pedido
 * para ser enviada al cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoResponse {
    
    /**
     * ID único del artículo/componente
     */
    private String idArticulo;
    
    /**
     * Descripción del artículo
     */
    private String descripcion;
    
    /**
     * Cantidad solicitada
     */
    private Integer cantidad;
    
    /**
     * Precio unitario del artículo
     */
    private BigDecimal precioUnitario;
    
    /**
     * Total cotizado para esta línea de detalle
     */
    private BigDecimal totalCotizado;
    
    /**
     * Número de detalle dentro del pedido (para ordenamiento)
     */
    private Integer numeroDetalle;
} 