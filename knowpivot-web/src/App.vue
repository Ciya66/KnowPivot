<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const route = useRoute()
const authStore = useAuthStore()

onMounted(() => {
  if (authStore.isLoggedIn) {
    authStore.fetchUserInfo()
  }
})
</script>

<template>
  <el-config-provider :locale="zhCn">
    <template v-if="route.name === 'login'">
      <router-view />
    </template>
    <DefaultLayout v-else v-slot="{ togglePanel }">
      <router-view v-slot="{ Component }">
        <Transition name="fade-slide" mode="out-in">
          <component :is="Component" :toggle-panel="togglePanel" />
        </Transition>
      </router-view>
    </DefaultLayout>
  </el-config-provider>
</template>

<style>
@import '@/styles/global.css';
@import '@/styles/transitions.css';
</style>
