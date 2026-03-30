<template>
  <div class="chat-page">
    <header class="chat-header">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <h1>恋语AI恋爱大师</h1>
      <div class="header-right">
        <span class="chat-id">会话: {{ chatId }}</span>
        <span class="user-name">{{ displayName }}</span>
        <button type="button" class="header-logout" @click="logout">退出</button>
      </div>
    </header>

    <div class="chat-container">
      <div class="messages" ref="messagesRef">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
        >
          <div class="message-content">
            <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            <div class="bubble">
              {{ msg.isStreaming ? streamingContent : msg.content }}
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <textarea
          v-model="inputText"
          placeholder="输入您的问题；可点「对象推荐」根据择偶/交友需求匹配资料库中的候选人"
          rows="2"
          :disabled="loading"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <div class="input-actions">
          <button
            type="button"
            class="recommend-btn"
            :disabled="loading || !inputText.trim()"
            title="基于知识库中的恋爱对象档案做匹配推荐（非流式）"
            @click="sendRecommend"
          >
            {{ loading ? '请稍候…' : '对象推荐' }}
          </button>
          <button
            class="send-btn"
            :disabled="loading || !inputText.trim()"
            @click="sendMessage"
          >
            {{ loading ? '请稍候…' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { generateChatId } from '../utils/chatId'
import { chatWithLoveAppSse } from '../utils/sse'
import { recommendPartner } from '../api/loveApp'
import { clearAuth, getStoredUsername } from '../utils/authToken'

const router = useRouter()
const displayName = computed(() => getStoredUsername() || '用户')

function logout() {
  clearAuth()
  router.replace('/login?redirect=/love-app')
}

const chatId = ref('')
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const streamingContent = ref('')
let abortController = null

onMounted(() => {
  chatId.value = generateChatId()
})

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  streamingContent.value = ''

  const aiMessage = { role: 'assistant', content: '', isStreaming: true }
  messages.value.push(aiMessage)

  await nextTick()
  scrollToBottom()

  abortController = chatWithLoveAppSse(
    text,
    chatId.value,
    {
      onChunk: (chunk) => {
        streamingContent.value += chunk
        nextTick(scrollToBottom)
      },
      onComplete: () => {
        aiMessage.content = streamingContent.value
        aiMessage.isStreaming = false
        streamingContent.value = ''
        loading.value = false
        nextTick(scrollToBottom)
      },
      onError: (err) => {
        aiMessage.content = streamingContent.value + `\n[错误: ${err.message}]`
        aiMessage.isStreaming = false
        streamingContent.value = ''
        loading.value = false
      }
    }
  )
}

/** 对象推荐：走 RAG 推荐接口，一次性展示全文（不走 SSE） */
async function sendRecommend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: `【对象推荐】${text}` })
  inputText.value = ''
  loading.value = true

  const aiMessage = { role: 'assistant', content: '正在从资料库匹配…', isStreaming: false }
  messages.value.push(aiMessage)

  await nextTick()
  scrollToBottom()

  try {
    const data = await recommendPartner(text, chatId.value)
    aiMessage.content = typeof data === 'string' ? data : String(data ?? '')
  } catch (e) {
    aiMessage.content = `[错误: ${e.message || e}]`
  } finally {
    loading.value = false
    nextTick(scrollToBottom)
  }
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  /* 锁定为视口高度，仅中间 .messages 滚动，顶栏与输入区始终可见 */
  height: 100vh;
  max-height: 100vh;
  height: 100dvh;
  max-height: 100dvh;
  min-height: 0;
  overflow: hidden;
  background: var(--color-bg-dark);
}

.chat-header {
  padding: 16px 24px;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.back-btn {
  color: var(--color-accent-love);
  text-decoration: none;
  font-size: 0.95rem;
  transition: color 0.2s;
}

.back-btn:hover {
  color: var(--color-accent);
}

.chat-header h1 {
  flex: 1;
  min-width: 0;
  font-family: var(--font-display);
  font-size: 1.15rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.chat-id {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.user-name {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-logout {
  padding: 6px 12px;
  font-size: 0.8rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  color: var(--color-text-muted);
  cursor: pointer;
  font-family: inherit;
}

.header-logout:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted);
}

.chat-container {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  -webkit-overflow-scrolling: touch;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 90%;
}

.message.user .message-content {
  flex-direction: row-reverse;
  margin-left: auto;
}

/* AI 消息：头像在左，文字左对齐 */
.message.assistant .message-content {
  flex-direction: row;
}

.avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 50%;
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  flex-shrink: 0;
}

.message.assistant .avatar {
  background: rgba(236, 72, 153, 0.2);
  border-color: rgba(236, 72, 153, 0.4);
  color: var(--color-accent-love);
}

.message.user .avatar {
  background: rgba(124, 58, 237, 0.2);
  border-color: rgba(124, 58, 237, 0.4);
  color: var(--color-accent-secondary);
}

.bubble {
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}

.message.user .bubble {
  background: rgba(124, 58, 237, 0.15);
  border-color: rgba(124, 58, 237, 0.3);
  color: var(--color-text);
}

.message.assistant .bubble {
  background: var(--color-bg-elevated);
  border-color: var(--color-border);
  color: var(--color-text);
}

.input-area {
  padding: 16px 24px 24px;
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.input-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.recommend-btn {
  padding: 10px 16px;
  background: transparent;
  color: var(--color-accent-love);
  border: 1px solid rgba(236, 72, 153, 0.55);
  border-radius: var(--radius-md);
  font-size: 0.9rem;
  font-family: inherit;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s, border-color 0.2s;
}

.recommend-btn:hover:not(:disabled) {
  background: rgba(236, 72, 153, 0.12);
  border-color: var(--color-accent-love);
}

.recommend-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.input-area textarea {
  flex: 1;
  padding: 12px 16px;
  background: var(--color-bg-dark);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-family: inherit;
  color: var(--color-text);
  resize: none;
}

.input-area textarea::placeholder {
  color: var(--color-text-muted);
}

.input-area textarea:focus {
  outline: none;
  border-color: var(--color-accent-love);
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #ec4899 0%, #be185d 100%);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-family: inherit;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.2s;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .chat-header {
    padding: 12px 16px;
  }

  .chat-header h1 {
    font-size: 1rem;
  }

  .messages {
    padding: 16px;
    gap: 16px;
  }

  .message-content {
    max-width: 95%;
  }

  .avatar {
    width: 32px;
    height: 32px;
    min-width: 32px;
    font-size: 0.75rem;
  }

  .input-area {
    padding: 12px 16px 20px;
  }
}

@media (max-width: 480px) {
  .chat-id {
    display: none;
  }

  .messages {
    padding: 12px;
  }
}
</style>
