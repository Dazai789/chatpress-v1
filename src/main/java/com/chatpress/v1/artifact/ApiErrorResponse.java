package com.chatpress.v1.artifact;

public record ApiErrorResponse(
        String code,
        String message
) {
}
