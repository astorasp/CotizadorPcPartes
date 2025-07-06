<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Navegación principal -->
    <Navbar />
    
    <!-- Contenido principal con router -->
    <main class="container mx-auto px-4 py-8">
      <RouterView />
    </main>
    
    <!-- Sistema de alertas global -->
    <AlertSystem />
    
    <!-- Modal de login global -->
    <LoginModal
      :show="authStore.showLoginModal"
      @close="authStore.closeLoginModal"
      @success="authStore.handleLoginSuccess"
    />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/layout/Navbar.vue'
import AlertSystem from '@/components/ui/AlertSystem.vue'
import LoginModal from '@/components/auth/LoginModal.vue'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
</script>

<style scoped>
/* Estilos específicos del componente App si son necesarios */
</style>