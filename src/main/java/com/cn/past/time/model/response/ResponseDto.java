package com.cn.past.time.model.response;

public record ResponseDto(
        boolean success,
        String message,
        Object body
) {
}
