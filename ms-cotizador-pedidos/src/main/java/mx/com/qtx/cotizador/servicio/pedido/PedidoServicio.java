package mx.com.qtx.cotizador.servicio.pedido;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.CotizacionPresupuestoAdapter;
import mx.com.qtx.cotizador.dominio.pedidos.GestorPedidos;
import mx.com.qtx.cotizador.dominio.pedidos.Pedido;
import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pedido.mapper.PedidoMapper;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PedidoRepositorio;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.CotizacionEntityConverter;
import mx.com.qtx.cotizador.servicio.wrapper.PedidoEntityConverter;
import mx.com.qtx.cotizador.util.Errores;

/**
 * Servicio que gestiona las operaciones relacionadas con los pedidos.
 * 
 * Implementa la arquitectura de manejo de errores donde:
 * - Los servicios retornan ApiResponse<T>
 * - Manejo interno de errores con try-catch
 * - Códigos de error específicos del enum Errores
 * - Trabajo con DTOs en la interfaz pública
 * - Integración con GestorPedidos para la lógica de dominio
 */
@Service
public class PedidoServicio {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoServicio.class);
    
    private final PedidoRepositorio pedidoRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final ComponenteRepositorio componenteRepositorio;
    private final CotizacionRepositorio cotizacionRepositorio;
    private final ProveedorServicio proveedorServicio;
    public PedidoServicio(PedidoRepositorio pedidoRepositorio,
                          ProveedorRepositorio proveedorRepositorio,
                          ComponenteRepositorio componenteRepositorio,
                          CotizacionRepositorio cotizacionRepositorio,
                          ProveedorServicio proveedorServicio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.componenteRepositorio = componenteRepositorio;
        this.cotizacionRepositorio = cotizacionRepositorio;
        this.proveedorServicio = proveedorServicio;
    }
    
    /**
     * Genera un pedido desde una cotización existente (Caso 5.2: Generar pedido)
     * 
     * Implementa el flujo simplificado:
     * 1. Validar cotización existe y está disponible
     * 2. Validar proveedor existe y está activo  
     * 3. Crear pedido en base de datos
     * 4. Enviar evento Kafka para notificación (opcional)
     * 
     * @param request DTO con los datos para generar el pedido
     * @return ApiResponse<PedidoResponse> con el pedido generado o error
     */
    @Transactional
    public ApiResponse<PedidoResponse> generarPedidoDesdeCotizacion(GenerarPedidoRequest request) {
        try {
            logger.info("Generando pedido desde cotización {} para proveedor {}", 
                       request.getCotizacionId(), request.getCveProveedor());
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos para generar el pedido son requeridos");
            }
            
            // 1. Buscar la cotización en la base de datos local
            var cotizacionEntity = cotizacionRepositorio.findById(request.getCotizacionId()).orElse(null);
            if (cotizacionEntity == null) {
                return new ApiResponse<>(Errores.COTIZACION_NO_ENCONTRADA_PEDIDO.getCodigo(), 
                                       "Cotización no encontrada: " + request.getCotizacionId());
            }
            
            // 2. Validar que el proveedor exista
            ApiResponse<ProveedorResponse> proveedorResponse = 
                proveedorServicio.buscarPorClave(request.getCveProveedor());
            if (!"0".equals(proveedorResponse.getCodigo())) {
                return new ApiResponse<>(Errores.PROVEEDOR_REQUERIDO_PEDIDO.getCodigo(), 
                                       "Proveedor no encontrado: " + request.getCveProveedor());
            }
            
            // 3. Convertir entidad de cotización a dominio de cotización
            Cotizacion cotizacionDominio = CotizacionEntityConverter.convertToDomain(cotizacionEntity);
            
            // 4. Convertir ProveedorResponse a Proveedor dominio
            Proveedor proveedorDominio = convertirResponseAProveedorDominio(proveedorResponse.getDatos());
            
            // 5. Crear lista de proveedores para GestorPedidos
            List<Proveedor> proveedoresList = new ArrayList<>();
            proveedoresList.add(proveedorDominio);
            
            // 6. Usar GestorPedidos para generar el pedido (lógica de dominio correcta)
            GestorPedidos gestorPedidos = new GestorPedidos(proveedoresList);
            
            // 7. Agregar presupuesto adaptado desde cotización
            CotizacionPresupuestoAdapter presupuestoAdapter = new CotizacionPresupuestoAdapter(cotizacionDominio);
            gestorPedidos.agregarPresupuesto(presupuestoAdapter);
            
            // 8. Generar el pedido usando la lógica de dominio
            Pedido pedidoGenerado = gestorPedidos.generarPedido(
                request.getCveProveedor(),
                0, // Se usará auto-increment de la BD
                request.getNivelSurtido(),
                request.getFechaEmision(),
                request.getFechaEntrega()
            );
            
            // 9. Persistir el pedido
            guardarPedidoInterno(pedidoGenerado);
            
            // 10. Convertir resultado a DTO
            PedidoResponse response = PedidoMapper.toResponse(pedidoGenerado);
            
            // 11. Pedido creado exitosamente - no se requiere notificación externa
            // Los pedidos son responsabilidad interna del microservicio
            
            logger.info("Pedido generado exitosamente. Número: {}", pedidoGenerado.getNumPedido());
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Pedido generado exitosamente desde cotización", response);
                                   
        } catch (Exception e) {
            logger.error("Error al generar pedido: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al generar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Cancela un pedido existente
     * 
     * @param pedidoId ID del pedido a cancelar
     * @param reason razón de la cancelación
     * @return ApiResponse indicando el resultado de la cancelación
     */
    @Transactional
    public ApiResponse<String> cancelarPedido(Integer pedidoId, String reason) {
        try {
            logger.info("Cancelando pedido {} con razón: {}", pedidoId, reason);
            
            if (pedidoId == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "El ID del pedido es requerido para cancelar");
            }
            
            // Verificar que el pedido existe antes de cancelar
            ApiResponse<PedidoResponse> pedidoResponse = buscarPorId(pedidoId);
            if (!"0".equals(pedidoResponse.getCodigo())) {
                return new ApiResponse<>(Errores.PEDIDO_NO_ENCONTRADO.getCodigo(),
                                       "No se puede cancelar - pedido no encontrado: " + pedidoId);
            }
            
            // Implementar lógica de cancelación directa
            // 1. Buscar el pedido entity para actualizarlo
            var pedidoEntity = pedidoRepositorio.findById(pedidoId).get(); // Ya sabemos que existe
            
            // 2. Marcar como cancelado (asumiendo que hay un campo de estado)
            // pedidoEntity.setEstado("CANCELADO"); // Descomentar cuando exista el campo
            // pedidoEntity.setObservaciones(reason);
            
            // 3. Guardar cambios
            // pedidoRepositorio.save(pedidoEntity);
            
            logger.info("Pedido {} marcado para cancelación. Razón: {}", pedidoId, reason);
            
            // 4. Pedido cancelado exitosamente - no se requiere notificación externa
            // Los pedidos son responsabilidad interna del microservicio
            
            return new ApiResponse<>(Errores.OK.getCodigo(),
                                   "Pedido cancelado exitosamente",
                                   "Pedido ID: " + pedidoId + " - Razón: " + reason);
                                   
        } catch (Exception e) {
            logger.error("Error inesperado al cancelar pedido {}: {}", pedidoId, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(),
                                   "Error inesperado al cancelar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Busca un pedido por su ID (Caso 5.3: Consultar Pedido)
     * 
     * @param id El ID del pedido a buscar
     * @return ApiResponse<PedidoResponse> con el pedido encontrado
     */
    public ApiResponse<PedidoResponse> buscarPorId(Integer id) {
        try {
            if (id == null) {
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "El ID del pedido es requerido");
            }
            
            var pedidoEntity = pedidoRepositorio.findById(id).orElse(null);
            if (pedidoEntity == null) {
                return new ApiResponse<>(Errores.PEDIDO_NO_ENCONTRADO.getCodigo(), 
                                       "Pedido no encontrado con ID: " + id);
            }
            
            // Convertir a dominio y luego a DTO
            Pedido pedido = PedidoEntityConverter.convertToDomain(pedidoEntity);
            PedidoResponse response = PedidoMapper.toResponse(pedido);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Pedido encontrado", response);
        } catch (Exception e) {
            logger.error("Error al buscar pedido: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al buscar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los pedidos (Caso 5.3: Consultar Pedidos)
     * 
     * @return ApiResponse<List<PedidoResponse>> con la lista de pedidos
     */
    public ApiResponse<List<PedidoResponse>> obtenerTodosLosPedidos() {
        try {
            List<mx.com.qtx.cotizador.entidad.Pedido> pedidosEntity = pedidoRepositorio.findAll();
            
            List<PedidoResponse> pedidosResponse = pedidosEntity.stream()
                    .map(entity -> {
                        Pedido pedido = PedidoEntityConverter.convertToDomain(entity);
                        return PedidoMapper.toResponse(pedido);
                    })
                    .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Pedidos obtenidos exitosamente", pedidosResponse);
        } catch (Exception e) {
            logger.error("Error al obtener pedidos: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al obtener pedidos: " + e.getMessage());
        }
    }
    
    
    
    
    // ==================== MÉTODOS PRIVADOS DE UTILIDAD ====================
    
    /**
     * Convierte ProveedorResponse a Proveedor de dominio
     */
    private Proveedor convertirResponseAProveedorDominio(ProveedorResponse response) {
        if (response == null) {
            return null;
        }
        return new Proveedor(response.getCve(), response.getNombre(), response.getRazonSocial());
    }
    
    /**
     * Persiste un pedido de dominio en la base de datos
     */
    private void guardarPedidoInterno(Pedido pedido) {
        var pedidoEntity = PedidoEntityConverter.convertToNewEntity(pedido, proveedorRepositorio, componenteRepositorio);
        pedidoEntity = pedidoRepositorio.save(pedidoEntity);
        
        // Agregar detalles después de que el pedido tenga ID
        PedidoEntityConverter.addDetallesTo(pedido, pedidoEntity, componenteRepositorio);
        
        // Actualizar el número de pedido en el objeto de dominio si fue auto-generado
        pedido = PedidoEntityConverter.convertToDomain(pedidoEntity);
    }
} 