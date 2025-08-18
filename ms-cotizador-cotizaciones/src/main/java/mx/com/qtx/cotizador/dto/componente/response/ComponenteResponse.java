package mx.com.qtx.cotizador.dto.componente.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO de respuesta para componentes.
 * Representa un componente individual en las respuestas de la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponenteResponse {
    
    /**
     * Identificador único del componente
     */
    private String id;
    
    /**
     * Nombre del componente
     */
    private String nombre;
    
    /**
     * Descripción del componente
     */
    private String descripcion;
    
    /**
     * Precio del componente
     */
    private BigDecimal precio;
    
    /**
     * Marca del componente
     */
    private String marca;
    
    /**
     * Modelo del componente
     */
    private String modelo;
    
    /**
     * Tipo de componente (CPU, GPU, RAM, etc.)
     */
    private String tipoComponente;
    
    /**
     * Stock disponible del componente
     */
    private Integer stock;
    
    /**
     * Indica si el componente está activo
     */
    private Boolean activo;
    
    /**
     * Categoría del componente
     */
    private String categoria;
    
    /**
     * Especificaciones técnicas adicionales
     */
    private String especificaciones;
}