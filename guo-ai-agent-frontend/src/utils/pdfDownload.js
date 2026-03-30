import axios from 'axios'
import { getToken } from './authToken'

/** 匹配消息中的 PDF 下载路径（相对或绝对） */
const PDF_PATH_RE = /(\/api\/files\/pdf\/[^\s]+|https?:\/\/[^\s]+\/api\/files\/pdf\/[^\s]+)/gi

/**
 * 模型常在 URL 后直接接中文句号、右括号等；[^\s]+ 会把它们吃进链接，导致请求 400/403。
 */
export function normalizePdfLink(raw) {
  if (raw == null || raw === '') {
    return raw
  }
  let s = raw.trim()
  // 中文标点 + 英文标点/括号（模型常把句号、右括号紧贴在 URL 后）
  s = s.replace(/[。，、；：．]+$/u, '')
  s = s.replace(/[.,;:!?)\]}>"'`「」『』（）【】《》〈〉]+$/u, '')
  return s
}

/**
 * 将文本拆成普通片段与 PDF 链接片段，供 Manus 气泡渲染「下载」按钮。
 */
export function parseTextWithPdfLinks(text) {
  if (text == null || text === '') {
    return [{ type: 'text', text: '' }]
  }
  const parts = []
  let last = 0
  let m
  const re = new RegExp(PDF_PATH_RE.source, PDF_PATH_RE.flags)
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) {
      parts.push({ type: 'text', text: text.slice(last, m.index) })
    }
    parts.push({ type: 'pdf', url: normalizePdfLink(m[1]) })
    last = m.index + m[1].length
  }
  if (last < text.length) {
    parts.push({ type: 'text', text: text.slice(last) })
  }
  return parts.length ? parts : [{ type: 'text', text }]
}

/**
 * 使用当前登录 JWT 拉取 PDF 并触发浏览器下载（直接打开链接无法带 Authorization）。
 */
export async function downloadAuthenticatedPdf(pathOrUrl) {
  let path = normalizePdfLink(pathOrUrl)
  if (/^https?:\/\//i.test(path)) {
    const u = new URL(path)
    path = u.pathname + u.search
  }
  if (!path.startsWith('/')) {
    path = `/${path}`
  }
  const token = getToken()
  const res = await axios.get(path, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
  let fileName = 'document.pdf'
  const cd = res.headers['content-disposition']
  if (cd) {
    const star = cd.match(/filename\*=UTF-8''([^;\s]+)/i)
    const quoted = cd.match(/filename="([^"]+)"/i)
    if (star) {
      try {
        fileName = decodeURIComponent(star[1])
      } catch {
        /* ignore */
      }
    } else if (quoted) {
      fileName = quoted[1]
    }
  }
  if (fileName === 'document.pdf') {
    const seg = path.split('/').filter(Boolean).pop()
    if (seg) {
      try {
        fileName = decodeURIComponent(seg.split('?')[0])
      } catch {
        fileName = seg.split('?')[0]
      }
    }
  }
  const blobUrl = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = blobUrl
  a.download = fileName
  a.rel = 'noopener'
  a.click()
  URL.revokeObjectURL(blobUrl)
}
