<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-background">
      <div class="bg-pattern"></div>
      <div class="bg-overlay"></div>
    </div>
    
    <div class="login-content">
      <!-- 左侧系统介绍 -->
      <div class="system-intro">
        <div class="intro-content">
          <div class="system-logo">
            <img src="/favicon.ico" alt="SkyEye" class="logo-icon" />
            <div class="logo-text">
              <h1>SkyEye</h1>
              <p>智能监控系统</p>
            </div>
          </div>
          
          <div class="system-features">
            <h2>专业监控解决方案</h2>
            <ul class="feature-list">
              <li>
                <el-icon><VideoCamera /></el-icon>
                <span>实时视频监控与录像回放</span>
              </li>
              <li>
                <el-icon><Monitor /></el-icon>
                <span>多屏幕显示与集中管控</span>
              </li>
              <li>
                <el-icon><Bell /></el-icon>
                <span>智能报警与事件处理</span>
              </li>
              <li>
                <el-icon><DataAnalysis /></el-icon>
                <span>数据分析与统计报表</span>
              </li>
              <li>
                <el-icon><Shield /></el-icon>
                <span>权限管理与安全防护</span>
              </li>
            </ul>
          </div>
          
          <div class="system-info">
            <p class="version">Version 1.0.0</p>
            <p class="copyright">&copy; 2024 SkyEye 智能监控系统</p>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-panel">
        <div class="login-card">
          <div class="login-header">
            <h2 class="login-title">系统登录</h2>
            <p class="login-subtitle">请输入您的账号信息</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <div class="login-options">
                <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
                <el-link type="primary" @click="showForgotPassword">忘记密码？</el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录系统' }}
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 测试账号提示 -->
          <div class="test-accounts">
            <el-divider content-position="center">测试账号</el-divider>
            <div class="account-tips">
              <div class="account-item">
                <span class="role">管理员：</span>
                <span class="credentials">admin / admin123</span>
              </div>
              <div class="account-item">
                <span class="role">操作员：</span>
                <span class="credentials">operator / operator123</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPasswordVisible"
      title="忘记密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="forgotPasswordRules"
      >
        <el-form-item label="邮箱地址" prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="forgotPasswordVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="forgotPasswordLoading"
            @click="handleForgotPassword"
          >
            发送重置邮件
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification, type FormInstance, type FormRules } from 'element-plus'
import { 
  User, 
  Lock, 
  Message, 
  VideoCamera, 
  Monitor, 
  Bell, 
  DataAnalysis, 
  Lock as Shield 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

interface LoginForm {
  username: string
  password: string
  rememberMe: boolean
}

interface ForgotPasswordForm {
  email: string
}

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref<FormInstance>()
const forgotPasswordFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  rememberMe: false
})

// 忘记密码表单数据
const forgotPasswordForm = reactive<ForgotPasswordForm>({
  email: ''
})

// 状态管理
const loading = ref(false)
const forgotPasswordVisible = ref(false)
const forgotPasswordLoading = ref(false)

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ]
}

const forgotPasswordRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    const success = await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })

    if (success) {
      ElMessage.success('登录成功，欢迎使用SkyEye智能监控系统')
      
      // 获取重定向路径或默认跳转到首页
      const redirect = router.currentRoute.value.query.redirect as string
      await router.push(redirect || '/')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 显示忘记密码对话框
const showForgotPassword = () => {
  forgotPasswordVisible.value = true
  forgotPasswordForm.email = ''
}

// 处理忘记密码
const handleForgotPassword = async () => {
  if (!forgotPasswordFormRef.value) return

  try {
    const valid = await forgotPasswordFormRef.value.validate()
    if (!valid) return

    forgotPasswordLoading.value = true

    await authStore.forgotPassword(forgotPasswordForm.email)
    
    ElNotification.success({
      title: '邮件发送成功',
      message: '密码重置邮件已发送到您的邮箱，请查收并按照邮件指引重置密码。'
    })
    
    forgotPasswordVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '发送重置邮件失败')
  } finally {
    forgotPasswordLoading.value = false
  }
}

// 组件挂载时检查是否已登录
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/')
  }
  
  // 如果启用了记住我功能，尝试从本地存储恢复用户名
  const savedUsername = localStorage.getItem('skyeye_remembered_username')
  if (savedUsername) {
    loginForm.username = savedUsername
    loginForm.rememberMe = true
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  background-size: 100px 100px;
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.1);
}

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100vh;
  max-width: 1400px;
  margin: 0 auto;
}

.system-intro {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: white;
}

.intro-content {
  max-width: 500px;
}

.system-logo {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 60px;
  height: 60px;
  margin-right: 20px;
}

.logo-text h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0;
  color: white;
}

.logo-text p {
  font-size: 18px;
  margin: 5px 0 0 0;
  color: rgba(255, 255, 255, 0.8);
}

.system-features h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
  color: white;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.feature-list li .el-icon {
  margin-right: 15px;
  font-size: 20px;
  color: #64b5f6;
}

.system-info {
  margin-top: 60px;
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.system-info p {
  margin: 5px 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.login-panel {
  flex: 0 0 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.login-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 30px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

.test-accounts {
  margin-top: 30px;
}

.account-tips {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.account-item:last-child {
  margin-bottom: 0;
}

.role {
  color: #666;
  font-weight: 500;
}

.credentials {
  color: #409eff;
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

/* PC端专用样式 */
@media (min-width: 1024px) {
  .login-content {
    padding: 0 40px;
  }
  
  .system-intro {
    padding: 80px 60px;
  }
  
  .login-panel {
    padding: 60px;
  }
}

/* 大屏幕优化 */
@media (min-width: 1440px) {
  .login-content {
    max-width: 1600px;
  }
  
  .system-intro {
    padding: 100px 80px;
  }
  
  .intro-content {
    max-width: 600px;
  }
  
  .login-panel {
    flex: 0 0 520px;
  }
}

/* 平板和小屏幕适配 */
@media (max-width: 1023px) {
  .login-content {
    flex-direction: column;
  }
  
  .system-intro {
    flex: none;
    padding: 40px 20px;
    text-align: center;
  }
  
  .login-panel {
    flex: none;
    padding: 20px;
    background: white;
  }
  
  .login-card {
    box-shadow: none;
    padding: 20px;
  }
}
</style>