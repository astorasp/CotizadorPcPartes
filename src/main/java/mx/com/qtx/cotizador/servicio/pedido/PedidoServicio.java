package mx.com.qtx.cotizador.servicio.pedido;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.pedidos.Pedido;
import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pedido.mapper.PedidoMapper;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.dto.pedido.request.PedidoCreateRequest;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PedidoRepositorio;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;
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
 * - Integración con ManejadorCreacionPedidos para generar desde cotizaciones
 */
@Service
public class PedidoServicio {

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
     * Crea un nuevo pedido (Caso 5.1: Agregar pedido)
     * 
     * @param request DTO con los datos del pedido a crear
     * @return ApiResponse<PedidoResponse> con el pedido creado
     */
    @Transactional
    public ApiResponse<PedidoResponse> crearPedido(PedidoCreateRequest request) {
        try {
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos del pedido son requeridos");
            }
            
            // Validar que el proveedor exista
            ApiResponse<ProveedorResponse> proveedorResponse = proveedorServicio.buscarPorClave(request.getCveProveedor());
            if (!"0".equals(proveedorResponse.getCodigo())) {
                return new ApiResponse<>(Errores.PROVEEDOR_REQUERIDO_PEDIDO.getCodigo(), 
                                       "Proveedor no encontrado: " + request.getCveProveedor());
            }
            
            // Validar que tenga detalles
            if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
                return new ApiResponse<>(Errores.PEDIDO_SIN_DETALLES.getCodigo(), 
                                       Errores.PEDIDO_SIN_DETALLES.getMensaje());
            }
            
            // Convertir ProveedorResponse a Proveedor dominio
            Proveedor proveedorDominio = convertirResponseADominio(proveedorResponse.getDatos());
            
            // Convertir DTO a dominio usando mapper (usamos 0 como placeholder, JPA generará automáticamente el ID)
            Pedido pedido = PedidoMapper.toPedido(request, proveedorDominio, 0L);
            
            // Persistir usando el método existente
            guardarPedidoInterno(pedido);
            
