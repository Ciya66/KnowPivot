<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  router.push({ name: 'login' })
}

const user = computed(() => authStore.userInfo)
const quotaPercent = computed(() => {
  if (!user.value) return 0
  return Math.min((user.value.tokenQuota / 10000) * 100, 100)
})
</script>

<template>
  <div class="view-container">
    <header class="view-header">
      <div class="header-left">
        <h1 class="page-title">设置</h1>
        <span class="page-subtitle">系统配置与偏好管理</span>
      </div>
    </header>

    <div class="view-body">
      <!-- User Info -->
      <div class="settings-section">
        <h2 class="section-heading">账户信息</h2>
        <div class="user-card" v-if="user">
          <div class="user-avatar-wrap">
            <el-avatar :size="48" class="user-avatar-el">
              {{ user.nickname?.charAt(0) || user.username.charAt(0) }}
            </el-avatar>
          </div>
          <div class="user-info">
            <span class="user-name">{{ user.nickname || user.username }}</span>
            <span class="user-username">@{{ user.username }}</span>
          </div>
          <el-tag
            :type="user.role === 1 ? 'warning' : 'primary'"
            effect="dark"
            size="small"
          >
            {{ user.role === 1 ? '管理员' : '用户' }}
          </el-tag>
        </div>

        <!-- Token Quota -->
        <div class="setting-row" v-if="user">
          <div class="setting-info">
            <span class="setting-label">Token 配额</span>
            <span class="setting-desc">
              剩余 <strong class="quota-value">{{ user.tokenQuota.toLocaleString() }}</strong> tokens
            </span>
          </div>
          <el-progress
            :percentage="quotaPercent"
            :color="quotaPercent < 20 ? '#EF4444' : '#3B82F6'"
            :stroke-width="6"
            :show-text="false"
            style="width: 120px;"
          />
        </div>
      </div>

      <!-- Appearance -->
      <div class="settings-section">
        <h2 class="section-heading">外观</h2>
        <div class="setting-row">
          <div class="setting-info">
            <span class="setting-label">主题模式</span>
            <span class="setting-desc">切换浅色 / 深色主题</span>
          </div>
          <el-switch
            :model-value="appStore.theme === 'dark'"
            active-text="深色"
            inactive-text="浅色"
            @change="appStore.toggleTheme()"
          />
        </div>
      </div>

      <!-- Security -->
      <div class="settings-section">
        <h2 class="section-heading">安全</h2>
        <div class="setting-row">
          <div class="setting-info">
            <span class="setting-label">数据加密</span>
            <span class="setting-desc">所有知识库文档采用 AES-256 端到端加密</span>
          </div>
          <el-tag type="success" effect="light" size="small">已启用</el-tag>
        </div>
        <div class="setting-row">
          <div class="setting-info">
            <span class="setting-label">访问权限</span>
            <span class="setting-desc">基于角色的细粒度访问控制</span>
          </div>
          <el-tag type="success" effect="light" size="small">已启用</el-tag>
        </div>
      </div>

      <!-- Logout -->
      <div class="settings-section">
        <el-button
          type="danger"
          plain
          style="width: 100%; height: 44px;"
          @click="handleLogout"
        >
          退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-body {
  max-width: 720px;
}

.settings-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.section-heading {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: var(--space-2);
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) 0;
  border-bottom: 1px solid var(--color-border-light);
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.setting-label {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.setting-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

.quota-value {
  color: var(--color-primary);
  font-family: var(--font-mono);
}

/* User Card */
.user-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
}

.user-avatar-wrap {
  flex-shrink: 0;
}

.user-avatar-el {
  background: var(--color-primary);
  color: white;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.user-username {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  font-family: var(--font-mono);
}
</style>
