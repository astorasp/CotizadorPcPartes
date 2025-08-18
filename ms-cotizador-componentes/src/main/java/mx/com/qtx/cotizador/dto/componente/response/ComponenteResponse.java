package mx.com.qtx.cotizador.dto.componente.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para componentes
 * Contiene toda la información del componente para ser enviada al cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteResponse {
    
    private String id;
    private String descripcion;
    private String marca;
    private String modelo;
    private BigDecimal costo;
    private BigDecimal precioBase;
    private String tipoComponente;
    
    // Campos específicos para disco duro
    private String capacidadAlm;
    
    // Campos específicos para tarjeta de video
    private String memoria;
    
    // Información adicional de la promoción si existe
    private String promocionId;
    private String promocionDescripcion;
} 