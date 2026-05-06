<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { KBCreateReq, KBItem } from '@/types/api'

const props = defineProps<{
  visible: boolean
  editData?: KBItem | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: KBCreateReq]
}>()

const form = reactive({
  name: '',
  description: '',
  chunkSize: 500,
  overlapSize: 50,
  similarityThreshold: 0.7,
})

const showAdvanced = ref(false)

watch(
  () => props.visible,
  (val) => {
    if (val && props.editData) {
      form.name = props.editData.name
      form.description = props.editData.description
    } else if (val) {
      form.name = ''
      form.description = ''
      form.chunkSize = 500
      form.overlapSize = 50
      form.similarityThreshold = 0.7
      showAdvanced.value = false
    }
  },
)

const handleSubmit = () => {
  if (!form.name.trim()) return
  emit('submit', {
    name: form.name.trim(),
    description: form.description.trim() || undefined,
    config: showAdvanced.value
      ? {
          chunkSize: form.chunkSize,
          overlapSize: form.overlapSize,
          similarityThreshold: form.similarityThreshold,
        }
      : undefined,
  })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="editData ? '编辑知识库' : '新建知识库'"
    width="440px"
    :close-on-click-modal="true"
    destroy-on-close
    @close="emit('close')"
  >
    <el-form :model="form" label-position="top">
      <el-form-item label="名称" required>
        <el-input
          v-model="form.name"
          placeholder="请输入知识库名称"
          maxlength="64"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="简要描述知识库用途"
          maxlength="256"
          show-word-limit
        />
      </el-form-item>

      <el-button link type="primary" class="advanced-toggle" @click="showAdvanced = !showAdvanced">
        {{ showAdvanced ? '收起' : '高级配置' }}
        <svg
          width="14"
          height="14"
          viewBox="0 0 14 14"
          fill="none"
          class="chevron-icon"
          :class="{ rotated: showAdvanced }"
        >
          <path
            d="M4 6L7 9L10 6"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </el-button>

      <Transition name="fade">
        <div v-if="showAdvanced" class="advanced-config">
          <el-form-item label="切片大小（tokens）">
            <el-input-number
              v-model="form.chunkSize"
              :min="100"
              :max="2000"
              :step="50"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="重叠大小">
            <el-input-number
              v-model="form.overlapSize"
              :min="0"
              :max="500"
              :step="10"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="相似度阈值">
            <el-input-number
              v-model="form.similarityThreshold"
              :min="0"
              :max="1"
              :step="0.05"
              :precision="2"
              controls-position="right"
            />
          </el-form-item>
        </div>
      </Transition>
    </el-form>

    <template #footer>
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="primary" :disabled="!form.name.trim()" @click="handleSubmit">
        {{ editData ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.advanced-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.chevron-icon {
  transition: transform var(--transition-fast);
}

.chevron-icon.rotated {
  transform: rotate(180deg);
}

.advanced-config {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--color-bg-sunken);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-3);
}

[data-theme='dark'] .advanced-config {
  background: var(--color-bg-sunken);
}
</style>
