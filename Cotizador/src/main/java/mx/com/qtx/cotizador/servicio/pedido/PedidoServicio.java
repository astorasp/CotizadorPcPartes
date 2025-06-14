package mx.com.qtx.cotizador.servicio.pedido;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;
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
    private final ProveedorServicio proveedorServicio;
    private final CotizacionServicio cotizacionServicio;
    
    public PedidoServicio(PedidoRepositorio pedidoRepositorio,
                          ProveedorRepositorio proveedorRepositorio,
                          ComponenteRepositorio componenteRepositorio,
                          ProveedorServicio proveedorServicio,
                          CotizacionServicio cotizacionServicio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.componenteRepositorio = componenteRepositorio;
        this.proveedorServicio = proveedorServicio;
        this.cotizacionServicio = cotizacionServicio;
    }
    
    /**
     * Genera un pedido desde una cotización existente (Caso 5.2: Generar pedido)
     * Usa GestorPedidos y CotizacionPresupuestoAdapter para aplicar correctamente
     * la lógica de dominio en la generación del pedido
     * 
     * @param request DTO con los datos para generar el pedido
     * @return ApiResponse<PedidoResponse> con el pedido generado
     */
    @Transactional
    public ApiResponse<PedidoResponse> generarPedidoDesdeCotizacion(GenerarPedidoRequest request) {
        try {
            logger.info("Iniciando generación de pedido desde cotización {} para proveedor {}", 
                       request.getCotizacionId(), request.getCveProveedor());
            
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos para generar el pedido son requeridos");
            }
            
            // 1. Buscar la cotización
            ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> cotizacionResponse = 
                cotizacionServicio.buscarCotizacionPorId(request.getCotizacionId());
            if (!"0".equals(cotizacionResponse.getCodigo())) {
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
            mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntity = cotizacionResponse.getDatos();
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
            
            logger.info("Pedido generado exitosamente con número: {}", pedidoGenerado.getNumPedido());
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Pedido generado exitosamente desde cotización", response);
                                   
        } catch (Exception e) {
            logger.error("Error al generar pedido desde cotización: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al generar pedido: " + e.getMessage());
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