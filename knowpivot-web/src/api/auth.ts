import http from '@/utils/request'
import type { LoginReq, LoginRes, RegisterReq, UserInfo } from '@/types/api'

export const authApi = {
  login: (data: LoginReq) =>
    http.post<never, LoginRes>('/api/v1/auth/login', data),

  register: (data: RegisterReq) =>
    http.post<never, { userId: string }>('/api/v1/auth/register', data),

  getUserInfo: () =>
    http.get<never, UserInfo>('/api/v1/users/me'),
}
