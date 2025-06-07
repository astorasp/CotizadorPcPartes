package mx.com.qtx.cotizadorv1ds.impuestos;

import java.math.BigDecimal;

public class CalculadorImpuestoFederal extends CalculadorImpuesto {
    private static final BigDecimal IMPUESTO_FEDERAL = BigDecimal.valueOf(0.05);
    public CalculadorImpuestoFederal(ICalculadorImpuestoPais calculoImpuestoPais) {
        super(calculoImpuestoPais);
    }

    @Override
    public BigDecimal calcularImpuesto(BigDecimal monto) {
        return calculoImpuestoPais.calcularImpuestoPais(monto) 
            .add(monto.multiply(IMPUESTO_FEDERAL));
    }
}