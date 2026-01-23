package mx.com.qtx.cotizador.dto.pc.response;

import lombok.Data;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para una PC completa con sus sub-componentes
 */
@Data
public class PcResponse {
    
    private String id;
    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    private String modelo;
    private String marca;
    private Integer cantidad;
    private String categoria = "PC";
    
    // Sub-componentes de la PC
    private List<ComponenteResponse> subComponentes;
    
    // Información adicional
    private BigDecimal precioTotal;
    private Integer totalSubComponentes;
    
    // Metadatos
    private String fechaCreacion;
    private String fechaActualizacion;
} 