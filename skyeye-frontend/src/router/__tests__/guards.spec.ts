import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore, UserRole } from '@/stores/auth'

// Mock Element Plus
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      warning: vi.fn(),
      error: vi.fn()
    }
  }
})

// Mock views
const mockViews = {
  LoginView: { template: '<div>Login</div>' },
  HomeView: { template: '<div>Home</div>' },
  DashboardView: { template: '<div>Dashboard</div>' },
  UsersView: { template: '<div>Users</div>' },
  SettingsView: { template: '<div>Settings</div>' }
}

describe('Router Guards', () => {
  let router: any
  let authStore: any

  beforeEach(async () => {
    setActivePinia(createPinia())
    authStore = useAuthStore()

    // Create router with the same configuration as the actual router
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/login',
          name: 'login',
          component: mockViews.LoginView,
          meta: {
            requiresAuth: false,
            title: '用户登录'
          }
        },
        {
          path: '/',
          name: 'home',
          component: mockViews.HomeView,
          meta: {
            requiresAuth: true,
            title: '首页'
          }
        },
        {
          path: '/dashboard',
          name: 'dashboard',
          component: mockViews.DashboardView,
          meta: {
            requiresAuth: true,
            title: '监控仪表板',
            permissions: ['dashboard:read']
          }
        },
        {
          path: '/users',
          name: 'users',
          component: mockViews.UsersView,
          meta: {
            requiresAuth: true,
            title: '用户管理',
            permissions: ['user:read'],
            roles: ['system_admin', 'security_admin']
          }
        },
        {
          path: '/settings',
          name: 'settings',
          component: mockViews.SettingsView,
          meta: {
            requiresAuth: true,
            title: '系统设置',
            roles: ['system_admin']
          }
        }
      ]
    })

    // Add the same beforeEach guard as in the actual router
    router.beforeEach(async (to: any, from: any, next: any) => {
      // Set page title
      if (to.meta.title) {
        document.title = `${to.meta.title} - SkyEye 智能监控系统`
      }

      // Check if authentication is required
      if (to.meta.requiresAuth) {
        if (!authStore.isAuthenticated) {
          ElMessage.warning('请先登录')
          next({
            name: 'login',
            query: { redirect: to.fullPath }
          })
          return
        }

        // Check permissions
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

        // Check roles
        if (to.meta.roles && Array.isArray(to.meta.roles)) {
          const hasRole = authStore.hasAnyRole(to.meta.roles as any[])
          
          if (!hasRole) {
            ElMessage.error('您的角色无法访问此页面')
            next({ name: 'home' })
            return
          }
        }
      } else {
        // If authenticated user visits login page, redirect to home
        if (to.name === 'login' && authStore.isAuthenticated) {
          next({ name: 'home' })
          return
        }
      }

      next()
    })

    vi.clearAllMocks()
  })

  describe('页面标题设置', () => {
    it('应该设置正确的页面标题', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      
      await router.push('/dashboard')
      
      expect(document.title).toBe('监控仪表板 - SkyEye 智能监控系统')
    })

    it('没有标题的页面不应该改变document.title', async () => {
      const originalTitle = document.title
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(false)
      
      // Create a route without title
      router.addRoute({
        path: '/no-title',
        name: 'no-title',
        component: { template: '<div>No Title</div>' },
        meta: { requiresAuth: false }
      })
      
      await router.push('/no-title')
      
      expect(document.title).toBe(originalTitle)
    })
  })

  describe('认证检查', () => {
    it('未认证用户访问需要认证的页面应该重定向到登录页', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(false)
      
      await router.push('/')
      
      expect(ElMessage.warning).toHaveBeenCalledWith('请先登录')
      expect(router.currentRoute.value.name).toBe('login')
      expect(router.currentRoute.value.query.redirect).toBe('/')
    })

    it('已认证用户可以访问需要认证的页面', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      vi.spyOn(authStore, 'hasPermission').mockReturnValue(true)
      vi.spyOn(authStore, 'hasAnyRole').mockReturnValue(true)
      
      await router.push('/')
      
      expect(router.currentRoute.value.name).toBe('home')
    })

    it('已认证用户访问登录页应该重定向到首页', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      
      await router.push('/login')
      
      expect(router.currentRoute.value.name).toBe('home')
    })

    it('未认证用户可以访问不需要认证的页面', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(false)
      
      await router.push('/login')
      
      expect(router.currentRoute.value.name).toBe('login')
    })
  })

  describe('权限检查', () => {
    beforeEach(() => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
    })

    it('有权限的用户可以访问需要权限的页面', async () => {
      vi.spyOn(authStore, 'hasPermission').mockImplementation((permission) => 
        permission === 'dashboard:read'
      )
      
      await router.push('/dashboard')
      
      expect(router.currentRoute.value.name).toBe('dashboard')
    })

    it('没有权限的用户不能访问需要权限的页面', async () => {
      vi.spyOn(authStore, 'hasPermission').mockReturnValue(false)
      
      await router.push('/dashboard')
      
      expect(ElMessage.error).toHaveBeenCalledWith('您没有访问此页面的权限')
      expect(router.currentRoute.value.name).toBe('home')
    })

    it('应该检查多个权限中的任意一个', async () => {
      // Mock route with multiple permissions
      router.addRoute({
        path: '/multi-permission',
        name: 'multi-permission',
        component: { template: '<div>Multi Permission</div>' },
        meta: {
          requiresAuth: true,
          permissions: ['perm1', 'perm2', 'perm3']
        }
      })

      vi.spyOn(authStore, 'hasPermission').mockImplementation((permission) => 
        permission === 'perm2'
      )
      
      await router.push('/multi-permission')
      
      expect(router.currentRoute.value.name).toBe('multi-permission')
    })
  })

  describe('角色检查', () => {
    beforeEach(() => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      vi.spyOn(authStore, 'hasPermission').mockReturnValue(true)
    })

    it('有正确角色的用户可以访问需要角色的页面', async () => {
      vi.spyOn(authStore, 'hasAnyRole').mockImplementation((roles) => 
        roles.includes('system_admin')
      )
      
      await router.push('/settings')
      
      expect(router.currentRoute.value.name).toBe('settings')
    })

    it('没有正确角色的用户不能访问需要角色的页面', async () => {
      vi.spyOn(authStore, 'hasAnyRole').mockReturnValue(false)
      
      await router.push('/settings')
      
      expect(ElMessage.error).toHaveBeenCalledWith('您的角色无法访问此页面')
      expect(router.currentRoute.value.name).toBe('home')
    })

    it('应该检查多个角色中的任意一个', async () => {
      vi.spyOn(authStore, 'hasAnyRole').mockImplementation((roles) => 
        roles.includes('security_admin')
      )
      
      await router.push('/users')
      
      expect(router.currentRoute.value.name).toBe('users')
    })
  })

  describe('复合权限检查', () => {
    beforeEach(() => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
    })

    it('应该同时检查权限和角色', async () => {
      vi.spyOn(authStore, 'hasPermission').mockImplementation((permission) => 
        permission === 'user:read'
      )
      vi.spyOn(authStore, 'hasAnyRole').mockImplementation((roles) => 
        roles.includes('system_admin')
      )
      
      await router.push('/users')
      
      expect(router.currentRoute.value.name).toBe('users')
    })

    it('权限不足时应该阻止访问', async () => {
      vi.spyOn(authStore, 'hasPermission').mockReturnValue(false)
      vi.spyOn(authStore, 'hasAnyRole').mockReturnValue(true)
      
      await router.push('/users')
      
      expect(ElMessage.error).toHaveBeenCalledWith('您没有访问此页面的权限')
      expect(router.currentRoute.value.name).toBe('home')
    })

    it('角色不足时应该阻止访问', async () => {
      vi.spyOn(authStore, 'hasPermission').mockReturnValue(true)
      vi.spyOn(authStore, 'hasAnyRole').mockReturnValue(false)
      
      await router.push('/users')
      
      expect(ElMessage.error).toHaveBeenCalledWith('您的角色无法访问此页面')
      expect(router.currentRoute.value.name).toBe('home')
    })
  })

  describe('重定向功能', () => {
    it('登录后应该重定向到原始请求的页面', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(false)
      
      // First, try to access a protected page
      await router.push('/dashboard')
      
      expect(router.currentRoute.value.name).toBe('login')
      expect(router.currentRoute.value.query.redirect).toBe('/dashboard')
    })

    it('应该处理复杂的重定向路径', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(false)
      
      await router.push('/dashboard?tab=monitoring&filter=active')
      
      expect(router.currentRoute.value.query.redirect).toBe('/dashboard?tab=monitoring&filter=active')
    })
  })

  describe('边界情况', () => {
    it('应该处理空的权限数组', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      
      router.addRoute({
        path: '/empty-permissions',
        name: 'empty-permissions',
        component: { template: '<div>Empty Permissions</div>' },
        meta: {
          requiresAuth: true,
          permissions: []
        }
      })
      
      await router.push('/empty-permissions')
      
      expect(router.currentRoute.value.name).toBe('empty-permissions')
    })

    it('应该处理空的角色数组', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      
      router.addRoute({
        path: '/empty-roles',
        name: 'empty-roles',
        component: { template: '<div>Empty Roles</div>' },
        meta: {
          requiresAuth: true,
          roles: []
        }
      })
      
      await router.push('/empty-roles')
      
      expect(router.currentRoute.value.name).toBe('empty-roles')
    })

    it('应该处理undefined的meta属性', async () => {
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      
      router.addRoute({
        path: '/no-meta',
        name: 'no-meta',
        component: { template: '<div>No Meta</div>' }
      })
      
      await router.push('/no-meta')
      
      expect(router.currentRoute.value.name).toBe('no-meta')
    })
  })
})