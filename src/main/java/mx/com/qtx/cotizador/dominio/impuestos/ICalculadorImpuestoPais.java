package mx.com.qtx.cotizador.impuestos;

import java.math.BigDecimal;

public interface ICalculadorImpuestoPais {
    BigDecimal calcularImpuestoPais(BigDecimal monto);
}
