import { ref, nextTick } from 'vue'
import { defineStore } from 'pinia'
import { chatApi } from '@/api/chat'
import { sendSSEMessage } from '@/utils/sse'
import type { ConvItem, MessageItem, SSEReferenceSource } from '@/types/api'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<ConvItem[]>([])
  const currentConvId = ref<string | null>(null)
  const messages = ref<MessageItem[]>([])
  const streaming = ref(false)
  const streamingContent = ref('')
  const streamingReferences = ref<SSEReferenceSource[]>([])
  const convLoading = ref(false)
  const msgLoading = ref(false)

  let abortController: AbortController | null = null

  const fetchConversations = async () => {
    convLoading.value = true
    try {
      const res = await chatApi.getConversations({ pageNum: 1, pageSize: 100 })
      conversations.value = res.list
    } finally {
      convLoading.value = false
    }
  }

  const createConversation = async (kbId?: string, title?: string) => {
    const conv = await chatApi.createConversation({ kbId, title })
    conversations.value.unshift(conv)
    currentConvId.value = conv.conversationId
    messages.value = []
    return conv
  }

  const setCurrentConversation = async (convId: string) => {
    currentConvId.value = convId
    messages.value = []
    await fetchMessages(convId)
  }

  const fetchMessages = async (convId: string) => {
    msgLoading.value = true
    try {
      const res = await chatApi.getMessages(convId, { pageNum: 1, pageSize: 200 })
      messages.value = res.list
    } finally {
      msgLoading.value = false
    }
  }

  const sendMessage = (content: string) => {
    if (!currentConvId.value || streaming.value) return

    // Add user message immediately
    const userMsg: MessageItem = {
      messageId: `temp-${Date.now()}`,
      role: 'user',
      content,
      createTime: new Date().toISOString(),
    }
    messages.value.push(userMsg)

    streaming.value = true
    streamingContent.value = ''
    streamingReferences.value = []

    abortController = sendSSEMessage(
      { conversationId: currentConvId.value, content },
      {
        onMessage: (delta) => {
          streamingContent.value += delta
        },
        onReferences: (sources) => {
          streamingReferences.value = sources
        },
        onDone: (data) => {
          const assistantMsg: MessageItem = {
            messageId: data.messageId,
            role: 'assistant',
            content: streamingContent.value,
            references: streamingReferences.value.map((s) => ({
              docId: s.segmentId,
              docName: s.docName,
              pageNum: s.pageNum ?? 0,
              content: s.content,
            })),
            createTime: new Date().toISOString(),
          }
          messages.value.push(assistantMsg)
          streaming.value = false
          streamingContent.value = ''
          streamingReferences.value = []
        },
        onError: () => {
          const errorMsg: MessageItem = {
            messageId: `err-${Date.now()}`,
            role: 'assistant',
            content: '抱歉，回答生成失败，请稍后重试。',
            createTime: new Date().toISOString(),
          }
          messages.value.push(errorMsg)
          streaming.value = false
          streamingContent.value = ''
          streamingReferences.value = []
        },
      },
    )
  }

  const stopGeneration = () => {
    abortController?.abort()
    abortController = null
    if (streamingContent.value) {
      const assistantMsg: MessageItem = {
        messageId: `partial-${Date.now()}`,
        role: 'assistant',
        content: streamingContent.value,
        references: streamingReferences.value.map((s) => ({
          docId: s.segmentId,
          docName: s.docName,
          pageNum: s.pageNum ?? 0,
          content: s.content,
        })),
        createTime: new Date().toISOString(),
      }
      messages.value.push(assistantMsg)
    }
    streaming.value = false
    streamingContent.value = ''
    streamingReferences.value = []
  }

  const deleteConversation = async (convId: string) => {
    await chatApi.deleteConversation(convId)
    const wasCurrent = currentConvId.value === convId
    if (wasCurrent) clearCurrent()
    await fetchConversations()
  }

  const clearCurrent = () => {
    currentConvId.value = null
    messages.value = []
    streamingContent.value = ''
    streamingReferences.value = []
    stopGeneration()
  }

  return {
    conversations,
    currentConvId,
    messages,
    streaming,
    streamingContent,
    streamingReferences,
    convLoading,
    msgLoading,
    fetchConversations,
    createConversation,
    setCurrentConversation,
    fetchMessages,
    sendMessage,
    stopGeneration,
    deleteConversation,
    clearCurrent,
  }
})
