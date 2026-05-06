<script setup lang="ts">
import { computed } from 'vue'
import { DOC_STATUS_MAP } from '@/types/api'

const props = defineProps<{
  status: number
}>()

const tagType = computed(() => {
  const map: Record<number, 'info' | 'primary' | 'success' | 'danger'> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger',
  }
  return map[props.status] ?? 'info'
})
</script>

<template>
  <el-tag
    :type="tagType"
    size="small"
    :class="{ 'is-parsing': status === 1 }"
    disable-transitions
  >
    {{ DOC_STATUS_MAP[status]?.label ?? '未知' }}
  </el-tag>
</template>

<style scoped>
.is-parsing :deep(.el-tag__content) {
  animation: pulse-glow 1.5s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
</style>
