package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorCreateRequest;
import mx.com.qtx.cotizador.dto.proveedor.request.ProveedorUpdateRequest;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;
import mx.com.qtx.cotizador.servicio.pedido.ProveedorServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de proveedores
 * 
 * Implementa los casos de uso:
 * - 4.1 Agregar proveedor
 * - 4.2 Modificar proveedor  
 * - 4.3 Consultar proveedores
 * - 4.4 Eliminar proveedor
 * 
 * Arquitectura consistente:
 * - Servicios retornan ApiResponse<T>
 * - Mapeo automático de códigos de error a HTTP status
 * - Solo manejo de DTOs, nunca objetos de dominio
 * 
 * Implementa control de acceso basado en roles:
 * - ADMIN: Acceso completo (todos los endpoints)
 * - GERENTE: Gestión comercial (crear, ver, modificar, buscar - sin eliminar)
 * - VENDEDOR: Solo lectura para proceso de ventas (ver, buscar)
 * - INVENTARIO: Gestión de inventario (crear, ver, modificar, buscar - sin eliminar)
 * - CONSULTOR: Solo lectura para consultoría (ver, buscar)
 */
@RestController
@RequestMapping("/proveedores/v1/api")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'INVENTARIO', 'CONSULTOR')")
public class ProveedorController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProveedorController.class);
    
    private final ProveedorServicio proveedorServicio;
    
    public ProveedorController(ProveedorServicio proveedorServicio) {
        this.proveedorServicio = proveedorServicio;
    }
    
    /**
     * Caso de uso 4.1: Agregar proveedor
     * Permisos: ADMIN, GERENTE, INVENTARIO
     * 
     * @param request DTO con los datos del nuevo proveedor
     * @return ResponseEntity con ApiResponse<ProveedorResponse>
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<ProveedorResponse>> crearProveedor(
            @Valid @RequestBody ProveedorCreateRequest request) {
        
        logger.info("Iniciando creación de proveedor con clave: {}", request.getCve());
        
        // Llamar al servicio para crear el proveedor
        ApiResponse<ProveedorResponse> respuestaServicio = proveedorServicio.crearProveedor(request);
        
        // LOGGING CRÍTICO PARA DEBUG
        logger.debug("=== CONTROLLER DEBUG ===");
        logger.debug("Respuesta servicio código: {}", respuestaServicio.getCodigo());
        logger.debug("Respuesta servicio mensaje: {}", respuestaServicio.getMensaje());
        logger.debug("Respuesta servicio datos: {}", respuestaServicio.getDatos());
        if (respuestaServicio.getDatos() != null) {
            ProveedorResponse data = respuestaServicio.getDatos();
            logger.debug("Data.getCve(): {}", data.getCve());
            logger.debug("Data.getNombre(): {}", data.getNombre());
            logger.debug("Data.getRazonSocial(): {}", data.getRazonSocial());
            logger.debug("Data.getNumeroPedidos(): {}", data.getNumeroPedidos());
        } else {
            logger.debug("DATA ES NULL EN EL CONTROLADOR (esto es normal para errores)");
        }
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        
        ResponseEntity<ApiResponse<ProveedorResponse>> responseEntity = ResponseEntity.status(httpStatus).body(respuestaServicio);
        logger.debug("ResponseEntity creado, body datos: {}", responseEntity.getBody().getDatos());
        logger.debug("=== FIN CONTROLLER DEBUG ===");
        
        return responseEntity;
    }
    
    /**
     * Caso de uso 4.2: Modificar proveedor
     * Permisos: ADMIN, GERENTE, INVENTARIO
     * 
     * @param cve Clave del proveedor a modificar
     * @param request DTO con los datos actualizados
     * @return ResponseEntity con ApiResponse<ProveedorResponse>
     */
    @PutMapping("/{cve}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizarProveedor(
            @PathVariable String cve,
            @Valid @RequestBody ProveedorUpdateRequest request) {
        
        logger.info("Iniciando actualización de proveedor con clave: {}", cve);
        
        // Llamar al servicio para actualizar el proveedor
        ApiResponse<ProveedorResponse> respuestaServicio = proveedorServicio.actualizarProveedor(cve, request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 4.3: Consultar proveedor específico por clave
     * Permisos: Todos los roles (datos filtrados según el rol)
     * 
     * @param cve Clave del proveedor a consultar
     * @return ResponseEntity con ApiResponse<ProveedorResponse>
     */
    @GetMapping("/{cve}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtenerProveedorPorClave(@PathVariable String cve) {
        
        logger.info("Consultando proveedor con clave: {}", cve);
        
        // Llamar al servicio para buscar el proveedor
        ApiResponse<ProveedorResponse> respuestaServicio = proveedorServicio.buscarPorClave(cve);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 4.3: Consultar todos los proveedores
     * Permisos: Todos los roles (datos filtrados según el rol)
     * 
     * @return ResponseEntity con ApiResponse<List<ProveedorResponse>>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> obtenerTodosLosProveedores() {
        
        logger.info("Consultando todos los proveedores");
        
        // Llamar al servicio para obtener todos los proveedores
        ApiResponse<List<ProveedorResponse>> respuestaServicio = proveedorServicio.obtenerTodosLosProveedores();
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 4.4: Eliminar proveedor
     * Permisos: Solo ADMIN (operación crítica para integridad del sistema)
     * 
     * @param cve Clave del proveedor a eliminar
     * @return ResponseEntity con ApiResponse<Void>
     */
    @DeleteMapping("/{cve}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminarProveedor(@PathVariable String cve) {
        
        logger.info("Iniciando eliminación de proveedor con clave: {}", cve);
        
        // Llamar al servicio para eliminar el proveedor
        ApiResponse<Void> respuestaServicio = proveedorServicio.eliminarProveedor(cve);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    // ==================== ENDPOINTS ADICIONALES DE BÚSQUEDA ====================
    
    /**
     * Búsqueda de proveedores por nombre (búsqueda parcial)
     * Permisos: Todos los roles (para consultas y reportes)
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return ResponseEntity con ApiResponse<List<ProveedorResponse>>
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> buscarProveedoresPorNombre(
            @RequestParam String nombre) {
        
        logger.info("Buscando proveedores por nombre: {}", nombre);
        
        // Llamar al servicio para buscar por nombre
        ApiResponse<List<ProveedorResponse>> respuestaServicio = proveedorServicio.buscarPorNombre(nombre);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Búsqueda de proveedores por razón social (búsqueda parcial)
     * Permisos: Todos los roles (para consultas y reportes)
     * 
     * @param razonSocial Razón social o parte de la razón social a buscar
     * @return ResponseEntity con ApiResponse<List<ProveedorResponse>>
     */
    @GetMapping("/buscar/razon-social")
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> buscarProveedoresPorRazonSocial(
            @RequestParam String razonSocial) {
        
        logger.info("Buscando proveedores por razón social: {}", razonSocial);
        
        // Llamar al servicio para buscar por razón social
        ApiResponse<List<ProveedorResponse>> respuestaServicio = proveedorServicio.buscarPorRazonSocial(razonSocial);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        logger.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
} 