package mx.com.qtx.cotizadorv1ds.servicios.wrapper;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;


import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Componente;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetallePedido;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetallePedido.DetallePedidoId;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ComponenteRepositorio;
import mx.com.qtx.cotizadorv1ds.persistencia.repositorios.ProveedorRepositorio;

/**
 * Conversor para transformar objetos Pedido del dominio de negocio a entidades Pedido para persistencia.
 * Maneja la conversión bidireccional entre mx.com.qtx.cotizadorv1ds.pedidos.Pedido y 
 * mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido, incluyendo sus respectivos detalles.
 */
public class PedidoEntityConverter {

    /**
     * Convierte un pedido del dominio a una entidad de persistencia.
     * No incluye los detalles del pedido.
     * 
     * @param pedidoCore Objeto Pedido del dominio de negocio
     * @param proveedorRepo Repositorio para buscar el proveedor en la base de datos
     * @return Una entidad Pedido lista para persistir, sin detalles
     */
    public static mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido convertToEntity(
            mx.com.qtx.cotizadorv1ds.pedidos.Pedido pedidoCore,
            ProveedorRepositorio proveedorRepo) {
        
        if (pedidoCore == null) {
            return null;
        }
        
        mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido pedidoEntity = 
                new mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido();
        
        // Convertir campos simples
        // Nota: El ID se generará automáticamente por la base de datos para nuevos pedidos
        
        // Convertir fecha de emisión a LocalDateTime
        if (pedidoCore.getFechaEmision() != null) {
            pedidoEntity.setFechaEmision(
                    LocalDate.of(
                            pedidoCore.getFechaEmision().getYear(),
                            pedidoCore.getFechaEmision().getMonth(),
                            pedidoCore.getFechaEmision().getDayOfMonth())
            );
        }
        
        // Establecer fecha de entrega
        if (pedidoCore.getFechaEntrega() != null) {
            pedidoEntity.setFechaEntrega(
                    LocalDate.of(
                            pedidoCore.getFechaEntrega().getYear(),
                            pedidoCore.getFechaEntrega().getMonth(),
                            pedidoCore.getFechaEntrega().getDayOfMonth())
            );
        }
        
        // Establecer nivel de surtido
        pedidoEntity.setNivelSurtido(pedidoCore.getNivelSurtido());
        
        // Buscar proveedor por su clave si existe
        if (pedidoCore.getProveedor() != null && proveedorRepo != null) {
            String claveProveedor = pedidoCore.getProveedor().getCve();
            // Buscamos directamente por la clave del proveedor
            pedidoEntity.setProveedor(
                proveedorRepo.findByCve(claveProveedor)
            );
        }
        
        return pedidoEntity;
    }
    
