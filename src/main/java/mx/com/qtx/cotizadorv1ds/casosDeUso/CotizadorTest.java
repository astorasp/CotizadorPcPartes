package mx.com.qtx.cotizadorv1ds.casosDeUso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import mx.com.qtx.cotizadorv1ds.config.SpringContextProvider;
import mx.com.qtx.cotizadorv1ds.core.Cotizacion;
import mx.com.qtx.cotizadorv1ds.core.ICotizador;
import mx.com.qtx.cotizadorv1ds.core.componentes.Componente;
import mx.com.qtx.cotizadorv1ds.cotizadorA.Cotizador;

public class CotizadorTest {

	public CotizadorTest() {
		SpringContextProvider.initialize();
	}

    public static void main(String[] args) {
		System.out.println("*** Iniciando test ***");
		CotizadorTest cotizadorTest = new CotizadorTest();
		
		System.out.println(LocalDateTime.now().toString()+"Inicia testAgregarComponentes");
    	cotizadorTest.agregarComponente();
		System.out.println(LocalDateTime.now().toString()+"Termina testAgregarComponentes");		
		
		
		System.out.println(LocalDateTime.now().toString()+"Inicia testEliminarComponente");
    	cotizadorTest.testEliminarComponente();
		System.out.println(LocalDateTime.now().toString()+"Termina testEliminarComponente");				
		
		
		System.out.println(LocalDateTime.now().toString()+"Inicia testCreacionPC");		
    	cotizadorTest.testCreacionPC();
		System.out.println(LocalDateTime.now().toString()+"Termina testCreacionPC");
		
		
		System.out.println(LocalDateTime.now().toString()+"***Inicia testGenerarCotizacion***");
		cotizadorTest.testGenerarCotizacion();
		System.out.println(LocalDateTime.now().toString()+"***Termina testGenerarCotizacion***");			
	
		
    }

	private void testCreacionPC() {
		System.out.println("*** testCreacionPC ***");
    	Componente disco1 = Componente.crearDiscoDuro("D004", "Disco Seagate", "TechXYZ", "X200", 
	                new BigDecimal("1880.00"), new BigDecimal("2000.00"), "1TB");   
		disco1.guardarComponente();
       	Componente monitor = Componente.crearMonitor("M004", "Monitor 17 pulgadas", "Sony", "Z9000", 
                new BigDecimal("3200.00"), new BigDecimal("6000.00"));   
		monitor.guardarComponente();
        Componente tarjeta = Componente.crearTarjetaVideo("C004", "Tarjeta XYZ", "TechBrand", "X200", 
                new BigDecimal("150.00"), new BigDecimal("200.00"), "16GB");
		tarjeta.guardarComponente();
        
    	Componente miPc = Componente.crearPc("PC007", "Laptop 15000 s300", "Dell", "Terminator",
    											List.of(disco1,monitor,tarjeta));
		miPc.guardarComponente();
		miPc.mostrarCaracteristicas();
		
	}

	private void agregarComponente() {
		Componente monitor = Componente.crearMonitor("M005","Monitor 17 pulgadas","Samsung","Goliat-500",
						new BigDecimal(1000), new BigDecimal(2000));
		monitor.guardarComponente();
		
		Componente monitor2 = Componente.crearMonitor("M006","Monitor 15 pulgadas","Sony","VR-30",
				new BigDecimal(1100), new BigDecimal(2000));
		monitor2.guardarComponente();
	}

	private void testEliminarComponente() {
		Componente monitor = Componente.buscarComponente("M005");
		Componente monitor2 = Componente.buscarComponente("M006");
		monitor.borrarComponente();
		monitor2.borrarComponente();
	}

	private void testMostrarCaracteristicas() {
		System.out.println("*** testMostrarCaracteristicas ***");
    	Componente disco1 = Componente.crearDiscoDuro("D001", "Disco Seagate", "TechXYZ", "X200", 
                new BigDecimal("1880.00"), new BigDecimal("2000.00"), "1TB");   
		disco1.mostrarCaracteristicas();
	}
	
	private void testCreacionPcOk_conPcBuilder() {
		System.out.println("*** testCreacionPcOk_conPcBuilder ***");
		
		Componente miPc = Componente.getPcBuilder()
				                    .definirId("pc0001")
		                            .definirDescripcion("Laptop 15000 s300")
		                            .definirMarcaYmodelo("Dell", "Terminator")
		                            .agregarDisco("D001", "Disco Seagate", "TechXYZ", "X200", 
		             	                new BigDecimal("1880.00"), new BigDecimal("2000.00"), "1TB")
		                            .agregarMonitor("M001", "Monitor 17 pulgadas", "Sony", "Z9000", 
		                                 new BigDecimal("3200.00"), new BigDecimal("6000.00"))
		                            .agregarTarjetaVideo("C001", "Tarjeta XYZ", "TechBrand", "X200", 
		                                 new BigDecimal("150.00"), new BigDecimal("200.00"), "16GB")
		                            .agregarDisco("D002", "Disco Seagate", "TechXYZJr", "X100", 
			             	                new BigDecimal("1000.00"), new BigDecimal("1600.00"), "500GB")
		                            .build();
		miPc.guardarComponente();
		miPc.mostrarCaracteristicas();
		
	}
	
