<script setup lang="ts">
import { onMounted, onUnmounted, computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useKnowledgeStore } from '@/stores/knowledge'
import { useDocumentStore } from '@/stores/document'
import { useChatStore } from '@/stores/chat'
import { ElMessageBox } from 'element-plus'
import UploadZone from '@/components/UploadZone.vue'
import DocStatusTag from '@/components/DocStatusTag.vue'

const route = useRoute()
const router = useRouter()
const kbStore = useKnowledgeStore()
const docStore = useDocumentStore()
const chatStore = useChatStore()

const kbId = route.params.kbId as string
const pageNum = ref(1)

const formatSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const currentKB = computed(() => kbStore.kbList.find((k) => k.kbId === kbId))

onMounted(async () => {
  if (!kbStore.kbList.length) {
    await kbStore.fetchKBList()
  }
  await docStore.fetchDocList(kbId, { pageNum: pageNum.value, pageSize: 15 })
  if (docStore.hasPendingDocs()) {
    docStore.startPolling(kbId)
  }
})

onUnmounted(() => {
  docStore.stopPolling()
})

const handleUpload = async (file: File) => {
  await docStore.uploadDoc(kbId, file)
  if (docStore.hasPendingDocs()) {
    docStore.startPolling(kbId)
  }
}

const handleDelete = async (doc: { docId: string; fileName: string }) => {
  try {
    await ElMessageBox.confirm(`确定删除文档「${doc.fileName}」？`, '提示', { type: 'warning' })
    await docStore.deleteDoc(kbId, doc.docId)
  } catch {
    /* cancelled */
  }
}

const handlePageChange = async (page: number) => {
  pageNum.value = page
  await docStore.fetchDocList(kbId, { pageNum: page, pageSize: 15 })
}

const handleChat = async () => {
  await chatStore.createConversation(kbId, currentKB.value?.name)
  router.push({ name: 'chat' })
}
</script>

<template>
  <div class="view-container">
    <header class="view-header">
      <div class="header-left">
        <el-button text @click="router.back()">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M11 4L6 9L11 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </el-button>
        <div>
          <h1 class="page-title">{{ currentKB?.name || '知识库详情' }}</h1>
          <span class="page-subtitle" v-if="currentKB?.description">{{ currentKB.description }}</span>
        </div>
      </div>
      <el-button type="primary" @click="handleChat">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M2 8L14 2L8 14L7 9L2 8Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
        开始对话
      </el-button>
    </header>

    <div class="view-body">
      <UploadZone @upload="handleUpload" />

      <!-- Document Table -->
      <el-table
        :data="docStore.docList"
        v-loading="docStore.loading && docStore.docList.length === 0"
        style="width: 100%;"
        :header-cell-style="{ background: 'var(--color-bg-sunken)', fontSize: 'var(--font-size-xs)', fontWeight: 'var(--font-weight-semibold)', color: 'var(--color-text-tertiary)', textTransform: 'uppercase', letterSpacing: '0.5px' }"
      >
        <el-table-column label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="doc-name-cell">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" class="file-icon"><path d="M4 2H9L12 5V14H4V2Z" stroke="currentColor" stroke-width="1.2"/><path d="M9 2V5H12" stroke="currentColor" stroke-width="1.2"/></svg>
              {{ row.fileName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="80">
          <template #default="{ row }">
            <span class="mono-text">{{ formatSize(row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <DocStatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="切片数" width="70">
          <template #default="{ row }">
            <span class="mono-text">{{ row.status === 2 ? row.chunkCount : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="120">
          <template #default="{ row }">
            <span class="mono-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="50" align="center">
          <template #default="{ row }">
            <el-button text type="danger" size="small" @click="handleDelete(row)" title="删除">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 4H11M5 4V3C5 2.45 5.45 2 6 2H8C8.55 2 9 2.45 9 3V4M10 4V12C10 12.55 9.55 13 9 13H5C4.45 13 4 12.55 4 12V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty v-if="!docStore.loading" description="暂无文档" />
        </template>
      </el-table>

      <el-pagination
        v-if="docStore.total > 15"
        v-model:current-page="pageNum"
        :total="docStore.total"
        :page-size="15"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        style="justify-content: center; padding: var(--space-4) 0;"
      />
    </div>
  </div>
</template>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-subtitle {
  display: block;
  margin-top: 2px;
}

.view-body {
  gap: var(--space-5);
}

/* Doc name cell */
.doc-name-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-icon {
  color: var(--color-text-tertiary);
  flex-shrink: 0;
}

/* Mono text for sizes, time, chunks */
.mono-text {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  font-family: var(--font-mono);
}

/* Override ElTable border radius to match design */
:deep(.el-table) {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  border-bottom: 1px solid var(--color-border-light);
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--font-size-sm);
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background: var(--color-surface-hover);
}

:deep(.el-table__empty-block) {
  min-height: 120px;
}

/* Override ElButton back button to match original back-btn layout */
:deep(.header-left .el-button) {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
}

:deep(.header-left .el-button:hover) {
  background: var(--color-surface-hover);
  color: var(--color-text-primary);
}
</style>
