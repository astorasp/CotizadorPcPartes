package mx.com.qtx.cotizadorv1ds.impuestos;

import java.math.BigDecimal;

public abstract class CalculadorImpuesto {
    protected ICalculadorImpuestoPais calculoImpuestoPais;

    protected CalculadorImpuesto(ICalculadorImpuestoPais calculoImpuestoPais) {
        this.calculoImpuestoPais = calculoImpuestoPais;
    }

    public abstract BigDecimal calcularImpuesto(BigDecimal monto);
}
