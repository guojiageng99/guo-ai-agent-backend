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
 * 持续接收文本碎片，按 SSE 格式解析（data: xxx）或作为原始文本流处理
 * 每次 onChunk 回调收到的碎片应在同一消息气泡中拼接，实现打字机效果
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
      let isSseFormat = null // null=未知, true=SSE格式, false=原始文本流

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk

        // 首次收到数据时判断格式
        if (isSseFormat === null && buffer.length > 0) {
          isSseFormat = buffer.includes('data:')
        }

        if (isSseFormat) {
          // SSE 格式：按 data: 行解析，同一事件内多行 data 需拼接
          const events = buffer.split(/\n\n+/)
          buffer = events.pop() || ''

          for (const eventBlock of events) {
            const dataParts = eventBlock
              .split('\n')
              .filter((line) => line.startsWith('data:'))
              .map((line) => line.slice(5).replace(/^ +| +$/g, ''))
            if (dataParts.length > 0 && onChunk) {
              onChunk(dataParts.join('\n'))
            }
          }
        } else {
          // 原始文本流：传递已缓冲的文本碎片，由调用方在同一气泡中拼接
          if (buffer && onChunk) {
            onChunk(buffer)
          }
          buffer = ''
        }
      }

      // 处理剩余 buffer
      if (buffer.length > 0) {
        if (isSseFormat) {
          const dataParts = buffer
            .split('\n')
            .filter((line) => line.startsWith('data:'))
            .map((line) => line.slice(5).replace(/^ +| +$/g, ''))
          if (dataParts.length > 0 && onChunk) {
            onChunk(dataParts.join('\n'))
          }
        } else if (onChunk) {
          onChunk(buffer)
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
