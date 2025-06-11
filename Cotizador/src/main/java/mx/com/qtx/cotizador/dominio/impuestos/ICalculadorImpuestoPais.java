package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

public interface ICalculadorImpuestoPais {
    BigDecimal calcularImpuestoPais(BigDecimal monto);
}
