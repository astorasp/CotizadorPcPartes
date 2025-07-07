import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

// Views
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import UsuariosView from '@/views/UsuariosView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const routes = [
  // Ruta raíz - redirige al dashboard o login
  {
    path: '/',
    redirect: () => {
      const authStore = useAuthStore()
      return authStore.isLoggedIn ? '/dashboard' : '/login'
    }
  },
  
  // Login
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { 
      requiresAuth: false,
      title: 'Iniciar Sesión'
    }
  },
  
  // Dashboard
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: { 
      requiresAuth: true,
      title: 'Dashboard'
    }
  },
  
  // Gestión de Usuarios (Solo ADMIN)
  {
    path: '/usuarios',
    name: 'Usuarios',
    component: UsuariosView,
    meta: { 
      requiresAuth: true,
      requiresRole: 'ADMIN',
      title: 'Gestión de Usuarios'
    }
  },
  
  // Rutas futuras para otras secciones
  {
    path: '/componentes',
    name: 'Componentes',
    redirect: '/dashboard', // Temporalmente redirige al dashboard
    meta: { 
      requiresAuth: true,
      title: 'Componentes'
    }
  },
  
  {
    path: '/pcs',
    name: 'PCs',
    redirect: '/dashboard',
    meta: { 
      requiresAuth: true,
      title: 'Armado PCs'
    }
  },
  
  {
    path: '/cotizaciones',
    name: 'Cotizaciones',
    redirect: '/dashboard',
    meta: { 
      requiresAuth: true,
      title: 'Cotizaciones'
    }
  },
  
  {
    path: '/proveedores',
    name: 'Proveedores',
    redirect: '/dashboard',
    meta: { 
      requiresAuth: true,
      title: 'Proveedores'
    }
  },
  
  {
    path: '/pedidos',
    name: 'Pedidos',
    redirect: '/dashboard',
    meta: { 
      requiresAuth: true,
      title: 'Pedidos'
    }
  },
  
  {
    path: '/promociones',
    name: 'Promociones',
    redirect: '/dashboard',
    meta: { 
      requiresAuth: true,
      title: 'Promociones'
    }
  },
  
  // 404 - Página no encontrada
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFoundView,
    meta: {
      title: 'Página no encontrada'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // Scroll al inicio en cada cambio de ruta
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// Navigation Guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // Actualizar título de la página
  if (to.meta.title) {
    document.title = `${to.meta.title} - Portal Cotizador Vue`
  }
  
  // Verificar autenticación
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      // Usuario no autenticado, redirigir al login
      console.log('[Router] Usuario no autenticado, redirigiendo al login')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Verificar roles si es necesario
    if (to.meta.requiresRole) {
      if (!authStore.hasRole(to.meta.requiresRole)) {
        console.log(`[Router] Usuario no tiene el rol requerido: ${to.meta.requiresRole}`)
        next({
          path: '/dashboard',
          query: { error: 'access_denied' }
        })
        return
      }
    }
  }
  
  // Si el usuario está autenticado e intenta ir al login, redirigir al dashboard
  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

router.afterEach((to, from) => {
  // Logs para debugging en desarrollo
  if (import.meta.env.DEV) {
    console.log(`[Router] Navegando de ${from.path} a ${to.path}`)
  }
})

// Manejo de errores de navegación
router.onError((error) => {
  console.error('[Router] Error de navegación:', error)
})

export default router