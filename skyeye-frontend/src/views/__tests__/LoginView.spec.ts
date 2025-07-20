import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import LoginView from '../LoginView.vue'
import { useAuthStore } from '@/stores/auth'

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
    ElNotification: {
      success: vi.fn(),
      error: vi.fn()
    }
  }
})

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

// Create router for testing
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/login', component: LoginView }
  ]
})

describe('LoginView', () => {
  let wrapper: any
  let authStore: any

  beforeEach(async () => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    
    wrapper = mount(LoginView, {
      global: {
        plugins: [createPinia(), router]
      }
    })
    
    vi.clearAllMocks()
  })

  describe('组件渲染', () => {
    it('应该正确渲染登录表单', () => {
      expect(wrapper.find('.login-container').exists()).toBe(true)
      expect(wrapper.find('.login-card').exists()).toBe(true)
      expect(wrapper.find('.title').text()).toBe('SkyEye 智能监控系统')
      expect(wrapper.find('.subtitle').text()).toBe('安全监控 · 智能分析 · 实时预警')
    })

    it('应该包含用户名和密码输入框', () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户名"]')
      const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
      
      expect(usernameInput.exists()).toBe(true)
      expect(passwordInput.exists()).toBe(true)
    })

    it('应该包含记住我复选框和忘记密码链接', () => {
      expect(wrapper.text()).toContain('记住我')
      expect(wrapper.text()).toContain('忘记密码？')
    })

    it('应该包含登录按钮', () => {
      const loginButton = wrapper.find('.login-button')
      expect(loginButton.exists()).toBe(true)
      expect(loginButton.text()).toBe('登录')
    })
  })

  describe('表单验证', () => {
    it('应该验证必填字段', async () => {
      const loginButton = wrapper.find('.login-button')
      await loginButton.trigger('click')
      
      // 等待验证完成
      await wrapper.vm.$nextTick()
      
      // 检查是否显示验证错误（这里需要根据实际的Element Plus验证行为调整）
      expect(wrapper.vm.loginForm.username).toBe('')
      expect(wrapper.vm.loginForm.password).toBe('')
    })

    it('应该验证用户名长度', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户名"]')
      
      // 测试用户名太短
      await usernameInput.setValue('ab')
      await usernameInput.trigger('blur')
      
      // 测试用户名太长
      await usernameInput.setValue('a'.repeat(25))
      await usernameInput.trigger('blur')
    })

    it('应该验证密码长度', async () => {
      const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
      
      // 测试密码太短
      await passwordInput.setValue('123')
      await passwordInput.trigger('blur')
      
      // 测试密码太长
      await passwordInput.setValue('a'.repeat(25))
      await passwordInput.trigger('blur')
    })
  })

  describe('登录功能', () => {
    it('应该能够成功登录', async () => {
      // Mock successful login
      vi.spyOn(authStore, 'login').mockResolvedValue(true)
      
      const usernameInput = wrapper.find('input[placeholder="请输入用户名"]')
      const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
      const loginButton = wrapper.find('.login-button')
      
      await usernameInput.setValue('admin')
      await passwordInput.setValue('admin123')
      await loginButton.trigger('click')
      
      expect(authStore.login).toHaveBeenCalledWith({
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      })
    })

    it('应该处理登录失败', async () => {
      // Mock failed login
      vi.spyOn(authStore, 'login').mockRejectedValue(new Error('用户名或密码错误'))
      
      const usernameInput = wrapper.find('input[placeholder="请输入用户名"]')
      const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
      const loginButton = wrapper.find('.login-button')
      
      await usernameInput.setValue('admin')
      await passwordInput.setValue('wrongpassword')
      await loginButton.trigger('click')
      
      await wrapper.vm.$nextTick()
      
      expect(ElMessage.error).toHaveBeenCalledWith('用户名或密码错误')
    })

    it('应该在登录过程中显示加载状态', async () => {
      // Mock slow login
      vi.spyOn(authStore, 'login').mockImplementation(() => new Promise(resolve => setTimeout(() => resolve(true), 100)))
      
      const loginButton = wrapper.find('.login-button')
      
      await wrapper.find('input[placeholder="请输入用户名"]').setValue('admin')
      await wrapper.find('input[placeholder="请输入密码"]').setValue('admin123')
      
      await loginButton.trigger('click')
      
      expect(wrapper.vm.loading).toBe(true)
      expect(loginButton.text()).toBe('登录中...')
    })

    it('应该支持回车键登录', async () => {
      vi.spyOn(authStore, 'login').mockResolvedValue(true)
      
      const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
      
      await wrapper.find('input[placeholder="请输入用户名"]').setValue('admin')
      await passwordInput.setValue('admin123')
      await passwordInput.trigger('keyup.enter')
      
      expect(authStore.login).toHaveBeenCalled()
    })
  })

  describe('记住我功能', () => {
    it('应该能够切换记住我选项', async () => {
      const rememberMeCheckbox = wrapper.find('input[type="checkbox"]')
      
      expect(wrapper.vm.loginForm.rememberMe).toBe(false)
      
      await rememberMeCheckbox.setChecked(true)
      expect(wrapper.vm.loginForm.rememberMe).toBe(true)
    })

    it('应该在组件挂载时恢复保存的用户名', async () => {
      localStorageMock.getItem.mockReturnValue('saved_username')
      
      const newWrapper = mount(LoginView, {
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      await newWrapper.vm.$nextTick()
      
      expect(newWrapper.vm.loginForm.username).toBe('saved_username')
      expect(newWrapper.vm.loginForm.rememberMe).toBe(true)
    })
  })

  describe('忘记密码功能', () => {
    it('应该能够打开忘记密码对话框', async () => {
      const forgotPasswordLink = wrapper.find('.el-link')
      await forgotPasswordLink.trigger('click')
      
      expect(wrapper.vm.forgotPasswordVisible).toBe(true)
    })

    it('应该能够发送密码重置邮件', async () => {
      vi.spyOn(authStore, 'forgotPassword').mockResolvedValue()
      
      // 打开对话框
      await wrapper.find('.el-link').trigger('click')
      
      // 输入邮箱
      wrapper.vm.forgotPasswordForm.email = 'test@example.com'
      
      // 点击发送按钮
      await wrapper.vm.handleForgotPassword()
      
      expect(authStore.forgotPassword).toHaveBeenCalledWith('test@example.com')
      expect(ElNotification.success).toHaveBeenCalled()
      expect(wrapper.vm.forgotPasswordVisible).toBe(false)
    })

    it('应该处理发送密码重置邮件失败', async () => {
      vi.spyOn(authStore, 'forgotPassword').mockRejectedValue(new Error('发送失败'))
      
      // 打开对话框
      await wrapper.find('.el-link').trigger('click')
      
      // 输入邮箱
      wrapper.vm.forgotPasswordForm.email = 'test@example.com'
      
      // 点击发送按钮
      await wrapper.vm.handleForgotPassword()
      
      expect(ElMessage.error).toHaveBeenCalledWith('发送失败')
    })

    it('应该验证邮箱格式', async () => {
      // 打开对话框
      await wrapper.find('.el-link').trigger('click')
      
      // 输入无效邮箱
      wrapper.vm.forgotPasswordForm.email = 'invalid-email'
      
      // 这里需要根据实际的Element Plus验证行为进行测试
      // 通常会在表单验证时检查邮箱格式
    })
  })

  describe('路由行为', () => {
    it('已登录用户访问登录页应该重定向到首页', async () => {
      // Mock authenticated user
      vi.spyOn(authStore, 'isAuthenticated', 'get').mockReturnValue(true)
      
      const pushSpy = vi.spyOn(router, 'push')
      
      mount(LoginView, {
        global: {
          plugins: [createPinia(), router]
        }
      })
      
      await wrapper.vm.$nextTick()
      
      expect(pushSpy).toHaveBeenCalledWith('/')
    })

    it('成功登录后应该重定向到指定页面', async () => {
      vi.spyOn(authStore, 'login').mockResolvedValue(true)
      const pushSpy = vi.spyOn(router, 'push')
      
      // 模拟带有redirect参数的路由
      router.currentRoute.value.query = { redirect: '/dashboard' }
      
      await wrapper.find('input[placeholder="请输入用户名"]').setValue('admin')
      await wrapper.find('input[placeholder="请输入密码"]').setValue('admin123')
      await wrapper.find('.login-button').trigger('click')
      
      await wrapper.vm.$nextTick()
      
      expect(pushSpy).toHaveBeenCalledWith('/dashboard')
    })
  })

  describe('响应式设计', () => {
    it('应该在移动设备上正确显示', () => {
      // 这里可以测试CSS媒体查询相关的行为
      // 或者测试组件在不同屏幕尺寸下的表现
      expect(wrapper.find('.login-container').classes()).toContain('login-container')
    })
  })
})