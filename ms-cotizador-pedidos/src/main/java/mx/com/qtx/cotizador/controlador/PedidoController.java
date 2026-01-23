package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Pedidos", description = "Gestión de pedidos del sistema")
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
    @Operation(
        summary = "Generar pedido desde cotización",
        description = "Crea un nuevo pedido basado en una cotización existente para un proveedor específico"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido generado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cotización o proveedor no encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para generar pedidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
    @Operation(
        summary = "Consultar pedido por ID",
        description = "Obtiene la información detallada de un pedido específico"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para consultar pedidos")
    })
    public ResponseEntity<ApiResponse<PedidoResponse>> obtenerPedidoPorId(
            @Parameter(description = "ID del pedido a consultar", required = true)
            @PathVariable Integer id) {
        
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
    @Operation(
        summary = "Consultar todos los pedidos",
        description = "Obtiene una lista completa de todos los pedidos en el sistema"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para consultar pedidos")
    })
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
    
    /**
     * Cancelar un pedido existente
     * Permisos: ADMIN, GERENTE, VENDEDOR
     * 
     * @param pedidoId ID del pedido a cancelar
     * @param reason razón de la cancelación (opcional)
     * @return ResponseEntity con ApiResponse<String>
     */
    @PostMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @Operation(
        summary = "Cancelar pedido",
        description = "Cancela un pedido existente por razones específicas"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido cancelado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para cancelar pedidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al cancelar pedido")
    })
    public ResponseEntity<ApiResponse<String>> cancelarPedido(
            @Parameter(description = "ID del pedido a cancelar", required = true)
            @PathVariable Integer pedidoId,
            
            @Parameter(description = "Razón de la cancelación")
            @RequestParam(required = false, defaultValue = "Cancelación manual") String reason) {
        
        logger.info("Solicitud de cancelación de pedido {} con razón: {}", pedidoId, reason);
        
        // Llamar al servicio para cancelar el pedido
        ApiResponse<String> respuestaServicio = pedidoServicio.cancelarPedido(pedidoId, reason);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", 
                   respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
} 