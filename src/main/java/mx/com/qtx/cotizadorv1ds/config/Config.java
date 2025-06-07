package mx.com.qtx.cotizadorv1ds.config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import mx.com.qtx.cotizadorv1ds.core.ICotizador;

public class Config {
	
	public static ICotizador getCotizador() {
		
		List<Class<?>> lstImplmICotizador = Config.buscarImplementacionesDe(ICotizador.class, "mx.com.qtx.cotizadorv1ds");
		int i = (int)(Math.random()*10000) % lstImplmICotizador.size();
		Class<?> claseCotizador = lstImplmICotizador.get(i);
		try {
			Constructor<?> constructorCotizador = claseCotizador.getDeclaredConstructor();
			ICotizador cotizador = (ICotizador) constructorCotizador.newInstance();
			return cotizador;
		} 
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | 
			   IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

    static List<Class<?>> buscarImplementacionesDe(Class<?> interfaceBuscada, String nomPaquete) {
        List<Class<?>> lstImplementaciones = new ArrayList<>();
        String rutaPaquete = nomPaquete.replace('.', '/');
        
        String[] classpathEntries = System.getProperty("java.class.path")
        		                          .split(File.pathSeparator);
        
        for (String entradaClassPathI:classpathEntries) {
        	
             	mostrarRecurso(entradaClassPathI);
             	
            	if(entradaClassPathI.endsWith(".jar")) {
            		processJarResource(entradaClassPathI, rutaPaquete, interfaceBuscada, lstImplementaciones);
            	}
            	else {
                    buscarImplementsEnDirRecursivamente(new File(entradaClassPathI), nomPaquete, interfaceBuscada, lstImplementaciones);
                }
         }
        
        return lstImplementaciones;
    }


	private static boolean archivoEsUnaClaseJava(File file) {
		if(file.isFile() == false)
			return false;
		
		return file.getName().endsWith(".class");
	}

    private static void buscarImplementsEnDirRecursivamente(File dirI, String nomPaquete, 
                                       Class<?> interfaceBuscada, List<Class<?>> lstImplementaciones) {
    	
//    	System.out.println("buscarImplementsEnDirRecursivamente(" + dirI.getAbsolutePath() + ", " 
//							    								   + nomPaquete + ", " 
//							                                       + interfaceBuscada.getName() + ", " 
//							    			                       + lstImplementaciones.getClass().getName() + ", ");
    	
    	// Recorrer todos los archivos en el directorio
        for (File fileI : dirI.listFiles()) {
            try {
                 
                if (archivoEsUnaClaseJava(fileI)) {   // Procesar solo archivos .class
                	String nomSimpleArcI = fileI.getName();
                	String paqueteFinal = calcularPaqueteFinal(dirI.getAbsolutePath(), nomPaquete);
                    agregarClaseSiImplementaInterface(paqueteFinal, interfaceBuscada, lstImplementaciones, nomSimpleArcI);
                }
                else 
                if (fileI.isDirectory()) { // Procesar subdirectorios recursivamente
                	String subDir = dirI.getAbsolutePath() + "\\" + fileI.getName();
                    buscarImplementsEnDirRecursivamente(new File(subDir), nomPaquete, interfaceBuscada, lstImplementaciones);
                }
                
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                System.err.println("Clase no encontrada: " + fileI.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }
	

	private static void agregarClaseSiImplementaInterface(String nomPaquete, Class<?> interfaceBuscada,
			List<Class<?>> lstImplementaciones, String nomSimpleArcI) throws ClassNotFoundException {

//		System.out.println("agregarClaseSiImplementaInterface(" + nomPaquete + ", " + interfaceBuscada.getName() + ", "
//							+ lstImplementaciones + ", " + nomSimpleArcI + " )");
		
		String nomCalifClaseI = nomPaquete + '.' 
							  + nomSimpleArcI.substring(0, nomSimpleArcI.length() - 6);
		
		Class<?> claseI = Class.forName(nomCalifClaseI);
		
		if(claseImplementaInterface(interfaceBuscada,claseI)){
		    lstImplementaciones.add(claseI);
		}
	}
	
	private static void mostrarRecurso(String resource) {
		System.out.println("path recurso I:" + resource);
		
	}
	
	private static void processJarResource(String rutaJar, String rutaPaquete, 
                              Class<?> interfaceBuscada, List<Class<?>> lstImplementaciones) {
	    try (JarFile jar = new JarFile(rutaJar)) {
	    	
	        Enumeration<JarEntry> enumEntradaJar = jar.entries();
	        while (enumEntradaJar.hasMoreElements()) {
	            JarEntry itemDelJar = enumEntradaJar.nextElement();
	            
	            if (itemJarReferenciaUnaClase(rutaPaquete, itemDelJar)) {
	                String nomClase = itemDelJar.getName().replace('/', '.')
	                		                               .replace(".class", "");
	                Class<?> claseI = Class.forName(nomClase);
	                if (claseImplementaInterface(interfaceBuscada, claseI)) {
	                    lstImplementaciones.add(claseI);
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	private static boolean claseImplementaInterface(Class<?> interfaceBuscada, Class<?> claseI) {
		if(claseI.isInterface())
			return false;
		return interfaceBuscada.isAssignableFrom(claseI);
	}

	private static boolean itemJarReferenciaUnaClase(String rutaPaquete, JarEntry itemDelJar) {
		String nomItemJar = itemDelJar.getName();
		
		if(nomItemJar.startsWith(rutaPaquete) == false)
			return false;
		
		if(nomItemJar.endsWith(".class"))
			return true;
		else
			return false;
	}	
	
	static void buscarRecursoEnClassPath(String rutaRecursoBuscado) {
		System.out.println("\n=========== Recurso buscado: " + rutaRecursoBuscado + " ===========");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String resourcePath = rutaRecursoBuscado; // Ruta a una clase específica
		URL resourceUrl = classLoader.getResource(resourcePath);

		if (resourceUrl != null) {
		    System.out.println("Recurso encontrado en: " + resourceUrl);
		    if (resourceUrl.getProtocol().equals("jar")) {
		        System.out.println("El recurso está dentro de un JAR.");
		    }
		} else {
		    System.out.println("El recurso no existe en el classpath.");
		}
		System.out.println("============================================"
				+ "==============================================\n");
	}

    static String calcularPaqueteFinal(String rutaAbsArchivo, String base) {
        // 1. Convertir el paquete base a segmentos (ej: "a.b.c" → ["a", "b", "c"])
        String[] segmentosBase = base.split("\\.");
        
        // 2. Dividir la ruta en segmentos (ignorando separadores \ o /)
        String[] segmentosRuta = rutaAbsArchivo.split("[\\\\/]+");
        
        // 3. Buscar la posición donde los segmentosBase coinciden en la ruta
        int inicioCoincidencia = encontrarCoincidencia(segmentosRuta, segmentosBase);
        
        if (inicioCoincidencia == -1) {
            throw new IllegalArgumentException("El paquete base no está contenido en la ruta: " + base);
        }
        
        // 4. Obtener los segmentos restantes después de la coincidencia
        List<String> restantes = new ArrayList<>();
        for (int i = inicioCoincidencia + segmentosBase.length; i < segmentosRuta.length; i++) {
            restantes.add(segmentosRuta[i]);
        }
        
        // 5. Construir el paquete final
        return restantes.isEmpty() ? 
               base : 
               base + "." + String.join(".", restantes);
    }
    
    private static int encontrarCoincidencia(String[] segmentosRuta, String[] segmentosBase) {
        // Recorre la ruta buscando la secuencia del paquete base
        for (int i = 0; i <= segmentosRuta.length - segmentosBase.length; i++) {
            boolean coincide = true;
            for (int j = 0; j < segmentosBase.length; j++) {
                if (!segmentosRuta[i + j].equals(segmentosBase[j])) {
                    coincide = false;
                    break;
                }
            }
            if (coincide) {
                return i; // Posición donde empieza la coincidencia
            }
        }
        return -1; // No se encontró
    }
	
	
}
