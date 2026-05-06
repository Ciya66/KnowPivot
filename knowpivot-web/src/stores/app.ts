import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', () => {
  const theme = ref<'light' | 'dark'>('light')
  const sidebarCollapsed = ref(false)

  const syncThemeDOM = () => {
    const html = document.documentElement
    html.setAttribute('data-theme', theme.value)
    html.classList.toggle('dark', theme.value === 'dark')
  }

  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    syncThemeDOM()
  }

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // Sync DOM on store creation
  syncThemeDOM()

  return { theme, sidebarCollapsed, toggleTheme, toggleSidebar }
})
