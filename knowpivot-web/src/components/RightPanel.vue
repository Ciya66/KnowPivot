<script setup lang="ts">
defineEmits<{
  close: []
}>()

const steps = [
  { label: '解析问题语义', time: '0.2s', type: 'success' as const },
  { label: '检索相关文档', time: '0.8s', type: 'success' as const },
  { label: '综合分析生成', time: '进行中...', type: 'primary' as const },
]
</script>

<template>
  <aside class="right-panel">
    <div class="panel-header">
      <h3 class="panel-title">引用 & 推理链</h3>
      <button class="panel-close" @click="$emit('close')">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path d="M5 5L13 13M13 5L5 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <el-scrollbar class="panel-body">
      <!-- Reasoning Steps -->
      <div class="panel-section">
        <h4 class="section-title">
          <span class="section-icon accent">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
              <path d="M8 5V8.5L10.5 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          推理步骤
        </h4>
        <el-timeline class="reasoning-timeline">
          <el-timeline-item
            v-for="(step, i) in steps"
            :key="i"
            :type="step.type"
            :timestamp="step.time"
            placement="top"
          >
            <span class="step-label">{{ step.label }}</span>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- Referenced Docs -->
      <div class="panel-section">
        <h4 class="section-title">
          <span class="section-icon">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M3 3H8L10 5H13V13H3V3Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            </svg>
          </span>
          引用文档
        </h4>
        <div class="doc-list">
          <el-card
            v-for="i in 3"
            :key="i"
            shadow="hover"
            class="doc-item"
            body-style="padding: 12px; display: flex; align-items: center; gap: var(--space-3);"
          >
            <div class="doc-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M4 2H9L12 5V14H4V2Z" stroke="currentColor" stroke-width="1.2"/>
                <path d="M9 2V5H12" stroke="currentColor" stroke-width="1.2"/>
              </svg>
            </div>
            <div class="doc-info">
              <span class="doc-name">技术白皮书 v{{ i }}.0.pdf</span>
              <span class="doc-meta">相似度 {{ (98 - i * 3) }}%</span>
            </div>
          </el-card>
        </div>
      </div>
    </el-scrollbar>
  </aside>
</template>

<style scoped>
.right-panel {
  width: var(--right-panel-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-left: 1px solid var(--color-border);
  flex-shrink: 0;
  z-index: var(--z-panel);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  height: var(--header-height);
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.panel-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
}

.panel-close:hover {
  background: var(--color-surface-hover);
  color: var(--color-text-primary);
}

.panel-body {
  flex: 1;
  padding: var(--space-4);
}

.panel-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.panel-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.section-icon {
  display: flex;
  align-items: center;
  color: var(--color-text-tertiary);
}

.section-icon.accent {
  color: var(--color-accent);
}

/* Reasoning Timeline */
.reasoning-timeline {
  padding-left: var(--space-1);
}

.step-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

/* Document List */
.doc-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.doc-item {
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
}

.doc-item:hover {
  transform: translateY(-1px);
}

.doc-item :deep(.el-card__body) {
  padding: 12px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.doc-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  flex-shrink: 0;
}

.doc-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.doc-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-family: var(--font-mono);
}
</style>
