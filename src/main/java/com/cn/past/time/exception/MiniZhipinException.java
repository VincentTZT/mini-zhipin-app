package com.cn.past.time.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MiniZhipinException extends RuntimeException {
    private final HttpStatus httpStatus;

    public MiniZhipinException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public MiniZhipinException(HttpStatus httpStatus, Exception e) {
        super(e);
        this.httpStatus = httpStatus;
    }
}
