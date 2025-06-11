package mx.com.qtx.cotizador.util;

public enum TipoComponenteEnum {
    DISCO_DURO("DISCO_DURO"),
    MONITOR("MONITOR"),
    PC("PC"),
    TARJETA_VIDEO("TARJETA_VIDEO");

    private final String nombre;

    TipoComponenteEnum(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
    
}
