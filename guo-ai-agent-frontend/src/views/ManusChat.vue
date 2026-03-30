<template>
  <div class="chat-page">
    <header class="chat-header">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <h1>AI 超级智能体</h1>
      <div class="header-right">
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
            <div v-if="msg.role === 'user'" class="bubble">{{ msg.content }}</div>
            <div v-else class="bubble bubble-rich">
              <span v-if="msg.isStreaming && !msg.content.trim()" class="typing-placeholder">正在生成…</span>
              <template v-else>
                <template v-for="(seg, si) in parseTextWithPdfLinks(msg.content)" :key="si">
                  <span v-if="seg.type === 'text'">{{ seg.text }}</span>
                  <button
                    v-else
                    type="button"
                    class="pdf-download-btn"
                    @click="onPdfDownload(seg.url)"
                  >
                    下载 PDF
                  </button>
                </template>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loading" class="processing-hint" role="status" aria-live="polite">
        <span class="processing-spinner" aria-hidden="true"></span>
        <div class="processing-text">
          <span class="processing-title">正在处理</span>
          <span class="processing-sub">智能体正在推理或调用工具，请稍候…</span>
        </div>
      </div>

      <div class="input-area">
        <textarea
          v-model="inputText"
          placeholder="输入您的问题..."
          rows="2"
          :disabled="loading"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <button
          class="send-btn"
          :disabled="loading || !inputText.trim()"
          @click="sendMessage"
        >
          {{ loading ? '发送中...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithManusSse } from '../utils/sse'
import { clearAuth, getStoredUsername } from '../utils/authToken'
import { parseTextWithPdfLinks, downloadAuthenticatedPdf } from '../utils/pdfDownload'

const router = useRouter()
const displayName = computed(() => getStoredUsername() || '用户')

function logout() {
  clearAuth()
  router.replace('/login?redirect=/manus')
}

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
let abortController = null

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()

  const aiMsg = { role: 'assistant', content: '', isStreaming: true }
  messages.value.push(aiMsg)

  abortController = chatWithManusSse(text, {
    onChunk: (chunk) => {
      const t = chunk != null ? String(chunk).trim() : ''
      if (!t) return
      aiMsg.content += (aiMsg.content ? '\n\n' : '') + t
      nextTick(scrollToBottom)
    },
    onComplete: () => {
      aiMsg.isStreaming = false
      if (!aiMsg.content.trim()) {
        aiMsg.content = '（本轮未返回可见正文，请换种方式描述需求或稍后重试。）'
      }
      loading.value = false
      nextTick(scrollToBottom)
    },
    onError: (err) => {
      aiMsg.isStreaming = false
      if (!aiMsg.content.trim()) {
        aiMsg.content = `[错误: ${err.message}]`
      } else {
        aiMsg.content += `\n\n[错误: ${err.message}]`
      }
      loading.value = false
      nextTick(scrollToBottom)
    }
  })
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

async function onPdfDownload(url) {
  try {
    await downloadAuthenticatedPdf(url)
  } catch (e) {
    messages.value.push({
      role: 'assistant',
      content: `[下载失败: ${e?.message || e}]。请确认已登录，且链接未过期。]`
    })
    nextTick(scrollToBottom)
  }
}
</script>

<style scoped>
.chat-page {
  flex: 1;
  display: flex;
  flex-direction: column;
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
  color: var(--color-accent-manus);
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
  background: rgba(6, 182, 212, 0.2);
  border-color: rgba(6, 182, 212, 0.4);
  color: var(--color-accent-manus);
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

.bubble-rich {
  white-space: pre-wrap;
  word-break: break-word;
}

.typing-placeholder {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.pdf-download-btn {
  display: inline-block;
  vertical-align: middle;
  padding: 6px 12px;
  margin: 0 4px;
  font-size: 0.85rem;
  font-family: inherit;
  color: #fff;
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.pdf-download-btn:hover {
  opacity: 0.92;
}

.processing-hint {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 16px 12px;
  padding: 12px 16px;
  background: rgba(6, 182, 212, 0.12);
  border: 1px solid rgba(6, 182, 212, 0.35);
  border-radius: var(--radius-md);
  color: var(--color-text);
}

.processing-spinner {
  width: 22px;
  height: 22px;
  border: 2px solid rgba(6, 182, 212, 0.25);
  border-top-color: var(--color-accent-manus);
  border-radius: 50%;
  flex-shrink: 0;
  animation: manus-spin 0.75s linear infinite;
}

.processing-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.processing-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-accent-manus);
}

.processing-sub {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

@keyframes manus-spin {
  to {
    transform: rotate(360deg);
  }
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
  border-color: var(--color-accent-manus);
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
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
  .messages {
    padding: 12px;
  }
}
</style>
