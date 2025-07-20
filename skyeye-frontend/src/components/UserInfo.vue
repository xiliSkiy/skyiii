<template>
  <div class="user-info">
    <!-- 用户头像和基本信息 -->
    <el-dropdown trigger="click" @command="handleCommand">
      <div class="user-avatar-container">
        <el-avatar
          :size="size"
          :src="user?.avatar"
          :icon="UserFilled"
          class="user-avatar"
        />
        <div v-if="showDetails" class="user-details">
          <div class="user-name">{{ user?.fullName || user?.username }}</div>
          <div class="user-role">{{ getRoleDisplayName(user?.role) }}</div>
        </div>
        <el-icon v-if="showDropdown" class="dropdown-icon">
          <ArrowDown />
        </el-icon>
      </div>
      
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">
            <el-icon><User /></el-icon>
            个人资料
          </el-dropdown-item>
          <el-dropdown-item command="settings" :disabled="!canAccessSettings">
            <el-icon><Setting /></el-icon>
            系统设置
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 用户状态指示器 -->
    <div v-if="showStatus" class="user-status">
      <el-badge
        :type="getStatusBadgeType(user?.status)"
        :value="getStatusDisplayName(user?.status)"
        class="status-badge"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  User,
  Setting,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'
import { useAuthStore, UserRole, UserStatus } from '@/stores/auth'

interface Props {
  size?: number | 'large' | 'default' | 'small'
  showDetails?: boolean
  showDropdown?: boolean
  showStatus?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  showDetails: true,
  showDropdown: true,
  showStatus: false
})

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const user = computed(() => authStore.user)

const canAccessSettings = computed(() => {
  return authStore.hasRole(UserRole.SYSTEM_ADMIN)
})

// 获取角色显示名称
const getRoleDisplayName = (role?: UserRole): string => {
  const roleMap = {
    [UserRole.SYSTEM_ADMIN]: '系统管理员',
    [UserRole.SECURITY_ADMIN]: '安全管理员',
    [UserRole.OPERATOR]: '操作员',
    [UserRole.VIEWER]: '查看者'
  }
  return role ? roleMap[role] : '未知角色'
}

// 获取状态显示名称
const getStatusDisplayName = (status?: UserStatus): string => {
  const statusMap = {
    [UserStatus.ACTIVE]: '在线',
    [UserStatus.INACTIVE]: '离线',
    [UserStatus.LOCKED]: '锁定'
  }
  return status ? statusMap[status] : '未知'
}

// 获取状态徽章类型
const getStatusBadgeType = (status?: UserStatus): 'success' | 'warning' | 'danger' | 'info' => {
  const typeMap = {
    [UserStatus.ACTIVE]: 'success' as const,
    [UserStatus.INACTIVE]: 'info' as const,
    [UserStatus.LOCKED]: 'danger' as const
  }
  return status ? typeMap[status] : 'info'
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      await router.push('/profile')
      break
      
    case 'settings':
      if (canAccessSettings.value) {
        await router.push('/settings')
      } else {
        ElMessage.warning('您没有访问系统设置的权限')
      }
      break
      
    case 'logout':
      await handleLogout()
      break
      
    default:
      console.warn('Unknown command:', command)
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await authStore.logout()
    ElMessage.success('已退出登录')
    await router.push('/login')
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      console.error('Logout error:', error)
      ElMessage.error('退出登录失败')
    }
  }
}
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-container {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-avatar-container:hover {
  background-color: var(--el-fill-color-light);
}

.user-avatar {
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-icon {
  font-size: 12px;
  color: var(--el-text-color-regular);
  transition: transform 0.2s;
}

.user-avatar-container:hover .dropdown-icon {
  transform: rotate(180deg);
}

.user-status {
  flex-shrink: 0;
}

.status-badge {
  --el-badge-size: 18px;
  --el-badge-padding: 4px 6px;
  --el-badge-font-size: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-details {
    display: none;
  }
  
  .user-avatar-container {
    padding: 4px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .user-avatar-container:hover {
    background-color: var(--el-fill-color-darker);
  }
}
</style>