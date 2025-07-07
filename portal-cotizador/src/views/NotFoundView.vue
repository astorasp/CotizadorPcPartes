<template>
  <div class="min-h-96 flex items-center justify-center">
    <div class="text-center">
      <div class="mx-auto h-24 w-24 text-gray-300">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
        </svg>
      </div>
      <h1 class="mt-4 text-3xl font-bold text-gray-900">404</h1>
      <p class="mt-2 text-lg text-gray-600">Página no encontrada</p>
      <p class="mt-2 text-sm text-gray-500">
        La página que buscas no existe o ha sido movida.
      </p>
      <div class="mt-6">
        <RouterLink to="/componentes" class="btn-primary btn-md">
          Volver al inicio
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
// Componente simple para páginas no encontradas
</script>