package mx.com.qtx.cotizadorv1ds.impuestos;

import java.math.BigDecimal;

public class CalculadorImpuestoMexico implements ICalculadorImpuestoPais {
    @Override
    public BigDecimal calcularImpuestoPais(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(0.16));
    }
}
