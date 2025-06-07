package mx.com.qtx.cotizadorv1ds.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizadorv1ds.pedidos.Pedido;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ComponenteRepositorio;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.PedidoRepositorio;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ProveedorRepositorio;
import mx.com.qtx.cotizadorv1ds.servicios.wrapper.PedidoEntityConverter;

@Service
public class PedidoServicio {

    private PedidoRepositorio pedidoRepositorio;
    private ProveedorRepositorio proveedorRepositorio;
    private ComponenteRepositorio componenteRepositorio;
    
    public PedidoServicio(PedidoRepositorio pedidoRepositorio, 
                          ProveedorRepositorio proveedorRepositorio,
                          ComponenteRepositorio componenteRepositorio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.componenteRepositorio = componenteRepositorio;
    }
    
    /**
     * Guarda un pedido del dominio de negocio en la base de datos.
     * Convierte el pedido del dominio a una entidad de persistencia y guarda tanto el pedido
     * como sus detalles en una única transacción.
     * 
     * @param pedido El pedido del dominio de negocio a guardar
     * @return La entidad de pedido persistida con su ID generado
     */
    @Transactional
    public void guardarPedido(Pedido pedido) {
        // 1. Convertir el pedido del dominio a una entidad sin detalles
        var pedidoEntity = PedidoEntityConverter.convertToEntity(pedido, proveedorRepositorio);
        
        // 2. Persistir primero el pedido para obtener su ID generado
        pedidoEntity = pedidoRepositorio.save(pedidoEntity);
        
        // 3. Ahora que el pedido tiene ID, agregar los detalles
        PedidoEntityConverter.addDetallesTo(pedido, pedidoEntity, componenteRepositorio);

        // 4. Guardar nuevamente para persistir los detalles
        pedidoRepositorio.save(pedidoEntity);        
    }
    
    /**
     * Busca un pedido por su ID en la base de datos y lo convierte a un objeto del dominio.
     * 
     * @param id El ID del pedido a buscar
     * @return El objeto Pedido del dominio si existe, o null si no se encuentra
     */
    public Pedido buscarPorId(Integer id) {
        // Primero recuperamos la entidad de la base de datos
        var pedidoEntity = pedidoRepositorio.findById(id).orElse(null);
        
        // Si no existe, retornamos null
        if (pedidoEntity == null) {
            return null;
        }
        
        // Convertimos la entidad a un objeto del dominio
        return PedidoEntityConverter.convertToDomain(pedidoEntity);
    }
}
