package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

public interface IPromocion {
	BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);
}
