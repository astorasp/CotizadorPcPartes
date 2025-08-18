package mx.com.qtx.cotizador.entidad;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

@Entity
@Table(name = "coproveedor")
public class Proveedor {
    
    @Id
    @Column(name = "cve")
    private String cve;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "razon_social")
    private String razonSocial;
    
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();
    
    // Constructores
    public Proveedor() {
        // Constructor vacío requerido por JPA
    }
    
    // Getters y setters
    public String getCve() {
        return cve;
    }
    
    public void setCve(String cve) {
        this.cve = cve;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRazonSocial() {
        return razonSocial;
    }
    
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
    
    public List<Pedido> getPedidos() {
        return pedidos;
    }
    
    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
    
    // Método helper para agregar un pedido
    public void addPedido(Pedido pedido) {
        pedidos.add(pedido);
        pedido.setProveedor(this);
    }
}
