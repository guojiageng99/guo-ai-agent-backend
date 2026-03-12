<template>
  <div class="chat-page">
    <header class="chat-header">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <h1>AI 恋爱大师</h1>
      <span class="chat-id">会话: {{ chatId }}</span>
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
import { ref, onMounted, nextTick } from 'vue'
import { generateChatId } from '../utils/chatId'
import { chatWithLoveAppSse } from '../utils/sse'

const chatId = ref('')
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
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

  const aiMessage = { role: 'assistant', content: '' }
  messages.value.push(aiMessage)

  await nextTick()
  scrollToBottom()

  abortController = chatWithLoveAppSse(
    text,
    chatId.value,
    {
      onChunk: (chunk) => {
        aiMessage.content += chunk
        nextTick(scrollToBottom)
      },
      onComplete: () => {
        loading.value = false
      },
      onError: (err) => {
        aiMessage.content += `\n[错误: ${err.message}]`
        loading.value = false
      }
    }
  )
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.chat-header {
  padding: 16px 24px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #fff;
  text-decoration: none;
  font-size: 0.95rem;
}

.back-btn:hover {
  text-decoration: underline;
}

.chat-header h1 {
  flex: 1;
  font-size: 1.25rem;
}

.chat-id {
  font-size: 0.8rem;
  opacity: 0.9;
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

.message.user .message-content {
  flex-direction: row-reverse;
}

.message.user .bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  margin-left: 0;
  margin-right: 12px;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 85%;
}

.message.user .message-content {
  margin-left: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.message.user .avatar {
  background: #764ba2;
  color: #fff;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.input-area {
  padding: 16px 24px 24px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-area textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 12px;
  font-size: 1rem;
  resize: none;
  font-family: inherit;
}

.input-area textarea:focus {
  outline: none;
  border-color: #667eea;
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  cursor: pointer;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
