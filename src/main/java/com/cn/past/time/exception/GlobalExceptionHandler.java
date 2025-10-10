package com.cn.past.time.exception;

import com.cn.past.time.util.Const;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private JavaMailSender emailSender;

    @ExceptionHandler(MiniZhipinException.class)
    public ResponseEntity<ExceptionVo> handleException(MiniZhipinException e, HttpServletRequest request) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        log.error("request url {}, phone {}, facing captured error.", request.getRequestURI(), phone);
        sendSimpleMessage(e.getHttpStatus().value(), request.getRequestURI() + "\n\n" + e.getMessage(), phone);
        return ResponseEntity.status(e.getHttpStatus()).body(new ExceptionVo(
                e.getHttpStatus().value(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionVo> handleException(Exception e, HttpServletRequest request) {
        String phone = request.getHeader(Const.ZHIPIN_PHONE);
        log.error("request url {}, phone {}, facing unexpected error.", request.getRequestURI(), phone);
        sendSimpleMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI() + "\n\n" + getRootCauseMessage(e), phone);
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

    private void sendSimpleMessage(int code, String text, String phone) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("zhipin_mini_app@126.com");
            message.setTo("13538909905@139.com");
            message.setSubject("ZhiPin Mini App Hit Error: " + code + ", phone: " + phone);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception e) {
//            log.error("send email error, phone {}, code {}, text {}", phone, code, text, e);
        }
    }
}
