const TOKEN_KEY = 'guo_ai_token'
const USERNAME_KEY = 'guo_ai_username'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setAuth(token, username) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  if (username != null) localStorage.setItem(USERNAME_KEY, username)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USERNAME_KEY)
}

export function getStoredUsername() {
  return localStorage.getItem(USERNAME_KEY) || ''
}

export function isLoggedIn() {
  return Boolean(getToken())
}

/** 401 等场景：清登录态并跳转登录页，避免与 router 循环依赖用 location */
export function redirectToLogin() {
  clearAuth()
  const path = window.location.pathname + window.location.search
  if (path.startsWith('/login')) return
  const redirect = encodeURIComponent(path || '/')
  window.location.assign(`/login?redirect=${redirect}`)
}
