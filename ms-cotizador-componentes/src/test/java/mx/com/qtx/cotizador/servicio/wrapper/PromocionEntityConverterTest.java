package mx.com.qtx.cotizador.servicio.wrapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.HashMap;

import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.entidad.DetallePromocion;
import mx.com.qtx.cotizador.entidad.DetallePromDsctoXCant;
// Alias to avoid naming conflicts between domain and entity classes
import mx.com.qtx.cotizador.dominio.promos.PromNXM;
import mx.com.qtx.cotizador.dominio.promos.PromBase;
import mx.com.qtx.cotizador.dominio.promos.PromSinDescto;
import mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano;
import mx.com.qtx.cotizador.dominio.promos.PromDsctoXcantidad;

/**
 * Tests unitarios para PromocionEntityConverter.
 * Verifica las conversiones entre objetos de dominio y entidades de persistencia.
 */
@DisplayName("PromocionEntityConverter - Tests de Conversión")
class PromocionEntityConverterTest {
    
    // ===== TESTS DE CONVERSIÓN ENTIDAD → DOMINIO =====
    
    /**
     * Dado: Una entidad con promoción base NXM
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea correctamente un PromNXM
     */
    @Test
    @DisplayName("Convertir entidad con base NXM debe crear PromNXM")
    void convertToPromocion_ConBaseNXM_DebeCrearPromNXM() {
        // Given
        Promocion entidad = crearEntidadConBaseNXM("Promo NXM", "Lleve 3 pague 2", 3, 2);
        
        // When
        mx.com.qtx.cotizador.dominio.promos.Promocion resultado = PromocionEntityConverter.convertToPromocion(entidad);
        
        // Then
        assertThat(resultado).isInstanceOf(PromNXM.class);
        PromNXM promNXM = (PromNXM) resultado;
        assertThat(promNXM.getNombre()).isEqualTo("Promo NXM");
        assertThat(promNXM.getDescripcion()).isEqualTo("Lleve 3 pague 2");
        assertThat(promNXM.getLleveN()).isEqualTo(3);
        assertThat(promNXM.getPagueM()).isEqualTo(2);
    }
    
    /**
     * Dado: Una entidad con promoción base sin descuento
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea correctamente un PromBase
     */
    @Test
    @DisplayName("Convertir entidad con base sin descuento debe crear PromBase")
    void convertToPromocion_ConBaseSinDescuento_DebeCrearPromBase() {
        // Given
        Promocion entidad = crearEntidadConBaseSinDescuento("Promo Base", "Sin descuento");
        
        // When
        mx.com.qtx.cotizador.dominio.promos.Promocion resultado = PromocionEntityConverter.convertToPromocion(entidad);
        
        // Then
        assertThat(resultado).isInstanceOf(PromSinDescto.class);
        assertThat(resultado.getNombre()).isEqualTo("Promo Base");
        assertThat(resultado.getDescripcion()).isEqualTo("Sin descuento");
    }
    
    /**
     * Dado: Una entidad con base NXM y descuento plano acumulable
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea correctamente decorador PromDsctoPlano sobre PromNXM
     */
    @Test
    @DisplayName("Convertir entidad con base NXM y descuento plano debe crear decorador")
    void convertToPromocion_ConBaseNXMYDescuentoPlano_DebeCrearDecorador() {
        // Given
        Promocion entidad = crearEntidadConBaseNXMYDescuentoPlano("Promo Combo", 2, 1, 15.0f);
        
        // When
        mx.com.qtx.cotizador.dominio.promos.Promocion resultado = PromocionEntityConverter.convertToPromocion(entidad);
        
        // Then
        assertThat(resultado).isInstanceOf(PromDsctoPlano.class);
        PromDsctoPlano promDscto = (PromDsctoPlano) resultado;
        assertThat(promDscto.getPorcDescto()).isEqualTo(15.0f);
        assertThat(promDscto.getPromoBase()).isInstanceOf(PromNXM.class);
    }
    
