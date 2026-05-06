<script setup lang="ts">
import { computed } from 'vue'
import { renderMarkdown } from '@/composables/useMarkdown'
import type { MessageItem, SSEReferenceSource } from '@/types/api'

const props = defineProps<{
  message: MessageItem
  streaming?: boolean
  streamingContent?: string
  references?: SSEReferenceSource[]
}>()

const isUser = computed(() => props.message.role === 'user')
const isAssistant = computed(() => props.message.role === 'assistant')

const renderedContent = computed(() => {
  const content = props.streaming ? props.streamingContent ?? '' : props.message.content
  return renderMarkdown(content)
})

const showReferences = computed(() => {
  if (props.streaming && props.references?.length) return true
  return isAssistant.value && (props.message.references?.length ?? 0) > 0
})

const displayReferences = computed(() => {
  if (props.streaming) return props.references ?? []
  return (props.message.references ?? []).map((r) => ({
    docName: r.docName,
    segmentId: r.docId,
    content: `第 ${r.pageNum} 页`,
  }))
})
</script>

<template>
  <div class="message" :class="{ user: isUser, assistant: isAssistant }">
    <!-- User Avatar -->
    <el-avatar v-if="isUser" :size="32" class="user-avatar-el">
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
        <circle cx="7" cy="5" r="2.5" stroke="currentColor" stroke-width="1.3"/>
        <path d="M3 12C3 9.79 4.79 8 7 8C9.21 8 11 9.79 11 12" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
      </svg>
    </el-avatar>

    <!-- AI Avatar -->
    <el-avatar v-if="isAssistant" :size="32" class="ai-avatar-el">
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
        <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.2"/>
        <path d="M4.5 7L6.5 9L10 5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </el-avatar>

    <!-- Bubble -->
    <div class="bubble" :class="{ 'user-bubble': isUser, 'ai-bubble': isAssistant }">
      <div v-if="isUser" class="user-text">{{ message.content }}</div>
      <div
        v-else
        class="ai-text markdown-body"
        :class="{ 'typing-cursor': streaming }"
        v-html="renderedContent"
      />

      <!-- References -->
      <div v-if="showReferences" class="references">
        <div class="ref-header">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 3H7L9 5H11V11H3V3Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
          引用来源
        </div>
        <div v-for="(ref, i) in displayReferences" :key="i" class="ref-item">
          <span class="ref-name">{{ ref.docName }}</span>
          <span class="ref-segment">{{ ref.content }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
  max-width: 85%;
}

.message.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.user-avatar-el {
  background: var(--color-primary);
  color: white;
  flex-shrink: 0;
}

.ai-avatar-el {
  background: var(--color-accent-bg);
  color: var(--color-accent);
  flex-shrink: 0;
}

.bubble {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  min-width: 0;
}

.user-bubble {
  background: var(--color-primary);
  color: white;
  border-bottom-right-radius: var(--radius-sm);
}

.ai-bubble {
  background: var(--color-bg-sunken);
  color: var(--color-text-primary);
  border-bottom-left-radius: var(--radius-sm);
  max-width: 100%;
}

.user-text {
  font-size: var(--font-size-base);
  line-height: var(--line-height-relaxed);
  white-space: pre-wrap;
}

.ai-text {
  font-size: var(--font-size-base);
  line-height: var(--line-height-relaxed);
}

/* Markdown body styles */
.ai-text :deep(p) { margin-bottom: var(--space-2); }
.ai-text :deep(p:last-child) { margin-bottom: 0; }
.ai-text :deep(ul),
.ai-text :deep(ol) { padding-left: var(--space-5); margin: var(--space-2) 0; }
.ai-text :deep(li) { margin-bottom: var(--space-1); }
.ai-text :deep(strong) { color: var(--color-primary); font-weight: var(--font-weight-semibold); }
.ai-text :deep(code) {
  font-family: var(--font-mono);
  font-size: 0.9em;
  background: var(--color-bg-sunken);
  padding: 1px var(--space-1);
  border-radius: var(--radius-sm);
}
.ai-text :deep(pre) {
  background: var(--color-bg-sunken);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: var(--space-2) 0;
}
.ai-text :deep(pre code) {
  background: none;
  padding: 0;
}
.ai-text :deep(blockquote) {
  border-left: 3px solid var(--color-primary);
  padding-left: var(--space-3);
  color: var(--color-text-secondary);
  margin: var(--space-2) 0;
}

/* References */
.references {
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.ref-header {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.ref-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
}

.ref-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
}

.ref-segment {
  color: var(--color-text-tertiary);
}

[data-theme='dark'] .ai-bubble {
  background: var(--color-bg-elevated);
}
</style>
