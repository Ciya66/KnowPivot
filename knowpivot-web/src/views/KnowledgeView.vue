<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useKnowledgeStore } from '@/stores/knowledge'
import { ElMessageBox } from 'element-plus'
import CreateKBDialog from '@/components/CreateKBDialog.vue'
import type { KBCreateReq, KBItem } from '@/types/api'

const router = useRouter()
const store = useKnowledgeStore()

const showDialog = ref(false)
const editData = ref<KBItem | null>(null)
const pageNum = ref(1)

onMounted(() => {
  store.fetchKBList(pageNum.value)
})

const handlePageChange = (page: number) => {
  pageNum.value = page
  store.fetchKBList(page)
}

const openCreate = () => {
  editData.value = null
  showDialog.value = true
}

const openEdit = (kb: KBItem) => {
  editData.value = kb
  showDialog.value = true
}

const handleSubmit = async (data: KBCreateReq) => {
  if (editData.value) {
    await store.updateKB(editData.value.kbId, data)
  } else {
    await store.createKB(data)
  }
  showDialog.value = false
}

const handleDelete = async (kb: KBItem) => {
  try {
    await ElMessageBox.confirm(`确定删除知识库「${kb.name}」？`, '提示', { type: 'warning' })
    await store.deleteKB(kb.kbId)
  } catch {
    /* cancelled */
  }
}

const enterKB = (kb: KBItem) => {
  router.push(`/knowledge/${kb.kbId}`)
}
</script>

<template>
  <div class="view-container">
    <header class="view-header">
      <div class="header-left">
        <h1 class="page-title">知识库</h1>
        <span class="page-subtitle">管理你的知识集合与文档</span>
      </div>
      <el-button type="primary" @click="openCreate">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 3V13M3 8H13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        新建知识库
      </el-button>
    </header>

    <div class="view-body">
      <!-- Loading skeleton -->
      <div v-if="store.loading && store.kbList.length === 0" class="kb-grid">
        <el-card v-for="i in 4" :key="i" shadow="hover" class="kb-card">
          <el-skeleton :rows="3" animated />
        </el-card>
      </div>

      <!-- Empty state -->
      <el-empty v-else-if="!store.loading && store.kbList.length === 0" description="暂无知识库">
        <el-button type="primary" @click="openCreate">创建第一个知识库</el-button>
      </el-empty>

      <!-- KB Grid -->
      <div v-else class="kb-grid">
        <el-card
          v-for="kb in store.kbList"
          :key="kb.kbId"
          shadow="hover"
          class="kb-card"
          @click="enterKB(kb)"
        >
          <div class="kb-card-header">
            <div class="kb-icon">
              <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                <path d="M4 5C4 4.45 4.45 4 5 4H11L13 6H17C17.55 6 18 6.45 18 7V17C18 17.55 17.55 18 17 18H5C4.45 18 4 17.55 4 17V5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                <path d="M8 11H14M8 14H12" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="kb-actions" @click.stop>
              <el-button text size="small" @click="openEdit(kb)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M10 2L12 4L5 11H3V9L10 2Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
              </el-button>
              <el-button text size="small" type="danger" @click="handleDelete(kb)" title="删除">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 4H11M5 4V3C5 2.45 5.45 2 6 2H8C8.55 2 9 2.45 9 3V4M10 4V12C10 12.55 9.55 13 9 13H5C4.45 13 4 12.55 4 12V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </el-button>
            </div>
          </div>
          <h3 class="kb-card-name">{{ kb.name }}</h3>
          <p class="kb-card-desc">{{ kb.description || '暂无描述' }}</p>
          <div class="kb-card-footer">
            <span class="kb-meta">{{ kb.docCount }} 篇文档</span>
            <span class="kb-time">{{ kb.createTime }}</span>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="store.total > 10"
        v-model:current-page="pageNum"
        :total="store.total"
        :page-size="10"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        style="justify-content: center; padding: var(--space-4) 0;"
      />
    </div>

    <CreateKBDialog :visible="showDialog" :edit-data="editData" @close="showDialog = false" @submit="handleSubmit" />
  </div>
</template>

<style scoped>
/* Grid */
.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

/* KB Card - override ElCard defaults */
.kb-card {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.kb-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-5);
}

.kb-card:hover {
  transform: translateY(-2px);
}

.kb-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.kb-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.kb-actions {
  display: flex;
  gap: var(--space-1);
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.kb-card:hover .kb-actions {
  opacity: 1;
}

.kb-card-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.kb-card-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-relaxed);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.kb-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border-light);
  margin-top: auto;
}

.kb-meta {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  font-family: var(--font-mono);
}

.kb-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}
</style>
