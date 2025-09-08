package com.cn.past.time;

import com.cn.past.time.model.response.AccountDto;
import com.cn.past.time.model.service.AccountBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@EnableCaching
@EnableAspectJAutoProxy
@SpringBootApplication
@RegisterReflectionForBinding({AccountBo.class, AccountDto.class})
public class MiniZhipinAppApplication {
    private static Instant startTime;
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        startTime = Instant.now();
        SpringApplication app = new SpringApplication(MiniZhipinAppApplication.class);
        context = app.run(args);
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES) // 每30分钟检查一次
    public void checkRunningTime() {
        Duration runningTime = Duration.between(startTime, Instant.now());
//        log.info("应用已运行 {} 分钟", runningTime.toMinutes());

        if (runningTime.toMinutes() >= 359) {
            log.warn("应用已运行满6小时，即将自动关闭...");
            // 优雅关闭应用
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 给1秒时间让当前请求完成
                    context.close();    // 关闭应用上下文
                    System.exit(0);     // 退出JVM
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
