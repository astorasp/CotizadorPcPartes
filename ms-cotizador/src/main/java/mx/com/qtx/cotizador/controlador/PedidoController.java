package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.dto.pedido.response.PedidoResponse;
import mx.com.qtx.cotizador.servicio.pedido.PedidoServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;

import java.util.List;

/**
 * Controlador REST para la gestión de pedidos
 * 
 * Implementa los casos de uso:
 * - 5.2 Generar pedido (desde cotización)
 * - 5.3 Consultar pedidos
 * 
 * Arquitectura consistente:
 * - Servicios retornan ApiResponse<T>
 * - Mapeo automático de códigos de error a HTTP status
 * - Solo manejo de DTOs, nunca objetos de dominio
 * - Logging completo para auditoría
 * 
 * Implementa control de acceso basado en roles:
 * - ADMIN: Acceso completo (todos los endpoints)
 * - GERENTE: Gestión y aprobación de pedidos
 * - VENDEDOR: Generación y consulta de pedidos
 * - INVENTARIO: Gestión de cumplimiento e inventario
 * - CONSULTOR: Solo lectura para análisis y reportes
 */
@RestController
@RequestMapping("/pedidos")
@CrossOrigin
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'INVENTARIO', 'CONSULTOR')")
public class PedidoController {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    
    private final PedidoServicio pedidoServicio;
    
    public PedidoController(PedidoServicio pedidoServicio) {
        this.pedidoServicio = pedidoServicio;
    }
    
    /**
     * Caso de uso 5.2: Generar pedido desde cotización
     * Permisos: ADMIN, GERENTE, VENDEDOR, INVENTARIO
     * 
     * @param request DTO con los datos para generar el pedido
     * @return ResponseEntity con ApiResponse<PedidoResponse>
     */
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<PedidoResponse>> generarPedidoDesdeCotizacion(
            @Valid @RequestBody GenerarPedidoRequest request) {
        
        logger.info("Iniciando generación de pedido desde cotización: {} para proveedor: {}", 
                request.getCotizacionId(), request.getCveProveedor());
        
        // Llamar al servicio para generar el pedido desde cotización
        ApiResponse<PedidoResponse> respuestaServicio = pedidoServicio.generarPedidoDesdeCotizacion(request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", 
                   respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 5.3: Consultar pedido específico por ID
     * Permisos: Todos los roles (datos filtrados según el rol)
     * 
     * @param id ID del pedido a consultar
     * @return ResponseEntity con ApiResponse<PedidoResponse>
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> obtenerPedidoPorId(@PathVariable Integer id) {
        
        logger.info("Consultando pedido con ID: {}", id);
        
        // Llamar al servicio para buscar el pedido
        ApiResponse<PedidoResponse> respuestaServicio = pedidoServicio.buscarPorId(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", 
                   respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 5.3: Consultar todos los pedidos
     * Permisos: Todos los roles (datos filtrados según el rol)
     * 
     * @return ResponseEntity con ApiResponse<List<PedidoResponse>>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> obtenerTodosLosPedidos() {
        
        logger.info("Consultando todos los pedidos");
        
        // Llamar al servicio para obtener todos los pedidos
        ApiResponse<List<PedidoResponse>> respuestaServicio = pedidoServicio.obtenerTodosLosPedidos();
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", 
                   respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
} 