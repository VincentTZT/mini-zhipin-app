package com.cn.past.time;

import com.cn.past.time.model.response.AccountDto;
import com.cn.past.time.model.service.AccountBo;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableCaching
@EnableAspectJAutoProxy
@SpringBootApplication
@RegisterReflectionForBinding({AccountBo.class, AccountDto.class})
public class MiniZhipinAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniZhipinAppApplication.class, args);
    }

}
