import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        title: 'Iniciar Sesión',
        requiresGuest: true  // Solo accesible para usuarios no autenticados
      }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: {
        title: 'Dashboard',
        requiresAuth: true
      }
    },
    {
      path: '/componentes',
      name: 'componentes',
      component: () => import('@/views/ComponentesView.vue'),
      meta: {
        title: 'Gestión de Componentes',
        requiresAuth: true
      }
    },
    {
      path: '/pcs',
      name: 'pcs',
      component: () => import('@/views/PcsView.vue'),
      meta: {
        title: 'Armado de PCs',
        requiresAuth: true
      }
    },
    {
      path: '/cotizaciones',
      name: 'cotizaciones',
      component: () => import('@/views/CotizacionesView.vue'),
      meta: {
        title: 'Cotizaciones',
        requiresAuth: true
      }
    },
    {
      path: '/proveedores',
      name: 'proveedores',
      component: () => import('@/views/ProveedoresView.vue'),
      meta: {
        title: 'Proveedores',
        requiresAuth: true
      }
    },
    {
      path: '/pedidos',
      name: 'pedidos',
      component: () => import('@/views/PedidosView.vue'),
      meta: {
        title: 'Pedidos',
        requiresAuth: true
      }
    },
    {
      path: '/promociones',
      name: 'promociones',
      component: () => import('@/views/PromocionesView.vue'),
      meta: {
        title: 'Promociones',
        requiresAuth: true
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue')
    }
  ]
})

// Guard para autenticación y título de página
router.beforeEach(async (to, from, next) => {
  // Actualizar título de página
  if (to.meta.title) {
    document.title = `${to.meta.title} - Portal Cotizador`
  }

  // Obtener authStore
  const authStore = useAuthStore()
  
  // Verificar si hay token pero está expirado
  const accessToken = localStorage.getItem('accessToken')
  if (accessToken) {
    try {
      // Decodificar el token para verificar expiración
      const payload = JSON.parse(atob(accessToken.split('.')[1]))
      const currentTime = Math.floor(Date.now() / 1000)
      
      // Si el token está expirado, hacer logout forzado
      if (payload.exp && payload.exp < currentTime) {
        console.warn('[Router] Token expirado, limpiando sesión')
        authStore.forceLogout()
      }
    } catch (error) {
      // Si no se puede decodificar el token, limpiarlo
      console.warn('[Router] Token inválido, limpiando sesión')
      authStore.forceLogout()
    }
  }
  
  // Verificar autenticación al inicializar
  authStore.checkAuthentication()
  
  // Rutas que requieren autenticación
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      // Usuario no autenticado, redirigir a login
      return next('/login')
    }
  }
  
  // Rutas que requieren ser visitante (no autenticado)
  if (to.meta.requiresGuest) {
    if (authStore.isLoggedIn) {
      // Usuario ya autenticado, redirigir al dashboard
      return next('/dashboard')
    }
  }
  
  next()
})

export default router