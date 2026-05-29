package com.chatpress.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String code,
        String message,
        Map<String, String> fields
) {

    public ApiErrorResponse(String code, String message) {
        this(code, message, null);
    }
}
