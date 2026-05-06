import { ref } from 'vue'
import { defineStore } from 'pinia'
import { knowledgeApi } from '@/api/knowledge'
import type { KBItem, KBCreateReq } from '@/types/api'

export const useKnowledgeStore = defineStore('knowledge', () => {
  const kbList = ref<KBItem[]>([])
  const total = ref(0)
  const loading = ref(false)
  const currentKB = ref<KBItem | null>(null)

  const fetchKBList = async (pageNum = 1, pageSize = 10) => {
    loading.value = true
    try {
      const res = await knowledgeApi.getList({ pageNum, pageSize })
      kbList.value = res.list
      total.value = res.total
    } finally {
      loading.value = false
    }
  }

  const createKB = async (data: KBCreateReq) => {
    const res = await knowledgeApi.create(data)
    await fetchKBList()
    return res
  }

  const updateKB = async (kbId: string, data: Partial<KBCreateReq>) => {
    await knowledgeApi.update(kbId, data)
    await fetchKBList()
  }

  const deleteKB = async (kbId: string) => {
    await knowledgeApi.delete(kbId)
    await fetchKBList()
  }

  const setCurrentKB = (kb: KBItem | null) => {
    currentKB.value = kb
  }

  return { kbList, total, loading, currentKB, fetchKBList, createKB, updateKB, deleteKB, setCurrentKB }
})
