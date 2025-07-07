package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pc.request.PcCreateRequest;
import mx.com.qtx.cotizador.dto.pc.request.PcUpdateRequest;
import mx.com.qtx.cotizador.dto.pc.request.AgregarComponenteRequest;
import mx.com.qtx.cotizador.dto.pc.response.PcResponse;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.servicio.componente.ComponenteServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de PCs completas con sub-componentes
 * 
 * Implementa la arquitectura de manejo de errores donde:
 * - Los servicios retornan ApiResponse<T>
 * - El controlador mapea códigos de error a HTTP status
 *   • Código "0" → HTTP 200
 *   • Código "3" → HTTP 500  
 *   • Todo lo demás → HTTP 400
 */
@Slf4j
@RestController
@RequestMapping("/pcs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'INVENTARIO', 'CONSULTOR')")
public class PcController {
    
    private final ComponenteServicio componenteServicio;
    
    /**
     * Crear una PC completa con sus sub-componentes
     * Permisos: ADMIN, GERENTE, INVENTARIO
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<PcResponse>> crearPc(
            @Valid @RequestBody PcCreateRequest request) {
        
        log.info("Iniciando creación de PC con ID: {}", request.getId());
        
        // Convertir DTO a objeto de dominio y llamar al servicio
        ApiResponse<PcResponse> respuestaServicio = componenteServicio.guardarPcCompleto(request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Actualizar una PC completa con sus sub-componentes
     * Permisos: ADMIN, GERENTE, INVENTARIO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<PcResponse>> actualizarPc(
            @PathVariable String id,
            @Valid @RequestBody PcUpdateRequest request) {
        
        log.info("Iniciando actualización de PC con ID: {}", id);
        
        // Llamar al servicio especializado para actualizar PCs
        ApiResponse<PcResponse> respuestaServicio = componenteServicio.actualizarPcCompleto(id, request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Obtener una PC por ID con sus sub-componentes
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PcResponse>> obtenerPcPorId(@PathVariable String id) {
        
        log.info("Consultando PC con ID: {}", id);
        
        // Usar el método especializado para buscar la PC
        ApiResponse<PcResponse> respuestaServicio = componenteServicio.buscarPcCompleto(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Eliminar una PC completa
     * Permisos: ADMIN, GERENTE (solo roles de mayor jerarquía)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponse<Void>> eliminarPc(@PathVariable String id) {
        
        log.info("Iniciando eliminación de PC con ID: {}", id);
        
        // Usar el método especializado para eliminar PCs completas
        ApiResponse<Void> respuestaServicio = componenteServicio.eliminarPcCompleta(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Listar todas las PCs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> obtenerTodasLasPcs() {
        
        log.info("Consultando todas las PCs");
        
        // Obtener todos los componentes y filtrar solo las PCs
        ApiResponse<List<ComponenteResponse>> respuestaServicio = componenteServicio.buscarPorTipo("PC");
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 2.2: Agregar un componente individual a una PC existente
     * Permisos: ADMIN, GERENTE, INVENTARIO
     */
    @PostMapping("/{pcId}/componentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<ComponenteResponse>> agregarComponenteAPc(
            @PathVariable String pcId,
            @Valid @RequestBody AgregarComponenteRequest request) {
        
        log.info("Agregando componente {} a PC {}", request.getId(), pcId);
        
        // Llamar al servicio para agregar el componente a la PC
        ApiResponse<ComponenteResponse> respuestaServicio = componenteServicio.agregarComponenteAPc(pcId, request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 2.3: Quitar un componente específico de una PC
     * Permisos: ADMIN, GERENTE, INVENTARIO
     */
    @DeleteMapping("/{pcId}/componentes/{componenteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'INVENTARIO')")
    public ResponseEntity<ApiResponse<Void>> quitarComponenteDePc(
            @PathVariable String pcId,
            @PathVariable String componenteId) {
        
        log.info("Quitando componente {} de PC {}", componenteId, pcId);
        
        // Llamar al servicio para quitar el componente de la PC
        ApiResponse<Void> respuestaServicio = componenteServicio.quitarComponenteDePc(pcId, componenteId);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Listar componentes de una PC específica
     * Endpoint de conveniencia para ver qué componentes tiene una PC
     */
    @GetMapping("/{pcId}/componentes")
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> listarComponentesDePc(
            @PathVariable String pcId) {
        
        log.info("Consultando componentes de PC {}", pcId);
        
        // Obtener la PC completa con sus componentes usando el método especializado
        ApiResponse<PcResponse> respuestaServicio = componenteServicio.buscarPcCompleto(pcId);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Extraer los sub-componentes de la PC
        ApiResponse<List<ComponenteResponse>> respuesta;
        if (respuestaServicio.getDatos() != null) {
            PcResponse pcResponse = respuestaServicio.getDatos();
            List<ComponenteResponse> componentesResponse = pcResponse.getSubComponentes();
            
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), 
                                        respuestaServicio.getMensaje(),
                                        componentesResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
} 