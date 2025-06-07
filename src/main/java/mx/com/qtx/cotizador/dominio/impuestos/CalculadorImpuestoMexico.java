package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

public class CalculadorImpuestoMexico implements ICalculadorImpuestoPais {
    @Override
    public BigDecimal calcularImpuestoPais(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(0.16));
    }
}
