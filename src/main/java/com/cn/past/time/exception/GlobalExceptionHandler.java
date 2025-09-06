package com.cn.past.time.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MiniZhipinException.class)
    public ResponseEntity<ExceptionVo> handleException(MiniZhipinException e, HttpServletRequest request) {
        log.error("request url {}, facing captured error.", request.getRequestURI());
        return ResponseEntity.status(e.getHttpStatus()).body(new ExceptionVo(
                e.getHttpStatus().value(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionVo> handleException(Exception e, HttpServletRequest request) {
        log.error("request url {}, facing unexpected error.", request.getRequestURI());
        return ResponseEntity.internalServerError().body(new ExceptionVo(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getRootCauseMessage(e)
        ));
    }

    private String getRootCauseMessage(Exception e) {
        if (e.getCause() == null) return e.getMessage();
        if (e.getCause().getCause() == null) return e.getMessage();
        return Objects.toString(e.getCause().getCause().getMessage(), e.getMessage());
    }
}
