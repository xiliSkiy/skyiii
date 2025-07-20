<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Monitor,
  VideoCamera,
  Setting,
  DataAnalysis,
  Bell,
  Operation,
  ArrowRight,
  Clock,
  TrendCharts,
  Warning,
  CircleCheck,
  User,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)

// 系统统计数据
const systemStats = ref([
  { label: '在线设备', value: 24, total: 28, icon: 'VideoCamera', color: '#67C23A', unit: '台' },
  { label: '今日报警', value: 3, total: 15, icon: 'Warning', color: '#E6A23C', unit: '条' },
  { label: '处理中事件', value: 1, total: 3, icon: 'Bell', color: '#F56C6C', unit: '个' },
  { label: '系统运行时间', value: '99.8%', icon: 'CircleCheck', color: '#409EFF', unit: '' },
])

// 最近活动数据
const recentActivities = ref([
  { 
    time: '2024-01-20 14:30:25', 
    type: 'info', 
    title: '设备上线', 
    content: '摄像头 CAM-001 重新连接成功',
    icon: 'VideoCamera'
  },
  { 
    time: '2024-01-20 14:25:18', 
    type: 'warning', 
    title: '异常检测', 
    content: '区域A检测到异常行为，已自动记录',
    icon: 'Warning'
  },
  { 
    time: '2024-01-20 14:20:45', 
    type: 'success', 
    title: '报警处理', 
    content: '火灾报警已确认为误报，状态已重置',
    icon: 'CircleCheck'
  },
  { 
    time: '2024-01-20 14:15:32', 
    type: 'error', 
    title: '设备离线', 
    content: '传感器 SEN-003 连接中断，请检查网络',
    icon: 'Warning'
  },
  { 
    time: '2024-01-20 14:10:15', 
    type: 'info', 
    title: '系统备份', 
    content: '定时数据备份任务执行完成',
    icon: 'Document'
  }
])

// 快捷操作
const quickActions = ref([
  { 
    title: '实时监控', 
    description: '查看所有设备实时状态', 
    icon: 'VideoCamera', 
    color: '#409EFF', 
    path: '/dashboard',
    permission: 'dashboard:read'
  },
  { 
    title: '设备管理', 
    description: '管理监控设备和传感器', 
    icon: 'Setting', 
    color: '#67C23A', 
    path: '/devices',
    permission: 'device:read'
  },
  { 
    title: '报警中心', 
    description: '处理系统报警和事件', 
    icon: 'Bell', 
    color: '#E6A23C', 
    path: '/alerts',
    permission: 'alert:read'
  },
  { 
    title: '用户管理', 
    description: '管理系统用户和权限', 
    icon: 'User', 
    color: '#F56C6C', 
    path: '/users',
    permission: 'user:read',
    roles: ['system_admin', 'security_admin']
  }
])

const navigateTo = (path: string) => {
  router.push(path)
}

// 检查用户是否有权限访问某个功能
const hasAccess = (action: any) => {
  if (action.permission && !authStore.hasPermission(action.permission)) {
    return false
  }
  if (action.roles && !authStore.hasAnyRole(action.roles)) {
    return false
  }
  return true
}

// 获取当前时间
const currentTime = ref('')
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
})
</script>

