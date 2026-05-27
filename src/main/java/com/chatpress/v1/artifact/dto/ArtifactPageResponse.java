package com.chatpress.v1.artifact.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ArtifactPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {

    public static <T> ArtifactPageResponse<T> from(Page<T> page) {
        return new ArtifactPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
