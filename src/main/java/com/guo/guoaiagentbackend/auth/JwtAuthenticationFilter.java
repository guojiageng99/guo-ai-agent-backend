package com.guo.guoaiagentbackend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Spring 6.x 默认 {@link OncePerRequestFilter#shouldNotFilterAsyncDispatch()} 为 {@code true}，
     * 异步分派（Flux/SSE 等）会跳过本过滤器，导致 JWT 未注入、{@code AuthorizationFilter} 报 Access Denied。
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    String username = jwtService.extractUsername(token);
                    // 异步分派（SSE/Flux）时 AnonymousAuthenticationFilter 可能已写入匿名身份，
                    // 若仍要求 getAuthentication()==null 则不会解析 Bearer，导致 AuthorizationFilter 拒绝。
                    if (username != null && jwtService.isTokenValid(token, username)) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception ignored) {
                    // 非法 token：不设置认证，受保护资源将返回 401
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
