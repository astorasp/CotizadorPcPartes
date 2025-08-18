package mx.com.qtx.cotizador.dominio.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una cotización de venta con sus detalles y cálculos totales.
 * Versión simplificada para el microservicio de pedidos.
 */
public class Cotizacion {
	
	private Integer id;
	private LocalDate fechaCreacion;
	private BigDecimal subtotal;
	private BigDecimal impuestos;
	private BigDecimal total;
	private String pais;
	private List<DetalleCotizacion> detalles;
	
	// Constructores
	public Cotizacion() {
		this.detalles = new ArrayList<>();
		this.subtotal = BigDecimal.ZERO;
		this.impuestos = BigDecimal.ZERO;
		this.total = BigDecimal.ZERO;
		this.fechaCreacion = LocalDate.now();
	}
	
	public Cotizacion(LocalDate fechaCreacion, String pais) {
		this();
		this.fechaCreacion = fechaCreacion;
		this.pais = pais;
	}
	
	// Getters y setters
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public LocalDate getFechaCreacion() {
		return fechaCreacion;
	}
	
	public void setFechaCreacion(LocalDate fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	
	public BigDecimal getSubtotal() {
		return subtotal;
	}
	
	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}
	
	public BigDecimal getImpuestos() {
		return impuestos;
	}
	
	public void setImpuestos(BigDecimal impuestos) {
		this.impuestos = impuestos;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	public String getPais() {
		return pais;
	}
	
	public void setPais(String pais) {
		this.pais = pais;
	}
	
	public List<DetalleCotizacion> getDetalles() {
		return detalles;
	}
	
	public void setDetalles(List<DetalleCotizacion> detalles) {
		this.detalles = detalles;
	}
	
	// Método helper para agregar detalle
	public void addDetalle(DetalleCotizacion detalle) {
		detalles.add(detalle);
	}
}
