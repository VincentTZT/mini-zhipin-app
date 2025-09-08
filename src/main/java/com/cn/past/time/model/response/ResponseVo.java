package com.cn.past.time.model.response;

public record ResponseVo(
        boolean success,
        String message,
        Object body
) {
}
