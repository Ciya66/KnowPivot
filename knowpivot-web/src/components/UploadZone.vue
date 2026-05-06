<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'

const emit = defineEmits<{
  upload: [file: File]
}>()

const acceptTypes = '.pdf,.doc,.docx,.txt'
const MAX_SIZE = 50 * 1024 * 1024 // 50MB

const handleChange = (file: UploadFile) => {
  if (file.size && file.size > MAX_SIZE) {
    ElMessage.warning('文件大小不能超过 50MB')
    return
  }
  if (file.raw) {
    emit('upload', file.raw)
  }
}
</script>

<template>
  <el-upload
    drag
    :auto-upload="false"
    :show-file-list="false"
    :accept="acceptTypes"
    :on-change="handleChange"
    class="upload-zone-wrapper"
  >
    <svg width="32" height="32" viewBox="0 0 32 32" fill="none" class="upload-icon">
      <path d="M16 20V8M16 8L11 13M16 8L21 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M4 22V26C4 27.1 4.9 28 6 28H26C27.1 28 28 27.1 28 26V22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    </svg>
    <p class="upload-text">拖拽文件到此处，或 <span class="upload-link">点击选择</span></p>
    <p class="upload-hint">支持 PDF、Word、TXT，单文件最大 50MB</p>
  </el-upload>
</template>

<style scoped>
.upload-zone-wrapper {
  width: 100%;
}

.upload-zone-wrapper :deep(.el-upload) {
  width: 100%;
}

.upload-zone-wrapper :deep(.el-upload-dragger) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-8) var(--space-6);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
}

.upload-zone-wrapper :deep(.el-upload-dragger:hover) {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.upload-zone-wrapper.is-dragover :deep(.el-upload-dragger) {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.upload-icon {
  opacity: 0.6;
}

.upload-zone-wrapper :deep(.el-upload-dragger:hover) .upload-icon {
  opacity: 1;
}

.upload-text {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.upload-link {
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.upload-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}
</style>
