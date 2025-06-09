package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.servicio.componente.ComponenteServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de componentes
 * 
 * Implementa la arquitectura refactorizada donde:
 * - El controlador NO conoce nada del dominio
 * - Solo maneja DTOs de request y response
 * - El servicio se encarga del mapeo DTO ↔ Dominio
 * - Los servicios retornan ApiResponse<DTO>
 * - El controlador mapea códigos de error a HTTP status:
 *   • Código "0" → HTTP 200
 *   • Código "3" → HTTP 500  
 *   • Todo lo demás → HTTP 400
 */
@Slf4j
@RestController
@RequestMapping("/componentes")
@RequiredArgsConstructor
public class ComponenteController {
    
    private final ComponenteServicio componenteServicio;
    
    /**
     * Caso de uso 1.1: Agregar componente
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ComponenteResponse>> crearComponente(
            @Valid @RequestBody ComponenteCreateRequest request) {
        
        log.info("Iniciando creación de componente con ID: {}", request.getId());
        
        // Llamar al servicio que maneja el mapeo internamente
        ApiResponse<ComponenteResponse> respuesta = componenteServicio.guardarComponente(request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.2: Modificar componente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponenteResponse>> modificarComponente(
            @PathVariable String id,
            @Valid @RequestBody ComponenteUpdateRequest request) {
        
        log.info("Iniciando actualización de componente con ID: {}", id);
        
        // Llamar al servicio que maneja el mapeo internamente
        ApiResponse<ComponenteResponse> respuesta = componenteServicio.actualizarComponente(id, request);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.3: Eliminar componente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarComponente(@PathVariable String id) {
        
        log.info("Iniciando eliminación de componente con ID: {}", id);
        
        // Llamar al servicio
        ApiResponse<Void> respuesta = componenteServicio.borrarComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.4: Consultar componentes - Obtener todos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> obtenerTodosLosComponentes() {
        
        log.info("Consultando todos los componentes");
        
        // Llamar al servicio que retorna directamente DTOs
        ApiResponse<List<ComponenteResponse>> respuesta = componenteServicio.obtenerTodosLosComponentes();
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.4: Consultar componentes - Obtener por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponenteResponse>> obtenerComponentePorId(@PathVariable String id) {
        
        log.info("Consultando componente con ID: {}", id);
        
        // Llamar al servicio que retorna directamente DTOs
        ApiResponse<ComponenteResponse> respuesta = componenteServicio.buscarComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.4: Consultar componentes - Filtrar por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> obtenerComponentesPorTipo(@PathVariable String tipo) {
        
        log.info("Consultando componentes por tipo: {}", tipo);
        
        // Llamar al servicio que retorna directamente DTOs
        ApiResponse<List<ComponenteResponse>> respuesta = componenteServicio.buscarPorTipo(tipo);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso adicional: Verificar existencia de componente
     */
    @GetMapping("/{id}/existe")
    public ResponseEntity<ApiResponse<Boolean>> verificarExistenciaComponente(@PathVariable String id) {
        
        log.info("Verificando existencia de componente con ID: {}", id);
        
        // Llamar al servicio
        ApiResponse<Boolean> respuesta = componenteServicio.existeComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuesta.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuesta.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
} 