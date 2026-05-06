<script setup lang="ts">
import { onMounted, watch, nextTick, ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import ConversationList from '@/components/ConversationList.vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'
import ReferencePanel from '@/components/ReferencePanel.vue'

const chatStore = useChatStore()
const messagesRef = ref<HTMLDivElement | null>(null)
const showReferences = ref(false)

onMounted(() => {
  chatStore.fetchConversations()
})

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

watch(() => chatStore.messages.length, scrollToBottom)
watch(() => chatStore.streamingContent, scrollToBottom)

const handleCreate = async () => {
  await chatStore.createConversation()
}

const handleSelect = async (convId: string) => {
  await chatStore.setCurrentConversation(convId)
}

const handleSend = (content: string) => {
  chatStore.sendMessage(content)
}

const handleStop = () => {
  chatStore.stopGeneration()
}

const handleDelete = async (convId: string) => {
  await chatStore.deleteConversation(convId)
}

const toggleReferences = () => {
  showReferences.value = !showReferences.value
}
</script>

<template>
  <div class="chat-layout">
    <!-- Conversation Sidebar -->
    <ConversationList
      :conversations="chatStore.conversations"
      :current-id="chatStore.currentConvId"
      :loading="chatStore.convLoading"
      @create="handleCreate"
      @select="handleSelect"
      @delete="handleDelete"
    />

    <!-- Main Chat Area -->
    <div class="chat-main">
      <!-- Empty State -->
      <el-empty v-if="!chatStore.currentConvId" description="" :image-size="80">
        <template #description>
          <h2 class="empty-title">开始智能对话</h2>
          <p class="empty-desc">选择一个知识库，或直接提问，AI 将为你提供精准回答</p>
        </template>
        <el-button type="primary" @click="handleCreate">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 3V13M3 8H13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          新建对话
        </el-button>
      </el-empty>

      <!-- Chat Active -->
      <template v-else>
        <!-- Top Bar -->
        <div class="chat-topbar">
          <span class="chat-title">
            {{ chatStore.conversations.find(c => c.conversationId === chatStore.currentConvId)?.title || '对话中' }}
          </span>
          <el-button
            text
            :type="showReferences ? 'primary' : 'default'"
            @click="toggleReferences"
            title="引用来源"
          >
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M4 3H9L11 5H14V15H4V3Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
              <path d="M7 9H11M7 11.5H9.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
          </el-button>
        </div>

        <!-- Messages -->
        <div ref="messagesRef" class="messages-area">
          <div v-if="chatStore.msgLoading" class="msg-loading">
            <div v-for="i in 3" :key="i" class="skeleton-msg">
              <el-skeleton-item variant="circle" style="width:32px;height:32px;" />
              <el-skeleton :rows="2" animated style="width:60%;" />
            </div>
          </div>

          <template v-else>
            <ChatMessage
              v-for="msg in chatStore.messages"
              :key="msg.messageId"
              :message="msg"
            />

            <!-- Streaming message -->
            <ChatMessage
              v-if="chatStore.streaming"
              :message="{ messageId: 'streaming', role: 'assistant', content: '', createTime: '' }"
              :streaming="true"
              :streaming-content="chatStore.streamingContent"
              :references="chatStore.streamingReferences"
            />
          </template>
        </div>

        <!-- Input -->
        <ChatInput
          :disabled="chatStore.msgLoading"
          :streaming="chatStore.streaming"
          @send="handleSend"
          @stop="handleStop"
        />
      </template>
    </div>

    <!-- Reference Panel -->
    <Transition name="slide-left">
      <ReferencePanel
        v-if="showReferences && chatStore.currentConvId"
        :sources="chatStore.streamingReferences"
        @close="showReferences = false"
      />
    </Transition>
  </div>
</template>

<style scoped>
.chat-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  background: var(--color-bg);
}

/* Main */
.chat-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Empty State description styling */
.chat-main :deep(.el-empty) {
  flex: 1;
}

.empty-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-2);
}

.empty-desc {
  font-size: var(--font-size-base);
  color: var(--color-text-tertiary);
  text-align: center;
  max-width: 400px;
  margin: 0;
}

/* Topbar */
.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  height: 48px;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-surface);
  flex-shrink: 0;
}

.chat-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

/* Messages */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* Loading skeleton */
.msg-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.skeleton-msg {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}
</style>
