package com.chatpress.common;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 255)
    private String target;

    @Column(nullable = false)
    private long durationMs;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OperationLog() {
    }

    public OperationLog(String username, String action, String target, long durationMs) {
        this.username = username;
        this.action = action;
        this.target = target;
        this.durationMs = durationMs;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getTarget() {
        return target;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
