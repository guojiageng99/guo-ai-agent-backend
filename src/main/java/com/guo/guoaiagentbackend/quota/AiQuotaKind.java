package com.guo.guoaiagentbackend.quota;

/**
 * AI 配额维度：恋语与 Manus 各自计数。
 */
public enum AiQuotaKind {

    LOVE("love"),
    MANUS("manus");

    private final String scope;

    AiQuotaKind(String scope) {
        this.scope = scope;
    }

    public String scope() {
        return scope;
    }

    /**
     * context-path 之后的 URI，例如 /ai/manus/chat、/ai/love_app/chat/sse
     */
    public static AiQuotaKind fromUriAfterContext(String uriAfterContext) {
        if (uriAfterContext != null && uriAfterContext.startsWith("/ai/manus")) {
            return MANUS;
        }
        return LOVE;
    }
}
