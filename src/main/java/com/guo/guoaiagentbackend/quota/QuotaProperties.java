package com.guo.guoaiagentbackend.quota;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 公网演示防刷：按用户每日 AI 调用上限、按 IP 每日注册上限。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.quota")
public class QuotaProperties {

    /**
     * 是否启用配额（本地调试可关）
     */
    private boolean enabled = true;

    /**
     * 恋语 AI：每用户每自然日请求上限（仅 /ai/love_app/**）
     */
    private int loveAppRequestsPerUserPerDay = 8;

    /**
     * 超级智能体 Manus：每用户每自然日请求上限（仅 /ai/manus/**）
     */
    private int manusRequestsPerUserPerDay = 8;

    /**
     * 每公网 IP 每自然日允许成功注册的次数（防多号绕过人限）
     */
    private int registrationsPerIpPerDay = 2;

    public int limitFor(AiQuotaKind kind) {
        return kind == AiQuotaKind.MANUS ? manusRequestsPerUserPerDay : loveAppRequestsPerUserPerDay;
    }
}
