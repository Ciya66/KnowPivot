import { ref } from 'vue'
import { defineStore } from 'pinia'
import { knowledgeApi } from '@/api/knowledge'
import type { DocItem, PageParams } from '@/types/api'

export const useDocumentStore = defineStore('document', () => {
  const docList = ref<DocItem[]>([])
  const total = ref(0)
  const loading = ref(false)
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const fetchDocList = async (kbId: string, params: PageParams & { status?: number } = { pageNum: 1, pageSize: 20 }) => {
    loading.value = true
    try {
      const res = await knowledgeApi.getDocList(kbId, params)
      docList.value = res.list
      total.value = res.total
    } finally {
      loading.value = false
    }
  }

  const uploadDoc = async (kbId: string, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const res = await knowledgeApi.uploadDoc(kbId, formData)
    await fetchDocList(kbId)
    return res
  }

  const deleteDoc = async (kbId: string, docId: string) => {
    await knowledgeApi.deleteDoc(docId)
    await fetchDocList(kbId)
  }

  const hasPendingDocs = () => {
    return docList.value.some((d) => d.status === 0 || d.status === 1)
  }

  const startPolling = (kbId: string) => {
    stopPolling()
    pollTimer = setInterval(async () => {
      await fetchDocList(kbId)
      if (!hasPendingDocs()) {
        stopPolling()
      }
    }, 3000)
  }

  const stopPolling = () => {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  return { docList, total, loading, fetchDocList, uploadDoc, deleteDoc, hasPendingDocs, startPolling, stopPolling }
})
