# Guo AI Agent — 全栈 AI 智能体应用

基于 **Spring Boot**、**Spring AI** 与 **Vue 3** 的全栈项目，提供 **恋语 AI 恋爱大师**（情感咨询、RAG、工具调用）与 **AI 超级智能体 Manus**（多工具协作、流式输出）两大应用；内置 **JWT 鉴权**与 **公网演示配额**（按用户 / 按 IP），便于安全上线展示。

---

## 目录

- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [仓库结构](#仓库结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [认证与配额](#认证与配额)
- [数据库与建表](#数据库与建表)
- [API 与文档](#api-与文档)
- [生产部署](#生产部署)
- [常见问题](#常见问题)

---

## 功能概览

| 模块 | 说明 |
|------|------|
| **恋语 AI** | 多轮对话、SSE 流式输出、RAG 知识库（PGVector）、可选 MCP 工具（如地图） |
| **超级智能体 Manus** | 基于工具链的自主规划与执行，网页搜索、资源处理、PDF 等 |
| **用户系统** | 注册 / 登录、JWT 无状态鉴权；`/ai/**` 需登录访问 |
| **演示防护** | 恋语与 Manus **分别**每日调用上限；按公网 IP 限制每日注册次数 |

---

## 技术栈

### 后端

- Java **21**、Spring Boot **3.4**
- **Spring Security** + **JWT**（jjwt）
- **Spring AI**、**Spring AI Alibaba**（通义千问 DashScope）
- **PostgreSQL** + **pgvector**（向量检索）
- **Spring AI MCP Client**
- **Knife4j** / OpenAPI 3

### 前端

- **Vue 3**、**Vite 5**、**Vue Router**、**Axios**
- 响应式布局，适配桌面与移动端

---

## 仓库结构

```
guo-ai-agent-backend/
├── pom.xml                         # Maven 配置
├── src/main/java/                  # 后端源码（包：com.guo.guoaiagentbackend）
├── src/main/resources/
│   ├── application.yml             # 公共配置（含 app.quota 默认值）
│   └── application-local.yml       # 本地覆盖（密钥勿提交）
├── guo-ai-agent-frontend/          # Vue3 前端
│   ├── vite.config.js              # 开发代理 /api → 后端
│   ├── src/
│   └── dist/                       # 生产构建产物
└── README.md
```

---

## 环境要求

| 依赖 | 版本建议 |
|------|-----------|
| JDK | **21** |
| Maven | 3.6+ |
| Node.js | **18+**（npm） |
| PostgreSQL | 支持 **pgvector** 扩展的实例 |

---

## 快速开始

### 1. 准备数据库

创建数据库并启用 `pgvector`（具体命令依你的 PG 版本而定），在 `application-local.yml` 中配置 `spring.datasource.*`。

### 2. 本地配置

复制或编辑 `src/main/resources/application-local.yml`，至少配置：

- `spring.ai.dashscope.api-key`（或等价配置项）
- `spring.datasource.url` / `username` / `password`
- `app.jwt.secret`（生产须为足够长度的随机密钥）
- 超级智能体网页搜索等：`search-api.api-key`（若使用该能力）

**切勿将含真实密钥的 `application-local.yml` 推送到公开仓库。**

### 3. 启动后端

```bash
cd guo-ai-agent-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

- 监听端口：**8123**
- **Servlet context-path：`/api`**  
  即健康检查等为：`http://localhost:8123/api/health`（若已配置）

### 4. 启动前端

```bash
cd guo-ai-agent-frontend
npm install
npm run dev
```

- 开发地址：`http://localhost:5173`
- 请求 `/api` 由 Vite 代理至 `http://localhost:8123`

---

## 配置说明

### 默认 Profile

`application.yml` 中 `spring.profiles.active` 默认为 `local`，本地开发加载 `application-local.yml`。

### 业务相关

| 配置项 | 说明 |
|--------|------|
| `app.public-base-url` | 生产可填公网 API 根地址（无尾斜杠），供 PDF 等工具生成完整下载链接 |
| `app.rag.pgvector-enabled` | 是否启用本地 PGVector 相关能力（按环境调整） |

### 配额（`app.quota`）

在 `application.yml` 中可调整（亦可在 `application-local.yml` 覆盖）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enabled` | `true` | 总开关；本地调试可设为 `false` 关闭全部配额逻辑 |
| `love-app-requests-per-user-per-day` | `8` | 恋语 `/ai/love_app/**` 每用户每日次数 |
| `manus-requests-per-user-per-day` | `8` | Manus `/ai/manus/**` 每用户每日次数 |
| `registrations-per-ip-per-day` | `2` | 每 IP 每自然日允许**成功注册**次数 |

---

## 认证与配额

### 认证

- 注册：`POST /api/auth/register`
- 登录：`POST /api/auth/login`，返回 JWT
- 调用受保护接口时在 Header 携带：`Authorization: Bearer <token>`
- `/ai/**` 在 Spring Security 中要求已认证用户

### 配额行为摘要

- **AI 调用**：在 JWT 校验通过之后、进入 Controller 之前按路径区分 **恋语** / **Manus** 分别计数；超限返回 **HTTP 429**，消息体为业务 JSON（如「恋语 AI 今日次数已用完」/「超级智能体今日次数已用完」）。
- **注册**：在同一事务内先校验用户名，再按客户端 IP 占用「当日注册额度」，防止刷号；超限提示「本 IP 今日注册次数已达上限」。
- **客户端 IP**：由 `ClientIpResolver` 读取 `X-Forwarded-For`（取第一项）、`X-Real-IP`，最后回退 `remoteAddr`。反向代理须在 `location /api` 中设置：

```nginx
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
```

SSE 场景建议关闭 `proxy_buffering` 并适当增大读写超时（见下文部署示例）。

---

## 数据库与建表

应用启动时由 `QuotaSchemaInitializer` 执行 DDL（**无独立 Flyway 文件**），在已连接的数据库中自动维护：

| 表名 | 用途 |
|------|------|
| `daily_ai_usage` | 按 `username` + `usage_date` + `app_scope`（`love` / `manus`）统计当日调用次数 |
| `daily_ip_registration` | 按 `client_ip` + `reg_date` 统计当日成功注册次数 |

若曾使用旧版仅 `(username, usage_date)` 主键的 `daily_ai_usage`，启动时会将旧表重命名为 `daily_ai_usage_legacy` 并创建新表（历史计数不迁移）。

**查询示例：**

```sql
SELECT * FROM daily_ai_usage ORDER BY usage_date DESC, username;
SELECT * FROM daily_ip_registration ORDER BY reg_date DESC, client_ip;
```

---

## API 与文档

### 主要接口（均需登录，除 auth 外）

| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/auth/register` | POST | 注册 |
| `/api/auth/login` | POST | 登录 |
| `/api/auth/me` | GET | 当前用户 |
| `/api/ai/love_app/chat/sse` | GET | 恋语流式对话，Query：`message`、`chatId` |
| `/api/ai/love_app/chat/recommend_partner` | GET | 恋语对象推荐（RAG） |
| `/api/ai/manus/chat` | GET | Manus 流式对话，Query：`message` |

### 在线文档

启动后端后访问（路径以实际部署为准）：

- **Knife4j**：`http://localhost:8123/api/doc.html`

---

## 生产部署

### 后端

```bash
mvn clean package -DskipTests
java -jar target/guo-ai-agent-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

（建议使用独立 `application-prod.yml` 管理密钥与数据库，勿使用 `local` 配置上线。）

### 前端

```bash
cd guo-ai-agent-frontend
npm ci
npm run build
```

将 `dist/` 部署到 Nginx（或其它静态资源服务器）根目录，**SPA 回退**到 `index.html`。

### Nginx 要点示例

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    root /var/www/guo-ai-agent-frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8123;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffering off;
        proxy_connect_timeout 30s;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }
}
```

修改配置后执行 `nginx -t` 并重载 Nginx。

---

## 常见问题

**本地不想被配额限制？**  
在 `application-local.yml` 设置 `app.quota.enabled: false`。

**配额已开但 IP 不准？**  
确认最外层反向代理是否传入 `X-Forwarded-For` / `X-Real-IP`；若前方还有 CDN，需在 CDN 侧开启真实 IP 传递。

**SSE 中断或缓冲延迟？**  
确认 Nginx 对 `/api` 使用 `proxy_buffering off` 并足够大的 `proxy_read_timeout`。

---

## 许可证

本项目为私有/演示用途时请自行声明版权与使用范围；若开源请替换本节并补充 `LICENSE` 文件。
