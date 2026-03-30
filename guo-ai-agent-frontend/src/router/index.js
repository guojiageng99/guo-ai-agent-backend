import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/authToken'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'AI 应用中心 - 恋语AI恋爱大师 · AI超级智能体',
      description: 'AI 智能体应用中心，选择恋语AI恋爱大师或AI超级智能体开始智能对话'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录 - AI 智能体应用',
      description: '登录后可使用恋语 AI 与超级智能体'
    }
  },
  {
    path: '/love-app',
    name: 'LoveApp',
    component: () => import('../views/LoveAppChat.vue'),
    meta: {
      requiresAuth: true,
      title: '恋语AI恋爱大师 - AI 智能体应用',
      description: '恋语AI恋爱大师，专业的恋爱顾问，为您的情感问题提供贴心建议'
    }
  },
  {
    path: '/manus',
    name: 'Manus',
    component: () => import('../views/ManusChat.vue'),
    meta: {
      requiresAuth: true,
      title: 'AI 超级智能体 - AI 智能体应用',
      description: 'AI 超级智能体，强大的多功能智能体，助您解决各类问题'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'AI 智能体应用'
  const desc = document.querySelector('meta[name="description"]')
  if (desc && to.meta.description) {
    desc.setAttribute('content', to.meta.description)
  }
  if (to.meta.requiresAuth && !getToken()) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }
  if (to.path === '/login' && getToken()) {
    const r = typeof to.query.redirect === 'string' ? to.query.redirect : '/'
    next(r || '/')
    return
  }
  next()
})

export default router
