<script setup lang="ts">
import SideNav from '@/components/SideNav.vue'
import RightPanel from '@/components/RightPanel.vue'
import { ref } from 'vue'

const isPanelOpen = ref(false)
const togglePanel = () => {
  isPanelOpen.value = !isPanelOpen.value
}
</script>

<template>
  <div class="layout">
    <SideNav />
    <main class="layout-main">
      <slot :toggle-panel="togglePanel" />
    </main>
    <Transition name="slide-left">
      <RightPanel v-if="isPanelOpen" @close="isPanelOpen = false" />
    </Transition>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: var(--color-bg);
}

.layout-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
