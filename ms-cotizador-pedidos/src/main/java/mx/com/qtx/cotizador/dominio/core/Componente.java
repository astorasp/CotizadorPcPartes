package mx.com.qtx.cotizador.dominio.core;

import java.math.BigDecimal;

/**
 * Objeto de dominio que representa un componente de hardware
 * Versión simplificada para el microservicio de pedidos
 */
public class Componente {
    
    private Integer id;
    private String marca;
    private String modelo;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal descuento;
    private String tipoComponente;
    
    // Constructores
    public Componente() {
        this.descuento = BigDecimal.ZERO;
    }
    
    public Componente(Integer id, String marca, String modelo, String descripcion, BigDecimal precio) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.descuento = BigDecimal.ZERO;
    }
    
    // Getters y setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    
    public BigDecimal getDescuento() {
        return descuento;
    }
    
    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }
    
    public String getTipoComponente() {
        return tipoComponente;
    }
    
    public void setTipoComponente(String tipoComponente) {
        this.tipoComponente = tipoComponente;
    }
    
    /**
     * Calcula el precio con descuento aplicado
     */
    public BigDecimal getPrecioConDescuento() {
        if (descuento == null || BigDecimal.ZERO.equals(descuento)) {
            return precio;
        }
        return precio.subtract(descuento);
    }
}