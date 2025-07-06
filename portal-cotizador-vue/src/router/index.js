import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/componentes'
    },
    {
      path: '/componentes',
      name: 'componentes',
      component: () => import('@/views/ComponentesView.vue'),
      meta: {
        title: 'Gestión de Componentes'
      }
    },
    {
      path: '/pcs',
      name: 'pcs',
      component: () => import('@/views/PcsView.vue'),
      meta: {
        title: 'Armado de PCs'
      }
    },
    {
      path: '/cotizaciones',
      name: 'cotizaciones',
      component: () => import('@/views/CotizacionesView.vue'),
      meta: {
        title: 'Cotizaciones'
      }
    },
    {
      path: '/proveedores',
      name: 'proveedores',
      component: () => import('@/views/ProveedoresView.vue'),
      meta: {
        title: 'Proveedores'
      }
    },
    {
      path: '/pedidos',
      name: 'pedidos',
      component: () => import('@/views/PedidosView.vue'),
      meta: {
        title: 'Pedidos'
      }
    },
    {
      path: '/promociones',
      name: 'promociones',
      component: () => import('@/views/PromocionesView.vue'),
      meta: {
        title: 'Promociones'
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue')
    }
  ]
})

// Guard para actualizar título de página
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - Portal Cotizador`
  }
  next()
})

export default router