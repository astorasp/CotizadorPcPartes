package mx.com.qtx.cotizador.dto.pedido.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO de respuesta para detalles de pedido
 * 
 * Contiene la información de cada línea de detalle de un pedido
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoResponse {
    
    /**
     * Identificador del artículo
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
     * Total de esta línea (cantidad * precio unitario)
     */
    private BigDecimal totalCotizado;
} 