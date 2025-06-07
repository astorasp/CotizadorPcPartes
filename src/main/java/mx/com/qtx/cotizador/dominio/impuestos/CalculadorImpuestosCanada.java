package mx.com.qtx.cotizador.impuestos;

import java.math.BigDecimal;

public class CalculadorImpuestosCanada implements ICalculadorImpuestoPais {
    @Override
    public BigDecimal calcularImpuestoPais(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(0.15));
    }
}
