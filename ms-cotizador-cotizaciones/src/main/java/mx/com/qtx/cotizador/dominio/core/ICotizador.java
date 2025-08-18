package mx.com.qtx.cotizador.dominio.core;

import java.util.List;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;

public interface ICotizador {
    void agregarComponente(int cantidad, Componente componente);
    void eliminarComponente(String idComponente) throws ComponenteInvalidoException;
    Cotizacion generarCotizacion(List<CalculadorImpuesto> CalculadorImpuesto);
    void listarComponentes();
}
