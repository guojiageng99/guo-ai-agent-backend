package com.guo.guoaiagentbackend.quota;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 无 Flyway 时自动建表；表已存在则跳过。
 * 若存在旧版 daily_ai_usage（无主键 app_scope），则改名为 legacy 再建新表。
 */
@Component
@Order(0)
@RequiredArgsConstructor
public class QuotaSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                """
                        DO $$
                        BEGIN
                          IF EXISTS (
                            SELECT 1 FROM information_schema.tables t
                            WHERE t.table_schema = current_schema() AND t.table_name = 'daily_ai_usage'
                          ) AND NOT EXISTS (
                            SELECT 1 FROM information_schema.columns c
                            WHERE c.table_schema = current_schema()
                              AND c.table_name = 'daily_ai_usage'
                              AND c.column_name = 'app_scope'
                          ) THEN
                            ALTER TABLE daily_ai_usage RENAME TO daily_ai_usage_legacy;
                          END IF;
                        END $$
                        """);
        jdbcTemplate.execute(
                """
                        CREATE TABLE IF NOT EXISTS daily_ai_usage (
                            username VARCHAR(128) NOT NULL,
                            usage_date DATE NOT NULL,
                            app_scope VARCHAR(32) NOT NULL,
                            request_count INT NOT NULL,
                            PRIMARY KEY (username, usage_date, app_scope)
                        )
                        """);
        jdbcTemplate.execute(
                """
                        CREATE TABLE IF NOT EXISTS daily_ip_registration (
                            client_ip VARCHAR(128) NOT NULL,
                            reg_date DATE NOT NULL,
                            register_count INT NOT NULL,
                            PRIMARY KEY (client_ip, reg_date)
                        )
                        """);
    }
}
