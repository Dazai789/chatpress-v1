package com.chatpress.artifact.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    public static <T> ArtifactPageResponse<T> from(IPage<T> page) {
        return new ArtifactPageResponse<>(
                page.getRecords(),
                (int) page.getCurrent() - 1, // MyBatis-Plus 1-based → 0-based
                (int) page.getSize(),
                page.getTotal(),
                (int) page.getPages()
        );
    }
}
