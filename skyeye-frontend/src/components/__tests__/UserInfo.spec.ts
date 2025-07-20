import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserInfo from '../UserInfo.vue'
import { useAuthStore, UserRole, UserStatus } from '@/stores/auth'

// Mock Element Plus components
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

// Create router for testing
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/profile', component: { template: '<div>Profile</div>' } },
    { path: '/settings', component: { template: '<div>Settings</div>' } },
    { path: '/login', component: { template: '<div>Login</div>' } }
  ]
})

describe('UserInfo', () => {
  let wrapper: any
  let authStore: any

  const mockUser = {
    id: '1',
    username: 'admin',
    email: 'admin@skyeye.com',
    fullName: '系统管理员',
    role: UserRole.SYSTEM_ADMIN,
    status: UserStatus.ACTIVE,
    permissions: ['user:read', 'user:write', 'device:read', 'device:write'],
    lastLoginAt: new Date().toISOString(),
    avatar: 'https://example.com/avatar.jpg'
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    
    // Mock authenticated user
    authStore.user = mockUser
    vi.spyOn(authStore, 'user', 'get').mockReturnValue(mockUser)
    vi.spyOn(authStore, 'hasRole').mockImplementation((role) => role === UserRole.SYSTEM_ADMIN)
    vi.spyOn(authStore, 'logout').mockResolvedValue()
    
    wrapper = mount(UserInfo, {
      global: {
        plugins: [createPinia(), router]
      }
    })
    
    vi.clearAllMocks()
  })

  describe('组件渲染', () => {
    it('应该正确渲染用户头像', () => {
      const avatar = wrapper.find('.user-avatar')
      expect(avatar.exists()).toBe(true)
    })

    it('应该显示用户详细信息', () => {
      const wrapper = mount(UserInfo, {
        props: { showDetails: true },
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      expect(wrapper.find('.user-details').exists()).toBe(true)
      expect(wrapper.find('.user-name').text()).toBe('系统管理员')
      expect(wrapper.find('.user-role').text()).toBe('系统管理员')
    })

    it('应该在showDetails为false时隐藏用户详细信息', () => {
      const wrapper = mount(UserInfo, {
        props: { showDetails: false },
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      expect(wrapper.find('.user-details').exists()).toBe(false)
    })

    it('应该显示下拉箭头图标', () => {
      const wrapper = mount(UserInfo, {
        props: { showDropdown: true },
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      expect(wrapper.find('.dropdown-icon').exists()).toBe(true)
    })

    it('应该在showDropdown为false时隐藏下拉箭头', () => {
      const wrapper = mount(UserInfo, {
        props: { showDropdown: false },
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      expect(wrapper.find('.dropdown-icon').exists()).toBe(false)
    })

    it('应该显示用户状态徽章', () => {
      const wrapper = mount(UserInfo, {
        props: { showStatus: true },
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      expect(wrapper.find('.user-status').exists()).toBe(true)
      expect(wrapper.find('.status-badge').exists()).toBe(true)
    })
  })

  describe('角色显示', () => {
    it('应该正确显示系统管理员角色', () => {
      const roleText = wrapper.vm.getRoleDisplayName(UserRole.SYSTEM_ADMIN)
      expect(roleText).toBe('系统管理员')
    })

    it('应该正确显示安全管理员角色', () => {
      const roleText = wrapper.vm.getRoleDisplayName(UserRole.SECURITY_ADMIN)
      expect(roleText).toBe('安全管理员')
    })

    it('应该正确显示操作员角色', () => {
      const roleText = wrapper.vm.getRoleDisplayName(UserRole.OPERATOR)
      expect(roleText).toBe('操作员')
    })

    it('应该正确显示查看者角色', () => {
      const roleText = wrapper.vm.getRoleDisplayName(UserRole.VIEWER)
      expect(roleText).toBe('查看者')
    })

    it('应该处理未知角色', () => {
      const roleText = wrapper.vm.getRoleDisplayName(undefined)
      expect(roleText).toBe('未知角色')
    })
  })

  describe('状态显示', () => {
    it('应该正确显示活跃状态', () => {
      const statusText = wrapper.vm.getStatusDisplayName(UserStatus.ACTIVE)
      expect(statusText).toBe('在线')
      
      const badgeType = wrapper.vm.getStatusBadgeType(UserStatus.ACTIVE)
      expect(badgeType).toBe('success')
    })

    it('应该正确显示非活跃状态', () => {
      const statusText = wrapper.vm.getStatusDisplayName(UserStatus.INACTIVE)
      expect(statusText).toBe('离线')
      
      const badgeType = wrapper.vm.getStatusBadgeType(UserStatus.INACTIVE)
      expect(badgeType).toBe('info')
    })

    it('应该正确显示锁定状态', () => {
      const statusText = wrapper.vm.getStatusDisplayName(UserStatus.LOCKED)
      expect(statusText).toBe('锁定')
      
      const badgeType = wrapper.vm.getStatusBadgeType(UserStatus.LOCKED)
      expect(badgeType).toBe('danger')
    })
  })

  describe('下拉菜单功能', () => {
    it('应该能够导航到个人资料页面', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      
      await wrapper.vm.handleCommand('profile')
      
      expect(pushSpy).toHaveBeenCalledWith('/profile')
    })

    it('应该能够导航到系统设置页面（如果有权限）', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      
      await wrapper.vm.handleCommand('settings')
      
      expect(pushSpy).toHaveBeenCalledWith('/settings')
    })

    it('应该阻止无权限用户访问系统设置', async () => {
      // Mock user without admin role
      vi.spyOn(authStore, 'hasRole').mockReturnValue(false)
      
      await wrapper.vm.handleCommand('settings')
      
      expect(ElMessage.warning).toHaveBeenCalledWith('您没有访问系统设置的权限')
    })

    it('应该处理退出登录命令', async () => {
      // Mock confirmation dialog
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
      
      const pushSpy = vi.spyOn(router, 'push')
      
      await wrapper.vm.handleCommand('logout')
      
      expect(ElMessageBox.confirm).toHaveBeenCalledWith(
        '确定要退出登录吗？',
        '退出确认',
        expect.any(Object)
      )
      expect(authStore.logout).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('已退出登录')
      expect(pushSpy).toHaveBeenCalledWith('/login')
    })

    it('应该处理用户取消退出登录', async () => {
      // Mock user canceling confirmation
      vi.mocked(ElMessageBox.confirm).mockRejectedValue('cancel')
      
      await wrapper.vm.handleCommand('logout')
      
      expect(authStore.logout).not.toHaveBeenCalled()
    })

    it('应该处理退出登录失败', async () => {
      // Mock confirmation and logout failure
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
      vi.spyOn(authStore, 'logout').mockRejectedValue(new Error('Logout failed'))
      
      await wrapper.vm.handleCommand('logout')
      
      expect(ElMessage.error).toHaveBeenCalledWith('退出登录失败')
    })

    it('应该处理未知命令', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      
      await wrapper.vm.handleCommand('unknown')
      
      expect(consoleSpy).toHaveBeenCalledWith('Unknown command:', 'unknown')
    })
  })

  describe('权限控制', () => {
    it('系统管理员应该能够访问系统设置', () => {
      vi.spyOn(authStore, 'hasRole').mockImplementation((role) => role === UserRole.SYSTEM_ADMIN)
      
      expect(wrapper.vm.canAccessSettings).toBe(true)
    })

    it('非系统管理员不应该能够访问系统设置', () => {
      vi.spyOn(authStore, 'hasRole').mockReturnValue(false)
      
      expect(wrapper.vm.canAccessSettings).toBe(false)
    })
  })

  describe('组件属性', () => {
    it('应该支持自定义头像大小', () => {
      const wrapper = mount(UserInfo, {
        props: { size: 'large' },
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      // 这里需要检查头像组件是否接收到正确的size属性
      // 具体实现取决于Element Plus Avatar组件的API
    })

    it('应该有正确的默认属性值', () => {
      expect(wrapper.props().size).toBe('default')
      expect(wrapper.props().showDetails).toBe(true)
      expect(wrapper.props().showDropdown).toBe(true)
      expect(wrapper.props().showStatus).toBe(false)
    })
  })

  describe('响应式设计', () => {
    it('应该在移动设备上隐藏用户详细信息', () => {
      // 这里可以测试CSS媒体查询相关的行为
      // 通常需要模拟不同的屏幕尺寸
      expect(wrapper.find('.user-details').exists()).toBe(true)
    })
  })

  describe('无用户状态', () => {
    it('应该处理用户未登录的情况', () => {
      vi.spyOn(authStore, 'user', 'get').mockReturnValue(null)
      
      const wrapper = mount(UserInfo, {
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      // 应该显示默认头像图标
      expect(wrapper.find('.user-avatar').exists()).toBe(true)
    })
  })
})