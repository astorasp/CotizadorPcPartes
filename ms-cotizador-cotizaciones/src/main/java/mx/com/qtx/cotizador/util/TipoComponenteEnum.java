package mx.com.qtx.cotizador.util;

/**
 * Enumeración que define los tipos de componentes disponibles en el sistema CotizadorPcPartes.
 * <p>
 * Esta enumeración establece los tipos estándar de componentes que pueden ser cotizados,
 * ensamblados en PCs, y gestionados en el sistema. Cada tipo tiene características
 * específicas y reglas de negocio asociadas.
 * </p>
 *
 * <h3>Tipos de componentes definidos:</h3>
 * <table border="1">
 *   <tr><th>Enum</th><th>Nombre</th><th>Descripción</th><th>Ejemplos</th></tr>
 *   <tr><td>CPU</td><td>"CPU"</td><td>Procesador central</td><td>Intel i7, AMD Ryzen</td></tr>
 *   <tr><td>GPU</td><td>"GPU"</td><td>Tarjeta gráfica dedicada</td><td>NVIDIA RTX, AMD Radeon</td></tr>
 *   <tr><td>RAM</td><td>"RAM"</td><td>Memoria RAM</td><td>DDR4 16GB, DDR5 32GB</td></tr>
 *   <tr><td>HDD</td><td>"HDD"</td><td>Disco duro mecánico</td><td>1TB SATA, 2TB SATA</td></tr>
 *   <tr><td>SSD</td><td>"SSD"</td><td>Disco de estado sólido</td><td>500GB NVMe, 1TB SATA</td></tr>
 *   <tr><td>MONITOR</td><td>"MONITOR"</td><td>Monitor de computadora</td><td>24" FullHD, 27" 4K</td></tr>
 *   <tr><td>DISCO_DURO</td><td>"DISCO_DURO"</td><td>Almacenamiento general</td><td>HDD, SSD, NVMe</td></tr>
 *   <tr><td>PC</td><td>"PC"</td><td>Computadora ensamblada</td><td>Gaming PC, Workstation</td></tr>
 *   <tr><td>TARJETA_VIDEO</td><td>"TARJETA_VIDEO"</td><td>Tarjeta de video</td><td>GPU dedicada, integrada</td></tr>
 * </table>
 *
 * <h3>Reglas de negocio por tipo:</h3>
 * <ul>
 *   <li><strong>MONITOR:</strong> Máximo 2 monitores por PC</li>
 *   <li><strong>TARJETA_VIDEO:</strong> Máximo 2 tarjetas de video por PC</li>
 *   <li><strong>DISCO_DURO:</strong> Máximo 3 discos duros por PC</li>
 *   <li><strong>PC:</strong> Componente compuesto (ensamblado)</li>
 *   <li><strong>Otros:</strong> Componentes simples individuales</li>
 * </ul>
 *
 * <h3>Uso típico:</h3>
 * <pre>{@code
 * // Validación de tipo de componente
 * if (componente.getTipoComponente().getNombre().equals(TipoComponenteEnum.PC.getNombre())) {
 *     // Lógica específica para PCs
 * }
 *
 * // En consultas por tipo
 * List<Componente> monitores = componenteRepo.findByTipoComponenteNombre(
 *     TipoComponenteEnum.MONITOR.getNombre());
 * }</pre>
 *
 * <h3>Relación con entidades:</h3>
 * <p>
 * Este enum se relaciona con la entidad {@link mx.com.qtx.cotizador.entidad.TipoComponente}
 * que persiste estos valores en la base de datos. Los valores del enum deben mantenerse
 * sincronizados con los registros en la tabla correspondiente.
 * </p>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.entidad.TipoComponente
 * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente
 */
public enum TipoComponenteEnum {

    /**
     * Procesador central (CPU).
     * Componente fundamental que ejecuta instrucciones y coordina operaciones.
     */
    CPU("CPU"),

    /**
     * Tarjeta gráfica dedicada (GPU).
     * Acelera procesamiento gráfico y tareas de computación paralela.
     */
    GPU("GPU"),

    /**
     * Memoria RAM.
     * Almacenamiento temporal de alta velocidad para datos activos.
     */
    RAM("RAM"),

    /**
     * Disco duro mecánico (HDD).
     * Almacenamiento magnético de alta capacidad pero menor velocidad.
     */
    HDD("HDD"),

    /**
     * Disco de estado sólido (SSD).
     * Almacenamiento flash de alta velocidad y menor capacidad.
     */
    SSD("SSD"),

    /**
     * Monitor de computadora.
     * Dispositivo de salida visual con restricciones de cantidad por PC.
     */
    MONITOR("MONITOR"),

    /**
     * Disco duro general.
     * Categoría amplia que incluye HDD, SSD y otros dispositivos de almacenamiento.
     */
    DISCO_DURO("DISCO_DURO"),

    /**
     * Computadora ensamblada.
     * Componente compuesto formado por múltiples subcomponentes.
     */
    PC("PC"),

    /**
     * Tarjeta de video.
     * Dispositivo para procesamiento gráfico, puede ser dedicado o integrado.
     */
    TARJETA_VIDEO("TARJETA_VIDEO");

    private final String nombre;

    /**
     * Constructor del enum.
     *
     * @param nombre Nombre textual del tipo de componente
     */
    TipoComponenteEnum(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el nombre textual del tipo de componente.
     * <p>
     * Este nombre se utiliza para persistencia en base de datos y
     * comparación con valores de otras fuentes.
     * </p>
     *
     * @return Nombre del tipo de componente
     */
    public String getNombre() {
        return nombre;
    }

}
