<template>
  <!-- Navegación principal - Solo mostrar cuando esté autenticado -->
  <nav v-if="authStore.isLoggedIn" class="bg-white shadow-lg border-b">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo y marca -->
        <div class="flex-shrink-0">
          <RouterLink to="/dashboard" class="flex items-center hover:opacity-80 transition-opacity">
            <svg 
              class="h-8 w-8 text-primary-600" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                stroke-linecap="round" 
                stroke-linejoin="round" 
                stroke-width="2" 
                d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" 
              />
            </svg>
            <span class="ml-2 text-xl font-bold text-gray-900 whitespace-nowrap">
              Portal Cotizador Vue
            </span>
          </RouterLink>
        </div>
        
        <!-- Enlaces de navegación (desktop) -->
        <div class="hidden md:flex items-center space-x-6 flex-1 justify-center mx-8">
          <RouterLink
            v-for="item in navigationItems"
            :key="item.name"
            :to="item.to"
            :class="getLinkClasses(item.to)"
            class="px-3 py-2 text-sm font-medium transition-colors"
          >
            {{ item.label }}
          </RouterLink>
          
        </div>
        
        <!-- Sección de autenticación -->
        <div class="flex items-center space-x-4 flex-shrink-0">
          <div v-if="authStore.isLoggedIn" class="flex items-center space-x-3">
            <span class="text-sm text-gray-700">
              Bienvenido, {{ authStore.userName }}
            </span>
            <button
              @click="handleLogout"
              class="text-sm text-red-600 hover:text-red-800 transition-colors"
            >
              Cerrar Sesión
            </button>
          </div>
          <div v-else>
            <RouterLink
              to="/login"
              class="px-4 py-2 bg-primary-600 text-white text-sm font-medium rounded-md hover:bg-primary-700 transition-colors"
            >
              Iniciar Sesión
            </RouterLink>
          </div>
        </div>
        
        <!-- Botón menú mobile -->
        <div class="md:hidden flex items-center">
          <button
            @click="toggleMobileMenu"
            class="text-gray-500 hover:text-gray-700 focus:outline-none focus:text-gray-700"
          >
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path 
                v-if="!mobileMenuOpen"
                stroke-linecap="round" 
                stroke-linejoin="round" 
                stroke-width="2" 
                d="M4 6h16M4 12h16M4 18h16" 
              />
              <path 
                v-else
                stroke-linecap="round" 
                stroke-linejoin="round" 
                stroke-width="2" 
                d="M6 18L18 6M6 6l12 12" 
              />
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- Menú mobile -->
    <Transition name="mobile-menu">
      <div v-if="mobileMenuOpen" class="md:hidden">
        <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3 bg-gray-50">
          <RouterLink
            v-for="item in navigationItems"
            :key="item.name"
            :to="item.to"
            @click="closeMobileMenu"
            :class="getMobileLinkClasses(item.to)"
            class="block px-3 py-2 text-base font-medium rounded-md transition-colors"
          >
            {{ item.label }}
          </RouterLink>
        </div>
      </div>
    </Transition>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const route = useRoute()
const router = useRouter()
const mobileMenuOpen = ref(false)
const authStore = useAuthStore()

// Inicializar store de autenticación
onMounted(() => {
  authStore.initialize()
})

// Items de navegación (migración del HTML original)
const navigationItems = [
  { name: 'dashboard', label: 'Dashboard', to: '/dashboard' },
  { name: 'componentes', label: 'Componentes', to: '/componentes' },
  { name: 'pcs', label: 'Armado PCs', to: '/pcs' },
  { name: 'cotizaciones', label: 'Cotizaciones', to: '/cotizaciones' },
  { name: 'proveedores', label: 'Proveedores', to: '/proveedores' },
  { name: 'pedidos', label: 'Pedidos', to: '/pedidos' },
  { name: 'promociones', label: 'Promociones', to: '/promociones' }
]

// Métodos para el menú mobile
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

// Manejo de logout
const handleLogout = async () => {
  await authStore.logout()
  // Redirigir a login después de logout
  await router.push('/login')
}

// Clases CSS para enlaces (migración de lógica original)
const getLinkClasses = (to) => {
  const isActive = route.path === to
  
  if (isActive) {
    return 'text-primary-600 border-b-2 border-primary-600'
  }
  return 'text-gray-500 hover:text-gray-700 border-b-2 border-transparent hover:border-gray-300'
}

const getMobileLinkClasses = (to) => {
  const isActive = route.path === to
  
  if (isActive) {
    return 'text-primary-600 bg-primary-50'
  }
  return 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
}

// Cerrar menú mobile cuando cambia la ruta
import { watch } from 'vue'
watch(() => route.path, () => {
  closeMobileMenu()
})
</script>

<style scoped>
.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  max-height: 0;
  opacity: 0;
}

.mobile-menu-enter-to,
.mobile-menu-leave-from {
  max-height: 300px;
  opacity: 1;
}
</style>