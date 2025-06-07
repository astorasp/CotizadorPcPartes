package mx.com.qtx.cotizadorv1ds.core.componentes;

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
