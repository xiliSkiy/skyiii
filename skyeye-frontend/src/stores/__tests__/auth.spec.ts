import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore, UserRole, UserStatus } from '../auth'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      const authStore = useAuthStore()
      
      expect(authStore.token).toBeNull()
      expect(authStore.user).toBeNull()
      expect(authStore.isAuthenticated).toBe(false)
      expect(authStore.userRole).toBeUndefined()
      expect(authStore.userPermissions).toEqual([])
    })
  })

  describe('登录功能', () => {
    it('应该能够成功登录管理员用户', async () => {
      const authStore = useAuthStore()
      
      const loginData = {
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      }

      const result = await authStore.login(loginData)
      
      expect(result).toBe(true)
      expect(authStore.isAuthenticated).toBe(true)
      expect(authStore.user?.username).toBe('admin')
      expect(authStore.user?.role).toBe(UserRole.SYSTEM_ADMIN)
      expect(authStore.userPermissions).toContain('user:read')
      expect(authStore.userPermissions).toContain('user:write')
    })

    it('应该能够成功登录操作员用户', async () => {
      const authStore = useAuthStore()
      
      const loginData = {
        username: 'operator',
        password: 'operator123',
        rememberMe: false
      }

      const result = await authStore.login(loginData)
      
      expect(result).toBe(true)
      expect(authStore.isAuthenticated).toBe(true)
      expect(authStore.user?.username).toBe('operator')
      expect(authStore.user?.role).toBe(UserRole.OPERATOR)
      expect(authStore.userPermissions).toContain('device:read')
      expect(authStore.userPermissions).not.toContain('user:write')
    })

    it('应该拒绝错误的登录凭据', async () => {
      const authStore = useAuthStore()
      
      const loginData = {
        username: 'admin',
        password: 'wrongpassword',
        rememberMe: false
      }

      await expect(authStore.login(loginData)).rejects.toThrow('用户名或密码错误')
      expect(authStore.isAuthenticated).toBe(false)
      expect(authStore.user).toBeNull()
    })

    it('应该在记住我选项启用时保存用户名', async () => {
      const authStore = useAuthStore()
      
      const loginData = {
        username: 'admin',
        password: 'admin123',
        rememberMe: true
      }

      await authStore.login(loginData)
      
      expect(localStorageMock.setItem).toHaveBeenCalledWith('skyeye_remembered_username', 'admin')
    })

    it('应该在记住我选项禁用时移除保存的用户名', async () => {
      const authStore = useAuthStore()
      
      const loginData = {
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      }

      await authStore.login(loginData)
      
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('skyeye_remembered_username')
    })

    it('应该将认证信息保存到localStorage', async () => {
      const authStore = useAuthStore()
      
      const loginData = {
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      }

      await authStore.login(loginData)
      
      expect(localStorageMock.setItem).toHaveBeenCalledWith('skyeye_token', expect.any(String))
      expect(localStorageMock.setItem).toHaveBeenCalledWith('skyeye_refresh_token', expect.any(String))
      expect(localStorageMock.setItem).toHaveBeenCalledWith('skyeye_user', expect.any(String))
      expect(localStorageMock.setItem).toHaveBeenCalledWith('skyeye_token_expiry', expect.any(String))
    })
  })

  describe('登出功能', () => {
    it('应该能够成功登出', async () => {
      const authStore = useAuthStore()
      
      // 先登录
      await authStore.login({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
      
      expect(authStore.isAuthenticated).toBe(true)
      
      // 然后登出
      await authStore.logout()
      
      expect(authStore.isAuthenticated).toBe(false)
      expect(authStore.user).toBeNull()
      expect(authStore.token).toBeNull()
    })

    it('应该清除localStorage中的认证信息', async () => {
      const authStore = useAuthStore()
      
      // 先登录
      await authStore.login({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
      
      // 然后登出
      await authStore.logout()
      
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('skyeye_token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('skyeye_refresh_token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('skyeye_user')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('skyeye_token_expiry')
    })
  })

  describe('权限检查功能', () => {
    it('应该正确检查用户权限', async () => {
      const authStore = useAuthStore()
      
      await authStore.login({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
      
      expect(authStore.hasPermission('user:read')).toBe(true)
      expect(authStore.hasPermission('user:write')).toBe(true)
      expect(authStore.hasPermission('nonexistent:permission')).toBe(false)
    })

    it('应该正确检查用户角色', async () => {
      const authStore = useAuthStore()
      
      await authStore.login({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
      
      expect(authStore.hasRole(UserRole.SYSTEM_ADMIN)).toBe(true)
      expect(authStore.hasRole(UserRole.OPERATOR)).toBe(false)
    })

    it('应该正确检查多个角色', async () => {
      const authStore = useAuthStore()
      
      await authStore.login({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
      
      expect(authStore.hasAnyRole([UserRole.SYSTEM_ADMIN, UserRole.SECURITY_ADMIN])).toBe(true)
      expect(authStore.hasAnyRole([UserRole.OPERATOR, UserRole.VIEWER])).toBe(false)
    })

    it('未认证用户应该没有任何权限', () => {
      const authStore = useAuthStore()
      
      expect(authStore.hasPermission('user:read')).toBe(false)
      expect(authStore.hasRole(UserRole.SYSTEM_ADMIN)).toBe(false)
      expect(authStore.hasAnyRole([UserRole.SYSTEM_ADMIN])).toBe(false)
    })
  })

  describe('忘记密码功能', () => {
    it('应该能够发送密码重置邮件', async () => {
      const authStore = useAuthStore()
      
      await expect(authStore.forgotPassword('test@example.com')).resolves.not.toThrow()
    })
  })

  describe('token刷新功能', () => {
    it('应该能够刷新有效的token', async () => {
      const authStore = useAuthStore()
      
      // 先登录获取refresh token
      await authStore.login({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
      
      const result = await authStore.refreshAuthToken()
      expect(result).toBe(true)
    })

    it('没有refresh token时应该返回false', async () => {
      const authStore = useAuthStore()
      
      const result = await authStore.refreshAuthToken()
      expect(result).toBe(false)
    })
  })

  describe('初始化认证状态', () => {
    it('应该从localStorage恢复用户信息', () => {
      const mockUser = {
        id: '1',
        username: 'admin',
        email: 'admin@skyeye.com',
        fullName: '系统管理员',
        role: UserRole.SYSTEM_ADMIN,
        status: UserStatus.ACTIVE,
        permissions: ['user:read', 'user:write']
      }
      
      localStorageMock.getItem.mockImplementation((key) => {
        switch (key) {
          case 'skyeye_token':
            return 'mock_token'
          case 'skyeye_refresh_token':
            return 'mock_refresh_token'
          case 'skyeye_user':
            return JSON.stringify(mockUser)
          case 'skyeye_token_expiry':
            return (Date.now() + 3600000).toString() // 1小时后过期
          default:
            return null
        }
      })
      
      const authStore = useAuthStore()
      authStore.initializeAuth()
      
      expect(authStore.user?.username).toBe('admin')
      expect(authStore.isAuthenticated).toBe(true)
    })

    it('应该处理无效的用户数据', () => {
      localStorageMock.getItem.mockImplementation((key) => {
        switch (key) {
          case 'skyeye_user':
            return 'invalid_json'
          default:
            return null
        }
      })
      
      const authStore = useAuthStore()
      authStore.initializeAuth()
      
      expect(authStore.user).toBeNull()
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('skyeye_user')
    })
  })
})