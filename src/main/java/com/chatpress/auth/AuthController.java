package com.chatpress.auth;

import com.chatpress.auth.dto.LoginRequest;
import com.chatpress.auth.dto.LoginResponse;
import com.chatpress.auth.dto.RegisterRequest;
import com.chatpress.auth.exception.AuthenticationException;
import com.chatpress.auth.exception.DuplicateUsernameException;
import com.chatpress.security.JwtUtil;
import com.chatpress.auth.User;
import com.chatpress.auth.UserRepository;

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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new DuplicateUsernameException(request.username());
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                "USER"
        );
        user = userRepository.save(user);

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        );
    }

    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}
