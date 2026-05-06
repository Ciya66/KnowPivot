import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { TOKEN_KEY } from '@/utils/request'
import type { UserInfo } from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)

  const setToken = (t: string) => {
    token.value = t
    localStorage.setItem(TOKEN_KEY, t)
  }

  const clearAuth = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    setToken(res.token)
    userInfo.value = res.userInfo
    return res
  }

  const register = async (username: string, password: string, nickname?: string) => {
    await authApi.register({ username, password, nickname })
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      userInfo.value = await authApi.getUserInfo()
    } catch {
      clearAuth()
    }
  }

  const logout = () => {
    clearAuth()
  }

  return { token, userInfo, isLoggedIn, login, register, fetchUserInfo, logout, setToken, clearAuth }
})
