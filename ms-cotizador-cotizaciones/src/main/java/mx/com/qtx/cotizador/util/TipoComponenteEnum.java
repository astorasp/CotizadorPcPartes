package mx.com.qtx.cotizador.util;

public enum TipoComponenteEnum {
    CPU("CPU"),
    GPU("GPU"),
    RAM("RAM"),
    HDD("HDD"),
    SSD("SSD"),
    MONITOR("MONITOR"),
    DISCO_DURO("DISCO_DURO"),
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
