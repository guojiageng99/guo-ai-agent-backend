package com.guo.guoaiagentbackend.quota;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(QuotaProperties.class)
public class QuotaConfiguration {
}
