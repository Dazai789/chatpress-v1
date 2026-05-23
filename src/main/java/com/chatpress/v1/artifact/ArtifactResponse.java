package com.chatpress.v1.artifact;

import java.time.LocalDateTime;

public record ArtifactResponse(
        Long id,
        String title,
        String slug,
        String sourceFormat,
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
                artifact.getSourceContent(),
                artifact.getRenderedHtml(),
                artifact.getStatus(),
                artifact.getCreatedAt(),
                artifact.getUpdatedAt()
        );
    }
}
