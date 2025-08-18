package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Interfaz para promociones aplicables a componentes.
 */
public interface IPromocion {
    
    /**
     * Calcula el importe de promoción para una cantidad y precio base dados.
     * 
     * @param cantidad Cantidad de componentes
     * @param precioBase Precio base del componente
     * @return Importe total con la promoción aplicada
     */
    BigDecimal calcularImportePromocion(int cantidad, BigDecimal precioBase);
}