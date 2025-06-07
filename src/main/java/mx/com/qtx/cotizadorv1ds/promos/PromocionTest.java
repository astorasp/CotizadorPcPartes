package mx.com.qtx.cotizadorv1ds.promos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import mx.com.qtx.cotizadorv1ds.config.Config;
import mx.com.qtx.cotizadorv1ds.core.Cotizacion;
import mx.com.qtx.cotizadorv1ds.core.ICotizador;
import mx.com.qtx.cotizadorv1ds.core.componentes.Componente;

public class PromocionTest {
	private static Map<Integer, Double> mapDsctos = Map.of(0,  0.0,
			   3,  5.0,
			   6, 10.0,
			   9, 12.0);

	public static void main(String[] args) {
		testGenerarCotizacion();

	}
	private static void testGenerarCotizacion() {
		
		ICotizador cotizador = getCotizadorActual();
		
		Componente monitor = Componente.crearMonitor("M001","Monitor 17 pulgadas","Samsung","Goliat-500",
						new BigDecimal(1000), new BigDecimal(2000));
		
		Promocion promoMonitor = getPromo_2x1_mas_5_mas_10();
		Promocion.mostrarEstructuraPromocion(promoMonitor);
		
		monitor.setPromo(promoMonitor);
		cotizador.agregarComponente(10, monitor);

		Componente monitor2 = Componente.crearMonitor("M022","Monitor 15 pulgadas","Sony","VR-30",
				new BigDecimal(1100), new BigDecimal(2000));
		
		monitor2.setPromo(Promocion.getBuilder()
				                   .conPromocionBaseSinDscto()
				                   .agregarDsctoXcantidad(mapDsctos)
				                   .build());
		cotizador.agregarComponente(4, monitor2);
		cotizador.agregarComponente(7, monitor2);
		
		Componente disco = Componente.crearDiscoDuro("D-23", "Disco estado sólido", "Seagate", "T-455", new BigDecimal(500), 
				new BigDecimal(1000), "2TB");

		cotizador.agregarComponente(10, disco);
	
	    Componente tarjeta = Componente.crearTarjetaVideo("C0XY", "Tarjeta THOR", "TechBrand", "X200-34", 
	            new BigDecimal("150.00"), new BigDecimal("300.00"), "8GB");
	    tarjeta.setPromo(Promocion.getBuilder().conPromocionBaseNXM(3, 2).build());
		cotizador.agregarComponente(10, tarjeta);
	    
    	Componente discoPc = Componente.crearDiscoDuro("D001", "Disco Seagate", "TechXYZ", "X200", 
                new BigDecimal("1880.00"), new BigDecimal("2000.00"), "1TB");   
	   	Componente monitorPc = Componente.crearMonitor("M001", "Monitor 17 pulgadas", "Sony", "Z9000", 
	            new BigDecimal("3200.00"), new BigDecimal("6000.00"));   
	    Componente tarjetaPc = Componente.crearTarjetaVideo("C001", "Tarjeta XYZ", "TechBrand", "X200", 
	            new BigDecimal("150.00"), new BigDecimal("200.00"), "16GB");
	    
		Componente miPc = Componente.crearPc("pc0001", "Laptop 15000 s300", "Dell", "Terminator",
												List.of(discoPc,monitorPc,tarjetaPc));
		miPc.setPromo(Promocion.getBuilder()
				                   .conPromocionBaseSinDscto()
				                   .agregarDsctoPlano(30f)
				                   .agregarDsctoPlano(20f)
				                   .build());
		cotizador.agregarComponente(1, miPc);
		Cotizacion cotizacion = cotizador.generarCotizacion(null);
		cotizacion.emitirComoReporte();
	}
	
	private static Promocion getPromo_SinDscto() {
		Promocion promo = new PromSinDescto();
		return promo;
	}
	
	private static Promocion getPromo_3x2() {
		Promocion promo = new PromNXM(3, 2);
		return promo;
	}
	
	private static Promocion getPromo_2x1_mas_5() {
		Promocion promo = new PromDsctoPlano(new PromNXM(2,1), 5.0f);
		return promo;
	}

	private static Promocion getPromo_2x1_mas_5_mas_10() {
		Promocion promo = new PromDsctoPlano(
								new PromDsctoPlano(
										new PromNXM(2,1), 
										5.0f),
								10.0f);
		return promo;
	}

	private static Promocion getPromo_2x1_mas_dsctosXcantidad() {
		Promocion promo = new PromDsctoXcantidad(new PromNXM(2,1), 
				                                 mapDsctos);
		return promo;
	}
	
	private static Promocion getPromo_3x2_mas_5_mas_10_mas_dsctosXcantidad() {
		PromocionBuilder promoBuilder = Promocion.getBuilder();
		
		Promocion promo = promoBuilder.conPromocionBaseNXM(3,2)
		            				  .agregarDsctoPlano(5.0f)
		            				  .agregarDsctoPlano(10f)
		            				  .agregarDsctoXcantidad(mapDsctos)
		            				  .build();
		
		return promo;
	}

	private static ICotizador getCotizadorActual() {
		return Config.getCotizador();
	}

}
