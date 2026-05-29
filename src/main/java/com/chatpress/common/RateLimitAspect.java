package com.chatpress.common;

import com.chatpress.common.annotation.RateLimit;
import com.chatpress.common.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimiter rateLimiter;

    public RateLimitAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Before("@annotation(com.chatpress.common.annotation.RateLimit)")
    public void checkRateLimit(org.aspectj.lang.JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit annotation = signature.getMethod().getAnnotation(RateLimit.class);

        String key = getClientIp();

        if (!rateLimiter.allow(key, annotation.maxRequests(), annotation.windowSeconds())) {
            throw new RateLimitExceededException(
                    "Too many requests. Limit: %d per %ds.".formatted(
                            annotation.maxRequests(), annotation.windowSeconds()));
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
