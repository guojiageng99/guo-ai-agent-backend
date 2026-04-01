package com.guo.guoaiagentbackend.quota;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    /**
     * 优先 X-Forwarded-For 第一个地址（反代场景），否则 remoteAddr。
     */
    public static String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            String first = forwarded.split(",")[0].trim();
            if (StringUtils.hasText(first)) {
                return first;
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        String addr = request.getRemoteAddr();
        return StringUtils.hasText(addr) ? addr : "unknown";
    }
}
