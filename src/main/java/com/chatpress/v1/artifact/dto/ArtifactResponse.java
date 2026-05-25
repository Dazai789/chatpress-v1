package com.chatpress.v1.artifact.dto;

import java.time.LocalDateTime;

import com.chatpress.v1.artifact.Artifact;

public record ArtifactResponse(
        Long id,
        String title,
        String slug,
        String sourceFormat,
        String sourceType,
        String sourceContent,
        String renderedHtml,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ArtifactResponse from(Artifact artifact) {
        return new ArtifactResponse(
                artifact.getId(),
                artifact.getTitle(),
                artifact.getSlug(),
                artifact.getSourceFormat(),
                artifact.getSourceType().name().toLowerCase(),
                artifact.getSourceContent(),
                artifact.getRenderedHtml(),
                artifact.getStatus().name().toLowerCase(),
                artifact.getCreatedAt(),
                artifact.getUpdatedAt()
        );
    }
}
