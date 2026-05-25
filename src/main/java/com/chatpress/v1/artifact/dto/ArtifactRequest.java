package com.chatpress.v1.artifact.dto;

import com.chatpress.v1.artifact.Artifact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record ArtifactRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 200)
        String slug,

        @Pattern(regexp = "markdown|ai_chat")
        String sourceType,

        @NotBlank
        String sourceContent
) {

    public Artifact.SourceType toArtifactSourceType() {
        if (sourceType == null) {
            return Artifact.SourceType.MARKDOWN;
        }
        return Artifact.SourceType.valueOf(sourceType.toUpperCase(Locale.ROOT));
    }
}
