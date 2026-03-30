import request from './request'

/**
 * 根据择偶/交友描述，RAG 推荐知识库中的意向对象（同步 JSON，非 SSE）
 */
export function recommendPartner(message, chatId) {
  return request.get('/ai/love_app/chat/recommend_partner', {
    params: { message, chatId }
  })
}
