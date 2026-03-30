<template>
  <div class="login-page">
    <div class="grid-bg" aria-hidden="true"></div>
    <div class="panel">
      <router-link to="/" class="back">← 返回首页</router-link>
      <h1 class="title">{{ mode === 'login' ? '登录' : '注册' }}</h1>
      <p class="hint">使用账号后可使用恋语 AI 与超级智能体</p>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span>用户名</span>
          <input v-model.trim="username" type="text" autocomplete="username" maxlength="64" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
            maxlength="64"
            required
          />
        </label>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="submit" :disabled="loading">
          {{ loading ? '请稍候…' : mode === 'login' ? '登录' : '注册并登录' }}
        </button>
      </form>

      <p class="switch">
        <template v-if="mode === 'login'">
          没有账号？
          <button type="button" class="link" @click="mode = 'register'; errorMsg = ''">去注册</button>
        </template>
        <template v-else>
          已有账号？
          <button type="button" class="link" @click="mode = 'login'; errorMsg = ''">去登录</button>
        </template>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as authApi from '../api/auth'
import { setAuth } from '../utils/authToken'

const route = useRoute()
const router = useRouter()

const mode = ref('login')
const username = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function onSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    const fn = mode.value === 'login' ? authApi.login : authApi.register
    const data = await fn(username.value, password.value)
    if (data && data.token) {
      setAuth(data.token, data.username || username.value)
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
      router.replace(redirect || '/')
    }
  } catch (e) {
    errorMsg.value = e?.message || '操作失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

.panel {
  position: relative;
  width: 100%;
  max-width: 400px;
  padding: 32px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-glow);
}

.back {
  display: inline-block;
  margin-bottom: 20px;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  text-decoration: none;
}

.back:hover {
  color: var(--color-accent);
}

.title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  margin-bottom: 8px;
  color: var(--color-text);
}

.hint {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin-bottom: 24px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.field input {
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
  color: var(--color-text);
  font-size: 1rem;
}

.field input:focus {
  outline: none;
  border-color: var(--color-accent);
}

.error {
  font-size: 0.85rem;
  color: #f472b6;
  margin: 0;
}

.submit {
  margin-top: 8px;
  padding: 12px;
  border: none;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-accent), #0891b2);
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  font-family: var(--font-display);
}

.submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.switch {
  margin-top: 20px;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  text-align: center;
}

.link {
  background: none;
  border: none;
  padding: 0;
  color: var(--color-accent);
  cursor: pointer;
  font-size: inherit;
  text-decoration: underline;
}
</style>
