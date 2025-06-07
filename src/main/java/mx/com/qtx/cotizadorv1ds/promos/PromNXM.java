package mx.com.qtx.cotizadorv1ds.promos;

import java.math.BigDecimal;

/**
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:21:01 p. m.
 */
public class PromNXM extends PromBase {

	private int lleveN;
	private int pagueM;


	public PromNXM(int n, int m) {
		super(n + " X " + m, "Lleve " + n + ", pague " + m);
		this.lleveN = n;
		this.pagueM = m;
	}


	public int getLleveN() {
		return lleveN;
	}


	public void setLleveN(int lleveN) {
		this.lleveN = lleveN;
	}


	public int getPagueM() {
		return pagueM;
	}


	public void setPagueM(int pagueM) {
		this.pagueM = pagueM;
	}


	/**
	 * 
	 * @param cant
	 * @param precioBase
	 */
	public BigDecimal calcularImportePromocion(int nUnidades, BigDecimal precioBase){
	    
	    // Calcular grupos completos de N unidades y unidades restantes
	    int gruposCompletos = nUnidades / this.lleveN;
	    int unidadesRestantes = nUnidades % this.lleveN;
	    
	    // Calcular total: (M * grupos) + restantes
	    BigDecimal total = precioBase
	        .multiply(BigDecimal.valueOf(gruposCompletos * this.pagueM + unidadesRestantes));
    
	    return total;
	}

}