            // Convertir resultado a DTO
            PedidoResponse response = PedidoMapper.toResponse(pedido, proveedorResponse.getDatos());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Pedido creado exitosamente", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al crear pedido: " + e.getMessage());
        }
    }
    
    /**
     * Genera un pedido desde una cotización existente (Caso 5.2: Generar pedido)
     * 
     * @param request DTO con los datos para generar el pedido
     * @return ApiResponse<PedidoResponse> con el pedido generado
     */
    @Transactional
    public ApiResponse<PedidoResponse> generarPedidoDesdeCotizacion(GenerarPedidoRequest request) {
        try {
            if (request == null) {
                return new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
                                       "Los datos para generar el pedido son requeridos");
            }
            
                         // Buscar cotización (convirtiendo String a Integer)
             Integer cotizacionId;
             try {
                 cotizacionId = Integer.parseInt(request.getCotizacionId());
             } catch (NumberFormatException e) {
                 return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                        "ID de cotización inválido: " + request.getCotizacionId());
             }
             
             ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> cotizacionResponse = 
                 cotizacionServicio.buscarCotizacionPorId(cotizacionId);
             if (!"0".equals(cotizacionResponse.getCodigo())) {
                 return new ApiResponse<>(Errores.COTIZACION_NO_ENCONTRADA_PEDIDO.getCodigo(), 
                                        "Cotización no encontrada: " + request.getCotizacionId());
             }
             
                          // Validar que el proveedor exista
             ApiResponse<ProveedorResponse> proveedorResponse = proveedorServicio.buscarPorClave(request.getCveProveedor());
             if (!"0".equals(proveedorResponse.getCodigo())) {
                 return new ApiResponse<>(Errores.PROVEEDOR_REQUERIDO_PEDIDO.getCodigo(), 
                                        "Proveedor no encontrado: " + request.getCveProveedor());
             }
             
             // Por ahora, creamos un pedido básico desde la cotización encontrada
             // En una implementación completa, aquí iría la integración con ManejadorCreacionPedidos
             
             // Convertir ProveedorResponse a Proveedor dominio
             Proveedor proveedorDominio = convertirResponseADominio(proveedorResponse.getDatos());
             
             // Crear pedido básico (simulado desde cotización) - JPA generará automáticamente el ID
             Pedido pedidoGenerado = new Pedido(
                 0L, // Placeholder, JPA generará el ID automáticamente
                 request.getFechaEmision(),
                 request.getFechaEntrega(),
                 request.getNivelSurtido(),
                 proveedorDominio
             );
             
             // Agregar un detalle básico basado en la cotización
             // En implementación real, se extraerían todos los detalles de la cotización
             pedidoGenerado.agregarDetallePedido(
                 "ITEM-COT-" + cotizacionId,
                 "Item generado desde cotización " + cotizacionId,
                 1,
                 cotizacionResponse.getDatos().getTotal(),
                 cotizacionResponse.getDatos().getTotal()
             );
             
             // Persistir el pedido
             guardarPedidoInterno(pedidoGenerado);
             
             // Convertir a DTO y retornar
             PedidoResponse response = PedidoMapper.toResponse(pedidoGenerado, proveedorResponse.getDatos());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Pedido generado exitosamente desde cotización", response);
        } catch (Exception e) {
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
                                       Errores.PEDIDO_NO_ENCONTRADO.getMensaje());
            }
            
            // Convertir a dominio y luego a DTO
            Pedido pedido = PedidoEntityConverter.convertToDomain(pedidoEntity);
            PedidoResponse response = PedidoMapper.toResponse(pedido);
            
            return new ApiResponse<>(Errores.OK.getCodigo(), "Pedido encontrado", response);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al buscar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los pedidos (Caso 5.3: Consultar Pedido)
     * 
     * @return ApiResponse<List<PedidoResponse>> con la lista de pedidos
     */
    public ApiResponse<List<PedidoResponse>> obtenerTodosLosPedidos() {
        try {
            var pedidoEntities = pedidoRepositorio.findAll();
            
            List<PedidoResponse> pedidos = pedidoEntities.stream()
                .map(entity -> {
                    Pedido pedido = PedidoEntityConverter.convertToDomain(entity);
                    return PedidoMapper.toResponse(pedido);
                })
                .collect(Collectors.toList());
            
            return new ApiResponse<>(Errores.OK.getCodigo(), 
                                   "Pedidos obtenidos exitosamente (" + pedidos.size() + " encontrados)", 
                                   pedidos);
        } catch (Exception e) {
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   "Error al obtener pedidos: " + e.getMessage());
        }
    }
    
    // ==================== MÉTODOS PRIVADOS DE SOPORTE ====================
    
    /**
     * Obtiene todos los proveedores como objetos de dominio
     * 
     * @return Lista de proveedores del dominio
     */
    private List<Proveedor> obtenerProveedoresDominio() {
        ApiResponse<List<ProveedorResponse>> respuesta = proveedorServicio.obtenerTodosLosProveedores();
        
        if (!"0".equals(respuesta.getCodigo()) || respuesta.getDatos() == null) {
            throw new RuntimeException("Error al obtener proveedores: " + respuesta.getMensaje());
        }
        
        return respuesta.getDatos().stream()
            .map(this::convertirResponseADominio)
            .collect(Collectors.toList());
    }
    
    /**
     * Convierte un ProveedorResponse a objeto de dominio Proveedor
     * 
     * @param response DTO del proveedor
     * @return Objeto de dominio Proveedor
     */
    private Proveedor convertirResponseADominio(ProveedorResponse response) {
        return new Proveedor(response.getCve(), response.getNombre(), response.getRazonSocial());
    }
    
    /**
     * Método interno para persistir un pedido del dominio (reutiliza lógica existente)
     * 
     * @param pedido Objeto del dominio a persistir
     */
    private void guardarPedidoInterno(Pedido pedido) {
        // 1. Convertir el pedido del dominio a una entidad sin detalles
        var pedidoEntity = PedidoEntityConverter.convertToEntity(pedido, proveedorRepositorio);
        
        // 2. Persistir primero el pedido para obtener su ID generado
        pedidoEntity = pedidoRepositorio.save(pedidoEntity);
        
        // 3. Ahora que el pedido tiene ID, agregar los detalles
        PedidoEntityConverter.addDetallesTo(pedido, pedidoEntity, componenteRepositorio);

        // 4. Guardar nuevamente para persistir los detalles
        pedidoRepositorio.save(pedidoEntity);
    }
}
