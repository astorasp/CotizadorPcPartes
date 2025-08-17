package mx.com.qtx.cotizador.dto.common.response;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ApiResponse<T> {
    private String codigo;
    private String mensaje;
    
    @JsonProperty("datos")
    private T datos;

    public ApiResponse(String codigo, String mensaje, T datos) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public ApiResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }
    
}