<template>
  <div class="home">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Monitor /></el-icon>
            <span>欢迎使用 SkyEye 智能监控系统</span>
          </div>
          <div class="header-right">
            <span class="current-time">{{ currentTime }}</span>
          </div>
        </div>
      </template>
      <div class="welcome-content">
        <p class="welcome-text">
          您好，<strong>{{ user?.fullName || user?.username }}</strong>！
          欢迎使用基于人工智能技术的综合性安全监控平台。
        </p>
        <div class="system-overview">
          <span class="overview-item">
            <el-icon><VideoCamera /></el-icon>
            视频监控
          </span>
          <span class="overview-item">
            <el-icon><Setting /></el-icon>
            设备管理
          </span>
          <span class="overview-item">
            <el-icon><DataAnalysis /></el-icon>
            智能分析
          </span>
          <span class="overview-item">
            <el-icon><Bell /></el-icon>
            报警处理
          </span>
        </div>
      </div>
    </el-card>

    <!-- 系统统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in systemStats" :key="stat.label">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ color: stat.color }">
              <el-icon size="36">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stat.value }}
                <span v-if="stat.unit" class="stat-unit">{{ stat.unit }}</span>
              </div>
              <div class="stat-label">{{ stat.label }}</div>
              <div v-if="stat.total" class="stat-progress">
                <el-progress 
                  :percentage="Math.round((stat.value / stat.total) * 100)" 
                  :stroke-width="4"
                  :show-text="false"
                  :color="stat.color"
                />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="content-row">
      <!-- 快捷操作 -->
      <el-col :span="16">
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <el-icon><Operation /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions-grid">
            <div 
              v-for="action in quickActions" 
              :key="action.title"
              v-show="hasAccess(action)"
              class="action-item"
              @click="navigateTo(action.path)"
            >
              <div class="action-icon" :style="{ backgroundColor: action.color }">
                <el-icon size="24">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <div class="action-content">
                <h4 class="action-title">{{ action.title }}</h4>
                <p class="action-description">{{ action.description }}</p>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近活动 -->
      <el-col :span="8">
        <el-card class="activities-card">
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>最近活动</span>
            </div>
          </template>
          <div class="activities-list">
            <div 
              v-for="activity in recentActivities" 
              :key="activity.time"
              class="activity-item"
              :class="`activity-${activity.type}`"
            >
              <div class="activity-icon">
                <el-icon>
                  <component :is="activity.icon" />
                </el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-description">{{ activity.content }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统状态 -->
    <el-row :gutter="20" class="status-row">
      <el-col :span="24">
        <el-card class="system-status-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>系统状态监控</span>
            </div>
          </template>
          <div class="status-indicators">
            <div class="status-item">
              <div class="status-label">CPU使用率</div>
              <el-progress :percentage="45" color="#409EFF" />
            </div>
            <div class="status-item">
              <div class="status-label">内存使用率</div>
              <el-progress :percentage="62" color="#67C23A" />
            </div>
            <div class="status-item">
              <div class="status-label">磁盘使用率</div>
              <el-progress :percentage="38" color="#E6A23C" />
            </div>
            <div class="status-item">
              <div class="status-label">网络带宽</div>
              <el-progress :percentage="25" color="#F56C6C" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #409eff;
}

.header-right {
  font-size: 14px;
  color: #666;
  font-weight: normal;
}

.current-time {
  font-family: 'Courier New', monospace;
  background: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
}

.welcome-content {
  padding: 20px 0;
}

.welcome-text {
  font-size: 16px;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.6;
}

.system-overview {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.overview-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 20px;
  border: 1px solid #e9ecef;
}

.overview-item .el-icon {
  margin-right: 6px;
  font-size: 16px;
  color: #409eff;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-info {
  text-align: left;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-progress {
  margin-top: 8px;
}

.content-row {
  margin-bottom: 20px;
}

.quick-actions-card {
  height: 100%;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.action-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.action-description {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.action-arrow {
  color: #c0c4cc;
  font-size: 16px;
  transition: color 0.3s;
}

.action-item:hover .action-arrow {
  color: #409eff;
}

.activities-card {
  height: 100%;
}

.activities-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.activity-info .el-icon {
  font-size: 16px;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.activity-description {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.activity-info {
  background: #f0f9ff;
  color: #0369a1;
}

.activity-warning {
  background: #fef3c7;
  color: #d97706;
}

.activity-success {
  background: #dcfce7;
  color: #16a34a;
}

.activity-error {
  background: #fee2e2;
  color: #dc2626;
}

.status-row {
  margin-bottom: 20px;
}

.system-status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.system-status-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.system-status-card :deep(.el-card__body) {
  background: transparent;
}

.system-status-card .card-header {
  color: white;
}

.system-status-card .card-header .el-icon {
  color: rgba(255, 255, 255, 0.9);
}

.status-indicators {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.status-item {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.status-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
  font-weight: 500;
}

.status-item :deep(.el-progress-bar__outer) {
  background-color: rgba(255, 255, 255, 0.2);
}

/* PC端专用优化 */
@media (min-width: 1200px) {
  .home {
    max-width: 1400px;
    margin: 0 auto;
  }
  
  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .status-indicators {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 1600px) {
  .home {
    max-width: 1600px;
  }
  
  .system-overview {
    gap: 40px;
  }
  
  .overview-item {
    padding: 10px 20px;
    font-size: 15px;
  }
}

/* 响应式适配 */
@media (max-width: 1199px) {
  .content-row .el-col:first-child {
    margin-bottom: 20px;
  }
  
  .system-overview {
    gap: 15px;
  }
  
  .overview-item {
    font-size: 13px;
    padding: 6px 12px;
  }
}

@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 10px;
  }
  
  .stat-content {
    flex-direction: column;
    gap: 8px;
  }
  
  .stat-info {
    text-align: center;
  }
  
  .system-overview {
    flex-direction: column;
    gap: 10px;
  }
  
  .status-indicators {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
