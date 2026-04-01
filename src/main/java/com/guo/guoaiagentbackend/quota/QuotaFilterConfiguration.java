package com.guo.guoaiagentbackend.quota;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuotaFilterConfiguration {

    @Bean
    public AiUsageQuotaFilter aiUsageQuotaFilter(
            QuotaService quotaService, QuotaProperties quotaProperties, ObjectMapper objectMapper) {
        return new AiUsageQuotaFilter(quotaService, quotaProperties, objectMapper);
    }
}
