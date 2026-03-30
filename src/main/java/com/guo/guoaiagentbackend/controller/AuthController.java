package com.guo.guoaiagentbackend.controller;

import com.guo.guoaiagentbackend.auth.AuthService;
import com.guo.guoaiagentbackend.auth.dto.LoginRequest;
import com.guo.guoaiagentbackend.auth.dto.RegisterRequest;
import com.guo.guoaiagentbackend.common.BaseResponse;
import com.guo.guoaiagentbackend.common.ResultUtils;
import com.guo.guoaiagentbackend.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public BaseResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ResultUtils.success(authService.register(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ResultUtils.success(authService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/me")
    public BaseResponse<?> me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(Map.of("username", authentication.getName()));
    }
}