    /**
     * Dado: Una entidad con base y descuento por cantidad
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se crea correctamente PromDsctoXcantidad con mapa
     */
    @Test
    @DisplayName("Convertir entidad con descuento por cantidad debe crear PromDsctoXcantidad")
    void convertToPromocion_ConDescuentoPorCantidad_DebeCrearPromDsctoXcantidad() {
        // Given
        Map<Integer, Double> escalas = Map.of(5, 5.0, 10, 10.0, 20, 15.0);
        Promocion entidad = crearEntidadConDescuentoPorCantidad("Promo Escalada", escalas);
        
        // When
        mx.com.qtx.cotizador.dominio.promos.Promocion resultado = PromocionEntityConverter.convertToPromocion(entidad);
        
        // Then
        assertThat(resultado).isInstanceOf(PromDsctoXcantidad.class);
        PromDsctoXcantidad promXcant = (PromDsctoXcantidad) resultado;
        Map<Integer, Double> mapaResultado = promXcant.getMapCantidadVsDscto();
        assertThat(mapaResultado).containsEntry(5, 5.0);
        assertThat(mapaResultado).containsEntry(10, 10.0);
        assertThat(mapaResultado).containsEntry(20, 15.0);
    }
    
    /**
     * Dado: Una entidad nula
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se retorna null
     */
    @Test
    @DisplayName("Convertir entidad nula debe retornar null")
    void convertToPromocion_ConEntidadNula_DebeRetornarNull() {
        // When
        mx.com.qtx.cotizador.dominio.promos.Promocion resultado = PromocionEntityConverter.convertToPromocion(null);
        
        // Then
        assertThat(resultado).isNull();
    }
    
    /**
     * Dado: Una entidad sin detalles
     * Cuando: Se convierte a objeto de dominio
     * Entonces: Se retorna null
     */
    @Test
    @DisplayName("Convertir entidad sin detalles debe retornar null")
    void convertToPromocion_ConEntidadSinDetalles_DebeRetornarNull() {
        // Given
        Promocion entidad = new Promocion();
        entidad.setNombre("Promo Vacía");
        
        // When
        mx.com.qtx.cotizador.dominio.promos.Promocion resultado = PromocionEntityConverter.convertToPromocion(entidad);
        
        // Then
        assertThat(resultado).isNull();
    }
    
    // ===== TESTS DE CONVERSIÓN DOMINIO → ENTIDAD =====
    
    /**
     * Dado: Un objeto de dominio PromNXM
     * Cuando: Se convierte a entidad
     * Entonces: Se crea entidad con detalle base NXM correcto
     */
    @Test
    @DisplayName("Convertir PromNXM de dominio debe crear entidad con detalle NXM")
    void convertToEntity_ConPromNXM_DebeCrearEntidadConDetalleNXM() {
        // Given
        PromNXM promNXM = new PromNXM(3, 2);
        promNXM.setNombre("Promo 3x2");
        promNXM.setDescripcion("Lleve 3 pague 2");
        
        // When
        Promocion resultado = PromocionEntityConverter.convertToEntity(promNXM);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Promo 3x2");
        assertThat(resultado.getDescripcion()).isEqualTo("Lleve 3 pague 2");
        assertThat(resultado.getDetalles()).hasSize(1);
        
        DetallePromocion detalleBase = resultado.getDetalles().get(0);
        assertThat(detalleBase.getEsBase()).isTrue();
        assertThat(detalleBase.getTipoPromBase()).isEqualTo("NXM");
        assertThat(detalleBase.getLlevent()).isEqualTo(3);
        assertThat(detalleBase.getPaguen()).isEqualTo(2);
    }
    
    /**
     * Dado: Un objeto de dominio PromBase
     * Cuando: Se convierte a entidad
     * Entonces: Se crea entidad con detalle base sin descuento
     */
    @Test
    @DisplayName("Convertir PromBase de dominio debe crear entidad con detalle sin descuento")
    void convertToEntity_ConPromBase_DebeCrearEntidadConDetalleSinDescuento() {
        // Given
        PromSinDescto promBase = new PromSinDescto();
        promBase.setNombre("Promo Base");
        promBase.setDescripcion("Sin promoción especial");
        
        // When
        Promocion resultado = PromocionEntityConverter.convertToEntity(promBase);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Promo Base");
        assertThat(resultado.getDetalles()).hasSize(1);
        
        DetallePromocion detalleBase = resultado.getDetalles().get(0);
        assertThat(detalleBase.getEsBase()).isTrue();
        assertThat(detalleBase.getTipoPromBase()).isEqualTo("SIN_DESCUENTO");
        assertThat(detalleBase.getLlevent()).isEqualTo(0);
        assertThat(detalleBase.getPaguen()).isEqualTo(0);
    }
    
