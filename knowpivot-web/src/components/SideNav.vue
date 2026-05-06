<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isCollapsed = ref(false)

interface NavItem {
  key: string
  label: string
  icon: string
  route: string
}

const navItems: NavItem[] = [
  { key: 'chat', label: '对话', icon: 'chat', route: '/' },
  { key: 'knowledge', label: '知识库', icon: 'knowledge', route: '/knowledge' },
  { key: 'settings', label: '设置', icon: 'settings', route: '/settings' },
]

const activeKey = computed(() => {
  const match = navItems.find((item) =>
    item.route === '/' ? route.path === '/' : route.path.startsWith(item.route),
  )
  return match?.key ?? 'chat'
})

const navigate = (item: NavItem) => {
  router.push(item.route)
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <!-- Logo -->
    <div class="sidebar-logo" @click="navigate(navItems[0]!)">
      <div class="logo-icon">
        <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
          <rect width="28" height="28" rx="8" fill="var(--color-primary)" />
          <path d="M8 14L12 18L20 10" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <Transition name="fade">
        <span v-if="!isCollapsed" class="logo-text">KnowPivot</span>
      </Transition>
    </div>

    <!-- Navigation -->
    <nav class="sidebar-nav">
      <button
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeKey === item.key }"
        @click="navigate(item)"
        :title="isCollapsed ? item.label : undefined"
      >
        <span class="nav-icon" v-html="getIcon(item.icon)" />
        <Transition name="fade">
          <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
        </Transition>
        <span v-if="activeKey === item.key" class="nav-indicator" />
      </button>
    </nav>

    <!-- Bottom: Collapse Toggle -->
    <div class="sidebar-footer">
      <button class="nav-item" @click="toggleCollapse" :title="isCollapsed ? '展开' : '收起'">
        <span class="nav-icon" v-html="isCollapsed ? getIcon('expand') : getIcon('collapse')" />
        <Transition name="fade">
          <span v-if="!isCollapsed" class="nav-label">收起</span>
        </Transition>
      </button>
    </div>
  </aside>
</template>

<script lang="ts">
function getIcon(name: string): string {
  const icons: Record<string, string> = {
    chat: `<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M3 4H17V13H7L3 17V4Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M7 7.5H13M7 10H10" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>`,
    knowledge: `<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M3 4C3 3.45 3.45 3 4 3H10L12 5H16C16.55 5 17 5.45 17 6V16C17 16.55 16.55 17 16 17H4C3.45 17 3 16.55 3 16V4Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M7 10H13M7 13H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>`,
    settings: `<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.5"/><path d="M10 2V4M10 16V18M18 10H16M4 10H2M15.66 4.34L14.24 5.76M5.76 14.24L4.34 15.66M15.66 15.66L14.24 14.24M5.76 5.76L4.34 4.34" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>`,
    collapse: `<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M13 4L7 10L13 16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
    expand: `<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M7 4L13 10L7 16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  }
  return icons[name] ?? ''
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
  transition: width var(--transition-slow);
  z-index: var(--z-sidebar);
  flex-shrink: 0;
  overflow: hidden;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-4);
  height: var(--header-height);
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  white-space: nowrap;
  letter-spacing: -0.3px;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-2);
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  white-space: nowrap;
  height: 40px;
}

.nav-item:hover {
  background: var(--color-surface-hover);
  color: var(--color-text-primary);
}

.nav-item.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.nav-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.nav-label {
  white-space: nowrap;
}

.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--color-primary);
  border-radius: var(--radius-full);
}

/* Footer */
.sidebar-footer {
  padding: var(--space-2);
  border-top: 1px solid var(--color-border-light);
  flex-shrink: 0;
}
</style>
