package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import mx.com.qtx.cotizadorv1ds.config.SpringContextProvider;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Cotizacion;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Componente;

/**
 * Clase de prueba para CotizacionRepository usando Java puro (sin frameworks de testing)
 */
public class CotizacionRepositoryTest {
    
    private CotizacionRepositorio cotizacionRepository;
    private ComponenteRepositorio componenteRepository;
    
    public CotizacionRepositoryTest() {
        // Inicializar el contexto de Spring si no está inicializado
        SpringContextProvider.initialize();
        
        // Obtener los repositorios del contexto de Spring
        this.cotizacionRepository = SpringContextProvider.getBean(CotizacionRepositorio.class);
        this.componenteRepository = SpringContextProvider.getBean(ComponenteRepositorio.class);
    }
    
    /**
     * Prueba la creación y recuperación de una cotización
     */
    public void testCrearYRecuperarCotizacion() {
        System.out.println("\n=== Prueba: Crear y Recuperar Cotización ===");
        
        // Crear una nueva cotización
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFecha("2025-05-11");
        cotizacion.setImpuestos(new BigDecimal("50.00"));
        cotizacion.setTotal(new BigDecimal("350.00"));
        cotizacion.setSubtotal(new BigDecimal("300.00"));   
        
        // Guardar la cotización
        Cotizacion cotizacionGuardada = cotizacionRepository.save(cotizacion);
        System.out.println("Cotización guardada: Folio " + cotizacionGuardada.getFolio() + 
                         ", Fecha: " + cotizacionGuardada.getFecha() + 
                         ", Total: " + cotizacionGuardada.getTotal());
        
        // Recuperar la cotización por folio
        Optional<Cotizacion> cotizacionRecuperada = cotizacionRepository.findById(cotizacionGuardada.getFolio());
        if (cotizacionRecuperada.isPresent()) {
            System.out.println("Cotización recuperada: Folio " + cotizacionRecuperada.get().getFolio() + 
                             ", Fecha: " + cotizacionRecuperada.get().getFecha() + 
                             ", Total: " + cotizacionRecuperada.get().getTotal());
        } else {
            System.out.println("ERROR: No se pudo recuperar la cotización");
        }
    }
    
    /**
     * Prueba la búsqueda de cotizaciones por fecha
     */
    public void testBuscarPorFecha() {
        System.out.println("\n=== Prueba: Buscar por Fecha ===");
        
        // Buscar cotizaciones por fecha
        String fechaBusqueda = "2025"; // Ajustar según los datos disponibles
        List<Cotizacion> cotizaciones = cotizacionRepository.findByFechaContaining(fechaBusqueda);
        System.out.println("Cotizaciones encontradas con fecha que contiene '" + fechaBusqueda + "': " + cotizaciones.size());
        
        // Mostrar las cotizaciones encontradas
        for (Cotizacion cot : cotizaciones) {
            System.out.println("- Folio: " + cot.getFolio() + ", Fecha: " + cot.getFecha() + ", Total: " + cot.getTotal());
        }
    }
    
    /**
     * Prueba la búsqueda de cotizaciones por rango de monto total
     */
    public void testBuscarPorRangoTotal() {
        System.out.println("\n=== Prueba: Buscar por Rango de Total ===");
        
        // Definir rango de totales para la búsqueda
        BigDecimal totalMin = new BigDecimal("100.00");
        BigDecimal totalMax = new BigDecimal("500.00");
        
        // Buscar cotizaciones en el rango de totales
        List<Cotizacion> cotizaciones = cotizacionRepository.findByTotalBetween(totalMin, totalMax);
        System.out.println("Cotizaciones encontradas con total entre " + totalMin + " y " + totalMax + ": " + cotizaciones.size());
        
        // Mostrar las cotizaciones encontradas
        for (Cotizacion cot : cotizaciones) {
            System.out.println("- Folio: " + cot.getFolio() + ", Fecha: " + cot.getFecha() + ", Total: " + cot.getTotal());
        }
    }
    
    /**
     * Prueba la búsqueda de cotizaciones con monto total mayor a un valor dado
     */
    public void testBuscarPorTotalMayorQue() {
        System.out.println("\n=== Prueba: Buscar por Total Mayor Que ===");
        
        // Definir total mínimo para la búsqueda
        BigDecimal totalMin = new BigDecimal("300.00");
        
        // Buscar cotizaciones con total mayor al mínimo
        List<Cotizacion> cotizaciones = cotizacionRepository.findByTotalGreaterThan(totalMin);
        System.out.println("Cotizaciones encontradas con total mayor a " + totalMin + ": " + cotizaciones.size());
        
        // Mostrar las cotizaciones encontradas
        for (Cotizacion cot : cotizaciones) {
            System.out.println("- Folio: " + cot.getFolio() + ", Fecha: " + cot.getFecha() + ", Total: " + cot.getTotal());
        }
    }
    
    /**
     * Prueba la búsqueda de cotizaciones que contienen un componente específico
     */
    public void testBuscarPorComponente() {
        System.out.println("\n=== Prueba: Buscar por Componente ===");
        
        // Obtener primer componente disponible
        List<Componente> componentes = componenteRepository.findAll();
        if (componentes.isEmpty()) {
            System.out.println("No hay componentes disponibles para realizar esta prueba");
            return;
        }
        
        String idComponente = componentes.get(0).getId();
        System.out.println("Buscando cotizaciones con el componente ID: " + idComponente);
        
        // Buscar cotizaciones con el componente
        List<Cotizacion> cotizaciones = cotizacionRepository.findCotizacionesByComponente(idComponente);
        System.out.println("Cotizaciones encontradas con el componente " + idComponente + ": " + cotizaciones.size());
        
        // Mostrar las cotizaciones encontradas
        for (Cotizacion cot : cotizaciones) {
            System.out.println("- Folio: " + cot.getFolio() + ", Fecha: " + cot.getFecha() + ", Total: " + cot.getTotal());
        }
    }
    
    /**
     * Prueba listar todas las cotizaciones
     */
    public void testListarTodas() {
        System.out.println("\n=== Prueba: Listar Todas las Cotizaciones ===");
        
        // Obtener todas las cotizaciones
        List<Cotizacion> todasCotizaciones = cotizacionRepository.findAll();
        System.out.println("Total de cotizaciones: " + todasCotizaciones.size());
        
        // Mostrar las primeras 5 cotizaciones (o menos si hay menos de 5)
        int limite = Math.min(5, todasCotizaciones.size());
        for (int i = 0; i < limite; i++) {
            Cotizacion cot = todasCotizaciones.get(i);
            System.out.println("- Folio: " + cot.getFolio() + ", Fecha: " + cot.getFecha() + ", Total: " + cot.getTotal());
        }
        
        if (todasCotizaciones.size() > 5) {
            System.out.println("... y " + (todasCotizaciones.size() - 5) + " más");
        }
    }
    
    /**
     * Método principal para ejecutar todas las pruebas
     */
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de CotizacionRepository...");
        
        CotizacionRepositoryTest test = new CotizacionRepositoryTest();
        
        try {
            // Ejecutar las pruebas
            test.testCrearYRecuperarCotizacion();
            test.testBuscarPorFecha();
            test.testBuscarPorRangoTotal();
            test.testBuscarPorTotalMayorQue();
            test.testBuscarPorComponente();
            test.testListarTodas();
            
            System.out.println("\nTodas las pruebas completadas exitosamente.");
        } catch (Exception e) {
            System.err.println("\nError durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
