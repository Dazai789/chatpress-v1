package com.chatpress.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static String csrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        return token != null ? token.getToken() : "";
    }
}
