// ============================================
// KnowPivot API Type Definitions
// ============================================

// ---- Unified Response ----
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface PageParams {
  pageNum: number
  pageSize: number
}

export interface PageData<T> {
  total: number
  list: T[]
}

// ---- Auth ----
export interface LoginReq {
  username: string
  password: string
}

export interface LoginRes {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export interface RegisterReq {
  username: string
  password: string
  nickname?: string
}

export interface UserInfo {
  userId: string
  username: string
  nickname: string
  avatarUrl: string
  tokenQuota: number
  role: number // 0: user, 1: admin
}

// ---- Knowledge Base ----
export interface KBConfig {
  chunkSize: number
  overlapSize: number
  similarityThreshold: number
}

export interface KBCreateReq {
  name: string
  description?: string
  config?: KBConfig
}

export interface KBCreateRes {
  kbId: string
  indexName: string
}

export interface KBItem {
  kbId: string
  name: string
  description: string
  creatorId: string
  createTime: string
  docCount: number
}

// ---- Document ----
export interface DocItem {
  docId: string
  fileName: string
  fileSize: number
  status: number // 0: uploaded, 1: parsing, 2: indexed, 3: failed
  chunkCount: number
  createTime: string
  updateTime: string
}

export interface DocUploadRes {
  docId: string
  status: number
  createTime: string
}

// ---- Conversation & Chat ----
export interface ConvItem {
  conversationId: string
  title: string
  createTime: string
}

export interface Reference {
  docId: string
  docName: string
  pageNum: number
}

export interface MessageItem {
  messageId: string
  role: 'user' | 'assistant'
  content: string
  references?: Reference[]
  feedback?: number // 1: like, 2: dislike
  createTime: string
}

// ---- SSE Event Data ----
export interface SSEMessageDelta {
  delta: string
}

export interface SSEReferenceSource {
  docName: string
  segmentId: string
  content: string
}

export interface SSEReferences {
  sources: SSEReferenceSource[]
}

export interface SSEDone {
  messageId: string
  tokenCount: number
  finishReason: string
}

// ---- Document Status Constants ----
export const DOC_STATUS = {
  UPLOADED: 0,
  PARSING: 1,
  INDEXED: 2,
  FAILED: 3,
} as const

export const DOC_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '已上传', color: 'default' },
  1: { label: '解析中', color: 'processing' },
  2: { label: '已索引', color: 'success' },
  3: { label: '失败', color: 'error' },
}
