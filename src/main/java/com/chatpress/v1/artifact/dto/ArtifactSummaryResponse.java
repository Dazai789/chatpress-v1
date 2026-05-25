package com.chatpress.v1.artifact.dto;

import java.time.LocalDateTime;

import com.chatpress.v1.artifact.Artifact;

public record ArtifactSummaryResponse(
        Long id,
        String title,
        String slug,
        String sourceType,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ArtifactSummaryResponse from(Artifact artifact) {
        return new ArtifactSummaryResponse(
                artifact.getId(),
                artifact.getTitle(),
                artifact.getSlug(),
                artifact.getSourceType().name().toLowerCase(),
                artifact.getStatus().name().toLowerCase(),
                artifact.getCreatedAt(),
                artifact.getUpdatedAt()
        );
    }
}
