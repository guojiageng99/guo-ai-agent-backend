# AI 智能体应用

基于 Spring AI + Vue3 的全栈 AI 应用，包含**恋语AI恋爱大师**情感咨询与**AI 超级智能体**多工具协作两大应用。

## 项目结构

```
guo-ai-agent-backend/
├── pom.xml                    # 后端 Maven 配置
├── src/                       # 后端 Spring Boot 源码
│   └── main/
│       ├── java/              # Java 源码
│       └── resources/         # 配置文件
├── guo-ai-agent-frontend/     # 前端 Vue3 项目
│   ├── package.json
│   ├── src/
│   └── dist/                  # 构建产物（部署用）
└── README.md
```

## 技术栈

### 后端
- **Spring Boot 3.4** + **Java 21**
- **Spring AI**：大模型集成（阿里云通义千问）
- **PGVector**：向量存储（RAG 知识库）
- **Spring AI MCP**：工具调用与 MCP 服务
- **Knife4j**：API 文档

### 前端
- **Vue 3** + **Vite 5**
- **Vue Router** + **Axios**
- 响应式布局，支持 PC / 平板 / 手机

## 功能模块

| 应用 | 说明 |
|------|------|
| 恋语AI恋爱大师 | 情感咨询，多轮对话，RAG 知识库，MCP 工具（如地图服务） |
| AI 超级智能体 | ReAct 自主规划，网页搜索、资源下载、PDF 生成等工具 |

## 环境要求

- **JDK 21**
- **Maven 3.6+**
- **Node.js 18+**、**npm**
- **PostgreSQL**（含 pgvector 扩展，用于 RAG 向量存储）

## 配置说明

### 后端配置

复制并编辑 `src/main/resources/application-local.yml`：

```yaml
# 阿里云 DashScope API Key（必填）
spring:
  ai:
    dashscope:
      api-key: "your-dashscope-api-key"

  # PostgreSQL 向量库（恋语AI 知识库）
  datasource:
    url: jdbc:postgresql://host:5432/yu_ai_agent
    username: postgres
    password: your-password

# 搜索 API（AI 超级智能体网页搜索）
search-api:
  api-key: your-search-api-key
```

默认激活 `local` 配置：`spring.profiles.active: local`。

### 前端配置

开发环境通过 Vite 代理访问后端，无需额外配置。生产环境需确保前端请求的 API 地址正确（`vite.config.js` 或环境变量）。

## 开发运行

### 1. 启动后端

```bash
cd guo-ai-agent-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

后端默认：`http://localhost:8123`，接口前缀 `/api`。

### 2. 启动前端

```bash
cd guo-ai-agent-frontend
npm install
npm run dev
```

前端默认：`http://localhost:5173`，代理 `/api` 到后端。

## 部署（Bash 命令行）

### 一、后端部署

```bash
# 1. 进入后端目录
cd guo-ai-agent-backend

# 2. 清理并打包（跳过测试可选）
mvn clean package -DskipTests

# 3. 运行 JAR
java -jar target/guo-ai-agent-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

# 或指定端口、配置文件
java -jar target/guo-ai-agent-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local \
  --server.port=8123
```

### 二、前端部署

```bash
# 1. 进入前端目录
cd guo-ai-agent-frontend

# 2. 安装依赖
npm install

# 3. 构建生产包
npm run build

# 4. 构建产物在 dist/ 目录，可部署到 Nginx 等静态服务器
```

### 三、Nginx 部署前后端（示例）

```bash
# 假设 Nginx 配置目录为 /etc/nginx/
# 将 dist 内容复制到 /var/www/ai-agent/
cp -r guo-ai-agent-frontend/dist/* /var/www/ai-agent/

# Nginx 配置示例（/etc/nginx/sites-available/ai-agent）：
# server {
#     listen 80;
#     server_name your-domain.com;
#     root /var/www/ai-agent;
#     index index.html;
#     location / {
#         try_files $uri $uri/ /index.html;
#     }
#     location /api {
#         proxy_pass http://127.0.0.1:8123;
#         proxy_http_version 1.1;
#         proxy_set_header Host $host;
#         proxy_set_header X-Real-IP $remote_addr;
#         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
#         proxy_set_header X-Forwarded-Proto $scheme;
#     }
# }
```

### 四、一键部署脚本示例

```bash
#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT"
FRONTEND_DIR="$PROJECT_ROOT/guo-ai-agent-frontend"

echo "=== 构建后端 ==="
cd "$BACKEND_DIR"
mvn clean package -DskipTests

echo "=== 构建前端 ==="
cd "$FRONTEND_DIR"
npm install
npm run build

echo "=== 部署完成 ==="
echo "后端 JAR: $BACKEND_DIR/target/guo-ai-agent-backend-0.0.1-SNAPSHOT.jar"
echo "前端静态: $FRONTEND_DIR/dist/"
```

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/love_app/chat/sse` | GET | 恋语AI 流式对话（SSE），参数：`message`、`chatId` |
| `/api/ai/manus/chat` | GET | AI 超级智能体流式对话（SSE），参数：`message` |

API 文档：`http://localhost:8123/api/doc.html`（Knife4j）

## 许可证

© AI 智能体应用 版权所有
