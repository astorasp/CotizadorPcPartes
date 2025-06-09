package mx.com.qtx.cotizador.dto.proveedor.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para proveedores
 * 
 * Contiene toda la información del proveedor para ser enviada al cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponse {
    
    /**
     * Clave única del proveedor
     */
    private String cve;
    
    /**
     * Nombre comercial del proveedor
     */
    private String nombre;
    
    /**
     * Razón social completa del proveedor
     */
    private String razonSocial;
    
    /**
     * Número de pedidos asociados (opcional, para estadísticas)
     */
    private Integer numeroPedidos;
} 