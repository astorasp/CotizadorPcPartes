package mx.com.qtx.cotizadorv1ds.config;

import java.util.List;

import mx.com.qtx.cotizadorv1ds.core.ICotizador;

public class TestConfig {

	public static void main(String[] args) {
		test_buscarImplementacionesDe();
		test_getCotizador();
		test_calcularPaqueteFinal();
	}

	private static void test_getCotizador() {
		System.out.println("\n============= test_getCotizador() =============");
		
		ICotizador cotizador = Config.getCotizador();
		System.out.println("Clase instanciada:" + cotizador.getClass().getName());
		
	}

	private static void test_buscarImplementacionesDe() {
		System.out.println("\n============= test_buscarImplementacionesDe() =============");
		
		System.out.println("classpath:" + System.getProperty("java.class.path"));
		
//		Config.buscarRecursoEnClassPath("mx/com/qtx/cotizadorv1ds/cotizadorA/Cotizador.class");		
//		Config.buscarRecursoEnClassPath("mx/com/qtx/cotizadorv1ds/cotizadorA");		
		
		
		List<Class<?>> lstImplmICotizador = Config.buscarImplementacionesDe(ICotizador.class, "mx.com.qtx.cotizadorv1ds");
		System.out.println("Implementaciones de ICotizador en el classpath:");
		lstImplmICotizador.forEach(implI->System.out.println("\t" + implI.getName()));
	}

	private static void test_calcularPaqueteFinal() {
		System.out.println("\n============= test_calcularPaqueteFinal() =============");
		
        // Ejemplo 1
        String ruta1 = "C:/qtx/workspacesSts/2025/Mod02_6toDiploArq/m02ejm02/bin/mx/com/qtx/cotizadorv1ds/casosDeUso";
        String base1 = "mx.com.qtx.cotizadorv1ds";
        System.out.println(Config.calcularPaqueteFinal(ruta1, base1)); // mx.com.qtx.cotizadorv1ds.casosDeUso

        // Ejemplo 2
        String ruta2 = "C:\\qtx\\workspacesSts\\2025\\Mod02_6toDiploArq\\m02ejm02\\bin\\mx\\com\\qtx\\cotizadorv1ds\\casosDeUso\\test";
        String base2 = "mx.com.qtx.cotizadorv1ds";
        System.out.println(Config.calcularPaqueteFinal(ruta2, base2)); // mx.com.qtx.cotizadorv1ds.casosDeUso.test
		
	}
}
