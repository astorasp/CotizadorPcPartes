package mx.com.qtx.cotizador.impuestos;

import java.math.BigDecimal;

public abstract class CalculadorImpuesto {
    protected ICalculadorImpuestoPais calculoImpuestoPais;

    protected CalculadorImpuesto(ICalculadorImpuestoPais calculoImpuestoPais) {
        this.calculoImpuestoPais = calculoImpuestoPais;
    }

    public abstract BigDecimal calcularImpuesto(BigDecimal monto);
}
