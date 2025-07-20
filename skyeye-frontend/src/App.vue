<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import UserInfo from '@/components/UserInfo.vue'
import {
  Monitor,
  House,
  InfoFilled,
  VideoCamera,
  Bell,
  User,
  Setting,
  DataAnalysis,
  Loading
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const isLoginPage = computed(() => route.name === 'login')
const showLayout = computed(() => isAuthenticated.value && !isLoginPage.value)
</script>

<template>
  <div id="app">
    <!-- 登录页面 -->
    <template v-if="isLoginPage">
      <RouterView />
    </template>

    <!-- 主应用布局 -->
    <template v-else-if="showLayout">
      <el-container class="layout-container">
        <el-header class="header">
          <div class="header-content">
            <div class="logo">
              <el-icon><Monitor /></el-icon>
              <span class="logo-text">SkyEye 智能监控系统</span>
            </div>
            <div class="header-actions">
              <UserInfo />
            </div>
          </div>
        </el-header>

        <el-container>
          <el-aside width="240px" class="sidebar">
            <div class="sidebar-header">
              <div class="sidebar-title">
                <el-icon><Monitor /></el-icon>
                <span>监控中心</span>
              </div>
            </div>
            
            <el-menu 
              :default-active="route.path" 
              class="sidebar-menu" 
              router
              unique-opened
            >
              <el-menu-item index="/">
                <el-icon><House /></el-icon>
                <span>系统首页</span>
              </el-menu-item>
              
              <el-sub-menu index="monitoring">
                <template #title>
                  <el-icon><VideoCamera /></el-icon>
                  <span>监控管理</span>
                </template>
                <el-menu-item index="/dashboard">
                  <el-icon><DataAnalysis /></el-icon>
                  <span>监控仪表板</span>
                </el-menu-item>
                <el-menu-item index="/devices">
                  <el-icon><VideoCamera /></el-icon>
                  <span>设备管理</span>
                </el-menu-item>
                <el-menu-item index="/alerts">
                  <el-icon><Bell /></el-icon>
                  <span>报警管理</span>
                </el-menu-item>
              </el-sub-menu>
              
              <el-sub-menu 
                v-if="authStore.hasAnyRole(['system_admin', 'security_admin'])"
                index="system"
              >
                <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>系统管理</span>
                </template>
                <el-menu-item 
                  v-if="authStore.hasAnyRole(['system_admin', 'security_admin'])"
                  index="/users"
                >
                  <el-icon><User /></el-icon>
                  <span>用户管理</span>
                </el-menu-item>
                <el-menu-item 
                  v-if="authStore.hasRole('system_admin')"
                  index="/settings"
                >
                  <el-icon><Setting /></el-icon>
                  <span>系统设置</span>
                </el-menu-item>
              </el-sub-menu>
              
              <el-menu-item index="/about">
                <el-icon><InfoFilled /></el-icon>
                <span>关于系统</span>
              </el-menu-item>
            </el-menu>
          </el-aside>

          <el-main class="main-content">
            <RouterView />
          </el-main>
        </el-container>
      </el-container>
    </template>

    <!-- 未认证状态 -->
    <template v-else>
      <div class="loading-container">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <p>正在加载...</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
#app {
  height: 100vh;
}

.layout-container {
  height: 100%;
}

.header {
  background-color: #001529;
  color: white;
  padding: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.logo .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.logo-text {
  color: white;
}

.sidebar {
  background-color: #f5f5f5;
  border-right: 1px solid #e6e6e6;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e6e6e6;
  background-color: #fafafa;
}

.sidebar-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.sidebar-title .el-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #409eff;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
  background-color: #f5f5f5;
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #1890ff;
  color: white;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
  font-weight: 500;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.main-content {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.loading-icon {
  font-size: 32px;
  color: #409eff;
  animation: rotate 2s linear infinite;
  margin-bottom: 16px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
