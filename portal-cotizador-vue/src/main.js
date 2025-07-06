import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// Importar estilos
import './assets/styles/main.css'

// Crear aplicación Vue
const app = createApp(App)

// Configurar plugins
const pinia = createPinia()
app.use(pinia)
app.use(router)

// Inicializar stores ANTES de montar la aplicación
import { useAuthStore } from '@/stores/useAuthStore'
const authStore = useAuthStore()
authStore.initialize()

// Montar aplicación después de inicializar el authStore
app.mount('#app')