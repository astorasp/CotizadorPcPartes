package mx.com.qtx.cotizadorv1ds.core;

import java.util.List;

import mx.com.qtx.cotizadorv1ds.core.componentes.Componente;
import mx.com.qtx.cotizadorv1ds.impuestos.CalculadorImpuesto;

public interface ICotizador {
    void agregarComponente(int cantidad, Componente componente);
    void eliminarComponente(String idComponente) throws ComponenteInvalidoException;
    Cotizacion generarCotizacion(List<CalculadorImpuesto> CalculadorImpuesto);
    void listarComponentes();
}
