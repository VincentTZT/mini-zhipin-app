package com.cn.past.time.model.payload;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MiniZhiPinPayload(
        @NotNull
        Method method,
        @NotNull
        String targetUrl,
        Map<String, Object> params,
        Map<String, String> headers
) {
    public enum Method {
        GET,
        POST,
        PUT
    }
}
