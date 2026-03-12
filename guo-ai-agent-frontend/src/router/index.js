import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: 'AI 应用中心' }
  },
  {
    path: '/love-app',
    name: 'LoveApp',
    component: () => import('../views/LoveAppChat.vue'),
    meta: { title: 'AI 恋爱大师' }
  },
  {
    path: '/manus',
    name: 'Manus',
    component: () => import('../views/ManusChat.vue'),
    meta: { title: 'AI 超级智能体' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'AI 智能体应用'
  next()
})

export default router