    /**
     * Dado: Un objeto de dominio PromDsctoPlano decorando PromBase
     * Cuando: Se convierte a entidad
     * Entonces: Se crean detalles base y acumulable correctos
     */
    @Test
    @DisplayName("Convertir PromDsctoPlano debe crear entidad con detalles base y acumulable")
    void convertToEntity_ConPromDsctoPlano_DebeCrearDetallesBaseYAcumulable() {
        // Given
        PromSinDescto base = new PromSinDescto();
        PromDsctoPlano promDscto = new PromDsctoPlano(base, 20.0f);
        promDscto.setNombre("Promo 20%");
        promDscto.setDescripcion("Descuento 20% adicional");
        
        // When
        Promocion resultado = PromocionEntityConverter.convertToEntity(promDscto);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDetalles()).hasSize(2);
        
        // Verificar detalle base
        DetallePromocion detalleBase = resultado.getDetalles().stream()
            .filter(DetallePromocion::getEsBase)
            .findFirst().orElse(null);
        assertThat(detalleBase).isNotNull();
        assertThat(detalleBase.getTipoPromBase()).isEqualTo("SIN_DESCUENTO");
        
        // Verificar detalle acumulable
        DetallePromocion detalleAcum = resultado.getDetalles().stream()
            .filter(d -> !d.getEsBase())
            .findFirst().orElse(null);
        assertThat(detalleAcum).isNotNull();
        assertThat(detalleAcum.getTipoPromAcumulable()).isEqualTo("DESCUENTO_PLANO");
        assertThat(detalleAcum.getPorcDctoPlano()).isEqualTo(20.0);
    }
    
    /**
     * Dado: Un objeto de dominio PromDsctoXcantidad
     * Cuando: Se convierte a entidad
     * Entonces: Se crean detalles con escalas de descuento correctas
     */
    @Test
    @DisplayName("Convertir PromDsctoXcantidad debe crear entidad con escalas")
    void convertToEntity_ConPromDsctoXcantidad_DebeCrearEntidadConEscalas() {
        // Given
        PromSinDescto base = new PromSinDescto();
        Map<Integer, Double> escalas = Map.of(5, 5.0, 10, 10.0);
        PromDsctoXcantidad promXcant = new PromDsctoXcantidad(base, escalas);
        promXcant.setNombre("Promo Escalada");
        
        // When
        Promocion resultado = PromocionEntityConverter.convertToEntity(promXcant);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDetalles()).hasSize(2);
        
        DetallePromocion detalleAcum = resultado.getDetalles().stream()
            .filter(d -> !d.getEsBase())
            .findFirst().orElse(null);
        assertThat(detalleAcum).isNotNull();
        assertThat(detalleAcum.getTipoPromAcumulable()).isEqualTo("DESCUENTO_POR_CANTIDAD");
        assertThat(detalleAcum.getDescuentosPorCantidad()).hasSize(2);
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
        Promocion resultado = PromocionEntityConverter.convertToEntity(null);
        
        // Then
        assertThat(resultado).isNull();
    }
    
    // ===== TESTS DE MERGE =====
    
    /**
     * Dado: Una promoción de dominio y entidad existente
     * Cuando: Se hace merge
     * Entonces: Se actualiza la entidad preservando el ID
     */
    @Test
    @DisplayName("Merge debe actualizar entidad existente preservando ID")
    void mergeIntoEntity_ConEntidadExistente_DebeActualizarPreservandoId() {
        // Given
        Promocion entidadExistente = new Promocion();
        entidadExistente.setIdPromocion(123);
        entidadExistente.setNombre("Nombre Original");
        
        PromSinDescto nuevaPromo = new PromSinDescto();
        nuevaPromo.setNombre("Nombre Actualizado");
        nuevaPromo.setDescripcion("Descripción Nueva");
        
        // When
        Promocion resultado = PromocionEntityConverter.mergeIntoEntity(nuevaPromo, entidadExistente);
        
        // Then
        assertThat(resultado).isSameAs(entidadExistente);
        assertThat(resultado.getIdPromocion()).isEqualTo(123); // ID preservado
        assertThat(resultado.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(resultado.getDescripcion()).isEqualTo("Descripción Nueva");
    }
    
    /**
     * Dado: Objetos nulos para merge
     * Cuando: Se hace merge
     * Entonces: Se retorna la entidad existente sin cambios
     */
    @Test
    @DisplayName("Merge con parámetros nulos debe retornar entidad existente")
    void mergeIntoEntity_ConParametrosNulos_DebeRetornarEntidadExistente() {
        // Given
        Promocion entidadExistente = new Promocion();
        entidadExistente.setNombre("Original");
        
        // When - dominio nulo
        Promocion resultado1 = PromocionEntityConverter.mergeIntoEntity(null, entidadExistente);
        
        // When - entidad nula
        Promocion resultado2 = PromocionEntityConverter.mergeIntoEntity(new PromSinDescto(), null);
        
        // Then
        assertThat(resultado1).isSameAs(entidadExistente);
        assertThat(resultado2).isNull();
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private Promocion crearEntidadConBaseNXM(String nombre, String descripcion, int lleveN, int pagueM) {
        Promocion entidad = new Promocion();
        entidad.setNombre(nombre);
        entidad.setDescripcion(descripcion);
        
        DetallePromocion detalleBase = new DetallePromocion();
        detalleBase.setPromocion(entidad);
        detalleBase.setEsBase(true);
        detalleBase.setNombre("Base");
        detalleBase.setTipoPromBase("NXM");
        detalleBase.setLlevent(lleveN);
        detalleBase.setPaguen(pagueM);
        detalleBase.setPorcDctoPlano(0.0);
        
        entidad.addDetalle(detalleBase);
        return entidad;
    }
    
    private Promocion crearEntidadConBaseSinDescuento(String nombre, String descripcion) {
        Promocion entidad = new Promocion();
        entidad.setNombre(nombre);
        entidad.setDescripcion(descripcion);
        
        DetallePromocion detalleBase = new DetallePromocion();
        detalleBase.setPromocion(entidad);
        detalleBase.setEsBase(true);
        detalleBase.setNombre("Base");
        detalleBase.setTipoPromBase("SIN_DESCUENTO");
        detalleBase.setLlevent(0);
        detalleBase.setPaguen(0);
        detalleBase.setPorcDctoPlano(0.0);
        
        entidad.addDetalle(detalleBase);
        return entidad;
    }
    
    private Promocion crearEntidadConBaseNXMYDescuentoPlano(String nombre, int lleveN, int pagueM, float descuentoPlano) {
        Promocion entidad = crearEntidadConBaseNXM(nombre, "Combo promo", lleveN, pagueM);
        
        DetallePromocion detalleAcum = new DetallePromocion();
        detalleAcum.setPromocion(entidad);
        detalleAcum.setEsBase(false);
        detalleAcum.setNombre("Acumulable");
        detalleAcum.setTipoPromAcumulable("DESCUENTO_PLANO");
        detalleAcum.setPorcDctoPlano((double) descuentoPlano);
        
        entidad.addDetalle(detalleAcum);
        return entidad;
    }
    
    private Promocion crearEntidadConDescuentoPorCantidad(String nombre, Map<Integer, Double> escalas) {
        Promocion entidad = crearEntidadConBaseSinDescuento(nombre, "Descuento por cantidad");
        
        DetallePromocion detalleAcum = new DetallePromocion();
        detalleAcum.setPromocion(entidad);
        detalleAcum.setEsBase(false);
        detalleAcum.setNombre("Acumulable");
        detalleAcum.setTipoPromAcumulable("DESCUENTO_POR_CANTIDAD");
        detalleAcum.setPorcDctoPlano(0.0);
        
        int num = 1;
        for (Map.Entry<Integer, Double> entrada : escalas.entrySet()) {
            DetallePromDsctoXCant escala = new DetallePromDsctoXCant();
            escala.setNumDscto(num++);
            escala.setCantidad(entrada.getKey());
            escala.setDscto(entrada.getValue());
            detalleAcum.addDescuentoPorCantidad(escala);
        }
        
        entidad.addDetalle(detalleAcum);
        return entidad;
    }
}