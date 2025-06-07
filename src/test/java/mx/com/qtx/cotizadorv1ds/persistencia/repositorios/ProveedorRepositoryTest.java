package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import mx.com.qtx.cotizadorv1ds.config.SpringContextProvider;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Proveedor;

/**
 * Clase de prueba para ProveedorRepository usando Java puro (sin frameworks de testing)
 */
public class ProveedorRepositoryTest {
    
    private ProveedorRepositorio proveedorRepository;
    
    public ProveedorRepositoryTest() {
        // Inicializar el contexto de Spring si no está inicializado
        SpringContextProvider.initialize();
        
        // Obtener el repositorio del contexto de Spring
        this.proveedorRepository = SpringContextProvider.getBean(ProveedorRepositorio.class);
    }
    
    /**
     * Prueba la creación y recuperación de un proveedor
     */
    public void testCrearYRecuperarProveedor() {
        System.out.println("\n=== Prueba: Crear y Recuperar Proveedor ===");
        
        // Crear un nuevo proveedor
        Proveedor proveedor = new Proveedor();
        proveedor.setCve("TEST-001");
        proveedor.setNombre("Proveedor de Prueba");
        proveedor.setRazonSocial("Pruebas Empresariales S.A. de C.V.");
        
        // Guardar el proveedor
        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
        System.out.println("Proveedor guardado: Clave " + proveedorGuardado.getCve() + 
                         ", Nombre: " + proveedorGuardado.getNombre() + 
                         ", Razón Social: " + proveedorGuardado.getRazonSocial());
        
        // Recuperar el proveedor por clave
        Proveedor proveedorRecuperado = proveedorRepository.findByCve(proveedorGuardado.getCve());
        if (proveedorRecuperado != null) {
            System.out.println("Proveedor recuperado: Clave " + proveedorRecuperado.getCve() + 
                             ", Nombre: " + proveedorRecuperado.getNombre() + 
                             ", Razón Social: " + proveedorRecuperado.getRazonSocial());
        } else {
            System.out.println("ERROR: No se pudo recuperar el proveedor");
        }
    }
    
    /**
     * Prueba la búsqueda de proveedores por nombre
     */
    public void testBuscarPorNombre() {
        System.out.println("\n=== Prueba: Buscar Proveedor por Nombre ===");
        
        // Buscar proveedores por nombre
        String nombreBusqueda = "Tech";
        List<Proveedor> proveedores = proveedorRepository.findByNombreContainingIgnoreCase(nombreBusqueda);
        System.out.println("Proveedores encontrados con nombre que contiene '" + nombreBusqueda + "': " + proveedores.size());
        
        // Mostrar los proveedores encontrados
        for (Proveedor prov : proveedores) {
            System.out.println("- Clave: " + prov.getCve() + ", Nombre: " + prov.getNombre() + 
                             ", Razón Social: " + prov.getRazonSocial());
        }
    }
    
    /**
     * Prueba la búsqueda de proveedores por razón social
     */
    public void testBuscarPorRazonSocial() {
        System.out.println("\n=== Prueba: Buscar Proveedor por Razón Social ===");
        
        // Buscar proveedor por razón social
        String razonSocialBusqueda = "Tech"; // Parte de TechSupply SA
        List<Proveedor> proveedores = proveedorRepository.findByRazonSocialContainingIgnoreCase(razonSocialBusqueda);
        
        if (!proveedores.isEmpty()) {
            System.out.println("Proveedores encontrados con razón social que contiene '" + razonSocialBusqueda + "': " + proveedores.size());
            for (Proveedor prov : proveedores) {
                System.out.println("- Clave: " + prov.getCve() + ", Nombre: " + prov.getNombre() + 
                                 ", Razón Social: " + prov.getRazonSocial());
            }
        } else {
            System.out.println("No se encontró ningún proveedor con razón social que contiene '" + razonSocialBusqueda + "'");
        }
    }
    
    /**
     * Prueba la búsqueda de proveedores por clave
     */
    public void testBuscarPorClave() {
        System.out.println("\n=== Prueba: Buscar Proveedor por Clave ===");
        
        // Buscar proveedor por clave
        String claveBusqueda = "TECH-001"; // Supuesta clave de TechSupply SA
        Proveedor proveedor = proveedorRepository.findByCve(claveBusqueda);
        
        if (proveedor != null) {
            System.out.println("Proveedor encontrado con clave '" + claveBusqueda + "':");
            System.out.println("- Clave: " + proveedor.getCve() + ", Nombre: " + proveedor.getNombre() + 
                             ", Razón Social: " + proveedor.getRazonSocial());
        } else {
            System.out.println("No se encontró ningún proveedor con clave '" + claveBusqueda + "'");
        }
    }
    
    /**
     * Prueba listar todos los proveedores
     */
    public void testListarTodos() {
        System.out.println("\n=== Prueba: Listar Todos los Proveedores ===");
        
        // Obtener todos los proveedores
        List<Proveedor> todosProveedores = proveedorRepository.findAll();
        System.out.println("Total de proveedores: " + todosProveedores.size());
        
        // Mostrar los primeros 5 proveedores (o menos si hay menos de 5)
        int limite = Math.min(5, todosProveedores.size());
        for (int i = 0; i < limite; i++) {
            Proveedor prov = todosProveedores.get(i);
            System.out.println("- Clave: " + prov.getCve() + ", Nombre: " + prov.getNombre() + 
                             ", Razón Social: " + prov.getRazonSocial());
        }
        
        if (todosProveedores.size() > 5) {
            System.out.println("... y " + (todosProveedores.size() - 5) + " más");
        }
    }
    
    /**
     * Método principal para ejecutar todas las pruebas
     */
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de ProveedorRepository...");
        
        ProveedorRepositoryTest test = new ProveedorRepositoryTest();
        
        try {
            // Ejecutar las pruebas
            test.testCrearYRecuperarProveedor();
            test.testBuscarPorNombre();
            test.testBuscarPorRazonSocial();
            test.testBuscarPorClave();
            test.testListarTodos();
            
            System.out.println("\nTodas las pruebas completadas exitosamente.");
        } catch (Exception e) {
            System.err.println("\nError durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
