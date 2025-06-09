package mx.com.qtx.cotizador.dto.pedido.response;

import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para pedidos
 * 
 * Contiene toda la información del pedido incluyendo:
 * - Datos básicos del pedido
 * - Información del proveedor
 * - Lista completa de detalles
 * - Totales calculados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    
    /**
     * Número único del pedido
     */
    private Integer numPedido;
    
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
     * Total del pedido (suma de todos los detalles)
     */
    private BigDecimal total;
    
    /**
     * Información completa del proveedor
     */
    private ProveedorResponse proveedor;
    
    /**
     * Lista de detalles del pedido
     */
    private List<DetallePedidoResponse> detalles;
    
    /**
     * Número total de líneas de detalle
     */
    private Integer totalDetalles;
    
    /**
     * Estado calculado basado en fechas
     */
    private String estado; // "PENDIENTE", "EN_PROCESO", "ENTREGADO", "VENCIDO"
} 