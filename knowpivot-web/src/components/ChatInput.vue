<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

defineProps<{
  disabled?: boolean
  streaming?: boolean
}>()

const emit = defineEmits<{
  send: [content: string]
  stop: []
}>()

const input = ref('')
const inputRef = ref<InstanceType<typeof import('element-plus')['ElInput']> | null>(null)
let textareaEl: HTMLTextAreaElement | null = null

const handleSend = () => {
  const text = input.value.trim()
  if (!text) return
  emit('send', text)
  input.value = ''
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

onMounted(async () => {
  await nextTick()
  if (inputRef.value) {
    textareaEl = inputRef.value.$el.querySelector('textarea')
    textareaEl?.addEventListener('keydown', handleKeydown)
  }
})

onBeforeUnmount(() => {
  textareaEl?.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="chat-input-bar">
    <el-input
      ref="inputRef"
      v-model="input"
      type="textarea"
      :rows="1"
      :autosize="{ minRows: 1, maxRows: 4 }"
      resize="none"
      :disabled="disabled"
      placeholder="输入你的问题，AI 将从知识库中检索回答…"
      class="chat-textarea"
    />
    <el-button
      v-if="streaming"
      circle
      type="danger"
      class="btn-stop"
      title="停止生成"
      @click="emit('stop')"
    >
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <rect x="4" y="4" width="8" height="8" rx="1.5" fill="currentColor"/>
      </svg>
    </el-button>
    <el-button
      v-else
      circle
      type="primary"
      class="btn-send"
      :disabled="!input.trim() || disabled"
      title="发送"
      @click="handleSend"
    >
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M2 8L14 2L8 14L7 9L2 8Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
      </svg>
    </el-button>
  </div>
</template>

<style scoped>
.chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border-light);
  background: var(--color-surface);
  flex-shrink: 0;
}

.chat-textarea {
  flex: 1;
}

.chat-textarea :deep(.el-textarea__inner) {
  min-height: 40px !important;
  max-height: 120px;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-family: var(--font-sans);
  line-height: var(--line-height-relaxed);
  box-shadow: none;
}

.chat-textarea :deep(.el-textarea__inner::placeholder) {
  color: var(--color-text-tertiary);
}

.chat-textarea :deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-bg);
}

.chat-textarea :deep(.el-textarea__inner:disabled) {
  opacity: 0.6;
  cursor: not-allowed;
  -webkit-text-fill-color: var(--color-text-primary);
}

.btn-send,
.btn-stop {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.btn-send:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.btn-stop:hover {
  background: var(--color-error);
  border-color: var(--color-error);
  color: white;
}
</style>
