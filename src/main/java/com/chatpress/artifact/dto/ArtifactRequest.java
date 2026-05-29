package com.chatpress.artifact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtifactRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        String sourceContent
) {
}
