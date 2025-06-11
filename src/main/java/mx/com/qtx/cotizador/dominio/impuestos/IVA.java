package mx.com.qtx.cotizador.dominio.impuestos;

import java.math.BigDecimal;

/**
 * Calculador de IVA para México (16%)
 */
public class IVA extends CalculadorImpuesto {
    
    public IVA() {
        super(new CalculadorImpuestoMexico());
    }

    @Override
    public BigDecimal calcularImpuesto(BigDecimal monto) {
        return calculoImpuestoPais.calcularImpuestoPais(monto);
    }
} 