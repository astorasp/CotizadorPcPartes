# Análisis del Proyecto: Sistema de Cotización de Partes de PC

*Última actualización: 2024-07-19*

Este documento es la fuente de verdad consolidada sobre la arquitectura, funcionalidades y mecanismos internos del proyecto, basado en un análisis exhaustivo del código fuente.

---

## 1. Descripción General del Proyecto

El proyecto es una aplicación web integral para la **gestión del ciclo de venta de componentes de hardware y PCs personalizadas**. Sirve como una herramienta interna para un negocio, cubriendo desde la gestión de catálogos y proveedores hasta la creación de cotizaciones y el seguimiento de pedidos.

---

## 2. Arquitectura del Sistema

El sistema sigue un patrón de **arquitectura de microservicios**, orquestado a través de **Docker**. Esta elección de diseño promueve la separación de responsabilidades, la escalabilidad y el despliegue independiente de cada componente.

### 2.1. Componentes Principales

| Componente | Responsabilidad | Tecnologías Clave |
| :--- | :--- | :--- |
| **`portal-cotizador`** | Frontend (SPA) | Vue.js 3, Pinia, TailwindCSS, Vue Router |
| **`ms-cotizador`** | Microservicio de Negocio | Java 21, Spring Boot, DDD, MySQL |
| **`ms-seguridad`** | Microservicio de Seguridad | Java 21, Spring Boot, JWT, MySQL |
| **`nginx-gateway`** | API Gateway | NGINX (Proxy Inverso) |
| **`cotizador-mysql`** | Base de Datos | MySQL (Datos de negocio) |
| **`seguridad-mysql`** | Base de Datos | MySQL (Usuarios, roles, sesiones) |

### 2.2. Patrones Arquitectónicos Clave

*   **API Gateway:** `nginx-gateway` actúa como el único punto de entrada, enrutando las peticiones al microservicio correspondiente y ocultando la topología de la red interna.
*   **Aislamiento de Datos:** Cada microservicio (`ms-cotizador` y `ms-seguridad`) posee su propia base de datos, garantizando un bajo acoplamiento.
*   **Diseño Orientado al Dominio (DDD):** `ms-cotizador` está estructurado en capas (Dominio, Aplicación, Infraestructura) para alinear el código con la lógica del negocio.
*   **Infraestructura como Código (IaC):** El archivo `docker-compose.yml` define y configura todo el entorno de la aplicación, garantizando consistencia y facilidad de despliegue.

---

## 3. Catálogo de Módulos y Funcionalidades

Este es el inventario completo de las capacidades del sistema, agrupadas por módulo funcional.

#### Módulo 1: Autenticación y Gestión de Sesión
*   **Iniciar Sesión:** Validación de credenciales (usuario/contraseña).
*   **Cerrar Sesión:** Finalización de la sesión activa.
*   **Ver Perfil Propio:** Visualización de la información de la cuenta del usuario.
*   **Manejo de Sesión:** Persistencia de sesión y gestión de expiración de tokens.

#### Módulo 2: Gestión de Catálogo (Componentes)
*   CRUD completo (Crear, Leer, Actualizar, Eliminar) para componentes de hardware.
*   Búsqueda y filtrado avanzado de componentes.

#### Módulo 3: Ensamblaje de PCs (Constructor de PCs)
*   Creación de nuevas configuraciones de PC.
*   Selección de componentes compatibles por ranura.
*   **Cálculo de Costo de Componentes:** Suma de los costos brutos de los componentes seleccionados (cálculo en el frontend).
*   Guardado y carga de configuraciones de PC.

#### Módulo 4: Gestión de Cotizaciones
*   Creación de cotizaciones a partir de ensamblajes de PC.
*   Asignación de clientes a cotizaciones.
*   Listado, búsqueda y filtrado de cotizaciones.
*   **Cálculo de Precio Final:** Lógica de negocio en el backend para aplicar promociones, márgenes e impuestos.
*   Gestión de estados de la cotización (Borrador, Enviada, Aprobada, etc.).

#### Módulo 5: Gestión de Pedidos
*   Conversión de una cotización aprobada en un pedido formal.
*   Listado, búsqueda y filtrado de pedidos.
*   Gestión de estados del pedido (Recibido, En preparación, Enviado, etc.).

#### Módulo 6: Gestión de Promociones
*   CRUD completo para las promociones del sistema (ej. descuentos, 2x1).
*   Activación y desactivación de promociones.

#### Módulo 7: Gestión de Usuarios
*   CRUD completo para las cuentas de usuario.
*   Asignación y modificación de roles y permisos.
*   Activación/desactivación de cuentas y reseteo de contraseñas.

#### Módulo 8: Gestión de Proveedores
*   CRUD completo para los proveedores.
*   Búsqueda de proveedores por nombre comercial o razón social.

---

## 4. Manejo de Sesión en el Portal (Flujo Detallado)

El portal utiliza un sistema de **autenticación stateless basado en JSON Web Tokens (JWT)**, orquestado por 5 componentes clave en el frontend:

1.  **`services/authService.js` (El Comunicador):** Realiza las llamadas a la API de `ms-seguridad` para `login`.
2.  **`stores/useAuthStore.js` (El Cerebro):** Mantiene el estado global de la sesión (token, datos de usuario) y lo persiste en `localStorage`.
3.  **`services/apiClient.js` (El Guardaespaldas):** Intercepta todas las peticiones a la API y les adjunta automáticamente el token JWT en la cabecera `Authorization`.
4.  **`router/index.js` (El Portero):** Utiliza "Navigation Guards" (`router.beforeEach`) para proteger las rutas que requieren autenticación, redirigiendo al login si el usuario no ha iniciado sesión.
5.  **`composables/useTokenMonitor.js` (El Relojero):** Vigila la fecha de expiración del token para gestionar proactivamente el fin de la sesión.

### Proceso Típico:
1.  **Login:** El usuario introduce su **nombre de usuario** y contraseña. `authService` lo envía a `ms-seguridad`. Si es exitoso, `useAuthStore` guarda el token JWT recibido en `localStorage`.
2.  **Navegación:** El usuario intenta acceder a una ruta protegida. El `router` verifica con `useAuthStore` si hay un token válido. Si no, redirige a `/login`.
3.  **Petición a API:** Al solicitar datos (ej. lista de proveedores), `apiClient` intercepta la llamada y le inyecta el token antes de enviarla a `ms-cotizador`.
4.  **Logout:** Al cerrar sesión, `useAuthStore` elimina el token de su estado y de `localStorage`, invalidando la sesión en el cliente.