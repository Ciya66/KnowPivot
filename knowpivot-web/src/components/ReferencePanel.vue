<script setup lang="ts">
import type { SSEReferenceSource } from '@/types/api'

defineProps<{
  sources: SSEReferenceSource[]
}>()

defineEmits<{
  close: []
}>()
</script>

<template>
  <aside class="ref-panel">
    <div class="ref-panel-header">
      <h3 class="ref-panel-title">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 3H7L9 5H11V11H3V3Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
        引用来源
      </h3>
      <button class="close-btn" @click="$emit('close')">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M4 4L12 12M12 4L4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
      </button>
    </div>

    <div class="ref-panel-body">
      <el-empty
        v-if="sources.length === 0"
        description="暂无引用来源"
        :image-size="60"
        class="ref-empty"
      />

      <el-card
        v-for="(source, i) in sources"
        :key="i"
        shadow="never"
        class="source-card"
        body-style="padding: var(--space-3); display: flex; flex-direction: column; gap: var(--space-2);"
      >
        <div class="source-header">
          <el-tag type="primary" size="small" effect="dark" round>{{ i + 1 }}</el-tag>
          <span class="source-name">{{ source.docName }}</span>
          <span v-if="source.pageNum !== undefined" class="source-page">第 {{ source.pageNum }} 页</span>
        </div>
        <p class="source-content">{{ source.content }}</p>
      </el-card>
    </div>
  </aside>
</template>

<style scoped>
.ref-panel {
  width: var(--right-panel-width);
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-left: 1px solid var(--color-border);
  flex-shrink: 0;
}

.ref-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  height: 52px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.ref-panel-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
}

.close-btn {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
}

.close-btn:hover {
  background: var(--color-surface-hover);
  color: var(--color-text-primary);
}

.ref-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.ref-empty {
  padding: var(--space-8) 0;
}

.source-card {
  background: var(--color-bg-sunken);
  border-radius: var(--radius-md);
  border: none;
}

.source-card :deep(.el-card__body) {
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.source-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.source-name {
  flex: 1;
  min-width: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-page {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  flex-shrink: 0;
}

.source-content {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  line-height: var(--line-height-relaxed);
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}
</style>
