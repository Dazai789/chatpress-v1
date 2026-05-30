package com.chatpress.auth;

import com.chatpress.auth.dto.LoginRequest;
import com.chatpress.auth.dto.LoginResponse;
import com.chatpress.auth.dto.RegisterRequest;
import com.chatpress.auth.exception.AuthenticationException;
import com.chatpress.auth.exception.DuplicateUsernameException;
import com.chatpress.security.JwtUtil;
import com.chatpress.auth.User;
import com.chatpress.auth.UserMapper;

import com.chatpress.common.annotation.RateLimit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        if (userMapper.findByUsername(request.username()).isPresent()) {
            throw new DuplicateUsernameException(request.username());
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                "USER"
        );
        userMapper.insert(user);

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        );
    }

    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userMapper.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}
