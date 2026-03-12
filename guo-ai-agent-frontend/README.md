# AI 智能体前端

Vue3 + Vite 构建的 AI 应用前端，包含 AI 恋爱大师和 AI 超级智能体两个聊天应用。

## 功能

- **主页**：应用切换入口
- **AI 恋爱大师**：聊天室风格，SSE 流式对话，自动生成会话 ID
- **AI 超级智能体**：聊天室风格，SSE 流式对话

## 技术栈

- Vue 3
- Vue Router
- Axios
- Vite

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器（需确保后端 http://localhost:8123 已启动）
npm run dev
```

## 构建

```bash
npm run build
```

## 后端接口

- 接口前缀：`http://localhost:8123/api`
- Love App SSE：`GET /ai/love_app/chat/sse?message=xxx&chatId=xxx`
- Manus Chat：`GET /ai/manus/chat?message=xxx`
