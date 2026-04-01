package com.guo.guoaiagentbackend.quota;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guo.guoaiagentbackend.common.BaseResponse;
import com.guo.guoaiagentbackend.common.ResultUtils;
import com.guo.guoaiagentbackend.exception.BusinessException;
import com.guo.guoaiagentbackend.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 在 JWT 之后执行：已登录用户访问 /ai/** 时按应用维度扣减当日次数；超限时直接写 JSON，不进入 Controller。
 * 异步分派（如 SSE）默认不重复过滤，避免同一次请求扣两次。
 */
@RequiredArgsConstructor
public class AiUsageQuotaFilter extends OncePerRequestFilter {

    private final QuotaService quotaService;
    private final QuotaProperties quotaProperties;
    private final ObjectMapper objectMapper;

    private static boolean isUnderAiPath(HttpServletRequest request) {
        String ctx = request.getContextPath();
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith(ctx + "/ai/");
    }

    private static String uriAfterContext(HttpServletRequest request) {
        String ctx = request.getContextPath();
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith(ctx)) {
            return uri != null ? uri : "";
        }
        return uri.substring(ctx.length());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!quotaProperties.isEnabled() || !isUnderAiPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal().toString())) {
            filterChain.doFilter(request, response);
            return;
        }
        String username = auth.getName();
        AiQuotaKind kind = AiQuotaKind.fromUriAfterContext(uriAfterContext(request));
        try {
            quotaService.consumeAiRequestOrThrow(username, kind);
        } catch (BusinessException e) {
            response.setStatus(429);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            BaseResponse<?> body = ResultUtils.error(e.getCode(), e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            BaseResponse<?> body = ResultUtils.error(ErrorCode.SYSTEM_ERROR, "配额校验失败");
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
