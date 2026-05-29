package com.chatpress.artifact.dto;

import java.time.LocalDateTime;

import com.chatpress.artifact.Artifact;

public record ArtifactSummaryResponse(
        Long id,
        String title,
        String slug,
        String sourceFormat,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ArtifactSummaryResponse from(Artifact artifact) {
        return new ArtifactSummaryResponse(
                artifact.getId(),
                artifact.getTitle(),
                artifact.getSlug(),
                artifact.getSourceFormat(),
                artifact.getStatus().name().toLowerCase(),
                artifact.getCreatedAt(),
                artifact.getUpdatedAt()
        );
    }
}
