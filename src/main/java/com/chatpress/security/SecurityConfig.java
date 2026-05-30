package com.chatpress.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/p/**", "/api/health", "/h2-console/**", "/api/auth/**", "/swagger", "/swagger/**", "/api/docs", "/api/docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .permitAll()
                .defaultSuccessUrl("/admin/artifacts", true)
                .failureUrl("/login?error")
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (response.isCommitted()) {
                        return;
                    }
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
                    response.getWriter().write("""
                            <!doctype html>
                            <html lang="en">
                            <head>
                                <meta charset="utf-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1">
                                <title>Session Expired</title>
                                <style>
                                    body {
                                        margin: 0; padding: 56px 20px;
                                        background: #f5f5f2; color: #242424;
                                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                                        text-align: center;
                                    }
                                    a { color: #0f766e; }
                                </style>
                            </head>
                            <body>
                                <h1>Session Expired</h1>
                                <p>Your session has expired or the form token is invalid.</p>
                                <p><a href="/login">Return to login</a></p>
                            </body>
                            </html>
                            """);
                })
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/**")
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
