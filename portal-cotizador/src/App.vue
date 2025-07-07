<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Navegación principal -->
    <Navbar />
    
    <!-- Contenido principal con router -->
    <main :class="authStore.isLoggedIn ? 'container mx-auto px-4 py-8' : ''">
      <RouterView />
    </main>
    
    <!-- Sistema de alertas global -->
    <AlertSystem />
    
    <!-- Sistema de loading global -->
    <GlobalLoadingManager />
    
    <!-- Modal de expiración de sesión - Solo mostrar si está autenticado -->
    <SessionExpirationModal
      v-if="authStore.isLoggedIn"
      :show="tokenMonitor.showExpirationWarning.value"
      :seconds-until-expiry="tokenMonitor.secondsUntilExpiry.value"
      :is-renewing="tokenMonitor.isRenewing.value"
      @extend="tokenMonitor.extendSession"
      @reject="tokenMonitor.rejectExtension"
    />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/layout/Navbar.vue'
import AlertSystem from '@/components/ui/AlertSystem.vue'
import GlobalLoadingManager from '@/components/ui/GlobalLoadingManager.vue'
import SessionExpirationModal from '@/components/auth/SessionExpirationModal.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { useTokenMonitor } from '@/composables/useTokenMonitor'

const authStore = useAuthStore()
const tokenMonitor = useTokenMonitor()
</script>

<style scoped>
/* Estilos específicos del componente App si son necesarios */
</style>