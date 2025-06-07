package mx.com.qtx.cotizadorv1ds.impuestos;

import java.math.BigDecimal;

public interface ICalculadorImpuestoPais {
    BigDecimal calcularImpuestoPais(BigDecimal monto);
}
