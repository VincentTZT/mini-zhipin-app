package com.cn.part.time.exception;

import com.cn.part.time.service.EmailService;
import com.cn.part.time.util.Const;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final EmailService emailService;

    @ExceptionHandler(MiniZhipinException.class)
    public ResponseEntity<ExceptionVo> handleException(MiniZhipinException e, HttpServletRequest request) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        log.error("request url {}, phone {}, facing captured error.", request.getRequestURI(), phone);
        emailService.sendSimpleMessage(e.getHttpStatus().value(), request.getRequestURI() + "\n\n" + e.getMessage(), phone);
        return ResponseEntity.status(e.getHttpStatus()).body(new ExceptionVo(
                e.getHttpStatus().value(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionVo> handleException(Exception e, HttpServletRequest request) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        log.error("request url {}, phone {}, facing unexpected error.", request.getRequestURI(), phone);
        emailService.sendSimpleMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI() + "\n\n" + getRootCauseMessage(e), phone);
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
