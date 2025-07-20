import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: {
        requiresAuth: false,
        title: '用户登录'
      }
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        requiresAuth: true,
        title: '首页'
      }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: {
        requiresAuth: true,
        title: '关于'
      }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: {
        requiresAuth: true,
        title: '监控仪表板',
        permissions: ['dashboard:read']
      }
    },
    {
      path: '/devices',
      name: 'devices',
      component: () => import('../views/DevicesView.vue'),
      meta: {
        requiresAuth: true,
        title: '设备管理',
        permissions: ['device:read']
      }
    },
    {
      path: '/alerts',
      name: 'alerts',
      component: () => import('../views/AlertsView.vue'),
      meta: {
        requiresAuth: true,
        title: '报警管理',
        permissions: ['alert:read']
      }
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UsersView.vue'),
      meta: {
        requiresAuth: true,
        title: '用户管理',
        permissions: ['user:read'],
        roles: ['system_admin', 'security_admin']
      }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        requiresAuth: true,
        title: '个人资料'
      }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/SettingsView.vue'),
      meta: {
        requiresAuth: true,
        title: '系统设置',
        roles: ['system_admin']
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: {
        requiresAuth: false,
        title: '页面未找到'
      }
    }
  ],
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - SkyEye 智能监控系统`
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next({
        name: 'login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 检查权限
    if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
      const hasPermission = to.meta.permissions.some((permission: string) => 
        authStore.hasPermission(permission)
      )
      
      if (!hasPermission) {
        ElMessage.error('您没有访问此页面的权限')
        next({ name: 'home' })
        return
      }
    }

    // 检查角色
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const hasRole = authStore.hasAnyRole(to.meta.roles as any[])
      
      if (!hasRole) {
        ElMessage.error('您的角色无法访问此页面')
        next({ name: 'home' })
        return
      }
    }
  } else {
    // 如果已登录用户访问登录页，重定向到首页
    if (to.name === 'login' && authStore.isAuthenticated) {
      next({ name: 'home' })
      return
    }
  }

  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('Router error:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router
