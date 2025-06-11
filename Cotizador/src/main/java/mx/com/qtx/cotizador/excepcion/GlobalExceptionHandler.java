package mx.com.qtx.cotizador.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.util.Errores;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        log.error("Error interno del servicio", ex);
        return new 
            ResponseEntity<>(new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje()), 
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Extrae los errores campo por campo
        Map<String, String> errores = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                err -> err.getField(),
                err -> err.getDefaultMessage(),
                (msg1, msg2) -> msg1 // Si hay campos duplicados, se queda con el primero
            ));
        return new ResponseEntity<>(new ApiResponse<>(Errores.ERROR_DE_VALIDACION.getCodigo(), 
            Errores.ERROR_DE_VALIDACION.getMensaje(), errores), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return new 
            ResponseEntity<>(new ApiResponse<>(Errores.ACCESO_DENEGADO.getCodigo(), Errores.ACCESO_DENEGADO.getMensaje()), 
                HttpStatus.FORBIDDEN);
    }    
}
