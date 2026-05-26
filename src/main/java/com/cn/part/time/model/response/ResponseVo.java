package com.cn.part.time.model.response;

public record ResponseVo(
        boolean success,
        String message,
        Object msg
) {
}
