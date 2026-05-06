<script setup lang="ts">
import type { ConvItem } from '@/types/api'

defineProps<{
  conversations: ConvItem[]
  currentId: string | null
  loading?: boolean
}>()

const emit = defineEmits<{
  select: [convId: string]
  create: []
  delete: [convId: string]
}>()

const truncate = (text: string, len = 18) => {
  return text.length > len ? text.slice(0, len) + '...' : text
}
</script>

<template>
  <aside class="conv-list">
    <div class="conv-header">
      <h3 class="conv-title">对话历史</h3>
      <button class="btn-new" @click="emit('create')" title="新建对话">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 3V13M3 8H13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <div class="conv-body">
      <div v-if="loading" class="conv-loading">
        <el-skeleton
          v-for="i in 4"
          :key="i"
          :rows="2"
          animated
          class="skeleton-item"
        />
      </div>

      <el-empty
        v-else-if="conversations.length === 0"
        description="暂无对话"
        :image-size="60"
        class="conv-empty"
      />

      <div
        v-for="conv in conversations"
        :key="conv.conversationId"
        class="conv-item-wrap"
      >
        <button
          class="conv-item"
          :class="{ active: conv.conversationId === currentId }"
          @click="emit('select', conv.conversationId)"
        >
          <span class="conv-item-title">{{ truncate(conv.title) }}</span>
          <span class="conv-item-time">{{ conv.createTime }}</span>
        </button>
        <el-popconfirm
          title="确定删除此对话？"
          @confirm="emit('delete', conv.conversationId)"
        >
          <template #reference>
            <el-button
              class="btn-delete"
              size="small"
              text
              @click.stop
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M3 4H11M5 4V3C5 2.45 5.45 2 6 2H8C8.55 2 9 2.45 9 3V4M10 4V12C10 12.55 9.55 13 9 13H5C4.45 13 4 12.55 4 12V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.conv-list {
  width: 240px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.conv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  height: 52px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.conv-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
}

.btn-new {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
}

.btn-new:hover {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.conv-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.conv-item-wrap {
  position: relative;
}

.conv-item-wrap:hover .btn-delete {
  opacity: 1;
}

.btn-delete {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  opacity: 0;
  transition: all var(--transition-fast);
}

.btn-delete:hover {
  background: var(--color-error-light);
  color: var(--color-error);
}

[data-theme='dark'] .btn-delete:hover {
  background: rgba(239, 68, 68, 0.15);
}

.conv-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  text-align: left;
  transition: all var(--transition-fast);
  min-height: 48px;
}

.conv-item:hover {
  background: var(--color-surface-hover);
}

.conv-item.active {
  background: var(--color-primary-bg);
}

.conv-item-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-item-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.conv-empty {
  padding: var(--space-8) var(--space-4);
}

.conv-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-3);
}

.skeleton-item {
  --el-skeleton-color: var(--color-bg-sunken);
  --el-skeleton-to-color: var(--color-border-light);
}
</style>
