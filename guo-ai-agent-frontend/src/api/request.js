import axios from 'axios'

// 开发环境使用相对路径走 Vite 代理
// const baseURL = import.meta.env.DEV ? '/api' : 'http://localhost:8123/api'
const baseURL = import.meta.env.DEV ? '/api' : '/api'

const request = axios.create({
  baseURL,
  timeout: 60000
})

/**
 * 与后端 BaseResponse 对齐：成功时返回 data，业务失败时 reject
 */
request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === 0) {
        return payload.data
      }
      const msg = payload.message != null ? String(payload.message) : '请求失败'
      return Promise.reject(new Error(msg))
    }
    return response.data
  },
  (error) => {
    const res = error.response?.data
    if (res && typeof res === 'object' && 'message' in res) {
      return Promise.reject(new Error(String(res.message)))
    }
    return Promise.reject(error)
  }
)

export default request
