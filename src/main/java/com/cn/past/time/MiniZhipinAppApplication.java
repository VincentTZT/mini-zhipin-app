package com.cn.past.time;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableCaching
@EnableAspectJAutoProxy
@SpringBootApplication
public class MiniZhipinAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniZhipinAppApplication.class, args);
    }

}
