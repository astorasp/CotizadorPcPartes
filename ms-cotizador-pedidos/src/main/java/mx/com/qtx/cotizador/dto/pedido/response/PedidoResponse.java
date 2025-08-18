package mx.com.qtx.cotizador.dto.pedido.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para pedidos
 * 
 * Contiene toda la información del pedido para ser enviada al cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {
    
    /**
     * Número único del pedido
     */
    private Long numPedido;
    
    /**
     * Fecha de emisión del pedido
     */
    private LocalDate fechaEmision;
    
    /**
     * Fecha de entrega programada
     */
    private LocalDate fechaEntrega;
    
    /**
     * Nivel de surtido del pedido (0-100)
     */
    private Integer nivelSurtido;
    
    /**
     * Clave del proveedor asociado
     */
    private String cveProveedor;
    
    /**
     * Nombre del proveedor asociado
     */
    private String nombreProveedor;
    
    /**
     * Total del pedido (suma de todos los detalles)
     */
    private BigDecimal total;
    
    /**
     * Lista de detalles del pedido
     */
    private List<DetallePedidoResponse> detalles;
    
    /**
     * Número total de líneas de detalle
     */
    private Integer totalDetalles;
} 