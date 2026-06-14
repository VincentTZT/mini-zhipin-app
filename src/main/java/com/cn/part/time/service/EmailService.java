package com.cn.part.time.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;

    /**
     * 异常邮件发送计数器（使用AtomicInteger保证线程安全），超过10次自动退出程序
     */
    private final AtomicInteger sendEmailCount = new AtomicInteger(0);
    private static final int MAX_EMAIL_COUNT = 5;

    public void sendSimpleMessage(int code, String text, String phone) {
        // 使用AtomicInteger的incrementAndGet保证原子性操作
        int currentCount = sendEmailCount.incrementAndGet();
        // 检查是否超过最大邮件发送次数
        if (currentCount > MAX_EMAIL_COUNT) {
            text += "\n\n 邮件发送次数已达" + MAX_EMAIL_COUNT + "次，程序即将退出。";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("zhipin_mini_app@126.com");
            message.setTo("13538909905@139.com");
            message.setSubject("ZhiPin Mini App Hit Error: " + code + ", phone: " + phone);
            message.setText(text);
            emailSender.send(message);

            if (currentCount > MAX_EMAIL_COUNT) {
                log.info("========== 程序即将在30秒后自动退出 ==========");
                log.info("退出原因：邮件发送次数已达{}次，程序即将退出", MAX_EMAIL_COUNT);

                try {
                    Thread.sleep(10000); // 延迟10秒再退出
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("========== 程序开始退出 ==========");
                System.exit(0);
            }
        } catch (Exception e) {
//            log.error("send email error, phone {}, code {}, text {}", phone, code, text, e);
        }
    }
}