    /**
     * Agrega los detalles del pedido a una entidad de pedido existente.
     * Debe llamarse después de que la entidad Pedido haya sido persistida y tenga un ID asignado.
     * 
     * @param pedidoCore Objeto Pedido del dominio de negocio con sus detalles
     * @param pedidoEntity Entidad Pedido ya persistida con ID generado
     * @param componenteRepo Repositorio para buscar los componentes asociados a los detalles
     */
    public static void addDetallesTo(
            mx.com.qtx.cotizadorv1ds.pedidos.Pedido pedidoCore,
            mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido pedidoEntity,
            ComponenteRepositorio componenteRepo) {
        
        if (pedidoCore == null || pedidoEntity == null) {
            return;
        }
        
        // Limpiar detalles existentes para evitar duplicados
        pedidoEntity.getDetalles().clear();
        
        // Calcular total acumulado
        BigDecimal totalAcumulado = BigDecimal.ZERO;
        
        // Obtener los detalles del pedido del dominio
        List<mx.com.qtx.cotizadorv1ds.pedidos.DetallePedido> detallesCore = 
                pedidoCore.getDetallesPedido();
        
        // Convertir cada detalle
        int numDetalle = 1;
        for (mx.com.qtx.cotizadorv1ds.pedidos.DetallePedido detalleCore : detallesCore) {
            // Crear detalle de entidad
            DetallePedido detalleEntity = new DetallePedido();
            
            // Crear una nueva instancia de DetallePedido con su ID compuesto
            DetallePedidoId detalleId = new DetallePedidoId();
            detalleId.setIdPedido(pedidoEntity.getNumPedido());
            detalleId.setNumDetalle(numDetalle); 
            detalleEntity.setId(detalleId);                     
            
            // Configurar campos del detalle
            detalleEntity.setCantidad(detalleCore.getCantidad());
            detalleEntity.setPrecioUnitario(detalleCore.getPrecioUnitario());
            detalleEntity.setTotalCotizado(detalleCore.getTotalCotizado());
            
            // Buscar componente por ID si existe
            if (componenteRepo != null) {
                Componente componente = componenteRepo.findById(detalleCore.getIdArticulo()).orElse(null);
                detalleEntity.setComponente(componente);
            }
            
            // Establecer la relación con el pedido
            pedidoEntity.addDetalle(detalleEntity);
            
            // Acumular al total
            totalAcumulado = totalAcumulado.add(detalleCore.getTotalCotizado());
            
            numDetalle++;
        }
        
        // Calcular el total del pedido sumando los totales de los detalles (informativo)
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido detalle : pedidoEntity.getDetalles()) {
            if (detalle.getTotalCotizado() != null) {
                total = total.add(detalle.getTotalCotizado());
            }
        }
    }
    
    /**
     * Método completo para convertir un pedido del dominio y todos sus detalles a una nueva entidad de persistencia.
     * Nota: La entidad resultante no tendrá un ID asignado hasta que se persista en la base de datos.
     * 
     * @param pedidoCore Objeto Pedido del dominio de negocio
     * @param proveedorRepo Repositorio para buscar el proveedor
     * @param componenteRepo Repositorio para buscar los componentes
     * @return Una nueva entidad Pedido con todos sus detalles lista para persistir
     */
    public static mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido convertToNewEntity(
            mx.com.qtx.cotizadorv1ds.pedidos.Pedido pedidoCore,
            ProveedorRepositorio proveedorRepo,
            ComponenteRepositorio componenteRepo) {
        
        if (pedidoCore == null) {
            return null;
        }
        
        // Primero convertimos el pedido principal sin detalles
        mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido pedidoEntity = 
                convertToEntity(pedidoCore, proveedorRepo);
        
        // Calcular el total del pedido sumando los totales de los detalles (informativo)
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido detalle : pedidoEntity.getDetalles()) {
            if (detalle.getTotalCotizado() != null) {
                total = total.add(detalle.getTotalCotizado());
            }
        }
        
        return pedidoEntity;
        
        // Nota: Los detalles no pueden agregarse en este punto porque el pedido aún no tiene un ID.
        // Se deben agregar después de que el pedido sea persistido usando el método addDetallesTo().
    }
    
    /**
     * Convierte una entidad de persistencia a un objeto del dominio de negocio.
     * Incluye la conversión de todos sus detalles.
     * 
     * @param pedidoEntity La entidad Pedido a convertir
     * @return Un objeto Pedido del dominio de negocio
     */
    public static mx.com.qtx.cotizadorv1ds.pedidos.Pedido convertToDomain(
            mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido pedidoEntity) {
        
        if (pedidoEntity == null) {
            return null;
        }
        
        // Verificar el número de pedido (debe existir)
        if (pedidoEntity.getNumPedido() == null) {
            throw new IllegalArgumentException(
                    "La entidad Pedido debe tener un número de pedido asignado antes de agregar detalles");
        }
        
        // Convertir la fecha de emisión de LocalDateTime a LocalDate
        java.time.LocalDate fechaEmision = null;
        if (pedidoEntity.getFechaEmision() != null) {
            fechaEmision = pedidoEntity.getFechaEmision();
        } else {
            fechaEmision = java.time.LocalDate.now(); // Valor por defecto
        }
        
        // Obtener la fecha de entrega
        java.time.LocalDate fechaEntrega = null;
        if (pedidoEntity.getFechaEntrega() != null) {
            fechaEntrega = pedidoEntity.getFechaEntrega();
        } else {
            fechaEntrega = fechaEmision.plusDays(7); // Valor por defecto: 7 días después
        }
        
        // Obtener el nivel de surtido
        int nivelSurtido = pedidoEntity.getNivelSurtido() != null ? 
            pedidoEntity.getNivelSurtido() : 0; // Valor por defecto
        
        // Crear el proveedor del dominio
        mx.com.qtx.cotizadorv1ds.pedidos.Proveedor proveedorDomain = null;
        if (pedidoEntity.getProveedor() != null) {
            // Extraer los datos del proveedor de la entidad
            String cveProveedor = pedidoEntity.getProveedor().getCve();
            String nombreProveedor = pedidoEntity.getProveedor().getNombre() != null ?
                pedidoEntity.getProveedor().getNombre() : "Sin nombre";
            
            // Obtener la razón social directamente del campo
            String razonSocial = pedidoEntity.getProveedor().getRazonSocial() != null ?
                pedidoEntity.getProveedor().getRazonSocial() : nombreProveedor;
            
            // Crear el proveedor del dominio con los tres parámetros requeridos
            proveedorDomain = new mx.com.qtx.cotizadorv1ds.pedidos.Proveedor(
                    cveProveedor, nombreProveedor, razonSocial);
        }
        
        // Crear el pedido del dominio con los datos básicos
        // Obtener el número de pedido
        long numPedido = pedidoEntity.getNumPedido() != null ? pedidoEntity.getNumPedido().longValue() : 0L;
        
        mx.com.qtx.cotizadorv1ds.pedidos.Pedido pedidoDomain = new mx.com.qtx.cotizadorv1ds.pedidos.Pedido(
                numPedido, fechaEmision, fechaEntrega, nivelSurtido, proveedorDomain);
        
        // Convertir y agregar detalles
        if (pedidoEntity.getDetalles() != null) {
            for (mx.com.qtx.cotizadorv1ds.persistencia.entidades.DetallePedido detalleEntity : pedidoEntity.getDetalles()) {
                // Extraer datos del detalle de la entidad
                String idArticulo = detalleEntity.getComponente() != null ? 
                        detalleEntity.getComponente().getId() : "N/A";
                        
                String descripcion = detalleEntity.getComponente() != null ? 
                        detalleEntity.getComponente().getDescripcion() : "Sin descripción";
                        
                int cantidad = detalleEntity.getCantidad() != null ? 
                        detalleEntity.getCantidad() : 0;
                        
                java.math.BigDecimal precioUnitario = detalleEntity.getPrecioUnitario() != null ? 
                        detalleEntity.getPrecioUnitario() : java.math.BigDecimal.ZERO;
                        
                java.math.BigDecimal totalCotizado = detalleEntity.getTotalCotizado() != null ? 
                        detalleEntity.getTotalCotizado() : java.math.BigDecimal.ZERO;
                
                // Agregar el detalle al objeto del dominio
                pedidoDomain.agregarDetallePedido(idArticulo, descripcion, cantidad, 
                        precioUnitario, totalCotizado);
            }
        }
        
        return pedidoDomain;
    }
}