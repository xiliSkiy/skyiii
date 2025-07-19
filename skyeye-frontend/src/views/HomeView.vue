<script setup lang="ts">
import { ref } from 'vue'

const systemStats = ref([
  { label: '在线设备', value: 24, icon: 'VideoCamera', color: '#67C23A' },
  { label: '今日报警', value: 3, icon: 'Warning', color: '#E6A23C' },
  { label: '处理中事件', value: 1, icon: 'Bell', color: '#F56C6C' },
  { label: '系统状态', value: '正常', icon: 'CircleCheck', color: '#409EFF' },
])
</script>

<template>
  <div class="home">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用 SkyEye 智能监控系统</span>
        </div>
      </template>
      <p>
        这是一个基于人工智能技术的综合性安全监控平台，集成了视频监控、设备管理、智能分析、报警处理、数据分析等功能模块。
      </p>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in systemStats" :key="stat.label">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ color: stat.color }">
              <el-icon size="32">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统功能</span>
            </div>
          </template>
          <el-space direction="vertical" size="large" style="width: 100%">
            <el-button type="primary" size="large" style="width: 100%">
              <el-icon><VideoCamera /></el-icon>
              实时监控
            </el-button>
            <el-button type="success" size="large" style="width: 100%">
              <el-icon><Setting /></el-icon>
              设备管理
            </el-button>
            <el-button type="warning" size="large" style="width: 100%">
              <el-icon><Bell /></el-icon>
              报警中心
            </el-button>
            <el-button type="info" size="large" style="width: 100%">
              <el-icon><DataAnalysis /></el-icon>
              数据分析
            </el-button>
          </el-space>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="2024-01-19 10:30" color="#0bbd87">
              系统启动完成
            </el-timeline-item>
            <el-timeline-item timestamp="2024-01-19 10:25" color="#409eff">
              摄像头 CAM-001 上线
            </el-timeline-item>
            <el-timeline-item timestamp="2024-01-19 10:20" color="#e6a23c">
              检测到异常行为报警
            </el-timeline-item>
            <el-timeline-item timestamp="2024-01-19 10:15" color="#f56c6c">
              设备 DEV-003 离线
            </el-timeline-item>
          </el-timeline>
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
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

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.content-row {
  margin-bottom: 20px;
}
</style>
