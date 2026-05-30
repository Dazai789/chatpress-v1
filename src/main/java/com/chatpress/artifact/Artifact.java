package com.chatpress.artifact;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "artifact")
public class Artifact {

    public enum Status {
        DRAFT,
        PUBLISHED;

        public static java.util.Optional<Status> fromString(String value) {
            if (value == null || value.isBlank()) {
                return java.util.Optional.empty();
            }
            try {
                return java.util.Optional.of(Status.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return java.util.Optional.empty();
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false, length = 50)
    private String sourceFormat = "markdown";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceContent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String renderedHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status = Status.DRAFT;

    @TableField(fill = FieldFill.INSERT)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 50)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "artifact_tag",
            joinColumns = @JoinColumn(name = "artifact_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    protected Artifact() {
    }

    public Artifact(String title, String slug, String sourceContent, String renderedHtml, String createdBy) {
        this.title = title;
        this.slug = slug;
        this.sourceContent = sourceContent;
        this.renderedHtml = renderedHtml;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public String getRenderedHtml() {
        return renderedHtml;
    }

    public void setRenderedHtml(String renderedHtml) {
        this.renderedHtml = renderedHtml;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }
}
