package com.chatpress.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Void> home() {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "chatpress-v1"
        );
    }
}