	private void testCreacionPcErroneo_conPcBuilder_DiscosDeMas() {
		System.out.println("*** testCreacionPcErroneo_conPcBuilder_DiscosDeMas ***");
		
		Componente miPc = Componente.getPcBuilder()
				                    .definirId("pc0001")
		                            .definirDescripcion("Laptop 15000 s300")
		                            .definirMarcaYmodelo("Dell", "Terminator")
		                            .agregarDisco("D001", "Disco Seagate", "TechXYZ", "X200", 
		             	                new BigDecimal("1880.00"), new BigDecimal("2000.00"), "1TB")
		                            .agregarMonitor("M001", "Monitor 17 pulgadas", "Sony", "Z9000", 
		                                 new BigDecimal("3200.00"), new BigDecimal("6000.00"))
		                            .agregarTarjetaVideo("C001", "Tarjeta XYZ", "TechBrand", "X200", 
		                                 new BigDecimal("150.00"), new BigDecimal("200.00"), "16GB")
		                            .agregarDisco("D002", "Disco Seagate", "TechXYZJr", "X100", 
			             	                new BigDecimal("1000.00"), new BigDecimal("1600.00"), "500GB")
		                            .agregarDisco("D003", "Disco Xtr 1TB", "Xtr-500", "Xtr", 
			             	                new BigDecimal("2000.00"), new BigDecimal("2600.00"), "1TB")
		                            .build();
		miPc.guardarComponente();
		miPc.mostrarCaracteristicas();
		
	}
	
	private void testCreacionPcErroneo_conPcBuilder_SinDiscos() {
		System.out.println("*** testCreacionPcOk_conPcBuilder ***");
		
		Componente miPc = Componente.getPcBuilder()
				                    .definirId("pc0001")
		                            .definirDescripcion("Laptop 15000 s300")
		                            .definirMarcaYmodelo("Dell", "Terminator")
		                            .agregarMonitor("M001", "Monitor 17 pulgadas", "Sony", "Z9000", 
		                                 new BigDecimal("3200.00"), new BigDecimal("6000.00"))
		                            .agregarTarjetaVideo("C001", "Tarjeta XYZ", "TechBrand", "X200", 
		                                 new BigDecimal("150.00"), new BigDecimal("200.00"), "16GB")
		                            .build();
		miPc.guardarComponente();
		miPc.mostrarCaracteristicas();
		
	}

	private void testGenerarCotizacion() {
		
		ICotizador cotizador = getCotizadorActual();
		
		Componente monitor = Componente.crearMonitor("M010","Monitor 17 pulgadas","Samsung","Goliat-500",
						new BigDecimal(1000), new BigDecimal(2000));
		cotizador.agregarComponente(1, monitor);
		monitor.guardarComponente();
		
		Componente monitor2 = Componente.crearMonitor("M011","Monitor 15 pulgadas","Sony","VR-30",
				new BigDecimal(1100), new BigDecimal(2000));
		monitor2.guardarComponente();
		cotizador.agregarComponente(4, monitor2);
		cotizador.agregarComponente(7, monitor2);
		
		Componente disco = Componente.crearDiscoDuro("D010", "Disco estado sólido", "Seagate", "T-455", new BigDecimal(500), 
				new BigDecimal(1000), "2TB");
		disco.guardarComponente();
		cotizador.agregarComponente(10, disco);
	
	    Componente tarjeta = Componente.crearTarjetaVideo("C010", "Tarjeta THOR", "TechBrand", "X200-34", 
	            new BigDecimal("150.00"), new BigDecimal("300.00"), "8GB");
		tarjeta.guardarComponente();	
		cotizador.agregarComponente(10, tarjeta);
	    
    	Componente discoPc = Componente.crearDiscoDuro("D011", "Disco Seagate", "TechXYZ", "X200", 
                new BigDecimal("1880.00"), new BigDecimal("2000.00"), "1TB");   
	   	Componente monitorPc = Componente.crearMonitor("M012", "Monitor 17 pulgadas", "Sony", "Z9000", 
	            new BigDecimal("3200.00"), new BigDecimal("6000.00"));   
	    Componente tarjetaPc = Componente.crearTarjetaVideo("C012", "Tarjeta XYZ", "TechBrand", "X200", 
	            new BigDecimal("150.00"), new BigDecimal("200.00"), "16GB");
	    
		Componente miPc = Componente.crearPc("PC010", "Laptop 15000 s300", "Dell", "Terminator",
											List.of(discoPc,monitorPc,tarjetaPc));
		miPc.guardarComponente();
		cotizador.agregarComponente(1, miPc);
		Cotizacion cotizacion = cotizador.generarCotizacion(null);		
		cotizacion.guardarCotizacion();
		cotizacion.emitirComoReporte();		
	}

	private static ICotizador getCotizadorActual() {
		return new Cotizador();
	}

}