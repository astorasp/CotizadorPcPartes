package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import mx.com.qtx.cotizadorv1ds.config.SpringContextProvider;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Componente;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.TipoComponente;
import mx.com.qtx.cotizadorv1ds.core.componentes.TipoComponenteEnum;
    
/**
 * Clase de prueba para ComponenteRepository usando Java puro (sin frameworks de testing)
 */
public class ComponenteRepositoryTest {
    
    private ComponenteRepositorio componenteRepository;
    private TipoComponenteRepositorio tipoComponenteRepository;
    
    public ComponenteRepositoryTest() {
        // Inicializar el contexto de Spring si no está inicializado
        SpringContextProvider.initialize();
        
        // Obtener los repositorios del contexto de Spring
        this.componenteRepository = SpringContextProvider.getBean(ComponenteRepositorio.class);
        this.tipoComponenteRepository = SpringContextProvider.getBean(TipoComponenteRepositorio.class);
    }
    
    /**
     * Prueba la creación y recuperación de un componente
     */
    public void testCrearYRecuperarComponente() {
        System.out.println("\n=== Prueba: Crear y Recuperar Componente ===");
        
        // Obtener un tipo de componente (o crear uno si no existe)
        TipoComponente tipoComponente = obtenerTipoComponente(TipoComponenteEnum.MONITOR.name());
        
        // Crear un nuevo componente
        Componente componente = new Componente();
        String idComponente = "TEST-MON-" + System.currentTimeMillis();
        componente.setId(idComponente);
        componente.setDescripcion("Monitor de prueba");
        componente.setMarca("Marca Test");
        componente.setModelo("Modelo Test");
        componente.setCosto(new BigDecimal("100.00"));
        componente.setPrecioBase(new BigDecimal("150.00"));
        componente.setTipoComponente(tipoComponente);
        
        // Guardar el componente
        Componente componenteGuardado = componenteRepository.save(componente);
        System.out.println("Componente guardado: " + componenteGuardado.getId() + " - " + componenteGuardado.getDescripcion());
        
        // Recuperar el componente por ID
        Optional<Componente> componenteRecuperado = componenteRepository.findById(idComponente);
        if (componenteRecuperado.isPresent()) {
            System.out.println("Componente recuperado: " + componenteRecuperado.get().getId() + " - " + componenteRecuperado.get().getDescripcion());
        } else {
            System.out.println("ERROR: No se pudo recuperar el componente");
        }
    }
    
    /**
     * Prueba la búsqueda de componentes por tipo
     */
    public void testBuscarPorTipo() {
        System.out.println("\n=== Prueba: Buscar por Tipo de Componente ===");
        
        // Obtener un tipo de componente existente
        TipoComponente tipoComponente = obtenerTipoComponente(TipoComponenteEnum.MONITOR.name());
        
        // Buscar componentes por tipo
        List<Componente> componentes = componenteRepository.findByTipoComponenteId(tipoComponente.getId());
        System.out.println("Componentes encontrados para el tipo " + tipoComponente.getNombre() + ": " + componentes.size());
        
        // Mostrar los componentes encontrados
        for (Componente comp : componentes) {
            System.out.println("- " + comp.getId() + " - " + comp.getDescripcion() + " - " + comp.getMarca());
        }
    }
    
    /**
     * Prueba la búsqueda de componentes por marca
     */
    public void testBuscarPorMarca() {
        System.out.println("\n=== Prueba: Buscar por Marca ===");
        
        // Buscar componentes por marca (usando una parte del nombre de la marca)
        String marcaBusqueda = "Test"; // Ajustar según los datos disponibles
        List<Componente> componentes = componenteRepository.findByMarcaContainingIgnoreCase(marcaBusqueda);
        System.out.println("Componentes encontrados con marca que contiene '" + marcaBusqueda + "': " + componentes.size());
        
        // Mostrar los componentes encontrados
        for (Componente comp : componentes) {
            System.out.println("- " + comp.getId() + " - " + comp.getDescripcion() + " - " + comp.getMarca());
        }
    }
    
    /**
     * Prueba la búsqueda de componentes por rango de precio
     */
    public void testBuscarPorRangoPrecio() {
        System.out.println("\n=== Prueba: Buscar por Rango de Precio ===");
        
        // Definir rango de precios para la búsqueda
        BigDecimal precioMin = new BigDecimal("100.00");
        BigDecimal precioMax = new BigDecimal("500.00");
        
        // Buscar componentes en el rango de precios
        List<Componente> componentes = componenteRepository.findByPrecioBaseBetween(precioMin, precioMax);
        System.out.println("Componentes encontrados con precio entre " + precioMin + " y " + precioMax + ": " + componentes.size());
        
        // Mostrar los componentes encontrados
        for (Componente comp : componentes) {
            System.out.println("- " + comp.getId() + " - " + comp.getDescripcion() + " - Precio: " + comp.getPrecioBase());
        }
    }
    
    /**
     * Prueba listar todos los componentes
     */
    public void testListarTodos() {
        System.out.println("\n=== Prueba: Listar Todos los Componentes ===");
        
        // Obtener todos los componentes
        List<Componente> todosComponentes = componenteRepository.findAll();
        System.out.println("Total de componentes: " + todosComponentes.size());
        
        // Mostrar los primeros 5 componentes (o menos si hay menos de 5)
        int limite = Math.min(5, todosComponentes.size());
        for (int i = 0; i < limite; i++) {
            Componente comp = todosComponentes.get(i);
            System.out.println("- " + comp.getId() + " - " + comp.getDescripcion() + " - " + comp.getMarca());
        }
        
        if (todosComponentes.size() > 5) {
            System.out.println("... y " + (todosComponentes.size() - 5) + " más");
        }
    }

    private TipoComponente obtenerTipoComponente(String nombre) {
        // Intentar recuperar el primer tipo de componente
        Optional<TipoComponente> tipoComponente = tipoComponenteRepository.findByNombreIgnoreCase(nombre);
        
        if (tipoComponente.isPresent()) {
            return tipoComponente.get();
        } else {
            // Si no existe ningún tipo, crear uno nuevo
            TipoComponente nuevoTipo = new TipoComponente();
            nuevoTipo.setId((short)1);
            nuevoTipo.setNombre("Tipo de Prueba");
            return tipoComponenteRepository.save(nuevoTipo);
        }
    }
    
    /**
     * Método principal para ejecutar todas las pruebas
     */
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de ComponenteRepository...");
        
        ComponenteRepositoryTest test = new ComponenteRepositoryTest();
        
        try {
            // Ejecutar las pruebas
            test.testCrearYRecuperarComponente();
            test.testBuscarPorTipo();
            test.testBuscarPorMarca();
            test.testBuscarPorRangoPrecio();
            test.testListarTodos();
            
            System.out.println("\nTodas las pruebas completadas exitosamente.");
        } catch (Exception e) {
            System.err.println("\nError durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
