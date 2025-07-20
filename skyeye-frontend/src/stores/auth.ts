import { ref, computed, readonly } from 'vue'
import { defineStore } from 'pinia'

// 用户信息接口
export interface User {
  id: string
  username: string
  email: string
  fullName: string
  role: UserRole
  status: UserStatus
  permissions: string[]
  lastLoginAt?: string
  avatar?: string
}

// 用户角色枚举
export enum UserRole {
  SYSTEM_ADMIN = 'system_admin',
  SECURITY_ADMIN = 'security_admin',
  OPERATOR = 'operator',
  VIEWER = 'viewer'
}

// 用户状态枚举
export enum UserStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  LOCKED = 'locked'
}

// 登录请求接口
export interface LoginRequest {
  username: string
  password: string
  rememberMe: boolean
}

// 登录响应接口
export interface LoginResponse {
  token: string
  refreshToken: string
  user: User
  expiresIn: number
}

// API响应接口
interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data?: T
  timestamp: number
  requestId: string
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('skyeye_token') || null)
  const refreshToken = ref<string | null>(localStorage.getItem('skyeye_refresh_token') || null)
  const user = ref<User | null>(null)
  const tokenExpiry = ref<number | null>(null)

  // 计算属性
  const isAuthenticated = computed(() => {
    return !!(token.value && user.value && (!tokenExpiry.value || Date.now() < tokenExpiry.value))
  })

  const userRole = computed(() => user.value?.role)
  const userPermissions = computed(() => user.value?.permissions || [])

  // 权限检查
  const hasPermission = (permission: string): boolean => {
    return userPermissions.value.includes(permission)
  }

  const hasRole = (role: UserRole): boolean => {
    return userRole.value === role
  }

  const hasAnyRole = (roles: UserRole[]): boolean => {
    return userRole.value ? roles.includes(userRole.value) : false
  }

  // 模拟API调用 - 在实际项目中应该调用真实的API
  const mockApiCall = async <T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> => {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1000))

    const { method = 'GET', body } = options

    // 模拟登录API
    if (endpoint === '/api/auth/login' && method === 'POST') {
      const loginData = JSON.parse(body as string) as LoginRequest
      
      // 模拟用户验证
      if (loginData.username === 'admin' && loginData.password === 'admin123') {
        const mockUser: User = {
          id: '1',
          username: 'admin',
          email: 'admin@skyeye.com',
          fullName: '系统管理员',
          role: UserRole.SYSTEM_ADMIN,
          status: UserStatus.ACTIVE,
          permissions: ['user:read', 'user:write', 'device:read', 'device:write', 'alert:read', 'alert:write'],
          lastLoginAt: new Date().toISOString(),
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }

        const response: LoginResponse = {
          token: 'mock_jwt_token_' + Date.now(),
          refreshToken: 'mock_refresh_token_' + Date.now(),
          user: mockUser,
          expiresIn: 24 * 60 * 60 * 1000 // 24小时
        }

        return {
          success: true,
          code: 200,
          message: '登录成功',
          data: response,
          timestamp: Date.now(),
          requestId: 'req_' + Date.now()
        }
      } else if (loginData.username === 'operator' && loginData.password === 'operator123') {
        const mockUser: User = {
          id: '2',
          username: 'operator',
          email: 'operator@skyeye.com',
          fullName: '操作员',
          role: UserRole.OPERATOR,
          status: UserStatus.ACTIVE,
          permissions: ['device:read', 'alert:read', 'alert:write'],
          lastLoginAt: new Date().toISOString()
        }

        const response: LoginResponse = {
          token: 'mock_jwt_token_' + Date.now(),
          refreshToken: 'mock_refresh_token_' + Date.now(),
          user: mockUser,
          expiresIn: 24 * 60 * 60 * 1000
        }

        return {
          success: true,
          code: 200,
          message: '登录成功',
          data: response,
          timestamp: Date.now(),
          requestId: 'req_' + Date.now()
        }
      } else {
        throw new Error('用户名或密码错误')
      }
    }

    // 模拟忘记密码API
    if (endpoint === '/api/auth/forgot-password' && method === 'POST') {
      return {
        success: true,
        code: 200,
        message: '密码重置邮件已发送',
        timestamp: Date.now(),
        requestId: 'req_' + Date.now()
      }
    }

    // 模拟获取用户信息API
    if (endpoint === '/api/auth/me' && method === 'GET') {
      if (!token.value) {
        throw new Error('未授权')
      }

      // 根据token返回对应用户信息（简化处理）
      const mockUser: User = {
        id: '1',
        username: 'admin',
        email: 'admin@skyeye.com',
        fullName: '系统管理员',
        role: UserRole.SYSTEM_ADMIN,
        status: UserStatus.ACTIVE,
        permissions: ['user:read', 'user:write', 'device:read', 'device:write', 'alert:read', 'alert:write'],
        lastLoginAt: new Date().toISOString(),
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      }

      return {
        success: true,
        code: 200,
        message: '获取用户信息成功',
        data: mockUser,
        timestamp: Date.now(),
        requestId: 'req_' + Date.now()
      }
    }

    // 模拟刷新token API
    if (endpoint === '/api/auth/refresh' && method === 'POST') {
      if (!refreshToken.value) {
        throw new Error('刷新token无效')
      }

      return {
        success: true,
        code: 200,
        message: 'Token刷新成功',
        data: {
          token: 'new_mock_jwt_token_' + Date.now(),
          expiresIn: 24 * 60 * 60 * 1000
        },
        timestamp: Date.now(),
        requestId: 'req_' + Date.now()
      }
    }

    throw new Error('API endpoint not found')
  }

  // 登录
  const login = async (loginData: LoginRequest): Promise<boolean> => {
    try {
      const response = await mockApiCall<LoginResponse>('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(loginData)
      })

      if (response.success && response.data) {
        const { token: newToken, refreshToken: newRefreshToken, user: userData, expiresIn } = response.data

        // 保存认证信息
        token.value = newToken
        refreshToken.value = newRefreshToken
        user.value = userData
        tokenExpiry.value = Date.now() + expiresIn

        // 持久化存储
        localStorage.setItem('skyeye_token', newToken)
        localStorage.setItem('skyeye_refresh_token', newRefreshToken)
        localStorage.setItem('skyeye_user', JSON.stringify(userData))
        localStorage.setItem('skyeye_token_expiry', tokenExpiry.value.toString())

        // 处理记住我功能
        if (loginData.rememberMe) {
          localStorage.setItem('skyeye_remembered_username', loginData.username)
        } else {
          localStorage.removeItem('skyeye_remembered_username')
        }

        return true
      }

      return false
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  }

  // 登出
  const logout = async (): Promise<void> => {
    try {
      // 在实际项目中，这里应该调用登出API来使token失效
      // await mockApiCall('/api/auth/logout', { method: 'POST' })

      // 清除状态
      token.value = null
      refreshToken.value = null
      user.value = null
      tokenExpiry.value = null

      // 清除本地存储
      localStorage.removeItem('skyeye_token')
      localStorage.removeItem('skyeye_refresh_token')
      localStorage.removeItem('skyeye_user')
      localStorage.removeItem('skyeye_token_expiry')
    } catch (error) {
      console.error('Logout error:', error)
      // 即使API调用失败，也要清除本地状态
      token.value = null
      refreshToken.value = null
      user.value = null
      tokenExpiry.value = null
      
      localStorage.removeItem('skyeye_token')
      localStorage.removeItem('skyeye_refresh_token')
      localStorage.removeItem('skyeye_user')
      localStorage.removeItem('skyeye_token_expiry')
    }
  }

  // 忘记密码
  const forgotPassword = async (email: string): Promise<void> => {
    try {
      await mockApiCall('/api/auth/forgot-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email })
      })
    } catch (error) {
      console.error('Forgot password error:', error)
      throw error
    }
  }

  // 刷新token
  const refreshAuthToken = async (): Promise<boolean> => {
    try {
      if (!refreshToken.value) {
        return false
      }

      const response = await mockApiCall<{ token: string; expiresIn: number }>('/api/auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ refreshToken: refreshToken.value })
      })

      if (response.success && response.data) {
        token.value = response.data.token
        tokenExpiry.value = Date.now() + response.data.expiresIn

        localStorage.setItem('skyeye_token', response.data.token)
        localStorage.setItem('skyeye_token_expiry', tokenExpiry.value.toString())

        return true
      }

      return false
    } catch (error) {
      console.error('Token refresh error:', error)
      return false
    }
  }

  // 获取用户信息
  const fetchUserInfo = async (): Promise<void> => {
    try {
      const response = await mockApiCall<User>('/api/auth/me')
      
      if (response.success && response.data) {
        user.value = response.data
        localStorage.setItem('skyeye_user', JSON.stringify(response.data))
      }
    } catch (error) {
      console.error('Fetch user info error:', error)
      throw error
    }
  }

  // 初始化认证状态（从本地存储恢复）
  const initializeAuth = (): void => {
    const savedUser = localStorage.getItem('skyeye_user')
    const savedTokenExpiry = localStorage.getItem('skyeye_token_expiry')

    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('Failed to parse saved user:', error)
        localStorage.removeItem('skyeye_user')
      }
    }

    if (savedTokenExpiry) {
      tokenExpiry.value = parseInt(savedTokenExpiry)
    }

    // 检查token是否过期
    if (tokenExpiry.value && Date.now() >= tokenExpiry.value) {
      // Token已过期，尝试刷新
      refreshAuthToken().catch(() => {
        // 刷新失败，清除认证状态
        logout()
      })
    }
  }

  return {
    // 状态
    token: readonly(token),
    user: readonly(user),
    
    // 计算属性
    isAuthenticated,
    userRole,
    userPermissions,
    
    // 方法
    login,
    logout,
    forgotPassword,
    refreshAuthToken,
    fetchUserInfo,
    initializeAuth,
    hasPermission,
    hasRole,
    hasAnyRole
  }
})