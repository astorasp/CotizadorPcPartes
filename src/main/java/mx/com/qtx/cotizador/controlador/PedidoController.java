package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.dto.pedido.request.PedidoCreateRequest;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.servicio.pedido.PedidoServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de pedidos
 * 
 * Implementa los casos de uso:
 * - 5.1 Agregar pedido
 * - 5.2 Generar pedido (desde cotización)
 * - 5.3 Consultar pedidos
 * 
 * Arquitectura consistente:
 * - Servicios retornan ApiResponse<T>
 * - Mapeo automático de códigos de error a HTTP status
 * - Solo manejo de DTOs, nunca objetos de dominio
 * - Logging completo para auditoría
 */
@Slf4j
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    
    private final PedidoServicio pedidoServicio;
    
    /**
     * Caso de uso 5.1: Agregar pedido
     * 
     * @param request DTO con los datos del nuevo pedido
     * @return ResponseEntity con ApiResponse<PedidoResponse>
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> crearPedido(
            @Valid @RequestBody PedidoCreateRequest request) {
        
        log.info("Iniciando creación de pedido para proveedor: {}", request.getCveProveedor());
        
        // Llamar al servicio para crear el pedido
        ApiResponse<PedidoResponse> respuestaServicio = pedidoServicio.crearPedido(request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 5.2: Generar pedido desde cotización
     * 
     * @param request DTO con los datos para generar el pedido
     * @return ResponseEntity con ApiResponse<PedidoResponse>
     */
    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<PedidoResponse>> generarPedidoDesdeCotizacion(
            @Valid @RequestBody GenerarPedidoRequest request) {
        
        log.info("Iniciando generación de pedido desde cotización: {} para proveedor: {}", 
                request.getCotizacionId(), request.getCveProveedor());
        
        // Llamar al servicio para generar el pedido desde cotización
        ApiResponse<PedidoResponse> respuestaServicio = pedidoServicio.generarPedidoDesdeCotizacion(request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 5.3: Consultar pedido específico por ID
     * 
     * @param id ID del pedido a consultar
     * @return ResponseEntity con ApiResponse<PedidoResponse>
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> obtenerPedidoPorId(@PathVariable Integer id) {
        
        log.info("Consultando pedido con ID: {}", id);
        
        // Llamar al servicio para buscar el pedido
        ApiResponse<PedidoResponse> respuestaServicio = pedidoServicio.buscarPorId(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 5.3: Consultar todos los pedidos
     * 
     * @return ResponseEntity con ApiResponse<List<PedidoResponse>>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> obtenerTodosLosPedidos() {
        
        log.info("Consultando todos los pedidos");
        
        // Llamar al servicio para obtener todos los pedidos
        ApiResponse<List<PedidoResponse>> respuestaServicio = pedidoServicio.obtenerTodosLosPedidos();
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    // ==================== ENDPOINTS ADICIONALES FUTUROS ====================
    
    /**
     * Búsqueda de pedidos por proveedor
     * 
     * @param cveProveedor Clave del proveedor
     * @return ResponseEntity con ApiResponse<List<PedidoResponse>>
     */
    @GetMapping("/proveedor/{cveProveedor}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> buscarPedidosPorProveedor(
            @PathVariable String cveProveedor) {
        
        log.info("Buscando pedidos por proveedor: {}", cveProveedor);
        
        // Por ahora retornamos respuesta básica - se puede implementar más adelante
        ApiResponse<List<PedidoResponse>> respuestaServicio = new ApiResponse<>(
            "0", "Funcionalidad en desarrollo", List.of());
        
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Búsqueda de pedidos por estado
     * 
     * @param estado Estado del pedido a buscar
     * @return ResponseEntity con ApiResponse<List<PedidoResponse>>
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> buscarPedidosPorEstado(
            @PathVariable String estado) {
        
        log.info("Buscando pedidos por estado: {}", estado);
        
        // Por ahora retornamos respuesta básica - se puede implementar más adelante
        ApiResponse<List<PedidoResponse>> respuestaServicio = new ApiResponse<>(
            "0", "Funcionalidad en desarrollo", List.of());
        
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
} 