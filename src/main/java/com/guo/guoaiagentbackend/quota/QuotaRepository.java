package com.guo.guoaiagentbackend.quota;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuotaRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 按应用维度原子增加当日 AI 调用次数；若已达上限则返回 empty。
     */
    public List<Integer> incrementAiUsageIfBelowLimit(String username, String appScope, int maxPerDay) {
        return jdbcTemplate.query(
                """
                        INSERT INTO daily_ai_usage (username, usage_date, app_scope, request_count)
                        VALUES (?, CURRENT_DATE, ?, 1)
                        ON CONFLICT (username, usage_date, app_scope)
                        DO UPDATE SET request_count = daily_ai_usage.request_count + 1
                        WHERE daily_ai_usage.request_count < ?
                        RETURNING request_count
                        """,
                (rs, rowNum) -> rs.getInt("request_count"),
                username,
                appScope,
                maxPerDay);
    }

    /**
     * 原子增加当日该 IP 成功注册计数；若已达上限则返回 empty。
     */
    public List<Integer> incrementRegistrationIpIfBelowLimit(String clientIp, int maxPerDay) {
        return jdbcTemplate.query(
                """
                        INSERT INTO daily_ip_registration (client_ip, reg_date, register_count)
                        VALUES (?, CURRENT_DATE, 1)
                        ON CONFLICT (client_ip, reg_date)
                        DO UPDATE SET register_count = daily_ip_registration.register_count + 1
                        WHERE daily_ip_registration.register_count < ?
                        RETURNING register_count
                        """,
                (rs, rowNum) -> rs.getInt("register_count"),
                clientIp,
                maxPerDay);
    }
}
