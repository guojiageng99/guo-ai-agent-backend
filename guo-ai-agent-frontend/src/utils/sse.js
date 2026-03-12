// 开发环境使用相对路径走 Vite 代理，生产环境使用完整地址
const API_BASE = import.meta.env.DEV ? '/api' : 'http://localhost:8123/api'

/**
 * 调用 Love App SSE 接口，流式获取对话内容
 * @param {string} message - 用户消息
 * @param {string} chatId - 聊天室 ID
 * @param {function} onChunk - 每收到一块数据时的回调
 * @param {function} onComplete - 完成时的回调
 * @param {function} onError - 错误时的回调
 */
export function chatWithLoveAppSse(message, chatId, { onChunk, onComplete, onError }) {
  const url = `${API_BASE}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return fetchSse(url, { onChunk, onComplete, onError })
}

/**
 * 调用 Manus 超级智能体 SSE 接口
 * @param {string} message - 用户消息
 * @param {function} onChunk - 每收到一块数据时的回调
 * @param {function} onComplete - 完成时的回调
 * @param {function} onError - 错误时的回调
 */
export function chatWithManusSse(message, { onChunk, onComplete, onError }) {
  const url = `${API_BASE}/ai/manus/chat?message=${encodeURIComponent(message)}`
  return fetchSse(url, { onChunk, onComplete, onError })
}

/**
 * 通用 SSE 请求处理
 */
function fetchSse(url, { onChunk, onComplete, onError }) {
  const controller = new AbortController()
  const signal = controller.signal

  fetch(url, {
    method: 'GET',
    signal,
    headers: {
      Accept: 'text/event-stream'
    }
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data && onChunk) {
              onChunk(data)
            }
          }
        }
      }

      if (buffer.startsWith('data:')) {
        const data = buffer.slice(5).trim()
        if (data && onChunk) {
          onChunk(data)
        }
      }

      if (onComplete) onComplete()
    })
    .catch((err) => {
      if (err.name !== 'AbortError' && onError) {
        onError(err)
      }
    })

  return {
    abort: () => controller.abort()
  }
}
