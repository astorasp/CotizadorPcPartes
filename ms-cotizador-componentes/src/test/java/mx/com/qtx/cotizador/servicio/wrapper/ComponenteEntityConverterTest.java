package mx.com.qtx.cotizador.servicio.wrapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteSimple;
import mx.com.qtx.cotizador.dominio.core.componentes.DiscoDuro;
import mx.com.qtx.cotizador.dominio.core.componentes.Monitor;
import mx.com.qtx.cotizador.dominio.core.componentes.TarjetaVideo;
import mx.com.qtx.cotizador.dominio.core.componentes.Pc;
import mx.com.qtx.cotizador.util.TipoComponenteEnum;

/**
 * Tests unitarios para ComponenteEntityConverter.
 * Verifica las conversiones bidireccionales entre objetos de dominio y entidades de persistencia.
 */
@DisplayName("ComponenteEntityConverter - Tests de Conversión")
class ComponenteEntityConverterTest {
    
    // ===== TESTS DE CONVERSIÓN DOMINIO → ENTIDAD =====
    
    /**
     * Dado: Un objeto DiscoDuro de dominio
     * Cuando: Se convierte a entidad
     * Entonces: Se crea entidad con propiedades correctas incluyendo capacidadAlm
     */
    @Test
    @DisplayName("Convertir DiscoDuro de dominio debe crear entidad con capacidadAlm")
    void convertToEntity_ConDiscoDuro_DebeCrearEntidadConCapacidad() {
        // Given
        DiscoDuro disco = (DiscoDuro) mx.com.qtx.cotizador.dominio.core.componentes.Componente
            .crearDiscoDuro("DD001", "Disco SSD 1TB", "Samsung", "980 EVO", 
                          new BigDecimal("150.00"), new BigDecimal("200.00"), "1TB");
        
        // When
        Componente entidad = ComponenteEntityConverter.convertToEntity(disco);
        
        // Then
        assertThat(entidad).isNotNull();
        assertThat(entidad.getId()).isEqualTo("DD001");
        assertThat(entidad.getDescripcion()).isEqualTo("Disco SSD 1TB");
        assertThat(entidad.getMarca()).isEqualTo("Samsung");
        assertThat(entidad.getModelo()).isEqualTo("980 EVO");
        assertThat(entidad.getCosto()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(entidad.getPrecioBase()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(entidad.getCapacidadAlm()).isEqualTo("1TB");
    }
    
    /**
     * Dado: Un objeto TarjetaVideo de dominio
     * Cuando: Se convierte a entidad
     * Entonces: Se crea entidad con propiedades correctas incluyendo memoria
     */
    @Test
    @DisplayName("Convertir TarjetaVideo de dominio debe crear entidad con memoria")
    void convertToEntity_ConTarjetaVideo_DebeCrearEntidadConMemoria() {
        // Given
        TarjetaVideo tarjeta = (TarjetaVideo) mx.com.qtx.cotizador.dominio.core.componentes.Componente
            .crearTarjetaVideo("TV001", "GTX 4060 Ti", "NVIDIA", "GeForce", 
                             new BigDecimal("400.00"), new BigDecimal("500.00"), "8GB");
        
        // When
        Componente entidad = ComponenteEntityConverter.convertToEntity(tarjeta);
        
        // Then
        assertThat(entidad).isNotNull();
        assertThat(entidad.getId()).isEqualTo("TV001");
        assertThat(entidad.getDescripcion()).isEqualTo("GTX 4060 Ti");
        assertThat(entidad.getMarca()).isEqualTo("NVIDIA");
        assertThat(entidad.getModelo()).isEqualTo("GeForce");
        assertThat(entidad.getCosto()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(entidad.getPrecioBase()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(entidad.getMemoria()).isEqualTo("8GB");
    }
    
    /**
     * Dado: Un objeto Monitor de dominio
     * Cuando: Se convierte a entidad
     * Entonces: Se crea entidad con propiedades básicas sin campos específicos
     */
    @Test
    @DisplayName("Convertir Monitor de dominio debe crear entidad con propiedades básicas")
    void convertToEntity_ConMonitor_DebeCrearEntidadBasica() {
        // Given
        Monitor monitor = (Monitor) mx.com.qtx.cotizador.dominio.core.componentes.Componente
            .crearMonitor("MON001", "Monitor 24 4K", "LG", "UltraFine", 
                         new BigDecimal("300.00"), new BigDecimal("400.00"));
        
        // When
        Componente entidad = ComponenteEntityConverter.convertToEntity(monitor);
        
        // Then
        assertThat(entidad).isNotNull();
        assertThat(entidad.getId()).isEqualTo("MON001");
        assertThat(entidad.getDescripcion()).isEqualTo("Monitor 24 4K");
        assertThat(entidad.getMarca()).isEqualTo("LG");
        assertThat(entidad.getModelo()).isEqualTo("UltraFine");
        assertThat(entidad.getCosto()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(entidad.getPrecioBase()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(entidad.getCapacidadAlm()).isNull();
        assertThat(entidad.getMemoria()).isNull();
    }
    
    /**
     * Dado: Un objeto de dominio nulo
     * Cuando: Se convierte a entidad
     * Entonces: Se retorna null
     */
    @Test
    @DisplayName("Convertir dominio nulo debe retornar null")
    void convertToEntity_ConDominioNulo_DebeRetornarNull() {
        // When
        Componente entidad = ComponenteEntityConverter.convertToEntity(null);
        
        // Then
        assertThat(entidad).isNull();
    }
    
    // ===== TESTS DE CONVERSIÓN ENTIDAD → DOMINIO =====
    
    /**
     * Dado: Una entidad de DiscoDuro
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea DiscoDuro con propiedades correctas
     */
    @Test
    @DisplayName("Convertir entidad DiscoDuro debe crear objeto DiscoDuro de dominio")
    void convertToComponente_ConEntidadDiscoDuro_DebeCrearDiscoDuroDominio() {
        // Given
        Componente entidad = crearEntidadDiscoDuro("DD002", "SSD NVMe 2TB", "Western Digital", 
                                                   "Black SN850", new BigDecimal("250.00"), 
                                                   new BigDecimal("320.00"), "2TB");
        
        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado = 
            ComponenteEntityConverter.convertToComponente(entidad, null);
        
        // Then
        assertThat(resultado).isInstanceOf(DiscoDuro.class);
        DiscoDuro disco = (DiscoDuro) resultado;
        assertThat(disco.getId()).isEqualTo("DD002");
        assertThat(disco.getDescripcion()).isEqualTo("SSD NVMe 2TB");
        assertThat(disco.getMarca()).isEqualTo("Western Digital");
        assertThat(disco.getModelo()).isEqualTo("Black SN850");
        assertThat(disco.getCosto()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(disco.getPrecioBase()).isEqualByComparingTo(new BigDecimal("320.00"));
        assertThat(disco.getCapacidadAlm()).isEqualTo("2TB");
        assertThat(disco.getCategoria()).isEqualTo("Disco Duro");
    }
    
    /**
     * Dado: Una entidad de TarjetaVideo
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea TarjetaVideo con propiedades correctas
     */
    @Test
    @DisplayName("Convertir entidad TarjetaVideo debe crear objeto TarjetaVideo de dominio")
    void convertToComponente_ConEntidadTarjetaVideo_DebeCrearTarjetaVideoDominio() {
        // Given
        Componente entidad = crearEntidadTarjetaVideo("TV002", "RTX 4070 Super", "NVIDIA", 
                                                      "GeForce RTX", new BigDecimal("600.00"), 
                                                      new BigDecimal("750.00"), "12GB");
        
        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado = 
            ComponenteEntityConverter.convertToComponente(entidad, null);
        
        // Then
        assertThat(resultado).isInstanceOf(TarjetaVideo.class);
        TarjetaVideo tarjeta = (TarjetaVideo) resultado;
        assertThat(tarjeta.getId()).isEqualTo("TV002");
        assertThat(tarjeta.getDescripcion()).isEqualTo("RTX 4070 Super");
        assertThat(tarjeta.getMarca()).isEqualTo("NVIDIA");
        assertThat(tarjeta.getModelo()).isEqualTo("GeForce RTX");
        assertThat(tarjeta.getCosto()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(tarjeta.getPrecioBase()).isEqualByComparingTo(new BigDecimal("750.00"));
        assertThat(tarjeta.getMemoria()).isEqualTo("12GB");
        assertThat(tarjeta.getCategoria()).isEqualTo("Tarjeta de Video");
    }
    
    /**
     * Dado: Una entidad de Monitor
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea Monitor con propiedades correctas
     */
    @Test
    @DisplayName("Convertir entidad Monitor debe crear objeto Monitor de dominio")
    void convertToComponente_ConEntidadMonitor_DebeCrearMonitorDominio() {
        // Given
        Componente entidad = crearEntidadMonitor("MON002", "Monitor Gaming 27\"", "ASUS", 
                                                 "ROG Swift", new BigDecimal("450.00"), 
                                                 new BigDecimal("550.00"));
        
        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado = 
            ComponenteEntityConverter.convertToComponente(entidad, null);
        
        // Then
        assertThat(resultado).isInstanceOf(Monitor.class);
        Monitor monitor = (Monitor) resultado;
        assertThat(monitor.getId()).isEqualTo("MON002");
        assertThat(monitor.getDescripcion()).isEqualTo("Monitor Gaming 27\"");
        assertThat(monitor.getMarca()).isEqualTo("ASUS");
        assertThat(monitor.getModelo()).isEqualTo("ROG Swift");
        assertThat(monitor.getCosto()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(monitor.getPrecioBase()).isEqualByComparingTo(new BigDecimal("550.00"));
        assertThat(monitor.getCategoria()).isEqualTo("Monitor");
    }
    
    /**
     * Dado: Una entidad de PC con componentes mínimos requeridos (1 monitor, 1 tarjeta, 1 disco)
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea PC válida con propiedades básicas y componentes mínimos
     */
    @Test
    @DisplayName("Convertir entidad PC con componentes mínimos debe crear PC válida de dominio")
    void convertToComponente_ConEntidadPcConComponentesMinimos_DebeCrearPcValidaDominio() {
        // Given
        Componente entidad = crearEntidadPc("PC001", "PC Gaming Básica", "Custom",
                                           "Build 2024", new BigDecimal("0.00"),
                                           new BigDecimal("0.00"));

        // Crear componentes mínimos requeridos según PRD
        List<Componente> componentesMinimos = new ArrayList<>();
        componentesMinimos.add(crearEntidadMonitor("MON001", "Monitor Gaming", "ASUS", "VG248QE",
                                                   new BigDecimal("200.00"), new BigDecimal("250.00")));
        componentesMinimos.add(crearEntidadTarjetaVideo("GPU001", "Tarjeta Gráfica", "NVIDIA", "RTX 3060",
                                                        new BigDecimal("300.00"), new BigDecimal("400.00"), "8GB"));
        componentesMinimos.add(crearEntidadDiscoDuro("HDD001", "Disco Duro", "WD", "Blue 1TB",
                                                     new BigDecimal("50.00"), new BigDecimal("75.00"), "1TB"));

        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado =
            ComponenteEntityConverter.convertToComponente(entidad, componentesMinimos);

        // Then
        assertThat(resultado).isInstanceOf(Pc.class);
        Pc pc = (Pc) resultado;
        assertThat(pc.getId()).isEqualTo("PC001");
        assertThat(pc.getDescripcion()).isEqualTo("PC Gaming Básica");
        assertThat(pc.getMarca()).isEqualTo("Custom");
        assertThat(pc.getModelo()).isEqualTo("Build 2024");
        assertThat(pc.getCategoria()).isEqualTo("PC");

        // Verificar que la PC tiene los componentes mínimos requeridos (3 componentes total)
        assertThat(pc.getSubComponentes()).hasSize(3);

        // Verificar que contiene cada tipo de componente
        List<ComponenteSimple> subComponentes = pc.getSubComponentes();
        boolean tieneMonitor = subComponentes.stream().anyMatch(c -> c.getCategoria().equals("Monitor"));
        boolean tieneTarjeta = subComponentes.stream().anyMatch(c -> c.getCategoria().equals("Tarjeta de Video"));
        boolean tieneDisco = subComponentes.stream().anyMatch(c -> c.getCategoria().equals("Disco Duro"));

        assertThat(tieneMonitor).isTrue();
        assertThat(tieneTarjeta).isTrue();
        assertThat(tieneDisco).isTrue();
    }
    
    /**
     * Dado: Una entidad de PC con subcomponentes
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea PC completa con todos los subcomponentes
     */
    @Test
    @DisplayName("Convertir entidad PC con subcomponentes debe crear PC completa")
    void convertToComponente_ConEntidadPcCompleta_DebeCrearPcCompleta() {
        // Given
        Componente entidadPc = crearEntidadPc("PC002", "PC Gaming Completa", "Custom", 
                                             "Build Pro", new BigDecimal("0.00"), 
                                             new BigDecimal("0.00"));
        
        List<Componente> subComponentes = Arrays.asList(
            crearEntidadDiscoDuro("DD003", "SSD 1TB", "Samsung", "980 PRO", 
                                 new BigDecimal("180.00"), new BigDecimal("230.00"), "1TB"),
            crearEntidadTarjetaVideo("TV003", "RTX 4080", "NVIDIA", "GeForce", 
                                   new BigDecimal("800.00"), new BigDecimal("1000.00"), "16GB"),
            crearEntidadMonitor("MON003", "Monitor 4K", "Dell", "UltraSharp", 
                               new BigDecimal("400.00"), new BigDecimal("500.00"))
        );
        
        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado = 
            ComponenteEntityConverter.convertToComponente(entidadPc, subComponentes);
        
        // Then
        assertThat(resultado).isInstanceOf(Pc.class);
        Pc pc = (Pc) resultado;
        assertThat(pc.getId()).isEqualTo("PC002");
        assertThat(pc.getDescripcion()).isEqualTo("PC Gaming Completa");
        assertThat(pc.getMarca()).isEqualTo("Custom");
        assertThat(pc.getModelo()).isEqualTo("Build Pro");
        assertThat(pc.getCategoria()).isEqualTo("PC");
        
        // Verificar que los subcomponentes están incluidos
        assertThat(pc.getCosto()).isGreaterThan(BigDecimal.ZERO);
        assertThat(pc.getPrecioBase()).isGreaterThan(BigDecimal.ZERO);
    }
    
    /**
     * Dado: Una entidad con tipo desconocido
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea Monitor como tipo por defecto
     */
    @Test
    @DisplayName("Convertir entidad con tipo desconocido debe crear Monitor por defecto")
    void convertToComponente_ConTipoDesconocido_DebeCrearMonitorPorDefecto() {
        // Given
        Componente entidad = crearEntidadTipoDesconocido("UNK001", "Componente Desconocido", 
                                                        "Generic", "Unknown", 
                                                        new BigDecimal("100.00"), 
                                                        new BigDecimal("150.00"));
        
        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado = 
            ComponenteEntityConverter.convertToComponente(entidad, null);
        
        // Then
        assertThat(resultado).isInstanceOf(Monitor.class);
        assertThat(resultado.getId()).isEqualTo("UNK001");
        assertThat(resultado.getDescripcion()).isEqualTo("Componente Desconocido");
        assertThat(resultado.getCategoria()).isEqualTo("Monitor");
    }
    
    /**
     * Dado: Una entidad nula
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se retorna null
     */
    @Test
    @DisplayName("Convertir entidad nula debe retornar null")
    void convertToComponente_ConEntidadNula_DebeRetornarNull() {
        // When
        mx.com.qtx.cotizador.dominio.core.componentes.Componente resultado = 
            ComponenteEntityConverter.convertToComponente(null, null);
        
        // Then
        assertThat(resultado).isNull();
    }
    
    // ===== TESTS DE CONVERSIÓN BIDIRECCIONAL =====
    
    /**
     * Dado: Un DiscoDuro de dominio
     * Cuando: Se convierte a entidad y luego de vuelta a dominio
     * Entonces: El objeto resultante mantiene todas las propiedades originales
     */
    @Test
    @DisplayName("Conversión bidireccional DiscoDuro debe preservar propiedades")
    void conversionBidireccional_DiscoDuro_DebePreservarPropiedades() {
        // Given
        DiscoDuro original = (DiscoDuro) mx.com.qtx.cotizador.dominio.core.componentes.Componente
            .crearDiscoDuro("DD004", "Test SSD", "TestBrand", "TestModel", 
                          new BigDecimal("100.00"), new BigDecimal("130.00"), "512GB");
        
        // When - Dominio → Entidad → Dominio
        Componente entidad = ComponenteEntityConverter.convertToEntity(original);
        entidad.setTipoComponente(crearTipoComponente(TipoComponenteEnum.DISCO_DURO.name()));
        
        mx.com.qtx.cotizador.dominio.core.componentes.Componente convertido = 
            ComponenteEntityConverter.convertToComponente(entidad, null);
        
        // Then
        assertThat(convertido).isInstanceOf(DiscoDuro.class);
        DiscoDuro disco = (DiscoDuro) convertido;
        assertThat(disco.getId()).isEqualTo(original.getId());
        assertThat(disco.getDescripcion()).isEqualTo(original.getDescripcion());
        assertThat(disco.getMarca()).isEqualTo(original.getMarca());
        assertThat(disco.getModelo()).isEqualTo(original.getModelo());
        assertThat(disco.getCosto()).isEqualByComparingTo(original.getCosto());
        assertThat(disco.getPrecioBase()).isEqualByComparingTo(original.getPrecioBase());
        assertThat(disco.getCapacidadAlm()).isEqualTo(original.getCapacidadAlm());
    }
    
    /**
     * Dado: Una TarjetaVideo de dominio
     * Cuando: Se convierte a entidad y luego de vuelta a dominio
     * Entonces: El objeto resultante mantiene todas las propiedades originales
     */
    @Test
    @DisplayName("Conversión bidireccional TarjetaVideo debe preservar propiedades")
    void conversionBidireccional_TarjetaVideo_DebePreservarPropiedades() {
        // Given
        TarjetaVideo original = (TarjetaVideo) mx.com.qtx.cotizador.dominio.core.componentes.Componente
            .crearTarjetaVideo("TV004", "Test GPU", "TestGPU", "TestSeries", 
                             new BigDecimal("500.00"), new BigDecimal("650.00"), "8GB");
        
        // When - Dominio → Entidad → Dominio
        Componente entidad = ComponenteEntityConverter.convertToEntity(original);
        entidad.setTipoComponente(crearTipoComponente(TipoComponenteEnum.TARJETA_VIDEO.name()));
        
        mx.com.qtx.cotizador.dominio.core.componentes.Componente convertido = 
            ComponenteEntityConverter.convertToComponente(entidad, null);
        
        // Then
        assertThat(convertido).isInstanceOf(TarjetaVideo.class);
        TarjetaVideo tarjeta = (TarjetaVideo) convertido;
        assertThat(tarjeta.getId()).isEqualTo(original.getId());
        assertThat(tarjeta.getDescripcion()).isEqualTo(original.getDescripcion());
        assertThat(tarjeta.getMarca()).isEqualTo(original.getMarca());
        assertThat(tarjeta.getModelo()).isEqualTo(original.getModelo());
        assertThat(tarjeta.getCosto()).isEqualByComparingTo(original.getCosto());
        assertThat(tarjeta.getPrecioBase()).isEqualByComparingTo(original.getPrecioBase());
        assertThat(tarjeta.getMemoria()).isEqualTo(original.getMemoria());
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private Componente crearEntidadDiscoDuro(String id, String descripcion, String marca, 
                                           String modelo, BigDecimal costo, BigDecimal precioBase, 
                                           String capacidad) {
        Componente entidad = new Componente();
        entidad.setId(id);
        entidad.setDescripcion(descripcion);
        entidad.setMarca(marca);
        entidad.setModelo(modelo);
        entidad.setCosto(costo);
        entidad.setPrecioBase(precioBase);
        entidad.setCapacidadAlm(capacidad);
        entidad.setTipoComponente(crearTipoComponente(TipoComponenteEnum.DISCO_DURO.name()));
        return entidad;
    }
    
    private Componente crearEntidadTarjetaVideo(String id, String descripcion, String marca, 
                                              String modelo, BigDecimal costo, BigDecimal precioBase, 
                                              String memoria) {
        Componente entidad = new Componente();
        entidad.setId(id);
        entidad.setDescripcion(descripcion);
        entidad.setMarca(marca);
        entidad.setModelo(modelo);
        entidad.setCosto(costo);
        entidad.setPrecioBase(precioBase);
        entidad.setMemoria(memoria);
        entidad.setTipoComponente(crearTipoComponente(TipoComponenteEnum.TARJETA_VIDEO.name()));
        return entidad;
    }
    
    private Componente crearEntidadMonitor(String id, String descripcion, String marca, 
                                         String modelo, BigDecimal costo, BigDecimal precioBase) {
        Componente entidad = new Componente();
        entidad.setId(id);
        entidad.setDescripcion(descripcion);
        entidad.setMarca(marca);
        entidad.setModelo(modelo);
        entidad.setCosto(costo);
        entidad.setPrecioBase(precioBase);
        entidad.setTipoComponente(crearTipoComponente(TipoComponenteEnum.MONITOR.name()));
        return entidad;
    }
    
    private Componente crearEntidadPc(String id, String descripcion, String marca, 
                                    String modelo, BigDecimal costo, BigDecimal precioBase) {
        Componente entidad = new Componente();
        entidad.setId(id);
        entidad.setDescripcion(descripcion);
        entidad.setMarca(marca);
        entidad.setModelo(modelo);
        entidad.setCosto(costo);
        entidad.setPrecioBase(precioBase);
        entidad.setTipoComponente(crearTipoComponente(TipoComponenteEnum.PC.name()));
        return entidad;
    }
    
    private Componente crearEntidadTipoDesconocido(String id, String descripcion, String marca, 
                                                 String modelo, BigDecimal costo, BigDecimal precioBase) {
        Componente entidad = new Componente();
        entidad.setId(id);
        entidad.setDescripcion(descripcion);
        entidad.setMarca(marca);
        entidad.setModelo(modelo);
        entidad.setCosto(costo);
        entidad.setPrecioBase(precioBase);
        entidad.setTipoComponente(crearTipoComponente("TIPO_DESCONOCIDO"));
        return entidad;
    }
    
    private TipoComponente crearTipoComponente(String nombre) {
        TipoComponente tipo = new TipoComponente();
        tipo.setNombre(nombre);
        return tipo;
    }
}