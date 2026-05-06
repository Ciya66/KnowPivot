import http from '@/utils/request'
import type {
  KBCreateReq,
  KBCreateRes,
  KBItem,
  PageParams,
  PageData,
  DocItem,
  DocUploadRes,
} from '@/types/api'

export const knowledgeApi = {
  // Knowledge Base CRUD
  create: (data: KBCreateReq) =>
    http.post<never, KBCreateRes>('/api/v1/knowledge-bases', data),

  getList: (params: PageParams) =>
    http.get<never, PageData<KBItem>>('/api/v1/knowledge-bases', { params }),

  update: (kbId: string, data: Partial<KBCreateReq>) =>
    http.put<never, void>(`/api/v1/knowledge-bases/${kbId}`, data),

  delete: (kbId: string) =>
    http.delete<never, void>(`/api/v1/knowledge-bases/${kbId}`),

  // Documents
  uploadDoc: (kbId: string, formData: FormData) =>
    http.post<never, DocUploadRes>(
      `/api/v1/knowledge-bases/${kbId}/documents/upload`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } },
    ),

  getDocList: (kbId: string, params: PageParams & { status?: number }) =>
    http.get<never, PageData<DocItem>>(
      `/api/v1/knowledge-bases/${kbId}/documents`,
      { params },
    ),

  deleteDoc: (docId: string) =>
    http.delete<never, void>(`/api/v1/documents/${docId}`),
}
