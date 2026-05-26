package com.cn.part.time;

import com.cn.part.time.model.payload.MiniZhiPinPayload;
import com.cn.part.time.model.response.AccountDto;
import com.cn.part.time.model.service.AccountBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableCaching
@EnableAspectJAutoProxy
@SpringBootApplication
@RegisterReflectionForBinding({AccountBo.class, AccountDto.class, MiniZhiPinPayload.class})
public class MiniZhipinAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniZhipinAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner shutdownRunner() {
        log.info("========== 程序已预设3小时后自动退出 ==========");
        return args -> {
            try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
                // 3小时后执行退出任务
                scheduler.schedule(() -> {
                    log.info("========== 程序即将在30秒后自动退出 ==========");
                    log.info("退出原因：已达到预设的运行时间（3小时）");

                    try {
                        Thread.sleep(30000); // 延迟30秒再退出
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    log.info("========== 程序开始退出 ==========");
                    System.exit(0);
                }, 3, TimeUnit.HOURS);
            }

            log.info("========== 自动退出定时任务已启动，将在3小时后退出 ==========");
        };
    }

}