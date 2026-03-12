<template>
  <div class="chat-page">
    <header class="chat-header">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <h1>AI 超级智能体</h1>
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
            <div class="bubble">{{ msg.content }}</div>
          </div>
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
import { ref, nextTick } from 'vue'
import { chatWithManusSse } from '../utils/sse'

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

  abortController = chatWithManusSse(text, {
    onChunk: (chunk) => {
      if (chunk && chunk.trim()) {
        messages.value.push({ role: 'assistant', content: chunk.trim() })
        nextTick(scrollToBottom)
      }
    },
    onComplete: () => {
      loading.value = false
      nextTick(scrollToBottom)
    },
    onError: (err) => {
      messages.value.push({ role: 'assistant', content: `[错误: ${err.message}]` })
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
</script>

<style scoped>
.chat-page {
  flex: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
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
  font-family: var(--font-display);
  font-size: 1.15rem;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.input-area {
  padding: 16px 24px 24px;
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
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
