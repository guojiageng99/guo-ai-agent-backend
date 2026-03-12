import axios from 'axios'

// 开发环境使用相对路径走 Vite 代理
const baseURL = import.meta.env.DEV ? '/api' : 'http://localhost:8123/api'

const request = axios.create({
  baseURL,
  timeout: 60000
})

export default request
