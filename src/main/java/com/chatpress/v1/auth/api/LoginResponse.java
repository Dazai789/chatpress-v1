package com.chatpress.v1.auth.api;

public record LoginResponse(
        String token,
        String username,
        String role
) {}
