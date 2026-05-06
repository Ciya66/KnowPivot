import http from '@/utils/request'
import type { ConvItem, MessageItem, PageParams, PageData } from '@/types/api'

export const chatApi = {
  createConversation: (data: { kbId?: string; title?: string }) =>
    http.post<never, ConvItem>('/api/v1/conversations', data),

  getConversations: (params?: PageParams) =>
    http.get<never, PageData<ConvItem>>('/api/v1/conversations', { params }),

  deleteConversation: (conversationId: string) =>
    http.delete<never, void>(`/api/v1/conversations/${conversationId}`),

  getMessages: (conversationId: string, params: PageParams) =>
    http.get<never, PageData<MessageItem>>(
      `/api/v1/conversations/${conversationId}/messages`,
      { params },
    ),
}
