# Análisis del Proyecto por Gemini

Basado en el análisis de los archivos `README.md`, `docker-compose.yml` y la configuración de NGINX, aquí tienes una descripción completa del proyecto y su arquitectura.

### Descripción General del Proyecto

El proyecto es un **Sistema de Cotización de Partes de PC**, una aplicación web integral diseñada para gestionar todo el ciclo de vida de la venta de componentes de hardware y computadoras personalizadas.

**Funcionalidad Principal:**

*   **Gestión de Catálogo:** Permite administrar un inventario de componentes de hardware (CPUs, GPUs, monitores, etc.), incluyendo sus costos, precios, marcas y proveedores.
*   **Ensamblaje de PCs:** Ofrece una herramienta para "construir" PCs personalizadas, seleccionando componentes compatibles y calculando el precio final automáticamente.
*   **Cotizaciones:** Permite a los vendedores crear y gestionar cotizaciones para clientes, aplicando promociones y calculando impuestos según el país.
*   **Gestión de Pedidos:** Convierte cotizaciones aprobadas en pedidos formales y permite dar seguimiento a su estado.
*   **Gestión de Proveedores y Promociones:** Administra la información de los proveedores y permite crear diversas promociones (ej. descuentos por volumen, ofertas N×M).
*   **Seguridad y Roles:** Implementa un sistema de Control de Acceso Basado en Roles (RBAC) con 5 roles predefinidos (Administrador, Gerente, Vendedor, Inventario, Consultor), cada uno con permisos específicos para acceder a diferentes funcionalidades.

---

### Arquitectura del Sistema

El sistema está diseñado siguiendo una **arquitectura de microservicios**, lo que significa que está compuesto por varios servicios más pequeños e independientes que trabajan juntos. Todos los servicios están containerizados con **Docker**, lo que facilita su despliegue y escalabilidad.

**Componentes Principales:**

1.  **`portal-cotizador` (Frontend):**
    *   **Tecnología:** Es una Single Page Application (SPA) desarrollada con **Vue.js 3**.
    *   **UI/UX:** Utiliza **TailwindCSS** para un diseño moderno y responsivo. Una característica clave es su **sistema de loading centralizado**, que proporciona feedback visual consistente al usuario durante cualquier operación (ej. guardar, cargar datos), evitando acciones duplicadas y mejorando la experiencia.
    *   **Gestión de Estado:** Usa **Pinia** para manejar el estado de la aplicación de forma centralizada (datos de usuario, listas de componentes, etc.).

2.  **`ms-cotizador` (Microservicio de Negocio):**
    *   **Tecnología:** Es el cerebro de la aplicación, desarrollado en **Java 21** con el framework **Spring Boot**.
    *   **Diseño:** Sigue los principios de **Domain-Driven Design (DDD)**, lo que organiza el código en capas claras (Dominio, Aplicación, Infraestructura) y lo alinea con la lógica del negocio.
    *   **Funcionalidad:** Expone una **API REST** que maneja toda la lógica de negocio: gestión de componentes, armado de PCs, cotizaciones, pedidos, etc.
    *   **Base de Datos:** Utiliza una base de datos **MySQL** dedicada para almacenar toda su información.

3.  **`ms-seguridad` (Microservicio de Seguridad):**
    *   **Tecnología:** También es un servicio de **Spring Boot** y **Java 21**.
    *   **Funcionalidad:** Su única responsabilidad es la seguridad. Gestiona la autenticación de usuarios, la validación de credenciales y la creación/validación de **Tokens JWT (JSON Web Tokens)**. Centralizar la seguridad en un microservicio es una práctica recomendada que mejora la robustez del sistema.
    *   **Base de Datos:** Utiliza su propia base de datos **MySQL** para almacenar usuarios, roles y sesiones.

4.  **`nginx-gateway` (API Gateway):**
    *   **Tecnología:** Es un servidor web **NGINX** que actúa como la única puerta de entrada a todo el sistema.
    *   **Funcionalidad:** Recibe todas las peticiones del exterior y las redirige al servicio correspondiente:
        *   Si la petición es para la interfaz web (ej. `http://localhost/`), la envía al `portal-cotizador`.
        *   Si es una llamada a la API de negocio (ej. `/api/cotizador/...`), la envía a `ms-cotizador`.
        *   Si es una llamada a la API de seguridad (ej. `/api/seguridad/login`), la envía a `ms-seguridad`.
    *   **Beneficios:** Este enfoque simplifica la comunicación, mejora la seguridad al no exponer los microservicios directamente y centraliza la gestión de rutas.

5.  **Bases de Datos (`cotizador-mysql` y `seguridad-mysql`):**
    *   Son dos instancias de **MySQL** independientes, una para cada microservicio. Esta separación asegura que los servicios sean autónomos y que un fallo en una base de datos no afecte directamente al otro.

**Flujo de Comunicación:**

1.  El **usuario** abre el portal web en su navegador.
2.  El **API Gateway (NGINX)** sirve la aplicación Vue.js.
3.  Para iniciar sesión, el portal envía las credenciales al Gateway, que las redirige al **`ms-seguridad`**.
4.  `ms-seguridad` valida las credenciales contra su base de datos y, si son correctas, devuelve un **token JWT**.
5.  A partir de ese momento, para cualquier otra operación (ej. "listar componentes"), el portal envía una petición al Gateway, incluyendo el token JWT.
6.  El Gateway redirige la petición al **`ms-cotizador`**.
7.  `ms-cotizador`, antes de procesar la petición, valida el token JWT (posiblemente comunicándose con `ms-seguridad`) para asegurar que el usuario tiene permiso.
8.  Una vez validado, `ms-cotizador` ejecuta la lógica de negocio, consulta su base de datos y devuelve el resultado.

En resumen, es un sistema moderno, bien estructurado y robusto que separa claramente las responsabilidades en microservicios especializados, siguiendo las mejores prácticas de desarrollo de software actual.
