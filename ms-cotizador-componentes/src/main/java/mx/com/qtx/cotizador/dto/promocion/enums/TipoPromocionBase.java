package mx.com.qtx.cotizador.dto.promocion.enums;

/**
 * Enum que define los tipos de promoción base disponibles en el sistema.
 * 
 * Corresponde a los valores encontrados en el campo 'tipo_prom_base' 
 * y las constantes del PromocionBuilder.
 */
public enum TipoPromocionBase {
    
    /**
     * Promoción sin descuento (promoción regular)
     * Corresponde a PROM_BASE_SIN_DSCTO = 1
     */
    SIN_DESCUENTO("SIN_DSCTO", "Sin descuento"),
    
    /**
     * Promoción tipo "Compra N, Paga M" (ej: 3x2)
     * Corresponde a PROM_BASE_NXM = 2
     */
    NXM("NXM", "Compra N, Paga M");
    
    private final String codigo;
    private final String descripcion;
    
    TipoPromocionBase(String codigo, String descripcion) {
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
     * Busca un tipo de promoción por su código
     * Incluye compatibilidad con datos existentes en BD
     */
    public static TipoPromocionBase fromCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        
        // Buscar por código exacto
        for (TipoPromocionBase tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        
        // Compatibilidad con códigos existentes en BD
        switch (codigo.toUpperCase()) {
            case "BASE":
                return SIN_DESCUENTO; // Mapear "BASE" a SIN_DESCUENTO por defecto
            case "SIN_DSCTO":
            case "SIN_DESCUENTO":
                return SIN_DESCUENTO;
            case "NXM":
                return NXM;
            default:
                throw new IllegalArgumentException("Tipo de promoción base no válido: " + codigo);
        }
    }
} 