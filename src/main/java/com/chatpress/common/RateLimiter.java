package com.chatpress.common;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final Map<String, WindowState> windows = new ConcurrentHashMap<>();

    public boolean allow(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;

        WindowState state = windows.compute(key, (k, current) -> {
            if (current == null || now - current.windowStart > windowMs) {
                return new WindowState(1, now);
            }
            if (current.count > maxRequests) {
                return current;
            }
            return new WindowState(current.count + 1, current.windowStart);
        });

        return state.count <= maxRequests;
    }

    @Scheduled(fixedRate = 300_000)
    public void cleanExpiredWindows() {
        long now = System.currentTimeMillis();
        long maxWindowMs = 120_000; // 2 minutes, longer than any configured window
        windows.entrySet().removeIf(entry ->
                now - entry.getValue().windowStart > maxWindowMs);
    }

    private static class WindowState {
        final int count;
        final long windowStart;

        WindowState(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
