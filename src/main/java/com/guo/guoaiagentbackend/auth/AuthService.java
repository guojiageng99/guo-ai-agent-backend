package com.guo.guoaiagentbackend.auth;

import com.guo.guoaiagentbackend.exception.BusinessException;
import com.guo.guoaiagentbackend.exception.ErrorCode;
import com.guo.guoaiagentbackend.user.AppUser;
import com.guo.guoaiagentbackend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }
        String hash = passwordEncoder.encode(password);
        long id = userRepository.insert(username, hash);
        String token = jwtService.generateToken(id, username);
        return Map.of("token", token, "username", username);
    }

    public Map<String, Object> login(String username, String password) {
        AppUser user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        String token = jwtService.generateToken(user.id(), user.username());
        return Map.of("token", token, "username", user.username());
    }
}
