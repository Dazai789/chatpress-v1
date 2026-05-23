package com.chatpress.v1.artifact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtifactRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 200)
        String slug,

        @NotBlank
        String sourceContent
) {
}
