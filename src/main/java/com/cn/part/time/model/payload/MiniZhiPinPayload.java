package com.cn.part.time.model.payload;

import java.util.Map;

public record MiniZhiPinPayload(
        Method method,
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
