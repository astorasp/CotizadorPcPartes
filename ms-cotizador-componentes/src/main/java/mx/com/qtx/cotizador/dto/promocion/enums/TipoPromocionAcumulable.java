package mx.com.qtx.cotizador.dto.promocion.enums;

/**
 * Enum que define los tipos de promoción acumulable disponibles en el sistema.
 * 
 * Estas promociones se "acumulan" sobre una promoción base mediante el patrón Decorator.
 */
public enum TipoPromocionAcumulable {
    
    /**
     * Descuento porcentual plano aplicado al total
     */
    DESCUENTO_PLANO("PLANO", "Descuento porcentual plano"),
    
    /**
     * Descuento basado en escalas de cantidad
     * Ejemplo: 1-5 unidades = 5%, 6-10 = 10%, 11+ = 15%
     */
    DESCUENTO_POR_CANTIDAD("POR_CANTIDAD", "Descuento por escalas de cantidad");
    
    private final String codigo;
    private final String descripcion;
    
    TipoPromocionAcumulable(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Busca un tipo de promoción acumulable por su código
     * Incluye compatibilidad con datos existentes en BD
     */
    public static TipoPromocionAcumulable fromCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        
        // Buscar por código exacto
        for (TipoPromocionAcumulable tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        
        // Compatibilidad con códigos existentes en BD
        switch (codigo.toUpperCase()) {
            case "PLANO":
            case "DESCUENTO_PLANO":
            case "DSCTO_PLANO":
                return DESCUENTO_PLANO;
                
            case "POR_CANTIDAD":
            case "DESCUENTO_POR_CANTIDAD":
            case "DSCTO_CANTIDAD":
            case "DSCTO_X_CANTIDAD":
            case "VOLUME":
            case "VOLUMEN":
            case "CANTIDAD":
                return DESCUENTO_POR_CANTIDAD;
                
            default:
                throw new IllegalArgumentException("Tipo de promoción acumulable no válido: " + codigo);
        }
    }
} 