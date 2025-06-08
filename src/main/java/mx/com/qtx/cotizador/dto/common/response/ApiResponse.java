package mx.com.qtx.cotizador.dto.common.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String codigo;
    private String mensaje;
    private T data;

    public ApiResponse(String codigo, String mensaje, T data) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.data = data;
    }

    public ApiResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }
    
}
