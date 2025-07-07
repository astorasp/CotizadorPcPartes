package mx.com.qtx.cotizador.controlador;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionUpdateRequest;
import mx.com.qtx.cotizador.dto.promocion.response.PromocionResponse;
import mx.com.qtx.cotizador.servicio.promocion.PromocionServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;

/**
 * Controlador REST para gestión de promociones.
 * 
 * Implementa los 4 casos de uso del diagrama:
 * - 6.1 Agregar promoción (POST)
 * - 6.2 Modificar promoción (PUT)  
 * - 6.3 Consultar promociones (GET)
 * - 6.4 Eliminar promociones (DELETE)
 * 
 * Arquitectura:
 * - No usa try-catch (delegado al servicio)
 * - Mapea códigos de ApiResponse a HTTP status con HttpStatusMapper
 * - Validación automática con @Valid
 * - Logging de todas las operaciones
 * 
 * Implementa control de acceso basado en roles con alto nivel de restricción
 * debido al impacto financiero directo de las promociones:
 * - ADMIN: Acceso completo (todos los endpoints)
 * - GERENTE: Gestión estratégica (crear, modificar, eliminar, consultar)
 * - VENDEDOR: Solo lectura para aplicar promociones en ventas
 * - INVENTARIO: Solo lectura para consultar impacto en inventario
 * - CONSULTOR: Solo lectura para análisis de efectividad
 */
@RestController
@RequestMapping("/promociones")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'INVENTARIO', 'CONSULTOR')")
public class PromocionControlador {
    
    private static final Logger logger = LoggerFactory.getLogger(PromocionControlador.class);
    
    private final PromocionServicio promocionServicio;
    
    public PromocionControlador(PromocionServicio promocionServicio) {
        this.promocionServicio = promocionServicio;
    }
    
    /**
     * Caso 6.1: Agregar promoción
     * Permisos: Solo ADMIN y GERENTE (alto impacto financiero)
     * 
     * POST /api/promociones
     * Content-Type: application/json
     * 
     * Crea una nueva promoción con toda su configuración compleja.
     * 
     * @param request DTO con datos de la promoción a crear
     * @return ResponseEntity con la promoción creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponse<PromocionResponse>> crearPromocion(
            @Valid @RequestBody PromocionCreateRequest request) {
        
        logger.info("POST /api/promociones - Creando promoción: {}", request.getNombre());
        
        ApiResponse<PromocionResponse> response = promocionServicio.crearPromocion(request);
        
        // Mapear código de respuesta a HTTP status
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Caso 6.2: Modificar promoción
     * Permisos: Solo ADMIN y GERENTE (alto impacto financiero)
     * 
     * PUT /api/promociones/{id}
     * Content-Type: application/json
     * 
     * Actualiza una promoción existente completamente.
     * 
     * @param id ID de la promoción a actualizar
     * @param request DTO con nuevos datos
     * @return ResponseEntity con la promoción actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponse<PromocionResponse>> actualizarPromocion(
            @PathVariable Integer id,
            @Valid @RequestBody PromocionUpdateRequest request) {
        
        logger.info("PUT /api/promociones/{} - Actualizando promoción: {}", id, request.getNombre());
        
        ApiResponse<PromocionResponse> response = promocionServicio.actualizarPromocion(id, request);
        
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Caso 6.3: Consultar promoción específica
     * Permisos: Todos los roles (lectura necesaria para aplicar promociones)
     * 
     * GET /api/promociones/{id}
     * 
     * Obtiene una promoción por su ID con todos sus detalles y metadatos.
     * 
     * @param id ID de la promoción a buscar
     * @return ResponseEntity con la promoción encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> buscarPromocionPorId(
            @PathVariable Integer id) {
        
        logger.info("GET /api/promociones/{} - Buscando promoción por ID", id);
        
        ApiResponse<PromocionResponse> response = promocionServicio.buscarPorId(id);
        
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Caso 6.3: Consultar todas las promociones
     * 
     * GET /api/promociones
     * 
     * Obtiene todas las promociones con sus metadatos calculados.
     * Útil para listados y gestión general.
     * 
     * @return ResponseEntity con lista de promociones
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> obtenerTodasLasPromociones() {
        
        logger.info("GET /api/promociones - Obteniendo todas las promociones");
        
        ApiResponse<List<PromocionResponse>> response = promocionServicio.obtenerTodasLasPromociones();
        
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Caso 6.4: Eliminar promoción
     * Permisos: Solo ADMIN y GERENTE (alto impacto financiero)
     * 
     * DELETE /api/promociones/{id}
     * 
     * Elimina una promoción específica. Valida que no tenga 
     * componentes asociados antes de eliminar.
     * 
     * @param id ID de la promoción a eliminar
     * @return ResponseEntity confirmando eliminación
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponse<Void>> eliminarPromocion(@PathVariable Integer id) {
        
        logger.info("DELETE /api/promociones/{} - Eliminando promoción", id);
        
        ApiResponse<Void> response = promocionServicio.eliminarPromocion(id);
        
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
} 