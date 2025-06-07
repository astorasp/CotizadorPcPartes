package mx.com.qtx.cotizadorv1ds.core.componentes;

import java.math.BigDecimal;

public interface IPromocion {
	BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);
}
